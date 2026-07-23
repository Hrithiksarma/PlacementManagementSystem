package com.pmrs.backend.entity;

/**
 * Valid states for Application.status.
 * DB values (stored as display strings): Applied / First Round /
 * Interview Scheduled / Selected / Rejected. "Selected" is the job-offer
 * stage itself (no separate Offer Released state).
 * Transition rules are enforced by ApplicationStatusValidator.
 */
public enum ApplicationStatus {
    APPLIED,
    FIRST_ROUND,
    INTERVIEW_SCHEDULED,
    SELECTED,
    OFFER_ACCEPTED,
    OFFER_REJECTED,
    OFFER_DECLINED,
    REJECTED,
    WITHDRAWN
}
