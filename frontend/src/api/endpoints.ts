import { http } from './http';
import type {
  AuthResponse, LoginRequest, RegisterRequest, ChangePasswordRequest,
  PageResponse, Prisoner, CaseRecord, SurrenderedArticle, ReleaseRecord,
  DutyRecord, Movement, CourtHearing, InterviewRequest, Notification,
  DashboardStats, UserAccount, CreateUserRequest, UpdateUserRequest,
  RoleInfo, StaffMember, AuditLogEntry, StatItem,
} from '@/types';

// Generic paged query params
export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
  [key: string]: string | number | boolean | undefined;
}

function clean(params?: PageParams): Record<string, unknown> {
  if (!params) return {};
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== '' && v !== null),
  );
}

// ---------------- Auth ----------------
export const authApi = {
  login: (body: LoginRequest) => http.post<AuthResponse>('/auth/login', body).then((r) => r.data),
  register: (body: RegisterRequest) =>
    http.post<AuthResponse>('/auth/register', body).then((r) => r.data),
  changePassword: (body: ChangePasswordRequest) =>
    http.post<void>('/auth/change-password', body).then((r) => r.data),
  logout: () => http.post<void>('/auth/logout').then((r) => r.data),
};

// ---------------- Prisoners / Cases / Articles ----------------
export const prisonerApi = {
  search: (params?: PageParams) =>
    http.get<PageResponse<Prisoner>>('/prisoners', { params: clean(params) }).then((r) => r.data),
  get: (id: number) => http.get<Prisoner>(`/prisoners/${id}`).then((r) => r.data),
  create: (body: Partial<Prisoner>) => http.post<Prisoner>('/prisoners', body).then((r) => r.data),
  update: (id: number, body: Partial<Prisoner>) =>
    http.put<Prisoner>(`/prisoners/${id}`, body).then((r) => r.data),
  remove: (id: number) => http.delete<void>(`/prisoners/${id}`).then((r) => r.data),
  history: (id: number) => http.get<unknown>(`/prisoners/${id}/history`).then((r) => r.data),
  cases: (id: number) => http.get<CaseRecord[]>(`/prisoners/${id}/cases`).then((r) => r.data),
  addCase: (id: number, body: Partial<CaseRecord>) =>
    http.post<CaseRecord>(`/prisoners/${id}/cases`, body).then((r) => r.data),
  articles: (id: number) =>
    http.get<SurrenderedArticle[]>(`/prisoners/${id}/articles`).then((r) => r.data),
  addArticle: (id: number, body: Partial<SurrenderedArticle>) =>
    http.post<SurrenderedArticle>(`/prisoners/${id}/articles`, body).then((r) => r.data),
};

export const caseApi = {
  search: (params?: PageParams) =>
    http.get<PageResponse<CaseRecord>>('/cases', { params: clean(params) }).then((r) => r.data),
  get: (id: number) => http.get<CaseRecord>(`/cases/${id}`).then((r) => r.data),
  update: (id: number, body: Partial<CaseRecord>) =>
    http.put<CaseRecord>(`/cases/${id}`, body).then((r) => r.data),
};

export const articleApi = {
  update: (id: number, body: Partial<SurrenderedArticle>) =>
    http.put<SurrenderedArticle>(`/articles/${id}`, body).then((r) => r.data),
  markReturned: (id: number) =>
    http.put<SurrenderedArticle>(`/articles/${id}/return`).then((r) => r.data),
};

// ---------------- Releases / Diary / Reports ----------------
export const releaseApi = {
  search: (params?: PageParams) =>
    http.get<PageResponse<ReleaseRecord>>('/releases', { params: clean(params) }).then((r) => r.data),
  get: (id: number) => http.get<ReleaseRecord>(`/releases/${id}`).then((r) => r.data),
  byPrisoner: (prisonerId: number) =>
    http.get<ReleaseRecord[]>(`/releases/prisoners/${prisonerId}`).then((r) => r.data),
  create: (body: Record<string, unknown>) =>
    http.post<ReleaseRecord>('/releases', body).then((r) => r.data),
  update: (id: number, body: Record<string, unknown>) =>
    http.put<ReleaseRecord>(`/releases/${id}`, body).then((r) => r.data),
  updateStatus: (id: number, body: Record<string, unknown>) =>
    http.put<ReleaseRecord>(`/releases/${id}/status`, body).then((r) => r.data),
  diary: (params?: PageParams) =>
    http.get<PageResponse<ReleaseRecord>>('/releases/diary', { params: clean(params) }).then((r) => r.data),
  diaryExportUrl: '/releases/diary/export',
};

export const reportApi = {
  dailyReleases: (date: string) =>
    http.get<ReleaseRecord[]>('/reports/releases/daily', { params: { date } }).then((r) => r.data),
  monthlyReleases: (year: number, month: number) =>
    http.get<ReleaseRecord[]>('/reports/releases/monthly', { params: { year, month } }).then((r) => r.data),
  upcomingReleases: (days: number) =>
    http.get<ReleaseRecord[]>('/reports/releases/upcoming', { params: { days } }).then((r) => r.data),
  prisonerStats: () =>
    http.get<StatItem[]>('/reports/statistics/prisoners').then((r) => r.data),
  caseStats: () => http.get<StatItem[]>('/reports/statistics/cases').then((r) => r.data),
};

// ---------------- Duty ----------------
export const dutyApi = {
  search: (params?: PageParams) =>
    http.get<PageResponse<DutyRecord>>('/duties', { params: clean(params) }).then((r) => r.data),
  get: (id: number) => http.get<DutyRecord>(`/duties/${id}`).then((r) => r.data),
  create: (body: Partial<DutyRecord>) => http.post<DutyRecord>('/duties', body).then((r) => r.data),
  update: (id: number, body: Partial<DutyRecord>) =>
    http.put<DutyRecord>(`/duties/${id}`, body).then((r) => r.data),
  remove: (id: number) => http.delete<void>(`/duties/${id}`).then((r) => r.data),
};

// ---------------- Movements / Court ----------------
export const movementApi = {
  search: (params?: PageParams) =>
    http.get<PageResponse<Movement>>('/movements', { params: clean(params) }).then((r) => r.data),
  get: (id: number) => http.get<Movement>(`/movements/${id}`).then((r) => r.data),
  create: (body: Partial<Movement>) => http.post<Movement>('/movements', body).then((r) => r.data),
  update: (id: number, body: Partial<Movement>) =>
    http.put<Movement>(`/movements/${id}`, body).then((r) => r.data),
  markReturned: (id: number, body?: Record<string, unknown>) =>
    http.put<Movement>(`/movements/${id}/return`, body ?? {}).then((r) => r.data),
  cancel: (id: number) => http.put<Movement>(`/movements/${id}/cancel`).then((r) => r.data),
  overdue: () => http.get<Movement[]>('/movements/overdue').then((r) => r.data),
};

export const courtApi = {
  search: (params?: PageParams) =>
    http.get<PageResponse<CourtHearing>>('/court-movements', { params: clean(params) }).then((r) => r.data),
  get: (id: number) => http.get<CourtHearing>(`/court-movements/${id}`).then((r) => r.data),
  create: (body: Partial<CourtHearing>) =>
    http.post<CourtHearing>('/court-movements', body).then((r) => r.data),
  update: (id: number, body: Partial<CourtHearing>) =>
    http.put<CourtHearing>(`/court-movements/${id}`, body).then((r) => r.data),
  updateStatus: (id: number, body: Record<string, unknown>) =>
    http.put<CourtHearing>(`/court-movements/${id}/status`, body).then((r) => r.data),
  notReturned: () => http.get<CourtHearing[]>('/court-movements/not-returned').then((r) => r.data),
};

// ---------------- Interviews ----------------
export const interviewApi = {
  search: (params?: PageParams) =>
    http.get<PageResponse<InterviewRequest>>('/interviews', { params: clean(params) }).then((r) => r.data),
  get: (id: number) => http.get<InterviewRequest>(`/interviews/${id}`).then((r) => r.data),
  create: (body: Partial<InterviewRequest>) =>
    http.post<InterviewRequest>('/interviews', body).then((r) => r.data),
  approve: (id: number, body?: Record<string, unknown>) =>
    http.put<InterviewRequest>(`/interviews/${id}/approve`, body ?? {}).then((r) => r.data),
  reject: (id: number, body?: Record<string, unknown>) =>
    http.put<InterviewRequest>(`/interviews/${id}/reject`, body ?? {}).then((r) => r.data),
  schedule: (id: number, body?: Record<string, unknown>) =>
    http.put<InterviewRequest>(`/interviews/${id}/schedule`, body ?? {}).then((r) => r.data),
  complete: (id: number, body?: Record<string, unknown>) =>
    http.put<InterviewRequest>(`/interviews/${id}/complete`, body ?? {}).then((r) => r.data),
  cancel: (id: number, body?: Record<string, unknown>) =>
    http.put<InterviewRequest>(`/interviews/${id}/cancel`, body ?? {}).then((r) => r.data),
};

// ---------------- Notifications ----------------
export const notificationApi = {
  list: (params?: PageParams) =>
    http.get<PageResponse<Notification>>('/notifications', { params: clean(params) }).then((r) => r.data),
  markRead: (id: number) => http.put<void>(`/notifications/${id}/read`).then((r) => r.data),
  generate: () => http.post<{ created: number }>('/notifications/generate').then((r) => r.data),
};

// ---------------- Dashboard ----------------
export const dashboardApi = {
  get: () => http.get<DashboardStats>('/dashboard').then((r) => r.data),
};

// ---------------- Users / Roles / Staff / Audit ----------------
export const userApi = {
  list: (params?: PageParams) =>
    http.get<PageResponse<UserAccount>>('/users', { params: clean(params) }).then((r) => r.data),
  get: (id: number) => http.get<UserAccount>(`/users/${id}`).then((r) => r.data),
  me: () => http.get<UserAccount>('/users/me').then((r) => r.data),
  create: (body: CreateUserRequest) => http.post<UserAccount>('/users', body).then((r) => r.data),
  update: (id: number, body: UpdateUserRequest) =>
    http.put<UserAccount>(`/users/${id}`, body).then((r) => r.data),
  activate: (id: number) => http.put<UserAccount>(`/users/${id}/activate`).then((r) => r.data),
  deactivate: (id: number) => http.put<UserAccount>(`/users/${id}/deactivate`).then((r) => r.data),
};

export const roleApi = {
  list: () => http.get<RoleInfo[]>('/roles').then((r) => r.data),
  permissions: () => http.get<string[]>('/roles/permissions').then((r) => r.data),
};

export const staffApi = {
  list: () => http.get<StaffMember[]>('/staff').then((r) => r.data),
};

export const auditApi = {
  search: (params?: PageParams) =>
    http.get<PageResponse<AuditLogEntry>>('/audit-logs', { params: clean(params) }).then((r) => r.data),
};
