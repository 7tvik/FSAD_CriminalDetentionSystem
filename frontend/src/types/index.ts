// ============================================================================
// Shared API types — mirror the backend DTOs (com.kt.cdp.dto.*).
// Kept deliberately close to the Java records so the client stays honest.
// ============================================================================

// ---------- Envelope ----------
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  timestamp?: string;
  status: number;
  error?: string;
  message: string;
  path?: string;
  fieldErrors?: { field: string; message: string }[];
}

// ---------- Auth ----------
export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  fullName: string;
  roles: string[];
  permissions: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

// ---------- Enums (string unions matching backend) ----------
export type PrisonerStatus = 'ACTIVE' | 'RELEASED' | 'TRANSFERRED' | 'DECEASED' | 'INACTIVE';
export type CaseStatus = 'UNDER_INVESTIGATION' | 'UNDER_TRIAL' | 'CONVICTED' | 'ACQUITTED' | 'CLOSED';
export type ArticleReturnStatus = 'HELD' | 'RETURNED';
export type ReleaseType = 'BAIL' | 'SENTENCE_COMPLETE' | 'ACQUITTAL' | 'PAROLE' | 'OTHER';
export type ReleaseStatus = 'UPCOMING' | 'READY' | 'RELEASED' | 'CANCELLED' | 'ON_HOLD';
export type InterviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
export type PersonType = 'PRISONER' | 'STAFF' | 'OTHER';
export type MovementType = 'COURT' | 'MEDICAL' | 'TRANSFER' | 'AUTHORIZED_VISIT' | 'OTHER';
export type MovementStatus = 'SCHEDULED' | 'OUT' | 'RETURNED' | 'CANCELLED';
export type CourtHearingStatus =
  | 'SCHEDULED' | 'DEPARTED' | 'AT_COURT' | 'HEARING_COMPLETED'
  | 'DEPARTED_COURT' | 'RETURNED' | 'CANCELLED';
export type NotificationType =
  | 'RELEASE_UPCOMING' | 'MOVEMENT_OVERDUE' | 'INTERVIEW_PENDING' | 'COURT_HEARING' | 'GENERAL';

// ---------- Prisoner / Case / Article ----------
export interface Prisoner {
  id: number;
  prisonerNumber: string;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: string;
  address?: string;
  emergencyContact?: string;
  idType?: string;
  idNumber?: string;
  admissionDate: string;
  status: PrisonerStatus;
  facility?: string;
  ward?: string;
  cell?: string;
  expectedReleaseDate?: string;
  photoUrl?: string;
  active: boolean;
}

export interface CaseRecord {
  id: number;
  prisonerId: number;
  prisonerName?: string;
  prisonerNumber?: string;
  caseNumber: string;
  firNumber?: string;
  crimeType: string;
  crimeDescription?: string;
  policeStation?: string;
  court?: string;
  sections?: string;
  arrestDate?: string;
  status: CaseStatus;
  investigatingOfficer?: string;
  remarks?: string;
  createdAt?: string;
}

export interface SurrenderedArticle {
  id: number;
  prisonerId: number;
  prisonerName?: string;
  prisonerNumber?: string;
  name: string;
  category?: string;
  quantity: number;
  description?: string;
  conditionNote?: string;
  receivedDate?: string;
  returnedDate?: string;
  returnStatus: ArticleReturnStatus;
  remarks?: string;
  createdAt?: string;
}

// ---------- Release / Reports ----------
export interface ReleaseRecord {
  id: number;
  prisonerId: number;
  prisonerNumber: string;
  prisonerName: string;
  expectedReleaseDate: string;
  releaseType: ReleaseType;
  reason?: string;
  status: ReleaseStatus;
  remarks?: string;
  authorizedByUsername?: string;
  actualReleaseDate?: string;
  createdAt?: string;
}

export interface StatItem {
  label: string;
  value: number;
}

// ---------- Duty ----------
export interface DutyRecord {
  id: number;
  staffId: number;
  staffName?: string;
  staffNumber?: string;
  location: string;
  purpose?: string;
  dutyDate: string;
  startTime: string;
  endTime: string;
  remarks?: string;
  createdByUsername?: string;
  createdAt?: string;
}

// ---------- Movement / Court ----------
export interface Movement {
  id: number;
  personType: PersonType;
  personRefId?: number;
  personName: string;
  movementType: MovementType;
  destination: string;
  reason?: string;
  movementDate: string;
  timeOut?: string;
  expectedReturn?: string;
  actualReturn?: string;
  escortOfficerId?: number;
  escortOfficerName?: string;
  status: MovementStatus;
  overdue?: boolean;
  remarks?: string;
}

export interface CourtHearing {
  id: number;
  movementId?: number;
  prisonerId?: number;
  prisonerName?: string;
  caseId?: number;
  caseNumber?: string;
  court: string;
  hearingDate: string;
  destination?: string;
  departureTime?: string;
  expectedReturn?: string;
  actualReturn?: string;
  escortOfficerName?: string;
  status: CourtHearingStatus;
  notReturned?: boolean;
  outcome?: string;
  remarks?: string;
}

// ---------- Interview ----------
export interface InterviewRequest {
  id: number;
  prisonerId: number;
  prisonerName?: string;
  prisonerNumber?: string;
  visitorName: string;
  relationship?: string;
  contact?: string;
  idDetails?: string;
  requestedDate: string;
  requestedTime?: string;
  purpose?: string;
  status: InterviewStatus;
  reviewedByUsername?: string;
  reviewDate?: string;
  remarks?: string;
}

// ---------- Notification ----------
export interface Notification {
  id: number;
  recipientUserId?: number;
  type: NotificationType;
  title: string;
  message: string;
  entityType?: string;
  entityId?: number;
  readFlag: boolean;
  createdAt: string;
}

// ---------- Dashboard ----------
export interface DashboardStats {
  totalPrisoners: number;
  activePrisoners: number;
  todaysReleases: number;
  thisWeekReleases: number;
  thisMonthReleases: number;
  upcomingReleases: number;
  pendingInterviews: number;
  todaysCourtMovements: number;
  currentlyOut: number;
  overdueMovements: number;
  activeCases: number;
}

// ---------- User / Role / Staff / Audit ----------
export interface UserAccount {
  id: number;
  username: string;
  email: string;
  fullName: string;
  active: boolean;
  roles: string[];
}

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  roles: string[];
}

export interface UpdateUserRequest {
  email?: string;
  fullName?: string;
  roles?: string[];
}

export interface RoleInfo {
  id: number;
  name: string;
  description?: string;
  permissions: string[];
}

export interface StaffMember {
  id: number;
  staffNumber: string;
  fullName: string;
  rankTitle?: string;
  contact?: string;
  active: boolean;
}

export interface AuditLogEntry {
  id: number;
  userId?: number;
  usernameSnapshot?: string;
  action: string;
  entityType?: string;
  entityId?: number;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  createdAt: string;
}
