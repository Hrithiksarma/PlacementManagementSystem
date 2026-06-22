package com.pmrs.backend.repository;

import com.pmrs.backend.entity.EligibilityCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EligibilityCriteriaRepository extends JpaRepository<EligibilityCriteria, Integer> {

    List<EligibilityCriteria> findByDrive_DriveId(Integer driveId);
}
