// ── StudentNotFoundException.java ─────────────────────────────
package com.academic.service.exception;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(String rollNo) {
        super("No student found with roll number: " + rollNo);
    }
}
