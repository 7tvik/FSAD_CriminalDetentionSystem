# Frontend Page Contract — KT_CDP_019

The React + TypeScript + Vite **foundation is complete and verified against the running backend**.
Page-building agents must ONLY create new page files in `src/pages/` and (if needed) small
components in `src/components/`. Do NOT modify the foundation files listed below, the types,
the api layer, the auth layer, or the styles. Follow these patterns exactly so all pages look
and behave consistently.

Frontend dir: `/Users/I578448/Documents/personal projects/criminal/frontend`
Stack: React 18, TypeScript (strict), Vite, react-router-dom v6, axios, recharts. Path alias `@/` → `src/`.

## Verified backend base URL & envelope
- Axios instance already handles JWT + errors. Use the typed API modules; never call axios directly.
- Paged endpoints return `PageResponse<T>` = `{ content: T[]; page; size; totalElements; totalPages; first; last }`.
- Query params for paged endpoints: `page` (0-based), `size`, `sort` (e.g. `"createdAt,desc"`), plus per-endpoint filters.

## API modules — `src/api/endpoints.ts` (import what you need)
- `authApi`: login, register, changePassword, logout
- `prisonerApi`: search(params), get(id), create(body), update(id,body), remove(id), history(id), cases(id), addCase(id,body), articles(id), addArticle(id,body)
- `caseApi`: search(params), get(id), update(id,body)
- `articleApi`: update(id,body), markReturned(id)
- `releaseApi`: search(params), get(id), byPrisoner(pid), create(body), update(id,body), updateStatus(id,body), diary(params), diaryExportUrl
- `reportApi`: dailyReleases(date), monthlyReleases(year,month), upcomingReleases(days), prisonerStats(), caseStats()
- `dutyApi`: search(params), get(id), create(body), update(id,body), remove(id)
- `movementApi`: search(params), get(id), create(body), update(id,body), markReturned(id,body?), cancel(id), overdue()
- `courtApi`: search(params), get(id), create(body), update(id,body), updateStatus(id,body), notReturned()
- `interviewApi`: search(params), get(id), create(body), approve(id,body?), reject(id,body?), schedule(id,body?), complete(id,body?), cancel(id,body?)
- `notificationApi`: list(params), markRead(id), generate()
- `dashboardApi`: get()
- `userApi`: list(params), get(id), me(), create(body), update(id,body), activate(id), deactivate(id)
- `roleApi`: list(), permissions()
- `staffApi`: list()
- `auditApi`: search(params)
- `toErrorMessage(err)` from `@/api/http` — normalize any error to a string.

## Types — `src/types/index.ts` (import types you need)
All domain interfaces exist: Prisoner, CaseRecord, SurrenderedArticle, ReleaseRecord, DutyRecord,
Movement, CourtHearing, InterviewRequest, Notification, DashboardStats, UserAccount, RoleInfo,
StaffMember, AuditLogEntry, StatItem, PageResponse<T>, and string-union enums (PrisonerStatus,
CaseStatus, ReleaseType, ReleaseStatus, InterviewStatus, MovementType, MovementStatus,
CourtHearingStatus, ArticleReturnStatus, NotificationType, PersonType).

### Enum value arrays for dropdowns (define locally in your page as needed; these are the exact values):
- PrisonerStatus: ACTIVE, RELEASED, TRANSFERRED, DECEASED, INACTIVE
- CaseStatus: UNDER_INVESTIGATION, UNDER_TRIAL, CONVICTED, ACQUITTED, CLOSED
- ArticleReturnStatus: HELD, RETURNED
- ReleaseType: BAIL, SENTENCE_COMPLETE, ACQUITTAL, PAROLE, OTHER
- ReleaseStatus: UPCOMING, READY, RELEASED, CANCELLED, ON_HOLD
- InterviewStatus: PENDING, APPROVED, REJECTED, SCHEDULED, COMPLETED, CANCELLED
- MovementType: COURT, MEDICAL, TRANSFER, AUTHORIZED_VISIT, OTHER
- MovementStatus: SCHEDULED, OUT, RETURNED, CANCELLED
- CourtHearingStatus: SCHEDULED, DEPARTED, AT_COURT, HEARING_COMPLETED, DEPARTED_COURT, RETURNED, CANCELLED

## Verified state machines (only show valid action buttons for the current status)
- Release status: UPCOMING→{READY,ON_HOLD,CANCELLED}; READY→{RELEASED,ON_HOLD,CANCELLED}; ON_HOLD→{UPCOMING,READY,CANCELLED}; RELEASED & CANCELLED are terminal. Update via `releaseApi.updateStatus(id, { status, actualReleaseDate?, remarks? })`.
- Interview: PENDING→approve/reject/cancel; APPROVED→schedule/cancel; SCHEDULED→complete/cancel; REJECTED/COMPLETED/CANCELLED terminal. Use interviewApi.approve/reject/schedule/complete/cancel (optional body `{ remarks?, requestedDate?, requestedTime? }`).
- Movement: markReturned only when status==OUT (body optional `{ actualReturn? }`); cancel when SCHEDULED|OUT. Create with `timeOut` set → starts OUT, else SCHEDULED. `overdue` boolean is on the response.
- Court: forward-only SCHEDULED→DEPARTED→AT_COURT→HEARING_COMPLETED→DEPARTED_COURT→RETURNED, plus SCHEDULED→CANCELLED. Advance via `courtApi.updateStatus(id, { status, outcome?, remarks? })`. `notReturned` boolean on response.

## Auth & permissions
- `useAuth()` from `@/auth/AuthContext` → `{ user, hasPermission, hasAnyPermission, hasRole, logout }`.
- Permission constants: `import { P } from '@/auth/permissions'` (e.g. `P.PRISONER_WRITE`).
- Gate write UI: `{hasPermission(P.PRISONER_WRITE) && <button>…</button>}`. The backend enforces auth regardless; hiding buttons is UX only.

## Reusable components & helpers (USE THESE — do not reinvent)
- `@/components/layout/PageHeader` → `<PageHeader title subtitle? actions? />`
- `@/components/ui/Primitives` → `Card`, `StatusBadge` (`<StatusBadge status={x} />`), `Badge`, `Spinner`, `LoadingBlock`, `EmptyState`, `ErrorAlert`, `SuccessAlert`, `Detail`
- `@/components/ui/Modal` → `<Modal open title onClose footer? wide?>…</Modal>`
- `@/components/ui/Pagination` → `<Pagination page={pageResponse} onPageChange={(p)=>…} />`
- `@/components/ui/Field` → `<Field label required? error? hint? full?>…</Field>`, `<EnumSelect value onChange options includeBlank? blankLabel? />`
- `@/hooks/useAsync` → `const { data, loading, error, reload } = useAsync(() => api.call(args), [deps])`
- `@/utils/format` → `formatDate`, `formatDateTime`, `formatTime`, `humanize`, `fullName(first,last)`, `todayISO()`, `statusVariant(status)`

## CSS classes available (see src/styles/components.css) — use className, no CSS-in-JS libraries
- Buttons: `btn btn-primary|btn-secondary|btn-danger|btn-ghost`, `btn-sm`, `btn-block`
- `card`, `card-header`, `card-title`, `card-body`
- `table` (wrap in `<div className="table-wrap">`), `table .actions`
- `stat-grid`, `stat-card`, `.stat-label`, `.stat-value`, accent modifiers `accent-warning|accent-danger|accent-success`
- `toolbar` + `.spacer` for filter bars above tables
- `badge badge-success|warning|danger|info|primary|neutral`
- `form-grid`, `field`, `input`, `select`, `textarea`, `alert alert-error|success|info`
- `detail-grid`, `tabs`/`tab`/`tab.active`, `stack`, `stack-sm`, `row`, `row-wrap`, `text-muted`, `mono`, `text-right`

## Standard list-page pattern (follow this shape)
```tsx
export function XPage() {
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({ q: '', status: '' });
  const { data, loading, error, reload } = useAsync(
    () => xApi.search({ page, size: 10, sort: 'createdAt,desc', ...cleanedFilters }),
    [page, filters],
  );
  // toolbar with filter inputs + (permission-gated) "New" button opening a <Modal> form
  // <div className="table-wrap"><table className="table">…rows…</table></div>
  // <Pagination page={data} onPageChange={setPage} />
  // create/edit via Modal; on submit call api then reload(); show ErrorAlert on catch
}
```

## Verify before reporting
Run `cd /Users/I578448/Documents/personal projects/criminal/frontend && npx tsc --noEmit` and fix all
type errors in YOUR files. (Dependencies are installed.) Report files created and confirm typecheck clean.
Do NOT run the dev server. Do NOT touch backend.
