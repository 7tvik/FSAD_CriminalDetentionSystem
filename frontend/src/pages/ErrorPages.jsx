import { Link } from 'react-router-dom';

export function ForbiddenPage() {
  return (
    <div className="center-screen">
      <h1>403 — Access Denied</h1>
      <p className="text-muted">You do not have permission to view this page.</p>
      <Link className="btn btn-primary" to="/dashboard">Back to Dashboard</Link>
    </div>
  );
}

export function NotFoundPage() {
  return (
    <div className="center-screen">
      <h1>404 — Not Found</h1>
      <p className="text-muted">The page you are looking for does not exist.</p>
      <Link className="btn btn-primary" to="/dashboard">Back to Dashboard</Link>
    </div>
  );
}
