package com.academic.service.service.impl;

import com.academic.service.entity.RollNumberCounter;
import com.academic.service.repository.RollNumberCounterRepository;
import com.academic.service.service.RollNumberService;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Primary
public class LocalRollNumberService implements RollNumberService {

    private static final Map<Integer, String> DEPT_CODE_MAP = Map.of(
        1, "111",
        2, "112",
        3, "121",
        4, "122",
        5, "211",
        6, "212",
        7, "221",
        8, "222"
    );

    private final RollNumberCounterRepository counterRepository;

    public LocalRollNumberService(RollNumberCounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @Override
    @Transactional
    public String generateRollNumber(Integer deptId, Integer admissionYear) {
        RollNumberCounter counter = getOrCreateCounter(deptId, admissionYear);
        counter.setLastSerial(counter.getLastSerial() + 1);
        counterRepository.save(counter);
        return String.format("%02d%s%02d",
            admissionYear % 100,
            resolveDeptCode(deptId),
            counter.getLastSerial()
        );
    }

    private RollNumberCounter getOrCreateCounter(Integer deptId, Integer admissionYear) {
        return counterRepository
            .findByDeptIdAndAdmissionYearWithLock(deptId, admissionYear)
            .orElseGet(() -> {
                try {
                    RollNumberCounter c = new RollNumberCounter();
                    c.setDeptId(deptId);
                    c.setAdmissionYear(admissionYear);
                    c.setLastSerial(0);
                    return counterRepository.saveAndFlush(c);
                } catch (DataIntegrityViolationException race) {
                    return counterRepository
                        .findByDeptIdAndAdmissionYearWithLock(deptId, admissionYear)
                        .orElseThrow(() -> new IllegalStateException(
                            "Counter not found after conflict for dept=" + deptId +
                            " year=" + admissionYear, race));
                }
            });
    }

    private String resolveDeptCode(Integer deptId) {
        String code = DEPT_CODE_MAP.get(deptId);
        if (code == null) {
            throw new IllegalArgumentException(
                "No department code defined for dept_id=" + deptId);
        }
        return code;
    }
}
