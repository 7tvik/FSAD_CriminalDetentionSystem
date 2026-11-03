import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';

export function Topbar({ onToggleSidebar }: { onToggleSidebar: () => void }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <header className="topbar">
      <div className="row">
        <button
          className="btn btn-ghost btn-sm"
          onClick={onToggleSidebar}
          aria-label="Toggle menu"
          style={{ fontSize: '1.2rem' }}
        >
          ☰
        </button>
      </div>
      <div className="topbar-right">
        <div className="user-chip">
          <span className="name">{user?.fullName ?? user?.username}</span>
          <span className="role">{user?.roles?.join(', ')}</span>
        </div>
        <div style={{ position: 'relative' }}>
          <button className="btn btn-secondary btn-sm" onClick={() => setMenuOpen((v) => !v)}>
            Account ▾
          </button>
          {menuOpen && (
            <div
              className="card"
              style={{ position: 'absolute', right: 0, top: '110%', width: 180, zIndex: 20 }}
              onMouseLeave={() => setMenuOpen(false)}
            >
              <div style={{ padding: 8 }} className="stack-sm">
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => {
                    setMenuOpen(false);
                    navigate('/profile');
                  }}
                >
                  My Profile
                </button>
                <button className="btn btn-danger btn-sm" onClick={handleLogout}>
                  Sign out
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
