import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';

/** Gate a route on authentication and (optionally) a required permission. */
export function ProtectedRoute({ children, permission }) {
  const { user, hasPermission } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (permission && !hasPermission(permission)) {
    return <Navigate to="/forbidden" replace />;
  }
  return <>{children}</>;
}
