import { useState, type FormEvent } from 'react';
import { interviewApi } from '@/api/endpoints';
import { useAsync } from '@/hooks/useAsync';
import { useAuth } from '@/auth/AuthContext';
import { P } from '@/auth/permissions';
import { toErrorMessage } from '@/api/http';
import { PageHeader } from '@/components/layout/PageHeader';
import { EmptyState, ErrorAlert, LoadingBlock, StatusBadge } from '@/components/ui/Primitives';
import { Modal } from '@/components/ui/Modal';
import { Pagination } from '@/components/ui/Pagination';
import { Field, EnumSelect } from '@/components/ui/Field';
import { formatDate, formatTime, todayISO } from '@/utils/format';
import type { InterviewRequest, InterviewStatus } from '@/types';

const INTERVIEW_STATUSES: InterviewStatus[] = [
  'PENDING', 'APPROVED', 'REJECTED', 'SCHEDULED', 'COMPLETED', 'CANCELLED',
];

type ActionType = 'approve' | 'reject' | 'schedule' | 'complete' | 'cancel';

const ACTIONS_FOR: Record<InterviewStatus, ActionType[]> = {
  PENDING: ['approve', 'reject', 'cancel'],
  APPROVED: ['schedule', 'cancel'],
  SCHEDULED: ['complete', 'cancel'],
  REJECTED: [],
  COMPLETED: [],
  CANCELLED: [],
};

const ACTION_LABEL: Record<ActionType, string> = {
  approve: 'Approve',
  reject: 'Reject',
  schedule: 'Schedule',
  complete: 'Complete',
  cancel: 'Cancel',
};

interface InterviewForm {
  prisonerId: string;
  visitorName: string;
  relationship: string;
  contact: string;
  idDetails: string;
  requestedDate: string;
  requestedTime: string;
  purpose: string;
  remarks: string;
}

const blankForm = (): InterviewForm => ({
  prisonerId: '',
  visitorName: '',
  relationship: '',
  contact: '',
  idDetails: '',
  requestedDate: todayISO(),
  requestedTime: '',
  purpose: '',
  remarks: '',
});

export function InterviewsPage() {
  const { hasPermission } = useAuth();
  const canWrite = hasPermission(P.INTERVIEW_WRITE);
  const canReview = hasPermission(P.INTERVIEW_REVIEW);

  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({ status: '', prisonerId: '' });
  const setFilter = (patch: Partial<typeof filters>) => {
    setFilters((f) => ({ ...f, ...patch }));
    setPage(0);
  };

  const { data, loading, error, reload } = useAsync(
    () =>
      interviewApi.search({
        page,
        size: 10,
        sort: 'requestedDate,desc',
        status: filters.status || undefined,
        prisonerId: filters.prisonerId ? Number(filters.prisonerId) : undefined,
      }),
    [page, filters],
  );

  const rows = data?.content ?? [];

  // create modal
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<InterviewForm>(blankForm());
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const openCreate = () => {
    setForm(blankForm());
    setFormError(null);
    setModalOpen(true);
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    const body: Partial<InterviewRequest> = {
      prisonerId: Number(form.prisonerId),
      visitorName: form.visitorName.trim(),
      relationship: form.relationship.trim() || undefined,
      contact: form.contact.trim() || undefined,
      idDetails: form.idDetails.trim() || undefined,
      requestedDate: form.requestedDate,
      requestedTime: form.requestedTime || undefined,
      purpose: form.purpose.trim() || undefined,
      remarks: form.remarks.trim() || undefined,
    };
    try {
      await interviewApi.create(body);
      setModalOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  // action modal
  const [action, setAction] = useState<{ row: InterviewRequest; type: ActionType } | null>(null);
  const [actionRemarks, setActionRemarks] = useState('');
  const [schedDate, setSchedDate] = useState('');
  const [schedTime, setSchedTime] = useState('');
  const [actionSaving, setActionSaving] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const openAction = (row: InterviewRequest, type: ActionType) => {
    setAction({ row, type });
    setActionRemarks('');
    setSchedDate(row.requestedDate ?? '');
    setSchedTime((row.requestedTime ?? '').slice(0, 5));
    setActionError(null);
  };

  const confirmAction = async () => {
    if (!action) return;
    setActionSaving(true);
    setActionError(null);
    const { row, type } = action;
    const remarks = actionRemarks.trim() || undefined;
    try {
      switch (type) {
        case 'approve':
          await interviewApi.approve(row.id, { remarks });
          break;
        case 'reject':
          await interviewApi.reject(row.id, { remarks });
          break;
        case 'schedule':
          await interviewApi.schedule(row.id, {
            requestedDate: schedDate || undefined,
            requestedTime: schedTime || undefined,
            remarks,
          });
          break;
        case 'complete':
          await interviewApi.complete(row.id, { remarks });
          break;
        case 'cancel':
          await interviewApi.cancel(row.id, { remarks });
          break;
      }
      setAction(null);
      reload();
    } catch (err) {
      setActionError(toErrorMessage(err));
    } finally {
      setActionSaving(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Interview Requests"
        subtitle="Visitor interview requests with prisoners"
        actions={
          canWrite ? (
            <button className="btn btn-primary" onClick={openCreate}>
              New Request
            </button>
          ) : undefined
        }
      />

      <div className="toolbar">
        <Field label="Status" htmlFor="f-status">
          <EnumSelect
            id="f-status"
            value={filters.status}
            onChange={(v) => setFilter({ status: v })}
            options={INTERVIEW_STATUSES}
            includeBlank
            blankLabel="All"
          />
        </Field>
        <Field label="Prisoner ID" htmlFor="f-pid">
          <input
            id="f-pid"
            type="number"
            className="input"
            value={filters.prisonerId}
            onChange={(e) => setFilter({ prisonerId: e.target.value })}
          />
        </Field>
      </div>

      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading interview requests…" />
      ) : rows.length === 0 ? (
        <EmptyState message="No interview requests found." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Prisoner</th>
                <th>Visitor</th>
                <th>Relationship</th>
                <th>Requested</th>
                <th>Purpose</th>
                <th>Status</th>
                <th>Reviewed By</th>
                {canReview && <th></th>}
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.id}>
                  <td>
                    {r.prisonerName ?? '—'}
                    {r.prisonerNumber && <span className="text-muted"> ({r.prisonerNumber})</span>}
                  </td>
                  <td>{r.visitorName}</td>
                  <td>{r.relationship ?? '—'}</td>
                  <td>
                    {formatDate(r.requestedDate)}
                    {r.requestedTime && <span className="text-muted"> {formatTime(r.requestedTime)}</span>}
                  </td>
                  <td>{r.purpose ?? '—'}</td>
                  <td>
                    <StatusBadge status={r.status} />
                  </td>
                  <td>{r.reviewedByUsername ?? '—'}</td>
                  {canReview && (
                    <td className="actions">
                      {ACTIONS_FOR[r.status].map((t) => (
                        <button
                          key={t}
                          className={`btn btn-sm ${
                            t === 'reject' || t === 'cancel' ? 'btn-danger' : 'btn-secondary'
                          }`}
                          onClick={() => openAction(r, t)}
                        >
                          {ACTION_LABEL[t]}
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

      {/* Create modal */}
      <Modal
        open={modalOpen}
        title="New Interview Request"
        onClose={() => setModalOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>
              Cancel
            </button>
            <button className="btn btn-primary" form="interview-form" type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </button>
          </>
        }
      >
        <form id="interview-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          <Field label="Prisoner ID" htmlFor="i-pid" required>
            <input
              id="i-pid"
              type="number"
              className="input"
              value={form.prisonerId}
              onChange={(e) => setForm((f) => ({ ...f, prisonerId: e.target.value }))}
              required
            />
          </Field>
          <Field label="Visitor Name" htmlFor="i-visitor" required>
            <input
              id="i-visitor"
              className="input"
              value={form.visitorName}
              onChange={(e) => setForm((f) => ({ ...f, visitorName: e.target.value }))}
              required
            />
          </Field>
          <Field label="Relationship" htmlFor="i-rel">
            <input
              id="i-rel"
              className="input"
              value={form.relationship}
              onChange={(e) => setForm((f) => ({ ...f, relationship: e.target.value }))}
            />
          </Field>
          <Field label="Contact" htmlFor="i-contact">
            <input
              id="i-contact"
              className="input"
              value={form.contact}
              onChange={(e) => setForm((f) => ({ ...f, contact: e.target.value }))}
            />
          </Field>
          <Field label="ID Details" htmlFor="i-id">
            <input
              id="i-id"
              className="input"
              value={form.idDetails}
              onChange={(e) => setForm((f) => ({ ...f, idDetails: e.target.value }))}
            />
          </Field>
          <Field label="Requested Date" htmlFor="i-date" required>
            <input
              id="i-date"
              type="date"
              className="input"
              value={form.requestedDate}
              onChange={(e) => setForm((f) => ({ ...f, requestedDate: e.target.value }))}
              required
            />
          </Field>
          <Field label="Requested Time" htmlFor="i-time">
            <input
              id="i-time"
              type="time"
              className="input"
              value={form.requestedTime}
              onChange={(e) => setForm((f) => ({ ...f, requestedTime: e.target.value }))}
            />
          </Field>
          <Field label="Purpose" htmlFor="i-purpose">
            <input
              id="i-purpose"
              className="input"
              value={form.purpose}
              onChange={(e) => setForm((f) => ({ ...f, purpose: e.target.value }))}
            />
          </Field>
          <Field label="Remarks" htmlFor="i-remarks" full>
            <textarea
              id="i-remarks"
              className="textarea"
              rows={2}
              value={form.remarks}
              onChange={(e) => setForm((f) => ({ ...f, remarks: e.target.value }))}
            />
          </Field>
        </form>
      </Modal>

      {/* Action modal */}
      <Modal
        open={!!action}
        title={action ? `${ACTION_LABEL[action.type]} Request` : ''}
        onClose={() => setAction(null)}
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setAction(null)}>
              Close
            </button>
            <button
              className={`btn ${
                action?.type === 'reject' || action?.type === 'cancel' ? 'btn-danger' : 'btn-primary'
              }`}
              onClick={confirmAction}
              disabled={actionSaving}
            >
              {actionSaving ? 'Working…' : 'Confirm'}
            </button>
          </>
        }
      >
        <div className="stack">
          <ErrorAlert message={actionError} />
          {action && (
            <p>
              {ACTION_LABEL[action.type]} interview request for{' '}
              <strong>{action.row.visitorName}</strong>?
            </p>
          )}
          {action?.type === 'schedule' && (
            <>
              <Field label="Scheduled Date" htmlFor="s-date">
                <input
                  id="s-date"
                  type="date"
                  className="input"
                  value={schedDate}
                  onChange={(e) => setSchedDate(e.target.value)}
                />
              </Field>
              <Field label="Scheduled Time" htmlFor="s-time">
                <input
                  id="s-time"
                  type="time"
                  className="input"
                  value={schedTime}
                  onChange={(e) => setSchedTime(e.target.value)}
                />
              </Field>
            </>
          )}
          <Field label="Remarks" htmlFor="ar-remarks">
            <textarea
              id="ar-remarks"
              className="textarea"
              rows={2}
              value={actionRemarks}
              onChange={(e) => setActionRemarks(e.target.value)}
            />
          </Field>
        </div>
      </Modal>
    </>
  );
}
