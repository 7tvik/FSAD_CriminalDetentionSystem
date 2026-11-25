import { useState } from 'react';
import { dutyApi } from '@/api/endpoints';
import { useAsync } from '@/hooks/useAsync';
import { useAuth } from '@/auth/AuthContext';
import { P } from '@/auth/permissions';
import { toErrorMessage } from '@/api/http';
import { PageHeader } from '@/components/layout/PageHeader';
import { EmptyState, ErrorAlert, LoadingBlock } from '@/components/ui/Primitives';
import { Modal } from '@/components/ui/Modal';
import { Pagination } from '@/components/ui/Pagination';
import { Field } from '@/components/ui/Field';
import { StaffSelect } from '@/components/ui/StaffSelect';
import { formatDate, formatTime, todayISO } from '@/utils/format';

const blankForm = () => ({
  staffId: '',
  location: '',
  purpose: '',
  dutyDate: todayISO(),
  startTime: '',
  endTime: '',
  remarks: '',
});

export function DutyPage() {
  const { hasPermission } = useAuth();
  const canWrite = hasPermission(P.DUTY_WRITE);

  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({ staffId: '', dutyDate: '', location: '' });
  const setFilter = (patch) => {
    setFilters((f) => ({ ...f, ...patch }));
    setPage(0);
  };

  const { data, loading, error, reload } = useAsync(
    () =>
      dutyApi.search({
        page,
        size: 10,
        sort: 'dutyDate,desc',
        staffId: filters.staffId || undefined,
        dutyDate: filters.dutyDate || undefined,
        location: filters.location || undefined,
      }),
    [page, filters],
  );

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(blankForm());
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState(null);

  const openCreate = () => {
    setEditing(null);
    setForm(blankForm());
    setFormError(null);
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setEditing(row);
    setForm({
      staffId: String(row.staffId),
      location: row.location ?? '',
      purpose: row.purpose ?? '',
      dutyDate: row.dutyDate,
      startTime: (row.startTime ?? '').slice(0, 5),
      endTime: (row.endTime ?? '').slice(0, 5),
      remarks: row.remarks ?? '',
    });
    setFormError(null);
    setModalOpen(true);
  };

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    const body = {
      staffId: Number(form.staffId),
      location: form.location.trim(),
      purpose: form.purpose.trim() || undefined,
      dutyDate: form.dutyDate,
      startTime: form.startTime,
      endTime: form.endTime,
      remarks: form.remarks.trim() || undefined,
    };
    try {
      if (editing) await dutyApi.update(editing.id, body);
      else await dutyApi.create(body);
      setModalOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const rows = data?.content ?? [];

  return (
    <>
      <PageHeader
        title="Duty Register"
        subtitle="Staff duty assignments"
        actions={
          canWrite ? (
            <button className="btn btn-primary" onClick={openCreate}>
              New Duty
            </button>
          ) : undefined
        }
      />

      <div className="toolbar">
        <Field label="Staff" htmlFor="f-staff">
          <StaffSelect
            id="f-staff"
            value={filters.staffId}
            onChange={(v) => setFilter({ staffId: v })}
            blankLabel="All"
          />
        </Field>
        <Field label="Duty Date" htmlFor="f-date">
          <input
            id="f-date"
            type="date"
            className="input"
            value={filters.dutyDate}
            onChange={(e) => setFilter({ dutyDate: e.target.value })}
          />
        </Field>
        <Field label="Location" htmlFor="f-loc">
          <input
            id="f-loc"
            className="input"
            placeholder="Filter by location"
            value={filters.location}
            onChange={(e) => setFilter({ location: e.target.value })}
          />
        </Field>
      </div>

      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading duties…" />
      ) : rows.length === 0 ? (
        <EmptyState message="No duty records found." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Staff</th>
                <th>Location</th>
                <th>Purpose</th>
                <th>Duty Date</th>
                <th>Time</th>
                <th>Remarks</th>
                {canWrite && <th></th>}
              </tr>
            </thead>
            <tbody>
              {rows.map((d) => (
                <tr key={d.id}>
                  <td>
                    {d.staffName ?? '—'}
                    {d.staffNumber && <span className="text-muted"> ({d.staffNumber})</span>}
                  </td>
                  <td>{d.location}</td>
                  <td>{d.purpose ?? '—'}</td>
                  <td>{formatDate(d.dutyDate)}</td>
                  <td className="mono">
                    {formatTime(d.startTime)}–{formatTime(d.endTime)}
                  </td>
                  <td>{d.remarks ?? '—'}</td>
                  {canWrite && (
                    <td className="actions">
                      <button className="btn btn-secondary btn-sm" onClick={() => openEdit(d)}>
                        Edit
                      </button>
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
        open={modalOpen}
        title={editing ? 'Edit Duty' : 'New Duty'}
        onClose={() => setModalOpen(false)}
        footer={
          <>
            <button className="btn btn-ghost" onClick={() => setModalOpen(false)}>
              Cancel
            </button>
            <button className="btn btn-primary" form="duty-form" type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </button>
          </>
        }
      >
        <form id="duty-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          <Field label="Staff" htmlFor="d-staff" required>
            <StaffSelect
              id="d-staff"
              value={form.staffId}
              onChange={(v) => setForm((f) => ({ ...f, staffId: v }))}
              includeBlank
              blankLabel="Select staff…"
            />
          </Field>
          <Field label="Location" htmlFor="d-loc" required>
            <input
              id="d-loc"
              className="input"
              value={form.location}
              onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))}
              required
            />
          </Field>
          <Field label="Purpose" htmlFor="d-purpose">
            <input
              id="d-purpose"
              className="input"
              value={form.purpose}
              onChange={(e) => setForm((f) => ({ ...f, purpose: e.target.value }))}
            />
          </Field>
          <Field label="Duty Date" htmlFor="d-date" required>
            <input
              id="d-date"
              type="date"
              className="input"
              value={form.dutyDate}
              onChange={(e) => setForm((f) => ({ ...f, dutyDate: e.target.value }))}
              required
            />
          </Field>
          <Field label="Start Time" htmlFor="d-start" required>
            <input
              id="d-start"
              type="time"
              className="input"
              value={form.startTime}
              onChange={(e) => setForm((f) => ({ ...f, startTime: e.target.value }))}
              required
            />
          </Field>
          <Field label="End Time" htmlFor="d-end" required>
            <input
              id="d-end"
              type="time"
              className="input"
              value={form.endTime}
              onChange={(e) => setForm((f) => ({ ...f, endTime: e.target.value }))}
              required
            />
          </Field>
          <Field label="Remarks" htmlFor="d-remarks" full>
            <textarea
              id="d-remarks"
              className="textarea"
              rows={2}
              value={form.remarks}
              onChange={(e) => setForm((f) => ({ ...f, remarks: e.target.value }))}
            />
          </Field>
        </form>
      </Modal>
    </>
  );
}
