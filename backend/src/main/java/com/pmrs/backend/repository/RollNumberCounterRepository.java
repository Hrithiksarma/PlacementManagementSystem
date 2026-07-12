package com.pmrs.backend.repository;

import com.pmrs.backend.entity.RollNumberCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RollNumberCounterRepository extends JpaRepository<RollNumberCounter, Long> {

    /**
     * Fetches the counter row for the given department and batch year, applying a
     * {@code PESSIMISTIC_WRITE} lock ({@code SELECT ... FOR UPDATE}).
     *
     * <p><b>What this lock does:</b> InnoDB places a row-level exclusive lock on
     * the matching counter row.  Any other transaction that tries to read or
     * modify the same row blocks immediately — it queues behind the lock and
     * proceeds only after the current transaction commits or rolls back.
     *
     * <p><b>Why this is correct:</b> The only operation on this row is
     * "read → increment → save".  By locking the row for the duration of that
     * sequence, we guarantee that exactly one thread sees each value of
     * {@code lastSerial}, so the serial numbers it produces are always unique.
     *
     * <p><b>Propagation:</b> This method must be called from within an active
     * transaction ({@code @Transactional} on the caller).  The lock is held
     * until that outer transaction commits — which is also when the student
     * INSERT commits — so the counter advance and the student record are
     * always in sync.
     *
     * @param deptId    the department ID
     * @param batchYear the batch year (e.g. 2025)
     * @return the locked counter row, or empty if none exists yet for this group
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RollNumberCounter r " +
           "WHERE r.deptId = :deptId AND r.batchYear = :batchYear")
    Optional<RollNumberCounter> findByDeptIdAndBatchYearWithLock(
            @Param("deptId")    Integer deptId,
            @Param("batchYear") Integer batchYear
    );
}