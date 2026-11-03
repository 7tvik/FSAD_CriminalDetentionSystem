package com.kt.cdp.common.enums;

/** Status of an interview request. Transitions enforced in InterviewService. */
public enum InterviewStatus {
    PENDING, APPROVED, REJECTED, SCHEDULED, COMPLETED, CANCELLED
}
