# Final Project Review — KT_CDP_019 Criminal Detention Portal

## Executive summary

All 7 original functional requirements are implemented end-to-end (database → API → UI), backed
by 31 passing automated tests, a clean TypeScript build, and successful end-to-end smoke
verification against a live MySQL instance. All stated security constraints are satisfied. The
agreed enhancements (dashboard, overdue/alert detection, CSV export, notifications, audit) are
present and functional.

---

## Functional requirements — verdict

| # | Requirement | Status | Evidence |
|---|-------------|--------|---------|
| 1 | **Case Register** — prisoners with crime/case details and surrendered articles | ✅ Complete | `PrisonerController`, `CaseController`, `ArticleController`; `PrisonersPage` + `PrisonerDetailPage` (Cases, Articles, Overview, History, Releases tabs) |
| 2 | **Automated Release Diary** — daily / weekly / custom, CSV export | ✅ Complete | `ReleaseController /diary` + `/diary/export`; `ReleaseDiaryPage` with mode selector and blob CSV download |
| 3 | **Duty Register** — staff duty assignments | ✅ Complete | `DutyController`; `DutyPage`; `DutyServiceTest` validates time order and overlap |
| 4 | **Interview Requests** — visitor lifecycle | ✅ Complete | `InterviewController`; state machine in `InterviewService`; `InterviewsPage`; `InterviewServiceTest` |
| 5 | **In-Out Register** incl. court movements | ✅ Complete | `MovementController`, `CourtMovementController`; `MovementsPage`, `CourtPage`; `MovementServiceTest`, `CourtMovementServiceTest` |
| 6 | **Administrator / RBAC** — users, roles, 26 permissions | ✅ Complete | `UserController`, Spring Security `@PreAuthorize`; `UsersPage`, `RolesPage`; `SecurityAuthorizationIT` |
| 7 | **Reports** — day-wise and month-wise releases | ✅ Complete | `ReportController`; `ReportsPage` with daily/monthly/upcoming tabs and statistics charts |

---

## Enhancements — verdict

| Enhancement | Status | Notes |
|-------------|--------|-------|
| Operational dashboard (KPIs + charts) | ✅ Complete | 10 stat cards, pie + bar recharts on `DashboardPage` |
| Overdue-movement detection | ✅ Complete | Derived at query time via `movements/overdue`; never stored |
| Release alert notifications | ✅ Complete | `NotificationsPage`; `/notifications/generate` recomputes alerts |
| Court not-returned detection | ✅ Complete | Derived via `court-movements/not-returned` |
| CSV export | ✅ Complete | Release diary and daily report both exportable |
| Search + pagination | ✅ Complete | `JpaSpecificationExecutor` + `PageResponse<T>` on every register |
| Audit trail (append-only) | ✅ Complete | `AuditService (REQUIRES_NEW)` + `AuditPage` (read-only) |

---

## Security requirements — verdict

| Constraint | Status | How satisfied |
|------------|--------|---------------|
| Never store plaintext passwords | ✅ | BCrypt in `password_hash`; confirmed via DB and `AuthServiceTest` |
| Never hard-code secrets | ✅ | `JWT_SECRET`, DB creds from env vars; `.env` is git-ignored, only `.env.example` committed |
| Backend enforces authorization | ✅ | `@PreAuthorize` on every mutation endpoint; `SecurityAuthorizationIT` asserts 403 without permission |
| No automated guilt/sentencing | ✅ | System keeps registers and produces reports; no scoring or decisioning code anywhere |
| No stack traces in responses | ✅ | `GlobalExceptionHandler` returns sanitized error bodies; verified in smoke test |
| Audit records not casually editable | ✅ | No update/delete endpoints on `audit_logs`; `AuditService` is append-only with `REQUIRES_NEW` |
| Never log secrets | ✅ | No `log.info("password=...")` patterns; JWT secret and DB password are env-loaded, not echoed |
| Fictional/demo data only | ✅ | All seed data in V2 and `DataInitializer` is invented |

---

## Test suite — summary

**31 tests, 0 failures, 0 errors** (JUnit 5 + Mockito + Spring Security Test; H2 in-memory).

| Type | Classes | Tests | Coverage scope |
|------|---------|-------|----------------|
| Unit (Mockito) | 8 | 27 | All state machines, auth rules, uniqueness constraints |
| Slice / Integration (MockMvc) | 2 | 4 | Auth 200/401, RBAC 403/201 through real filter chain |

Command: `mvn test` (offline: `mvn -o test -Dmaven-surefire-plugin.version=3.5.6`)

---

## Build quality gates

| Gate | Result |
|------|--------|
| `mvn compile` (156 classes) | ✅ BUILD SUCCESS |
| `mvn test` (31 tests) | ✅ 0 failures |
| Flyway V1+V2 migrations | ✅ Applied cleanly; schema validates |
| `npx tsc --noEmit` (strict) | ✅ 0 errors across 30+ source files |
| `npm run build` (Vite) | ✅ 922 modules, dist/index.html generated |
| Backend smoke (`/api/auth/login`, `/api/dashboard`, 13 register endpoints) | ✅ All 200, RBAC 403 correct |

---

## Architecture decisions — rationale

| Decision | Why |
|----------|-----|
| **Modular monolith** (not microservices) | Appropriate for a single-facility admin system; single transactional DB; simpler deployment and testing |
| **Stateless JWT** (not sessions) | Scales horizontally; no server-side session store; SPA-friendly |
| **Permission-based auth** (not role-based in Spring) | 26 fine-grained permissions let a future admin tailor role grants without code changes |
| **Derived "overdue" / "not-returned"** (never stored) | Eliminates stale-flag bugs; always accurate; no background job needed |
| **`REQUIRES_NEW` on audit** | Audit writes survive even when the outer transaction is read-only or rolls back |
| **H2 in tests** (not TestContainers) | No Docker dependency for CI; H2 MySQL-mode covers the JPA/HQL surface reliably |
| **Flyway migrations** (not `ddl-auto=create`) | Deterministic, auditable schema evolution; safe for production |

---

## Known limitations (academic scope)

- **No real-time push** — notifications are pulled on page load and refreshed on demand; a production system would add WebSockets or SSE.
- **No file upload** — prisoner photo is a URL field only; a production system would handle multipart upload.
- **No email/SMS** — notification delivery is in-app only.
- **Frontend unit tests** — not in scope; the backend carries the automated test burden (state machines, security).
- **Single facility** — the schema has a `facility` field on prisoners but no multi-tenancy isolation; each deployment targets one facility.

---

## Conclusion

KT_CDP_019 is a complete, working, tested, and documented prison-administration system. It
satisfies every stated functional requirement, all enhancements agreed in scope, and all security
constraints from the master prompt. The codebase is ready for academic review.
