import { useState } from 'react';
import { courtApi } from '@/api/endpoints';
import { useAsync } from '@/hooks/useAsync';
import { useAuth } from '@/auth/AuthContext';
import { P } from '@/auth/permissions';
import { toErrorMessage } from '@/api/http';
import { PageHeader } from '@/components/layout/PageHeader';
import { Badge, EmptyState, ErrorAlert, LoadingBlock, StatusBadge } from '@/components/ui/Primitives';
import { Modal } from '@/components/ui/Modal';
import { Pagination } from '@/components/ui/Pagination';
import { Field, EnumSelect } from '@/components/ui/Field';
import { StaffSelect } from '@/components/ui/StaffSelect';
import { formatDate, formatDateTime, humanize, todayISO } from '@/utils/format';

const COURT_STATUSES = [
  'SCHEDULED', 'DEPARTED', 'AT_COURT', 'HEARING_COMPLETED', 'DEPARTED_COURT', 'RETURNED', 'CANCELLED',
];

const NEXT_STATUS = {
  SCHEDULED: ['DEPARTED', 'CANCELLED'],
  DEPARTED: ['AT_COURT'],
  AT_COURT: ['HEARING_COMPLETED'],
  HEARING_COMPLETED: ['DEPARTED_COURT'],
  DEPARTED_COURT: ['RETURNED'],
  RETURNED: [],
  CANCELLED: [],
};

const blankForm = () => ({
  prisonerId: '',
  caseId: '',
  court: '',
  hearingDate: todayISO(),
  destination: '',
  departureTime: '',
  expectedReturn: '',
  escortOfficerId: '',
  remarks: '',
});

export function CourtPage() {
  const { hasPermission } = useAuth();
  const canWrite = hasPermission(P.COURT_WRITE);

  const [page, setPage] = useState(0);
  const [notReturnedOnly, setNotReturnedOnly] = useState(false);
  const [filters, setFilters] = useState({
    status: '',
    hearingDateFrom: '',
    hearingDateTo: '',
    prisonerId: '',
  });
  const setFilter = (patch) => {
    setFilters((f) => ({ ...f, ...patch }));
    setPage(0);
  };

  const { data, loading, error, reload } = useAsync(
    () =>
      notReturnedOnly
        ? courtApi.notReturned()
        : courtApi.search({
            page,
            size: 10,
            sort: 'hearingDate,desc',
            status: filters.status || undefined,
            hearingDateFrom: filters.hearingDateFrom || undefined,
            hearingDateTo: filters.hearingDateTo || undefined,
            prisonerId: filters.prisonerId ? Number(filters.prisonerId) : undefined,
          }),
    [notReturnedOnly, page, filters],
  );

  const rows = data ? (Array.isArray(data) ? data : data.content) : [];
  const pageData = data && !Array.isArray(data) ? data : null;

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(blankForm());
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState(null);

  const openCreate = () => {
    setForm(blankForm());
    setFormError(null);
    setModalOpen(true);
  };

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    const body = {
      prisonerId: Number(form.prisonerId),
      caseId: form.caseId ? Number(form.caseId) : undefined,
      court: form.court.trim(),
      hearingDate: form.hearingDate,
      destination: form.destination.trim() || undefined,
      departureTime: form.departureTime || undefined,
      expectedReturn: form.expectedReturn || undefined,
      escortOfficerId: form.escortOfficerId ? Number(form.escortOfficerId) : undefined,
      remarks: form.remarks.trim() || undefined,
    };
    try {
      await courtApi.create(body);
      setModalOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const [advance, setAdvance] = useState(null);
  const [outcome, setOutcome] = useState('');
  const [advanceRemarks, setAdvanceRemarks] = useState('');
  const [advanceSaving, setAdvanceSaving] = useState(false);
  const [advanceError, setAdvanceError] = useState(null);

  const openAdvance = (row, next) => {
    setAdvance({ row, next });
    setOutcome(row.outcome ?? '');
    setAdvanceRemarks('');
    setAdvanceError(null);
  };

  const confirmAdvance = async () => {
    if (!advance) return;
    setAdvanceSaving(true);
    setAdvanceError(null);
    try {
      await courtApi.updateStatus(advance.row.id, {
        status: advance.next,
        outcome: outcome.trim() || undefined,
        remarks: advanceRemarks.trim() || undefined,
      });
      setAdvance(null);
      reload();
    } catch (err) {
      setAdvanceError(toErrorMessage(err));
    } finally {
      setAdvanceSaving(false);
    }
  };

  const outcomeEmphasis =
    advance?.next === 'HEARING_COMPLETED' || advance?.next === 'RETURNED';

  return (
    <>
      <PageHeader
        title="Court Movements"
        subtitle="Prisoner court hearings and production"
        actions={
          <>
            <button
              className={`btn ${notReturnedOnly ? 'btn-danger' : 'btn-secondary'}`}
              onClick={() => {
                setNotReturnedOnly((v) => !v);
                setPage(0);
              }}
            >
              {notReturnedOnly ? 'Show all' : 'Show not-returned'}
            </button>
            {canWrite && (
              <button className="btn btn-primary" onClick={openCreate}>
                New Court Movement
              </button>
            )}
          </>
        }
      />

      {!notReturnedOnly && (
        <div className="toolbar">
          <Field label="Status" htmlFor="f-status">
            <EnumSelect
              id="f-status"
              value={filters.status}
              onChange={(v) => setFilter({ status: v })}
              options={COURT_STATUSES}
              includeBlank
              blankLabel="All"
            />
          </Field>
          <Field label="Hearing From" htmlFor="f-from">
            <input
              id="f-from"
              type="date"
              className="input"
              value={filters.hearingDateFrom}
              onChange={(e) => setFilter({ hearingDateFrom: e.target.value })}
            />
          </Field>
          <Field label="Hearing To" htmlFor="f-to">
            <input
              id="f-to"
              type="date"
              className="input"
              value={filters.hearingDateTo}
              onChange={(e) => setFilter({ hearingDateTo: e.target.value })}
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
      )}

      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading court movements…" />
      ) : rows.length === 0 ? (
        <EmptyState message={notReturnedOnly ? 'No not-returned hearings.' : 'No court movements found.'} />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Prisoner</th>
                <th>Case</th>
                <th>Court</th>
                <th>Hearing Date</th>
                <th>Departure</th>
                <th>Expected Return</th>
                <th>Status</th>
                {canWrite && <th></th>}
              </tr>
            </thead>
            <tbody>
              {rows.map((c) => (
                <tr key={c.id}>
                  <td>
                    {c.prisonerName ?? '—'}
                    {c.prisonerId != null && <span className="text-muted"> (#{c.prisonerId})</span>}
                  </td>
                  <td>{c.caseNumber ?? '—'}</td>
                  <td>{c.court}</td>
                  <td>{formatDate(c.hearingDate)}</td>
                  <td>{formatDateTime(c.departureTime)}</td>
                  <td>{formatDateTime(c.expectedReturn)}</td>
                  <td>
                    <div className="row row-wrap">
                      <StatusBadge status={c.status} />
                      {c.notReturned && <Badge variant="badge-danger">NOT RETURNED</Badge>}
                    </div>
                  </td>
                  {canWrite && (
                    <td className="actions">
                      {NEXT_STATUS[c.status].map((next) => (
                        <button
                          key={next}
                          className={`btn btn-sm ${next === 'CANCELLED' ? 'btn-danger' : 'btn-secondary'}`}
                          onClick={() => openAdvance(c, next)}
                        >
                          {next === 'CANCELLED' ? 'Cancel' : humanize(next)}
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

      {!notReturnedOnly && <Pagination page={pageData} onPageChange={setPage} />}

      <Modal
        open={modalOpen}
        title="New Court Movement"
        onClose={() => setModalOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>
              Cancel
            </button>
            <button className="btn btn-primary" form="court-form" type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </button>
          </>
        }
      >
        <form id="court-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          <Field label="Prisoner ID" htmlFor="c-pid" required>
            <input
              id="c-pid"
              type="number"
              className="input"
              value={form.prisonerId}
              onChange={(e) => setForm((f) => ({ ...f, prisonerId: e.target.value }))}
              required
            />
          </Field>
          <Field label="Case ID" htmlFor="c-cid">
            <input
              id="c-cid"
              type="number"
              className="input"
              value={form.caseId}
              onChange={(e) => setForm((f) => ({ ...f, caseId: e.target.value }))}
            />
          </Field>
          <Field label="Court" htmlFor="c-court" required>
            <input
              id="c-court"
              className="input"
              value={form.court}
              onChange={(e) => setForm((f) => ({ ...f, court: e.target.value }))}
              required
            />
          </Field>
          <Field label="Hearing Date" htmlFor="c-date" required>
            <input
              id="c-date"
              type="date"
              className="input"
              value={form.hearingDate}
              onChange={(e) => setForm((f) => ({ ...f, hearingDate: e.target.value }))}
              required
            />
          </Field>
          <Field label="Destination" htmlFor="c-dest">
            <input
              id="c-dest"
              className="input"
              value={form.destination}
              onChange={(e) => setForm((f) => ({ ...f, destination: e.target.value }))}
            />
          </Field>
          <Field label="Departure Time" htmlFor="c-dep">
            <input
              id="c-dep"
              type="datetime-local"
              className="input"
              value={form.departureTime}
              onChange={(e) => setForm((f) => ({ ...f, departureTime: e.target.value }))}
            />
          </Field>
          <Field label="Expected Return" htmlFor="c-exp">
            <input
              id="c-exp"
              type="datetime-local"
              className="input"
              value={form.expectedReturn}
              onChange={(e) => setForm((f) => ({ ...f, expectedReturn: e.target.value }))}
            />
          </Field>
          <Field label="Escort Officer" htmlFor="c-escort">
            <StaffSelect
              id="c-escort"
              value={form.escortOfficerId}
              onChange={(v) => setForm((f) => ({ ...f, escortOfficerId: v }))}
              includeBlank
              blankLabel="None"
            />
          </Field>
          <Field label="Remarks" htmlFor="c-remarks" full>
            <textarea
              id="c-remarks"
              className="textarea"
              rows={2}
              value={form.remarks}
              onChange={(e) => setForm((f) => ({ ...f, remarks: e.target.value }))}
            />
          </Field>
        </form>
      </Modal>

      <Modal
        open={!!advance}
        title="Advance Status"
        onClose={() => setAdvance(null)}
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setAdvance(null)}>
              Cancel
            </button>
            <button
              className={`btn ${advance?.next === 'CANCELLED' ? 'btn-danger' : 'btn-primary'}`}
              onClick={confirmAdvance}
              disabled={advanceSaving}
            >
              {advanceSaving ? 'Working…' : 'Confirm'}
            </button>
          </>
        }
      >
        <div className="stack">
          <ErrorAlert message={advanceError} />
          {advance && (
            <p>
              Move to <StatusBadge status={advance.next} /> for{' '}
              <strong>{advance.row.prisonerName ?? `Prisoner #${advance.row.prisonerId ?? ''}`}</strong>?
            </p>
          )}
          <Field label="Outcome" htmlFor="a-outcome" required={outcomeEmphasis}>
            <input
              id="a-outcome"
              className="input"
              value={outcome}
              onChange={(e) => setOutcome(e.target.value)}
            />
          </Field>
          <Field label="Remarks" htmlFor="a-remarks">
            <textarea
              id="a-remarks"
              className="textarea"
              rows={2}
              value={advanceRemarks}
              onChange={(e) => setAdvanceRemarks(e.target.value)}
            />
          </Field>
        </div>
      </Modal>
    </>
  );
}
