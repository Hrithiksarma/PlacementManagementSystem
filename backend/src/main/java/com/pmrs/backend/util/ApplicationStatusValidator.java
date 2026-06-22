package com.pmrs.backend.util;

import java.util.List;
import java.util.Map;

/**
 * Enforces the one-way status progression for placement applications.
 *
 * Admin flow (via updateStatus):
 *   Applied → Shortlisted → Interview Scheduled → Selected → Offer Released
 *   Any non-final stage → Rejected
 *
 * Student flow (via acceptOffer / rejectOffer):
 *   Offer Released → Offer Accepted | Offer Rejected
 *
 * System flow (automatic):
 *   Any non-final stage → Withdrawn (triggered by Offer Accepted at a blocking tier)
 */
public class ApplicationStatusValidator {

    private static final Map<String, List<String>> VALID_TRANSITIONS = Map.of(
            "Applied",             List.of("Shortlisted", "Rejected"),
            "Shortlisted",         List.of("Interview Scheduled", "Rejected"),
            "Interview Scheduled", List.of("Selected", "Rejected"),
            "Selected",            List.of("Offer Released", "Rejected"),
            "Offer Released",      List.of("Offer Accepted", "Offer Rejected")
    );

    /**
     * Final statuses — no further transitions allowed.
     * Note: "Selected" is NOT final — it leads to "Offer Released".
     * "Withdrawn" is system-set on revocation and cannot be manually assigned.
     */
    public static boolean isFinalStatus(String status) {
        return "Offer Accepted".equals(status)
            || "Offer Rejected".equals(status)
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
