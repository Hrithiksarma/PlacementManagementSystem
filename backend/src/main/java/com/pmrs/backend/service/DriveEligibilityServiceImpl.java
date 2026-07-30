package com.pmrs.backend.service;

import com.pmrs.backend.dto.PenaltyStatusDTO;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.EligibilityCriteria;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.ApplicationRepository;
import com.pmrs.backend.repository.DriveRepository;
import com.pmrs.backend.repository.EligibilityCriteriaRepository;
import com.pmrs.backend.repository.StudentRepository;
import com.pmrs.backend.util.AcademicEligibility;
import com.pmrs.backend.util.TierPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Re-implements the same three eligibility checks as
 * {@code StudentPortalServiceImpl} (penalty bar, academic CGPA/backlog +
 * department match, upgradation policy) but in the opposite direction:
 * given a drive, which students qualify. Academic eligibility is shared via
 * {@link com.pmrs.backend.util.AcademicEligibility}; department matching
 * and the upgradation policy are still separate copies — if those rules
 * change, both places need updating.
 */
@Service
public class DriveEligibilityServiceImpl implements DriveEligibilityService {

    private final DriveRepository              driveRepository;
    private final StudentRepository            studentRepository;
    private final ApplicationRepository        applicationRepository;
    private final EligibilityCriteriaRepository eligibilityCriteriaRepository;
    private final PenaltyService               penaltyService;

    public DriveEligibilityServiceImpl(DriveRepository              driveRepository,
                                       StudentRepository            studentRepository,
                                       ApplicationRepository        applicationRepository,
                                       EligibilityCriteriaRepository eligibilityCriteriaRepository,
                                       PenaltyService                penaltyService) {
        this.driveRepository              = driveRepository;
        this.studentRepository            = studentRepository;
        this.applicationRepository        = applicationRepository;
        this.eligibilityCriteriaRepository = eligibilityCriteriaRepository;
        this.penaltyService               = penaltyService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> getEligibleStudents(Integer driveId) {
        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id: " + driveId));

        List<EligibilityCriteria> driveCriteria =
                eligibilityCriteriaRepository.findByDrive_DriveId(driveId);

        List<Student> eligible = new ArrayList<>();
        for (Student student : studentRepository.findAll()) {
            if (isEligible(student, drive, driveCriteria)) {
                eligible.add(student);
            }
        }
        return eligible;
    }

    private boolean isEligible(Student student, Drive drive, List<EligibilityCriteria> driveCriteria) {
        PenaltyStatusDTO bar = penaltyService.getPenaltyStatus(student.getStudentId());
        if (bar.isBarred()) {
            return false;
        }

        Optional<EligibilityCriteria> ec = findApplicable(driveCriteria, student);
        if (AcademicEligibility.checkReason(student, drive, ec) != null) {
            return false;
        }

        return validateUpgradationPolicy(student, drive) == null;
    }

    private Optional<EligibilityCriteria> findApplicable(List<EligibilityCriteria> criteria, Student student) {
        if (criteria.isEmpty()) return Optional.empty();

        String deptName = student.getDepartment() != null
                ? student.getDepartment().getDeptName() : null;

        for (EligibilityCriteria ec : criteria) {
            String allowed = ec.getAllowedDepartments();
            if (allowed == null || allowed.isBlank()
                    || (deptName != null && allowed.contains(deptName))) {
                return Optional.of(ec);
            }
        }
        return Optional.of(criteria.get(0));
    }

    private List<Application> getAcceptedOffers(Student student) {
        return applicationRepository.findByStudent_StudentId(student.getStudentId()).stream()
                .filter(a -> "Offer Accepted".equalsIgnoreCase(a.getStatus()))
                .toList();
    }

    private static BigDecimal currentOfferCtc(List<Application> accepted) {
        return accepted.stream()
                .map(a -> a.getDrive() != null ? a.getDrive().getPackageLpa() : null)
                .filter(ctc -> ctc != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private String validateUpgradationPolicy(Student student, Drive drive) {
        List<Application> accepted = getAcceptedOffers(student);
        if (accepted.isEmpty()) {
            return null; // first placement — no restriction
        }
        if (accepted.size() >= 2) {
            return TierPolicy.upgradationUsedMessage();
        }
        BigDecimal currentCtc = currentOfferCtc(accepted);
        BigDecimal newCtc     = drive.getPackageLpa();
        String driveTier      = drive.getCompany() != null ? drive.getCompany().getTier() : null;
        return TierPolicy.upgradationBlockReason(currentCtc, newCtc, driveTier);
    }
}
