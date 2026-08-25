package com.pmrs.backend.service;

import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.DriveRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DriveServiceImpl implements DriveService {

    private final DriveRepository driveRepository;
    private final PenaltyService  penaltyService;
    private final DocumentTextExtractionService documentTextExtractionService;

    public DriveServiceImpl(DriveRepository driveRepository, PenaltyService penaltyService,
                            DocumentTextExtractionService documentTextExtractionService) {
        this.driveRepository = driveRepository;
        this.penaltyService  = penaltyService;
        this.documentTextExtractionService = documentTextExtractionService;
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
        boolean isNew = drive.getDriveId() == null;
        Drive saved = driveRepository.save(drive);
        Drive reloaded = driveRepository.findById(saved.getDriveId())
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));
        if (isNew) {
            // A newly opened drive consumes one unit of every SKIP_DRIVES penalty
            penaltyService.onDriveCreated(reloaded);
        }
        return reloaded;
    }

    @Override
    public Drive updateDrive(Integer id, Drive updated) {
        Drive existing = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id: " + id));
        existing.setCompany(updated.getCompany());
        existing.setHrContact(updated.getHrContact());
        existing.setDriveDate(updated.getDriveDate());
        existing.setPptDate(updated.getPptDate());
        existing.setResumeSelectionDate(updated.getResumeSelectionDate());
        existing.setFinalSelectionDate(updated.getFinalSelectionDate());
        existing.setRoleOffered(updated.getRoleOffered());
        existing.setPackageLpa(updated.getPackageLpa());
        existing.setDriveType(updated.getDriveType());
        existing.setStatus(updated.getStatus());
        existing.setMinCgpa(updated.getMinCgpa());
        existing.setMaxBacklogs(updated.getMaxBacklogs());
        return driveRepository.save(existing);
    }

    private static final java.util.Set<String> VALID_STATUSES =
            java.util.Set.of("Upcoming", "Active", "Completed", "Cancelled");

    @Override
    public Drive updateStatus(Integer id, String status) {
        Drive existing = driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id: " + id));

        String match = VALID_STATUSES.stream()
                .filter(s -> s.equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid status '" + status + "'. Must be one of " + VALID_STATUSES + "."));

        existing.setStatus(match);
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

    @Override
    public Drive setJdUrl(Integer id, String jdUrl) {
        Drive drive = getDriveById(id);
        drive.setJdUrl(jdUrl);
        String jdText = documentTextExtractionService.extractText(jdUrl);
        drive.setJdText(jdText);
        drive.setJdExtractedAt(jdText != null ? LocalDateTime.now() : null);
        return driveRepository.save(drive);
    }

    @Override
    public Drive extractJd(Integer id) {
        Drive drive = getDriveById(id);
        if (drive.getJdUrl() == null || drive.getJdUrl().isBlank()) {
            throw new IllegalArgumentException("Drive " + id + " has no JD URL set yet.");
        }
        String jdText = documentTextExtractionService.extractText(drive.getJdUrl());
        drive.setJdText(jdText);
        drive.setJdExtractedAt(jdText != null ? LocalDateTime.now() : null);
        return driveRepository.save(drive);
    }
}
