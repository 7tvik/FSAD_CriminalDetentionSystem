import { http } from './http';

// Generic paged query params
function clean(params) {
  if (!params) return {};
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== '' && v !== null),
  );
}

// ---------------- Auth ----------------
export const authApi = {
  login: (body) => http.post('/auth/login', body).then((r) => r.data),
  register: (body) => http.post('/auth/register', body).then((r) => r.data),
  changePassword: (body) => http.post('/auth/change-password', body).then((r) => r.data),
  logout: () => http.post('/auth/logout').then((r) => r.data),
};

// ---------------- Prisoners / Cases / Articles ----------------
export const prisonerApi = {
  search: (params) =>
    http.get('/prisoners', { params: clean(params) }).then((r) => r.data),
  get: (id) => http.get(`/prisoners/${id}`).then((r) => r.data),
  create: (body) => http.post('/prisoners', body).then((r) => r.data),
  update: (id, body) => http.put(`/prisoners/${id}`, body).then((r) => r.data),
  remove: (id) => http.delete(`/prisoners/${id}`).then((r) => r.data),
  history: (id) => http.get(`/prisoners/${id}/history`).then((r) => r.data),
  cases: (id) => http.get(`/prisoners/${id}/cases`).then((r) => r.data),
  addCase: (id, body) => http.post(`/prisoners/${id}/cases`, body).then((r) => r.data),
  articles: (id) => http.get(`/prisoners/${id}/articles`).then((r) => r.data),
  addArticle: (id, body) => http.post(`/prisoners/${id}/articles`, body).then((r) => r.data),
};

export const caseApi = {
  search: (params) =>
    http.get('/cases', { params: clean(params) }).then((r) => r.data),
  get: (id) => http.get(`/cases/${id}`).then((r) => r.data),
  update: (id, body) => http.put(`/cases/${id}`, body).then((r) => r.data),
};

export const articleApi = {
  update: (id, body) => http.put(`/articles/${id}`, body).then((r) => r.data),
  markReturned: (id) => http.put(`/articles/${id}/return`).then((r) => r.data),
};

// ---------------- Releases / Diary / Reports ----------------
export const releaseApi = {
  search: (params) =>
    http.get('/releases', { params: clean(params) }).then((r) => r.data),
  get: (id) => http.get(`/releases/${id}`).then((r) => r.data),
  byPrisoner: (prisonerId) =>
    http.get(`/releases/prisoners/${prisonerId}`).then((r) => r.data),
  create: (body) => http.post('/releases', body).then((r) => r.data),
  update: (id, body) => http.put(`/releases/${id}`, body).then((r) => r.data),
  updateStatus: (id, body) => http.put(`/releases/${id}/status`, body).then((r) => r.data),
  diary: (params) =>
    http.get('/releases/diary', { params: clean(params) }).then((r) => r.data),
  diaryExportUrl: '/releases/diary/export',
};

export const reportApi = {
  dailyReleases: (date) =>
    http.get('/reports/releases/daily', { params: { date } }).then((r) => r.data.releases),
  monthlyReleases: (year, month) =>
    http.get('/reports/releases/monthly', { params: { year, month } }).then((r) => r.data.releases),
  upcomingReleases: (days) =>
    http.get('/reports/releases/upcoming', { params: { days } }).then((r) => r.data.releases),
  prisonerStats: () =>
    http.get('/reports/statistics/prisoners').then((r) => r.data),
  caseStats: () => http.get('/reports/statistics/cases').then((r) => r.data),
};

// ---------------- Duty ----------------
export const dutyApi = {
  search: (params) =>
    http.get('/duties', { params: clean(params) }).then((r) => r.data),
  get: (id) => http.get(`/duties/${id}`).then((r) => r.data),
  create: (body) => http.post('/duties', body).then((r) => r.data),
  update: (id, body) => http.put(`/duties/${id}`, body).then((r) => r.data),
  remove: (id) => http.delete(`/duties/${id}`).then((r) => r.data),
};

// ---------------- Movements / Court ----------------
export const movementApi = {
  search: (params) =>
    http.get('/movements', { params: clean(params) }).then((r) => r.data),
  get: (id) => http.get(`/movements/${id}`).then((r) => r.data),
  create: (body) => http.post('/movements', body).then((r) => r.data),
  update: (id, body) => http.put(`/movements/${id}`, body).then((r) => r.data),
  markReturned: (id, body) =>
    http.put(`/movements/${id}/return`, body ?? {}).then((r) => r.data),
  cancel: (id) => http.put(`/movements/${id}/cancel`).then((r) => r.data),
  overdue: () => http.get('/movements/overdue').then((r) => r.data),
};

export const courtApi = {
  search: (params) =>
    http.get('/court-movements', { params: clean(params) }).then((r) => r.data),
  get: (id) => http.get(`/court-movements/${id}`).then((r) => r.data),
  create: (body) => http.post('/court-movements', body).then((r) => r.data),
  update: (id, body) => http.put(`/court-movements/${id}`, body).then((r) => r.data),
  updateStatus: (id, body) =>
    http.put(`/court-movements/${id}/status`, body).then((r) => r.data),
  notReturned: () => http.get('/court-movements/not-returned').then((r) => r.data),
};

// ---------------- Interviews ----------------
export const interviewApi = {
  search: (params) =>
    http.get('/interviews', { params: clean(params) }).then((r) => r.data),
  get: (id) => http.get(`/interviews/${id}`).then((r) => r.data),
  create: (body) => http.post('/interviews', body).then((r) => r.data),
  approve: (id, body) =>
    http.put(`/interviews/${id}/approve`, body ?? {}).then((r) => r.data),
  reject: (id, body) =>
    http.put(`/interviews/${id}/reject`, body ?? {}).then((r) => r.data),
  schedule: (id, body) =>
    http.put(`/interviews/${id}/schedule`, body ?? {}).then((r) => r.data),
  complete: (id, body) =>
    http.put(`/interviews/${id}/complete`, body ?? {}).then((r) => r.data),
  cancel: (id, body) =>
    http.put(`/interviews/${id}/cancel`, body ?? {}).then((r) => r.data),
};

// ---------------- Notifications ----------------
export const notificationApi = {
  list: (params) =>
    http.get('/notifications', { params: clean(params) }).then((r) => r.data),
  markRead: (id) => http.put(`/notifications/${id}/read`).then((r) => r.data),
  generate: () => http.post('/notifications/generate').then((r) => r.data),
};

// ---------------- Dashboard ----------------
export const dashboardApi = {
  get: () => http.get('/dashboard').then((r) => r.data),
};

// ---------------- Users / Roles / Staff / Audit ----------------
export const userApi = {
  list: (params) =>
    http.get('/users', { params: clean(params) }).then((r) => r.data),
  get: (id) => http.get(`/users/${id}`).then((r) => r.data),
  me: () => http.get('/users/me').then((r) => r.data),
  create: (body) => http.post('/users', body).then((r) => r.data),
  update: (id, body) => http.put(`/users/${id}`, body).then((r) => r.data),
  activate: (id) => http.put(`/users/${id}/activate`).then((r) => r.data),
  deactivate: (id) => http.put(`/users/${id}/deactivate`).then((r) => r.data),
};

export const roleApi = {
  list: () => http.get('/roles').then((r) => r.data),
  permissions: () => http.get('/roles/permissions').then((r) => r.data),
};

export const staffApi = {
  list: () => http.get('/staff').then((r) => r.data),
};

export const auditApi = {
  search: (params) =>
    http.get('/audit-logs', { params: clean(params) }).then((r) => r.data),
};
