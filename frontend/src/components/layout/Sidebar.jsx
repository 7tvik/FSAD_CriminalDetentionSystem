import { NavLink } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';
import { P } from '@/auth/permissions';

const GROUPS = [
  {
    heading: 'Overview',
    items: [
      { to: '/dashboard', label: 'Dashboard', icon: '▤', perm: P.DASHBOARD_READ },
      { to: '/notifications', label: 'Notifications', icon: '◔', perm: P.NOTIFICATION_READ },
    ],
  },
  {
    heading: 'Registers',
    items: [
      { to: '/prisoners', label: 'Case Register', icon: '⚖', perm: P.PRISONER_READ },
      { to: '/releases', label: 'Releases', icon: '⇥', perm: P.RELEASE_READ },
      { to: '/release-diary', label: 'Release Diary', icon: '▦', perm: P.RELEASE_READ },
      { to: '/duty', label: 'Duty Register', icon: '☰', perm: P.DUTY_READ },
      { to: '/movements', label: 'In-Out Register', icon: '⇄', perm: P.MOVEMENT_READ },
      { to: '/court', label: 'Court Movements', icon: '§', perm: P.COURT_READ },
      { to: '/interviews', label: 'Interview Requests', icon: '☏', perm: P.INTERVIEW_READ },
    ],
  },
  {
    heading: 'Reports',
    items: [{ to: '/reports', label: 'Reports', icon: '▥', perm: P.REPORT_READ }],
  },
  {
    heading: 'Administration',
    items: [
      { to: '/users', label: 'Users', icon: '◕', perm: P.USER_READ },
      { to: '/roles', label: 'Roles & Permissions', icon: '⚿', perm: P.ROLE_READ },
      { to: '/audit', label: 'Audit Log', icon: '❖', perm: P.AUDIT_READ },
    ],
  },
];

export function Sidebar({ open }) {
  const { hasPermission } = useAuth();
  return (
    <aside className={`sidebar ${open ? 'open' : ''}`}>
      <div className="sidebar-brand">
        Criminal Detention Portal
        <small>Prison Administration System</small>
      </div>
      {GROUPS.map((group) => {
        const visible = group.items.filter((i) => !i.perm || hasPermission(i.perm));
        if (visible.length === 0) return null;
        return (
          <nav className="nav-section" key={group.heading}>
            <div className="nav-heading">{group.heading}</div>
            {visible.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                <span className="nav-icon" aria-hidden>{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </nav>
        );
      })}
    </aside>
  );
}
