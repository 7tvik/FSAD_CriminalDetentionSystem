import { useState, type FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';
import { toErrorMessage } from '@/api/http';
import { ErrorAlert, Spinner } from '@/components/ui/Primitives';

const DEMO_ACCOUNTS = [
  ['admin', 'Admin@123', 'Administrator (full access)'],
  ['officer', 'Officer@123', 'Prison Officer'],
  ['police', 'Police@123', 'Police Official'],
  ['govt', 'Govt@123', 'Government Official (read-only)'],
  ['auditor', 'Auditor@123', 'Auditor'],
];

export function LoginPage() {
  const { user, login, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);

  const from = (location.state as { from?: string } | null)?.from || '/dashboard';

  if (user) return <Navigate to={from} replace />;

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await login(username.trim(), password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(toErrorMessage(err) || 'Invalid credentials');
    }
  };

  const quickFill = (u: string, p: string) => {
    setUsername(u);
    setPassword(p);
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="brand">
          <h1>Criminal Detention Portal</h1>
          <p>Prison Administration System — sign in to continue</p>
        </div>
        <form onSubmit={submit} className="stack">
          <ErrorAlert message={error} />
          <div className="field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              className="input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoFocus
              autoComplete="username"
              required
            />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              className="input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </div>
          <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
            {loading ? <Spinner /> : 'Sign in'}
          </button>
        </form>
        <div className="demo-creds">
          <strong>Demo accounts</strong> (click to fill):
          <div className="stack-sm mt-4">
            {DEMO_ACCOUNTS.map(([u, p, desc]) => (
              <button
                key={u}
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => quickFill(u, p)}
                style={{ justifyContent: 'flex-start' }}
              >
                <code>{u}</code> — {desc}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
