package com.kt.cdp.common.enums;

/**
 * Stored status of a movement. OVERDUE is NOT stored here - it is derived
 * (status == OUT AND expectedReturn &lt; now) in queries and the scheduled job.
 */
public enum MovementStatus {
    SCHEDULED, OUT, RETURNED, CANCELLED
}
