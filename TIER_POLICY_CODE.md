# Tier A/B/C Rename + CTC-Based Upgradation Policy — All Edited Code

Generated 2026-07-20. Full contents of every file created or modified for this change.

## Policy

**Job categories (assigned from the Google Form's CTC):**

| Tier | CTC |
|---|---|
| A | 0 – 5.99 LPA |
| B | 6.0 – 11.99 LPA |
| C | >= 12.0 LPA |

**Upgradation policy (replaces the old label-based tier comparison):**

| Current offer | Allowed next offer |
|---|---|
| Unplaced | Anything — this is first placement, not upgradation |
| Tier A | new CTC > 1.5x current CTC |
| Tier B | new CTC >= max(1.5x current CTC, just-over-12 LPA) |
| Tier C | new CTC > 2.5x current CTC |

**Upgradation is one-time.** A student may hold at most two accepted offers ever:
first placement, plus one upgrade. Accepting a second offer withdraws every other
active application unconditionally — there is no third offer. Applying to a same-tier
or lower-tier drive while already placed is blocked outright with an explicit message.

---

## `backend/src/main/java/com/pmrs/backend/util/TierPolicy.java`

_NEW — CTC-to-tier mapping, tier rank, and the upgradation-block-reason calculation shared by student portal + penalty service_

```java
package com.pmrs.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Job categories and the one-time upgradation policy.
 *
 * Tiers are assigned from the CTC offered:
 *   A: 0 – 5.99 LPA · B: 6.0 – 11.99 LPA · C: ≥ 12.0 LPA
 *
 * A placed student may participate further only if the new CTC clears the
 * category's threshold, and upgradation is a one-time option:
 *   A: new CTC  >  1.5 × current CTC
 *   B: new CTC  ≥  max(1.5 × current CTC, "greater than 12 LPA")
 *   C: new CTC  >  2.5 × current CTC
 */
public final class TierPolicy {

    private static final BigDecimal SIX     = BigDecimal.valueOf(6);
    private static final BigDecimal TWELVE  = BigDecimal.valueOf(12);
    private static final BigDecimal ONE_5   = new BigDecimal("1.5");
    private static final BigDecimal TWO_5   = new BigDecimal("2.5");

    private TierPolicy() {}

    /** CTC → "A" / "B" / "C"; null CTC → null. */
    public static String tierForCtc(BigDecimal ctc) {
        if (ctc == null) return null;
        if (ctc.compareTo(SIX) < 0)    return "A";
        if (ctc.compareTo(TWELVE) < 0) return "B";
        return "C";
    }

    /** A=1, B=2, C=3; anything else (null, "Unplaced", legacy) = 0. */
    public static int rank(String tier) {
        if (tier == null) return 0;
        return switch (tier) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            default  -> 0;
        };
    }

    /**
     * Why a student holding ONE accepted offer may not apply to a drive —
     * or null when the upgradation policy allows it. The one-time check
     * (no third offer, ever) belongs to the caller, which knows the
     * accepted-offer count.
     *
     * @param currentCtc  CTC of the student's accepted offer (may be null)
     * @param newCtc      CTC of the drive being applied to (may be null)
     * @param newTierFallback tier to assume for the drive when newCtc is null
     */
    public static String upgradationBlockReason(BigDecimal currentCtc,
                                                BigDecimal newCtc,
                                                String newTierFallback) {
        if (newCtc == null && rank(newTierFallback) == 0) {
            return "This drive has no CTC recorded — placed students cannot apply"
                 + " until the placement office sets the package.";
        }
        if (currentCtc == null) {
            return "Your accepted offer has no CTC recorded — contact the placement"
                 + " office before applying further.";
        }

        String currentTier = tierForCtc(currentCtc);
        String newTier     = newCtc != null ? tierForCtc(newCtc) : newTierFallback;

        if (rank(newTier) < rank(currentTier)) {
            return "You cannot apply to a Tier " + newTier + " company below your"
                 + " current Tier " + currentTier + " placement.";
        }
        if (rank(newTier) == rank(currentTier)) {
            return "You are already placed in a Tier " + currentTier + " company —"
                 + " you cannot apply to another Tier " + currentTier + " company.";
        }
        if (newCtc == null) {
            return "This drive has no CTC recorded — placed students cannot apply"
                 + " until the placement office sets the package.";
        }

        return switch (currentTier) {
            case "A" -> {
                BigDecimal threshold = mul(currentCtc, ONE_5);
                yield newCtc.compareTo(threshold) > 0 ? null
                        : "Your current offer is " + plain(currentCtc) + " LPA (Tier A) — a new"
                        + " offer must exceed " + plain(threshold) + " LPA (1.5× your current CTC).";
            }
            case "B" -> {
                BigDecimal th15 = mul(currentCtc, ONE_5);
                if (th15.compareTo(TWELVE) >= 0) {
                    yield newCtc.compareTo(th15) >= 0 ? null
                            : "Your current offer is " + plain(currentCtc) + " LPA (Tier B) — a new"
                            + " offer must be at least " + plain(th15) + " LPA (1.5× your current CTC).";
                }
                yield newCtc.compareTo(TWELVE) > 0 ? null
                        : "Your current offer is " + plain(currentCtc) + " LPA (Tier B) — a new"
                        + " offer must be greater than 12 LPA.";
            }
            case "C" -> {
                BigDecimal threshold = mul(currentCtc, TWO_5);
                yield newCtc.compareTo(threshold) > 0 ? null
                        : "Your current offer is " + plain(currentCtc) + " LPA (Tier C) — a new"
                        + " offer must exceed " + plain(threshold) + " LPA (2.5× your current CTC).";
            }
            default -> null; // unreachable: currentCtc non-null always maps to A/B/C
        };
    }

    /** The message shown once the one-time upgradation has been consumed. */
    public static String upgradationUsedMessage() {
        return "Upgradation is a one-time option — you have already used your upgradation.";
    }

    private static BigDecimal mul(BigDecimal a, BigDecimal factor) {
        return a.multiply(factor).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private static String plain(BigDecimal v) {
        return v.stripTrailingZeros().toPlainString();
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/config/TierNameMigration.java`

_NEW — startup runner that renames legacy DB values (Normal/Dream/Super Dream) to A/B/C; no-op once migrated_

```java
package com.pmrs.backend.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-time rename of the legacy placement-tier labels to the job-category
 * codes (A/B/C) introduced by the CTC-based tier policy. The CTC boundaries
 * are unchanged (< 6 / 6–11.99 / ≥ 12 LPA), so existing rows only need their
 * label renamed, never recomputed:
 *
 *   Companies.tier / Students.placement_tier:
 *     "Normal" → "A", "Dream" → "B", "Super Dream" → "C"
 *
 * Runs on every startup but is a no-op once migrated (the WHERE clause only
 * matches legacy values), so it is safe to leave in place indefinitely.
 */
@Component
@Order(1) // before DataInitializer, so seed data always sees the new codes
public class TierNameMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TierNameMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int companies = renameTier("Companies", "tier");
        int students  = renameTier("Students", "placement_tier");
        if (companies > 0 || students > 0) {
            log.info("Tier naming migration: renamed {} compan{}, {} student(s) to the A/B/C scheme.",
                    companies, companies == 1 ? "y" : "ies", students);
        }
    }

    private int renameTier(String table, String column) {
        int updated = 0;
        updated += entityManager.createNativeQuery(
                "UPDATE " + table + " SET " + column + " = 'A' WHERE " + column + " = 'Normal'")
                .executeUpdate();
        updated += entityManager.createNativeQuery(
                "UPDATE " + table + " SET " + column + " = 'B' WHERE " + column + " = 'Dream'")
                .executeUpdate();
        updated += entityManager.createNativeQuery(
                "UPDATE " + table + " SET " + column + " = 'C' WHERE " + column + " = 'Super Dream'")
                .executeUpdate();
        return updated;
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/GoogleFormSyncService.java`

_MODIFIED — tierForPackage now returns A/B/C via TierPolicy_

```java
package com.pmrs.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.pmrs.backend.entity.DriveFormSubmission;
import com.pmrs.backend.repository.DriveFormSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pulls new drive submissions from the Google Form's linked response Sheet
 * into the DriveFormSubmissions staging table.
 *
 * <p>Columns are located by the sheet's header row (row 1), matching each
 * header against keywords ("company", "cgpa", "backlog", ...), so the form's
 * question order doesn't matter. If the first row doesn't look like a header,
 * falls back to the fixed A–N order:
 * A Timestamp · B Company Name · C Sector · D Tier · E Website · F HR Name ·
 * G HR Email · H HR Phone · I Drive Date · J Role Offered · K Package (LPA) ·
 * L Drive Type · M Min CGPA · N Max Backlogs.
 *
 * <p>Runs on a schedule and on demand via POST /form-submissions/sync.
 * If the spreadsheet ID or the service-account key isn't configured yet,
 * every sync is a no-op that logs a warning instead of failing the app.
 */
@Service
public class GoogleFormSyncService {

    private static final Logger log = LoggerFactory.getLogger(GoogleFormSyncService.class);

    private static final String CLASSPATH_PREFIX = "classpath:";

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            // Google Sheets renders form dates as M/d/yyyy (single digits, US order)
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
    };

    private final DriveFormSubmissionRepository submissionRepository;

    @Value("${google.forms.spreadsheet-id:}")
    private String spreadsheetId;

    @Value("${google.forms.sheet-range:Form Responses 1!A1:AT}")
    private String sheetRange;

    @Value("${google.forms.credentials-path:classpath:google-service-account.json}")
    private String credentialsPath;

    private Sheets sheetsClient;

    public GoogleFormSyncService(DriveFormSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Scheduled(fixedDelayString = "${google.forms.poll-interval-ms:300000}")
    public void scheduledSync() {
        try {
            syncResponses();
        } catch (Exception e) {
            // Scheduled runs must never take the app down over a transient
            // network/API failure — log and try again on the next tick.
            log.warn("Google Form sync failed: {}", e.getMessage());
        }
    }

    /**
     * Reads the response sheet and stores any row not seen before as a
     * PENDING submission.
     *
     * @return number of new submissions saved
     */
    public synchronized int syncResponses() throws Exception {
        if (spreadsheetId == null || spreadsheetId.isBlank()) {
            log.warn("google.forms.spreadsheet-id is not set — skipping Google Form sync.");
            return 0;
        }

        ValueRange response = getSheetsClient().spreadsheets().values()
                .get(spreadsheetId, sheetRange)
                .execute();

        List<List<Object>> rows = response.getValues();
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        ColumnMap cols = ColumnMap.fromHeader(rows.get(0));
        int firstDataRow = 1;
        if (cols == null) {
            log.warn("First sheet row doesn't look like a header — assuming the fixed A–N column order.");
            cols = ColumnMap.positional();
            firstDataRow = 0;
        }

        int created = 0;
        for (int i = firstDataRow; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            String timestamp   = cell(row, cols.timestamp);
            String companyName = cell(row, cols.companyName);
            String roleOffered = cell(row, cols.roleOffered);

            // Blank filler rows at the bottom of the sheet
            if (timestamp.isEmpty() && companyName.isEmpty()) {
                continue;
            }

            String responseKey = timestamp + "|" + companyName + "|" + roleOffered;
            if (submissionRepository.existsByResponseKey(responseKey)) {
                continue;
            }

            DriveFormSubmission s = new DriveFormSubmission();
            s.setResponseKey(responseKey);
            s.setSubmittedAt(timestamp);
            s.setCompanyName(companyName);
            BigDecimal packageLpa = parseDecimal(cell(row, cols.packageLpa));
            s.setSector(cell(row, cols.sector));
            s.setTier(tierForPackage(packageLpa));
            s.setWebsite(cell(row, cols.website));
            s.setHrName(cell(row, cols.hrName));
            s.setHrEmail(cell(row, cols.hrEmail));
            s.setHrPhone(cell(row, cols.hrPhone));
            s.setDriveDate(parseDate(cell(row, cols.driveDate)));
            s.setRoleOffered(roleOffered);
            s.setPackageLpa(packageLpa);
            s.setDriveType(cell(row, cols.driveType));
            s.setMinCgpa(parseDouble(cell(row, cols.minCgpa)));
            s.setMaxBacklogs(parseBacklogs(cell(row, cols.maxBacklogs)));
            s.setStatus(DriveFormSubmission.STATUS_PENDING);

            submissionRepository.save(s);
            created++;
        }

        if (created > 0) {
            log.info("Google Form sync: {} new drive submission(s) staged.", created);
        }
        return created;
    }

    private Sheets getSheetsClient() throws Exception {
        if (sheetsClient == null) {
            GoogleCredentials credentials;
            try (InputStream in = openCredentials()) {
                credentials = GoogleCredentials.fromStream(in)
                        .createScoped(List.of(SheetsScopes.SPREADSHEETS_READONLY));
            }
            sheetsClient = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("PRMS")
                    .build();
        }
        return sheetsClient;
    }

    private InputStream openCredentials() throws Exception {
        if (credentialsPath.startsWith(CLASSPATH_PREFIX)) {
            String resource = credentialsPath.substring(CLASSPATH_PREFIX.length());
            InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
            if (in == null) {
                throw new IllegalStateException(
                        "Service-account key not found on classpath: " + resource
                        + " — put the JSON key in src/main/resources or set google.forms.credentials-path.");
            }
            return in;
        }
        return new FileInputStream(credentialsPath);
    }

    private static String cell(List<Object> row, int index) {
        if (index < 0 || index >= row.size() || row.get(index) == null) {
            return "";
        }
        return row.get(index).toString().trim();
    }

    /**
     * Maps each submission field to its column index in the sheet.
     * An index of -1 means "no such column"; {@link #cell} returns "" for it.
     */
    private static final class ColumnMap {
        int timestamp = -1, companyName = -1, sector = -1, tier = -1, website = -1,
            hrName = -1, hrEmail = -1, hrPhone = -1, driveDate = -1, roleOffered = -1,
            packageLpa = -1, driveType = -1, minCgpa = -1, maxBacklogs = -1;

        /** The legacy fixed A–N layout. */
        static ColumnMap positional() {
            ColumnMap m = new ColumnMap();
            m.timestamp = 0; m.companyName = 1; m.sector = 2; m.tier = 3; m.website = 4;
            m.hrName = 5; m.hrEmail = 6; m.hrPhone = 7; m.driveDate = 8; m.roleOffered = 9;
            m.packageLpa = 10; m.driveType = 11; m.minCgpa = 12; m.maxBacklogs = 13;
            return m;
        }

        /**
         * Builds a map from the header row. Pass 1 matches the institute
         * intake form's exact question texts; pass 2 fills any still-unmapped
         * field by generic keywords so renamed/reordered questions still land.
         * Returns null if the row doesn't look like a header (no timestamp or
         * company column).
         */
        static ColumnMap fromHeader(List<Object> headerRow) {
            ColumnMap m = new ColumnMap();
            String[] headers = new String[headerRow.size()];
            for (int i = 0; i < headerRow.size(); i++) {
                Object raw = headerRow.get(i);
                headers[i] = raw == null ? ""
                        : raw.toString().trim().toLowerCase().replaceAll("\\s+", " ");
            }

            // Pass 1 — exact/prefix matches for the known intake-form questions.
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i];
                if (h.isEmpty()) {
                    continue;
                }
                if (m.timestamp < 0 && h.equals("timestamp")) {
                    m.timestamp = i;
                } else if (m.companyName < 0 && h.equals("name of company")) {
                    m.companyName = i;
                } else if (m.sector < 0 && h.equals("nature of business")) {
                    m.sector = i;
                } else if (m.website < 0 && h.equals("company website")) {
                    m.website = i;
                } else if (m.hrName < 0 && h.equals("contact person name")) {
                    m.hrName = i;
                } else if (m.hrEmail < 0 && h.startsWith("contact person e-mail")) {
                    m.hrEmail = i;
                } else if (m.hrPhone < 0 && h.startsWith("contact number 1")) {
                    m.hrPhone = i;
                } else if (m.driveDate < 0 && h.startsWith("tentative date of online/offline exam")) {
                    m.driveDate = i;
                } else if (m.roleOffered < 0 && h.startsWith("designation offered")) {
                    m.roleOffered = i;
                } else if (m.driveType < 0 && h.equals("role offered")) {
                    // "ROLE OFFERED" on this form is Full-time vs Internship.
                    m.driveType = i;
                } else if (m.packageLpa < 0 && h.startsWith("tentative ctc")) {
                    m.packageLpa = i;
                } else if (m.minCgpa < 0 && h.equals("eligibility cgpa")) {
                    m.minCgpa = i;
                } else if (m.maxBacklogs < 0 && h.startsWith("active backlog")) {
                    m.maxBacklogs = i;
                }
            }

            // Pass 2 — generic keywords for whatever pass 1 didn't find.
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i];
                if (h.isEmpty()) {
                    continue;
                }
                if (m.timestamp < 0 && h.contains("timestamp")) {
                    m.timestamp = i;
                } else if (m.maxBacklogs < 0 && h.contains("backlog")) {
                    m.maxBacklogs = i;
                } else if (m.minCgpa < 0 && (h.contains("cgpa") || h.contains("gpa"))) {
                    m.minCgpa = i;
                } else if (m.packageLpa < 0 && (h.contains("package") || h.contains("ctc")
                        || h.contains("lpa") || h.contains("salary"))) {
                    m.packageLpa = i;
                } else if (m.hrEmail < 0 && h.contains("mail") && h.contains("contact")) {
                    m.hrEmail = i;
                } else if (m.hrPhone < 0 && (h.contains("phone") || h.contains("mobile")
                        || h.contains("contact number") || h.contains("contact no"))) {
                    m.hrPhone = i;
                } else if (m.website < 0 && (h.contains("website") || h.contains("url"))) {
                    m.website = i;
                } else if (m.tier < 0 && h.contains("tier")) {
                    m.tier = i;
                } else if (m.sector < 0 && (h.contains("sector") || h.contains("industry")
                        || h.contains("nature of business"))) {
                    m.sector = i;
                } else if (m.roleOffered < 0 && !h.contains("contact person")
                        && !h.contains("internship")
                        && (h.contains("role") || h.contains("position")
                            || h.contains("designation") || h.contains("job title"))) {
                    m.roleOffered = i;
                } else if (m.driveDate < 0 && h.contains("date")
                        && !h.contains("establishment") && !h.contains("internship")) {
                    m.driveDate = i;
                } else if (m.hrName < 0 && h.contains("name")
                        && (h.contains("hr") || h.contains("contact") || h.contains("recruiter")
                            || h.contains("person") || h.contains("poc"))) {
                    m.hrName = i;
                } else if (m.companyName < 0 && (h.contains("company")
                        || h.contains("organization") || h.contains("organisation"))) {
                    m.companyName = i;
                } else if (m.driveType < 0 && (h.contains("type") || h.contains("mode"))) {
                    m.driveType = i;
                }
            }

            if (m.timestamp < 0 || m.companyName < 0) {
                return null;
            }
            return m;
        }
    }

    private static LocalDate parseDate(String raw) {
        if (raw.isEmpty()) {
            return null;
        }
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(raw, fmt);
            } catch (Exception ignored) {
                // try the next format
            }
        }
        log.warn("Could not parse drive date '{}' from Google Form response.", raw);
        return null;
    }

    private static BigDecimal parseDecimal(String raw) {
        String cleaned = raw.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String raw) {
        BigDecimal d = parseDecimal(raw);
        return d == null ? null : d.doubleValue();
    }

    private static Integer parseInt(String raw) {
        BigDecimal d = parseDecimal(raw);
        return d == null ? null : d.intValue();
    }

    /**
     * Tier is derived from the offered CTC using the job-categories policy:
     * A below 6 LPA, B 6–11.99 LPA, C at or above 12 LPA.
     */
    private static String tierForPackage(BigDecimal packageLpa) {
        String tier = com.pmrs.backend.util.TierPolicy.tierForCtc(packageLpa);
        return tier == null ? "" : tier;
    }

    /**
     * The form asks "Active backlog allowed?" as Yes/No: Yes → no limit
     * (null), No → 0. Numeric answers are stored as-is.
     */
    private static Integer parseBacklogs(String raw) {
        if (raw.equalsIgnoreCase("yes")) {
            return null;
        }
        if (raw.equalsIgnoreCase("no")) {
            return 0;
        }
        return parseInt(raw);
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/DriveFormServiceImpl.java`

_MODIFIED — normaliseTier accepts/defaults to A/B/C_

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
                    // Company.tier is mandatory — default new companies to Tier A
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
        String t = tier == null ? "" : tier.trim().toUpperCase();
        return switch (t) {
            case "A", "B", "C" -> t;
            default            -> "A";
        };
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/StudentPortalServiceImpl.java`

_MODIFIED — validatePlacementRestriction replaced by CTC-ratio + one-time validateUpgradationPolicy; acceptOffer/declineAcceptedOffer/revoke logic rewritten around accepted-offer CTC instead of tier labels_

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
        if (!"Offer Released".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException(
                    "Cannot accept: status is '" + app.getStatus() + "', expected 'Offer Released'.");
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
}```

---

## `backend/src/main/java/com/pmrs/backend/service/PenaltyServiceImpl.java`

_MODIFIED — wouldBeEligible (skip-drives counter) uses the same CTC-ratio + one-time policy; now depends on ApplicationRepository_

```java
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
```

---

## `backend/src/main/java/com/pmrs/backend/dto/StudentProfileDTO.java`

_MODIFIED — doc comment updated to A/B/C_

```java
package com.pmrs.backend.dto;

public class StudentProfileDTO {

    private Integer studentId;
    private String  rollNo;
    private String  name;
    private String  email;
    private String  phone;
    private String  branch;
    private String  program;
    private Integer batchYear;
    private Double  cgpa;
    private Integer activeBacklogs;
    private String  placementTier;    // Actual placement outcome: Unplaced / A / B / C
    private String  placementStatus;  // "Placed" | "Not Placed"

    public StudentProfileDTO() {}

    public StudentProfileDTO(Integer studentId, String rollNo, String name, String email, String phone,
                             String branch, String program, Integer batchYear,
                             Double cgpa, Integer activeBacklogs,
                             String placementTier, String placementStatus) {
        this.studentId       = studentId;
        this.rollNo          = rollNo;
        this.name            = name;
        this.email           = email;
        this.phone           = phone;
        this.branch          = branch;
        this.program         = program;
        this.batchYear       = batchYear;
        this.cgpa            = cgpa;
        this.activeBacklogs  = activeBacklogs;
        this.placementTier   = placementTier;
        this.placementStatus = placementStatus;
    }

    public Integer getStudentId()       { return studentId; }
    public void    setStudentId(Integer v) { this.studentId = v; }

    public String  getRollNo()          { return rollNo; }
    public void    setRollNo(String v)  { this.rollNo = v; }

    public String  getName()            { return name; }
    public void    setName(String v)    { this.name = v; }

    public String  getEmail()           { return email; }
    public void    setEmail(String v)   { this.email = v; }

    public String  getPhone()           { return phone; }
    public void    setPhone(String v)   { this.phone = v; }

    public String  getBranch()          { return branch; }
    public void    setBranch(String v)  { this.branch = v; }

    public String  getProgram()         { return program; }
    public void    setProgram(String v) { this.program = v; }

    public Integer getBatchYear()           { return batchYear; }
    public void    setBatchYear(Integer v)  { this.batchYear = v; }

    public Double  getCgpa()            { return cgpa; }
    public void    setCgpa(Double v)    { this.cgpa = v; }

    public Integer getActiveBacklogs()          { return activeBacklogs; }
    public void    setActiveBacklogs(Integer v) { this.activeBacklogs = v; }

    public String  getPlacementTier()             { return placementTier; }
    public void    setPlacementTier(String v)     { this.placementTier = v; }

    public String  getPlacementStatus()           { return placementStatus; }
    public void    setPlacementStatus(String v)   { this.placementStatus = v; }
}```

---

## `backend/src/main/java/com/pmrs/backend/dto/DashboardDTO.java`

_MODIFIED — superDreamCount/dreamCount/normalCount renamed to tierCCount/tierBCount/tierACount_

```java
package com.pmrs.backend.dto;

import java.util.List;

public class DashboardDTO {

    // KPI
    private long totalStudents;
    private long totalCompanies;
    private long totalDrives;
    private long totalApplications;
    private long totalPlacedStudents;
    private double placementPercentage;

    // Tier distribution
    private long tierCCount;
    private long tierBCount;
    private long tierACount;
    private long unplacedCount;

    // Package analytics
    private double highestPackage;
    private double averagePackage;
    private double medianPackage;

    // Drive stats
    private long upcomingDrives;
    private long completedDrives;
    private long cancelledDrives;

    // Eligibility snapshot (renamed from eligibleStudents)
    private long noBacklogStudents;
    private long dreamEligibleStudents;
    private long superDreamEligibleStudents;
    private long studentsWithBacklogs;

    // Filter option lists (always unfiltered — for populating dropdowns)
    private List<Integer> availableYears;
    private List<String> availableBranches;

    // Quick insight fields
    private String mostAppliedCompanyName;
    private long mostAppliedCompanyCount;
    private String lastUpdated;

    // Charts / lists
    private List<TrendPointDTO> placementTrends;
    private List<BranchPlacementDTO> branchWisePlacement;
    private List<RecruiterDTO> topRecruiters;
    private List<ActivityDTO> recentActivities;

    // ─── Getters / Setters ─────────────────────────────────────────────────────

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(long totalCompanies) { this.totalCompanies = totalCompanies; }

    public long getTotalDrives() { return totalDrives; }
    public void setTotalDrives(long totalDrives) { this.totalDrives = totalDrives; }

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getTotalPlacedStudents() { return totalPlacedStudents; }
    public void setTotalPlacedStudents(long totalPlacedStudents) { this.totalPlacedStudents = totalPlacedStudents; }

    public double getPlacementPercentage() { return placementPercentage; }
    public void setPlacementPercentage(double placementPercentage) { this.placementPercentage = placementPercentage; }

    public long getTierCCount() { return tierCCount; }
    public void setTierCCount(long tierCCount) { this.tierCCount = tierCCount; }

    public long getTierBCount() { return tierBCount; }
    public void setTierBCount(long tierBCount) { this.tierBCount = tierBCount; }

    public long getTierACount() { return tierACount; }
    public void setTierACount(long tierACount) { this.tierACount = tierACount; }

    public long getUnplacedCount() { return unplacedCount; }
    public void setUnplacedCount(long unplacedCount) { this.unplacedCount = unplacedCount; }

    public double getHighestPackage() { return highestPackage; }
    public void setHighestPackage(double highestPackage) { this.highestPackage = highestPackage; }

    public double getAveragePackage() { return averagePackage; }
    public void setAveragePackage(double averagePackage) { this.averagePackage = averagePackage; }

    public double getMedianPackage() { return medianPackage; }
    public void setMedianPackage(double medianPackage) { this.medianPackage = medianPackage; }

    public long getUpcomingDrives() { return upcomingDrives; }
    public void setUpcomingDrives(long upcomingDrives) { this.upcomingDrives = upcomingDrives; }

    public long getCompletedDrives() { return completedDrives; }
    public void setCompletedDrives(long completedDrives) { this.completedDrives = completedDrives; }

    public long getCancelledDrives() { return cancelledDrives; }
    public void setCancelledDrives(long cancelledDrives) { this.cancelledDrives = cancelledDrives; }

    public long getNoBacklogStudents() { return noBacklogStudents; }
    public void setNoBacklogStudents(long noBacklogStudents) { this.noBacklogStudents = noBacklogStudents; }

    public long getDreamEligibleStudents() { return dreamEligibleStudents; }
    public void setDreamEligibleStudents(long dreamEligibleStudents) { this.dreamEligibleStudents = dreamEligibleStudents; }

    public long getSuperDreamEligibleStudents() { return superDreamEligibleStudents; }
    public void setSuperDreamEligibleStudents(long v) { this.superDreamEligibleStudents = v; }

    public long getStudentsWithBacklogs() { return studentsWithBacklogs; }
    public void setStudentsWithBacklogs(long studentsWithBacklogs) { this.studentsWithBacklogs = studentsWithBacklogs; }

    public String getMostAppliedCompanyName() { return mostAppliedCompanyName; }
    public void setMostAppliedCompanyName(String v) { this.mostAppliedCompanyName = v; }

    public long getMostAppliedCompanyCount() { return mostAppliedCompanyCount; }
    public void setMostAppliedCompanyCount(long v) { this.mostAppliedCompanyCount = v; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public List<Integer> getAvailableYears() { return availableYears; }
    public void setAvailableYears(List<Integer> availableYears) { this.availableYears = availableYears; }

    public List<String> getAvailableBranches() { return availableBranches; }
    public void setAvailableBranches(List<String> availableBranches) { this.availableBranches = availableBranches; }

    public List<TrendPointDTO> getPlacementTrends() { return placementTrends; }
    public void setPlacementTrends(List<TrendPointDTO> placementTrends) { this.placementTrends = placementTrends; }

    public List<BranchPlacementDTO> getBranchWisePlacement() { return branchWisePlacement; }
    public void setBranchWisePlacement(List<BranchPlacementDTO> branchWisePlacement) { this.branchWisePlacement = branchWisePlacement; }

    public List<RecruiterDTO> getTopRecruiters() { return topRecruiters; }
    public void setTopRecruiters(List<RecruiterDTO> topRecruiters) { this.topRecruiters = topRecruiters; }

    public List<ActivityDTO> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<ActivityDTO> recentActivities) { this.recentActivities = recentActivities; }

    // ─── Nested DTOs ───────────────────────────────────────────────────────────

    public static class TrendPointDTO {
        private int year;
        private long totalStudents;
        private long placedStudents;
        private double placementPercentage;

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

        public long getPlacedStudents() { return placedStudents; }
        public void setPlacedStudents(long placedStudents) { this.placedStudents = placedStudents; }

        public double getPlacementPercentage() { return placementPercentage; }
        public void setPlacementPercentage(double placementPercentage) { this.placementPercentage = placementPercentage; }
    }

    public static class BranchPlacementDTO {
        private String branch;
        private long totalStudents;
        private long placedStudents;
        private double placementPercentage;

        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }

        public long getTotalStudents() { return totalStudents; }
        public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

        public long getPlacedStudents() { return placedStudents; }
        public void setPlacedStudents(long placedStudents) { this.placedStudents = placedStudents; }

        public double getPlacementPercentage() { return placementPercentage; }
        public void setPlacementPercentage(double v) { this.placementPercentage = v; }
    }

    public static class RecruiterDTO {
        private String companyName;
        private String tier;
        private long studentsHired;

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getTier() { return tier; }
        public void setTier(String tier) { this.tier = tier; }

        public long getStudentsHired() { return studentsHired; }
        public void setStudentsHired(long studentsHired) { this.studentsHired = studentsHired; }
    }

    public static class ActivityDTO {
        private String studentName;
        private String companyName;
        private String status;
        private String applicationDate;

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getApplicationDate() { return applicationDate; }
        public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/service/DashboardServiceImpl.java`

_MODIFIED — tier distribution counts filter on A/B/C_

```java
package com.pmrs.backend.service;

import com.pmrs.backend.dto.DashboardDTO;
import com.pmrs.backend.entity.Application;
import com.pmrs.backend.entity.Drive;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.repository.ApplicationRepository;
import com.pmrs.backend.repository.CompanyRepository;
import com.pmrs.backend.repository.DriveRepository;
import com.pmrs.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final DriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;

    public DashboardServiceImpl(StudentRepository studentRepository,
                                CompanyRepository companyRepository,
                                DriveRepository driveRepository,
                                ApplicationRepository applicationRepository) {
        this.studentRepository = studentRepository;
        this.companyRepository = companyRepository;
        this.driveRepository = driveRepository;
        this.applicationRepository = applicationRepository;
    }

    private static boolean isFiltered(String val) {
        return val != null && !val.isEmpty() && !"All".equalsIgnoreCase(val);
    }

    @Override
    public DashboardDTO getDashboardData(Integer year, String branch, String program) {
        List<Student> allStudents = studentRepository.findAll();
        List<Drive> drives = driveRepository.findAll();
        List<Application> allApplications = applicationRepository.findAll();
        long totalCompanies = companyRepository.count();

        // ── Filter options (always from all students, for dropdown population) ─
        List<Integer> availableYears = allStudents.stream()
                .map(Student::getBatchYear)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> availableBranches = allStudents.stream()
                .filter(s -> s.getDepartment() != null && s.getDepartment().getBranch() != null)
                .map(s -> s.getDepartment().getBranch())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // ── Apply cohort filters to students ──────────────────────────────────
        List<Student> students = allStudents.stream()
                .filter(s -> year == null || year.equals(s.getBatchYear()))
                .filter(s -> !isFiltered(branch) || (s.getDepartment() != null
                        && branch.equals(s.getDepartment().getBranch())))
                .filter(s -> !isFiltered(program) || (s.getDepartment() != null
                        && program.equals(s.getDepartment().getProgram())))
                .collect(Collectors.toList());

        Set<Integer> filteredStudentIds = students.stream()
                .map(Student::getStudentId)
                .collect(Collectors.toSet());

        List<Application> applications = allApplications.stream()
                .filter(a -> a.getStudent() != null
                        && filteredStudentIds.contains(a.getStudent().getStudentId()))
                .collect(Collectors.toList());

        DashboardDTO dto = new DashboardDTO();
        dto.setAvailableYears(availableYears);
        dto.setAvailableBranches(availableBranches);

        // ── KPI ───────────────────────────────────────────────────────────────
        dto.setTotalStudents(students.size());
        dto.setTotalCompanies(totalCompanies);
        dto.setTotalDrives(drives.size());
        dto.setTotalApplications(applications.size());

        long placed = students.stream()
                .filter(s -> s.getPlacementTier() != null
                        && !s.getPlacementTier().equalsIgnoreCase("Unplaced"))
                .count();
        dto.setTotalPlacedStudents(placed);
        dto.setPlacementPercentage(students.isEmpty() ? 0
                : Math.round((placed * 100.0 / students.size()) * 10.0) / 10.0);

        // ── Tier distribution ─────────────────────────────────────────────────
        dto.setTierCCount(students.stream()
                .filter(s -> "C".equals(s.getPlacementTier())).count());
        dto.setTierBCount(students.stream()
                .filter(s -> "B".equals(s.getPlacementTier())).count());
        dto.setTierACount(students.stream()
                .filter(s -> "A".equals(s.getPlacementTier())).count());
        dto.setUnplacedCount(students.stream()
                .filter(s -> s.getPlacementTier() == null
                        || s.getPlacementTier().equalsIgnoreCase("Unplaced"))
                .count());

        // ── Package analytics (from all drives — not cohort-specific) ─────────
        List<Double> packages = drives.stream()
                .filter(d -> d.getPackageLpa() != null)
                .map(d -> d.getPackageLpa().doubleValue())
                .sorted()
                .collect(Collectors.toList());

        if (!packages.isEmpty()) {
            dto.setHighestPackage(packages.stream().mapToDouble(Double::doubleValue).max().orElse(0));
            double avg = packages.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            dto.setAveragePackage(Math.round(avg * 10.0) / 10.0);
            int mid = packages.size() / 2;
            double median = packages.size() % 2 == 0
                    ? (packages.get(mid - 1) + packages.get(mid)) / 2.0
                    : packages.get(mid);
            dto.setMedianPackage(Math.round(median * 10.0) / 10.0);
        }

        // ── Drive statistics (all drives, not cohort-filtered) ────────────────
        dto.setUpcomingDrives(drives.stream()
                .filter(d -> "Upcoming".equalsIgnoreCase(d.getStatus())).count());
        dto.setCompletedDrives(drives.stream()
                .filter(d -> "Completed".equalsIgnoreCase(d.getStatus())).count());
        dto.setCancelledDrives(drives.stream()
                .filter(d -> "Cancelled".equalsIgnoreCase(d.getStatus())).count());

        // ── Eligibility snapshot ──────────────────────────────────────────────
        dto.setNoBacklogStudents(students.stream()
                .filter(s -> s.getActiveBacklogs() == null || s.getActiveBacklogs() == 0)
                .count());
        dto.setDreamEligibleStudents(students.stream()
                .filter(s -> (s.getActiveBacklogs() == null || s.getActiveBacklogs() == 0)
                        && s.getCgpa() != null && s.getCgpa() >= 7.0)
                .count());
        dto.setSuperDreamEligibleStudents(students.stream()
                .filter(s -> (s.getActiveBacklogs() == null || s.getActiveBacklogs() == 0)
                        && s.getCgpa() != null && s.getCgpa() >= 8.5)
                .count());
        dto.setStudentsWithBacklogs(students.stream()
                .filter(s -> s.getActiveBacklogs() != null && s.getActiveBacklogs() > 0)
                .count());

        // ── Placement trends (always global — all batch years, all branches) ──
        Map<Integer, List<Student>> byYear = allStudents.stream()
                .filter(s -> s.getBatchYear() != null)
                .collect(Collectors.groupingBy(Student::getBatchYear));

        List<DashboardDTO.TrendPointDTO> trends = byYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    DashboardDTO.TrendPointDTO t = new DashboardDTO.TrendPointDTO();
                    t.setYear(e.getKey());
                    t.setTotalStudents(e.getValue().size());
                    long p = e.getValue().stream()
                            .filter(s -> s.getPlacementTier() != null
                                    && !s.getPlacementTier().equalsIgnoreCase("Unplaced"))
                            .count();
                    t.setPlacedStudents(p);
                    t.setPlacementPercentage(Math.round((p * 100.0 / e.getValue().size()) * 10.0) / 10.0);
                    return t;
                })
                .collect(Collectors.toList());
        dto.setPlacementTrends(trends);

        // ── Branch-wise placement ─────────────────────────────────────────────
        Map<String, List<Student>> byBranch = students.stream()
                .filter(s -> s.getDepartment() != null && s.getDepartment().getBranch() != null)
                .collect(Collectors.groupingBy(s -> s.getDepartment().getBranch()));

        List<DashboardDTO.BranchPlacementDTO> branchList = byBranch.entrySet().stream()
                .map(e -> {
                    DashboardDTO.BranchPlacementDTO b = new DashboardDTO.BranchPlacementDTO();
                    b.setBranch(e.getKey());
                    b.setTotalStudents(e.getValue().size());
                    long placedInBranch = e.getValue().stream()
                            .filter(s -> s.getPlacementTier() != null
                                    && !s.getPlacementTier().equalsIgnoreCase("Unplaced"))
                            .count();
                    b.setPlacedStudents(placedInBranch);
                    b.setPlacementPercentage(Math.round(
                            (placedInBranch * 100.0 / e.getValue().size()) * 10.0) / 10.0);
                    return b;
                })
                .sorted(Comparator.comparingDouble(
                        DashboardDTO.BranchPlacementDTO::getPlacementPercentage).reversed())
                .collect(Collectors.toList());
        dto.setBranchWisePlacement(branchList);

        // ── Top recruiters (filtered — selections from cohort students) ────────
        Map<String, long[]> recruiterData = new LinkedHashMap<>();
        Map<String, String> recruiterTier = new HashMap<>();

        applications.stream()
                .filter(a -> "Selected".equalsIgnoreCase(a.getStatus())
                        && a.getDrive() != null
                        && a.getDrive().getCompany() != null)
                .forEach(a -> {
                    String name = a.getDrive().getCompany().getCompanyName();
                    String tier = a.getDrive().getCompany().getTier();
                    recruiterData.merge(name, new long[]{1}, (existing, val) -> {
                        existing[0]++;
                        return existing;
                    });
                    recruiterTier.putIfAbsent(name, tier);
                });

        List<DashboardDTO.RecruiterDTO> recruiters = recruiterData.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> {
                    DashboardDTO.RecruiterDTO r = new DashboardDTO.RecruiterDTO();
                    r.setCompanyName(e.getKey());
                    r.setTier(recruiterTier.get(e.getKey()));
                    r.setStudentsHired(e.getValue()[0]);
                    return r;
                })
                .collect(Collectors.toList());
        dto.setTopRecruiters(recruiters);

        // ── Recent activities (filtered — latest 10 from cohort students) ──────
        List<DashboardDTO.ActivityDTO> activities = applications.stream()
                .filter(a -> a.getStudent() != null
                        && a.getDrive() != null
                        && a.getDrive().getCompany() != null)
                .sorted((a, b) -> {
                    if (a.getApplicationDate() == null) return 1;
                    if (b.getApplicationDate() == null) return -1;
                    return b.getApplicationDate().compareTo(a.getApplicationDate());
                })
                .limit(10)
                .map(a -> {
                    DashboardDTO.ActivityDTO act = new DashboardDTO.ActivityDTO();
                    act.setStudentName(a.getStudent().getName());
                    act.setCompanyName(a.getDrive().getCompany().getCompanyName());
                    act.setStatus(a.getStatus());
                    act.setApplicationDate(
                            a.getApplicationDate() != null ? a.getApplicationDate().toString() : null);
                    return act;
                })
                .collect(Collectors.toList());
        dto.setRecentActivities(activities);

        // ── Most applied company (all statuses, filtered cohort) ──────────────
        Map<String, Long> appliedByCompany = new HashMap<>();
        applications.stream()
                .filter(a -> a.getDrive() != null && a.getDrive().getCompany() != null)
                .forEach(a -> appliedByCompany.merge(
                        a.getDrive().getCompany().getCompanyName(), 1L, Long::sum));

        appliedByCompany.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> {
                    dto.setMostAppliedCompanyName(e.getKey());
                    dto.setMostAppliedCompanyCount(e.getValue());
                });

        // ── Last updated timestamp ─────────────────────────────────────────────
        dto.setLastUpdated(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));

        return dto;
    }
}
```

---

## `backend/src/main/java/com/pmrs/backend/config/DataInitializer.java`

_MODIFIED — seed student's placementTier set to "B"_

```java
package com.pmrs.backend.config;

import com.pmrs.backend.entity.Role;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.entity.User;
import com.pmrs.backend.repository.DepartmentRepository;
import com.pmrs.backend.repository.StudentRepository;
import com.pmrs.backend.repository.UserRepository;
import com.pmrs.backend.service.RollNumberService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds demo admin/officer/student accounts for local development. Runs by
 * default (no active profile), but is skipped entirely with
 * {@code spring.profiles.active=prod} — a real deployment should have exactly
 * one admin account, bootstrapped directly in MySQL, and zero seeded demo
 * accounts with known passwords.
 */
@Profile("!prod")
@Component
public class DataInitializer implements ApplicationRunner {

    private static final String SEED_STUDENT_EMAIL   = "student@pmrs.edu";
    private static final String SEED_STUDENT_PHONE    = "9876543210";

    private final UserRepository       userRepository;
    private final StudentRepository    studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder      passwordEncoder;
    private final RollNumberService    rollNumberService;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder,
                           RollNumberService rollNumberService) {
        this.userRepository       = userRepository;
        this.studentRepository    = studentRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder      = passwordEncoder;
        this.rollNumberService    = rollNumberService;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUser("admin",   "admin123",   "admin@pmrs.com",   Role.ADMIN,             null);
        seedUser("officer", "officer123", "officer@pmrs.com", Role.PLACEMENT_OFFICER, null);
        seedStudentUser();
    }

    // Demo/fixed accounts have a known password up front, so they're never
    // subject to the forced first-login password change.
    private void seedUser(String username, String rawPassword, String email,
                          Role role, Integer studentId) {
        if (!userRepository.existsByUsername(username)) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setEmail(email);
            u.setRole(role);
            u.setStudentId(studentId);
            u.setMustChangePassword(false);
            userRepository.save(u);
        }
    }

    private void seedStudentUser() {
        userRepository.findByUsername("student").ifPresentOrElse(
            existing -> {
                if (existing.getStudentId() == null) {
                    Student s = findOrCreateSeedStudent();
                    existing.setStudentId(s.getStudentId());
                    userRepository.save(existing);
                }
            },
            () -> {
                Student s = findOrCreateSeedStudent();
                User u = new User();
                u.setUsername("student");
                u.setPassword(passwordEncoder.encode("student123"));
                u.setEmail(SEED_STUDENT_EMAIL);
                u.setRole(Role.STUDENT);
                u.setStudentId(s.getStudentId());
                u.setMustChangePassword(false);
                userRepository.save(u);
            }
        );
    }

    /**
     * Creates the demo seed student if one does not already exist.
     *
     * Roll number generation now goes through {@link RollNumberService} —
     * the same path as the REST API — so the seed student receives a proper
     * roll number and the counter table stays consistent.
     *
     * The method is {@code @Transactional} so the counter increment and the
     * student INSERT commit together, matching the behaviour of
     * {@link com.pmrs.backend.service.StudentServiceImpl#saveStudent}.
     */
    @Transactional
    private Student findOrCreateSeedStudent() {
        return studentRepository.findByPhone(SEED_STUDENT_PHONE).orElseGet(() -> {
            Student s = new Student();
            s.setName("Rahul Sharma");
            s.setEmail(SEED_STUDENT_EMAIL);
            s.setPhone(SEED_STUDENT_PHONE);
            s.setBatchYear(2025);
            s.setCgpa(8.2);
            s.setActiveBacklogs(0);
            s.setPlacementTier("B");

            departmentRepository.findAll().stream()
                .filter(d -> "CSE".equalsIgnoreCase(d.getBranch())
                          && "B.Tech".equalsIgnoreCase(d.getProgram()))
                .findFirst()
                .ifPresent(dept -> {
                    s.setDepartment(dept);
                    // Generate roll number through the service — not directly.
                    String rollNo = rollNumberService.generateRollNumber(
                        dept.getDeptId(), s.getBatchYear()
                    );
                    s.setRollNo(rollNo);
                });

            return studentRepository.save(s);
        });
    }
}```

---

## `frontend/src/pages/Dashboard.jsx`

_MODIFIED — tier colors/badges/pie-chart keyed on A/B/C, labels show 'Tier X', eligibility snapshot wording_

```jsx
import { useEffect, useRef, useState, useCallback } from "react";
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Cell as BarCell,
  LineChart, Line, ReferenceLine,
} from "recharts";
import Layout from "../components/Layout";
import { getDashboardData } from "../services/dashboardService";
import "./Dashboard.css";

// ─── Constants ────────────────────────────────────────────────────────────────

const TIER_COLORS = {
  C:           "#7c3aed",
  B:           "#16a34a",
  A:           "#2563eb",
  "Unplaced":  "#374151",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

const STATUS_COLOR = {
  "Selected":              "#16a34a",
  "Shortlisted":           "#7c3aed",
  "Interview Scheduled":   "#d97706",
  "Applied":               "#2563eb",
  "Rejected":              "#dc2626",
};

const STATUS_EMOJI = {
  "Selected":            "🟢",
  "Shortlisted":         "🟣",
  "Interview Scheduled": "🟡",
  "Applied":             "🔵",
  "Rejected":            "🔴",
};

const PROGRAMS = ["All", "B.Tech", "M.Tech", "PhD"];

const EMPTY_FILTERS = { year: "", branch: "All", program: "All" };

// ─── Filter persistence (URL + sessionStorage) ────────────────────────────────

function readFiltersFromUrl() {
  const p = new URLSearchParams(window.location.search);
  const year    = p.get("year")    ?? "";
  const branch  = p.get("branch")  ?? "All";
  const program = p.get("program") ?? "All";
  if (year || branch !== "All" || program !== "All") return { year, branch, program };
  return null;
}

function loadFilters() {
  const fromUrl = readFiltersFromUrl();
  if (fromUrl) return fromUrl;
  try {
    const s = sessionStorage.getItem("dashFilters");
    return s ? JSON.parse(s) : EMPTY_FILTERS;
  } catch { return EMPTY_FILTERS; }
}

function persistFilters(f) {
  try { sessionStorage.setItem("dashFilters", JSON.stringify(f)); } catch {}
  const url = new URL(window.location.href);
  if (f.year)              url.searchParams.set("year",    f.year);
  else                     url.searchParams.delete("year");
  if (f.branch !== "All")  url.searchParams.set("branch",  f.branch);
  else                     url.searchParams.delete("branch");
  if (f.program !== "All") url.searchParams.set("program", f.program);
  else                     url.searchParams.delete("program");
  window.history.replaceState({}, "", url);
}

function apiParams(f) {
  const p = {};
  if (f.year && f.year !== "")   p.year    = f.year;
  if (f.branch  && f.branch  !== "All") p.branch  = f.branch;
  if (f.program && f.program !== "All") p.program = f.program;
  return p;
}

function isActive(f) {
  return (f.year && f.year !== "") ||
         (f.branch  && f.branch  !== "All") ||
         (f.program && f.program !== "All");
}

function filterLabel(f) {
  const parts = [];
  if (f.year)              parts.push(f.year);
  if (f.branch !== "All")  parts.push(f.branch);
  if (f.program !== "All") parts.push(f.program);
  return parts.join(" · ") || null;
}

// ─── Time ago ─────────────────────────────────────────────────────────────────

function timeAgo(dateStr) {
  if (!dateStr) return "—";
  const ms   = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(ms / 60000);
  const hrs  = Math.floor(ms / 3600000);
  const days = Math.floor(ms / 86400000);
  if (mins  < 1)  return "Just now";
  if (mins  < 60) return `${mins}m ago`;
  if (hrs   < 24) return `${hrs}h ago`;
  if (days  === 1) return "Yesterday";
  if (days  < 7)  return `${days} days ago`;
  return new Date(dateStr).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short",
  });
}

function formatDate(dateStr) {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function fmt(val, decimals = 0) {
  if (val == null || val === 0) return "0";
  return typeof val === "number"
    ? decimals > 0 ? val.toFixed(decimals) : val.toLocaleString()
    : String(val);
}

// ─── Reusable small components ────────────────────────────────────────────────

function SectionHeader({ title, sub, badge }) {
  return (
    <div className="dash-section-header">
      <span className="dash-section-title">{title}</span>
      {sub   && <span className="dash-section-sub">{sub}</span>}
      {badge && <span className="dash-section-badge">{badge}</span>}
    </div>
  );
}

function TierBadge({ tier }) {
  const MAP = {
    C:           "tier-super-dream",
    B:           "tier-dream",
    A:           "tier-normal",
    "Unplaced":  "tier-unplaced",
  };
  return (
    <span className={`tier-badge ${MAP[tier] ?? "tier-unplaced"}`}>
      {tier ? (TIER_LABEL[tier] ?? tier) : "Unplaced"}
    </span>
  );
}

function EmptyState({ message }) {
  return (
    <div className="empty-chart-state">
      <span className="empty-chart-icon">📊</span>
      <p>{message ?? "No data available yet"}</p>
    </div>
  );
}

function Skeleton({ h = 80, r = 10 }) {
  return <div className="dash-skeleton" style={{ height: h, borderRadius: r }} />;
}

// ─── KPI Cards ────────────────────────────────────────────────────────────────

function KpiCard({ icon, label, value, accent, sub }) {
  return (
    <div className="kpi-card" style={{ "--kpi-accent": accent }}>
      <div className="kpi-icon-wrap">{icon}</div>
      <div className="kpi-body">
        <div className="kpi-value">{value}</div>
        <div className="kpi-label">{label}</div>
        {sub && <div className="kpi-sub">{sub}</div>}
      </div>
    </div>
  );
}

function PlacementRateCard({ placed, total, percentage }) {
  return (
    <div className="kpi-card" style={{ "--kpi-accent": "#16a34a" }}>
      <div className="kpi-icon-wrap">📈</div>
      <div className="kpi-body">
        <div className="kpi-value" style={{ color: "#16a34a" }}>
          {fmt(percentage, 1)}%
        </div>
        <div className="kpi-label">Placement Rate</div>
        <div className="kpi-rate-breakdown">
          <span className="kpi-rate-placed">{placed} Placed</span>
          <span className="kpi-rate-sep"> / </span>
          <span className="kpi-rate-total">{total} Students</span>
        </div>
      </div>
    </div>
  );
}

function StatCard({ label, value, accent, icon, tooltip }) {
  return (
    <div className="stat-card" style={{ "--stat-accent": accent }}>
      <div className="stat-icon">{icon}</div>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
      {tooltip && (
        <div className="stat-tooltip-wrap">
          <span className="stat-tooltip-trigger">ⓘ</span>
          <div className="stat-tooltip-box">{tooltip}</div>
        </div>
      )}
    </div>
  );
}

// ─── Chart tooltips ───────────────────────────────────────────────────────────

function PieTooltip({ active, payload }) {
  if (!active || !payload?.length) return null;
  const { name, value } = payload[0];
  return (
    <div className="chart-tooltip">
      <strong>{name}</strong>: {value} student{value !== 1 ? "s" : ""}
    </div>
  );
}

function BarTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  const d = payload[0].payload;
  return (
    <div className="chart-tooltip">
      <strong>{label}</strong><br />
      {d.placedStudents} / {d.totalStudents} placed ({payload[0].value}%)
    </div>
  );
}

function TrendTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  const d = payload[0].payload;
  return (
    <div className="chart-tooltip">
      <strong>Batch {label}</strong><br />
      Placed: {d.placedStudents}<br />
      Total Students: {d.totalStudents}<br />
      <span style={{ color: "#93c5fd" }}>
        Placement Rate: {payload[0].value}%
      </span>
    </div>
  );
}

// ─── Filter Bar ───────────────────────────────────────────────────────────────

function FilterBar({ filters, onChange, onClear, availableYears, availableBranches }) {
  const active = isActive(filters);
  const sel    = (key) => (e) => onChange(key, e.target.value);

  return (
    <div className={`dash-filter-bar${active ? " dash-filter-bar--active" : ""}`}>
      <div className="filter-bar-label">
        <span>⚙</span> Filters
        {active && <span className="filter-active-dot" />}
      </div>

      <div className="filter-controls">
        <div className="filter-group">
          <label className="filter-label">Academic Year</label>
          <select className="filter-select" value={filters.year} onChange={sel("year")}>
            <option value="">All Years</option>
            {availableYears.map((y) => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label className="filter-label">Branch</label>
          <select className="filter-select" value={filters.branch} onChange={sel("branch")}>
            <option value="All">All Branches</option>
            {availableBranches.map((b) => (
              <option key={b} value={b}>{b}</option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label className="filter-label">Program</label>
          <select className="filter-select" value={filters.program} onChange={sel("program")}>
            {PROGRAMS.map((p) => (
              <option key={p} value={p}>{p === "All" ? "All Programs" : p}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="filter-actions">
        {active && (
          <button className="filter-btn-clear" onClick={onClear}>
            Clear Filters ✕
          </button>
        )}
        {active && (
          <span className="filter-active-label">
            Filtered: {filterLabel(filters)}
          </span>
        )}
      </div>
    </div>
  );
}

// ─── Loading skeleton ─────────────────────────────────────────────────────────

function LoadingState() {
  return (
    <Layout>
      <div className="dash-container">
        <Skeleton h={54} />
        <div className="dash-row kpi-grid">
          {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} h={88} />)}
        </div>
        <div className="dash-row two-col"><Skeleton h={290} /><Skeleton h={290} /></div>
        <Skeleton h={200} />
        <Skeleton h={220} />
        <div className="dash-row two-col"><Skeleton h={280} /><Skeleton h={280} /></div>
        <Skeleton h={150} />
        <Skeleton h={70}  />
      </div>
    </Layout>
  );
}

// ─── Main Dashboard ───────────────────────────────────────────────────────────

function Dashboard() {
  const [data,     setData]     = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);
  const [filters,  setFilters]  = useState(loadFilters);
  const [showAllRecruiters, setShowAllRecruiters] = useState(false);

  const debounce = useRef(null);
  const isFirst  = useRef(true);

  const fetchData = useCallback((f) => {
    setLoading(true);
    setError(null);
    getDashboardData(apiParams(f))
      .then((res) => { setData(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load dashboard data."); setLoading(false); });
  }, []);

  // Initial fetch
  useEffect(() => { fetchData(filters); }, []);

  // Auto-refresh on filter change (debounced, skip first render)
  useEffect(() => {
    if (isFirst.current) { isFirst.current = false; return; }
    clearTimeout(debounce.current);
    debounce.current = setTimeout(() => {
      persistFilters(filters);
      setShowAllRecruiters(false);
      fetchData(filters);
    }, 380);
    return () => clearTimeout(debounce.current);
  }, [filters.year, filters.branch, filters.program]);

  const handleFilterChange = (key, value) =>
    setFilters((f) => ({ ...f, [key]: value }));

  const handleClearFilters = () => setFilters(EMPTY_FILTERS);

  if (loading) return <LoadingState />;

  if (error) {
    return (
      <Layout>
        <div className="alert alert-danger m-3">{error}</div>
      </Layout>
    );
  }

  // ── Derived ──────────────────────────────────────────────────────────────

  const tierPieData = [
    { name: "Tier C",   key: "C", value: data.tierCCount },
    { name: "Tier B",   key: "B", value: data.tierBCount },
    { name: "Tier A",   key: "A", value: data.tierACount },
    { name: "Unplaced", key: "Unplaced", value: data.unplacedCount },
  ].filter((d) => d.value > 0);

  const branchBarData = (data.branchWisePlacement ?? []).map((b) => ({
    branch:              b.branch,
    placementPercentage: b.placementPercentage,
    placedStudents:      b.placedStudents,
    totalStudents:       b.totalStudents,
  }));

  const trendData = (data.placementTrends ?? []).map((t) => ({
    year:                t.year,
    placementPercentage: t.placementPercentage,
    placedStudents:      t.placedStudents,
    totalStudents:       t.totalStudents,
  }));

  const displayedRecruiters = showAllRecruiters
    ? (data.topRecruiters ?? [])
    : (data.topRecruiters ?? []).slice(0, 5);

  const activeBadge = isActive(filters) ? filterLabel(filters) : null;
  const topBranch   = branchBarData[0];
  const topRecruiter = data.topRecruiters?.[0];

  return (
    <Layout>
      <div className="dash-container">

        {/* ── Filter Bar (auto-refresh) ────────────────────────────────── */}
        <FilterBar
          filters={filters}
          onChange={handleFilterChange}
          onClear={handleClearFilters}
          availableYears={data.availableYears ?? []}
          availableBranches={data.availableBranches ?? []}
        />

        {/* ── Row 1 · KPI Grid ─────────────────────────────────────────── */}
        <div className="dash-row kpi-grid">
          <KpiCard icon="👥" label="Total Students"
            value={fmt(data.totalStudents)} accent="#2563eb" />
          <KpiCard icon="🏢" label="Companies"
            value={fmt(data.totalCompanies)} accent="#7c3aed" />
          <KpiCard icon="📋" label="Drives"
            value={fmt(data.totalDrives)} accent="#0891b2" />
          <KpiCard icon="📄" label="Applications"
            value={fmt(data.totalApplications)} accent="#ea580c" />
          <KpiCard icon="✅" label="Placed Students"
            value={fmt(data.totalPlacedStudents)} accent="#16a34a" />
          <PlacementRateCard
            placed={data.totalPlacedStudents}
            total={data.totalStudents}
            percentage={data.placementPercentage}
          />
        </div>

        {/* ── Row 2 · Tier Pie + Drive & Package Stats ─────────────────── */}
        <div className="dash-row two-col">

          <div className="dash-card">
            <SectionHeader
              title="Placement Tier Distribution"
              sub={`${data.totalStudents} students`}
              badge={activeBadge}
            />
            {tierPieData.length === 0 ? (
              <EmptyState message="No tier data assigned yet" />
            ) : (
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie data={tierPieData} cx="50%" cy="48%"
                    innerRadius={58} outerRadius={95} paddingAngle={3} dataKey="value">
                    {tierPieData.map((e) => (
                      <Cell key={e.name} fill={TIER_COLORS[e.key] ?? "#94a3b8"} />
                    ))}
                  </Pie>
                  <Tooltip content={<PieTooltip />} />
                  <Legend iconType="circle" iconSize={9}
                    formatter={(value, entry) => (
                      <span style={{ fontSize: "0.76rem", color: "#475569" }}>
                        {value} <strong style={{ color: "#1e293b" }}>({entry.payload.value})</strong>
                      </span>
                    )}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="dash-card">
            <SectionHeader title="Drive Statistics" sub={`${data.totalDrives} total`} />
            <div className="stat-grid-3">
              <StatCard label="Upcoming"  value={data.upcomingDrives}  accent="#2563eb" icon="🗓️" />
              <StatCard label="Completed" value={data.completedDrives} accent="#16a34a" icon="✅" />
              <StatCard label="Cancelled" value={data.cancelledDrives} accent="#dc2626" icon="❌" />
            </div>
            <div className="dash-divider" />
            <SectionHeader title="Package Analytics" sub="All drives" />
            <div className="stat-grid-3">
              <StatCard label="Highest"
                value={data.highestPackage > 0 ? `${data.highestPackage} LPA` : "—"}
                accent="#7c3aed" icon="🏆" />
              <StatCard label="Average"
                value={data.averagePackage > 0 ? `${fmt(data.averagePackage, 1)} LPA` : "—"}
                accent="#0891b2" icon="📊" />
              <StatCard label="Median"
                value={data.medianPackage > 0 ? `${fmt(data.medianPackage, 1)} LPA` : "—"}
                accent="#ea580c" icon="📉" />
            </div>
          </div>
        </div>

        {/* ── Placement Trend ───────────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Placement Trend by Batch Year"
            sub="Global · Placed ÷ Total Students per year"
          />
          {trendData.length < 2 ? (
            <EmptyState message={
              trendData.length === 1
                ? "Only one batch year found — add more data for a trend"
                : "No batch year data found"
            } />
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <LineChart data={trendData} margin={{ top: 8, right: 36, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <XAxis dataKey="year" tick={{ fontSize: 11, fill: "#94a3b8" }}
                  axisLine={false} tickLine={false} />
                <YAxis domain={[0, 100]} tickFormatter={(v) => `${v}%`}
                  tick={{ fontSize: 11, fill: "#94a3b8" }}
                  axisLine={false} tickLine={false} width={38} />
                <Tooltip content={<TrendTooltip />} />
                <ReferenceLine y={50} stroke="#e2e8f0" strokeDasharray="4 4" />
                <Line type="monotone" dataKey="placementPercentage"
                  stroke="#2563eb" strokeWidth={2.5}
                  dot={{ r: 4, fill: "#2563eb", stroke: "#fff", strokeWidth: 2 }}
                  activeDot={{ r: 6, fill: "#2563eb" }} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* ── Branch-wise Bar Chart ─────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Branch-wise Placement Performance"
            sub="Sorted highest → lowest"
            badge={activeBadge}
          />
          {branchBarData.length === 0 ? (
            <EmptyState message="No branch data for the selected filters" />
          ) : (
            <ResponsiveContainer width="100%"
              height={Math.max(160, branchBarData.length * 46)}>
              <BarChart data={branchBarData} layout="vertical"
                margin={{ top: 4, right: 64, left: 16, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f1f5f9" />
                <XAxis type="number" domain={[0, 100]}
                  tickFormatter={(v) => `${v}%`}
                  tick={{ fontSize: 11, fill: "#94a3b8" }}
                  axisLine={false} tickLine={false} />
                <YAxis type="category" dataKey="branch" width={94}
                  tick={{ fontSize: 12, fill: "#475569", fontWeight: 600 }}
                  axisLine={false} tickLine={false} />
                <Tooltip content={<BarTooltip />} />
                <Bar dataKey="placementPercentage" radius={[0, 4, 4, 0]}
                  label={{
                    position: "right",
                    formatter: (v) => `${v}%`,
                    fontSize: 11, fill: "#475569", fontWeight: 600,
                  }}>
                  {branchBarData.map((_, i) => (
                    <BarCell
                      key={i}
                      fill={i === 0 ? "#1d4ed8" : i === 1 ? "#2563eb" : "#3b82f6"}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* ── Top Recruiters + Recent Activities ───────────────────────── */}
        <div className="dash-row two-col">

          {/* Top Recruiters */}
          <div className="dash-card">
            <SectionHeader
              title="Top Recruiting Companies"
              sub={(data.topRecruiters?.length ?? 0) > 0
                ? `${data.topRecruiters.length} recruiter${data.topRecruiters.length !== 1 ? "s" : ""} · by hires`
                : undefined}
              badge={activeBadge}
            />

            {!(data.topRecruiters?.length) ? (
              <EmptyState message="No placement records available yet" />
            ) : (
              <>
                <div className="recruiter-table">
                  <div className="recruiter-header">
                    <span className="rh-rank">#</span>
                    <span className="rh-name">Company</span>
                    <span className="rh-tier">Tier</span>
                    <span className="rh-count">Hires</span>
                  </div>
                  {displayedRecruiters.map((r, i) => (
                    <div key={r.companyName}
                      className={`recruiter-row${i === 0 ? " recruiter-row--top" : ""}`}>
                      <span className="recruiter-rank">
                        {i === 0 ? "🥇" : i === 1 ? "🥈" : i === 2 ? "🥉" : i + 1}
                      </span>
                      <span className="recruiter-name">{r.companyName}</span>
                      <span className="recruiter-tier"><TierBadge tier={r.tier} /></span>
                      <span className="recruiter-count">{r.studentsHired}</span>
                    </div>
                  ))}
                </div>

                <div className="recruiter-footer">
                  <span className="recruiter-total-label">
                    Total Recruiters: <strong>{data.topRecruiters.length}</strong>
                  </span>
                  {(data.topRecruiters?.length ?? 0) > 5 && (
                    <button className="view-all-btn"
                      onClick={() => setShowAllRecruiters((v) => !v)}>
                      {showAllRecruiters ? "Show Less ▲" : `View All ${data.topRecruiters.length} ▼`}
                    </button>
                  )}
                </div>
              </>
            )}
          </div>

          {/* Recent Activities */}
          <div className="dash-card">
            <SectionHeader
              title="Recent Placement Activities"
              sub="Latest 10 events"
              badge={activeBadge}
            />
            {!(data.recentActivities?.length) ? (
              <EmptyState message="No application activity yet" />
            ) : (
              <div className="activity-feed">
                {data.recentActivities.map((a, i) => (
                  <div key={i} className="activity-item">
                    <span className="activity-status-icon">
                      {STATUS_EMOJI[a.status] ?? "⚪"}
                    </span>
                    <div className="activity-body">
                      <span className="activity-student">{a.studentName}</span>
                      <span className="activity-connector">
                        {a.status === "Selected"            ? " selected at "    :
                         a.status === "Shortlisted"         ? " shortlisted at " :
                         a.status === "Interview Scheduled" ? " interview at "   :
                         a.status === "Rejected"            ? " rejected at "    :
                                                              " applied at "}
                      </span>
                      <span className="activity-company">{a.companyName}</span>
                    </div>
                    <div className="activity-meta">
                      <span className="activity-badge"
                        style={{ background: STATUS_COLOR[a.status] ?? "#94a3b8" }}>
                        {a.status}
                      </span>
                      <span className="activity-date"
                        title={formatDate(a.applicationDate)}>
                        {timeAgo(a.applicationDate)}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* ── Placement Insights ────────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Quick Placement Insights"
            sub="Key metrics at a glance"
            badge={activeBadge}
          />
          <div className="insights-grid">
            <InsightItem
              icon="🏆"
              label="Highest Placement Branch"
              value={topBranch
                ? `${topBranch.branch} (${topBranch.placementPercentage}%)`
                : "—"}
              accent="#2563eb"
            />
            <InsightItem
              icon="🥇"
              label="Top Recruiter"
              value={topRecruiter
                ? `${topRecruiter.companyName} (${topRecruiter.studentsHired} hires)`
                : "—"}
              accent="#7c3aed"
            />
            <InsightItem
              icon="📌"
              label="Most Applied Company"
              value={data.mostAppliedCompanyName
                ? `${data.mostAppliedCompanyName} (${data.mostAppliedCompanyCount} applications)`
                : "—"}
              accent="#ea580c"
            />
            <InsightItem
              icon="📈"
              label="Placement Rate"
              value={`${fmt(data.placementPercentage, 1)}%`}
              accent="#16a34a"
            />
            <InsightItem
              icon="⏳"
              label="Students Remaining"
              value={fmt(data.totalStudents - data.totalPlacedStudents)}
              accent="#d97706"
            />
          </div>
        </div>

        {/* ── Eligibility Snapshot ──────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Student Eligibility Snapshot"
            sub="Tier C ≥ 8.5 CGPA · Tier B ≥ 7.0 CGPA · hover cards for details"
            badge={activeBadge}
          />
          <div className="stat-grid-4">
            <StatCard
              label="No Active Backlogs"
              value={data.noBacklogStudents}
              accent="#16a34a"
              icon="✅"
              tooltip="Students whose current active backlog count is zero"
            />
            <StatCard
              label="Tier B Eligible (CGPA ≥ 7.0)"
              value={data.dreamEligibleStudents}
              accent="#16a34a"
              icon="🌟"
              tooltip="Students with CGPA ≥ 7.0 and no active backlogs"
            />
            <StatCard
              label="Tier C Eligible (CGPA ≥ 8.5)"
              value={data.superDreamEligibleStudents}
              accent="#7c3aed"
              icon="🏆"
              tooltip="Students with CGPA ≥ 8.5 and no active backlogs"
            />
            <StatCard
              label="Students with Backlogs"
              value={data.studentsWithBacklogs}
              accent="#dc2626"
              icon="⚠️"
              tooltip="Students having one or more active backlogs"
            />
          </div>
        </div>

        {/* ── Footer Summary ────────────────────────────────────────────── */}
        <div className="dash-footer-card">
          <span className="footer-label">Data Summary</span>
          <div className="footer-stats">
            <FooterStat label="Students"     value={data.totalStudents} />
            <FooterStat label="Companies"    value={data.totalCompanies} />
            <FooterStat label="Drives"       value={data.totalDrives} />
            <FooterStat label="Applications" value={data.totalApplications} />
            <FooterStat label="Placed"       value={data.totalPlacedStudents} />
          </div>
          <span className="footer-updated">
            Last updated: {data.lastUpdated ?? "—"}
          </span>
        </div>

      </div>
    </Layout>
  );
}

// ─── InsightItem ──────────────────────────────────────────────────────────────

function InsightItem({ icon, label, value, accent }) {
  return (
    <div className="insight-item" style={{ "--insight-accent": accent }}>
      <span className="insight-icon">{icon}</span>
      <div className="insight-body">
        <div className="insight-label">{label}</div>
        <div className="insight-value">{value}</div>
      </div>
    </div>
  );
}

// ─── FooterStat ───────────────────────────────────────────────────────────────

function FooterStat({ label, value }) {
  return (
    <div className="footer-stat">
      <span className="footer-stat-value">{value}</span>
      <span className="footer-stat-label">{label}</span>
    </div>
  );
}

export default Dashboard;
```

---

## `frontend/src/pages/Applications.jsx`

_MODIFIED — tier badge classes/labels on A/B/C_

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

// Tier A/B/C reuse the original normal/dream/super-dream color classes.
const TIER_BADGE_CLASS = {
  C:           "tier-super-dream",
  B:           "tier-dream",
  A:           "tier-normal",
  "Unplaced":  "tier-unplaced",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

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
      {tier ? (TIER_LABEL[tier] ?? tier) : "Unplaced"}
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

## `frontend/src/pages/Companies.jsx`

_MODIFIED — tier filter/form dropdowns + table on A/B/C with 'Tier X' labels_

```jsx
import { useState } from "react";
import Layout from "../components/Layout";
import { getAllCompanies, addCompany, updateCompany, deleteCompany } from "../services/companyService";
import { isAdmin } from "../services/authService";
import "./Companies.css";

const TIERS = ["All", "A", "B", "C"];
const SECTORS = ["All", "IT", "Finance", "Fintech", "Consulting", "Core", "Analytics"];

// Job categories by CTC: A < 6 LPA · B 6–11.99 LPA · C ≥ 12 LPA
const tierLabelFor = (t) => (t === "All" ? "All Tiers" : t ? `Tier ${t}` : t);

const emptyForm = {
  companyName: "",
  sector: "",
  tier: "",
  website: "",
};

function Companies() {
  const [allCompanies, setAllCompanies] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [search, setSearch] = useState("");
  const [filters, setFilters] = useState({ tier: "All", sector: "All" });
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);

  const admin = isAdmin();

  const handleSearch = async () => {
    try {
      const res = await getAllCompanies();
      setAllCompanies(res.data);
      setHasSearched(true);
    } catch (err) {
      console.error("Error loading companies:", err);
    }
  };

  const handleEdit = (company) => {
    setForm({
      companyName: company.companyName,
      sector: company.sector,
      tier: company.tier,
      website: company.website,
    });
    setEditingId(company.companyId);
    setShowForm(true);
  };

  const handleSaveCompany = async () => {
    try {
      if (editingId) {
        await updateCompany(editingId, form);
        alert("Company Updated Successfully");
      } else {
        await addCompany(form);
        alert("Company Added Successfully");
      }
      if (hasSearched) handleSearch();
      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this company?")) return;
    try {
      await deleteCompany(id);
      alert("Company Deleted Successfully");
      if (hasSearched) handleSearch();
    } catch (err) {
      console.error(err);
      alert("Failed to delete company");
    }
  };

  const field = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  });

  const displayedCompanies = allCompanies
    .filter((c) => filters.tier === "All" || c.tier === filters.tier)
    .filter((c) => filters.sector === "All" || c.sector === filters.sector)
    .filter((c) => c.companyName.toLowerCase().includes(search.toLowerCase()));

  const tierLabel   = tierLabelFor(filters.tier);
  const sectorLabel = filters.sector === "All" ? "All Sectors" : filters.sector;

  return (
    <Layout>
      <div className="d-flex justify-content-between mb-3">
        <h2>Companies</h2>
        <button
          className="btn btn-primary"
          onClick={() => {
            setShowForm(!showForm);
            if (showForm) { setEditingId(null); setForm(emptyForm); }
          }}
        >
          {showForm ? "Cancel" : "Add Company"}
        </button>
      </div>

      {/* Filter Panel */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-3">
              <label className="form-label fw-semibold">Tier</label>
              <select
                className="form-select"
                value={filters.tier}
                onChange={(e) => setFilters({ ...filters, tier: e.target.value })}
              >
                {TIERS.map((t) => (
                  <option key={t} value={t}>{tierLabelFor(t)}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label fw-semibold">Sector</label>
              <select
                className="form-select"
                value={filters.sector}
                onChange={(e) => setFilters({ ...filters, sector: e.target.value })}
              >
                {SECTORS.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <button className="btn btn-primary w-100" onClick={handleSearch}>
                Load Companies
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Add / Edit Form */}
      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <div className="row g-2">
              <div className="col-md-6">
                <label className="form-label">Company Name</label>
                <input className="form-control" placeholder="Company name" {...field("companyName")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Sector</label>
                <select className="form-select" {...field("sector")}>
                  <option value="">Select sector</option>
                  {SECTORS.filter((s) => s !== "All").map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Tier</label>
                <select className="form-select" {...field("tier")}>
                  <option value="">Select tier</option>
                  {TIERS.filter((t) => t !== "All").map((t) => (
                    <option key={t} value={t}>{tierLabelFor(t)}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Website</label>
                <input className="form-control" placeholder="https://..." {...field("website")} />
              </div>
            </div>
            <button className="btn btn-success mt-3" onClick={handleSaveCompany}>
              {editingId ? "Update Company" : "Save Company"}
            </button>
          </div>
        </div>
      )}

      {/* Results */}
      {hasSearched && (
        <>
          <div className="mb-3">
            <input
              type="text"
              className="form-control"
              placeholder="Search company..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <p className="text-muted mb-2">
            Showing <strong>{displayedCompanies.length}</strong> company(s) — {tierLabel} • {sectorLabel}
            {!admin && (
              <span className="badge bg-info text-white ms-2" style={{ fontSize: "0.7rem" }}>
                Delete restricted
              </span>
            )}
          </p>

          {displayedCompanies.length === 0 ? (
            <div className="alert alert-warning">
              No companies found for selected filters.
            </div>
          ) : (
            <table className="table table-striped table-bordered table-hover">
              <thead className="table-dark">
                <tr>
                  <th>ID</th>
                  <th>Company Name</th>
                  <th>Sector</th>
                  <th>Tier</th>
                  <th>Website</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {displayedCompanies.map((company) => (
                  <tr key={company.companyId}>
                    <td>{company.companyId}</td>
                    <td>{company.companyName}</td>
                    <td>{company.sector}</td>
                    <td>{tierLabelFor(company.tier)}</td>
                    <td>{company.website}</td>
                    <td>
                      <button
                        className="btn btn-sm action-edit me-2"
                        onClick={() => handleEdit(company)}
                      >
                        Edit
                      </button>
                      {admin && (
                        <button
                          className="btn btn-sm action-delete"
                          onClick={() => handleDelete(company.companyId)}
                        >
                          Delete
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </Layout>
  );
}

export default Companies;
```

---

## `frontend/src/pages/Drives.jsx`

_MODIFIED — tier filter dropdown + table on A/B/C with 'Tier X' labels_

```jsx
import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import { getAllDrives, addDrive, updateDrive, deleteDrive } from "../services/driveService";
import { getAllCompanies } from "../services/companyService";
import { isAdmin } from "../services/authService";
import "./Drives.css";

const TIERS    = ["All", "A", "B", "C"];
const STATUSES = ["Upcoming", "Active", "Completed", "Cancelled"];

// Job categories by CTC: A < 6 LPA · B 6–11.99 LPA · C ≥ 12 LPA
const tierLabelFor = (t) => (t === "All" ? "All Tiers" : t ? `Tier ${t}` : t);

const emptyForm = {
  companyId:   "",
  roleOffered: "",
  packageLpa:  "",
  minCgpa:     "",
  maxBacklogs: "",
  driveDate:   "",
  status:      "Upcoming",
};

const STATUS_BADGE = {
  Upcoming:  "primary",
  Active:    "success",
  Completed: "secondary",
  Cancelled: "danger",
};

function Drives() {
  const [companies,   setCompanies]   = useState([]);
  const [allDrives,   setAllDrives]   = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [filters,     setFilters]     = useState({ companyId: "All", tier: "All" });
  const [showForm,    setShowForm]    = useState(false);
  const [editingId,   setEditingId]   = useState(null);
  const [form,        setForm]        = useState(emptyForm);

  const admin = isAdmin();

  // A Placement Officer can delete only non-completed drives
  const canDeleteDrive = (drive) =>
    admin || drive.status?.toLowerCase() !== "completed";

  useEffect(() => {
    getAllCompanies()
      .then((res) => setCompanies(res.data))
      .catch((err) => console.error("Error loading companies:", err));
  }, []);

  const handleSearch = async () => {
    try {
      const res = await getAllDrives();
      setAllDrives(res.data);
      setHasSearched(true);
    } catch (err) {
      console.error("Error loading drives:", err);
    }
  };

  const buildPayload = () => ({
    company:     form.companyId ? { companyId: parseInt(form.companyId) } : null,
    roleOffered: form.roleOffered,
    packageLpa:  form.packageLpa  !== "" ? parseFloat(form.packageLpa)  : null,
    minCgpa:     form.minCgpa     !== "" ? parseFloat(form.minCgpa)     : null,
    maxBacklogs: form.maxBacklogs !== "" ? parseInt(form.maxBacklogs)   : null,
    driveDate:   form.driveDate || null,
    status:      form.status || "Upcoming",
  });

  const handleSaveDrive = async () => {
    try {
      if (editingId) {
        await updateDrive(editingId, buildPayload());
        alert("Drive Updated Successfully");
      } else {
        await addDrive(buildPayload());
        alert("Drive Added Successfully");
      }
      if (hasSearched) handleSearch();
      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);
    } catch (err) {
      console.error(err);
    }
  };

  const handleEdit = (drive) => {
    setForm({
      companyId:   drive.company?.companyId ?? "",
      roleOffered: drive.roleOffered ?? "",
      packageLpa:  drive.packageLpa  ?? "",
      minCgpa:     drive.minCgpa     ?? "",
      maxBacklogs: drive.maxBacklogs ?? "",
      driveDate:   drive.driveDate   ?? "",
      status:      drive.status      ?? "Upcoming",
    });
    setEditingId(drive.driveId);
    setShowForm(true);
  };

  const handleDelete = async (drive) => {
    if (!canDeleteDrive(drive)) {
      alert("Placement Officers cannot delete completed drives. Contact an Admin.");
      return;
    }
    if (!window.confirm("Delete this drive?")) return;
    try {
      await deleteDrive(drive.driveId);
      alert("Drive Deleted Successfully");
      if (hasSearched) handleSearch();
    } catch (err) {
      console.error(err);
      alert(err.response?.data ?? "Failed to delete drive");
    }
  };

  const field = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  });

  const displayedDrives = allDrives
    .filter((d) =>
      filters.companyId === "All" ||
      String(d.company?.companyId) === filters.companyId
    )
    .filter((d) =>
      filters.tier === "All" || d.company?.tier === filters.tier
    );

  const companyLabel =
    filters.companyId === "All"
      ? "All Companies"
      : companies.find((c) => String(c.companyId) === filters.companyId)
          ?.companyName ?? "All Companies";
  const tierLabel = tierLabelFor(filters.tier);

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    return new Date(dateStr).toLocaleDateString("en-IN", {
      day: "2-digit", month: "short",
    });
  };

  return (
    <Layout>
      <div className="d-flex justify-content-between mb-3">
        <h2>Drives</h2>
        <button
          className="btn btn-primary"
          onClick={() => {
            setShowForm(!showForm);
            if (showForm) { setEditingId(null); setForm(emptyForm); }
          }}
        >
          {showForm ? "Cancel" : "Add Drive"}
        </button>
      </div>

      {/* Filter Panel */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-4">
              <label className="form-label fw-semibold">Company</label>
              <select
                className="form-select"
                value={filters.companyId}
                onChange={(e) => setFilters({ ...filters, companyId: e.target.value })}
              >
                <option value="All">All</option>
                {companies.map((c) => (
                  <option key={c.companyId} value={String(c.companyId)}>
                    {c.companyName}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label fw-semibold">Tier</label>
              <select
                className="form-select"
                value={filters.tier}
                onChange={(e) => setFilters({ ...filters, tier: e.target.value })}
              >
                {TIERS.map((t) => (
                  <option key={t} value={t}>{tierLabelFor(t)}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <button className="btn btn-primary w-100" onClick={handleSearch}>
                Load Drives
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Add / Edit Form */}
      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <div className="row g-2">
              <div className="col-md-6">
                <label className="form-label">Company</label>
                <select className="form-select" {...field("companyId")}>
                  <option value="">Select company</option>
                  {companies.map((c) => (
                    <option key={c.companyId} value={c.companyId}>
                      {c.companyName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Role</label>
                <input
                  className="form-control"
                  placeholder="e.g. SWE Intern"
                  {...field("roleOffered")}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Package (LPA)</label>
                <input
                  className="form-control"
                  type="number"
                  step="0.5"
                  min="0"
                  placeholder="e.g. 12.5"
                  {...field("packageLpa")}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Min CGPA</label>
                <input
                  className="form-control"
                  type="number"
                  step="0.1"
                  min="0"
                  max="10"
                  placeholder="e.g. 7.5"
                  {...field("minCgpa")}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Max Backlogs</label>
                <input
                  className="form-control"
                  type="number"
                  min="0"
                  placeholder="e.g. 0"
                  {...field("maxBacklogs")}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Drive Date</label>
                <input
                  className="form-control"
                  type="date"
                  {...field("driveDate")}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Status</label>
                <select className="form-select" {...field("status")}>
                  {STATUSES.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            </div>
            <button className="btn btn-success mt-3" onClick={handleSaveDrive}>
              {editingId ? "Update Drive" : "Save Drive"}
            </button>
          </div>
        </div>
      )}

      {/* Results */}
      {hasSearched && (
        <>
          <p className="text-muted mb-2">
            Showing <strong>{displayedDrives.length}</strong> drive(s) — {companyLabel} • {tierLabel}
            {!admin && (
              <span className="badge bg-warning text-dark ms-2" style={{ fontSize: "0.7rem" }}>
                Completed drives locked
              </span>
            )}
          </p>

          {displayedDrives.length === 0 ? (
            <div className="alert alert-warning">
              No drives found for selected filters.
            </div>
          ) : (
            <table className="table table-striped table-bordered table-hover">
              <thead className="table-dark">
                <tr>
                  <th>ID</th>
                  <th>Company</th>
                  <th>Tier</th>
                  <th>Role</th>
                  <th>Package (LPA)</th>
                  <th>Min CGPA</th>
                  <th>Max Backlogs</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {displayedDrives.map((drive) => (
                  <tr key={drive.driveId}>
                    <td>{drive.driveId}</td>
                    <td>{drive.company?.companyName ?? "—"}</td>
                    <td>{drive.company?.tier ? tierLabelFor(drive.company.tier) : "—"}</td>
                    <td>{drive.roleOffered}</td>
                    <td>{drive.packageLpa ?? "—"}</td>
                    <td>{drive.minCgpa ?? "—"}</td>
                    <td>{drive.maxBacklogs ?? "—"}</td>
                    <td>{formatDate(drive.driveDate)}</td>
                    <td>
                      {drive.status ? (
                        <span className={`badge bg-${STATUS_BADGE[drive.status] ?? "secondary"}`}>
                          {drive.status}
                        </span>
                      ) : "—"}
                    </td>
                    <td>
                      <button
                        className="btn btn-sm action-edit me-2"
                        onClick={() => handleEdit(drive)}
                      >
                        Edit
                      </button>
                      {canDeleteDrive(drive) ? (
                        <button
                          className="btn btn-sm action-delete"
                          onClick={() => handleDelete(drive)}
                        >
                          Delete
                        </button>
                      ) : (
                        <span
                          className="btn btn-sm btn-outline-secondary disabled"
                          title="Completed drives can only be deleted by Admin"
                          style={{ cursor: "not-allowed", opacity: 0.55, fontSize: "0.75rem" }}
                        >
                          🔒 Locked
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </Layout>
  );
}

export default Drives;
```

---

## `frontend/src/pages/Students.jsx`

_MODIFIED — placement tier column shows 'Tier X'_

```jsx
import { useState } from "react";
import Layout from "../components/Layout";
import {
  deleteStudent,
  getFilteredStudents,
  previewStudent,
  importStudent,
  backfillStudentAccounts,
} from "../services/studentService";
import { isAdmin } from "../services/authService";
import "./Students.css";

const PROGRAMS = ["B.Tech", "M.Tech", "PhD"];

// Job categories by CTC: A < 6 LPA · B 6–11.99 LPA · C ≥ 12 LPA
const tierLabelFor = (t) => (t && t !== "Unplaced" ? `Tier ${t}` : (t ?? "—"));

const DEPT_BY_PROGRAM = {
  "B.Tech": ["CSE", "ECE"],
  "M.Tech": ["CSE", "ECE"],
  "PhD":    ["CSE", "ECE", "Science & Mathematics", "HSS"],
};
const BATCH_YEARS = [];
for (let y = 2015; y <= 2030; y++) BATCH_YEARS.push(y);

function Students() {
  const [students, setStudents] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [filters, setFilters] = useState({
    department: "CSE",
    program: "B.Tech",
    batchYear: 2025,
  });
  const [showImport, setShowImport]       = useState(false);
  const [importRollNo, setImportRollNo]   = useState("");
  const [importPreview, setImportPreview] = useState(null);
  const [importLoading, setImportLoading] = useState(false);
  const [importError, setImportError]     = useState(null);

  const [backfillLoading, setBackfillLoading] = useState(false);
  const [backfillResult, setBackfillResult]   = useState(null);
  const [backfillError, setBackfillError]     = useState(null);

  const admin = isAdmin();

  const handleSearch = async () => {
    try {
      const res = await getFilteredStudents(
        filters.department,
        filters.program,
        filters.batchYear
      );
      setStudents(res.data);
      setHasSearched(true);
    } catch (err) {
      console.error("Error filtering students:", err);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this student?")) return;
    try {
      await deleteStudent(id);
      alert("Student Deleted Successfully");
      if (hasSearched) handleSearch();
    } catch (err) {
      console.error(err);
      alert("Failed to delete student");
    }
  };

  const handlePreview = async () => {
    if (!importRollNo.trim()) return;
    setImportLoading(true);
    setImportError(null);
    setImportPreview(null);
    try {
      const res = await previewStudent(importRollNo.trim());
      setImportPreview(res.data);
    } catch (err) {
      setImportError(
        err.response?.data?.message ?? "Roll number not found in Academic ERP."
      );
    } finally {
      setImportLoading(false);
    }
  };

  const handleBackfill = async () => {
    setBackfillLoading(true);
    setBackfillError(null);
    setBackfillResult(null);
    try {
      const res = await backfillStudentAccounts();
      setBackfillResult(res.data);
    } catch (err) {
      setBackfillError(
        err.response?.data?.message ?? "Failed to create missing login accounts."
      );
    } finally {
      setBackfillLoading(false);
    }
  };

  const handleImport = async () => {
    if (!importPreview) return;
    setImportLoading(true);
    setImportError(null);
    try {
      await importStudent(importRollNo.trim());
      alert(`Student ${importPreview.firstName} ${importPreview.lastName} imported successfully.`);
      setShowImport(false);
      setImportRollNo("");
      setImportPreview(null);
      if (hasSearched) handleSearch();
    } catch (err) {
      setImportError(
        err.response?.data?.message ?? "Import failed. Student may already exist in PRMS."
      );
    } finally {
      setImportLoading(false);
    }
  };

  return (
    <Layout>
      {admin && (
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h2 className="mb-0">Students</h2>
          <div className="d-flex gap-2 align-items-center">
            <span
              className="text-muted d-flex align-items-center gap-1"
              style={{ fontSize: "0.82rem" }}
            >
              🎓 New students are registered in Academic ERP
            </span>
            <a
              href="http://localhost:5174"
              target="_blank"
              rel="noreferrer"
              className="btn btn-outline-secondary btn-sm"
            >
              Open Academic ERP ↗
            </a>
            <button
              className="btn btn-outline-secondary btn-sm"
              onClick={handleBackfill}
              disabled={backfillLoading}
              title="Create login accounts (username & temp password = roll number) for any student that doesn't have one yet"
            >
              {backfillLoading ? "Creating logins…" : "Create Missing Logins"}
            </button>
            <button
              className="btn btn-primary btn-sm"
              onClick={() => {
                setShowImport(!showImport);
                setImportRollNo("");
                setImportPreview(null);
                setImportError(null);
              }}
            >
              {showImport ? "Cancel Import" : "Import Student"}
            </button>
          </div>
        </div>
      )}

      {admin && backfillError && (
        <div className="alert alert-danger py-2">{backfillError}</div>
      )}

      {admin && backfillResult && (
        <div className="alert alert-success py-2">
          Created <strong>{backfillResult.created}</strong> new login account(s)
          {backfillResult.skipped > 0 && <> — {backfillResult.skipped} student(s) already had one</>}.
          {backfillResult.created > 0 && (
            <>
              {" "}Username and temporary password = student's roll number
              {backfillResult.createdRollNumbers?.length > 0 && (
                <> ({backfillResult.createdRollNumbers.join(", ")})</>
              )}.
              Students will be prompted to set a new password on first login.
            </>
          )}
        </div>
      )}

      {admin && showImport && (
        <div className="card mb-4 border-primary">
          <div className="card-header bg-primary text-white fw-semibold">
            Import Student from Academic ERP
          </div>
          <div className="card-body">

            {/* Roll number input */}
            <div className="d-flex gap-2 align-items-end mb-3">
              <div style={{ flex: 1 }}>
                <label className="form-label fw-semibold">Roll Number</label>
                <input
                  className="form-control"
                  placeholder="e.g. 2511104"
                  value={importRollNo}
                  onChange={(e) => {
                    setImportRollNo(e.target.value);
                    setImportPreview(null);
                    setImportError(null);
                  }}
                  onKeyDown={(e) => e.key === "Enter" && handlePreview()}
                  maxLength={7}
                />
              </div>
              <button
                className="btn btn-outline-primary"
                onClick={handlePreview}
                disabled={importLoading || !importRollNo.trim()}
              >
                {importLoading && !importPreview ? "Searching…" : "Look Up"}
              </button>
            </div>

            {/* Error */}
            {importError && (
              <div className="alert alert-danger py-2">{importError}</div>
            )}

            {/* Preview card */}
            {importPreview && (
              <div className="card bg-light mb-3">
                <div className="card-body">
                  <h6 className="card-title mb-3">
                    {importPreview.firstName}{" "}
                    {importPreview.middleName ? importPreview.middleName + " " : ""}
                    {importPreview.lastName}
                  </h6>
                  <div className="row g-2" style={{ fontSize: "0.88rem" }}>
                    <div className="col-md-4">
                      <span className="text-muted">Roll No</span>
                      <div className="fw-semibold" style={{ fontFamily: "monospace" }}>
                        {importPreview.rollNo}
                      </div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Email</span>
                      <div>{importPreview.email}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Phone</span>
                      <div>{importPreview.phone}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Department</span>
                      <div>{importPreview.department?.deptName ?? "—"}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Program</span>
                      <div>{importPreview.department?.program} · {importPreview.department?.branch}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Admission Year</span>
                      <div>{importPreview.admissionYear}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">CGPA</span>
                      <div>{importPreview.cgpa}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Backlogs</span>
                      <div>{importPreview.activeBacklogs}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Placement tier</span>
                      <div className="text-muted fst-italic">Unplaced (set after import)</div>
                    </div>
                  </div>
                  <button
                    className="btn btn-success mt-3"
                    onClick={handleImport}
                    disabled={importLoading}
                  >
                    {importLoading ? "Importing…" : "Confirm Import"}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Filter Panel — unchanged */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-3">
              <label className="form-label fw-semibold">Branch</label>
              <select
                className="form-select"
                value={filters.department}
                onChange={(e) => setFilters({ ...filters, department: e.target.value })}
              >
                {DEPT_BY_PROGRAM[filters.program].map((d) => (
                  <option key={d} value={d}>{d}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label fw-semibold">Program</label>
              <select
                className="form-select"
                value={filters.program}
                onChange={(e) => {
                  const prog = e.target.value;
                  setFilters({
                    ...filters,
                    program: prog,
                    department: DEPT_BY_PROGRAM[prog][0],
                  });
                }}
              >
                {PROGRAMS.map((p) => (
                  <option key={p} value={p}>{p}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label fw-semibold">Batch Year</label>
              <select
                className="form-select"
                value={filters.batchYear}
                onChange={(e) => setFilters({ ...filters, batchYear: parseInt(e.target.value) })}
              >
                {BATCH_YEARS.map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <button className="btn btn-primary w-100" onClick={handleSearch}>
                Load Students
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Results Table */}
      {hasSearched && (
        <>
          <p className="text-muted mb-2">
            Showing <strong>{students.length}</strong> student(s) —{" "}
            {filters.program} · {filters.department} · {filters.batchYear} Batch
          </p>
          <table className="table table-striped table-bordered table-hover">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Roll No</th>
                <th>Department</th>
                <th>Batch</th>
                <th>CGPA</th>
                <th>Backlogs</th>
                <th>Phone</th>
                <th>Email</th>
                <th>Tier</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {students.length === 0 ? (
                <tr>
                  <td colSpan="11" className="text-center text-muted py-3">
                    No students found for the selected filters.
                  </td>
                </tr>
              ) : (
                students.map((student) => (
                  <tr key={student.studentId}>
                    <td>{student.studentId}</td>
                    
                    <td>{student.name}</td>
                     <td>
                     <span style={{ fontFamily: "monospace", fontWeight: 400, letterSpacing: "0.04em" }}>
                      {student.rollNo ?? "—"}
                       </span>
                      </td>
                    <td>{student.department?.deptName ?? "—"}</td>
                    <td>{student.batchYear}</td>
                    <td>{student.cgpa}</td>
                    <td>{student.activeBacklogs}</td>
                    <td>{student.phone}</td>
                    <td>{student.email}</td>
                    <td>{tierLabelFor(student.placementTier)}</td>
                    <td>
                      {admin ? (
                        <button
                          className="btn btn-sm action-delete"
                          onClick={() => handleDelete(student.studentId)}
                        >
                          Delete
                        </button>
                      ) : (
                        <span className="text-muted" style={{ fontSize: "0.78rem" }}>
                          View Only
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </>
      )}
    </Layout>
  );
}

export default Students;
```

---

## `frontend/src/pages/student/EligibleDrives.jsx`

_MODIFIED — tier colors/badges/filter on A/B/C_

```jsx
import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import { getEligibleDrives, applyToDrive, getPenaltyStatus } from "../../services/studentPortalService";
import "./EligibleDrives.css";

const TIER_COLOR = {
  C: "#7c3aed",
  B: "#16a34a",
  A: "#2563eb",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

const DRIVE_STATUS_BADGE = {
  "Upcoming":  { bg: "#eff6ff", color: "#1d4ed8" },
  "Active":    { bg: "#f0fdf4", color: "#15803d" },
  "Completed": { bg: "#f1f5f9", color: "#475569" },
};

function TierBadge({ tier }) {
  const color = TIER_COLOR[tier] ?? "#374151";
  return (
    <span className="ed-tier" style={{ background: color }}>
      {tier ? (TIER_LABEL[tier] ?? tier) : "—"}
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
              {tiers.map((t) => <option key={t} value={t}>{t === "All" ? t : (TIER_LABEL[t] ?? t)}</option>)}
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

## `frontend/src/pages/student/MyApplications.jsx`

_MODIFIED — tier colors/badge label on A/B/C_

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
  C: "#7c3aed",
  B: "#16a34a",
  A: "#2563eb",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

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
                          {TIER_LABEL[app.companyTier] ?? app.companyTier}
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

## `frontend/src/pages/student/MyProfile.jsx`

_MODIFIED — placement tier badge label on A/B/C_

```jsx
import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import { getStudentProfile } from "../../services/studentPortalService";
import "./MyProfile.css";

const PLACEMENT_COLOR = {
  C:           "#7c3aed",
  B:           "#16a34a",
  A:           "#2563eb",
  "Unplaced":  "#475569",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

function Field({ label, value, highlight }) {
  return (
    <div className="mp-field">
      <span className="mp-field-label">{label}</span>
      <span className={`mp-field-value${highlight ? " mp-field-highlight" : ""}`}>
        {value ?? "—"}
      </span>
    </div>
  );
}

function MyProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    getStudentProfile()
      .then((res) => { setProfile(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load profile."); setLoading(false); });
  }, []);

  if (loading) {
    return (
      <Layout>
        <div className="mp-container">
          <div className="mp-skeleton" style={{ height: 90 }} />
          <div className="mp-skeleton" style={{ height: 320 }} />
        </div>
      </Layout>
    );
  }

  if (error) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  const placementColor = PLACEMENT_COLOR[profile?.placementTier] ?? "#475569";
  const initial        = profile?.name?.charAt(0)?.toUpperCase() ?? "S";

  return (
    <Layout>
      <div className="mp-container">

        {/* ── Profile Header ──────────────────────────────────────────── */}
        <div className="mp-header-card">
          <div className="mp-avatar" style={{ background: placementColor }}>{initial}</div>
          <div className="mp-header-body">
            <div className="mp-full-name">{profile?.name}</div>
            <div className="mp-sub-row">
              {profile?.rollNo && (
                <span className="mp-chip" style={{ fontFamily: "monospace", letterSpacing: "0.06em", fontWeight: 600 }}>
                  {profile.rollNo}
                </span>
              )}
              {profile?.branch   && <span className="mp-chip">{profile.branch}</span>}
              {profile?.program  && <span className="mp-chip">{profile.program}</span>}
              {profile?.batchYear && <span className="mp-chip">Batch {profile.batchYear}</span>}
            </div>
          </div>
          <div className="mp-header-right">
            <span className="mp-tier-badge" style={{ background: placementColor }}>
              {profile?.placementTier === "Unplaced" || !profile?.placementTier
                ? "Not Placed"
                : `Placed (${TIER_LABEL[profile.placementTier] ?? profile.placementTier})`}
            </span>
          </div>
        </div>

        {/* ── Details ─────────────────────────────────────────────────── */}
        <div className="mp-details-grid">

          <div className="mp-card">
            <div className="mp-card-title">📋 Personal Information</div>
            <div className="mp-fields">
              <Field label="Roll Number"   value={profile?.rollNo} />
              <Field label="Student ID"    value={`#${profile?.studentId}`} />
              <Field label="Full Name"     value={profile?.name} />
              <Field label="Email Address" value={profile?.email} />
              <Field label="Phone Number"  value={profile?.phone} />
            </div>
          </div>

          <div className="mp-card">
            <div className="mp-card-title">🎓 Academic Information</div>
            <div className="mp-fields">
              <Field label="Branch / Department" value={profile?.branch} />
              <Field label="Program"             value={profile?.program} />
              <Field label="Batch Year"          value={profile?.batchYear} />
              <Field label="CGPA" value={
                profile?.cgpa != null
                  ? `${profile.cgpa} / 10.0`
                  : "—"
              } highlight />
              <Field label="Active Backlogs" value={
                profile?.activeBacklogs != null
                  ? profile.activeBacklogs === 0
                    ? "0 ✓"
                    : String(profile.activeBacklogs)
                  : "—"
              } highlight={profile?.activeBacklogs === 0} />
            </div>
          </div>

          <div className="mp-card mp-card--full">
            <div className="mp-card-title">🏆 Placement Information</div>
            <div className="mp-placement-row">
              <div className="mp-placement-item">
                <div className="mp-pl-label">Placement Status</div>
                <div className="mp-pl-value">
                  <span className="mp-tier-badge" style={{ background: placementColor }}>
                    {profile?.placementTier === "Unplaced" || !profile?.placementTier
                      ? "Not Placed"
                      : `Placed (${profile.placementTier})`}
                  </span>
                </div>
              </div>
              <div className="mp-placement-item">
                <div className="mp-pl-label">Active Backlogs</div>
                <div className="mp-pl-value">
                  {profile?.activeBacklogs != null ? (
                    profile.activeBacklogs === 0
                      ? <span className="mp-elig-chip mp-elig-super">0 — Clear</span>
                      : <span className="mp-elig-chip mp-elig-block">
                          {profile.activeBacklogs} backlog{profile.activeBacklogs > 1 ? "s" : ""}
                        </span>
                  ) : "—"}
                </div>
              </div>
            </div>
            <div className="mp-readonly-note">
              🔒 Profile is read-only. Contact the Placement Cell to update your information.
            </div>
          </div>

        </div>
      </div>
    </Layout>
  );
}

export default MyProfile;```

---

## `frontend/src/pages/student/PlacementStatus.jsx`

_MODIFIED — tier colors/badge labels on A/B/C, eligibility tip wording_

```jsx
import { useEffect, useState } from "react";
import { Swiper, SwiperSlide } from "swiper/react";
import { Pagination } from "swiper/modules";
import "swiper/css";
import "swiper/css/pagination";
import Layout from "../../components/Layout";
import {
  getPlacementStatus,
  getPlacementOffers,
} from "../../services/studentPortalService";
import "./PlacementStatus.css";

const TIER_COLOR = {
  C: "#7c3aed",
  B: "#16a34a",
  A: "#2563eb",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

const PIPELINE = ["Applied", "Shortlisted", "Interview Scheduled", "Selected"];

function formatDate(ds) {
  if (!ds) return "—";
  return new Date(ds).toLocaleDateString("en-IN", {
    day: "2-digit", month: "long", year: "numeric",
  });
}

function ProgressTracker({ currentStage }) {
  const currentIdx = PIPELINE.indexOf(currentStage);
  const isRejected = currentStage === "Rejected";
  const allDone    = currentStage === "Selected";

  return (
    <div className="ps-tracker-card">
      <div className="ps-tracker-header">
        <div className="ps-tracker-title">Application Progress</div>
        {isRejected && (
          <span className="ps-tracker-rejected-badge">Not Progressed</span>
        )}
        {allDone && (
          <span className="ps-tracker-placed-badge">🎉 Placed!</span>
        )}
      </div>

      <div className="ps-tracker-steps">
        {PIPELINE.map((stage, i) => {
          const done   = allDone || (!isRejected && i < currentIdx);
          const active = !allDone && !isRejected && i === currentIdx;
          const cls    = done ? "ps-ts-done" : active ? "ps-ts-active" : "ps-ts-future";
          return (
            <div key={stage} className="ps-tracker-col">
              <div className={`ps-ts ${cls}`}>
                <div className="ps-ts-dot">{done ? "✓" : i + 1}</div>
                <div className="ps-ts-label">{stage}</div>
              </div>
              {i < PIPELINE.length - 1 && (
                <div className={`ps-ts-connector ${done ? "ps-ts-conn-done" : ""}`} />
              )}
            </div>
          );
        })}
      </div>

      {isRejected && (
        <div className="ps-tracker-rejected-note">
          Your application was not shortlisted at this time. Keep applying to other drives!
        </div>
      )}
    </div>
  );
}

function OfferCard({ offer }) {
  const tierColor = TIER_COLOR[offer.companyTier] ?? "#374151";
  return (
    <div className={`ps-offer-card ${offer.currentOffer ? "ps-offer-card--current" : "ps-offer-card--previous"}`}>
      {/* Badge */}
      <div className={`ps-offer-label ${offer.currentOffer ? "ps-offer-label--current" : "ps-offer-label--previous"}`}>
        {offer.currentOffer ? "⭐ Current Best Offer" : "Previous Offer"}
      </div>

      {/* Company + tier */}
      <div className="ps-offer-company">{offer.companyName ?? "—"}</div>
      {offer.companyTier && (
        <span className="ps-tier-badge" style={{ background: tierColor }}>
          {TIER_LABEL[offer.companyTier] ?? offer.companyTier}
        </span>
      )}

      {/* Details */}
      <div className="ps-offer-grid">
        <div className="ps-offer-item">
          <div className="ps-offer-item-label">Role</div>
          <div className="ps-offer-item-value">{offer.roleOffered ?? "—"}</div>
        </div>
        <div className="ps-offer-item">
          <div className="ps-offer-item-label">Package</div>
          <div className={`ps-offer-item-value ${offer.currentOffer ? "ps-offer-pkg-current" : "ps-offer-pkg-prev"}`}>
            {offer.packageLpa != null ? `${offer.packageLpa} LPA` : "—"}
          </div>
        </div>
        <div className="ps-offer-item">
          <div className="ps-offer-item-label">Date</div>
          <div className="ps-offer-item-value">{formatDate(offer.selectionDate)}</div>
        </div>
        <div className="ps-offer-item">
          <div className="ps-offer-item-label">Status</div>
          <div className="ps-offer-item-value">
            <span className="ps-offer-selected-chip">✓ Selected</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function PlacementStatus() {
  const [status,  setStatus]  = useState(null);
  const [offers,  setOffers]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    Promise.all([getPlacementStatus(), getPlacementOffers()])
      .then(([statusRes, offersRes]) => {
        setStatus(statusRes.data);
        setOffers(offersRes.data);
        setLoading(false);
      })
      .catch(() => {
        setError("Failed to load placement status.");
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <Layout>
        <div className="ps-container">
          <div className="ps-skeleton" style={{ height: 140 }} />
          <div className="ps-skeleton" style={{ height: 280 }} />
        </div>
      </Layout>
    );
  }

  if (error) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  const hasActiveApplication = status?.currentStage && status.currentStage !== "Not Applied";

  return (
    <Layout>
      <div className="ps-container">

        {/* ── Progress tracker ─────────────────────────────────────────────── */}
        {hasActiveApplication && (
          <ProgressTracker currentStage={status.currentStage} />
        )}

        {/* ── Pending offer (Offer Released — awaiting student decision) ──── */}
        {!status?.placed && status?.hasPendingOffer && (
          <div className="ps-pending-offer-card">
            <div className="ps-pending-offer-header">
              <span className="ps-pending-offer-icon">📩</span>
              <div>
                <div className="ps-pending-offer-title">Offer Available — Action Required</div>
                <div className="ps-pending-offer-sub">
                  {status.pendingOfferCompany}
                  {status.pendingOfferTier && (
                    <span className="ps-tier-badge ms-2"
                          style={{ background: TIER_COLOR[status.pendingOfferTier] ?? "#374151" }}>
                      {TIER_LABEL[status.pendingOfferTier] ?? status.pendingOfferTier}
                    </span>
                  )}
                </div>
              </div>
            </div>
            <div className="ps-pending-offer-meta">
              {status.pendingOfferRole && <span>{status.pendingOfferRole}</span>}
              {status.pendingOfferPackage != null && <span>💰 {status.pendingOfferPackage} LPA</span>}
              {status.pendingOfferDeadline && (
                <span style={{ color: "#dc2626" }}>⏰ Valid until {formatDate(status.pendingOfferDeadline)}</span>
              )}
            </div>
            <p className="ps-pending-offer-hint">
              Go to <strong>My Applications</strong> to accept or reject this offer.
            </p>
          </div>
        )}

        {status?.placed ? (
          /* ── PLACED — offer carousel ─────────────────────────────────────── */
          <>
            <div className="ps-placed-banner">
              <div className="ps-confetti-row">🎉 🎊 🏆 🎊 🎉</div>
              <div className="ps-placed-headline">Congratulations!</div>
              <div className="ps-placed-sub">
                {offers.length > 1
                  ? `You have accepted ${offers.length} placement offers. Your best offer is shown first.`
                  : "You have accepted a placement offer."}
              </div>
            </div>

            {offers.length > 0 && (
              <div className="ps-swiper-wrap">
                {offers.length === 1 ? (
                  <OfferCard offer={offers[0]} />
                ) : (
                  <Swiper
                    modules={[Pagination]}
                    pagination={{ clickable: true }}
                    spaceBetween={20}
                    slidesPerView={1}
                    className="ps-swiper"
                  >
                    {offers.map((offer) => (
                      <SwiperSlide key={offer.applicationId}>
                        <OfferCard offer={offer} />
                      </SwiperSlide>
                    ))}
                  </Swiper>
                )}

                {offers.length > 1 && (
                  <p className="ps-swipe-hint">← Swipe to see all offers →</p>
                )}
              </div>
            )}
          </>
        ) : hasActiveApplication ? (
          /* ── IN PROGRESS ─────────────────────────────────────────────────── */
          <div className="ps-pending-card">
            <div className="ps-pending-icon">⏳</div>
            <div className="ps-pending-headline">Placement In Progress</div>
            <div className="ps-pending-sub">
              {status?.message ?? "Your placement journey is in progress. Keep applying!"}
            </div>

            <div className="ps-tips-grid">
              <div className="ps-tip">
                <span className="ps-tip-icon">📋</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Track Your Applications</div>
                  <div className="ps-tip-text">
                    Monitor status updates on your My Applications page.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">🎯</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Apply to More Drives</div>
                  <div className="ps-tip-text">
                    Increase your chances by applying to more eligible drives.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">📈</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Prepare for Interviews</div>
                  <div className="ps-tip-text">
                    Review technical topics relevant to your applied roles.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">✅</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Stay Reachable</div>
                  <div className="ps-tip-text">
                    Keep your contact details updated so the placement cell can reach you.
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          /* ── NO APPLICATIONS YET ─────────────────────────────────────────── */
          <div className="ps-pending-card">
            <div className="ps-pending-icon">🚀</div>
            <div className="ps-pending-headline">Start Your Journey</div>
            <div className="ps-pending-sub">
              You haven&apos;t applied to any drives yet. Browse eligible drives and submit your first application!
            </div>

            <div className="ps-tips-grid">
              <div className="ps-tip">
                <span className="ps-tip-icon">🎯</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Browse Eligible Drives</div>
                  <div className="ps-tip-text">
                    Check drives you qualify for based on your CGPA and backlog criteria.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">📈</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Improve Your Profile</div>
                  <div className="ps-tip-text">
                    Higher CGPA unlocks Tier B and Tier C company drives.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">✅</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Clear Backlogs</div>
                  <div className="ps-tip-text">
                    Zero active backlogs maximises your eligible drive count.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">📋</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Prepare Early</div>
                  <div className="ps-tip-text">
                    Review aptitude, DSA, and company-specific prep materials.
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

      </div>
    </Layout>
  );
}

export default PlacementStatus;
```

---

## `frontend/src/pages/student/StudentDashboard.jsx`

_MODIFIED — placement tier badge label on A/B/C_

```jsx
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../../components/Layout";
import {
  getStudentProfile,
  getEligibleDrives,
  getStudentApplications,
  getRecentAchievements,
} from "../../services/studentPortalService";
import "./StudentDashboard.css";

const PLACEMENT_COLOR = {
  C:           "#7c3aed",
  B:           "#16a34a",
  A:           "#2563eb",
  "Unplaced":  "#475569",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };


function StatCard({ icon, label, value, accent, to }) {
  const inner = (
    <div className="sd-stat-card" style={{ "--sd-accent": accent }}>
      <div className="sd-stat-icon">{icon}</div>
      <div className="sd-stat-value">{value ?? "—"}</div>
      <div className="sd-stat-label">{label}</div>
    </div>
  );
  return to ? <Link to={to} className="sd-stat-link">{inner}</Link> : inner;
}

function AppStatusPill({ status }) {
  const COLOR = {
    Applied:               "#2563eb",
    Shortlisted:           "#7c3aed",
    "Interview Scheduled": "#d97706",
    Selected:              "#16a34a",
    Rejected:              "#dc2626",
    Withdrawn:             "#94a3b8",
  };
  return (
    <span className="sd-app-status" style={{ background: COLOR[status] ?? "#94a3b8" }}>
      {status}
    </span>
  );
}

function Skeleton({ h = 60 }) {
  return <div className="sd-skeleton" style={{ height: h }} />;
}

function StudentDashboard() {
  const [profile,      setProfile]      = useState(null);
  const [eligible,     setEligible]     = useState([]);
  const [apps,         setApps]         = useState([]);
  const [achievements, setAchievements] = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState(null);

  useEffect(() => {
    Promise.all([
      getStudentProfile(),
      getEligibleDrives(),
      getStudentApplications(),
      getRecentAchievements(),
    ])
      .then(([p, e, a, ach]) => {
        setProfile(p.data);
        setEligible(e.data);
        setApps(a.data);
        setAchievements(ach.data);
        setLoading(false);
      })
      .catch(() => {
        setError("Failed to load your dashboard. Please try again.");
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <Layout>
        <div className="sd-container">
          <Skeleton h={130} />
          <div className="sd-stat-grid">
            {[1,2,3,4,5].map((i) => <Skeleton key={i} />)}
          </div>
          <Skeleton h={120} />
          <Skeleton h={200} />
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="alert alert-danger m-3">{error}</div>
      </Layout>
    );
  }

  // Only count drives the student is genuinely eligible for
  const eligibleCount    = eligible.filter((d) => d.eligible !== false).length;
  const shortlistedCount = apps.filter((a) => a.status === "Shortlisted").length;
  const interviewCount   = apps.filter((a) => a.status === "Interview Scheduled").length;
  const offersCount      = apps.filter((a) => a.status === "Selected").length;

  const recentApps = apps.slice(0, 5);

  // Open drives the student is eligible for and hasn't applied to yet
  const openDrives = eligible
    .filter((d) => d.eligible !== false && !d.alreadyApplied && d.driveStatus !== "Completed")
    .slice(0, 4);

  return (
    <Layout>
      <div className="sd-container">

        {/* ── Welcome Banner ─────────────────────────────────────────────── */}
        <div className="sd-welcome-card">
          <div className="sd-avatar">
            {profile?.name?.charAt(0)?.toUpperCase() ?? "S"}
          </div>
          <div className="sd-welcome-body">
            <div className="sd-greeting">Welcome back,</div>
            <div className="sd-name">{profile?.name ?? "Student"}</div>
            <div className="sd-meta-row">
              {profile?.branch    && <span className="sd-meta-chip">{profile.branch}</span>}
              {profile?.program   && <span className="sd-meta-chip">{profile.program}</span>}
              {profile?.batchYear && <span className="sd-meta-chip">Batch {profile.batchYear}</span>}
              {profile?.cgpa != null && (
                <span className="sd-meta-chip">CGPA {profile.cgpa}</span>
              )}
            </div>
          </div>
          <div className="sd-welcome-right">
            {(() => {
              const placed = profile?.placementTier && profile.placementTier !== "Unplaced";
              const hasPending = !placed && apps.some((a) => a.status === "Offer Released");
              const label  = placed
                ? `Placed (${TIER_LABEL[profile.placementTier] ?? profile.placementTier})`
                : hasPending ? "📩 Offer Pending"
                : "Not Placed";
              const bg = placed
                ? (PLACEMENT_COLOR[profile.placementTier] ?? "#475569")
                : hasPending ? "#d97706"
                : "#475569";
              return (
                <span className="sd-tier-badge" style={{ background: bg }}>
                  {label}
                </span>
              );
            })()}
          </div>
        </div>

        {/* ── Stat Cards ─────────────────────────────────────────────────── */}
        <div className="sd-stat-grid">
          <StatCard
            icon="🎯" label="Eligible Drives"
            value={eligibleCount}
            accent="#2563eb"
            to="/student/eligible-drives"
          />
          <StatCard
            icon="📄" label="Applied"
            value={apps.length}
            accent="#7c3aed"
            to="/student/applications"
          />
          <StatCard
            icon="⭐" label="Shortlisted"
            value={shortlistedCount}
            accent="#d97706"
            to="/student/applications"
          />
          <StatCard
            icon="📅" label="Interviews"
            value={interviewCount}
            accent="#ea580c"
            to="/student/applications"
          />
          <StatCard
            icon="🏆" label="Offers"
            value={offersCount}
            accent="#16a34a"
            to="/student/placement-status"
          />
        </div>

        {/* ── Quick Actions ───────────────────────────────────────────────── */}
        <div className="sd-section-card">
          <div className="sd-section-head">Quick Actions</div>
          <div className="sd-quick-links">
            <Link to="/student/eligible-drives"  className="sd-quick-btn sd-qb-blue">
              🎯 Browse Drives
            </Link>
            <Link to="/student/applications"     className="sd-quick-btn sd-qb-purple">
              📋 My Applications
            </Link>
            <Link to="/student/profile"          className="sd-quick-btn sd-qb-slate">
              👤 View Profile
            </Link>
            <Link to="/student/placement-status" className="sd-quick-btn sd-qb-green">
              🏆 Placement Status
            </Link>
          </div>
        </div>

        {/* ── Recent Placement Achievements (public — Selected only) ──────── */}
        {achievements.length > 0 && (
          <div className="sd-section-card">
            <div className="sd-section-head">
              🎉 Recent Placement Achievements
            </div>
            <div className="sd-achievement-list">
              {achievements.map((a, i) => (
                <div key={i} className="sd-achievement-row">
                  <div className="sd-achievement-icon">🎊</div>
                  <div className="sd-achievement-body">
                    <span className="sd-achievement-name">{a.studentName}</span>
                    <span className="sd-achievement-text"> selected at </span>
                    <span className="sd-achievement-company">{a.companyName ?? "—"}</span>
                    {a.roleOffered && (
                      <span className="sd-achievement-role"> · {a.roleOffered}</span>
                    )}
                  </div>
                  {a.packageLpa && (
                    <span className="sd-achievement-pkg">{a.packageLpa} LPA</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ── My Recent Applications (own data only) ──────────────────────── */}
        {recentApps.length > 0 && (
          <div className="sd-section-card">
            <div className="sd-section-head">
              My Recent Applications
              <Link to="/student/applications" className="sd-section-link">View All</Link>
            </div>
            <div className="sd-app-list">
              {recentApps.map((a) => (
                <div key={a.applicationId} className="sd-app-row">
                  <div className="sd-app-company">{a.companyName ?? "—"}</div>
                  <div className="sd-app-role">{a.roleOffered ?? "—"}</div>
                  <AppStatusPill status={a.status} />
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ── Open Drives You Can Apply To ────────────────────────────────── */}
        {openDrives.length > 0 && (
          <div className="sd-section-card">
            <div className="sd-section-head">
              Open Drives You Can Apply To
              <Link to="/student/eligible-drives" className="sd-section-link">View All</Link>
            </div>
            <div className="sd-drive-list">
              {openDrives.map((d) => (
                <div key={d.driveId} className="sd-drive-row">
                  <div className="sd-drive-company">{d.companyName ?? "—"}</div>
                  <div className="sd-drive-role">{d.roleOffered}</div>
                  {d.packageLpa && (
                    <span className="sd-drive-pkg">{d.packageLpa} LPA</span>
                  )}
                  <Link to="/student/eligible-drives" className="sd-apply-link">
                    Apply →
                  </Link>
                </div>
              ))}
            </div>
          </div>
        )}

      </div>
    </Layout>
  );
}

export default StudentDashboard;
```

