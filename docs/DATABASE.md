# Database — KT_CDP_019 Criminal Detention Portal

**Engine:** MySQL 8 (InnoDB). **Schema management:** Flyway. Enums are stored as `VARCHAR`
and mapped to Java enums via JPA `@Enumerated(STRING)`, so values are human-readable in the DB
and safe to reorder in code.

## Migrations

| Version | File | Purpose |
|---------|------|---------|
| `V1` | `V1__schema.sql` | All 17 tables, indexes, and foreign keys |
| `V2` | `V2__seed_data.sql` | Roles, 26 permissions, role→permission grants, staff, and fictional demo data (prisoners, cases, articles, movements, …) |

Flyway runs automatically at backend startup and records applied versions in
`flyway_schema_history`. Migrations are **immutable once applied** — schema changes ship as new
`V3+` files, never by editing `V1`/`V2`.

> Demo **users** (with BCrypt password hashes) are seeded by a `DataInitializer`
> `CommandLineRunner` rather than in SQL, so no password hash is committed to a migration file.

## Entity-relationship overview

```
                    ┌──────────┐        ┌──────────────┐
      user_roles ───┤  users   ├────────┤  role_perms  ├─── permissions
                    └────┬─────┘        └──────┬───────┘
                         │                     │
                         │                  ┌──┴───┐
                         │                  │roles │
                         │                  └──────┘
                    ┌────┴─────┐
                    │  staff   │────────────┐
                    └────┬─────┘            │ (escort_officer)
                         │ (created_by)     │
        ┌────────────────┼──────────────────┼──────────────────────┐
        │                │                  │                       │
   ┌────┴─────┐    ┌─────┴──────┐     ┌──────┴──────┐        ┌──────┴───────┐
   │  duty_   │    │ prisoners  │◄────┤  movements  │        │ court_       │
   │ register │    └─────┬──────┘     └──────┬──────┘        │ hearings     │
   └──────────┘          │                   │ (movement_id) │              │
                         │◄──────────────────┴───────────────┤              │
         ┌───────────────┼───────────────┬──────────────┐    └──────────────┘
         │               │               │              │
   ┌─────┴──────┐  ┌──────┴───────┐ ┌─────┴──────┐ ┌─────┴─────────┐
   │   cases    │  │ surrendered_ │ │  release_  │ │  interview_   │
   │            │  │  articles    │ │  records   │ │  requests     │
   └────────────┘  └──────────────┘ └────────────┘ └───────────────┘

   notifications ── recipient_user_id → users
   audit_logs    ── user_id → users (append-only; no FK-cascade deletes)
```

## Tables

### Security / RBAC

| Table | Key columns | Notes |
|-------|-------------|-------|
| `roles` | `name` (unique) | 5 roles |
| `permissions` | `name` (unique) | 26 permissions |
| `users` | `username` (unique), `email` (unique), `password_hash`, `active` | Passwords stored only as BCrypt hashes |
| `user_roles` | (`user_id`,`role_id`) | Many-to-many join |
| `role_permissions` | (`role_id`,`permission_id`) | Many-to-many join |

### Core registers

| Table | Purpose | Notable columns / indexes |
|-------|---------|---------------------------|
| `staff` | Facility staff (escorts, duty officers) | `staff_number` unique; optional link to `users` |
| `prisoners` | Prisoner master record | `prisoner_number` unique; `status`; indexed on status, last_name, admission_date, expected_release_date |
| `cases` | Case register (crime/FIR/court) | `case_number` unique; FK → prisoners; indexed on crime_type, court, status |
| `surrendered_articles` | Articles taken into custody | FK → prisoners; `return_status` (HELD/RETURNED) |
| `release_records` | Planned/actual releases | `expected_release_date`, `status`, `actual_release_date`; FK → prisoners, users |
| `duty_register` | Staff duty roster | `duty_date`, `start_time`, `end_time`; FK → staff, users |
| `interview_requests` | Visitor interview lifecycle | `status`, `requested_date`; FK → prisoners, users (reviewer) |
| `movements` | In-out register | `person_type`, `movement_type`, `time_out`, `expected_return`, `actual_return`, `status`; FK → staff (escort) |
| `court_hearings` | Court-movement specialization | Optional 1:1 → `movements` (`uq_court_movement`); FK → prisoners, cases, staff; `status`, `outcome` |

### Cross-cutting

| Table | Purpose | Notes |
|-------|---------|-------|
| `notifications` | In-app alerts | `type`, `read_flag`, `recipient_user_id` (nullable = broadcast) |
| `audit_logs` | Append-only audit trail | `old_value`/`new_value` are `TEXT`; `entity_id` stored as VARCHAR; no update/delete path |

## Design notes

- **Derived states are never stored.** "Overdue" movements and "not returned" court hearings are
  computed at query time from `expected_return`/status, so the database can never hold a stale
  overdue flag.
- **Soft delete** for prisoners: `active = false` + status `INACTIVE` instead of row deletion, so
  history and audit references remain intact.
- **Referential integrity** is enforced with foreign keys throughout; nullable FKs (e.g.
  `movements.escort_officer`, `notifications.recipient_user_id`) model genuinely optional links.
- **Audit immutability**: `audit_logs` has no `updated_at` and is written only via the append-only
  `AuditService`; there are no controller endpoints that update or delete audit rows.
- **Indexing**: every register is indexed on the columns its search/report screens filter and sort
  by (status, dates, foreign keys), matching the `JpaSpecificationExecutor` queries.

## Seed data summary

`V2` + `DataInitializer` provide a working demo out of the box: 5 roles, 26 permissions with
role grants, 5 demo users, staff members, and a small set of fictional prisoners, cases,
surrendered articles, and movements so every screen and report has data to show.
