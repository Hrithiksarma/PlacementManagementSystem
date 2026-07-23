package com.pmrs.backend.dto;

import java.util.List;

/**
 * Result of the "Import All Students" bulk action — how many staged
 * submissions became real students, and which ones failed and why.
 * Modeled on {@link BackfillResultDTO}.
 */
public class BulkImportResultDTO {

    /** One failed row: the student's name (or email) and why it was skipped. */
    public static class Failure {
        private final String name;
        private final String reason;

        public Failure(String name, String reason) {
            this.name = name;
            this.reason = reason;
        }

        public String getName()   { return name; }
        public String getReason() { return reason; }
    }

    private final int created;
    private final int failed;
    private final List<Failure> failures;

    public BulkImportResultDTO(int created, int failed, List<Failure> failures) {
        this.created = created;
        this.failed = failed;
        this.failures = failures;
    }

    public int getCreated() { return created; }
    public int getFailed() { return failed; }
    public List<Failure> getFailures() { return failures; }
}
