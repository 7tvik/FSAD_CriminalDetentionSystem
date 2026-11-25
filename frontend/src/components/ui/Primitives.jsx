import { statusVariant, humanize } from '@/utils/format';

// ---------- Badge ----------
export function Badge({ children, variant = 'badge-neutral' }) {
  return <span className={`badge ${variant}`}>{children}</span>;
}

export function StatusBadge({ status }) {
  if (!status) return <span className="text-muted">—</span>;
  return <span className={`badge ${statusVariant(status)}`}>{humanize(status)}</span>;
}

// ---------- Spinner / loading / empty ----------
export function Spinner() {
  return <span className="spinner" aria-label="Loading" />;
}

export function LoadingBlock({ label = 'Loading…' }) {
  return (
    <div className="empty-state">
      <Spinner />
      <div className="mt-4">{label}</div>
    </div>
  );
}

export function EmptyState({ message = 'No records found.' }) {
  return <div className="empty-state">{message}</div>;
}

// ---------- Alerts ----------
export function ErrorAlert({ message }) {
  if (!message) return null;
  return <div className="alert alert-error" role="alert">{message}</div>;
}

export function SuccessAlert({ message }) {
  if (!message) return null;
  return <div className="alert alert-success">{message}</div>;
}

// ---------- Card ----------
export function Card({ title, actions, children }) {
  return (
    <div className="card">
      {(title || actions) && (
        <div className="card-header">
          <h3 className="card-title">{title}</h3>
          {actions}
        </div>
      )}
      <div className="card-body">{children}</div>
    </div>
  );
}

// ---------- Detail item ----------
export function Detail({ label, children }) {
  return (
    <div className="detail-item">
      <div className="dt">{label}</div>
      <div className="dd">{children ?? '—'}</div>
    </div>
  );
}
