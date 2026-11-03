import { useState, type FormEvent } from 'react';
import { auditApi } from '@/api/endpoints';
import type { AuditLogEntry } from '@/types';
import { useAsync } from '@/hooks/useAsync';
import { PageHeader } from '@/components/layout/PageHeader';
import {
  Badge, Detail, EmptyState, ErrorAlert, LoadingBlock,
} from '@/components/ui/Primitives';
import { Modal } from '@/components/ui/Modal';
import { Pagination } from '@/components/ui/Pagination';
import { formatDateTime } from '@/utils/format';

interface Filters {
  action: string;
  entityType: string;
  userId: string;
  from: string;
  to: string;
}

const emptyFilters = (): Filters => ({ action: '', entityType: '', userId: '', from: '', to: '' });

export function AuditPage() {
  const [page, setPage] = useState(0);
  const [draft, setDraft] = useState<Filters>(emptyFilters());
  const [filters, setFilters] = useState<Filters>(emptyFilters());
  const [selected, setSelected] = useState<AuditLogEntry | null>(null);

  const { data, loading, error } = useAsync(
    () => auditApi.search({
      page,
      size: 20,
      sort: 'createdAt,desc',
      action: filters.action || undefined,
      entityType: filters.entityType || undefined,
      userId: filters.userId ? Number(filters.userId) : undefined,
      from: filters.from || undefined,
      to: filters.to || undefined,
    }),
    [page, filters],
  );

  const applyFilters = (e: FormEvent) => {
    e.preventDefault();
    setPage(0);
    setFilters(draft);
  };

  const resetFilters = () => {
    setDraft(emptyFilters());
    setFilters(emptyFilters());
    setPage(0);
  };

  const setD = (k: keyof Filters, v: string) => setDraft((f) => ({ ...f, [k]: v }));

  return (
    <>
      <PageHeader
        title="Audit Log"
        subtitle="Append-only trail of system activity (read-only)"
      />

      <form className="toolbar" onSubmit={applyFilters}>
        <input className="input" placeholder="Action…" value={draft.action} onChange={(e) => setD('action', e.target.value)} style={{ maxWidth: 160 }} />
        <input className="input" placeholder="Entity type…" value={draft.entityType} onChange={(e) => setD('entityType', e.target.value)} style={{ maxWidth: 160 }} />
        <input className="input" type="number" placeholder="User ID" value={draft.userId} onChange={(e) => setD('userId', e.target.value)} style={{ maxWidth: 110 }} />
        <input className="input" type="date" value={draft.from} onChange={(e) => setD('from', e.target.value)} title="From" />
        <input className="input" type="date" value={draft.to} onChange={(e) => setD('to', e.target.value)} title="To" />
        <button className="btn btn-secondary" type="submit">Search</button>
        <button className="btn btn-ghost" type="button" onClick={resetFilters}>Reset</button>
        <span className="spacer" />
      </form>

      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading audit log…" />
      ) : !data || data.content.length === 0 ? (
        <EmptyState message="No audit entries found." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Time</th>
                <th>User</th>
                <th>Action</th>
                <th>Entity</th>
                <th>IP</th>
                <th className="text-right">Details</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((e) => (
                <tr key={e.id}>
                  <td className="nowrap">{formatDateTime(e.createdAt)}</td>
                  <td>{e.usernameSnapshot || (e.userId != null ? `#${e.userId}` : '—')}</td>
                  <td><Badge variant="badge-info">{e.action}</Badge></td>
                  <td>{e.entityType ? `${e.entityType}${e.entityId != null ? `#${e.entityId}` : ''}` : '—'}</td>
                  <td className="mono">{e.ipAddress || '—'}</td>
                  <td className="actions text-right">
                    <button className="btn btn-ghost btn-sm" onClick={() => setSelected(e)}>Details</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={data} onPageChange={setPage} />

      <Modal
        open={selected !== null}
        title={`Audit Entry${selected ? ` #${selected.id}` : ''}`}
        onClose={() => setSelected(null)}
        wide
      >
        {selected && (
          <div className="stack">
            <div className="detail-grid">
              <Detail label="Action">{selected.action}</Detail>
              <Detail label="Entity">
                {selected.entityType ? `${selected.entityType}${selected.entityId != null ? `#${selected.entityId}` : ''}` : '—'}
              </Detail>
              <Detail label="User">
                {selected.usernameSnapshot || (selected.userId != null ? `#${selected.userId}` : '—')}
              </Detail>
              <Detail label="IP Address">{selected.ipAddress || '—'}</Detail>
              <Detail label="Timestamp">{formatDateTime(selected.createdAt)}</Detail>
            </div>
            <div>
              <div className="dt" style={{ fontWeight: 600 }}>Old Value</div>
              <pre className="mono" style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                {selected.oldValue || '—'}
              </pre>
            </div>
            <div>
              <div className="dt" style={{ fontWeight: 600 }}>New Value</div>
              <pre className="mono" style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                {selected.newValue || '—'}
              </pre>
            </div>
          </div>
        )}
      </Modal>
    </>
  );
}
