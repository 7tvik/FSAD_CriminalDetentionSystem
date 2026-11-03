package com.kt.cdp.common.enums;

/** Fine-grained lifecycle of a court production. Transitions enforced in CourtMovementService. */
public enum CourtHearingStatus {
    SCHEDULED, DEPARTED, AT_COURT, HEARING_COMPLETED, DEPARTED_COURT, RETURNED, CANCELLED
}
