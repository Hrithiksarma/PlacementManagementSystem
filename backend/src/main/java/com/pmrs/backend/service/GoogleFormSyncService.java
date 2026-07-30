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

    @Value("${google.forms.sheet-range:Form Responses 1}")
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
            s.setPptDate(parseDate(cell(row, cols.pptDate)));
            s.setResumeSelectionDate(parseDate(cell(row, cols.resumeSelectionDate)));
            s.setFinalSelectionDate(parseDate(cell(row, cols.finalSelectionDate)));
            s.setRoleOffered(roleOffered);
            s.setJdUrl(cell(row, cols.jdUrl));
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
            packageLpa = -1, driveType = -1, minCgpa = -1, maxBacklogs = -1, jdUrl = -1,
            pptDate = -1, resumeSelectionDate = -1, finalSelectionDate = -1;

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
                } else if (m.jdUrl < 0 && (h.equals("jd upload") || h.equals("job description document"))) {
                    m.jdUrl = i;
                } else if (m.hrName < 0 && h.equals("contact person name")) {
                    m.hrName = i;
                } else if (m.hrEmail < 0 && h.startsWith("contact person e-mail")) {
                    m.hrEmail = i;
                } else if (m.hrPhone < 0 && h.startsWith("contact number 1")) {
                    m.hrPhone = i;
                } else if (m.driveDate < 0 && h.startsWith("tentative date of online/offline exam")) {
                    m.driveDate = i;
                } else if (m.pptDate < 0 && h.startsWith("tentative date of pre placement talk")) {
                    m.pptDate = i;
                } else if (m.resumeSelectionDate < 0 && h.startsWith("tentative date of resume selection")) {
                    m.resumeSelectionDate = i;
                } else if (m.finalSelectionDate < 0 && h.startsWith("tentative date of final selection")) {
                    m.finalSelectionDate = i;
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
                } else if (m.jdUrl < 0 && (h.contains("jd") || h.contains("attachment")
                        || h.contains("job desc"))) {
                    m.jdUrl = i;
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
