package com.kt.cdp.dto.interview;

/**
 * Optional body for interview review transitions (approve / reject / schedule /
 * complete / cancel). Carries free-text remarks explaining the decision.
 */
public record InterviewReviewRequest(
        String remarks
) {}
