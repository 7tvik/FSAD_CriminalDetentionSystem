import { useState } from 'react';
import { Link } from 'react-router-dom';
import { prisonerApi } from '@/api/endpoints';
import { toErrorMessage } from '@/api/http';
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
import { formatDate, fullName, todayISO } from '@/utils/format';

const PRISONER_STATUSES = ['ACTIVE', 'RELEASED', 'TRANSFERRED', 'DECEASED', 'INACTIVE'];

const emptyForm = () => ({
  prisonerNumber: '',
  firstName: '',
  lastName: '',
  dateOfBirth: '',
  gender: '',
  address: '',
  emergencyContact: '',
  idType: '',
  idNumber: '',
  admissionDate: todayISO(),
  status: 'ACTIVE',
  facility: '',
  ward: '',
  cell: '',
  expectedReleaseDate: '',
});

export function PrisonersPage() {
  const { hasPermission } = useAuth();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [status, setStatus] = useState('');

  const { data, loading, error, reload } = useAsync(
    () => prisonerApi.search({ page, size: 10, sort: 'createdAt,desc', search, status }),
    [page, search, status],
  );

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm());
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  const set = (k, v) => {
    setForm((f) => ({ ...f, [k]: v }));
    setFieldErrors((e) => ({ ...e, [k]: undefined }));
  };

  const validate = (f) => {
    const errs = {};
    const today = todayISO();

    if (f.dateOfBirth) {
      if (f.dateOfBirth >= today) {
        errs.dateOfBirth = 'Date of birth must be in the past.';
      } else if (f.admissionDate && f.dateOfBirth >= f.admissionDate) {
        errs.dateOfBirth = 'Date of birth must be before admission date.';
      } else if (f.admissionDate) {
        const ageAtAdmission = (new Date(f.admissionDate) - new Date(f.dateOfBirth)) / (1000 * 60 * 60 * 24 * 365.25);
        if (ageAtAdmission < 18) {
          errs.dateOfBirth = 'Prisoner must be at least 18 years old at time of admission.';
        }
      }
    }

    if (f.admissionDate && f.admissionDate > today) {
      errs.admissionDate = 'Admission date cannot be in the future.';
    }

    if (f.expectedReleaseDate && f.admissionDate && f.expectedReleaseDate <= f.admissionDate) {
      errs.expectedReleaseDate = 'Expected release date must be after admission date.';
    }

    return errs;
  };

  const openCreate = () => {
    setForm(emptyForm());
    setFormError(null);
    setFieldErrors({});
    setModalOpen(true);
  };

  const submitSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setSearch(searchInput.trim());
  };

  const submit = async (e) => {
    e.preventDefault();
    const errs = validate(form);
    if (Object.keys(errs).length > 0) {
      setFieldErrors(errs);
      return;
    }
    setSaving(true);
    setFormError(null);
    try {
      const body = Object.fromEntries(
        Object.entries(form).filter(([, v]) => v !== ''),
      );
      await prisonerApi.create(body);
      setModalOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Case Register"
        subtitle="Prisoners, their cases and surrendered articles"
        actions={
          hasPermission(P.PRISONER_WRITE) && (
            <button className="btn btn-primary" onClick={openCreate}>+ New Prisoner</button>
          )
        }
      />

      <form className="toolbar" onSubmit={submitSearch}>
        <input
          className="input"
          placeholder="Search name or number…"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          style={{ maxWidth: 260 }}
        />
        <EnumSelect
          value={status}
          onChange={(v) => { setPage(0); setStatus(v); }}
          options={PRISONER_STATUSES}
          includeBlank
          blankLabel="All statuses"
        />
        <button className="btn btn-secondary" type="submit">Search</button>
        <span className="spacer" />
      </form>

      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading prisoners…" />
      ) : !data || data.content.length === 0 ? (
        <EmptyState message="No prisoners found." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Prisoner #</th>
                <th>Name</th>
                <th>Status</th>
                <th>Facility</th>
                <th>Admission Date</th>
                <th>Expected Release</th>
                <th className="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((p) => (
                <tr key={p.id}>
                  <td className="mono">{p.prisonerNumber}</td>
                  <td><Link to={`/prisoners/${p.id}`}>{fullName(p.firstName, p.lastName)}</Link></td>
                  <td><StatusBadge status={p.status} /></td>
                  <td>{p.facility || '—'}</td>
                  <td>{formatDate(p.admissionDate)}</td>
                  <td>{formatDate(p.expectedReleaseDate)}</td>
                  <td className="actions text-right">
                    <Link className="btn btn-ghost btn-sm" to={`/prisoners/${p.id}`}>View</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={data} onPageChange={setPage} />

      <Modal
        open={modalOpen}
        title="New Prisoner"
        onClose={() => setModalOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setModalOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-primary" form="prisoner-form" type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Create Prisoner'}
            </button>
          </>
        }
      >
        <form id="prisoner-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          <Field label="Prisoner Number" required>
            <input className="input" value={form.prisonerNumber} onChange={(e) => set('prisonerNumber', e.target.value)} required />
          </Field>
          <Field label="Status">
            <EnumSelect value={form.status} onChange={(v) => set('status', v)} options={PRISONER_STATUSES} />
          </Field>
          <Field label="First Name" required>
            <input className="input" value={form.firstName} onChange={(e) => set('firstName', e.target.value)} required />
          </Field>
          <Field label="Last Name" required>
            <input className="input" value={form.lastName} onChange={(e) => set('lastName', e.target.value)} required />
          </Field>
          <Field label="Date of Birth" error={fieldErrors.dateOfBirth}>
            <input className="input" type="date" value={form.dateOfBirth} onChange={(e) => set('dateOfBirth', e.target.value)} max={todayISO()} />
          </Field>
          <Field label="Gender">
            <input className="input" value={form.gender} onChange={(e) => set('gender', e.target.value)} />
          </Field>
          <Field label="Address" full>
            <input className="input" value={form.address} onChange={(e) => set('address', e.target.value)} />
          </Field>
          <Field label="Emergency Contact">
            <input className="input" value={form.emergencyContact} onChange={(e) => set('emergencyContact', e.target.value)} />
          </Field>
          <Field label="ID Type">
            <input className="input" value={form.idType} onChange={(e) => set('idType', e.target.value)} />
          </Field>
          <Field label="ID Number">
            <input className="input" value={form.idNumber} onChange={(e) => set('idNumber', e.target.value)} />
          </Field>
          <Field label="Admission Date" required error={fieldErrors.admissionDate}>
            <input className="input" type="date" value={form.admissionDate} onChange={(e) => set('admissionDate', e.target.value)} required max={todayISO()} />
          </Field>
          <Field label="Expected Release Date" error={fieldErrors.expectedReleaseDate}>
            <input className="input" type="date" value={form.expectedReleaseDate} onChange={(e) => set('expectedReleaseDate', e.target.value)} min={form.admissionDate || undefined} />
          </Field>
          <Field label="Facility">
            <input className="input" value={form.facility} onChange={(e) => set('facility', e.target.value)} />
          </Field>
          <Field label="Ward">
            <input className="input" value={form.ward} onChange={(e) => set('ward', e.target.value)} />
          </Field>
          <Field label="Cell">
            <input className="input" value={form.cell} onChange={(e) => set('cell', e.target.value)} />
          </Field>
        </form>
      </Modal>
    </>
  );
}
