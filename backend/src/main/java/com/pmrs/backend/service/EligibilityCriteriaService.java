package com.pmrs.backend.service;

import com.pmrs.backend.entity.EligibilityCriteria;

import java.util.List;

public interface EligibilityCriteriaService {

    List<EligibilityCriteria> getAllCriteria();

    EligibilityCriteria getCriteriaById(Integer id);

    EligibilityCriteria saveCriteria(EligibilityCriteria criteria);

    EligibilityCriteria updateCriteria(Integer id, EligibilityCriteria criteria);

    void deleteCriteria(Integer id);

    List<EligibilityCriteria> getCriteriaByDriveId(Integer driveId);
}
