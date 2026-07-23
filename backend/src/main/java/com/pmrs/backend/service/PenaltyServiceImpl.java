package com.pmrs.backend.service;

import com.pmrs.backend.dto.PenaltyDTO;
import com.pmrs.backend.dto.PenaltyStatusDTO;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.PlacementPenalty;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.ApplicationRepository;
import com.pmrs.backend.repository.PlacementPenaltyRepository;
import com.pmrs.backend.util.TierPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Withdrawal-penalty rules (see also {@link #previewPenalty}):
 *
 *   Applied                         → no penalty (nothing has happened yet)
 *   First Round                     → barred from the next 5 eligible companies
 *   Interview Scheduled (final)     → banned from all drives for 1 month
 *   Selected, offer then rejected   → banned from all drives for 1 month (same as above)
 *   Offer Accepted, then declined   → permanent bar + disciplinary referral
 *
 * Penalties stack: each offence creates its own row, and SKIP_DRIVES counters
 * are consumed oldest-first as new eligible drives open.
 */
@Service
public class PenaltyServiceImpl implements PenaltyService {

    private static final Logger log = LoggerFactory.getLogger(PenaltyServiceImpl.class);

    static final int SKIP_DRIVES_COUNT = 5;
    static final int TIME_BAN_MONTHS   = 1;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final PlacementPenaltyRepository penaltyRepository;
    private final ApplicationRepository      applicationRepository;

    public PenaltyServiceImpl(PlacementPenaltyRepository penaltyRepository,
                              ApplicationRepository applicationRepository) {
        this.penaltyRepository     = penaltyRepository;
        this.applicationRepository = applicationRepository;
    }

    // ── Issuing penalties ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public String applyWithdrawalPenalty(Application application, String stageAtWithdrawal) {
        String drive = describeDrive(application);

        switch (stageAtWithdrawal) {
            case "First Round" -> {
                PlacementPenalty p = base(application);
                p.setPenaltyType(PlacementPenalty.TYPE_SKIP_DRIVES);
                p.setDrivesRemaining(SKIP_DRIVES_COUNT);
                p.setReason("Withdrew from " + drive + " at the First Round stage");
                penaltyRepository.save(p);
                return "You are now barred from the next " + SKIP_DRIVES_COUNT
                     + " companies you would be eligible for.";
            }
            case "Interview Scheduled" -> {
                PlacementPenalty p = base(application);
                p.setPenaltyType(PlacementPenalty.TYPE_TIME_BAN);
                p.setBarUntilDate(LocalDate.now().plusMonths(TIME_BAN_MONTHS));
                p.setReason("Withdrew from " + drive + " at the Interview Scheduled (final) stage");
                penaltyRepository.save(p);
                return timeBanMessage(p);
            }
            case "Selected" -> {
                // Reached only from rejectOffer() — Selected IS the job offer,
                // so rejecting it is "withdrawing from the final stage".
                PlacementPenalty p = base(application);
                p.setPenaltyType(PlacementPenalty.TYPE_TIME_BAN);
                p.setBarUntilDate(LocalDate.now().plusMonths(TIME_BAN_MONTHS));
                p.setReason("Rejected the offer from " + drive);
                penaltyRepository.save(p);
                return timeBanMessage(p);
            }
            default -> {
                return null; // "Applied" and anything unexpected: no penalty
            }
        }
    }

    private String timeBanMessage(PlacementPenalty p) {
        return "You are now banned from all placement drives until "
             + p.getBarUntilDate().format(DATE_FMT) + ".";
    }

    @Override
    @Transactional
    public String applyDeclinePenalty(Application application) {
        PlacementPenalty p = base(application);
        p.setPenaltyType(PlacementPenalty.TYPE_PERMANENT_BAN);
        p.setDisciplinaryReferral(true);
        p.setReason("Declined an accepted offer from " + describeDrive(application));
        penaltyRepository.save(p);
        return "You are permanently barred from further placement drives and your record"
             + " has been flagged for disciplinary referral.";
    }

    private static PlacementPenalty base(Application application) {
        PlacementPenalty p = new PlacementPenalty();
        p.setStudent(application.getStudent());
        p.setSourceApplicationId(application.getApplicationId());
        p.setIssuedBy("SYSTEM");
        p.setActive(true);
        p.setCreatedAt(LocalDate.now());
        return p;
    }

    private static String describeDrive(Application a) {
        String company = a.getDrive() != null && a.getDrive().getCompany() != null
                ? a.getDrive().getCompany().getCompanyName() : "a drive";
        String role = a.getDrive() != null && a.getDrive().getRoleOffered() != null
                ? " – " + a.getDrive().getRoleOffered() : "";
        return company + role;
    }

    // ── Preview (single source of truth for the confirm dialog) ───────────────

    @Override
    public Map<String, String> previewPenalty(String applicationStatus) {
        String status = applicationStatus == null ? "Applied" : applicationStatus;
        return switch (status) {
            case "Applied" -> Map.of(
                    "penaltyType", "NONE",
                    "message", "No penalty — you have not progressed past the application stage.");
            case "First Round" -> Map.of(
                    "penaltyType", PlacementPenalty.TYPE_SKIP_DRIVES,
                    "message", "Withdrawing after the first round will bar you from the next "
                             + SKIP_DRIVES_COUNT + " companies you would otherwise be eligible for.");
            case "Interview Scheduled" -> Map.of(
                    "penaltyType", PlacementPenalty.TYPE_TIME_BAN,
                    "message", "Withdrawing at the final (Interview Scheduled) stage will ban you from ALL"
                             + " placement drives for " + TIME_BAN_MONTHS + " month.");
            case "Selected" -> Map.of(
                    "penaltyType", PlacementPenalty.TYPE_TIME_BAN,
                    "message", "Rejecting this offer will ban you from ALL placement drives for "
                             + TIME_BAN_MONTHS + " month.");
            case "Offer Accepted" -> Map.of(
                    "penaltyType", PlacementPenalty.TYPE_PERMANENT_BAN,
                    "message", "Declining an accepted offer permanently bars you from all further placement"
                             + " drives and flags your record for disciplinary referral.");
            default -> Map.of(
                    "penaltyType", "BLOCKED",
                    "message", "This application cannot be withdrawn.");
        };
    }

    // ── Resolving a student's current bar ─────────────────────────────────────

    @Override
    @Transactional
    public PenaltyStatusDTO getPenaltyStatus(Integer studentId) {
        List<PlacementPenalty> active =
                penaltyRepository.findByStudent_StudentIdAndActiveTrue(studentId);

        // Lazily expire what's run its course
        LocalDate today = LocalDate.now();
        for (PlacementPenalty p : active) {
            boolean expired =
                    (PlacementPenalty.TYPE_TIME_BAN.equals(p.getPenaltyType())
                            && p.getBarUntilDate() != null && p.getBarUntilDate().isBefore(today))
                 || (PlacementPenalty.TYPE_SKIP_DRIVES.equals(p.getPenaltyType())
                            && (p.getDrivesRemaining() == null || p.getDrivesRemaining() <= 0));
            if (expired) {
                p.setActive(false);
                penaltyRepository.save(p);
            }
        }
        List<PlacementPenalty> live = active.stream().filter(PlacementPenalty::isActive).toList();

        PenaltyStatusDTO dto = new PenaltyStatusDTO();
        dto.setDisciplinaryReferral(
                live.stream().anyMatch(PlacementPenalty::isDisciplinaryReferral));

        if (live.stream().anyMatch(p -> PlacementPenalty.TYPE_PERMANENT_BAN.equals(p.getPenaltyType()))) {
            dto.setBarred(true);
            dto.setPenaltyType(PlacementPenalty.TYPE_PERMANENT_BAN);
            dto.setMessage("You are permanently barred from placement drives"
                    + " (declined an accepted offer). Contact the placement office.");
            return dto;
        }

        LocalDate banUntil = live.stream()
                .filter(p -> PlacementPenalty.TYPE_TIME_BAN.equals(p.getPenaltyType()))
                .map(PlacementPenalty::getBarUntilDate)
                .filter(d -> d != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
        if (banUntil != null) {
            dto.setBarred(true);
            dto.setPenaltyType(PlacementPenalty.TYPE_TIME_BAN);
            dto.setBarUntilDate(banUntil);
            dto.setMessage("You are banned from all placement drives until "
                    + banUntil.format(DATE_FMT) + " (withdrew from or rejected an offer at the final stage).");
            return dto;
        }

        int skipRemaining = live.stream()
                .filter(p -> PlacementPenalty.TYPE_SKIP_DRIVES.equals(p.getPenaltyType()))
                .mapToInt(p -> p.getDrivesRemaining() == null ? 0 : p.getDrivesRemaining())
                .sum();
        if (skipRemaining > 0) {
            dto.setBarred(true);
            dto.setPenaltyType(PlacementPenalty.TYPE_SKIP_DRIVES);
            dto.setDrivesRemaining(skipRemaining);
            dto.setMessage("You are barred from the next " + skipRemaining
                    + " eligible compan" + (skipRemaining == 1 ? "y" : "ies")
                    + " due to a mid-process withdrawal.");
            return dto;
        }

        dto.setBarred(false);
        return dto;
    }

    // ── Drive-opened counter decrement ────────────────────────────────────────

    /**
     * The "next 5 eligible companies" counter: when a drive opens, every
     * student holding an active SKIP_DRIVES penalty who would have been
     * eligible for it consumes one unit (oldest penalty first).
     *
     * Eligibility here mirrors the drive-level rules the student portal
     * enforces: the drive's own min-CGPA / max-backlogs plus the placement-
     * tier progression restriction.
     */
    @Override
    @Transactional
    public void onDriveCreated(Drive drive) {
        List<PlacementPenalty> skips = penaltyRepository.findByActiveTrue().stream()
                .filter(p -> PlacementPenalty.TYPE_SKIP_DRIVES.equals(p.getPenaltyType()))
                .sorted(Comparator.comparing(PlacementPenalty::getPenaltyId))
                .toList();

        java.util.Set<Integer> decrementedStudents = new java.util.HashSet<>();
        for (PlacementPenalty p : skips) {
            Integer studentId = p.getStudent().getStudentId();
            if (decrementedStudents.contains(studentId)) continue;   // one unit per drive
            if (p.getDrivesRemaining() == null || p.getDrivesRemaining() <= 0) continue;
            if (!wouldBeEligible(p.getStudent(), drive)) continue;

            p.setDrivesRemaining(p.getDrivesRemaining() - 1);
            if (p.getDrivesRemaining() <= 0) {
                p.setActive(false);
            }
            penaltyRepository.save(p);
            decrementedStudents.add(studentId);
            log.info("Penalty {}: drive '{}' consumed one skip for student {} ({} remaining).",
                    p.getPenaltyId(), describeDriveShort(drive), studentId, p.getDrivesRemaining());
        }
    }

    private static String describeDriveShort(Drive d) {
        return (d.getCompany() != null ? d.getCompany().getCompanyName() : "?")
                + " – " + d.getRoleOffered();
    }

    private boolean wouldBeEligible(Student s, Drive drive) {
        if (drive.getMinCgpa() != null
                && (s.getCgpa() == null || s.getCgpa() < drive.getMinCgpa())) {
            return false;
        }
        if (drive.getMaxBacklogs() != null
                && (s.getActiveBacklogs() == null || s.getActiveBacklogs() > drive.getMaxBacklogs())) {
            return false;
        }

        // Same job-categories upgradation policy the student portal enforces
        // at apply time: unplaced students always pass; a placed student who
        // has already used their one-time upgrade never passes; otherwise the
        // drive's CTC must clear the ratio required by the current offer.
        List<Application> accepted = applicationRepository
                .findByStudent_StudentId(s.getStudentId()).stream()
                .filter(a -> "Offer Accepted".equalsIgnoreCase(a.getStatus()))
                .toList();
        if (accepted.isEmpty()) {
            return true;
        }
        if (accepted.size() >= 2) {
            return false;
        }
        BigDecimal currentCtc = accepted.stream()
                .map(a -> a.getDrive() != null ? a.getDrive().getPackageLpa() : null)
                .filter(c -> c != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
        String driveTier = drive.getCompany() != null ? drive.getCompany().getTier() : null;
        return TierPolicy.upgradationBlockReason(currentCtc, drive.getPackageLpa(), driveTier) == null;
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PenaltyDTO> getAllPenalties() {
        return penaltyRepository.findAllByOrderByPenaltyIdDesc().stream()
                .map(PenaltyServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PenaltyDTO liftPenalty(Integer penaltyId, String adminUsername) {
        PlacementPenalty p = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new ResourceNotFoundException("Penalty not found: " + penaltyId));
        if (!p.isActive()) {
            throw new IllegalStateException("Penalty " + penaltyId + " is not active.");
        }
        p.setActive(false);
        p.setLiftedBy(adminUsername);
        p.setLiftedAt(LocalDate.now());
        return toDto(penaltyRepository.save(p));
    }

    private static PenaltyDTO toDto(PlacementPenalty p) {
        PenaltyDTO dto = new PenaltyDTO();
        dto.setPenaltyId(p.getPenaltyId());
        if (p.getStudent() != null) {
            dto.setStudentId(p.getStudent().getStudentId());
            dto.setStudentName(p.getStudent().getName());
            dto.setRollNo(p.getStudent().getRollNo());
        }
        dto.setPenaltyType(p.getPenaltyType());
        dto.setDrivesRemaining(p.getDrivesRemaining());
        dto.setBarUntilDate(p.getBarUntilDate());
        dto.setReason(p.getReason());
        dto.setIssuedBy(p.getIssuedBy());
        dto.setActive(p.isActive());
        dto.setDisciplinaryReferral(p.isDisciplinaryReferral());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setLiftedBy(p.getLiftedBy());
        dto.setLiftedAt(p.getLiftedAt());
        return dto;
    }
}
