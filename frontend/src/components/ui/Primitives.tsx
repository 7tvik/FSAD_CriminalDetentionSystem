import type { ReactNode } from 'react';
import { statusVariant, humanize } from '@/utils/format';

// ---------- Badge ----------
export function Badge({ children, variant = 'badge-neutral' }: { children: ReactNode; variant?: string }) {
  return <span className={`badge ${variant}`}>{children}</span>;
}

export function StatusBadge({ status }: { status?: string }) {
  if (!status) return <span className="text-muted">—</span>;
  return <span className={`badge ${statusVariant(status)}`}>{humanize(status)}</span>;
}

// ---------- Spinner / loading / empty ----------
export function Spinner() {
  return <span className="spinner" aria-label="Loading" />;
}

export function LoadingBlock({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="empty-state">
      <Spinner />
      <div className="mt-4">{label}</div>
    </div>
  );
}

export function EmptyState({ message = 'No records found.' }: { message?: string }) {
  return <div className="empty-state">{message}</div>;
}

// ---------- Alerts ----------
export function ErrorAlert({ message }: { message?: string | null }) {
  if (!message) return null;
  return <div className="alert alert-error" role="alert">{message}</div>;
}

export function SuccessAlert({ message }: { message?: string | null }) {
  if (!message) return null;
  return <div className="alert alert-success">{message}</div>;
}

// ---------- Card ----------
export function Card({ title, actions, children }: { title?: ReactNode; actions?: ReactNode; children: ReactNode }) {
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
export function Detail({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="detail-item">
      <div className="dt">{label}</div>
      <div className="dd">{children ?? '—'}</div>
    </div>
  );
}
