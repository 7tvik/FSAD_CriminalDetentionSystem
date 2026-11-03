# Testing — KT_CDP_019 Criminal Detention Portal

## Strategy

Testing focuses where the risk is: the **business rules and state machines** in the service layer,
the **security posture** (authentication + authorization), and the **JWT** mechanics. The layering
supports two test styles:

- **Unit tests** (JUnit 5 + Mockito, no Spring context) — pure service logic with mocked
  repositories. Fast, deterministic, one behavior per test.
- **Slice / integration tests** (`@SpringBootTest` + MockMvc + Spring Security Test) — exercise the
  real security filter chain and controller wiring against an in-memory **H2** database
  (`@ActiveProfiles("test")`, MySQL-compatibility mode), so no MySQL server is needed to run tests.

The data-seeding `CommandLineRunner` (`DataInitializer`) is mocked (`@MockBean`) in integration
tests so startup does not depend on Flyway-seeded rows.

## Running

```bash
cd backend
mvn test
```

**Result: 31 tests, 0 failures, 0 errors.**

> Offline-environment note: this workstation's local Maven cache was missing the surefire provider
> and `junit-platform-launcher` versions that Spring Boot 3.3.4 pins, which are normally fetched on
> first build. The suite was verified green by pinning cached versions:
> `mvn -o test -Dmaven-surefire-plugin.version=3.5.6 -Djunit-jupiter.version=5.10.0`.
> In a normal (online) environment plain `mvn test` needs no such flags. `pom.xml` includes a
> surefire `<includes>` block so `*IT` integration tests run under `mvn test`.

## Coverage

### Unit tests (Mockito)

| Test class | Tests | What it asserts |
|------------|:----:|-----------------|
| `JwtServiceTest` | 4 | Generate + extract username; valid for matching user; tampered token rejected; expired token rejected |
| `ReleaseServiceTest` | 4 | UPCOMING→READY; READY→RELEASED sets actual date + prisoner RELEASED; illegal RELEASED→UPCOMING throws; CANCELLED terminal |
| `InterviewServiceTest` | 3 | PENDING→APPROVED sets reviewer/date; illegal PENDING→COMPLETED throws; duplicate request → conflict |
| `MovementServiceTest` | 4 | markReturned from OUT ok; from SCHEDULED throws; cancel from SCHEDULED ok; overdue query delegates correctly |
| `CourtMovementServiceTest` | 2 | SCHEDULED→DEPARTED sets departure time; skipping to AT_COURT throws |
| `PrisonerServiceTest` | 4 | Duplicate prisoner number → conflict; expected release < admission → business rule; soft-delete → inactive/INACTIVE; missing id → not found |
| `DutyServiceTest` | 2 | end ≤ start rejected; overlapping duty rejected |
| `AuthServiceTest` | 3 | Duplicate username → conflict; register ignores requested roles (assigns GOVERNMENT_OFFICIAL only); wrong current password → business rule |

### Slice / integration tests (Spring + MockMvc)

| Test class | Tests | What it asserts |
|------------|:----:|-----------------|
| `AuthControllerIT` | 3 | Login returns 200 + token; bad password → 401; unauthenticated `GET /api/prisoners` → 401 |
| `SecurityAuthorizationIT` | 2 | `POST /api/prisoners` with only `PRISONER_READ` → **403**; with `PRISONER_WRITE` → **201** |

**Total: 31 tests.**

## What each layer proves

- **State machines** — every register with a lifecycle has at least one legal-transition test and
  one illegal-transition test asserting the `BusinessRuleException`/422 path.
- **Security** — bad credentials give 401, missing token gives 401, and insufficient permissions
  give 403 through the *real* filter chain (not mocked), confirming backend-enforced authorization.
- **Auth policy** — self-registration cannot escalate privileges (requested roles are ignored;
  only `GOVERNMENT_OFFICIAL` is granted).
- **Data integrity** — uniqueness (prisoner/case numbers, usernames) and cross-field rules
  (release ≥ admission, duty end > start, no overlapping duty) are enforced in the service layer.

## Manual / smoke verification (runtime)

Against the running MySQL-backed backend, the following were confirmed end-to-end:

- Flyway `V1`+`V2` apply cleanly; 5 demo users seeded; all 14 domains return `200`.
- RBAC returns `403`/`200` per role; business rules return `422`; validation `400`; auth `401`.
- Swagger UI loads and authorizes with a bearer token.
- Frontend builds (`tsc --noEmit` clean, `npm run build`) and the SPA drives the API through the
  Vite `/api` proxy: login, dashboard KPIs/charts, register CRUD, release diary + CSV export,
  status transitions, and notifications.

## Not covered (scope boundaries)

Frontend unit tests, exhaustive controller-per-endpoint tests, and load/performance testing are
out of scope for this academic build. The chosen tests target the highest-risk logic (security and
state machines) rather than maximizing raw line coverage.
