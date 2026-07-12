package com.academic.service.repository;

import com.academic.service.entity.RollNumberCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RollNumberCounterRepository extends JpaRepository<RollNumberCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RollNumberCounter r " +
           "WHERE r.deptId = :deptId AND r.admissionYear = :admissionYear")
    Optional<RollNumberCounter> findByDeptIdAndAdmissionYearWithLock(
            @Param("deptId")        Integer deptId,
            @Param("admissionYear") Integer admissionYear
    );
}
