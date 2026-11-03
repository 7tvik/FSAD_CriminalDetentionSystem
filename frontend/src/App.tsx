import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute } from '@/auth/ProtectedRoute';
import { P } from '@/auth/permissions';
import { LoginPage } from '@/pages/LoginPage';
import { ForbiddenPage, NotFoundPage } from '@/pages/ErrorPages';
import { DashboardPage } from '@/pages/DashboardPage';
import { PrisonersPage } from '@/pages/PrisonersPage';
import { PrisonerDetailPage } from '@/pages/PrisonerDetailPage';
import { ReleasesPage } from '@/pages/ReleasesPage';
import { ReleaseDiaryPage } from '@/pages/ReleaseDiaryPage';
import { DutyPage } from '@/pages/DutyPage';
import { MovementsPage } from '@/pages/MovementsPage';
import { CourtPage } from '@/pages/CourtPage';
import { InterviewsPage } from '@/pages/InterviewsPage';
import { NotificationsPage } from '@/pages/NotificationsPage';
import { ReportsPage } from '@/pages/ReportsPage';
import { UsersPage } from '@/pages/UsersPage';
import { RolesPage } from '@/pages/RolesPage';
import { AuditPage } from '@/pages/AuditPage';
import { ProfilePage } from '@/pages/ProfilePage';

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />

      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<ProtectedRoute permission={P.DASHBOARD_READ}><DashboardPage /></ProtectedRoute>} />
        <Route path="/prisoners" element={<ProtectedRoute permission={P.PRISONER_READ}><PrisonersPage /></ProtectedRoute>} />
        <Route path="/prisoners/:id" element={<ProtectedRoute permission={P.PRISONER_READ}><PrisonerDetailPage /></ProtectedRoute>} />
        <Route path="/releases" element={<ProtectedRoute permission={P.RELEASE_READ}><ReleasesPage /></ProtectedRoute>} />
        <Route path="/release-diary" element={<ProtectedRoute permission={P.RELEASE_READ}><ReleaseDiaryPage /></ProtectedRoute>} />
        <Route path="/duty" element={<ProtectedRoute permission={P.DUTY_READ}><DutyPage /></ProtectedRoute>} />
        <Route path="/movements" element={<ProtectedRoute permission={P.MOVEMENT_READ}><MovementsPage /></ProtectedRoute>} />
        <Route path="/court" element={<ProtectedRoute permission={P.COURT_READ}><CourtPage /></ProtectedRoute>} />
        <Route path="/interviews" element={<ProtectedRoute permission={P.INTERVIEW_READ}><InterviewsPage /></ProtectedRoute>} />
        <Route path="/notifications" element={<ProtectedRoute permission={P.NOTIFICATION_READ}><NotificationsPage /></ProtectedRoute>} />
        <Route path="/reports" element={<ProtectedRoute permission={P.REPORT_READ}><ReportsPage /></ProtectedRoute>} />
        <Route path="/users" element={<ProtectedRoute permission={P.USER_READ}><UsersPage /></ProtectedRoute>} />
        <Route path="/roles" element={<ProtectedRoute permission={P.ROLE_READ}><RolesPage /></ProtectedRoute>} />
        <Route path="/audit" element={<ProtectedRoute permission={P.AUDIT_READ}><AuditPage /></ProtectedRoute>} />
        <Route path="/profile" element={<ProfilePage />} />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
