package com.pmrs.backend.service;

import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.DriveRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriveServiceImpl implements DriveService {

    private final DriveRepository driveRepository;

    public DriveServiceImpl(DriveRepository driveRepository) {
        this.driveRepository = driveRepository;
    }

    @Override
    public List<Drive> getAllDrives() {
        return driveRepository.findAll();
    }

    @Override
    public Drive getDriveById(Integer id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id: " + id));
    }

    @Override
    public Drive saveDrive(Drive drive) {
        Drive saved = driveRepository.save(drive);
        return driveRepository.findById(saved.getDriveId())
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));
    }

    @Override
    public Drive updateDrive(Integer id, Drive updated) {
        Drive existing = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id: " + id));
        existing.setCompany(updated.getCompany());
        existing.setHrContact(updated.getHrContact());
        existing.setDriveDate(updated.getDriveDate());
        existing.setRoleOffered(updated.getRoleOffered());
        existing.setPackageLpa(updated.getPackageLpa());
        existing.setDriveType(updated.getDriveType());
        existing.setStatus(updated.getStatus());
        existing.setMinCgpa(updated.getMinCgpa());
        existing.setMaxBacklogs(updated.getMaxBacklogs());
        return driveRepository.save(existing);
    }

    @Override
    public void deleteDrive(Integer id) {
        driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id: " + id));
        driveRepository.deleteById(id);
    }

    @Override
    public List<Drive> getDrivesByStatus(String status) {
        return driveRepository.findByStatus(status);
    }

    @Override
    public List<Drive> getDrivesByCompanyId(Integer companyId) {
        return driveRepository.findByCompany_CompanyId(companyId);
    }
}
