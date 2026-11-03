package com.kt.cdp.common;

/**
 * Central catalogue of permission names. Kept in sync with the permissions
 * seeded in V2__seed_data.sql. Used in @PreAuthorize expressions as
 * hasAuthority('PERM_...').
 */
public final class Permissions {
    private Permissions() {}

    public static final String PRISONER_READ = "PRISONER_READ";
    public static final String PRISONER_WRITE = "PRISONER_WRITE";
    public static final String PRISONER_DELETE = "PRISONER_DELETE";
    public static final String CASE_READ = "CASE_READ";
    public static final String CASE_WRITE = "CASE_WRITE";
    public static final String ARTICLE_READ = "ARTICLE_READ";
    public static final String ARTICLE_WRITE = "ARTICLE_WRITE";
    public static final String RELEASE_READ = "RELEASE_READ";
    public static final String RELEASE_WRITE = "RELEASE_WRITE";
    public static final String DUTY_READ = "DUTY_READ";
    public static final String DUTY_WRITE = "DUTY_WRITE";
    public static final String INTERVIEW_READ = "INTERVIEW_READ";
    public static final String INTERVIEW_WRITE = "INTERVIEW_WRITE";
    public static final String INTERVIEW_REVIEW = "INTERVIEW_REVIEW";
    public static final String MOVEMENT_READ = "MOVEMENT_READ";
    public static final String MOVEMENT_WRITE = "MOVEMENT_WRITE";
    public static final String COURT_READ = "COURT_READ";
    public static final String COURT_WRITE = "COURT_WRITE";
    public static final String REPORT_READ = "REPORT_READ";
    public static final String DASHBOARD_READ = "DASHBOARD_READ";
    public static final String NOTIFICATION_READ = "NOTIFICATION_READ";
    public static final String USER_READ = "USER_READ";
    public static final String USER_WRITE = "USER_WRITE";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_WRITE = "ROLE_WRITE";
    public static final String AUDIT_READ = "AUDIT_READ";
}
