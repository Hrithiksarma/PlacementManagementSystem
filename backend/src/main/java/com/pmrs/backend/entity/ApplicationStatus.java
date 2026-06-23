package com.pmrs.backend.entity;

/**
 * Valid states for Application.status.
 * DB values (stored as display strings): Applied / Shortlisted /
 * Interview Scheduled / Selected / Rejected.
 * Transition rules are enforced by ApplicationStatusValidator.
 */
public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    SELECTED,
    OFFER_RELEASED,
    OFFER_ACCEPTED,
    OFFER_REJECTED,
    REJECTED,
    WITHDRAWN
}
