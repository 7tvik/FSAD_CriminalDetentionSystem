# Requirements Traceability — KT_CDP_019 Criminal Detention Portal

The system's purpose (from the spec): *a collection of registers and reports for the effective
management of prisons.* This matrix maps every functional requirement to its backend and frontend
implementation and its verification.

## Core functional requirements

| # | Requirement | Backend | Frontend | Verified by |
|---|-------------|---------|----------|-------------|
| **1** | **Case Register** — register prisoners with crime/case details **and surrendered articles** | `PrisonerController`, `CaseController`, `ArticleController`; `prisoners`/`cases`/`surrendered_articles` tables | `PrisonersPage`, `PrisonerDetailPage` (Cases + Articles tabs) | `PrisonerServiceTest`; `SecurityAuthorizationIT` (write requires `PRISONER_WRITE`) |
| **2** | **Automated Release Diary** — daily / weekly / custom range, exportable | `ReleaseController` `/diary` + `/diary/export` (CSV) | `ReleaseDiaryPage` (mode selector + CSV export) | Manual smoke; diary derives from `expected_release_date` |
| **3** | **Duty Register** — staff duty assignments | `DutyController`; `duty_register` table | `DutyPage` | `DutyServiceTest` (time order + overlap rejection) |
| **4** | **Interview Requests** — visitor request lifecycle | `InterviewController`; state machine in `InterviewService` | `InterviewsPage` | `InterviewServiceTest` (transitions + duplicate conflict) |
| **5** | **In-Out Register** — movements incl. **Court movements** | `MovementController`, `CourtMovementController`; `movements`/`court_hearings` | `MovementsPage`, `CourtPage` | `MovementServiceTest`, `CourtMovementServiceTest` |
| **6** | **Administrator / RBAC** — users, roles, 26 permissions | `UserController`, `RoleController`; Spring Security `@PreAuthorize` | `UsersPage`, `RolesPage`, `ProfilePage` | `SecurityAuthorizationIT` (403 vs 201), `AuthControllerIT` |
| **7** | **Reports** — day-wise & month-wise releases | `ReportController` (daily, monthly, upcoming, stats) | `ReportsPage` | Manual smoke; aggregation queries |

## Enhancements (light, agreed scope)

| Feature | Backend | Frontend | Notes |
|---------|---------|----------|-------|
| **Operational dashboard** | `DashboardController` KPI aggregates | `DashboardPage` (10 KPI cards + pie/bar charts) | recharts |
| **Overdue-movement detection** | `MovementService.findOverdue` / `/movements/overdue` | dashboard KPI + Movements filter | **Derived**, never stored |
| **Release alerts / notifications** | `NotificationController` `/generate` | `NotificationsPage` | Recomputed on demand |
| **Court not-returned detection** | `/court-movements/not-returned` | `CourtPage` | Derived |
| **CSV export** | release diary + daily report | export buttons | Blob download |
| **Search + pagination** | `JpaSpecificationExecutor` + `PageResponse<T>` | standard list-page pattern + `Pagination` | Every register |
| **Audit trail** | `AuditService` (append-only, `REQUIRES_NEW`) | `AuditPage` (read-only) | Immutable |

## Non-functional & security requirements

| Requirement | How it is met | Verified by |
|-------------|---------------|-------------|
| Never store plaintext passwords | BCrypt hashes only | `AuthServiceTest`; DB inspection |
| Never hard-code secrets | `JWT_SECRET`, DB creds from env vars; `.env` git-ignored | Config review |
| Enforce authz on the backend | `@PreAuthorize` + `@EnableMethodSecurity`; frontend hiding is UX only | `SecurityAuthorizationIT` (403 without permission) |
| Stateless authentication | JWT (HS256, jjwt), `OncePerRequestFilter` | `JwtServiceTest`, `AuthControllerIT` |
| Fictional/demo data only | Seed data is invented | Migration/seed review |
| No automated guilt/sentencing decisions | System keeps registers + reports; no scoring/decisioning code | Code review |
| Do not expose stack traces | `@RestControllerAdvice` returns sanitized error bodies | Error-handling tests / manual |
| Audit not casually editable | No update/delete endpoints on `audit_logs` | API surface review |
| Never log secrets | No logging of passwords/JWT/DB password | Code review |
| Validation on input | `jakarta.validation` on DTOs → `400` | Bean validation; manual |

## Requirement → state-machine mapping

| Requirement | Lifecycle enforced (illegal transition → 422) |
|-------------|-----------------------------------------------|
| Release Diary (#2) | Release: UPCOMING/READY/ON_HOLD/RELEASED/CANCELLED |
| Interviews (#4) | PENDING→APPROVED/REJECTED→SCHEDULED→COMPLETED (+CANCELLED) |
| In-Out Register (#5) | Movement: SCHEDULED/OUT→RETURNED (+CANCELLED); overdue derived |
| Court movements (#5) | Forward-only SCHEDULED→…→RETURNED (+CANCELLED); not-returned derived |

## Coverage summary

All 7 core functional requirements are implemented end-to-end (DB → API → UI), guarded by RBAC,
and — where they carry business rules or lifecycles — covered by automated tests. All stated
security constraints are satisfied and, where testable, asserted. See
[FINAL_PROJECT_REVIEW.md](FINAL_PROJECT_REVIEW.md) for the closing self-assessment.
