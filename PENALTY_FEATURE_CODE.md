# Withdrawal Penalty System — All Edited Code

Generated 2026-07-19. Full contents of every file created or modified for the withdrawal-penalty feature.

## Penalty rules

| Stage at withdrawal | Penalty |
|---|---|
| Applied | None |
| Shortlisted / Interview Scheduled | Barred from next 5 eligible companies (counter decrements as new eligible drives open) |
| Selected (final round) | 1-month ban from all drives |
| Offer Released | Cannot withdraw (must accept/reject) |
| Decline after accepting | Permanent bar + disciplinary referral |

---

## `backend/src/main/java/com/pmrs/backend/entity/PlacementPenalty.java`

_NEW — penalty row: type, counter/date, reason, issued_by, active, disciplinary flag, lift audit_

```java
package com.pmrs.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * A disciplinary bar on a student's participation in placement drives.
 *
 * Three kinds:
 *   SKIP_DRIVES   — barred from the next N companies they'd be eligible for
 *                   (drivesRemaining decrements as new eligible drives open);
 *   TIME_BAN      — barred from all drives until barUntilDate;
 *   PERMANENT_BAN — barred from all further drives (decline-after-accept);
 *                   carries the disciplinaryReferral flag.
 *
 * Penalties stack: multiple active rows may exist per student. A penalty is
 * never deleted — it is deactivated when it expires or an admin lifts it.
 */
@Entity
@Table(name = "PlacementPenalties")
public class PlacementPenalty {

    public static final String TYPE_SKIP_DRIVES   = "SKIP_DRIVES";
    public static final String TYPE_TIME_BAN      = "TIME_BAN";
    public static final String TYPE_PERMANENT_BAN = "PERMANENT_BAN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penalty_id")
    private Integer penaltyId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "penalty_type", nullable = false)
    private String penaltyType;

    @Column(name = "drives_remaining")
    private Integer drivesRemaining;

    @Column(name = "bar_until_date")
    private LocalDate barUntilDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "issued_by", nullable = false)
    private String issuedBy;

    @Column(name = "source_application_id")
    private Integer sourceApplicationId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "disciplinary_referral", nullable = false)
    private boolean disciplinaryReferral = false;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "lifted_by")
    private String liftedBy;

    @Column(name = "lifted_at")
    private LocalDate liftedAt;

    public Integer getPenaltyId()                     { return penaltyId; }
    public void setPenaltyId(Integer v)               { this.penaltyId = v; }

    public Student getStudent()                       { return student; }
    public void setStudent(Student v)                 { this.student = v; }

    public String getPenaltyType()                    { return penaltyType; }
    public void setPenaltyType(String v)              { this.penaltyType = v; }

    public Integer getDrivesRemaining()               { return drivesRemaining; }
    public void setDrivesRemaining(Integer v)         { this.drivesRemaining = v; }

    public LocalDate getBarUntilDate()                { return barUntilDate; }
    public void setBarUntilDate(LocalDate v)          { this.barUntilDate = v; }

    public String getReason()                         { return reason; }
    public void setReason(String v)                   { this.reason = v; }

    public String getIssuedBy()                       { return issuedBy; }
    public void setIssuedBy(String v)                 { this.issuedBy = v; }

    public Integer getSourceApplicationId()           { return sourceApplicationId; }
    public void setSourceApplicationId(Integer v)     { this.sourceApplicationId = v; }

    public boolean isActive()                         { return active; }
    public void setActive(boolean v)                  { this.active = v; }

    public boolean isDisciplinaryReferral()           { return disciplinaryReferral; }
    public void setDisciplinaryReferral(boolean v)    { this.disciplinaryReferral = v; }

    public LocalDate getCreatedAt()                   { return createdAt; }
    public void setCreatedAt(LocalDate v)             { this.createdAt = v; }

    public String getLiftedBy()                       { return liftedBy; }
    public void setLiftedBy(String v)                 { this.liftedBy = v; }

    public LocalDate getLiftedAt()                    { return liftedAt; }
    public void setLiftedAt(LocalDate v)              { this.liftedAt = v; }
}
```

---

## `backend/src/main/java/com/pmrs/backend/repository/PlacementPenaltyRepository.java`

_NEW — active-penalty finders_

```java
package com.pmrs.backend.repository;

import com.pmrs.backend.entity.PlacementPenalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacementPenaltyRepository extends JpaRepository<PlacementPenalty, Integer> {

    List<PlacementPenalty> findByStudent_StudentIdAndActiveTrue(Integer studentId);

    List<PlacementPenalty> findByActiveTrue();

    List<PlacementPenalty> findAllByOrderByPenaltyIdDesc();
}
```

---

## `backend/src/main/java/com/pmrs/backend/dto/PenaltyStatusDTO.java`

_NEW — student's resolved current bar_

```java
package com.pmrs.backend.dto;

import java.time.LocalDate;

/**
 * A student's current standing with respect to placement penalties —
 * the resolved answer to "can this student apply to drives right now?".
 */
public class PenaltyStatusDTO {

    private boolean   barred;
    private String    penaltyType;       // strongest active: PERMANENT_BAN > TIME_BAN > SKIP_DRIVES
    private String    message;           // human-readable explanation for banners / errors
    private Integer   drivesRemaining;   // total across active SKIP_DRIVES penalties
    private LocalDate barUntilDate;      // latest active TIME_BAN end date
    private boolean   disciplinaryReferral;

    public boolean   isBarred()                      { return barred; }
    public void      setBarred(boolean v)            { this.barred = v; }

    public String    getPenaltyType()                { return penaltyType; }
    public void      setPenaltyType(String v)        { this.penaltyType = v; }

    public String    getMessage()                    { return message; }
    public void      setMessage(String v)            { this.message = v; }

    public Integer   getDrivesRemaining()            { return drivesRemaining; }
    public void      setDrivesRemaining(Integer v)   { this.drivesRemaining = v; }

    public LocalDate getBarUntilDate()               { return barUntilDate; }
    public void      setBarUntilDate(LocalDate v)    { this.barUntilDate = v; }

    public boolean   isDisciplinaryReferral()        { return disciplinaryReferral; }
    public void      setDisciplinaryReferral(boolean v) { this.disciplinaryReferral = v; }
}
```

---

## `backend/src/main/java/com/pmrs/backend/dto/PenaltyDTO.java`

_NEW — admin list view of a penalty_

```java
package com.pmrs.backend.dto;

import java.time.LocalDate;

/** Flat view of a PlacementPenalty row for the admin Penalties page. */
public class PenaltyDTO {

    private Integer   penaltyId;
    private Integer   studentId;
    private String    studentName;
    private String    rollNo;
    private String    penaltyType;
    private Integer   drivesRemaining;
    private LocalDate barUntilDate;
    private String    reason;
    private String    issuedBy;
    private boolean   active;
    private boolean   disciplinaryReferral;
    private LocalDate createdAt;
    private String    liftedBy;
    private LocalDate liftedAt;

    public Integer   getPenaltyId()                  { return penaltyId; }
    public void      setPenaltyId(Integer v)         { this.penaltyId = v; }

    public Integer   getStudentId()                  { return studentId; }
    public void      setStudentId(Integer v)         { this.studentId = v; }

    public String    getStudentName()                { return studentName; }
    public void      setStudentName(String v)        { this.studentName = v; }

    public String    getRollNo()                     { return rollNo; }
    public void      setRollNo(String v)             { this.rollNo = v; }

    public String    getPenaltyType()                { return penaltyType; }
    public void      setPenaltyType(String v)        { this.penaltyType = v; }

    public Integer   getDrivesRemaining()            { return drivesRemaining; }
    public void      setDrivesRemaining(Integer v)   { this.drivesRemaining = v; }

    public LocalDate getBarUntilDate()               { return barUntilDate; }
    public void      setBarUntilDate(LocalDate v)    { this.barUntilDate = v; }

    public String    getReason()                     { return reason; }
    public void      setReason(String v)             { this.reason = v; }

    public String    getIssuedBy()                   { return issuedBy; }
    public void      setIssuedBy(String v)           { this.issuedBy = v; }

    public boolean   isActive()                      { return active; }
    public void      setActive(boolean v)            { this.active = v; }

    public boolean   isDisciplinaryReferral()        { return disciplinaryReferral; }
    public void      setDisciplinaryReferral(boolean v) { this.disciplinaryReferral = v; }

    public LocalDate getCreatedAt()                  { return createdAt; }
    public void      setCreatedAt(LocalDate v)       { this.createdAt = v; }

    public String    getLiftedBy()                   { return liftedBy; }
    public void      setLiftedBy(String v)           { this.liftedBy = v; }

    public LocalDate getLiftedAt()                   { return liftedAt; }
    public void      setLiftedAt(LocalDate v)        { this.liftedAt = v; }
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/PenaltyService.java`

_NEW — penalty engine interface_

```java
package com.pmrs.backend.service;

import com.pmrs.backend.dto.PenaltyDTO;
import com.pmrs.backend.dto.PenaltyStatusDTO;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;

import java.util.List;
import java.util.Map;

public interface PenaltyService {

    /**
     * Issues the penalty owed for withdrawing the given application.
     * No-op (returns null) when the stage carries no penalty.
     *
     * @param stageAtWithdrawal the application status BEFORE it was set to Withdrawn
     * @return a human-readable description of the penalty applied, or null
     */
    String applyWithdrawalPenalty(Application application, String stageAtWithdrawal);

    /** Permanent bar + disciplinary referral for declining an accepted offer. */
    String applyDeclinePenalty(Application application);

    /**
     * What penalty WOULD be applied if the student withdrew/declined this
     * application right now — for the confirmation dialog. Keys:
     * "penaltyType" (NONE / SKIP_DRIVES / TIME_BAN / PERMANENT_BAN / BLOCKED)
     * and "message".
     */
    Map<String, String> previewPenalty(String applicationStatus);

    /** The student's resolved current bar (expiry is applied lazily here). */
    PenaltyStatusDTO getPenaltyStatus(Integer studentId);

    /**
     * Called whenever a new drive opens: decrements the SKIP_DRIVES counter
     * of every penalized student who would have been eligible for it.
     */
    void onDriveCreated(Drive drive);

    List<PenaltyDTO> getAllPenalties();

    PenaltyDTO liftPenalty(Integer penaltyId, String adminUsername);
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/PenaltyServiceImpl.java`

_NEW — rules, preview, bar resolution, drive-opened counter decrement, admin list/lift_

```java
package com.pmrs.backend.service;

import com.pmrs.backend.dto.PenaltyDTO;
import com.pmrs.backend.dto.PenaltyStatusDTO;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.PlacementPenalty;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.PlacementPenaltyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Withdrawal-penalty rules (see also {@link #previewPenalty}):
 *
 *   Applied                         → no penalty (nothing has happened yet)
 *   Shortlisted / Interview Sched.  → barred from the next 5 eligible companies
 *   Selected (final round)          → banned from all drives for 1 month
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

    public PenaltyServiceImpl(PlacementPenaltyRepository penaltyRepository) {
        this.penaltyRepository = penaltyRepository;
    }

    // ── Issuing penalties ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public String applyWithdrawalPenalty(Application application, String stageAtWithdrawal) {
        String drive = describeDrive(application);

        switch (stageAtWithdrawal) {
            case "Shortlisted", "Interview Scheduled" -> {
                PlacementPenalty p = base(application);
                p.setPenaltyType(PlacementPenalty.TYPE_SKIP_DRIVES);
                p.setDrivesRemaining(SKIP_DRIVES_COUNT);
                p.setReason("Withdrew from " + drive + " at the " + stageAtWithdrawal + " stage");
                penaltyRepository.save(p);
                return "You are now barred from the next " + SKIP_DRIVES_COUNT
                     + " companies you would be eligible for.";
            }
            case "Selected" -> {
                PlacementPenalty p = base(application);
                p.setPenaltyType(PlacementPenalty.TYPE_TIME_BAN);
                p.setBarUntilDate(LocalDate.now().plusMonths(TIME_BAN_MONTHS));
                p.setReason("Withdrew from " + drive + " at the final (Selected) round");
                penaltyRepository.save(p);
                return "You are now banned from all placement drives until "
                     + p.getBarUntilDate().format(DATE_FMT) + ".";
            }
            default -> {
                return null; // "Applied" and anything unexpected: no penalty
            }
        }
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
            case "Shortlisted", "Interview Scheduled" -> Map.of(
                    "penaltyType", PlacementPenalty.TYPE_SKIP_DRIVES,
                    "message", "Withdrawing mid-process will bar you from the next "
                             + SKIP_DRIVES_COUNT + " companies you would otherwise be eligible for.");
            case "Selected" -> Map.of(
                    "penaltyType", PlacementPenalty.TYPE_TIME_BAN,
                    "message", "Withdrawing at the final round will ban you from ALL placement drives for "
                             + TIME_BAN_MONTHS + " month.");
            case "Offer Released" -> Map.of(
                    "penaltyType", "BLOCKED",
                    "message", "An offer has been released — accept or reject it instead of withdrawing.");
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
                    + banUntil.format(DATE_FMT) + " (withdrew at the final round).");
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

    private static boolean wouldBeEligible(Student s, Drive drive) {
        if (drive.getMinCgpa() != null
                && (s.getCgpa() == null || s.getCgpa() < drive.getMinCgpa())) {
            return false;
        }
        if (drive.getMaxBacklogs() != null
                && (s.getActiveBacklogs() == null || s.getActiveBacklogs() > drive.getMaxBacklogs())) {
            return false;
        }
        String driveTier = drive.getCompany() != null ? drive.getCompany().getTier() : null;
        String pt = s.getPlacementTier() != null ? s.getPlacementTier() : "Unplaced";
        return switch (pt) {
            case "Normal"      -> !"Normal".equals(driveTier);
            case "Dream"       -> "Super Dream".equals(driveTier);
            case "Super Dream" -> false;
            default            -> true; // Unplaced
        };
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
```

---

## `backend/src/main/java/com/pmrs/backend/controller/PenaltyController.java`

_NEW — GET /penalties, POST /penalties/{id}/lift_

```java
package com.pmrs.backend.controller;

import com.pmrs.backend.dto.PenaltyDTO;
import com.pmrs.backend.service.PenaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Placement Penalty APIs", description = "Review and lift withdrawal/decline penalties")
@RestController
@RequestMapping("/penalties")
@CrossOrigin("*")
public class PenaltyController {

    private final PenaltyService penaltyService;

    public PenaltyController(PenaltyService penaltyService) {
        this.penaltyService = penaltyService;
    }

    @Operation(summary = "List all penalties, newest first (active and historical)")
    @GetMapping
    public List<PenaltyDTO> getAllPenalties() {
        return penaltyService.getAllPenalties();
    }

    @Operation(summary = "Lift an active penalty (recorded with the lifting admin's name)")
    @PostMapping("/{id}/lift")
    public ResponseEntity<?> liftPenalty(@PathVariable Integer id, Authentication auth) {
        try {
            return ResponseEntity.ok(penaltyService.liftPenalty(id, auth.getName()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/util/ApplicationStatusValidator.java`

_MODIFIED — 'Offer Declined' added as final status (also carries earlier forward-jump change)_

```java
package com.pmrs.backend.util;

import java.util.List;
import java.util.Map;

/**
 * Enforces the one-way status progression for placement applications.
 *
 * Admin flow (via updateStatus):
 *   Applied → Shortlisted → Interview Scheduled → Selected → Offer Released,
 *   where skipping ahead is allowed (e.g. Applied → Selected) but moving
 *   backwards is not. Any non-final stage → Rejected.
 *
 * Student flow (via acceptOffer / rejectOffer):
 *   Offer Released → Offer Accepted | Offer Rejected
 *
 * System flow (automatic):
 *   Any non-final stage → Withdrawn (triggered by Offer Accepted at a blocking tier)
 */
public class ApplicationStatusValidator {

    /** The admin pipeline, in order. Forward jumps of any length are valid. */
    private static final List<String> PIPELINE = List.of(
            "Applied", "Shortlisted", "Interview Scheduled", "Selected", "Offer Released");

    private static final Map<String, List<String>> VALID_TRANSITIONS = buildTransitions();

    private static Map<String, List<String>> buildTransitions() {
        java.util.Map<String, List<String>> transitions = new java.util.LinkedHashMap<>();
        for (int i = 0; i < PIPELINE.size() - 1; i++) {
            java.util.List<String> next = new java.util.ArrayList<>(
                    PIPELINE.subList(i + 1, PIPELINE.size()));
            next.add("Rejected");
            transitions.put(PIPELINE.get(i), List.copyOf(next));
        }
        // Offer decisions are the student's (acceptOffer / rejectOffer).
        transitions.put("Offer Released", List.of("Offer Accepted", "Offer Rejected"));
        return Map.copyOf(transitions);
    }

    /**
     * Final statuses — no further transitions allowed.
     * Note: "Selected" is NOT final — it leads to "Offer Released".
     * "Withdrawn" is system-set on revocation and cannot be manually assigned.
     */
    public static boolean isFinalStatus(String status) {
        return "Offer Accepted".equals(status)
            || "Offer Rejected".equals(status)
            || "Offer Declined".equals(status)
            || "Rejected".equals(status)
            || "Withdrawn".equals(status);
    }

    public static boolean isValidTransition(String current, String next) {
        return VALID_TRANSITIONS.getOrDefault(current, List.of()).contains(next);
    }

    public static List<String> allowedTransitions(String current) {
        return VALID_TRANSITIONS.getOrDefault(current, List.of());
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/StudentPortalService.java`

_MODIFIED — withdraw returns message; declineAcceptedOffer, previewWithdrawalPenalty, getPenaltyStatus added_

```java
package com.pmrs.backend.service;

import com.pmrs.backend.dto.EligibleDriveDTO;
import com.pmrs.backend.dto.PlacementAchievementDTO;
import com.pmrs.backend.dto.PlacementOfferDTO;
import com.pmrs.backend.dto.PlacementStatusDTO;
import com.pmrs.backend.dto.StudentApplicationDTO;
import com.pmrs.backend.dto.StudentProfileDTO;

import java.util.List;

public interface StudentPortalService {

    StudentProfileDTO getProfile(String username);

    List<EligibleDriveDTO> getEligibleDrives(String username);

    List<StudentApplicationDTO> getApplications(String username);

    PlacementStatusDTO getPlacementStatus(String username);

    List<PlacementOfferDTO> getPlacementOffers(String username);

    void acceptOffer(String username, Integer applicationId);

    void rejectOffer(String username, Integer applicationId);

    /** @return a message describing the withdrawal and any penalty applied */
    String withdrawApplication(String username, Integer applicationId);

    /** Decline an already-accepted offer — permanent bar + disciplinary referral. */
    String declineAcceptedOffer(String username, Integer applicationId);

    /** Penalty the student would incur by withdrawing/declining this application now. */
    java.util.Map<String, String> previewWithdrawalPenalty(String username, Integer applicationId);

    /** The student's current penalty standing (for banners and enforcement). */
    com.pmrs.backend.dto.PenaltyStatusDTO getPenaltyStatus(String username);

    void applyToDrive(String username, Integer driveId);

    List<PlacementAchievementDTO> getRecentAchievements();
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/StudentPortalServiceImpl.java`

_MODIFIED — penalty on withdraw, decline flow with tier recompute, preview, bar enforcement in applyToDrive + getEligibleDrives_

```java
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

    /**
     * Placement-progression restriction: does the student's current placement
     * outcome (Students.placement_tier, set by the after_selection trigger)
     * block them from applying to a drive of this company tier?
     *
     * placement_tier is the ACTUAL offer tier — Unplaced / Normal / Dream / Super Dream.
     * It is NOT a CGPA-based academic classification.
     */
    private String validatePlacementRestriction(Student student, String driveTier) {
        String pt = student.getPlacementTier() != null ? student.getPlacementTier() : "Unplaced";
        switch (pt) {
            case "Unplaced":
                return null;
            case "Normal":
                if ("Normal".equals(driveTier)) {
                    return "Already placed in a Normal company. "
                         + "You can only apply for Dream or Super Dream opportunities.";
                }
                return null;
            case "Dream":
                if (!"Super Dream".equals(driveTier)) {
                    return "Already placed in a Dream company. "
                         + "Only Super Dream opportunities are available.";
                }
                return null;
            case "Super Dream":
                return "Already placed in a Super Dream company. "
                     + "Placement process completed.";
            default:
                return null;
        }
    }

    /**
     * Single source of truth for drive eligibility.
     * Checks academic rules first, then placement-progression rules.
     * Returns null when the student is eligible, or a reason string when not.
     */
    private String getEligibilityReason(Student student, Drive drive,
                                         Optional<EligibilityCriteria> ec) {
        String academic = validateAcademicEligibility(student, ec);
        if (academic != null) return academic;

        String driveTier = drive.getCompany() != null ? drive.getCompany().getTier() : null;
        return validatePlacementRestriction(student, driveTier);
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

            String reason = bar.isBarred() ? bar.getMessage()
                                           : getEligibilityReason(s, drive, ecOpt);

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
            "Offer Accepted", "Offer Released", "Selected",
            "Interview Scheduled", "Shortlisted", "Applied", "Rejected"
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

        // Pending offer: an "Offer Released" app awaiting student decision
        apps.stream()
                .filter(a -> "Offer Released".equalsIgnoreCase(a.getStatus()))
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

    private static int tierRankStr(String tier) {
        if (tier == null) return 0;
        return switch (tier) {
            case "Super Dream" -> 3;
            case "Dream"       -> 2;
            case "Normal"      -> 1;
            default            -> 0;
        };
    }

    private int tierRank(Application a) {
        if (a.getDrive() == null || a.getDrive().getCompany() == null) return 0;
        return tierRankStr(a.getDrive().getCompany().getTier());
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
        if (!"Offer Released".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException(
                    "Cannot accept: status is '" + app.getStatus() + "', expected 'Offer Released'.");
        }

        app.setStatus("Offer Accepted");
        app.setOfferAcceptedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDate.now());
        applicationRepository.save(app);

        // Upgrade placement tier (only if this offer is higher than current)
        String companyTier = app.getDrive() != null && app.getDrive().getCompany() != null
                ? app.getDrive().getCompany().getTier() : null;
        if (companyTier != null && tierRankStr(companyTier) > tierRankStr(s.getPlacementTier())) {
            s.setPlacementTier(companyTier);
            studentRepository.save(s);
        }

        // Revoke active applications at tiers now blocked
        if (companyTier != null) {
            List<String> tiersToRevoke = switch (companyTier) {
                case "Normal"      -> List.of("Normal");
                case "Dream"       -> List.of("Normal", "Dream");
                case "Super Dream" -> List.of("Normal", "Dream", "Super Dream");
                default            -> List.of();
            };
            if (!tiersToRevoke.isEmpty()) {
                revokeOnAccept(s, tiersToRevoke, app.getApplicationId());
            }
        }
    }

    @Override
    @Transactional
    public void rejectOffer(String username, Integer applicationId) {
        Student s = resolveStudent(username);
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (!app.getStudent().getStudentId().equals(s.getStudentId())) {
            throw new IllegalStateException("You can only reject your own offers.");
        }
        if (!"Offer Released".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException(
                    "Cannot reject: status is '" + app.getStatus() + "', expected 'Offer Released'.");
        }

        app.setStatus("Offer Rejected");
        app.setOfferRejectedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDate.now());
        applicationRepository.save(app);
        // No revocation — student continues applying
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
        if ("Offer Released".equalsIgnoreCase(status)) {
            throw new IllegalStateException(
                    "An offer has been released — please accept or reject it instead of withdrawing.");
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

        // Recompute placement tier from any remaining accepted offers.
        String newTier = applicationRepository.findByStudent_StudentId(s.getStudentId()).stream()
                .filter(a -> "Offer Accepted".equalsIgnoreCase(a.getStatus()))
                .map(a -> a.getDrive() != null && a.getDrive().getCompany() != null
                        ? a.getDrive().getCompany().getTier() : null)
                .filter(t -> t != null)
                .max(Comparator.comparingInt(StudentPortalServiceImpl::tierRankStr))
                .orElse("Unplaced");
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

    private void revokeOnAccept(Student student, List<String> tiersToRevoke, Integer exceptAppId) {
        List<Application> all = applicationRepository.findByStudent_StudentId(student.getStudentId());
        List<Application> toWithdraw = new ArrayList<>();
        for (Application a : all) {
            if (a.getApplicationId().equals(exceptAppId)) continue;
            String st = a.getStatus() != null ? a.getStatus() : "Applied";
            if (ApplicationStatusValidator.isFinalStatus(st)) continue;
            if (a.getDrive() != null && a.getDrive().getCompany() != null
                    && tiersToRevoke.contains(a.getDrive().getCompany().getTier())) {
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
}```

---

## `backend/src/main/java/com/pmrs/backend/controller/StudentPortalController.java`

_MODIFIED — withdraw returns penalty message; decline-offer, withdrawal-penalty-preview, penalty-status endpoints_

```java
package com.pmrs.backend.controller;

import com.pmrs.backend.dto.EligibleDriveDTO;
import com.pmrs.backend.dto.PlacementAchievementDTO;
import com.pmrs.backend.dto.PlacementOfferDTO;
import com.pmrs.backend.dto.PlacementStatusDTO;
import com.pmrs.backend.dto.StudentApplicationDTO;
import com.pmrs.backend.dto.StudentProfileDTO;
import com.pmrs.backend.service.StudentPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Student Portal APIs", description = "Endpoints accessible only by logged-in students")
@RestController
@RequestMapping("/api/student")
@CrossOrigin("*")
public class StudentPortalController {

    private final StudentPortalService portalService;

    public StudentPortalController(StudentPortalService portalService) {
        this.portalService = portalService;
    }

    @Operation(summary = "Get the logged-in student's profile")
    @GetMapping("/profile")
    public StudentProfileDTO getProfile(Authentication auth) {
        return portalService.getProfile(auth.getName());
    }

    @Operation(summary = "Get drives the student is eligible for")
    @GetMapping("/eligible-drives")
    public List<EligibleDriveDTO> getEligibleDrives(Authentication auth) {
        return portalService.getEligibleDrives(auth.getName());
    }

    @Operation(summary = "Get the student's own applications")
    @GetMapping("/applications")
    public List<StudentApplicationDTO> getApplications(Authentication auth) {
        return portalService.getApplications(auth.getName());
    }

    @Operation(summary = "Get the student's placement status")
    @GetMapping("/placement-status")
    public PlacementStatusDTO getPlacementStatus(Authentication auth) {
        return portalService.getPlacementStatus(auth.getName());
    }

    @Operation(summary = "Get all Selected offers for the student, sorted best-first (tier DESC, then most recent)")
    @GetMapping("/placement-offers")
    public List<PlacementOfferDTO> getPlacementOffers(Authentication auth) {
        return portalService.getPlacementOffers(auth.getName());
    }

    @Operation(summary = "Get recent placement achievements (Selected status only — public celebration feed)")
    @GetMapping("/achievements")
    public List<PlacementAchievementDTO> getRecentAchievements() {
        return portalService.getRecentAchievements();
    }

    @Operation(summary = "Accept a released offer — triggers placement confirmation and revokes blocked applications")
    @PostMapping("/applications/{appId}/accept-offer")
    public ResponseEntity<Map<String, String>> acceptOffer(@PathVariable Integer appId,
                                                           Authentication auth) {
        try {
            portalService.acceptOffer(auth.getName(), appId);
            return ResponseEntity.ok(Map.of("message", "Offer accepted. Congratulations!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Reject a released offer — student continues applying")
    @PostMapping("/applications/{appId}/reject-offer")
    public ResponseEntity<Map<String, String>> rejectOffer(@PathVariable Integer appId,
                                                           Authentication auth) {
        try {
            portalService.rejectOffer(auth.getName(), appId);
            return ResponseEntity.ok(Map.of("message", "Offer rejected. You can continue applying to other drives."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Withdraw from a drive mid-process — applies the stage-based penalty")
    @PostMapping("/applications/{appId}/withdraw")
    public ResponseEntity<Map<String, String>> withdrawApplication(@PathVariable Integer appId,
                                                                   Authentication auth) {
        try {
            String message = portalService.withdrawApplication(auth.getName(), appId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Decline an already-accepted offer — permanent bar + disciplinary referral")
    @PostMapping("/applications/{appId}/decline-offer")
    public ResponseEntity<Map<String, String>> declineAcceptedOffer(@PathVariable Integer appId,
                                                                    Authentication auth) {
        try {
            String message = portalService.declineAcceptedOffer(auth.getName(), appId);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Preview the exact penalty withdrawing/declining this application would incur")
    @GetMapping("/applications/{appId}/withdrawal-penalty-preview")
    public Map<String, String> previewWithdrawalPenalty(@PathVariable Integer appId,
                                                        Authentication auth) {
        return portalService.previewWithdrawalPenalty(auth.getName(), appId);
    }

    @Operation(summary = "Get the student's current penalty standing (bans, skip counters, referral flag)")
    @GetMapping("/penalty-status")
    public com.pmrs.backend.dto.PenaltyStatusDTO getPenaltyStatus(Authentication auth) {
        return portalService.getPenaltyStatus(auth.getName());
    }

    @Operation(summary = "Apply to a drive")
    @PostMapping("/apply/{driveId}")
    public ResponseEntity<Map<String, String>> applyToDrive(@PathVariable Integer driveId,
                                                            Authentication auth) {
        try {
            portalService.applyToDrive(auth.getName(), driveId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Application submitted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
        // TransactionSystemException / DataIntegrityViolationException (DB trigger violations)
        // propagate to GlobalExceptionHandler which strips the INELIGIBLE: prefix and returns 400
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/DriveServiceImpl.java`

_MODIFIED — new drives trigger onDriveCreated (skip-counter decrement)_

```java
package com.pmrs.backend.service;

import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.DriveRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriveServiceImpl implements DriveService {

    private final DriveRepository driveRepository;
    private final PenaltyService  penaltyService;

    public DriveServiceImpl(DriveRepository driveRepository, PenaltyService penaltyService) {
        this.driveRepository = driveRepository;
        this.penaltyService  = penaltyService;
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
```

---

## `backend/src/main/java/com/pmrs/backend/service/DriveFormServiceImpl.java`

_MODIFIED — drives created from Google Form submissions also trigger onDriveCreated_

```java
package com.pmrs.backend.service;

import com.pmrs.backend.entity.Company;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.DriveFormSubmission;
import com.pmrs.backend.entity.HRContact;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.CompanyRepository;
import com.pmrs.backend.repository.DriveFormSubmissionRepository;
import com.pmrs.backend.repository.DriveRepository;
import com.pmrs.backend.repository.HRContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Promotes staged Google Form submissions into the real placement tables.
 *
 * <p>Mirrors the Academic ERP import pattern: the staging row is the source,
 * "include" is the human approval step, and everything created goes through
 * the normal Companies/HRContacts/Drives tables so the drive automatically
 * appears in every existing view (Drives page, dashboards, eligible drives).
 */
@Service
public class DriveFormServiceImpl implements DriveFormService {

    private final DriveFormSubmissionRepository submissionRepository;
    private final CompanyRepository             companyRepository;
    private final HRContactRepository           hrContactRepository;
    private final DriveRepository               driveRepository;
    private final PenaltyService                penaltyService;

    public DriveFormServiceImpl(DriveFormSubmissionRepository submissionRepository,
                                CompanyRepository             companyRepository,
                                HRContactRepository           hrContactRepository,
                                DriveRepository               driveRepository,
                                PenaltyService                penaltyService) {
        this.submissionRepository = submissionRepository;
        this.companyRepository    = companyRepository;
        this.hrContactRepository  = hrContactRepository;
        this.driveRepository      = driveRepository;
        this.penaltyService       = penaltyService;
    }

    @Override
    public List<DriveFormSubmission> getSubmissionsByStatus(String status) {
        return submissionRepository.findByStatusOrderBySubmissionIdDesc(status);
    }

    @Override
    public List<Company> getCompaniesWithoutSubmission() {
        Set<String> submittedNames = submissionRepository.findAll().stream()
                .map(DriveFormSubmission::getCompanyName)
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.trim().toLowerCase())
                .collect(Collectors.toSet());

        return companyRepository.findAll().stream()
                .filter(c -> c.getCompanyName() != null
                        && !submittedNames.contains(c.getCompanyName().trim().toLowerCase()))
                .toList();
    }

    @Override
    @Transactional
    public DriveFormSubmission includeSubmission(Integer submissionId) {
        DriveFormSubmission submission = getSubmission(submissionId);

        if (!DriveFormSubmission.STATUS_PENDING.equals(submission.getStatus())) {
            throw new IllegalArgumentException(
                    "Submission " + submissionId + " is already " + submission.getStatus() + ".");
        }
        if (submission.getCompanyName() == null || submission.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Submission has no company name — fix the sheet row and re-sync.");
        }
        if (submission.getRoleOffered() == null || submission.getRoleOffered().isBlank()) {
            throw new IllegalArgumentException("Submission has no role offered — fix the sheet row and re-sync.");
        }
        if (submission.getDriveDate() == null) {
            throw new IllegalArgumentException(
                    "Submission has no valid drive date (use YYYY-MM-DD or DD/MM/YYYY in the form).");
        }

        Company company = findOrCreateCompany(submission);
        HRContact hrContact = findOrCreateHrContact(submission, company);

        Drive drive = new Drive();
        drive.setCompany(company);
        drive.setHrContact(hrContact);
        drive.setDriveDate(submission.getDriveDate());
        drive.setRoleOffered(submission.getRoleOffered().trim());
        drive.setPackageLpa(submission.getPackageLpa());
        drive.setDriveType(blankToNull(submission.getDriveType()));
        drive.setStatus("Upcoming");
        drive.setMinCgpa(submission.getMinCgpa());
        drive.setMaxBacklogs(submission.getMaxBacklogs());
        Drive savedDrive = driveRepository.save(drive);
        penaltyService.onDriveCreated(savedDrive);

        submission.setStatus(DriveFormSubmission.STATUS_INCLUDED);
        submission.setDriveId(savedDrive.getDriveId());
        return submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public DriveFormSubmission rejectSubmission(Integer submissionId) {
        DriveFormSubmission submission = getSubmission(submissionId);
        if (!DriveFormSubmission.STATUS_PENDING.equals(submission.getStatus())) {
            throw new IllegalArgumentException(
                    "Submission " + submissionId + " is already " + submission.getStatus() + ".");
        }
        submission.setStatus(DriveFormSubmission.STATUS_REJECTED);
        return submissionRepository.save(submission);
    }

    private DriveFormSubmission getSubmission(Integer submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Form submission not found with id: " + submissionId));
    }

    private Company findOrCreateCompany(DriveFormSubmission submission) {
        String name = submission.getCompanyName().trim();
        return companyRepository.findByCompanyNameIgnoreCase(name)
                .orElseGet(() -> {
                    Company company = new Company();
                    company.setCompanyName(name);
                    company.setSector(blankToNull(submission.getSector()));
                    // Company.tier is mandatory — default new companies to Normal
                    // when the form left it blank; an admin can retier later.
                    company.setTier(normaliseTier(submission.getTier()));
                    company.setWebsite(blankToNull(submission.getWebsite()));
                    return companyRepository.save(company);
                });
    }

    /**
     * Reuses an existing HR contact (matched by email within the company) or
     * creates one. Returns null when the form didn't provide a full contact —
     * Drive.hrContact is optional, so the drive is still created.
     */
    private HRContact findOrCreateHrContact(DriveFormSubmission submission, Company company) {
        String email = blankToNull(submission.getHrEmail());
        String name  = blankToNull(submission.getHrName());
        String phone = blankToNull(submission.getHrPhone());

        if (email != null) {
            HRContact existing = hrContactRepository
                    .findFirstByCompany_CompanyIdAndHrEmailIgnoreCase(company.getCompanyId(), email)
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        // HRContact requires name, email and phone — skip creation if any is missing.
        if (email == null || name == null || phone == null) {
            return null;
        }

        HRContact contact = new HRContact();
        contact.setCompany(company);
        contact.setHrName(name);
        contact.setHrEmail(email);
        contact.setHrPhone(phone);
        return hrContactRepository.save(contact);
    }

    private static String normaliseTier(String tier) {
        String t = tier == null ? "" : tier.trim();
        return switch (t.toLowerCase()) {
            case "dream"       -> "Dream";
            case "super dream" -> "Super Dream";
            case "normal"      -> "Normal";
            default            -> "Normal";
        };
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/security/SecurityConfig.java`

_MODIFIED — /penalties/** for ADMIN + PLACEMENT_OFFICER_

```java
package com.pmrs.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter,
                          JwtAuthenticationEntryPoint entryPoint,
                          CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
            .authorizeHttpRequests(auth -> auth

                // ── Public ────────────────────────────────────────────────
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/dashboard/**").permitAll()
                .requestMatchers(
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                ).permitAll()

                // ── Students: read → both roles; mutate → ADMIN only ──────
                .requestMatchers(HttpMethod.GET,    "/students/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")
                .requestMatchers(HttpMethod.POST,   "/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/students/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/students/**").hasRole("ADMIN")

                // ── Companies: delete → ADMIN only; rest → both roles ─────
                .requestMatchers(HttpMethod.DELETE, "/companies/**").hasRole("ADMIN")
                .requestMatchers("/companies/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Drives: delete of completed → checked at controller; rest → both ──
                .requestMatchers("/drives/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Google Form drive submissions: review + include → both roles ──
                .requestMatchers("/form-submissions/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Withdrawal/decline penalties: review + lift → both roles ──
                .requestMatchers("/penalties/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Applications: POST blocked (creation only via /api/student/apply)
                .requestMatchers(HttpMethod.POST, "/applications/**").denyAll()
                // Applications: everything else → ADMIN or PLACEMENT_OFFICER
                .requestMatchers("/applications/**").hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // ── Student portal: STUDENT role only ─────────────────────
                .requestMatchers("/api/student/**").hasRole("STUDENT")

                // ── Admin-only: officer account management, never PLACEMENT_OFFICER ──
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ── Supporting data ────────────────────────────────────────
                .requestMatchers("/departments/**", "/hrcontacts/**",
                                 "/eligibilitycriteria/**")
                    .hasAnyRole("ADMIN", "PLACEMENT_OFFICER")

                // Everything else must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

## `frontend/src/services/penaltyService.js`

_NEW — admin penalty API calls_

```js
import axios from "axios";

const API_URL = "http://localhost:8080/penalties";

export const getPenalties = () => axios.get(API_URL);

export const liftPenalty = (id) => axios.post(`${API_URL}/${id}/lift`);
```

---

## `frontend/src/pages/Penalties.jsx`

_NEW — admin Penalties page: list, disciplinary badges, Lift button_

```jsx
import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { getPenalties, liftPenalty } from "../services/penaltyService";

const TYPE_META = {
  SKIP_DRIVES:   { label: "Skip Next Drives", bg: "#fffbeb", color: "#92400e" },
  TIME_BAN:      { label: "Time Ban",         bg: "#fff7ed", color: "#9a3412" },
  PERMANENT_BAN: { label: "Permanent Bar",    bg: "#fff1f2", color: "#be123c" },
};

function formatDate(ds) {
  if (!ds) return "—";
  return new Date(ds).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function Penalties() {
  const [penalties, setPenalties] = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState(null);
  const [message,   setMessage]   = useState(null);
  const [lifting,   setLifting]   = useState(null);
  const [showAll,   setShowAll]   = useState(false);

  const load = () => {
    setLoading(true);
    getPenalties()
      .then((res) => { setPenalties(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load penalties."); setLoading(false); });
  };

  useEffect(() => { load(); }, []);

  useEffect(() => {
    if (!message) return;
    const t = setTimeout(() => setMessage(null), 4000);
    return () => clearTimeout(t);
  }, [message]);

  const handleLift = async (p) => {
    if (!window.confirm(`Lift this penalty for ${p.studentName}? This restores their access to drives.`)) return;
    setLifting(p.penaltyId);
    try {
      await liftPenalty(p.penaltyId);
      setMessage({ type: "success", text: `Penalty lifted for ${p.studentName}.` });
      load();
    } catch (err) {
      setMessage({
        type: "error",
        text: err.response?.data?.message ?? "Failed to lift penalty.",
      });
    } finally {
      setLifting(null);
    }
  };

  const displayed = showAll ? penalties : penalties.filter((p) => p.active);
  const activeCount = penalties.filter((p) => p.active).length;
  const referralCount = penalties.filter((p) => p.active && p.disciplinaryReferral).length;

  const remainingText = (p) => {
    if (p.penaltyType === "SKIP_DRIVES")   return `${p.drivesRemaining ?? 0} drive(s) left`;
    if (p.penaltyType === "TIME_BAN")      return `until ${formatDate(p.barUntilDate)}`;
    if (p.penaltyType === "PERMANENT_BAN") return "permanent";
    return "—";
  };

  return (
    <Layout>
      <div className="d-flex align-items-center mb-3">
        <h2 className="mb-0">Penalties</h2>
        <span
          className="ms-3 badge bg-secondary-subtle text-secondary-emphasis fw-normal"
          style={{ fontSize: "0.72rem", padding: "4px 10px", borderRadius: 6 }}
        >
          Withdrawal &amp; offer-decline penalties, issued automatically
        </span>
        <div className="ms-auto d-flex align-items-center gap-3">
          {referralCount > 0 && (
            <span className="badge bg-danger">{referralCount} disciplinary referral(s)</span>
          )}
          <label className="d-flex align-items-center gap-1" style={{ fontSize: "0.85rem" }}>
            <input type="checkbox" checked={showAll}
                   onChange={(e) => setShowAll(e.target.checked)} />
            Show lifted/expired
          </label>
        </div>
      </div>

      {message && (
        <div className={`alert alert-${message.type === "success" ? "success" : "danger"} py-2`}>
          {message.text}
        </div>
      )}
      {error && <div className="alert alert-danger py-2">{error}</div>}

      {loading ? (
        <p className="text-muted">Loading…</p>
      ) : displayed.length === 0 ? (
        <div className="text-center py-5 text-muted">
          <div style={{ fontSize: "2.5rem" }}>✅</div>
          <p className="fw-semibold mt-2 mb-1 text-dark">
            {showAll ? "No penalties on record" : "No active penalties"}
          </p>
          <p className="small">Penalties are issued automatically when a student withdraws mid-process or declines an accepted offer.</p>
        </div>
      ) : (
        <>
          <p className="text-muted small mb-2">
            Showing <strong>{displayed.length}</strong> penalt{displayed.length === 1 ? "y" : "ies"}
            {!showAll && ` (${activeCount} active)`}
          </p>
          <table className="table table-striped table-bordered table-hover table-sm">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Student</th>
                <th>Roll No</th>
                <th>Type</th>
                <th>Remaining</th>
                <th>Reason</th>
                <th>Issued</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayed.map((p) => {
                const meta = TYPE_META[p.penaltyType] ?? {};
                return (
                  <tr key={p.penaltyId} style={!p.active ? { opacity: 0.6 } : {}}>
                    <td>{p.penaltyId}</td>
                    <td className="fw-semibold">
                      {p.studentName ?? "—"}
                      {p.disciplinaryReferral && (
                        <span className="badge bg-danger ms-2" style={{ fontSize: "0.65rem" }}>
                          Disciplinary Referral
                        </span>
                      )}
                    </td>
                    <td>{p.rollNo ?? "—"}</td>
                    <td>
                      <span style={{
                        background: meta.bg, color: meta.color, fontWeight: 600,
                        padding: "3px 10px", borderRadius: 6, fontSize: "0.75rem",
                        whiteSpace: "nowrap",
                      }}>
                        {meta.label ?? p.penaltyType}
                      </span>
                    </td>
                    <td style={{ whiteSpace: "nowrap" }}>{remainingText(p)}</td>
                    <td style={{ fontSize: "0.82rem" }}>{p.reason ?? "—"}</td>
                    <td style={{ fontSize: "0.82rem", whiteSpace: "nowrap" }}>
                      {formatDate(p.createdAt)}
                      <div className="text-muted">by {p.issuedBy}</div>
                    </td>
                    <td>
                      {p.active ? (
                        <span className="badge bg-warning text-dark">Active</span>
                      ) : p.liftedBy ? (
                        <span className="badge bg-success" title={`Lifted by ${p.liftedBy} on ${formatDate(p.liftedAt)}`}>
                          Lifted
                        </span>
                      ) : (
                        <span className="badge bg-secondary">Expired</span>
                      )}
                    </td>
                    <td>
                      {p.active && (
                        <button
                          className="btn btn-sm btn-outline-success"
                          disabled={lifting === p.penaltyId}
                          onClick={() => handleLift(p)}
                        >
                          {lifting === p.penaltyId ? "…" : "Lift"}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </>
      )}
    </Layout>
  );
}

export default Penalties;
```

---

## `frontend/src/services/studentPortalService.js`

_MODIFIED — declineOffer, getWithdrawalPenaltyPreview, getPenaltyStatus_

```js
import axios from "axios";

const BASE = "http://localhost:8080/api/student";

export const getStudentProfile      = ()         => axios.get(`${BASE}/profile`);
export const getEligibleDrives      = ()         => axios.get(`${BASE}/eligible-drives`);
export const getStudentApplications = ()         => axios.get(`${BASE}/applications`);
export const getPlacementStatus     = ()         => axios.get(`${BASE}/placement-status`);
export const getPlacementOffers     = ()         => axios.get(`${BASE}/placement-offers`);
export const acceptOffer            = (appId)    => axios.post(`${BASE}/applications/${appId}/accept-offer`);
export const rejectOffer            = (appId)    => axios.post(`${BASE}/applications/${appId}/reject-offer`);
export const withdrawApplication    = (appId)    => axios.post(`${BASE}/applications/${appId}/withdraw`);
export const declineOffer           = (appId)    => axios.post(`${BASE}/applications/${appId}/decline-offer`);
export const getWithdrawalPenaltyPreview = (appId) =>
  axios.get(`${BASE}/applications/${appId}/withdrawal-penalty-preview`);
export const getPenaltyStatus       = ()         => axios.get(`${BASE}/penalty-status`);
export const applyToDrive           = (driveId)  => axios.post(`${BASE}/apply/${driveId}`);
export const getRecentAchievements  = ()         => axios.get(`${BASE}/achievements`);
```

---

## `frontend/src/pages/student/MyApplications.jsx`

_MODIFIED — penalty-preview confirm modal, Decline Offer button, bar banner, Offer Declined status_

```jsx
import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import {
  getStudentApplications,
  acceptOffer,
  rejectOffer,
  withdrawApplication,
  declineOffer,
  getWithdrawalPenaltyPreview,
  getPenaltyStatus,
} from "../../services/studentPortalService";
import "./MyApplications.css";

const PIPELINE = [
  "Applied", "Shortlisted", "Interview Scheduled",
  "Selected", "Offer Released", "Offer Accepted",
];

const STATUS_META = {
  Applied:               { color: "#2563eb", bg: "#eff6ff",  icon: "📝" },
  Shortlisted:           { color: "#7c3aed", bg: "#f5f3ff",  icon: "⭐" },
  "Interview Scheduled": { color: "#d97706", bg: "#fffbeb",  icon: "📅" },
  Selected:              { color: "#0891b2", bg: "#ecfeff",  icon: "🎯" },
  "Offer Released":      { color: "#78350f", bg: "#fffbeb",  icon: "📩" },
  "Offer Accepted":      { color: "#16a34a", bg: "#f0fdf4",  icon: "✅" },
  "Offer Rejected":      { color: "#ea580c", bg: "#fff7ed",  icon: "↩️" },
  "Offer Declined":      { color: "#be123c", bg: "#fff1f2",  icon: "⛔" },
  Rejected:              { color: "#dc2626", bg: "#fef2f2",  icon: "❌" },
  Withdrawn:             { color: "#64748b", bg: "#f8fafc",  icon: "🚫" },
};

const TIER_COLOR = {
  "Super Dream": "#7c3aed",
  "Dream":       "#16a34a",
  "Normal":      "#2563eb",
};

function StatusTimeline({ status }) {
  const isRejected      = status === "Rejected";
  const isWithdrawn     = status === "Withdrawn";
  const isOfferRejected = status === "Offer Rejected";
  const isOfferDeclined = status === "Offer Declined";
  const isTerminal      = isRejected || isWithdrawn || isOfferRejected || isOfferDeclined;
  const activeIdx       = PIPELINE.indexOf(status);

  return (
    <div className="ma-timeline">
      {PIPELINE.map((step, i) => {
        const isDone    = !isTerminal && i < activeIdx;
        const isCurrent = !isTerminal && i === activeIdx;

        return (
          <div key={step} className="ma-timeline-step">
            <div className={`ma-step-dot ${
              isCurrent ? "ma-dot-active" :
              isDone    ? "ma-dot-done"   :
              isTerminal && i === 0 ? "ma-dot-done" : "ma-dot-future"
            }`}>
              {isDone || (isTerminal && i === 0) ? "✓" : i + 1}
            </div>
            {i < PIPELINE.length - 1 && (
              <div className={`ma-step-line ${isDone ? "ma-line-done" : "ma-line-future"}`} />
            )}
            <div className={`ma-step-label ${isCurrent ? "ma-label-active" : !isDone ? "ma-label-future" : ""}`}>
              {step}
            </div>
          </div>
        );
      })}

      {isRejected && (
        <div className="ma-timeline-step">
          <div className="ma-step-dot ma-dot-rejected">✕</div>
          <div className="ma-step-label ma-label-rejected">Rejected</div>
        </div>
      )}
      {isOfferRejected && (
        <div className="ma-timeline-step">
          <div className="ma-step-dot" style={{ background: "#ea580c", color: "#fff", border: "none" }}>↩</div>
          <div className="ma-step-label" style={{ color: "#ea580c" }}>Offer Rejected</div>
        </div>
      )}
      {isWithdrawn && (
        <div className="ma-timeline-step">
          <div className="ma-step-dot" style={{ background: "#94a3b8", color: "#fff", border: "none" }}>–</div>
          <div className="ma-step-label" style={{ color: "#64748b" }}>Withdrawn</div>
        </div>
      )}
      {isOfferDeclined && (
        <div className="ma-timeline-step">
          <div className="ma-step-dot" style={{ background: "#be123c", color: "#fff", border: "none" }}>⛔</div>
          <div className="ma-step-label" style={{ color: "#be123c" }}>Offer Declined</div>
        </div>
      )}
    </div>
  );
}

function formatDate(ds) {
  if (!ds) return "—";
  return new Date(ds).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function MyApplications() {
  const [apps,    setApps]    = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);
  const [filter,  setFilter]  = useState("All");
  const [acting,  setActing]  = useState(null);
  const [toast,   setToast]   = useState(null);
  const [penalty, setPenalty] = useState(null);   // current bar status (banner)
  // { app, preview, mode: "withdraw" | "decline", acknowledged } — null = closed
  const [confirmModal, setConfirmModal] = useState(null);

  const loadPenaltyStatus = () => {
    getPenaltyStatus()
      .then((res) => setPenalty(res.data))
      .catch(() => {});
  };

  useEffect(() => {
    getStudentApplications()
      .then((res) => { setApps(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load applications."); setLoading(false); });
    loadPenaltyStatus();
  }, []);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 4500);
    return () => clearTimeout(t);
  }, [toast]);

  const handleAccept = async (appId) => {
    setActing(appId);
    try {
      await acceptOffer(appId);
      setApps((prev) =>
        prev.map((a) => a.applicationId === appId ? { ...a, status: "Offer Accepted" } : a)
      );
      setToast({ type: "success", msg: "Offer accepted! Congratulations on your placement." });
    } catch (err) {
      setToast({ type: "error", msg: err.response?.data?.message ?? "Failed to accept offer." });
    } finally {
      setActing(null);
    }
  };

  const handleReject = async (appId) => {
    if (!window.confirm("Reject this offer? This cannot be undone.")) return;
    setActing(appId);
    try {
      await rejectOffer(appId);
      setApps((prev) =>
        prev.map((a) => a.applicationId === appId ? { ...a, status: "Offer Rejected" } : a)
      );
      setToast({ type: "success", msg: "Offer rejected. You can continue applying to other drives." });
    } catch (err) {
      setToast({ type: "error", msg: err.response?.data?.message ?? "Failed to reject offer." });
    } finally {
      setActing(null);
    }
  };

  // Opens the confirmation modal with the server-computed penalty for this app.
  const openConfirm = async (app, mode) => {
    setActing(app.applicationId);
    try {
      const res = await getWithdrawalPenaltyPreview(app.applicationId);
      setConfirmModal({ app, preview: res.data, mode, acknowledged: false });
    } catch {
      setToast({ type: "error", msg: "Could not determine the withdrawal penalty. Try again." });
    } finally {
      setActing(null);
    }
  };

  const handleConfirmedAction = async () => {
    const { app, mode } = confirmModal;
    setActing(app.applicationId);
    try {
      const res = mode === "decline"
        ? await declineOffer(app.applicationId)
        : await withdrawApplication(app.applicationId);
      const newStatus = mode === "decline" ? "Offer Declined" : "Withdrawn";
      setApps((prev) =>
        prev.map((a) => a.applicationId === app.applicationId ? { ...a, status: newStatus } : a)
      );
      setToast({ type: "success", msg: res.data?.message ?? "Done." });
      loadPenaltyStatus();
    } catch (err) {
      setToast({ type: "error", msg: err.response?.data?.message ?? "Action failed." });
    } finally {
      setActing(null);
      setConfirmModal(null);
    }
  };

  const displayed = filter === "All" ? apps : apps.filter((a) => a.status === filter);

  const counts = Object.fromEntries(
    Object.keys(STATUS_META).map((s) => [s, apps.filter((a) => a.status === s).length])
  );

  if (loading) {
    return (
      <Layout>
        <div className="ma-container">
          {[1, 2, 3].map((i) => <div key={i} className="ma-skeleton" style={{ height: 160 }} />)}
        </div>
      </Layout>
    );
  }

  if (error) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  return (
    <Layout>
      <div className="ma-container">

        {/* ── Toast ──────────────────────────────────────────────────── */}
        {toast && (
          <div className={`alert alert-${toast.type === "success" ? "success" : "danger"} d-flex align-items-center py-2`}>
            <span className="me-2">{toast.type === "success" ? "✅" : "⚠️"}</span>
            <span className="flex-grow-1">{toast.msg}</span>
            <button className="btn-close btn-close-sm" onClick={() => setToast(null)} />
          </div>
        )}

        {/* ── Active penalty banner ──────────────────────────────────── */}
        {penalty?.barred && (
          <div className="alert alert-warning d-flex align-items-center py-2">
            <span className="me-2">⚠️</span>
            <span className="flex-grow-1">
              {penalty.message}
              {penalty.disciplinaryReferral && (
                <strong> Your record has been flagged for disciplinary referral.</strong>
              )}
            </span>
          </div>
        )}

        {/* ── Header ─────────────────────────────────────────────────── */}
        <div className="ma-page-header">
          <div>
            <h2 className="ma-page-title">My Applications</h2>
            <p className="ma-page-sub">{apps.length} total application{apps.length !== 1 ? "s" : ""}</p>
          </div>
        </div>

        {/* ── Filter pills ────────────────────────────────────────────── */}
        <div className="ma-filter-row">
          {["All", ...Object.keys(STATUS_META)].map((s) => {
            const meta  = STATUS_META[s];
            const count = s === "All" ? apps.length : (counts[s] ?? 0);
            return (
              <button
                key={s}
                className={`ma-filter-pill ${filter === s ? "ma-filter-active" : ""}`}
                style={filter === s && meta
                  ? { background: meta.bg, color: meta.color, borderColor: meta.color }
                  : {}}
                onClick={() => setFilter(s)}
              >
                {meta?.icon} {s} ({count})
              </button>
            );
          })}
        </div>

        {/* ── Cards ───────────────────────────────────────────────────── */}
        {displayed.length === 0 ? (
          <div className="ma-empty">
            <span className="ma-empty-icon">📋</span>
            <p>{filter === "All"
              ? "You haven't applied to any drives yet."
              : `No applications with status "${filter}".`}
            </p>
          </div>
        ) : (
          <div className="ma-cards">
            {displayed.map((app) => {
              const meta      = STATUS_META[app.status] ?? STATUS_META.Applied;
              const tierColor = TIER_COLOR[app.companyTier] ?? "#374151";
              const isPending = app.status === "Offer Released";
              // A student can bail out at any stage before an offer is released
              const canWithdraw = ["Applied", "Shortlisted", "Interview Scheduled", "Selected"]
                .includes(app.status);

              return (
                <div key={app.applicationId}
                     className={`ma-card ${isPending ? "ma-card--offer-pending" : ""}`}>

                  {/* Header */}
                  <div className="ma-card-head">
                    <div className="ma-card-left">
                      <div className="ma-company">{app.companyName ?? "—"}</div>
                      <div className="ma-role">{app.roleOffered ?? "—"}</div>
                    </div>
                    <div className="ma-card-right">
                      {app.companyTier && (
                        <span className="ma-tier-badge" style={{ background: tierColor }}>
                          {app.companyTier}
                        </span>
                      )}
                      <span className="ma-status-badge"
                            style={{ background: meta.bg, color: meta.color }}>
                        {meta.icon} {app.status}
                      </span>
                      {canWithdraw && (
                        <button
                          className="btn btn-sm btn-outline-secondary"
                          style={{ fontSize: "0.75rem" }}
                          disabled={acting === app.applicationId}
                          onClick={() => openConfirm(app, "withdraw")}
                        >
                          {acting === app.applicationId ? "…" : "🚫 Withdraw"}
                        </button>
                      )}
                      {app.status === "Offer Accepted" && (
                        <button
                          className="btn btn-sm btn-outline-danger"
                          style={{ fontSize: "0.75rem" }}
                          disabled={acting === app.applicationId}
                          onClick={() => openConfirm(app, "decline")}
                        >
                          {acting === app.applicationId ? "…" : "⛔ Decline Offer"}
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Offer Released — decision banner */}
                  {isPending && (
                    <div className="ma-offer-banner">
                      <div className="ma-offer-banner-body">
                        <span className="ma-offer-banner-icon">📩</span>
                        <div>
                          <div className="ma-offer-banner-title">Offer Available — Action Required</div>
                          {app.offerDeadline
                            ? <div className="ma-offer-banner-deadline">Valid until {formatDate(app.offerDeadline)}</div>
                            : <div className="ma-offer-banner-deadline">Please respond at your earliest</div>}
                        </div>
                      </div>
                      <div className="ma-offer-actions">
                        <button
                          className="btn btn-sm ma-btn-accept"
                          disabled={acting === app.applicationId}
                          onClick={() => handleAccept(app.applicationId)}
                        >
                          {acting === app.applicationId ? "…" : "✓ Accept Offer"}
                        </button>
                        <button
                          className="btn btn-sm ma-btn-reject"
                          disabled={acting === app.applicationId}
                          onClick={() => handleReject(app.applicationId)}
                        >
                          ✕ Reject
                        </button>
                      </div>
                    </div>
                  )}

                  {/* Meta chips */}
                  <div className="ma-meta-row">
                    {app.packageLpa != null && (
                      <span className="ma-meta-chip ma-chip-pkg">💰 {app.packageLpa} LPA</span>
                    )}
                    <span className="ma-meta-chip">📝 Applied: {formatDate(app.applicationDate)}</span>
                    {app.driveDate && (
                      <span className="ma-meta-chip">📅 Drive: {formatDate(app.driveDate)}</span>
                    )}
                  </div>

                  {/* Timeline */}
                  <div className="ma-timeline-wrap">
                    <StatusTimeline status={app.status} />
                  </div>
                </div>
              );
            })}
          </div>
        )}
        {/* ── Penalty confirmation modal ──────────────────────────────── */}
        {confirmModal && (() => {
          const { app, preview, mode, acknowledged } = confirmModal;
          const blocked   = preview.penaltyType === "BLOCKED";
          const hasPenalty = !blocked && preview.penaltyType !== "NONE";
          const isPermanent = preview.penaltyType === "PERMANENT_BAN";
          const verb = mode === "decline" ? "Decline Offer" : "Withdraw";

          return (
            <div style={{
              position: "fixed", inset: 0, background: "rgba(15,23,42,0.55)",
              display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1050,
            }}>
              <div style={{
                background: "#fff", borderRadius: 12, padding: "22px 24px",
                width: "min(480px, 92vw)", boxShadow: "0 20px 50px rgba(0,0,0,0.3)",
              }}>
                <h5 className="mb-1">
                  {blocked ? "Not allowed" : `${verb} — ${app.companyName ?? "this drive"}?`}
                </h5>
                <div className="text-muted mb-3" style={{ fontSize: "0.85rem" }}>
                  {app.roleOffered} · current stage: <strong>{app.status}</strong>
                </div>

                <div style={{
                  background: hasPenalty ? "#fef2f2" : "#f0fdf4",
                  border: `1px solid ${hasPenalty ? "#fecaca" : "#bbf7d0"}`,
                  color: hasPenalty ? "#991b1b" : "#166534",
                  borderRadius: 8, padding: "12px 14px", fontSize: "0.88rem",
                }}>
                  {hasPenalty ? "⚠️ " : "ℹ️ "}{preview.message}
                </div>

                {isPermanent && !blocked && (
                  <label className="d-flex align-items-start gap-2 mt-3" style={{ fontSize: "0.85rem" }}>
                    <input
                      type="checkbox"
                      checked={acknowledged}
                      onChange={(e) =>
                        setConfirmModal({ ...confirmModal, acknowledged: e.target.checked })}
                    />
                    <span>
                      I understand this <strong>permanently</strong> ends my participation in
                      campus placements and refers my record for disciplinary action.
                    </span>
                  </label>
                )}

                <div className="d-flex justify-content-end gap-2 mt-4">
                  <button className="btn btn-sm btn-outline-secondary"
                          onClick={() => setConfirmModal(null)}>
                    Cancel
                  </button>
                  {!blocked && (
                    <button
                      className={`btn btn-sm ${hasPenalty ? "btn-danger" : "btn-primary"}`}
                      disabled={acting === app.applicationId || (isPermanent && !acknowledged)}
                      onClick={handleConfirmedAction}
                    >
                      {acting === app.applicationId ? "…" : `Confirm ${verb}`}
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })()}
      </div>
    </Layout>
  );
}

export default MyApplications;
```

---

## `frontend/src/pages/student/EligibleDrives.jsx`

_MODIFIED — active-penalty banner_

```jsx
import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import { getEligibleDrives, applyToDrive, getPenaltyStatus } from "../../services/studentPortalService";
import "./EligibleDrives.css";

const TIER_COLOR = {
  "Super Dream": "#7c3aed",
  "Dream":       "#16a34a",
  "Normal":      "#2563eb",
};

const DRIVE_STATUS_BADGE = {
  "Upcoming":  { bg: "#eff6ff", color: "#1d4ed8" },
  "Active":    { bg: "#f0fdf4", color: "#15803d" },
  "Completed": { bg: "#f1f5f9", color: "#475569" },
};

function TierBadge({ tier }) {
  const color = TIER_COLOR[tier] ?? "#374151";
  return (
    <span className="ed-tier" style={{ background: color }}>
      {tier ?? "—"}
    </span>
  );
}

function DriveStatusBadge({ status }) {
  const s = DRIVE_STATUS_BADGE[status];
  if (!s) return null;
  return (
    <span className="ed-drive-status" style={{ background: s.bg, color: s.color }}>
      {status}
    </span>
  );
}

function EligibleDrives() {
  const [drives,      setDrives]      = useState([]);
  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState(null);
  const [applying,    setApplying]    = useState(null);
  const [toast,       setToast]       = useState(null);
  const [filterTier,  setFilterTier]  = useState("All");
  const [filterStatus,setFilterStatus]= useState("All");

  const [penalty, setPenalty] = useState(null);

  const loadDrives = () => {
    setLoading(true);
    getEligibleDrives()
      .then((res) => { setDrives(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load drives."); setLoading(false); });
    getPenaltyStatus()
      .then((res) => setPenalty(res.data))
      .catch(() => {});
  };

  useEffect(() => { loadDrives(); }, []);

  const showToast = (msg, type = "success") => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  };

  const handleApply = async (driveId) => {
    setApplying(driveId);
    try {
      await applyToDrive(driveId);
      showToast("Application submitted successfully!");
      loadDrives();
    } catch (err) {
      const data = err?.response?.data;
      const msg =
        (typeof data === "string" && data ? data : null) ||
        data?.message ||
        data?.error ||
        "Failed to apply. Please try again.";
      showToast(String(msg), "error");
    } finally {
      setApplying(null);
    }
  };

  const tiers    = ["All", ...new Set(drives.map((d) => d.companyTier).filter(Boolean))];
  const statuses = ["All", ...new Set(drives.map((d) => d.driveStatus).filter(Boolean))];

  const filtered = drives
    .filter((d) => filterTier   === "All" || d.companyTier  === filterTier)
    .filter((d) => filterStatus === "All" || d.driveStatus  === filterStatus);

  const openCount       = filtered.filter((d) => (d.eligible ?? true) && !d.alreadyApplied && d.driveStatus !== "Completed").length;
  const appliedCount    = filtered.filter((d) => d.alreadyApplied).length;
  const ineligibleCount = filtered.filter((d) => d.eligible === false && !d.alreadyApplied).length;

  const formatDate = (ds) => ds
    ? new Date(ds).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })
    : "—";

  if (loading) {
    return (
      <Layout>
        <div className="ed-container">
          {[1, 2, 3].map((i) => (
            <div key={i} className="ed-skeleton" style={{ height: 120 }} />
          ))}
        </div>
      </Layout>
    );
  }

  if (error) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  return (
    <Layout>
      <div className="ed-container">

        {/* Toast */}
        {toast && (
          <div className={`ed-toast ${toast.type === "error" ? "ed-toast-error" : "ed-toast-ok"}`}>
            {toast.msg}
          </div>
        )}

        {/* ── Active penalty banner ──────────────────────────────────── */}
        {penalty?.barred && (
          <div className="alert alert-warning d-flex align-items-center py-2 mb-3">
            <span className="me-2">⚠️</span>
            <span className="flex-grow-1">{penalty.message}</span>
          </div>
        )}

        {/* ── Header ─────────────────────────────────────────────────── */}
        <div className="ed-page-header">
          <div>
            <h2 className="ed-page-title">Placement Drives</h2>
            <p className="ed-page-sub">
              All active drives — your eligibility is shown on each card
            </p>
          </div>
          <div className="ed-summary-pills">
            <span className="ed-sum-pill ed-sum-open">{openCount} Open</span>
            <span className="ed-sum-pill ed-sum-applied">{appliedCount} Applied</span>
            {ineligibleCount > 0 && (
              <span className="ed-sum-pill ed-sum-ineligible">{ineligibleCount} Not Eligible</span>
            )}
          </div>
        </div>

        {/* ── Filters ────────────────────────────────────────────────── */}
        <div className="ed-filter-bar">
          <div className="ed-filter-group">
            <label className="ed-filter-label">Tier</label>
            <select className="ed-filter-select" value={filterTier}
              onChange={(e) => setFilterTier(e.target.value)}>
              {tiers.map((t) => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div className="ed-filter-group">
            <label className="ed-filter-label">Status</label>
            <select className="ed-filter-select" value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}>
              {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          {(filterTier !== "All" || filterStatus !== "All") && (
            <button className="ed-filter-clear"
              onClick={() => { setFilterTier("All"); setFilterStatus("All"); }}>
              Clear ✕
            </button>
          )}
        </div>

        {/* ── Drive Cards ─────────────────────────────────────────────── */}
        {filtered.length === 0 ? (
          <div className="ed-empty">
            <span className="ed-empty-icon">🎯</span>
            <p>No drives found for the selected filters.</p>
          </div>
        ) : (
          <div className="ed-cards">
            {filtered.map((d) => {
              const isCompleted = d.driveStatus === "Completed";
              const isEligible  = d.eligible ?? true;

              return (
                <div key={d.driveId}
                  className={`ed-card${d.alreadyApplied ? " ed-card-applied" : ""}${(!isEligible && !d.alreadyApplied) ? " ed-card-ineligible" : ""}`}>

                  {/* Card header */}
                  <div className="ed-card-head">
                    <div className="ed-card-company">{d.companyName ?? "—"}</div>
                    <div className="ed-card-badges">
                      {d.companyTier && <TierBadge tier={d.companyTier} />}
                      {d.driveStatus && <DriveStatusBadge status={d.driveStatus} />}
                      {d.alreadyApplied && (
                        <span className="ed-applied-badge">✓ Applied</span>
                      )}
                    </div>
                  </div>

                  {/* Role & package */}
                  <div className="ed-card-role">{d.roleOffered}</div>
                  {d.companyWebsite && (
                    <div className="ed-card-website">{d.companyWebsite}</div>
                  )}

                  {/* Ineligibility reason */}
                  {!isEligible && !d.alreadyApplied && d.eligibilityReason && (
                    <div className="ed-ineligible-reason">
                      ⚠ {d.eligibilityReason}
                    </div>
                  )}

                  {/* Criteria chips */}
                  <div className="ed-criteria-row">
                    {d.packageLpa != null && (
                      <span className="ed-crit ed-crit-pkg">
                        💰 {d.packageLpa} LPA
                      </span>
                    )}
                    {d.minCgpa != null && (
                      <span className="ed-crit">Min CGPA {d.minCgpa}</span>
                    )}
                    {d.maxBacklogs != null && (
                      <span className="ed-crit">Max Backlogs {d.maxBacklogs}</span>
                    )}
                    {d.driveDate && (
                      <span className="ed-crit">📅 {formatDate(d.driveDate)}</span>
                    )}
                  </div>

                  {/* Action */}
                  <div className="ed-card-footer">
                    {d.alreadyApplied ? (
                      <span className="ed-applied-msg">✅ Application Submitted</span>
                    ) : isCompleted ? (
                      <span className="ed-closed-msg">🔒 Applications Closed</span>
                    ) : (
                      <button
                        className="ed-apply-btn"
                        disabled={!isEligible || applying === d.driveId}
                        onClick={() => handleApply(d.driveId)}
                      >
                        {applying === d.driveId ? "Submitting…" : "Apply Now →"}
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </Layout>
  );
}

export default EligibleDrives;
```

---

## `frontend/src/pages/Applications.jsx`

_MODIFIED — Offer Declined in filters + final-status badge styles_

```jsx
import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import {
  getFilteredApplications,
  updateApplicationStatus,
  deleteApplication,
} from "../services/applicationService";
import { getStudents } from "../services/studentService";
import { getAllDrives } from "../services/driveService";
import "./Applications.css";

// ─── Constants ───────────────────────────────────────────────────────────────

const PROGRAMS = ["B.Tech", "M.Tech", "PhD"];

const DEPT_BY_PROGRAM = {
  "B.Tech": ["CSE", "ECE"],
  "M.Tech": ["CSE", "ECE"],
  "PhD":    ["CSE", "ECE", "Science & Mathematics", "HSS"],
};

const BATCH_YEARS = [];
for (let y = 2015; y <= 2030; y++) BATCH_YEARS.push(y);

// Filters panel — all possible statuses
const STATUS_OPTIONS = [
  "Applied", "Shortlisted", "Interview Scheduled",
  "Selected", "Offer Released", "Offer Accepted", "Offer Rejected",
  "Offer Declined", "Rejected", "Withdrawn",
];

// Admin-editable transitions: any later pipeline stage plus Rejected.
// (Offer Accepted / Offer Rejected are student-only, Withdrawn is system-set.)
const STATUS_TRANSITIONS = {
  "Applied":             ["Shortlisted", "Interview Scheduled", "Selected", "Offer Released", "Rejected"],
  "Shortlisted":         ["Interview Scheduled", "Selected", "Offer Released", "Rejected"],
  "Interview Scheduled": ["Selected", "Offer Released", "Rejected"],
  "Selected":            ["Offer Released", "Rejected"],
};

const STATUS_SELECT_STYLE = {
  Applied:               { background: "#eff6ff", color: "#1d4ed8" },
  Shortlisted:           { background: "#f5f3ff", color: "#6d28d9" },
  "Interview Scheduled": { background: "#fffbeb", color: "#92400e" },
  Selected:              { background: "#fefce8", color: "#92400e" },
};

const FINAL_STATUS_STYLE = {
  "Offer Released": { background: "#fffbeb", color: "#78350f", border: "1px solid #fde68a" },
  "Offer Accepted": { background: "#f0fdf4", color: "#14532d", border: "1px solid #bbf7d0" },
  "Offer Rejected": { background: "#fff7ed", color: "#9a3412", border: "1px solid #fed7aa" },
  "Offer Declined": { background: "#fff1f2", color: "#be123c", border: "1px solid #fecdd3" },
  Rejected:         { background: "#fef2f2", color: "#991b1b", border: "1px solid #fecaca" },
  Withdrawn:        { background: "#f8fafc", color: "#64748b", border: "1px solid #cbd5e1" },
};

const TIER_BADGE_CLASS = {
  "Super Dream": "tier-super-dream",
  "Dream":       "tier-dream",
  "Normal":      "tier-normal",
  "Unplaced":    "tier-unplaced",
};

// ─── Utility ─────────────────────────────────────────────────────────────────

function formatDate(dateStr) {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

// ─── Components ──────────────────────────────────────────────────────────────

function TierBadge({ tier }) {
  return (
    <span className={`tier-badge ${TIER_BADGE_CLASS[tier] ?? "tier-unplaced"}`}>
      {tier ?? "Unplaced"}
    </span>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

function Applications() {
  const [students,        setStudents]        = useState([]);
  const [drives,          setDrives]          = useState([]);
  const [allApplications, setAllApplications] = useState([]);
  const [hasSearched,     setHasSearched]     = useState(false);
  const [successMessage,  setSuccessMessage]  = useState(null);
  const [errorMessage,    setErrorMessage]    = useState(null);
  const [savingId,        setSavingId]        = useState(null);

  const [cohort, setCohort] = useState({
    branch: "CSE", program: "B.Tech", batchYear: 2025,
  });
  const [filters, setFilters] = useState({
    studentId: "All", driveId: "All", status: "All",
  });

  useEffect(() => {
    if (!successMessage) return;
    const t = setTimeout(() => setSuccessMessage(null), 4000);
    return () => clearTimeout(t);
  }, [successMessage]);

  useEffect(() => {
    if (!errorMessage) return;
    const t = setTimeout(() => setErrorMessage(null), 5000);
    return () => clearTimeout(t);
  }, [errorMessage]);

  useEffect(() => {
    getStudents()
      .then((res) => setStudents(res.data))
      .catch((err) => console.error("Error loading students:", err));
    getAllDrives()
      .then((res) => setDrives(res.data))
      .catch((err) => console.error("Error loading drives:", err));
  }, []);

  // ── Derived ───────────────────────────────────────────────────────────────

  const cohortStudents = students.filter(
    (s) =>
      s.department?.branch   === cohort.branch &&
      s.department?.program  === cohort.program &&
      s.batchYear            === cohort.batchYear
  );

  const uniqueApplicants = new Set(
    allApplications.map((a) => a.student?.studentId).filter(Boolean)
  ).size;

  const displayedApplications = allApplications
    .filter((a) =>
      filters.studentId === "All" ||
      String(a.student?.studentId) === filters.studentId
    )
    .filter((a) =>
      filters.driveId === "All" ||
      String(a.drive?.driveId) === filters.driveId
    )
    .filter((a) => filters.status === "All" || a.status === filters.status)
    // Normalize: null/empty status defaults to "Applied" so the cell never renders "X"
    .map((a) => ({ ...a, status: a.status || "Applied" }));

  // ── Handlers ─────────────────────────────────────────────────────────────

  const handleSearch = async () => {
    try {
      const res = await getFilteredApplications(
        cohort.branch, cohort.program, cohort.batchYear
      );
      setAllApplications(res.data);
      setHasSearched(true);
    } catch (err) {
      console.error("Error loading applications:", err);
    }
  };

  const handleQuickStatusUpdate = async (id, newStatus) => {
    setSavingId(id);
    try {
      await updateApplicationStatus(id, newStatus);
      setAllApplications((prev) =>
        prev.map((a) => {
          if (a.applicationId !== id) return a;
          // If selected, also update the student's placement tier optimistically
          if (newStatus === "Selected" && a.drive?.company?.tier) {
            return {
              ...a,
              status: newStatus,
              student: { ...a.student, placementTier: a.drive.company.tier },
            };
          }
          return { ...a, status: newStatus };
        })
      );
      setSuccessMessage(`Status updated to "${newStatus}".`);
    } catch (err) {
      const msg = err.response?.data?.message
               || err.response?.data?.error
               || "Status update failed.";
      setErrorMessage(msg);
    } finally {
      setSavingId(null);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this application?")) return;
    try {
      await deleteApplication(id);
      setSuccessMessage("Application deleted.");
      if (hasSearched) handleSearch();
    } catch (err) {
      console.error(err);
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <Layout>

      {/* Page header */}
      <div className="d-flex align-items-center mb-3">
        <h2 className="mb-0">Applications</h2>
        <span
          className="ms-3 badge bg-secondary-subtle text-secondary-emphasis fw-normal"
          style={{ fontSize: "0.72rem", padding: "4px 10px", borderRadius: 6 }}
        >
          Students apply via the Student Portal
        </span>
      </div>

      {/* Success toast */}
      {successMessage && (
        <div className="alert alert-success d-flex align-items-center py-2 mb-3" role="alert">
          <span className="me-2">✅</span>
          <span className="flex-grow-1">{successMessage}</span>
          <button type="button" className="btn-close btn-close-sm" onClick={() => setSuccessMessage(null)} />
        </div>
      )}

      {/* Error toast */}
      {errorMessage && (
        <div className="alert alert-danger d-flex align-items-center py-2 mb-3" role="alert">
          <span className="me-2">⚠️</span>
          <span className="flex-grow-1">{errorMessage}</span>
          <button type="button" className="btn-close btn-close-sm" onClick={() => setErrorMessage(null)} />
        </div>
      )}

      {/* ── Filter Panel ────────────────────────────────────────────────── */}
      <div className="card mb-3">
        <div className="card-body" style={{ padding: "16px 20px" }}>

          {/* Row 1: Cohort */}
          <div className="row g-2 mb-2">
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Branch</label>
              <select
                className="form-select form-select-sm"
                value={cohort.branch}
                onChange={(e) => setCohort({ ...cohort, branch: e.target.value })}
              >
                {DEPT_BY_PROGRAM[cohort.program].map((b) => (
                  <option key={b} value={b}>{b}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Program</label>
              <select
                className="form-select form-select-sm"
                value={cohort.program}
                onChange={(e) => {
                  const prog = e.target.value;
                  setCohort({ ...cohort, program: prog, branch: DEPT_BY_PROGRAM[prog][0] });
                  setFilters({ ...filters, studentId: "All" });
                }}
              >
                {PROGRAMS.map((p) => (
                  <option key={p} value={p}>{p}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Batch Year</label>
              <select
                className="form-select form-select-sm"
                value={cohort.batchYear}
                onChange={(e) => {
                  setCohort({ ...cohort, batchYear: parseInt(e.target.value) });
                  setFilters({ ...filters, studentId: "All" });
                }}
              >
                {BATCH_YEARS.map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Row 2: Student + Drive */}
          <div className="row g-2 mb-2">
            <div className="col-md-6">
              <label className="form-label form-label-sm mb-1 fw-semibold">
                Student
                <span className="text-muted fw-normal ms-1">
                  ({cohortStudents.length} in cohort)
                </span>
              </label>
              <select
                className="form-select form-select-sm"
                value={filters.studentId}
                onChange={(e) => setFilters({ ...filters, studentId: e.target.value })}
              >
                <option value="All">All</option>
                {cohortStudents.map((s) => (
                  <option key={s.studentId} value={String(s.studentId)}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-6">
              <label className="form-label form-label-sm mb-1 fw-semibold">Drive</label>
              <select
                className="form-select form-select-sm"
                value={filters.driveId}
                onChange={(e) => setFilters({ ...filters, driveId: e.target.value })}
              >
                <option value="All">All</option>
                {drives.map((d) => (
                  <option key={d.driveId} value={String(d.driveId)}>
                    {d.company?.companyName} – {d.roleOffered}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Row 3: Status + Load */}
          <div className="row g-2 align-items-end">
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Status</label>
              <select
                className="form-select form-select-sm"
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
              >
                <option value="All">All</option>
                {STATUS_OPTIONS.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <button className="btn btn-primary btn-sm w-100" onClick={handleSearch}>
                Load Applications
              </button>
            </div>
          </div>

        </div>
      </div>

      {/* ── Cohort Summary Bar ───────────────────────────────────────────── */}
      {hasSearched && (
        <div className="d-flex gap-0 mb-3 border rounded overflow-hidden">
          <SummaryCell
            label="Cohort"
            value={`${cohort.branch} · ${cohort.program} · ${cohort.batchYear}`}
          />
          <SummaryCell label="Students in Cohort" value={cohortStudents.length} border />
          <SummaryCell label="Unique Applicants"  value={uniqueApplicants}       border />
          <SummaryCell label="Total Applications" value={allApplications.length} border />
        </div>
      )}

      {/* ── Applications Table ───────────────────────────────────────────── */}
      {hasSearched && (
        displayedApplications.length === 0 ? (
          <div className="text-center py-5 text-muted">
            <div style={{ fontSize: "2.5rem" }}>🔍</div>
            <p className="fw-semibold mt-2 mb-1 text-dark">No applications found</p>
            <ul className="list-unstyled small">
              <li>Try changing the Branch, Program, or Batch Year</li>
              <li>Select a different Drive or Status filter</li>
              <li>Students apply via the Student Portal — Eligible Drives page</li>
            </ul>
          </div>
        ) : (
          <table className="table table-striped table-bordered table-hover table-sm">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Student</th>
                <th>CGPA</th>
                <th>Placement Status</th>
                <th>Company</th>
                <th>Role</th>
                <th>Date Applied</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayedApplications.map((app) => (
                <tr key={app.applicationId}>
                  <td>{app.applicationId}</td>
                  <td>{app.student?.name ?? "—"}</td>
                  <td>{app.student?.cgpa ?? "—"}</td>
                  <td><TierBadge tier={app.student?.placementTier} /></td>
                  <td>{app.drive?.company?.companyName ?? "—"}</td>
                  <td>{app.drive?.roleOffered ?? "—"}</td>
                  <td>{formatDate(app.applicationDate)}</td>
                  <td>
                    {STATUS_TRANSITIONS[app.status] ? (
                      <>
                        <select
                          className="form-select form-select-sm"
                          value={app.status}
                          disabled={savingId === app.applicationId}
                          onChange={(e) =>
                            handleQuickStatusUpdate(app.applicationId, e.target.value)
                          }
                          style={{
                            minWidth: 185,
                            fontWeight: 600,
                            fontSize: "0.78rem",
                            cursor: "pointer",
                            ...STATUS_SELECT_STYLE[app.status],
                          }}
                        >
                          <option value={app.status}>{app.status}</option>
                          {STATUS_TRANSITIONS[app.status].map((s) => (
                            <option key={s} value={s}>{s}</option>
                          ))}
                        </select>
                        {savingId === app.applicationId && (
                          <span className="ms-1 text-muted" style={{ fontSize: "0.72rem" }}>
                            Saving…
                          </span>
                        )}
                      </>
                    ) : (
                      <span
                        style={{
                          display: "inline-block",
                          padding: "5px 14px",
                          borderRadius: 6,
                          fontWeight: 700,
                          fontSize: "0.78rem",
                          ...FINAL_STATUS_STYLE[app.status],
                        }}
                      >
                        {app.status === "Offer Accepted" ? "✓" : app.status === "Offer Released" ? "📩" : app.status === "Withdrawn" ? "–" : "✗"} {app.status}
                      </span>
                    )}
                  </td>
                  <td>
                    <button
                      className="btn btn-sm action-delete"
                      onClick={() => handleDelete(app.applicationId)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      )}
    </Layout>
  );
}

function SummaryCell({ label, value, border }) {
  return (
    <div className={`px-4 py-2 bg-light ${border ? "border-end" : ""}`}>
      <div
        className="text-muted"
        style={{ fontSize: "0.7rem", textTransform: "uppercase", letterSpacing: "0.05em" }}
      >
        {label}
      </div>
      <div className="fw-semibold small">{value}</div>
    </div>
  );
}

export default Applications;
```

---

## `frontend/src/App.jsx`

_MODIFIED — /admin/penalties route_

```jsx
import { BrowserRouter, Routes, Route } from "react-router-dom";

// Activate the axios interceptors on app load
import "./services/authService";

import Dashboard    from "./pages/Dashboard";
import Companies    from "./pages/Companies";
import Students     from "./pages/Students";
import Drives       from "./pages/Drives";
import Applications from "./pages/Applications";
import Login          from "./pages/Login";
import AccessDenied   from "./pages/AccessDenied";
import ChangePassword from "./pages/ChangePassword";
import ManageOfficers from "./pages/ManageOfficers";
import FormSubmissions from "./pages/FormSubmissions";
import Penalties       from "./pages/Penalties";

import StudentDashboard from "./pages/student/StudentDashboard";
import MyProfile        from "./pages/student/MyProfile";
import EligibleDrives   from "./pages/student/EligibleDrives";
import MyApplications   from "./pages/student/MyApplications";
import PlacementStatus  from "./pages/student/PlacementStatus";

import StudentRoute    from "./routes/StudentRoute";
import AdminRoute      from "./routes/AdminRoute";
import SuperAdminRoute from "./routes/SuperAdminRoute";
import RoleRedirect    from "./routes/RoleRedirect";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* ── Public ──────────────────────────────────────────────── */}
        <Route path="/login"         element={<Login />} />
        <Route path="/access-denied" element={<AccessDenied />} />

        {/* ── Root: redirect to role-appropriate dashboard ─────────── */}
        <Route path="/" element={<RoleRedirect />} />

        {/* ── Student portal ───────────────────────────────────────── */}
        <Route path="/student/change-password" element={
          <StudentRoute><ChangePassword /></StudentRoute>
        } />
        <Route path="/student/dashboard" element={
          <StudentRoute><StudentDashboard /></StudentRoute>
        } />
        <Route path="/student/profile" element={
          <StudentRoute><MyProfile /></StudentRoute>
        } />
        <Route path="/student/eligible-drives" element={
          <StudentRoute><EligibleDrives /></StudentRoute>
        } />
        <Route path="/student/applications" element={
          <StudentRoute><MyApplications /></StudentRoute>
        } />
        <Route path="/student/placement-status" element={
          <StudentRoute><PlacementStatus /></StudentRoute>
        } />

        {/* ── Admin / Placement Officer ────────────────────────────── */}
        <Route path="/admin/change-password" element={
          <AdminRoute><ChangePassword /></AdminRoute>
        } />
        <Route path="/admin/dashboard" element={
          <AdminRoute><Dashboard /></AdminRoute>
        } />
        <Route path="/admin/students" element={
          <AdminRoute><Students /></AdminRoute>
        } />
        <Route path="/admin/companies" element={
          <AdminRoute><Companies /></AdminRoute>
        } />
        <Route path="/admin/drives" element={
          <AdminRoute><Drives /></AdminRoute>
        } />
        <Route path="/admin/applications" element={
          <AdminRoute><Applications /></AdminRoute>
        } />
        <Route path="/admin/forms" element={
          <AdminRoute><FormSubmissions /></AdminRoute>
        } />
        <Route path="/admin/penalties" element={
          <AdminRoute><Penalties /></AdminRoute>
        } />
        <Route path="/admin/officers" element={
          <SuperAdminRoute><ManageOfficers /></SuperAdminRoute>
        } />

        {/* ── Catch-all ────────────────────────────────────────────── */}
        <Route path="*" element={<RoleRedirect />} />

      </Routes>
    </BrowserRouter>
  );
}

export default App;
```

---

## `frontend/src/components/Sidebar.jsx`

_MODIFIED — Penalties nav item_

```jsx
import { Link, useLocation } from "react-router-dom";
import { getRole } from "../services/authService";

const ALL_ITEMS = [
  // ── Admin / Placement Officer ──────────────────────────────────────────────
  { path: "/admin/dashboard",    label: "Dashboard",    icon: "📊", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/students",     label: "Students",     icon: "👥", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/companies",    label: "Companies",    icon: "🏢", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/drives",       label: "Drives",       icon: "📋", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/applications", label: "Applications", icon: "📄", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/forms",        label: "Forms",        icon: "📝", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/penalties",    label: "Penalties",    icon: "⚖️", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/officers",     label: "Officers",      icon: "🛡️", roles: ["ADMIN"] },

  // ── Student portal ─────────────────────────────────────────────────────────
  { path: "/student/dashboard",        label: "Dashboard",       icon: "📊", roles: ["STUDENT"] },
  { path: "/student/profile",          label: "My Profile",      icon: "👤", roles: ["STUDENT"] },
  { path: "/student/eligible-drives",  label: "Eligible Drives", icon: "🎯", roles: ["STUDENT"] },
  { path: "/student/applications",     label: "My Applications", icon: "📋", roles: ["STUDENT"] },
  { path: "/student/placement-status", label: "Placement Status",icon: "🏆", roles: ["STUDENT"] },
];

function Sidebar() {
  const location = useLocation();
  const role     = getRole();

  const items = ALL_ITEMS.filter((item) => item.roles.includes(role));

  return (
    <div
      className="bg-light border-end d-flex flex-column"
      style={{ width: 210, minHeight: "100vh", flexShrink: 0 }}
    >
      <div className="p-3 pt-4">
        <p
          className="mb-3"
          style={{
            fontSize: "0.62rem",
            fontWeight: 700,
            color: "#94a3b8",
            textTransform: "uppercase",
            letterSpacing: "0.07em",
            paddingLeft: 4,
          }}
        >
          {role === "STUDENT" ? "Student Portal" : "Navigation"}
        </p>

        <ul className="nav flex-column gap-1">
          {items.map((item) => {
            const active =
              item.path === "/admin/dashboard" || item.path === "/student/dashboard"
                ? location.pathname === item.path
                : location.pathname.startsWith(item.path);

            return (
              <li key={item.path} className="nav-item">
                <Link
                  className="nav-link d-flex align-items-center gap-2"
                  to={item.path}
                  style={{
                    fontSize: "0.84rem",
                    fontWeight: active ? 700 : 500,
                    color: active ? "#2563eb" : "#475569",
                    background: active ? "#eff6ff" : "transparent",
                    borderRadius: 7,
                    padding: "7px 10px",
                    transition: "background 0.12s ease, color 0.12s ease",
                  }}
                >
                  <span style={{ fontSize: "1rem" }}>{item.icon}</span>
                  {item.label}
                  {active && (
                    <span
                      style={{
                        marginLeft: "auto",
                        width: 4,
                        height: 4,
                        borderRadius: "50%",
                        background: "#2563eb",
                      }}
                    />
                  )}
                </Link>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
}

export default Sidebar;
```

