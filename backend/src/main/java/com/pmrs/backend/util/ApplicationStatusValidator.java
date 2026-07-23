package com.pmrs.backend.util;

import java.util.List;
import java.util.Map;

/**
 * Enforces the one-way status progression for placement applications.
 *
 * Admin flow (via updateStatus):
 *   Applied → First Round → Interview Scheduled → Selected,
 *   where skipping ahead is allowed (e.g. Applied → Selected) but moving
 *   backwards is not. Any non-final stage → Rejected. "Selected" IS the job
 *   offer (there is no separate "Offer Released" stage) — from there the
 *   decision belongs to the student.
 *
 * Student flow (via acceptOffer / rejectOffer):
 *   Selected → Offer Accepted | Offer Rejected
 *
 * System flow (automatic):
 *   Any non-final stage → Withdrawn (triggered by Offer Accepted at a blocking tier)
 */
public class ApplicationStatusValidator {

    /** The admin pipeline, in order. Forward jumps of any length are valid. */
    private static final List<String> PIPELINE = List.of(
            "Applied", "First Round", "Interview Scheduled", "Selected");

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
        transitions.put("Selected", List.of("Offer Accepted", "Offer Rejected"));
        return Map.copyOf(transitions);
    }

    /**
     * Final statuses — no further transitions allowed.
     * Note: "Selected" is NOT final — the student still decides Accept/Reject.
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
