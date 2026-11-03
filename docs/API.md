# API Reference — KT_CDP_019 Criminal Detention Portal

Base URL: **`/api`** (backend on `http://localhost:8080`). All responses are JSON.
Interactive docs: **`/swagger-ui/index.html`** · OpenAPI JSON: **`/v3/api-docs`**.

## Authentication

All endpoints except `POST /api/auth/login` and `POST /api/auth/register` require a
`Authorization: Bearer <token>` header. Obtain a token from login:

```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "Admin@123" }
```

```json
{
  "token": "eyJhbGci...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "fullName": "System Administrator",
  "roles": ["ADMINISTRATOR"],
  "permissions": ["PRISONER_READ", "PRISONER_WRITE", "..."]
}
```

## Conventions

- **Pagination:** list endpoints accept `page` (0-based), `size`, `sort` (e.g. `createdAt,desc`),
  plus per-endpoint filters. They return a `PageResponse<T>` envelope:
  `{ content, page, size, totalElements, totalPages, first, last }`.
- **Errors** use a consistent body; status codes: `400` validation, `401` unauthenticated,
  `403` forbidden, `404` not found, `409` conflict, `422` business-rule violation.

  ```json
  { "timestamp": "...", "status": 422, "error": "Unprocessable Entity",
    "message": "Illegal transition RELEASED → UPCOMING", "path": "/api/releases/5/status" }
  ```
- **Authorization** is per-endpoint via permissions (shown in the tables below as the required
  authority). Read = `*_READ`, mutations = `*_WRITE`/`*_DELETE`.

---

## Auth — `/api/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/login` | public | Authenticate, return JWT + profile |
| POST | `/register` | public | Self-register (assigned `GOVERNMENT_OFFICIAL` only) |
| POST | `/change-password` | authenticated | Change own password |
| POST | `/logout` | authenticated | Client-side token discard (stateless) |

## Prisoners (Case Register) — `/api/prisoners`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | `PRISONER_READ` | Search/paginate (filters: `search`, `status`) |
| GET | `/{id}` | `PRISONER_READ` | Prisoner detail |
| POST | `/` | `PRISONER_WRITE` | Register prisoner |
| PUT | `/{id}` | `PRISONER_WRITE` | Update prisoner |
| DELETE | `/{id}` | `PRISONER_DELETE` | Soft-delete (INACTIVE) |
| GET | `/{id}/cases` | `CASE_READ` | Cases for prisoner |
| POST | `/{id}/cases` | `CASE_WRITE` | Add case |
| GET | `/{id}/articles` | `ARTICLE_READ` | Surrendered articles |
| POST | `/{id}/articles` | `ARTICLE_WRITE` | Add article |
| GET | `/{id}/history` | `PRISONER_READ` | Aggregated history (cases/articles/releases/movements) |

## Cases — `/api/cases`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | `CASE_READ` | Search cases (filters: `search`, `crimeType`, `status`, `court`) |
| GET | `/{id}` | `CASE_READ` | Case detail |
| PUT | `/{id}` | `CASE_WRITE` | Update case |

## Surrendered articles — `/api/articles`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| PUT | `/{id}` | `ARTICLE_WRITE` | Update article |
| POST | `/{id}/return` | `ARTICLE_WRITE` | Mark article returned |

## Releases & Release Diary — `/api/releases`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | `RELEASE_READ` | Search release records (filter: `status`) |
| GET | `/{id}` | `RELEASE_READ` | Release detail |
| GET | `/prisoners/{pid}` | `RELEASE_READ` | Releases for a prisoner |
| POST | `/` | `RELEASE_WRITE` | Create release record |
| PUT | `/{id}` | `RELEASE_WRITE` | Update release record |
| PUT | `/{id}/status` | `RELEASE_WRITE` | Transition status (state machine) |
| GET | `/diary` | `RELEASE_READ` | **Automated release diary** (`mode`=DAILY/WEEKLY/CUSTOM, `from`, `to`) |
| GET | `/diary/export` | `RELEASE_READ` | Diary as CSV download |

## Reports — `/api/reports`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/releases/daily` | `REPORT_VIEW` | Day-wise release report (`date`) |
| GET | `/releases/daily/export` | `REPORT_VIEW` | Daily report CSV |
| GET | `/releases/monthly` | `REPORT_VIEW` | Month-wise release report (`year`, `month`) |
| GET | `/releases/upcoming` | `REPORT_VIEW` | Upcoming releases (`days`) |
| GET | `/stats/prisoners` | `REPORT_VIEW` | Prisoner counts by status |
| GET | `/stats/cases` | `REPORT_VIEW` | Case counts by status |

## Duty Register — `/api/duties`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | `DUTY_READ` | Search duties (filters: `staffId`, `date`, `location`) |
| GET | `/{id}` | `DUTY_READ` | Duty detail |
| POST | `/` | `DUTY_WRITE` | Create duty (validates times + overlap) |
| PUT | `/{id}` | `DUTY_WRITE` | Update duty |
| DELETE | `/{id}` | `DUTY_WRITE` | Delete duty |

## In-Out Register (Movements) — `/api/movements`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | `MOVEMENT_READ` | Search movements (filters: `status`, `type`, `date`) |
| GET | `/overdue` | `MOVEMENT_READ` | Overdue movements (derived) |
| GET | `/{id}` | `MOVEMENT_READ` | Movement detail |
| POST | `/` | `MOVEMENT_WRITE` | Record movement (OUT if `timeOut` set, else SCHEDULED) |
| PUT | `/{id}` | `MOVEMENT_WRITE` | Update movement |
| POST | `/{id}/return` | `MOVEMENT_WRITE` | Mark returned (only from OUT) |
| POST | `/{id}/cancel` | `MOVEMENT_WRITE` | Cancel movement |

## Court Movements — `/api/court-movements`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | `COURT_READ` | Search court hearings |
| GET | `/not-returned` | `COURT_READ` | Hearings not yet returned (derived) |
| GET | `/{id}` | `COURT_READ` | Hearing detail |
| POST | `/` | `COURT_WRITE` | Schedule court movement |
| PUT | `/{id}` | `COURT_WRITE` | Update hearing |
| PUT | `/{id}/status` | `COURT_WRITE` | Advance status (forward-only state machine) |

## Interview Requests — `/api/interviews`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | `INTERVIEW_READ` | Search requests (filter: `status`) |
| GET | `/{id}` | `INTERVIEW_READ` | Request detail |
| POST | `/` | `INTERVIEW_WRITE` | Create request |
| POST | `/{id}/approve` | `INTERVIEW_WRITE` | Approve (from PENDING) |
| POST | `/{id}/reject` | `INTERVIEW_WRITE` | Reject (from PENDING) |
| POST | `/{id}/schedule` | `INTERVIEW_WRITE` | Schedule (from APPROVED) |
| POST | `/{id}/complete` | `INTERVIEW_WRITE` | Complete (from SCHEDULED) |
| POST | `/{id}/cancel` | `INTERVIEW_WRITE` | Cancel |

## Notifications — `/api/notifications`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | authenticated | List notifications (paged) |
| POST | `/{id}/read` | authenticated | Mark read |
| POST | `/generate` | `NOTIFICATION_READ` | Recompute operational alerts |

## Dashboard — `/api/dashboard`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | authenticated | KPI aggregates for the dashboard |

## Administration — `/api/users`, `/api/roles`, `/api/staff`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/users` | `USER_READ` | List users (paged) |
| GET | `/users/me` | authenticated | Current user profile |
| GET | `/users/{id}` | `USER_READ` | User detail |
| POST | `/users` | `USER_WRITE` | Create user |
| PUT | `/users/{id}` | `USER_WRITE` | Update user (email, name, roles) |
| POST | `/users/{id}/activate` | `USER_WRITE` | Activate user |
| POST | `/users/{id}/deactivate` | `USER_WRITE` | Deactivate user |
| GET | `/roles` | `USER_READ` | List roles + their permissions |
| GET | `/roles/permissions` | `USER_READ` | List all permissions |
| GET | `/staff` | `DUTY_READ` | List staff (for duty/escort selection) |

## Audit — `/api/audit-logs`

| Method | Path | Permission | Description |
|--------|------|-----------|-------------|
| GET | `/` | `AUDIT_VIEW` | Search audit log (filters: `action`, `entityType`, `userId`, `from`, `to`); **read-only, append-only — no update/delete** |
