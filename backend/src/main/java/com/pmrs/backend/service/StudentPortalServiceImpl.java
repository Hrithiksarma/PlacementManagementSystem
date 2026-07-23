package com.pmrs.backend.service;

import com.pmrs.backend.dto.EligibleDriveDTO;
import com.pmrs.backend.dto.PenaltyStatusDTO;
import com.pmrs.backend.dto.PlacementAchievementDTO;
import com.pmrs.backend.dto.PlacementOfferDTO;
import com.pmrs.backend.dto.PlacementStatusDTO;
import com.pmrs.backend.dto.StudentApplicationDTO;
import com.pmrs.backend.dto.StudentProfileDTO;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.EligibilityCriteria;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.entity.User;
import com.pmrs.backend.repository.ApplicationRepository;
import com.pmrs.backend.repository.DriveRepository;
import com.pmrs.backend.repository.EligibilityCriteriaRepository;
import com.pmrs.backend.repository.StudentRepository;
import com.pmrs.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.util.ApplicationStatusValidator;
import com.pmrs.backend.util.TierPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentPortalServiceImpl implements StudentPortalService {

    private final UserRepository                userRepository;
    private final StudentRepository             studentRepository;
    private final DriveRepository               driveRepository;
    private final ApplicationRepository         applicationRepository;
    private final EligibilityCriteriaRepository eligibilityCriteriaRepository;
    private final PenaltyService                penaltyService;

    public StudentPortalServiceImpl(UserRepository userRepository,
                                    StudentRepository studentRepository,
                                    DriveRepository driveRepository,
                                    ApplicationRepository applicationRepository,
                                    EligibilityCriteriaRepository eligibilityCriteriaRepository,
                                    PenaltyService penaltyService) {
        this.userRepository               = userRepository;
        this.studentRepository            = studentRepository;
        this.driveRepository              = driveRepository;
        this.applicationRepository        = applicationRepository;
        this.eligibilityCriteriaRepository = eligibilityCriteriaRepository;
        this.penaltyService               = penaltyService;
    }

    // ── Resolve username → Student ────────────────────────────────────────────

    private Student resolveStudent(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        if (user.getStudentId() == null) {
            throw new IllegalStateException("No student profile linked to this account");
        }
        return studentRepository.findById(user.getStudentId())
                .orElseThrow(() -> new IllegalStateException("Student record not found"));
    }

    // ── EligibilityCriteria helpers ───────────────────────────────────────────

    /**
     * Returns the criteria row that applies to this student's department.
     * Prefers a row where allowedDepartments includes the student's dept name,
     * or where allowedDepartments is null/blank (applies to all departments).
     * Falls back to the first row if no dept-specific match is found.
     */
    private Optional<EligibilityCriteria> findApplicable(
            List<EligibilityCriteria> criteria, Student student) {
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

    // ── Eligibility helpers (two concerns, kept strictly separate) ───────────

    /**
     * Academic eligibility: does this student meet the drive's CGPA / backlog
     * requirements?  Has nothing to do with placement outcome.
     */
    private String validateAcademicEligibility(Student student,
                                                Optional<EligibilityCriteria> ec) {
        if (ec.isEmpty()) return null;
        EligibilityCriteria criteria = ec.get();
        if (criteria.getMinCgpa() != null
                && (student.getCgpa() == null || student.getCgpa() < criteria.getMinCgpa())) {
            return "Minimum CGPA required is " + criteria.getMinCgpa() + ".";
        }
        if (criteria.getMaxBacklogs() != null
                && (student.getActiveBacklogs() == null
                        || student.getActiveBacklogs() > criteria.getMaxBacklogs())) {
            return "Maximum allowed backlogs is " + criteria.getMaxBacklogs() + ".";
        }
        return null;
    }

    /** All of the student's accepted offers — the source of "current CTC". */
    private List<Application> getAcceptedOffers(Student student) {
        return applicationRepository.findByStudent_StudentId(student.getStudentId()).stream()
                .filter(a -> "Offer Accepted".equalsIgnoreCase(a.getStatus()))
                .toList();
    }

    /** Highest CTC among the student's accepted offers, or null if unplaced. */
    private static BigDecimal currentOfferCtc(List<Application> accepted) {
        return accepted.stream()
                .map(a -> a.getDrive() != null ? a.getDrive().getPackageLpa() : null)
                .filter(ctc -> ctc != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * Job-categories upgradation policy (see {@link TierPolicy}): a placed
     * student may take at most ONE further offer, and only into a company
     * whose CTC clears the ratio required by their current tier.
     *
     * Unplaced students are always allowed through (this is first placement,
     * not upgradation).
     */
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

    /**
     * Single source of truth for drive eligibility.
     * Checks academic rules first, then the upgradation policy.
     * Returns null when the student is eligible, or a reason string when not.
     */
    private String getEligibilityReason(Student student, Drive drive,
                                         Optional<EligibilityCriteria> ec) {
        String academic = validateAcademicEligibility(student, ec);
        if (academic != null) return academic;

        return validateUpgradationPolicy(student, drive);
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StudentProfileDTO getProfile(String username) {
        Student s = resolveStudent(username);

        StudentProfileDTO dto = new StudentProfileDTO();
        dto.setStudentId(s.getStudentId());
        dto.setRollNo(s.getRollNo());
        dto.setName(s.getName());
        dto.setEmail(s.getEmail());
        dto.setPhone(s.getPhone());
        dto.setBranch(s.getDepartment() != null ? s.getDepartment().getBranch()  : null);
        dto.setProgram(s.getDepartment() != null ? s.getDepartment().getProgram() : null);
        dto.setBatchYear(s.getBatchYear());
        dto.setCgpa(s.getCgpa());
        dto.setActiveBacklogs(s.getActiveBacklogs());
        dto.setPlacementTier(s.getPlacementTier());
        dto.setPlacementStatus(isPlaced(s) ? "Placed" : "Not Placed");
        return dto;
    }

    // ── Eligible Drives ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<EligibleDriveDTO> getEligibleDrives(String username) {
        Student s = resolveStudent(username);

        // Penalty bar applies to every drive; shown per-card and blocks Apply.
        PenaltyStatusDTO bar = penaltyService.getPenaltyStatus(s.getStudentId());

        Set<Integer> appliedDriveIds = applicationRepository
                .findByStudent_StudentId(s.getStudentId())
                .stream()
                .map(a -> a.getDrive().getDriveId())
                .collect(Collectors.toSet());

        // Pre-load all criteria to avoid N+1 queries
        Map<Integer, List<EligibilityCriteria>> criteriaByDrive =
                eligibilityCriteriaRepository.findAll().stream()
                        .collect(Collectors.groupingBy(ec -> ec.getDrive().getDriveId()));

        List<EligibleDriveDTO> result = new ArrayList<>();

        for (Drive drive : driveRepository.findAll()) {
            if ("Cancelled".equalsIgnoreCase(drive.getStatus())) continue;

            List<EligibilityCriteria> driveCriteria =
                    criteriaByDrive.getOrDefault(drive.getDriveId(), List.of());
            Optional<EligibilityCriteria> ecOpt = findApplicable(driveCriteria, s);

            // Penalty bars and academic (CGPA/backlog) misses are hard blocks —
            // the drive is shown but visibly disabled. A failed upgradation
            // check is different: the student HAS an offer and this drive is
            // otherwise open to them, so the card stays fully interactive and
            // the policy explanation only surfaces if they actually try to
            // apply (server-side rejection message, same wording as here).
            String hardBlockReason = bar.isBarred() ? bar.getMessage()
                                                     : validateAcademicEligibility(s, ecOpt);
            String upgradationReason = hardBlockReason == null
                    ? validateUpgradationPolicy(s, drive) : null;
            String reason = hardBlockReason != null ? hardBlockReason : upgradationReason;

            EligibleDriveDTO dto = new EligibleDriveDTO();
            dto.setDriveId(drive.getDriveId());
            dto.setRoleOffered(drive.getRoleOffered());
            dto.setPackageLpa(drive.getPackageLpa());
            dto.setMinCgpa(ecOpt.map(EligibilityCriteria::getMinCgpa)
                    .orElse(drive.getMinCgpa()));
            dto.setMaxBacklogs(ecOpt.map(EligibilityCriteria::getMaxBacklogs)
                    .orElse(drive.getMaxBacklogs()));
            dto.setDriveDate(drive.getDriveDate());
            dto.setDriveStatus(drive.getStatus());
            if (drive.getCompany() != null) {
                dto.setCompanyName(drive.getCompany().getCompanyName());
                dto.setCompanyTier(drive.getCompany().getTier());
                dto.setCompanyWebsite(drive.getCompany().getWebsite());
            }
            dto.setAlreadyApplied(appliedDriveIds.contains(drive.getDriveId()));
            dto.setEligible(reason == null);
            dto.setEligibilityReason(reason);
            dto.setUpgradationBlocked(upgradationReason != null);
            result.add(dto);
        }
        return result;
    }

    // ── My Applications ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<StudentApplicationDTO> getApplications(String username) {
        Student s = resolveStudent(username);

        return applicationRepository.findByStudent_StudentId(s.getStudentId())
                .stream()
                .sorted((a, b) -> {
                    if (a.getApplicationDate() == null) return 1;
                    if (b.getApplicationDate() == null) return -1;
                    return b.getApplicationDate().compareTo(a.getApplicationDate());
                })
                .map(a -> {
                    StudentApplicationDTO dto = new StudentApplicationDTO();
                    dto.setApplicationId(a.getApplicationId());
                    dto.setStatus(a.getStatus());
                    dto.setApplicationDate(a.getApplicationDate());
                    if (a.getDrive() != null) {
                        dto.setRoleOffered(a.getDrive().getRoleOffered());
                        dto.setPackageLpa(a.getDrive().getPackageLpa());
                        dto.setDriveDate(a.getDrive().getDriveDate());
                        dto.setDriveStatus(a.getDrive().getStatus());
                        if (a.getDrive().getCompany() != null) {
                            dto.setCompanyName(a.getDrive().getCompany().getCompanyName());
                            dto.setCompanyTier(a.getDrive().getCompany().getTier());
                        }
                    }
                    dto.setOfferDeadline(a.getOfferDeadline());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ── Placement Status ──────────────────────────────────────────────────────

    private static final List<String> STAGE_PRIORITY = List.of(
            "Offer Accepted", "Selected",
            "Interview Scheduled", "First Round", "Applied", "Rejected"
    );

    private String computeCurrentStage(List<Application> apps) {
        // Withdrawn and Offer Rejected are dead-ends — exclude from progress display.
        List<Application> active = apps.stream()
                .filter(a -> !"Withdrawn".equalsIgnoreCase(a.getStatus())
                          && !"Offer Rejected".equalsIgnoreCase(a.getStatus()))
                .toList();
        if (active.isEmpty()) return "Not Applied";
        for (String stage : STAGE_PRIORITY) {
            if (active.stream().anyMatch(a -> stage.equalsIgnoreCase(a.getStatus()))) {
                return stage;
            }
        }
        return "Applied";
    }

    /**
     * Single source of truth for "is this student placed?".
     * Reads Students.placement_tier — set by the after_selection trigger
     * when a student's application status changes to Selected.
     * Never queries Applications directly for placement determination.
     */
    private boolean isPlaced(Student student) {
        String tier = student.getPlacementTier();
        return tier != null && !"Unplaced".equalsIgnoreCase(tier);
    }

    @Override
    @Transactional(readOnly = true)
    public PlacementStatusDTO getPlacementStatus(String username) {
        Student s = resolveStudent(username);
        List<Application> apps = applicationRepository.findByStudent_StudentId(s.getStudentId());

        PlacementStatusDTO dto = new PlacementStatusDTO();
        dto.setCurrentStage(computeCurrentStage(apps));

        // Placement is confirmed only when the student has Accepted an offer.
        // placement_tier is authoritative (set by acceptOffer); the app query populates display details.
        if (isPlaced(s)) {
            dto.setPlaced(true);
            dto.setMessage("Congratulations! You have accepted a placement offer.");
            apps.stream()
                    .filter(a -> "Offer Accepted".equalsIgnoreCase(a.getStatus()))
                    .max(Comparator.comparing(
                            Application::getOfferAcceptedAt,
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .ifPresent(a -> {
                        dto.setPlacementDate(a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getApplicationDate());
                        if (a.getDrive() != null) {
                            dto.setRoleOffered(a.getDrive().getRoleOffered());
                            dto.setPackageLpa(a.getDrive().getPackageLpa());
                            if (a.getDrive().getCompany() != null) {
                                dto.setCompanyName(a.getDrive().getCompany().getCompanyName());
                                dto.setCompanyTier(a.getDrive().getCompany().getTier());
                            }
                        }
                    });
        } else {
            dto.setPlaced(false);
            dto.setMessage("Your placement journey is in progress. Keep applying!");
        }

        // Pending offer: a "Selected" app awaiting the student's Accept/Reject decision
        apps.stream()
                .filter(a -> "Selected".equalsIgnoreCase(a.getStatus()))
                .findFirst()
                .ifPresent(a -> {
                    dto.setHasPendingOffer(true);
                    dto.setPendingOfferAppId(a.getApplicationId());
                    dto.setPendingOfferDeadline(a.getOfferDeadline());
                    if (a.getDrive() != null) {
                        dto.setPendingOfferRole(a.getDrive().getRoleOffered());
                        dto.setPendingOfferPackage(a.getDrive().getPackageLpa());
                        if (a.getDrive().getCompany() != null) {
                            dto.setPendingOfferCompany(a.getDrive().getCompany().getCompanyName());
                            dto.setPendingOfferTier(a.getDrive().getCompany().getTier());
                        }
                    }
                });

        return dto;
    }

    // ── Recent Placement Achievements (public, Selected only) ─────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PlacementAchievementDTO> getRecentAchievements() {
        return applicationRepository.findByStatus("Selected")
                .stream()
                .sorted((a, b) -> {
                    LocalDate da = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getApplicationDate();
                    LocalDate db = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getApplicationDate();
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return db.compareTo(da);
                })
                .limit(10)
                .map(a -> {
                    PlacementAchievementDTO dto = new PlacementAchievementDTO();
                    dto.setStudentName(a.getStudent() != null ? a.getStudent().getName() : "—");
                    if (a.getDrive() != null) {
                        dto.setRoleOffered(a.getDrive().getRoleOffered());
                        dto.setPackageLpa(a.getDrive().getPackageLpa());
                        if (a.getDrive().getCompany() != null) {
                            dto.setCompanyName(a.getDrive().getCompany().getCompanyName());
                            dto.setCompanyTier(a.getDrive().getCompany().getTier());
                        }
                    }
                    LocalDate date = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getApplicationDate();
                    dto.setSelectionDate(date);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ── All Selected Offers ───────────────────────────────────────────────────

    private int tierRank(Application a) {
        if (a.getDrive() == null || a.getDrive().getCompany() == null) return 0;
        return TierPolicy.rank(a.getDrive().getCompany().getTier());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlacementOfferDTO> getPlacementOffers(String username) {
        Student s = resolveStudent(username);

        // Only confirmed (Offer Accepted) applications appear in the offer history
        List<Application> accepted = applicationRepository
                .findByStudent_StudentId(s.getStudentId())
                .stream()
                .filter(a -> "Offer Accepted".equalsIgnoreCase(a.getStatus()))
                .sorted((a, b) -> {
                    int tierCmp = Integer.compare(tierRank(b), tierRank(a));
                    if (tierCmp != 0) return tierCmp;
                    if (a.getOfferAcceptedAt() == null && b.getOfferAcceptedAt() == null) return 0;
                    if (a.getOfferAcceptedAt() == null) return 1;
                    if (b.getOfferAcceptedAt() == null) return -1;
                    return b.getOfferAcceptedAt().compareTo(a.getOfferAcceptedAt());
                })
                .collect(Collectors.toList());

        List<PlacementOfferDTO> result = new ArrayList<>();
        for (int i = 0; i < accepted.size(); i++) {
            Application a = accepted.get(i);
            PlacementOfferDTO dto = new PlacementOfferDTO();
            dto.setApplicationId(a.getApplicationId());
            dto.setCurrentOffer(i == 0);
            dto.setSelectionDate(a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getApplicationDate());
            if (a.getDrive() != null) {
                dto.setRoleOffered(a.getDrive().getRoleOffered());
                dto.setPackageLpa(a.getDrive().getPackageLpa());
                if (a.getDrive().getCompany() != null) {
                    dto.setCompanyName(a.getDrive().getCompany().getCompanyName());
                    dto.setCompanyTier(a.getDrive().getCompany().getTier());
                }
            }
            result.add(dto);
        }
        return result;
    }

    // ── Offer Accept / Reject (student-initiated) ─────────────────────────────

    @Override
    @Transactional
    public void acceptOffer(String username, Integer applicationId) {
        Student s = resolveStudent(username);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getStudent().getStudentId().equals(s.getStudentId())) {
            throw new IllegalStateException("You can only accept your own offers.");
        }
        if (!"Selected".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException(
                    "Cannot accept: status is '" + app.getStatus() + "', expected 'Selected'.");
        }

        // Captured BEFORE this acceptance: tells us whether this is the
        // student's first placement or their one-time upgradation.
        List<Application> priorAccepted = getAcceptedOffers(s);
        if (priorAccepted.size() >= 2) {
            throw new IllegalStateException(TierPolicy.upgradationUsedMessage());
        }
        boolean isUpgrade = priorAccepted.size() == 1;

        app.setStatus("Offer Accepted");
        app.setOfferAcceptedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDate.now());
        applicationRepository.save(app);

        BigDecimal newCtc = app.getDrive() != null ? app.getDrive().getPackageLpa() : null;
        String newTier = TierPolicy.tierForCtc(newCtc);
        if (newTier != null) {
            s.setPlacementTier(newTier);
            studentRepository.save(s);
        }

        if (isUpgrade) {
            // The one-time upgradation is now used — no third offer, ever.
            revokeAllActive(s, app.getApplicationId());
        } else if (newCtc != null) {
            // First placement: only applications that could still legitimately
            // be pursued under the upgradation policy remain open.
            revokeIneligibleUnderPolicy(s, newCtc, app.getApplicationId());
        }
    }

    @Override
    @Transactional
    public String rejectOffer(String username, Integer applicationId) {
        Student s = resolveStudent(username);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getStudent().getStudentId().equals(s.getStudentId())) {
            throw new IllegalStateException("You can only reject your own offers.");
        }
        if (!"Selected".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException(
                    "Cannot reject: status is '" + app.getStatus() + "', expected 'Selected'.");
        }

        app.setStatus("Offer Rejected");
        app.setOfferRejectedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDate.now());
        applicationRepository.save(app);
        // No revocation — student continues applying, but rejecting at this
        // final stage still carries the same 1-month ban as withdrawing here.
        String penaltyMsg = penaltyService.applyWithdrawalPenalty(app, "Selected");
        return "Offer rejected. " + penaltyMsg;
    }

    @Override
    @Transactional
    public String withdrawApplication(String username, Integer applicationId) {
        Student s = resolveStudent(username);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getStudent().getStudentId().equals(s.getStudentId())) {
            throw new IllegalStateException("You can only withdraw your own applications.");
        }

        String status = app.getStatus() == null || app.getStatus().isBlank()
                ? "Applied" : app.getStatus();
        if (ApplicationStatusValidator.isFinalStatus(status)) {
            throw new IllegalStateException(
                    "Cannot withdraw: application is already " + status + ".");
        }
        if ("Selected".equalsIgnoreCase(status)) {
            throw new IllegalStateException(
                    "This application has an offer awaiting your decision — "
                    + "please accept or reject it instead of withdrawing.");
        }

        app.setStatus("Withdrawn");
        app.setUpdatedAt(LocalDate.now());
        applicationRepository.save(app);

        String penaltyMsg = penaltyService.applyWithdrawalPenalty(app, status);
        return penaltyMsg == null
                ? "Application withdrawn. No penalty applies at this stage."
                : "Application withdrawn. " + penaltyMsg;
    }

    @Override
    @Transactional
    public String declineAcceptedOffer(String username, Integer applicationId) {
        Student s = resolveStudent(username);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getStudent().getStudentId().equals(s.getStudentId())) {
            throw new IllegalStateException("You can only decline your own offers.");
        }
        if (!"Offer Accepted".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException(
                    "Cannot decline: status is '" + app.getStatus() + "', expected 'Offer Accepted'.");
        }

        app.setStatus("Offer Declined");
        app.setUpdatedAt(LocalDate.now());
        applicationRepository.save(app);

        // Recompute placement tier from any remaining accepted offer's CTC
        // (there can be at most one left, since upgradation is one-time).
        BigDecimal remainingCtc = currentOfferCtc(getAcceptedOffers(s));
        String newTier = remainingCtc != null ? TierPolicy.tierForCtc(remainingCtc) : "Unplaced";
        s.setPlacementTier(newTier);
        studentRepository.save(s);

        return "Offer declined. " + penaltyService.applyDeclinePenalty(app);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> previewWithdrawalPenalty(String username, Integer applicationId) {
        Student s = resolveStudent(username);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (!app.getStudent().getStudentId().equals(s.getStudentId())) {
            throw new IllegalStateException("You can only view your own applications.");
        }
        return penaltyService.previewPenalty(app.getStatus());
    }

    @Override
    @Transactional
    public PenaltyStatusDTO getPenaltyStatus(String username) {
        Student s = resolveStudent(username);
        return penaltyService.getPenaltyStatus(s.getStudentId());
    }

    /**
     * The one-time upgradation has just been consumed (this is the student's
     * SECOND accepted offer) — there is no third offer, so every other
     * active application is withdrawn unconditionally.
     */
    private void revokeAllActive(Student student, Integer exceptAppId) {
        withdrawMatching(student, exceptAppId, a -> true);
    }

    /**
     * First placement just happened at {@code newCtc} — active applications
     * that could no longer be legitimately pursued under the upgradation
     * policy (same tier or below, or failing the CTC ratio) are withdrawn.
     * Applications that DO clear the policy are left open as the student's
     * one remaining upgradation attempt.
     */
    private void revokeIneligibleUnderPolicy(Student student, BigDecimal newCtc, Integer exceptAppId) {
        withdrawMatching(student, exceptAppId, a -> {
            if (a.getDrive() == null) return false;
            String reason = TierPolicy.upgradationBlockReason(
                    newCtc, a.getDrive().getPackageLpa(),
                    a.getDrive().getCompany() != null ? a.getDrive().getCompany().getTier() : null);
            return reason != null;
        });
    }

    private void withdrawMatching(Student student, Integer exceptAppId,
                                  java.util.function.Predicate<Application> shouldWithdraw) {
        List<Application> all = applicationRepository.findByStudent_StudentId(student.getStudentId());
        List<Application> toWithdraw = new ArrayList<>();
        for (Application a : all) {
            if (a.getApplicationId().equals(exceptAppId)) continue;
            String st = a.getStatus() != null ? a.getStatus() : "Applied";
            if (ApplicationStatusValidator.isFinalStatus(st)) continue;
            if (shouldWithdraw.test(a)) {
                a.setStatus("Withdrawn");
                a.setUpdatedAt(LocalDate.now());
                toWithdraw.add(a);
            }
        }
        if (!toWithdraw.isEmpty()) {
            applicationRepository.saveAll(toWithdraw);
        }
    }

    // ── Apply to Drive ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void applyToDrive(String username, Integer driveId) {
        Student s     = resolveStudent(username);
        Drive   drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new IllegalArgumentException("Drive not found: " + driveId));

        // Block applications to cancelled/completed drives
        if ("Cancelled".equalsIgnoreCase(drive.getStatus())
                || "Completed".equalsIgnoreCase(drive.getStatus())) {
            throw new IllegalArgumentException("Applications are closed for this drive");
        }

        // Active withdrawal/decline penalties override everything else
        PenaltyStatusDTO bar = penaltyService.getPenaltyStatus(s.getStudentId());
        if (bar.isBarred()) {
            throw new IllegalArgumentException(bar.getMessage());
        }

        // Re-verify all eligibility rules (must mirror the DB trigger)
        List<EligibilityCriteria> driveCriteria =
                eligibilityCriteriaRepository.findByDrive_DriveId(driveId);
        Optional<EligibilityCriteria> ecOpt = findApplicable(driveCriteria, s);

        String reason = getEligibilityReason(s, drive, ecOpt);
        if (reason != null) {
            throw new IllegalArgumentException(reason);
        }

        // Duplicate guard
        if (!applicationRepository
                .findByStudent_StudentIdAndDrive_DriveId(s.getStudentId(), driveId)
                .isEmpty()) {
            throw new IllegalStateException("You have already applied to this drive");
        }

        Application app = new Application();
        app.setStudent(s);
        app.setDrive(drive);
        app.setStatus("Applied");
        app.setApplicationDate(LocalDate.now());
        app.setUpdatedAt(LocalDate.now());
        applicationRepository.save(app);
    }
}