package com.pmrs.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.pmrs.backend.entity.StudentFormSubmission;
import com.pmrs.backend.repository.StudentFormSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Pulls new student registrations from the student Google Form's linked
 * response Sheet into the StudentFormSubmissions staging table.
 *
 * <p>Columns are located by the sheet's header row (row 1), matching each
 * header against keywords ("name", "email", "cgpa", "resume", ...), so the
 * form's question order doesn't matter. If the first row doesn't look like a
 * header, falls back to the fixed A–J order:
 * A Timestamp · B Full Name · C Email · D Phone · E Program · F Branch ·
 * G Batch Year · H CGPA · I Active Backlogs · J Resume.
 *
 * <p>Runs on a schedule and on demand via POST /student-form-submissions/sync.
 * Mirrors {@link GoogleFormSyncService} but reads the {@code
 * google.student-forms.*} config and its own dedicated Sheets client.
 */
@Service
public class StudentFormSyncService {

    private static final Logger log = LoggerFactory.getLogger(StudentFormSyncService.class);

    private static final String CLASSPATH_PREFIX = "classpath:";

    private final StudentFormSubmissionRepository submissionRepository;
    private final AdminNotificationService        adminNotificationService;

    @Value("${google.student-forms.spreadsheet-id:}")
    private String spreadsheetId;

    @Value("${google.student-forms.sheet-range:Form Responses 1!A1:Z}")
    private String sheetRange;

    @Value("${google.student-forms.credentials-path:classpath:google-service-account.json}")
    private String credentialsPath;

    private Sheets sheetsClient;

    public StudentFormSyncService(StudentFormSubmissionRepository submissionRepository,
                                  AdminNotificationService        adminNotificationService) {
        this.submissionRepository     = submissionRepository;
        this.adminNotificationService = adminNotificationService;
    }

    @Scheduled(fixedDelayString = "${google.student-forms.poll-interval-ms:300000}")
    public void scheduledSync() {
        try {
            syncResponses();
        } catch (Exception e) {
            // Scheduled runs must never take the app down over a transient
            // network/API failure — log and try again on the next tick.
            log.warn("Student Google Form sync failed: {}", e.getMessage());
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
            log.warn("google.student-forms.spreadsheet-id is not set — skipping student form sync.");
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
            log.warn("First student-sheet row doesn't look like a header — assuming the fixed A–J column order.");
            cols = ColumnMap.positional();
            firstDataRow = 0;
        }

        int created = 0;
        for (int i = firstDataRow; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            String timestamp = cell(row, cols.timestamp);
            String email     = cell(row, cols.email);
            String fullName  = cell(row, cols.fullName);
            String rollNo    = cell(row, cols.rollNo);

            // Blank filler rows at the bottom of the sheet
            if (timestamp.isEmpty() && email.isEmpty() && fullName.isEmpty() && rollNo.isEmpty()) {
                continue;
            }

            // Roll number is the true student identifier coming out of the form.
            String responseKey = timestamp + "|" + rollNo;
            if (submissionRepository.existsByResponseKey(responseKey)) {
                continue;
            }

            StudentFormSubmission s = new StudentFormSubmission();
            s.setResponseKey(responseKey);
            s.setSubmittedAt(timestamp);
            s.setFullName(fullName);
            s.setRollNo(emptyToNull(rollNo));
            s.setEmail(email);
            s.setPhone(cell(row, cols.phone));
            s.setProgram(cell(row, cols.program));
            s.setBranch(cell(row, cols.branch));
            s.setBatchYear(deriveBatchYear(rollNo, cell(row, cols.batchYear)));
            s.setCgpa(parseDouble(cell(row, cols.cgpa)));
            s.setActiveBacklogs(parseBacklogCount(cell(row, cols.activeBacklogs)));
            s.setResumeUrl(emptyToNull(cell(row, cols.resumeUrl)));
            s.setPhotoUrl(emptyToNull(cell(row, cols.photoUrl)));
            s.setGradeSheetUrl(emptyToNull(cell(row, cols.gradeSheetUrl)));
            s.setStatus(StudentFormSubmission.STATUS_PENDING);

            submissionRepository.save(s);
            created++;

            // If a prior flagged submission from the same student is still
            // pending, this row is their resubmission — supersede the old
            // one and let the placement cell know.
            Optional<StudentFormSubmission> prior = Optional.empty();
            if (s.getRollNo() != null) {
                prior = submissionRepository.findFirstByRollNoAndFlaggedTrueAndStatusOrderBySubmissionIdDesc(
                        s.getRollNo(), StudentFormSubmission.STATUS_PENDING);
            }
            if (prior.isEmpty() && s.getEmail() != null && !s.getEmail().isBlank()) {
                prior = submissionRepository.findFirstByEmailAndFlaggedTrueAndStatusOrderBySubmissionIdDesc(
                        s.getEmail(), StudentFormSubmission.STATUS_PENDING);
            }
            if (prior.isPresent()) {
                StudentFormSubmission old = prior.get();
                old.setStatus(StudentFormSubmission.STATUS_SUPERSEDED);
                submissionRepository.save(old);
                adminNotificationService.notifyResubmission(s);
            }
        }

        if (created > 0) {
            log.info("Student form sync: {} new student submission(s) staged.", created);
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
                        + " — put the JSON key in src/main/resources or set google.student-forms.credentials-path.");
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

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    /**
     * Maps each submission field to its column index in the sheet.
     * An index of -1 means "no such column"; {@link #cell} returns "" for it.
     */
    private static final class ColumnMap {
        int timestamp = -1, fullName = -1, email = -1, phone = -1, program = -1,
            branch = -1, batchYear = -1, cgpa = -1, activeBacklogs = -1, rollNo = -1,
            resumeUrl = -1, photoUrl = -1, gradeSheetUrl = -1;

        /**
         * Best-effort fixed layout, used only when the first row isn't a
         * recognizable header. Matches the institute registration form's
         * observed column order.
         */
        static ColumnMap positional() {
            ColumnMap m = new ColumnMap();
            m.timestamp = 0; m.email = 1; m.fullName = 4; m.rollNo = 10; m.phone = 11;
            m.photoUrl = 13; m.program = 20; m.branch = 21; m.cgpa = 27;
            m.gradeSheetUrl = 28; m.activeBacklogs = 31; m.resumeUrl = 33;
            return m;
        }

        /**
         * Builds a map from the header row. Pass 1 matches the institute
         * registration form's distinctive question texts; pass 2 fills any
         * still-unmapped field by generic keywords so a simpler/reordered form
         * still lands. Returns null if the row doesn't look like a header
         * (no timestamp, or neither email nor name).
         */
        static ColumnMap fromHeader(List<Object> headerRow) {
            ColumnMap m = new ColumnMap();
            String[] headers = new String[headerRow.size()];
            for (int i = 0; i < headerRow.size(); i++) {
                Object raw = headerRow.get(i);
                headers[i] = raw == null ? ""
                        : raw.toString().trim().toLowerCase().replaceAll("\\s+", " ");
            }

            // Pass 1 — the institute registration form's specific questions.
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i];
                if (h.isEmpty()) {
                    continue;
                }
                if (m.timestamp < 0 && h.equals("timestamp")) {
                    m.timestamp = i;
                } else if (m.email < 0 && h.contains("institute email address")) {
                    m.email = i;                                   // primary = institute email
                } else if (m.fullName < 0 && h.equals("name")) {
                    m.fullName = i;
                } else if (m.rollNo < 0 && (h.equals("roll no") || h.equals("roll number"))) {
                    m.rollNo = i;
                } else if (m.batchYear < 0 && h.contains("batch year")) {
                    m.batchYear = i;                               // "Batch Year (Year of Admission)"
                } else if (m.phone < 0 && h.equals("mobile number")) {
                    m.phone = i;
                } else if (m.program < 0 && h.equals("course")) {
                    m.program = i;                                 // "Course" = B.Tech / M.Tech / PhD
                } else if (m.branch < 0 && h.equals("branch")) {
                    m.branch = i;
                } else if (m.cgpa < 0 && h.contains("cgpa till")) {
                    m.cgpa = i;                                    // current CGPA at IIITG
                } else if (m.photoUrl < 0 && h.contains("profile photo")) {
                    m.photoUrl = i;
                } else if (m.gradeSheetUrl < 0 && (h.contains("gradesheet")
                        || h.contains("gadesheet") || h.contains("grade sheet")
                        || h.contains("grade card") || h.contains("gradecard"))) {
                    m.gradeSheetUrl = i;
                } else if (m.resumeUrl < 0 && h.contains("resume")) {
                    m.resumeUrl = i;
                } else if (m.activeBacklogs < 0 && h.contains("backlog") && h.contains("existing")) {
                    m.activeBacklogs = i;
                }
            }

            // Pass 2 — generic keywords for whatever pass 1 didn't find,
            // so a simpler student form (Full Name / Email / Program / ...) also works.
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i];
                if (h.isEmpty()) {
                    continue;
                }
                if (m.timestamp < 0 && h.contains("timestamp")) {
                    m.timestamp = i;
                } else if (m.photoUrl < 0 && h.contains("photo")) {
                    m.photoUrl = i;
                } else if (m.gradeSheetUrl < 0 && (h.contains("grade") || h.contains("marksheet"))) {
                    m.gradeSheetUrl = i;
                } else if (m.resumeUrl < 0 && (h.contains("resume") || h.contains("cv"))) {
                    m.resumeUrl = i;
                } else if (m.activeBacklogs < 0 && h.contains("backlog")) {
                    m.activeBacklogs = i;
                } else if (m.cgpa < 0 && (h.contains("cgpa") || h.contains("gpa"))) {
                    m.cgpa = i;
                } else if (m.rollNo < 0 && h.contains("roll")) {
                    m.rollNo = i;
                } else if (m.batchYear < 0 && (h.contains("batch") || h.contains("admission year"))) {
                    m.batchYear = i;
                } else if (m.program < 0 && (h.contains("program") || h.contains("course")
                        || h.contains("degree"))) {
                    m.program = i;
                } else if (m.branch < 0 && (h.contains("branch") || h.contains("department")
                        || h.contains("dept") || h.contains("discipline"))) {
                    m.branch = i;
                } else if (m.phone < 0 && (h.contains("phone") || h.contains("mobile")
                        || h.contains("contact"))) {
                    m.phone = i;
                } else if (m.email < 0 && h.contains("mail")) {
                    m.email = i;
                } else if (m.fullName < 0 && h.contains("name") && !h.contains("father")
                        && !h.contains("college") && !h.contains("school")) {
                    m.fullName = i;
                }
            }

            if (m.timestamp < 0 || (m.email < 0 && m.fullName < 0)) {
                return null;
            }
            return m;
        }
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
     * Prefers the form's explicit "Batch Year (Year of Admission)" answer when
     * it's a plausible year. Falls back to the roll number, which encodes the
     * admission year at IIITG (e.g. "2521512" → 2025), for older responses
     * submitted before that question existed.
     */
    private static Integer deriveBatchYear(String rollNo, String batchCell) {
        Integer fromCell = parseInt(batchCell);
        if (fromCell != null && fromCell >= 2015 && fromCell <= 2035) {
            return fromCell;
        }
        if (rollNo != null) {
            String digits = rollNo.replaceAll("[^0-9]", "");
            if (digits.length() >= 2) {
                int year = 2000 + Integer.parseInt(digits.substring(0, 2));
                if (year >= 2015 && year <= 2035) {
                    return year;
                }
            }
        }
        return fromCell;
    }

    /**
     * The backlog question holds subject codes or "NIL", not a count. Treat a
     * pure number as the count, "NIL"/blank as 0, and anything else (subject
     * codes) as 0 rather than mis-parsing digits out of course codes.
     */
    private static Integer parseBacklogCount(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        String t = raw.trim();
        if (t.matches("\\d+")) {
            return Integer.parseInt(t);
        }
        return 0;
    }
}
