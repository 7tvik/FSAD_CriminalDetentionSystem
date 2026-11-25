import { useState } from 'react';
import { movementApi } from '@/api/endpoints';
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

const PERSON_TYPES = ['PRISONER', 'STAFF', 'OTHER'];
const MOVEMENT_TYPES = ['COURT', 'MEDICAL', 'TRANSFER', 'AUTHORIZED_VISIT', 'OTHER'];
const MOVEMENT_STATUSES = ['SCHEDULED', 'OUT', 'RETURNED', 'CANCELLED'];

const blankForm = () => ({
  personType: 'PRISONER',
  personRefId: '',
  personName: '',
  movementType: 'COURT',
  destination: '',
  reason: '',
  movementDate: todayISO(),
  timeOut: '',
  expectedReturn: '',
  escortOfficerId: '',
  remarks: '',
});

export function MovementsPage() {
  const { hasPermission } = useAuth();
  const canWrite = hasPermission(P.MOVEMENT_WRITE);

  const [page, setPage] = useState(0);
  const [overdueOnly, setOverdueOnly] = useState(false);
  const [filters, setFilters] = useState({
    movementType: '',
    status: '',
    dateFrom: '',
    dateTo: '',
  });
  const setFilter = (patch) => {
    setFilters((f) => ({ ...f, ...patch }));
    setPage(0);
  };

  const { data, loading, error, reload } = useAsync(
    () =>
      overdueOnly
        ? movementApi.overdue()
        : movementApi.search({
            page,
            size: 10,
            sort: 'movementDate,desc',
            movementType: filters.movementType || undefined,
            status: filters.status || undefined,
            dateFrom: filters.dateFrom || undefined,
            dateTo: filters.dateTo || undefined,
          }),
    [overdueOnly, page, filters],
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
      personType: form.personType,
      personRefId: form.personRefId ? Number(form.personRefId) : undefined,
      personName: form.personName.trim(),
      movementType: form.movementType,
      destination: form.destination.trim(),
      reason: form.reason.trim() || undefined,
      movementDate: form.movementDate,
      timeOut: form.timeOut || undefined,
      expectedReturn: form.expectedReturn || undefined,
      escortOfficerId: form.escortOfficerId ? Number(form.escortOfficerId) : undefined,
      remarks: form.remarks.trim() || undefined,
    };
    try {
      await movementApi.create(body);
      setModalOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const [action, setAction] = useState(null);
  const [actualReturn, setActualReturn] = useState('');
  const [actionSaving, setActionSaving] = useState(false);
  const [actionError, setActionError] = useState(null);

  const openAction = (row, type) => {
    setAction({ row, type });
    setActualReturn('');
    setActionError(null);
  };

  const confirmAction = async () => {
    if (!action) return;
    setActionSaving(true);
    setActionError(null);
    try {
      if (action.type === 'return') {
        await movementApi.markReturned(action.row.id, actualReturn ? { actualReturn } : {});
      } else {
        await movementApi.cancel(action.row.id);
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
        title="In-Out Register"
        subtitle="General movements of prisoners and staff"
        actions={
          <>
            <button
              className={`btn ${overdueOnly ? 'btn-danger' : 'btn-secondary'}`}
              onClick={() => {
                setOverdueOnly((v) => !v);
                setPage(0);
              }}
            >
              {overdueOnly ? 'Show all' : 'Show overdue only'}
            </button>
            {canWrite && (
              <button className="btn btn-primary" onClick={openCreate}>
                New Movement
              </button>
            )}
          </>
        }
      />

      {!overdueOnly && (
        <div className="toolbar">
          <Field label="Type" htmlFor="f-type">
            <EnumSelect
              id="f-type"
              value={filters.movementType}
              onChange={(v) => setFilter({ movementType: v })}
              options={MOVEMENT_TYPES}
              includeBlank
              blankLabel="All"
            />
          </Field>
          <Field label="Status" htmlFor="f-status">
            <EnumSelect
              id="f-status"
              value={filters.status}
              onChange={(v) => setFilter({ status: v })}
              options={MOVEMENT_STATUSES}
              includeBlank
              blankLabel="All"
            />
          </Field>
          <Field label="Date From" htmlFor="f-from">
            <input
              id="f-from"
              type="date"
              className="input"
              value={filters.dateFrom}
              onChange={(e) => setFilter({ dateFrom: e.target.value })}
            />
          </Field>
          <Field label="Date To" htmlFor="f-to">
            <input
              id="f-to"
              type="date"
              className="input"
              value={filters.dateTo}
              onChange={(e) => setFilter({ dateTo: e.target.value })}
            />
          </Field>
        </div>
      )}

      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading movements…" />
      ) : rows.length === 0 ? (
        <EmptyState message={overdueOnly ? 'No overdue movements.' : 'No movements found.'} />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Person</th>
                <th>Type</th>
                <th>Destination</th>
                <th>Movement Date</th>
                <th>Time Out</th>
                <th>Expected Return</th>
                <th>Status</th>
                {canWrite && <th></th>}
              </tr>
            </thead>
            <tbody>
              {rows.map((m) => (
                <tr key={m.id}>
                  <td>
                    {m.personName}
                    <span className="text-muted"> ({humanize(m.personType)})</span>
                  </td>
                  <td>{humanize(m.movementType)}</td>
                  <td>{m.destination}</td>
                  <td>{formatDate(m.movementDate)}</td>
                  <td>{formatDateTime(m.timeOut)}</td>
                  <td>{formatDateTime(m.expectedReturn)}</td>
                  <td>
                    <div className="row row-wrap">
                      <StatusBadge status={m.status} />
                      {m.overdue && <Badge variant="badge-danger">OVERDUE</Badge>}
                    </div>
                  </td>
                  {canWrite && (
                    <td className="actions">
                      {m.status === 'OUT' && (
                        <button
                          className="btn btn-secondary btn-sm"
                          onClick={() => openAction(m, 'return')}
                        >
                          Mark Returned
                        </button>
                      )}
                      {(m.status === 'SCHEDULED' || m.status === 'OUT') && (
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => openAction(m, 'cancel')}
                        >
                          Cancel
                        </button>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!overdueOnly && <Pagination page={pageData} onPageChange={setPage} />}

      <Modal
        open={modalOpen}
        title="New Movement"
        onClose={() => setModalOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>
              Cancel
            </button>
            <button className="btn btn-primary" form="movement-form" type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </button>
          </>
        }
      >
        <form id="movement-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          <Field label="Person Type" htmlFor="m-ptype" required>
            <EnumSelect
              id="m-ptype"
              value={form.personType}
              onChange={(v) => setForm((f) => ({ ...f, personType: v }))}
              options={PERSON_TYPES}
            />
          </Field>
          <Field label="Person Ref ID" htmlFor="m-pref" hint="Optional prisoner/staff id">
            <input
              id="m-pref"
              type="number"
              className="input"
              value={form.personRefId}
              onChange={(e) => setForm((f) => ({ ...f, personRefId: e.target.value }))}
            />
          </Field>
          <Field label="Person Name" htmlFor="m-pname" required>
            <input
              id="m-pname"
              className="input"
              value={form.personName}
              onChange={(e) => setForm((f) => ({ ...f, personName: e.target.value }))}
              required
            />
          </Field>
          <Field label="Movement Type" htmlFor="m-type" required>
            <EnumSelect
              id="m-type"
              value={form.movementType}
              onChange={(v) => setForm((f) => ({ ...f, movementType: v }))}
              options={MOVEMENT_TYPES}
            />
          </Field>
          <Field label="Destination" htmlFor="m-dest" required>
            <input
              id="m-dest"
              className="input"
              value={form.destination}
              onChange={(e) => setForm((f) => ({ ...f, destination: e.target.value }))}
              required
            />
          </Field>
          <Field label="Reason" htmlFor="m-reason">
            <input
              id="m-reason"
              className="input"
              value={form.reason}
              onChange={(e) => setForm((f) => ({ ...f, reason: e.target.value }))}
            />
          </Field>
          <Field label="Movement Date" htmlFor="m-date" required>
            <input
              id="m-date"
              type="date"
              className="input"
              value={form.movementDate}
              onChange={(e) => setForm((f) => ({ ...f, movementDate: e.target.value }))}
              required
            />
          </Field>
          <Field label="Time Out" htmlFor="m-out" hint="Set to start the movement as OUT">
            <input
              id="m-out"
              type="datetime-local"
              className="input"
              value={form.timeOut}
              onChange={(e) => setForm((f) => ({ ...f, timeOut: e.target.value }))}
            />
          </Field>
          <Field label="Expected Return" htmlFor="m-exp">
            <input
              id="m-exp"
              type="datetime-local"
              className="input"
              value={form.expectedReturn}
              onChange={(e) => setForm((f) => ({ ...f, expectedReturn: e.target.value }))}
            />
          </Field>
          <Field label="Escort Officer" htmlFor="m-escort">
            <StaffSelect
              id="m-escort"
              value={form.escortOfficerId}
              onChange={(v) => setForm((f) => ({ ...f, escortOfficerId: v }))}
              includeBlank
              blankLabel="None"
            />
          </Field>
          <Field label="Remarks" htmlFor="m-remarks" full>
            <textarea
              id="m-remarks"
              className="textarea"
              rows={2}
              value={form.remarks}
              onChange={(e) => setForm((f) => ({ ...f, remarks: e.target.value }))}
            />
          </Field>
        </form>
      </Modal>

      <Modal
        open={!!action}
        title={action?.type === 'return' ? 'Mark Returned' : 'Cancel Movement'}
        onClose={() => setAction(null)}
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setAction(null)}>
              Close
            </button>
            <button
              className={`btn ${action?.type === 'cancel' ? 'btn-danger' : 'btn-primary'}`}
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
          {action?.type === 'return' ? (
            <>
              <p>
                Mark <strong>{action.row.personName}</strong> as returned?
              </p>
              <Field label="Actual Return" htmlFor="a-return">
                <input
                  id="a-return"
                  type="datetime-local"
                  className="input"
                  value={actualReturn}
                  onChange={(e) => setActualReturn(e.target.value)}
                />
              </Field>
            </>
          ) : (
            <p>
              Cancel the movement for <strong>{action?.row.personName}</strong>?
            </p>
          )}
        </div>
      </Modal>
    </>
  );
}
