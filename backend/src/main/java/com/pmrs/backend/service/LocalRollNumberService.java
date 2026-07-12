package com.pmrs.backend.service;

import com.pmrs.backend.entity.RollNumberCounter;
import com.pmrs.backend.repository.RollNumberCounterRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Local implementation of {@link RollNumberService}.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Lock the counter row for {@code (deptId, batchYear)} using
 *       {@code SELECT ... FOR UPDATE}.</li>
 *   <li>Increment {@code lastSerial} by 1.</li>
 *   <li>Save the counter row (still inside the same transaction).</li>
 *   <li>Build and return the 7-character roll number string.</li>
 * </ol>
 *
 * <h2>Concurrency guarantee</h2>
 * InnoDB row locking ensures that only one transaction at a time can read and
 * increment the counter for a given {@code (deptId, batchYear)}.  Concurrent
 * requests queue behind the lock and execute serially — each sees a different
 * {@code lastSerial} and produces a unique roll number.
 *
 * <h2>Transaction participation</h2>
 * {@link #generateRollNumber} is annotated {@code @Transactional}.  With
 * default propagation ({@code REQUIRED}), it joins the caller's transaction
 * instead of opening a new one.  This means the counter increment and the
 * student {@code INSERT} commit together.  If the student save fails, the
 * counter rolls back — no serial is permanently wasted.
 *
 * <h2>Replacing this implementation</h2>
 * {@code @Primary} makes Spring inject this bean wherever {@link RollNumberService}
 * is required.  To hand responsibility to an Academic Service, annotate the new
 * implementation {@code @Primary} and remove it from this class.
 * {@code StudentService} does not change.
 */
@Service
@Primary
public class LocalRollNumberService implements RollNumberService {

    /**
     * Maps each {@code dept_id} to a 3-digit department code.
     *
     * <p>The code encodes three properties:
     * <ul>
     *   <li>Digit 1 — program  : 1 = B.Tech, 2 = M.Tech</li>
     *   <li>Digit 2 — branch   : 1 = CSE,    2 = ECE</li>
     *   <li>Digit 3 — section  : 1 = first section, 2 = second section</li>
     * </ul>
     *
     * <p>When a new department is added to the Departments table, add its
     * entry here before registering the first student.
     */
    private static final Map<Integer, String> DEPT_CODE_MAP = Map.of(
        1, "111",   // B.Tech CSE C13
        2, "112",   // B.Tech CSE C14
        3, "121",   // B.Tech ECE E13
        4, "122",   // B.Tech ECE E14
        5, "211",   // M.Tech CSE M13
        6, "212",   // M.Tech CSE M14
        7, "221",   // M.Tech ECE ME13
        8, "222"    // M.Tech ECE ME14
    );

    private final RollNumberCounterRepository counterRepository;

    public LocalRollNumberService(RollNumberCounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Locking:</b> the counter row is fetched with {@code PESSIMISTIC_WRITE}.
     * Concurrent requests for the same group queue at the database and proceed one
     * at a time — no application-level synchronisation required.
     */
    @Override
    @Transactional
    public String generateRollNumber(Integer deptId, Integer batchYear) {
        RollNumberCounter counter = getOrCreateCounter(deptId, batchYear);
        counter.setLastSerial(counter.getLastSerial() + 1);
        counterRepository.save(counter);
        return buildRollNumber(batchYear, deptId, counter.getLastSerial());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Returns the locked counter row for {@code (deptId, batchYear)}, creating
     * it if this is the first student in that group.
     *
     * <p>Creation is guarded against the race where two threads simultaneously
     * try to insert the first counter for a new group: the second thread will
     * hit the {@code UNIQUE} constraint, catch {@link DataIntegrityViolationException},
     * and re-read the row that the first thread just created — with the lock.
     */
    private RollNumberCounter getOrCreateCounter(Integer deptId, Integer batchYear) {
        return counterRepository
            .findByDeptIdAndBatchYearWithLock(deptId, batchYear)
            .orElseGet(() -> {
                try {
                    RollNumberCounter fresh = new RollNumberCounter();
                    fresh.setDeptId(deptId);
                    fresh.setBatchYear(batchYear);
                    fresh.setLastSerial(0);
                    // saveAndFlush writes immediately so the UNIQUE constraint is
                    // checked before any other thread tries the same insert.
                    return counterRepository.saveAndFlush(fresh);
                } catch (DataIntegrityViolationException raceCondition) {
                    // Another thread created this counter a moment earlier.
                    // Re-read with the lock and proceed normally.
                    return counterRepository
                        .findByDeptIdAndBatchYearWithLock(deptId, batchYear)
                        .orElseThrow(() -> new IllegalStateException(
                            "Counter disappeared after creation conflict for dept="
                            + deptId + " batch=" + batchYear, raceCondition));
                }
            });
    }

    /**
     * Assembles the 7-character roll number string from its three components.
     *
     * <p>Format: {@code [YY][DDD][NN]}
     * <ul>
     *   <li>{@code YY}  — {@code batchYear % 100}, zero-padded to 2 digits</li>
     *   <li>{@code DDD} — 3-digit code from {@link #DEPT_CODE_MAP}</li>
     *   <li>{@code NN}  — {@code serial}, zero-padded to 2 digits</li>
     * </ul>
     */
    private String buildRollNumber(Integer batchYear, Integer deptId, Integer serial) {
        return String.format("%02d%s%02d",
            batchYear % 100,
            resolveDeptCode(deptId),
            serial
        );
    }

    private String resolveDeptCode(Integer deptId) {
        String code = DEPT_CODE_MAP.get(deptId);
        if (code == null) {
            throw new IllegalArgumentException(
                "No department code registered for dept_id=" + deptId
                + ". Add an entry to LocalRollNumberService.DEPT_CODE_MAP.");
        }
        return code;
    }
}