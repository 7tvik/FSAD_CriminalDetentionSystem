# Architecture — KT_CDP_019 Criminal Detention Portal

## 1. Style and rationale

The system is a **modular monolith**: a single Spring Boot deployable, organised internally into
feature modules (prisoners, cases, releases, duties, interviews, movements, court, admin, audit).
This was a deliberate choice for an academic administration system — it gives clear module
boundaries and a single transactional database without the operational complexity of
microservices. There is **no** microservice split, no MongoDB, and no Node backend.

```
┌──────────────────────────┐        ┌───────────────────────────────────────┐
│   React SPA (Vite)       │  HTTPS │        Spring Boot (Java 21)          │
│  react-router + axios    │ ─────► │  Controller → Service → Repository     │
│  JWT in localStorage     │  /api  │  Spring Security (JWT filter)          │
│  recharts dashboard      │ ◄───── │  Flyway • JPA/Hibernate                │
└──────────────────────────┘  JSON  └────────────────┬──────────────────────┘
                                                      │ JDBC
                                              ┌───────┴────────┐
                                              │   MySQL 8      │
                                              └────────────────┘
```

## 2. Backend layers

| Layer | Package | Responsibility |
|-------|---------|----------------|
| **Controller** | `controller` | REST endpoints, request/response DTOs, `@PreAuthorize` authorization, pagination params. No business logic. |
| **Service** | `service` | Business rules, **state machines**, transactions, validation, audit + notification hooks. |
| **Repository** | `repository` | Spring Data JPA interfaces; `JpaSpecificationExecutor` for dynamic paginated search. |
| **Entity** | `entity` | JPA `@Entity` classes mapping the schema. |
| **DTO** | `dto` | Java `record` request/response types — entities are never serialized directly. |
| **Security** | `security` | JWT generation/validation, authentication filter, `UserDetails`. |
| **Config** | `config` | Security filter chain, CORS, OpenAPI, data seeding. |
| **Exception** | `exception` | Typed exceptions + `@RestControllerAdvice` global handler. |

**Dependency rule:** controllers depend on services; services depend on repositories. DTOs cross
the controller boundary; entities never leave the service layer.

## 3. Security architecture

- **Stateless JWT.** `POST /api/auth/login` verifies credentials (BCrypt) and returns a signed
  HS256 token carrying the username. No server session; every request re-authenticates via a
  `OncePerRequestFilter` that validates the token and loads authorities.
- **Permission-based authorization.** Each of 26 permissions is a Spring authority. Endpoints are
  guarded with `@PreAuthorize("hasAuthority('PRISONER_WRITE')")` (etc.) and
  `@EnableMethodSecurity`. Roles are bundles of permissions:

  | Role | Focus |
  |------|-------|
  | `ADMINISTRATOR` | Everything, incl. user management |
  | `PRISON_OFFICER` | Registers, movements, releases, interviews, duties |
  | `POLICE_OFFICIAL` | Cases, court movements |
  | `GOVERNMENT_OFFICIAL` | Read-only + reports |
  | `AUDITOR` | Read-only + audit logs |

- **Backend is the source of truth.** The SPA hides controls the user lacks permission for, but
  every mutation is independently enforced server-side — a hidden button is UX, not security.
- **Secrets** (`JWT_SECRET`, DB credentials) are read from environment variables; none are
  hard-coded or committed. Passwords are persisted only as BCrypt hashes and never logged.
- **Password policy** enforced on registration/change (min length + letter + digit).

## 4. State machines

Lifecycle transitions are validated in the service layer; an illegal transition raises a
`BusinessRuleException` → HTTP **422**. Derived states are computed, never persisted.

| Domain | States & legal transitions |
|--------|----------------------------|
| **Release** | `UPCOMING → {READY, ON_HOLD, CANCELLED}` · `READY → {RELEASED, ON_HOLD, CANCELLED}` · `ON_HOLD → {UPCOMING, READY, CANCELLED}` · `RELEASED`, `CANCELLED` terminal. Reaching `RELEASED` sets `actual_release_date` and flips the prisoner to `RELEASED`. |
| **Interview** | `PENDING → approve/reject/cancel` · `APPROVED → schedule/cancel` · `SCHEDULED → complete/cancel` · `REJECTED`/`COMPLETED`/`CANCELLED` terminal. |
| **Movement** | Created with `time_out` set → `OUT`, else `SCHEDULED`. `markReturned` valid only from `OUT`; `cancel` valid from `SCHEDULED`/`OUT`. **Overdue** = `OUT` and past `expected_return` (derived). |
| **Court hearing** | Forward-only `SCHEDULED → DEPARTED → AT_COURT → HEARING_COMPLETED → DEPARTED_COURT → RETURNED`, plus `SCHEDULED → CANCELLED`. Skipping a step is rejected. **Not-returned** is derived. |

## 5. Cross-cutting concerns

- **Search & pagination.** List endpoints accept `page`, `size`, `sort`, and per-domain filters,
  build a JPA `Specification`, and return a uniform `PageResponse<T>` envelope
  (`content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`).
- **Audit trail.** An `AuditService` writes append-only `audit_logs` rows (action, entity, actor,
  before/after, IP). It runs in a **`REQUIRES_NEW`** transaction so audit writes succeed even when
  the surrounding request transaction is read-only or later rolls back, and so the audit record is
  never lost with the business change.
- **Notifications.** Domain events (upcoming releases, overdue movements, pending interviews,
  court hearings) generate `notifications`; a generate endpoint recomputes alerts on demand.
- **Error handling.** A single `@RestControllerAdvice` maps exceptions to a consistent JSON error
  body — `400` validation, `401` unauthenticated, `403` forbidden, `404` not found, `409` conflict,
  `422` business-rule violation. **Stack traces are never exposed** to clients.
- **Reports** are read-only aggregations (day-wise / month-wise / upcoming releases, prisoner- and
  case-status statistics), served as DTOs and, for the release diary, exportable as CSV.

## 6. Frontend architecture

- **Foundation / feature split.** A shared foundation (`api`, `auth`, `components`, `hooks`,
  `styles`, `types`, `utils`) provides the axios client, auth context, UI primitives, and design
  tokens; each register is a page under `pages/` following one standard list-page pattern.
- **Auth.** `AuthProvider` stores the JWT + user in `localStorage`, exposes
  `hasPermission/hasAnyPermission/hasRole`; `ProtectedRoute` gates routes; the axios response
  interceptor clears state and redirects on `401`.
- **Type safety.** `src/types/index.ts` mirrors the backend DTOs (including string-union enums), so
  the client stays honest against the API contract. The build runs under TypeScript strict mode.
- **Presentation.** Institutional slate/steel-blue palette via CSS custom properties; recharts for
  dashboard visualizations; no CSS-in-JS.

## 7. Environments & profiles

- **Runtime:** MySQL 8 (Docker); Flyway migrates + seeds on startup; `DataInitializer` seeds users.
- **Test:** `@ActiveProfiles("test")` uses in-memory **H2** (MySQL-compatibility mode) so unit and
  slice tests run without a database server; the seeding `CommandLineRunner` is mocked out in
  integration tests.
