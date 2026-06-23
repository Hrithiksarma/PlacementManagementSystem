package com.pmrs.backend.service;

import com.pmrs.backend.entity.EligibilityCriteria;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.EligibilityCriteriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EligibilityCriteriaServiceImpl implements EligibilityCriteriaService {

    private final EligibilityCriteriaRepository criteriaRepository;

    public EligibilityCriteriaServiceImpl(EligibilityCriteriaRepository criteriaRepository) {
        this.criteriaRepository = criteriaRepository;
    }

    @Override
    public List<EligibilityCriteria> getAllCriteria() {
        return criteriaRepository.findAll();
    }

    @Override
    public EligibilityCriteria getCriteriaById(Integer id) {
        return criteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eligibility criteria not found with id: " + id));
    }

    @Override
    public EligibilityCriteria saveCriteria(EligibilityCriteria criteria) {
        return criteriaRepository.save(criteria);
    }

    @Override
    public EligibilityCriteria updateCriteria(Integer id, EligibilityCriteria updated) {
        EligibilityCriteria existing = criteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eligibility criteria not found with id: " + id));
        existing.setDrive(updated.getDrive());
        existing.setMinCgpa(updated.getMinCgpa());
        existing.setMaxBacklogs(updated.getMaxBacklogs());
        existing.setAllowedTiers(updated.getAllowedTiers());
        existing.setAllowedDepartments(updated.getAllowedDepartments());
        return criteriaRepository.save(existing);
    }

    @Override
    public void deleteCriteria(Integer id) {
        criteriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eligibility criteria not found with id: " + id));
        criteriaRepository.deleteById(id);
    }

    @Override
    public List<EligibilityCriteria> getCriteriaByDriveId(Integer driveId) {
        return criteriaRepository.findByDrive_DriveId(driveId);
    }
}
