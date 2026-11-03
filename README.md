# KT_CDP_019 — Criminal Detention Portal

An academic full-stack **prison-administration system**: *a collection of registers and reports
for the effective management of prisons*. It digitises the day-to-day registers a detention
facility keeps — the case register, release diary, duty roster, interview requests, and the
in-out (movement) register — and adds role-based access, an operational dashboard, notifications,
and an append-only audit trail on top.

> **Academic / demonstration project.** All data is fictional. The system keeps registers and
> produces reports; it makes **no** automated decisions about guilt, sentencing, or risk.

---

## Table of contents

- [Feature overview](#feature-overview)
- [Technology stack](#technology-stack)
- [Repository layout](#repository-layout)
- [Prerequisites](#prerequisites)
- [Quick start](#quick-start)
  - [1. Database (MySQL via Docker)](#1-database-mysql-via-docker)
  - [2. Backend (Spring Boot)](#2-backend-spring-boot)
  - [3. Frontend (React + Vite)](#3-frontend-react--vite)
- [Demo accounts](#demo-accounts)
- [Configuration & environment variables](#configuration--environment-variables)
- [Testing](#testing)
- [API documentation (Swagger)](#api-documentation-swagger)
- [Security notes](#security-notes)
- [Further documentation](#further-documentation)

---

## Feature overview

| # | Requirement | What it does |
|---|-------------|--------------|
| 1 | **Case Register** | Register prisoners with their crime/case details (FIR, sections, court, investigating officer) and the **surrendered articles** taken into custody at admission. |
| 2 | **Automated Release Diary** | Generates the upcoming-release diary for a **day / week / custom range** from expected release dates, with CSV export. |
| 3 | **Duty Register** | Records staff duty assignments (location, purpose, date, shift times) with overlap validation. |
| 4 | **Interview Requests** | Visitor interview requests with a review lifecycle (pending → approved/rejected → scheduled → completed). |
| 5 | **In-Out Register** | Tracks movements of prisoners/staff in and out of the facility, including a dedicated **Court movement** register with hearing outcomes. |
| 6 | **Administrator / RBAC** | User management, 5 roles, 26 fine-grained permissions enforced on the backend. |
| 7 | **Reports** | Day-wise and month-wise release reports, upcoming-release report, prisoner- and case-status statistics. |

**Enhancements** layered on the core: operational **dashboard** with KPI cards and charts,
**overdue-movement** and **release-alert** detection (derived, never stored), in-app
**notifications**, **audit logging**, and search + pagination across every register.

---

## Technology stack

**Backend**
- Java 21, Spring Boot 3.3.4 (modular monolith)
- Spring Web, Spring Security (stateless **JWT**, HS256 via jjwt 0.12.6, BCrypt)
- Spring Data JPA / Hibernate, `JpaSpecificationExecutor` for paginated search
- MySQL 8 with **Flyway** migrations (`V1` schema, `V2` seed)
- springdoc-openapi 2.6.0 (Swagger UI)
- JUnit 5 + Mockito + Spring Security Test (H2 in-memory for the test profile)

**Frontend**
- React 18 + TypeScript (strict) + Vite
- react-router-dom v6, axios (JWT interceptor + 401 handling), recharts

---

## Repository layout

```
criminal/
├── backend/                 Spring Boot application
│   ├── src/main/java/com/kt/cdp/
│   │   ├── config/          Security, OpenAPI, CORS, data seeding
│   │   ├── auth/            Authentication + audit service
│   │   ├── security/        JWT service, filter, user details
│   │   ├── entity/          JPA entities
│   │   ├── repository/      Spring Data repositories
│   │   ├── service/         Business logic + state machines
│   │   ├── controller/      REST controllers
│   │   ├── dto/             Request/response records
│   │   └── exception/       Global handler + typed exceptions
│   └── src/main/resources/db/migration/   Flyway V1 + V2
├── frontend/                React + Vite SPA
│   └── src/{api,auth,components,hooks,pages,styles,types,utils}
└── docs/                    Architecture, database, API, requirements, testing
```

## Prerequisites

- **JDK 21** and **Maven 3.9+**
- **Node.js 18+** and npm
- **Docker** (for MySQL) — or a locally installed MySQL 8

## Quick start

### 1. Database (MySQL via Docker)

Copy the example environment file and start MySQL:

```bash
cp .env.example .env        # then review the values
docker run -d --name cdp-mysql -p 3306:3306 \
  -e MYSQL_DATABASE=cdp \
  -e MYSQL_USER=cdp_user \
  -e MYSQL_PASSWORD=cdp_local_dev_pw \
  -e MYSQL_ROOT_PASSWORD=cdp_local_root_pw \
  mysql:8.4
```

> A `docker-compose.yml` is also provided. If your Docker install has Compose v2
> (`docker compose up -d`), that is the simplest path. The `docker run` command above is the
> fallback used when Compose is unavailable.

Flyway creates and seeds the schema automatically the first time the backend starts.

### 2. Backend (Spring Boot)

```bash
cd backend
set -a && . ../.env && set +a     # export DB creds + JWT secret into the environment
mvn spring-boot:run
```

The API comes up on **http://localhost:8080**. On first start Flyway applies `V1`/`V2` and a
`DataInitializer` seeds the five demo users.

### 3. Frontend (React + Vite)

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

The SPA runs on **http://localhost:5173** and proxies `/api` to the backend on port 8080.

## Demo accounts

All demo passwords follow the pattern shown; they are seeded (BCrypt-hashed) at startup.

| Username | Password | Role | Access |
|----------|----------|------|--------|
| `admin`   | `Admin@123`   | Administrator      | Full access, user management |
| `officer` | `Officer@123` | Prison Officer     | Registers, movements, releases, interviews |
| `police`  | `Police@123`  | Police Official    | Cases, court movements |
| `govt`    | `Govt@123`    | Government Official | Read-only + reports |
| `auditor` | `Auditor@123` | Auditor            | Read-only + audit logs |

## Configuration & environment variables

**No secret is hard-coded.** The backend reads everything from environment variables (see
`.env.example`); credentials and the JWT secret must be supplied at runtime.

| Variable | Purpose |
|----------|---------|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | MySQL connection |
| `DB_USERNAME`, `DB_PASSWORD` | Application DB user |
| `MYSQL_ROOT_PASSWORD` | Root password for the MySQL container only |
| `JWT_SECRET` | HS256 signing key (min 32 bytes) — **must** be set |
| `JWT_EXPIRATION_MS` | Token lifetime (default 24h) |
| `CORS_ALLOWED_ORIGINS` | Allowed SPA origin(s) |
| `VITE_API_BASE_URL` | Frontend API base (defaults to `/api`) |

The real `.env` is git-ignored; only `.env.example` (placeholder values) is committed.

## Testing

```bash
cd backend
mvn test
```

Runs 31 tests (unit + Spring slice/integration) against an in-memory H2 database — no MySQL
required. See [docs/TESTING.md](docs/TESTING.md) for coverage details.

## API documentation (Swagger)

With the backend running:

- Swagger UI: **http://localhost:8080/swagger-ui/index.html**
- OpenAPI JSON: **http://localhost:8080/v3/api-docs**

Click **Authorize** and paste a JWT (from `POST /api/auth/login`) to call secured endpoints.

## Security notes

- Passwords are stored **only** as BCrypt hashes; plaintext is never persisted or logged.
- Authorization is enforced on the **backend** via `@PreAuthorize`; hiding frontend buttons is UX only.
- Secrets come from environment variables, never source control.
- Audit records are append-only (no update/delete endpoints).
- All data is fictional; the system performs no automated guilt/sentencing decisions.

## Further documentation

| Document | Contents |
|----------|----------|
| [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) | Requirement traceability matrix |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layers, security, state machines, cross-cutting concerns |
| [docs/DATABASE.md](docs/DATABASE.md) | Schema, tables, relationships, migrations |
| [docs/API.md](docs/API.md) | REST endpoint reference |
| [docs/TESTING.md](docs/TESTING.md) | Test strategy and coverage |
| [docs/FINAL_PROJECT_REVIEW.md](docs/FINAL_PROJECT_REVIEW.md) | Final self-review against the spec |
# FSAD
