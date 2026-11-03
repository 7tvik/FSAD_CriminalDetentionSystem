import { useState, type FormEvent } from 'react';
import { releaseApi } from '@/api/endpoints';
import { toErrorMessage } from '@/api/http';
import type { ReleaseRecord, ReleaseStatus, ReleaseType } from '@/types';
import { useAsync } from '@/hooks/useAsync';
import { useAuth } from '@/auth/AuthContext';
import { P } from '@/auth/permissions';
import { PageHeader } from '@/components/layout/PageHeader';
import {
  EmptyState, ErrorAlert, LoadingBlock, StatusBadge,
} from '@/components/ui/Primitives';
import { Modal } from '@/components/ui/Modal';
import { Pagination } from '@/components/ui/Pagination';
import { Field, EnumSelect } from '@/components/ui/Field';
import { formatDate, humanize, todayISO } from '@/utils/format';

const RELEASE_STATUSES: ReleaseStatus[] = ['UPCOMING', 'READY', 'RELEASED', 'CANCELLED', 'ON_HOLD'];
const RELEASE_TYPES: ReleaseType[] = ['BAIL', 'SENTENCE_COMPLETE', 'ACQUITTAL', 'PAROLE', 'OTHER'];

// Verified state machine: valid target states per current status.
const TRANSITIONS: Record<ReleaseStatus, ReleaseStatus[]> = {
  UPCOMING: ['READY', 'ON_HOLD', 'CANCELLED'],
  READY: ['RELEASED', 'ON_HOLD', 'CANCELLED'],
  ON_HOLD: ['UPCOMING', 'READY', 'CANCELLED'],
  RELEASED: [],
  CANCELLED: [],
};

const emptyForm = () => ({
  prisonerId: '',
  expectedReleaseDate: todayISO(),
  releaseType: 'BAIL' as ReleaseType,
  reason: '',
  status: 'UPCOMING' as ReleaseStatus,
  remarks: '',
});

export function ReleasesPage() {
  const { hasPermission } = useAuth();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('');

  const { data, loading, error, reload } = useAsync(
    () => releaseApi.search({ page, size: 10, sort: 'expectedReleaseDate,asc', status }),
    [page, status],
  );

  const [createOpen, setCreateOpen] = useState(false);
  const [form, setForm] = useState(emptyForm());
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const set = (k: keyof ReturnType<typeof emptyForm>, v: string) => setForm((f) => ({ ...f, [k]: v }));

  const openCreate = () => {
    setForm(emptyForm());
    setFormError(null);
    setCreateOpen(true);
  };

  const submitCreate = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    try {
      const { prisonerId, ...rest } = form;
      const body: Record<string, unknown> = {
        ...Object.fromEntries(Object.entries(rest).filter(([, v]) => v !== '')),
        prisonerId: Number(prisonerId),
      };
      await releaseApi.create(body);
      setCreateOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  // Status-change modal state
  const [target, setTarget] = useState<{ release: ReleaseRecord; status: ReleaseStatus } | null>(null);
  const [actualReleaseDate, setActualReleaseDate] = useState(todayISO());
  const [statusRemarks, setStatusRemarks] = useState('');
  const [statusSaving, setStatusSaving] = useState(false);
  const [statusError, setStatusError] = useState<string | null>(null);

  const openStatus = (release: ReleaseRecord, s: ReleaseStatus) => {
    setTarget({ release, status: s });
    setActualReleaseDate(todayISO());
    setStatusRemarks('');
    setStatusError(null);
  };

  const submitStatus = async () => {
    if (!target) return;
    setStatusSaving(true);
    setStatusError(null);
    try {
      const body: Record<string, unknown> = { status: target.status };
      if (target.status === 'RELEASED') body.actualReleaseDate = actualReleaseDate;
      if (statusRemarks.trim()) body.remarks = statusRemarks.trim();
      await releaseApi.updateStatus(target.release.id, body);
      setTarget(null);
      reload();
    } catch (err) {
      setStatusError(toErrorMessage(err));
    } finally {
      setStatusSaving(false);
    }
  };

  const canWrite = hasPermission(P.RELEASE_WRITE);

  return (
    <>
      <PageHeader
        title="Releases"
        subtitle="Release register and workflow"
        actions={canWrite && <button className="btn btn-primary" onClick={openCreate}>+ New Release</button>}
      />

      <div className="toolbar">
        <EnumSelect
          value={status}
          onChange={(v) => { setPage(0); setStatus(v); }}
          options={RELEASE_STATUSES}
          includeBlank
          blankLabel="All statuses"
        />
        <span className="spacer" />
      </div>

      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading releases…" />
      ) : !data || data.content.length === 0 ? (
        <EmptyState message="No releases found." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Prisoner</th>
                <th>Expected Release</th>
                <th>Type</th>
                <th>Status</th>
                <th>Actual Release</th>
                <th>Authorized By</th>
                {canWrite && <th className="text-right">Actions</th>}
              </tr>
            </thead>
            <tbody>
              {data.content.map((r) => (
                <tr key={r.id}>
                  <td>{r.prisonerName}<div className="text-muted mono">{r.prisonerNumber}</div></td>
                  <td>{formatDate(r.expectedReleaseDate)}</td>
                  <td>{humanize(r.releaseType)}</td>
                  <td><StatusBadge status={r.status} /></td>
                  <td>{formatDate(r.actualReleaseDate)}</td>
                  <td>{r.authorizedByUsername || '—'}</td>
                  {canWrite && (
                    <td className="actions text-right">
                      {TRANSITIONS[r.status].map((s) => (
                        <button
                          key={s}
                          className={`btn btn-sm ${s === 'CANCELLED' ? 'btn-danger' : 'btn-secondary'}`}
                          onClick={() => openStatus(r, s)}
                        >
                          {humanize(s)}
                        </button>
                      ))}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={data} onPageChange={setPage} />

      <Modal
        open={createOpen}
        title="New Release"
        onClose={() => setCreateOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setCreateOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-primary" form="release-form" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Create Release'}</button>
          </>
        }
      >
        <form id="release-form" className="form-grid" onSubmit={submitCreate}>
          <ErrorAlert message={formError} />
          <Field label="Prisoner ID" required>
            <input className="input" type="number" value={form.prisonerId} onChange={(e) => set('prisonerId', e.target.value)} required />
          </Field>
          <Field label="Expected Release Date" required>
            <input className="input" type="date" value={form.expectedReleaseDate} onChange={(e) => set('expectedReleaseDate', e.target.value)} required />
          </Field>
          <Field label="Release Type" required>
            <EnumSelect value={form.releaseType} onChange={(v) => set('releaseType', v)} options={RELEASE_TYPES} />
          </Field>
          <Field label="Status">
            <EnumSelect value={form.status} onChange={(v) => set('status', v)} options={RELEASE_STATUSES} />
          </Field>
          <Field label="Reason" full>
            <textarea className="textarea" value={form.reason} onChange={(e) => set('reason', e.target.value)} />
          </Field>
          <Field label="Remarks" full>
            <textarea className="textarea" value={form.remarks} onChange={(e) => set('remarks', e.target.value)} />
          </Field>
        </form>
      </Modal>

      <Modal
        open={!!target}
        title={target ? `Set status to ${humanize(target.status)}` : ''}
        onClose={() => setTarget(null)}
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setTarget(null)} disabled={statusSaving}>Cancel</button>
            <button
              className={`btn ${target?.status === 'CANCELLED' ? 'btn-danger' : 'btn-primary'}`}
              onClick={submitStatus}
              disabled={statusSaving}
            >
              {statusSaving ? 'Saving…' : 'Confirm'}
            </button>
          </>
        }
      >
        {target && (
          <div className="stack">
            <ErrorAlert message={statusError} />
            <p>
              Change release for <strong>{target.release.prisonerName}</strong> to{' '}
              <strong>{humanize(target.status)}</strong>?
            </p>
            {target.status === 'RELEASED' && (
              <Field label="Actual Release Date" required>
                <input className="input" type="date" value={actualReleaseDate} onChange={(e) => setActualReleaseDate(e.target.value)} />
              </Field>
            )}
            <Field label="Remarks">
              <textarea className="textarea" value={statusRemarks} onChange={(e) => setStatusRemarks(e.target.value)} />
            </Field>
          </div>
        )}
      </Modal>
    </>
  );
}
