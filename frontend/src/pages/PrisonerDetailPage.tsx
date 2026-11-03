import { useState, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { prisonerApi, releaseApi, articleApi } from '@/api/endpoints';
import { toErrorMessage } from '@/api/http';
import type {
  Prisoner, CaseRecord, SurrenderedArticle, ReleaseRecord, PrisonerStatus, CaseStatus,
} from '@/types';
import { useAsync } from '@/hooks/useAsync';
import { useAuth } from '@/auth/AuthContext';
import { P } from '@/auth/permissions';
import { PageHeader } from '@/components/layout/PageHeader';
import {
  Card, Detail, EmptyState, ErrorAlert, LoadingBlock, StatusBadge,
} from '@/components/ui/Primitives';
import { Modal } from '@/components/ui/Modal';
import { Field, EnumSelect } from '@/components/ui/Field';
import { formatDate, fullName, humanize, todayISO } from '@/utils/format';

const PRISONER_STATUSES: PrisonerStatus[] = ['ACTIVE', 'RELEASED', 'TRANSFERRED', 'DECEASED', 'INACTIVE'];
const CASE_STATUSES: CaseStatus[] = ['UNDER_INVESTIGATION', 'UNDER_TRIAL', 'CONVICTED', 'ACQUITTED', 'CLOSED'];

type TabKey = 'overview' | 'cases' | 'articles' | 'releases' | 'history';
const TABS: { key: TabKey; label: string }[] = [
  { key: 'overview', label: 'Overview' },
  { key: 'cases', label: 'Cases' },
  { key: 'articles', label: 'Articles' },
  { key: 'releases', label: 'Releases' },
  { key: 'history', label: 'History' },
];

interface HistoryMovement {
  id: number;
  movementType?: string;
  destination?: string;
  movementDate?: string;
  status?: string;
}
interface PrisonerHistory {
  prisonerId: number;
  prisonerNumber: string;
  prisonerName: string;
  status: string;
  cases: CaseRecord[];
  articles: SurrenderedArticle[];
  releases: ReleaseRecord[];
  movements: HistoryMovement[];
}

export function PrisonerDetailPage() {
  const { id } = useParams();
  const prisonerId = Number(id);
  const navigate = useNavigate();
  const [tab, setTab] = useState<TabKey>('overview');

  const prisoner = useAsync(() => prisonerApi.get(prisonerId), [prisonerId]);

  return (
    <>
      <PageHeader
        title={prisoner.data ? fullName(prisoner.data.firstName, prisoner.data.lastName) : 'Prisoner'}
        subtitle={prisoner.data ? `Prisoner #${prisoner.data.prisonerNumber}` : undefined}
        actions={<button className="btn btn-secondary" onClick={() => navigate('/prisoners')}>← Back</button>}
      />

      <ErrorAlert message={prisoner.error} />

      {prisoner.loading ? (
        <LoadingBlock label="Loading prisoner…" />
      ) : !prisoner.data ? (
        <EmptyState message="Prisoner not found." />
      ) : (
        <>
          <div className="tabs">
            {TABS.map((t) => (
              <button
                key={t.key}
                className={`tab ${tab === t.key ? 'active' : ''}`}
                onClick={() => setTab(t.key)}
              >
                {t.label}
              </button>
            ))}
          </div>

          {tab === 'overview' && (
            <OverviewTab prisoner={prisoner.data} onChanged={prisoner.reload} onDeleted={() => navigate('/prisoners')} />
          )}
          {tab === 'cases' && <CasesTab prisonerId={prisonerId} />}
          {tab === 'articles' && <ArticlesTab prisonerId={prisonerId} />}
          {tab === 'releases' && <ReleasesTab prisonerId={prisonerId} />}
          {tab === 'history' && <HistoryTab prisonerId={prisonerId} />}
        </>
      )}
    </>
  );
}

// ---------------- Overview ----------------
function OverviewTab({ prisoner, onChanged, onDeleted }: { prisoner: Prisoner; onChanged: () => void; onDeleted: () => void }) {
  const { hasPermission } = useAuth();
  const [editOpen, setEditOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [form, setForm] = useState<Partial<Prisoner>>({});
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const openEdit = () => {
    setForm({ ...prisoner });
    setFormError(null);
    setEditOpen(true);
  };
  const set = (k: keyof Prisoner, v: string) => setForm((f) => ({ ...f, [k]: v }));

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    try {
      await prisonerApi.update(prisoner.id, form);
      setEditOpen(false);
      onChanged();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const doDelete = async () => {
    setSaving(true);
    setFormError(null);
    try {
      await prisonerApi.remove(prisoner.id);
      onDeleted();
    } catch (err) {
      setFormError(toErrorMessage(err));
      setSaving(false);
    }
  };

  return (
    <Card
      title="Overview"
      actions={
        <div className="row">
          {hasPermission(P.PRISONER_WRITE) && <button className="btn btn-secondary btn-sm" onClick={openEdit}>Edit</button>}
          {hasPermission(P.PRISONER_DELETE) && <button className="btn btn-danger btn-sm" onClick={() => { setFormError(null); setDeleteOpen(true); }}>Delete</button>}
        </div>
      }
    >
      <div className="detail-grid">
        <Detail label="Prisoner Number"><span className="mono">{prisoner.prisonerNumber}</span></Detail>
        <Detail label="Name">{fullName(prisoner.firstName, prisoner.lastName)}</Detail>
        <Detail label="Status"><StatusBadge status={prisoner.status} /></Detail>
        <Detail label="Date of Birth">{formatDate(prisoner.dateOfBirth)}</Detail>
        <Detail label="Gender">{prisoner.gender || '—'}</Detail>
        <Detail label="Address">{prisoner.address || '—'}</Detail>
        <Detail label="Emergency Contact">{prisoner.emergencyContact || '—'}</Detail>
        <Detail label="ID Type">{prisoner.idType || '—'}</Detail>
        <Detail label="ID Number">{prisoner.idNumber || '—'}</Detail>
        <Detail label="Admission Date">{formatDate(prisoner.admissionDate)}</Detail>
        <Detail label="Expected Release">{formatDate(prisoner.expectedReleaseDate)}</Detail>
        <Detail label="Facility">{prisoner.facility || '—'}</Detail>
        <Detail label="Ward">{prisoner.ward || '—'}</Detail>
        <Detail label="Cell">{prisoner.cell || '—'}</Detail>
        <Detail label="Active">{prisoner.active ? 'Yes' : 'No'}</Detail>
      </div>

      <Modal
        open={editOpen}
        title="Edit Prisoner"
        onClose={() => setEditOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setEditOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-primary" form="prisoner-edit-form" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
          </>
        }
      >
        <form id="prisoner-edit-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          <Field label="Prisoner Number" required>
            <input className="input" value={form.prisonerNumber ?? ''} onChange={(e) => set('prisonerNumber', e.target.value)} required />
          </Field>
          <Field label="Status">
            <EnumSelect value={form.status ?? 'ACTIVE'} onChange={(v) => set('status', v)} options={PRISONER_STATUSES} />
          </Field>
          <Field label="First Name" required>
            <input className="input" value={form.firstName ?? ''} onChange={(e) => set('firstName', e.target.value)} required />
          </Field>
          <Field label="Last Name" required>
            <input className="input" value={form.lastName ?? ''} onChange={(e) => set('lastName', e.target.value)} required />
          </Field>
          <Field label="Date of Birth">
            <input className="input" type="date" value={form.dateOfBirth ?? ''} onChange={(e) => set('dateOfBirth', e.target.value)} />
          </Field>
          <Field label="Gender">
            <input className="input" value={form.gender ?? ''} onChange={(e) => set('gender', e.target.value)} />
          </Field>
          <Field label="Address" full>
            <input className="input" value={form.address ?? ''} onChange={(e) => set('address', e.target.value)} />
          </Field>
          <Field label="Emergency Contact">
            <input className="input" value={form.emergencyContact ?? ''} onChange={(e) => set('emergencyContact', e.target.value)} />
          </Field>
          <Field label="ID Type">
            <input className="input" value={form.idType ?? ''} onChange={(e) => set('idType', e.target.value)} />
          </Field>
          <Field label="ID Number">
            <input className="input" value={form.idNumber ?? ''} onChange={(e) => set('idNumber', e.target.value)} />
          </Field>
          <Field label="Admission Date" required>
            <input className="input" type="date" value={form.admissionDate ?? ''} onChange={(e) => set('admissionDate', e.target.value)} required />
          </Field>
          <Field label="Expected Release Date">
            <input className="input" type="date" value={form.expectedReleaseDate ?? ''} onChange={(e) => set('expectedReleaseDate', e.target.value)} />
          </Field>
          <Field label="Facility">
            <input className="input" value={form.facility ?? ''} onChange={(e) => set('facility', e.target.value)} />
          </Field>
          <Field label="Ward">
            <input className="input" value={form.ward ?? ''} onChange={(e) => set('ward', e.target.value)} />
          </Field>
          <Field label="Cell">
            <input className="input" value={form.cell ?? ''} onChange={(e) => set('cell', e.target.value)} />
          </Field>
        </form>
      </Modal>

      <Modal
        open={deleteOpen}
        title="Delete Prisoner"
        onClose={() => setDeleteOpen(false)}
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setDeleteOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-danger" onClick={doDelete} disabled={saving}>{saving ? 'Deleting…' : 'Delete'}</button>
          </>
        }
      >
        <ErrorAlert message={formError} />
        <p>Are you sure you want to delete <strong>{fullName(prisoner.firstName, prisoner.lastName)}</strong>? This performs a soft delete.</p>
      </Modal>
    </Card>
  );
}

// ---------------- Cases ----------------
function CasesTab({ prisonerId }: { prisonerId: number }) {
  const { hasPermission } = useAuth();
  const { data, loading, error, reload } = useAsync(() => prisonerApi.cases(prisonerId), [prisonerId]);
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [form, setForm] = useState({
    caseNumber: '', firNumber: '', crimeType: '', crimeDescription: '', policeStation: '',
    court: '', sections: '', arrestDate: '', status: 'UNDER_INVESTIGATION', investigatingOfficer: '', remarks: '',
  });
  const set = (k: keyof typeof form, v: string) => setForm((f) => ({ ...f, [k]: v }));

  const openAdd = () => {
    setForm({
      caseNumber: '', firNumber: '', crimeType: '', crimeDescription: '', policeStation: '',
      court: '', sections: '', arrestDate: '', status: 'UNDER_INVESTIGATION', investigatingOfficer: '', remarks: '',
    });
    setFormError(null);
    setOpen(true);
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    try {
      const body = Object.fromEntries(Object.entries(form).filter(([, v]) => v !== '')) as Partial<CaseRecord>;
      await prisonerApi.addCase(prisonerId, body);
      setOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card
      title="Cases"
      actions={hasPermission(P.CASE_WRITE) && <button className="btn btn-primary btn-sm" onClick={openAdd}>+ Add Case</button>}
    >
      <ErrorAlert message={error} />
      {loading ? (
        <LoadingBlock label="Loading cases…" />
      ) : !data || data.length === 0 ? (
        <EmptyState message="No cases recorded." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>Case #</th><th>Crime Type</th><th>Court</th><th>Status</th><th>Arrest Date</th></tr>
            </thead>
            <tbody>
              {data.map((c) => (
                <tr key={c.id}>
                  <td className="mono">{c.caseNumber}</td>
                  <td>{c.crimeType}</td>
                  <td>{c.court || '—'}</td>
                  <td><StatusBadge status={c.status} /></td>
                  <td>{formatDate(c.arrestDate)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        open={open}
        title="Add Case"
        onClose={() => setOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-primary" form="case-form" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Add Case'}</button>
          </>
        }
      >
        <form id="case-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          <Field label="Crime Type" required>
            <input className="input" value={form.crimeType} onChange={(e) => set('crimeType', e.target.value)} required />
          </Field>
          <Field label="Status">
            <EnumSelect value={form.status} onChange={(v) => set('status', v)} options={CASE_STATUSES} />
          </Field>
          <Field label="Case Number">
            <input className="input" value={form.caseNumber} onChange={(e) => set('caseNumber', e.target.value)} />
          </Field>
          <Field label="FIR Number">
            <input className="input" value={form.firNumber} onChange={(e) => set('firNumber', e.target.value)} />
          </Field>
          <Field label="Crime Description" full>
            <textarea className="textarea" value={form.crimeDescription} onChange={(e) => set('crimeDescription', e.target.value)} />
          </Field>
          <Field label="Police Station">
            <input className="input" value={form.policeStation} onChange={(e) => set('policeStation', e.target.value)} />
          </Field>
          <Field label="Court">
            <input className="input" value={form.court} onChange={(e) => set('court', e.target.value)} />
          </Field>
          <Field label="Sections">
            <input className="input" value={form.sections} onChange={(e) => set('sections', e.target.value)} />
          </Field>
          <Field label="Arrest Date">
            <input className="input" type="date" value={form.arrestDate} onChange={(e) => set('arrestDate', e.target.value)} />
          </Field>
          <Field label="Investigating Officer">
            <input className="input" value={form.investigatingOfficer} onChange={(e) => set('investigatingOfficer', e.target.value)} />
          </Field>
          <Field label="Remarks" full>
            <textarea className="textarea" value={form.remarks} onChange={(e) => set('remarks', e.target.value)} />
          </Field>
        </form>
      </Modal>
    </Card>
  );
}

// ---------------- Articles ----------------
function ArticlesTab({ prisonerId }: { prisonerId: number }) {
  const { hasPermission } = useAuth();
  const { data, loading, error, reload } = useAsync(() => prisonerApi.articles(prisonerId), [prisonerId]);
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [rowError, setRowError] = useState<string | null>(null);
  const [form, setForm] = useState({
    name: '', category: '', quantity: '1', description: '', conditionNote: '', receivedDate: todayISO(), remarks: '',
  });
  const set = (k: keyof typeof form, v: string) => setForm((f) => ({ ...f, [k]: v }));

  const openAdd = () => {
    setForm({ name: '', category: '', quantity: '1', description: '', conditionNote: '', receivedDate: todayISO(), remarks: '' });
    setFormError(null);
    setOpen(true);
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setFormError(null);
    try {
      const { quantity, ...rest } = form;
      const body: Partial<SurrenderedArticle> = {
        ...Object.fromEntries(Object.entries(rest).filter(([, v]) => v !== '')),
        quantity: Number(quantity) || 1,
      };
      await prisonerApi.addArticle(prisonerId, body);
      setOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const markReturned = async (articleId: number) => {
    setRowError(null);
    try {
      await articleApi.markReturned(articleId);
      reload();
    } catch (err) {
      setRowError(toErrorMessage(err));
    }
  };

  return (
    <Card
      title="Articles"
      actions={hasPermission(P.ARTICLE_WRITE) && <button className="btn btn-primary btn-sm" onClick={openAdd}>+ Add Article</button>}
    >
      <ErrorAlert message={error} />
      <ErrorAlert message={rowError} />
      {loading ? (
        <LoadingBlock label="Loading articles…" />
      ) : !data || data.length === 0 ? (
        <EmptyState message="No surrendered articles." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>Name</th><th>Category</th><th>Qty</th><th>Return Status</th><th>Received</th><th>Returned</th><th className="text-right">Actions</th></tr>
            </thead>
            <tbody>
              {data.map((a) => (
                <tr key={a.id}>
                  <td>{a.name}</td>
                  <td>{a.category || '—'}</td>
                  <td>{a.quantity}</td>
                  <td><StatusBadge status={a.returnStatus} /></td>
                  <td>{formatDate(a.receivedDate)}</td>
                  <td>{formatDate(a.returnedDate)}</td>
                  <td className="actions text-right">
                    {a.returnStatus === 'HELD' && hasPermission(P.ARTICLE_WRITE) && (
                      <button className="btn btn-secondary btn-sm" onClick={() => markReturned(a.id)}>Mark Returned</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        open={open}
        title="Add Article"
        onClose={() => setOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-primary" form="article-form" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Add Article'}</button>
          </>
        }
      >
        <form id="article-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          <Field label="Name" required>
            <input className="input" value={form.name} onChange={(e) => set('name', e.target.value)} required />
          </Field>
          <Field label="Category">
            <input className="input" value={form.category} onChange={(e) => set('category', e.target.value)} />
          </Field>
          <Field label="Quantity" required>
            <input className="input" type="number" min={1} value={form.quantity} onChange={(e) => set('quantity', e.target.value)} required />
          </Field>
          <Field label="Received Date" required>
            <input className="input" type="date" value={form.receivedDate} onChange={(e) => set('receivedDate', e.target.value)} required />
          </Field>
          <Field label="Description" full>
            <textarea className="textarea" value={form.description} onChange={(e) => set('description', e.target.value)} />
          </Field>
          <Field label="Condition Note">
            <input className="input" value={form.conditionNote} onChange={(e) => set('conditionNote', e.target.value)} />
          </Field>
          <Field label="Remarks" full>
            <textarea className="textarea" value={form.remarks} onChange={(e) => set('remarks', e.target.value)} />
          </Field>
        </form>
      </Modal>
    </Card>
  );
}

// ---------------- Releases ----------------
function ReleasesTab({ prisonerId }: { prisonerId: number }) {
  const { data, loading, error } = useAsync(() => releaseApi.byPrisoner(prisonerId), [prisonerId]);
  return (
    <Card title="Releases">
      <ErrorAlert message={error} />
      {loading ? (
        <LoadingBlock label="Loading releases…" />
      ) : !data || data.length === 0 ? (
        <EmptyState message="No release records." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr><th>Expected Release</th><th>Type</th><th>Status</th><th>Actual Release</th></tr>
            </thead>
            <tbody>
              {data.map((r) => (
                <tr key={r.id}>
                  <td>{formatDate(r.expectedReleaseDate)}</td>
                  <td>{humanize(r.releaseType)}</td>
                  <td><StatusBadge status={r.status} /></td>
                  <td>{formatDate(r.actualReleaseDate)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
}

// ---------------- History ----------------
function HistoryTab({ prisonerId }: { prisonerId: number }) {
  const { data, loading, error } = useAsync(
    () => prisonerApi.history(prisonerId).then((d) => d as PrisonerHistory),
    [prisonerId],
  );

  if (loading) return <LoadingBlock label="Loading history…" />;
  if (error) return <ErrorAlert message={error} />;
  if (!data) return <EmptyState message="No history available." />;

  return (
    <div className="stack">
      <Card title="Cases">
        {data.cases?.length ? (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Case #</th><th>Crime Type</th><th>Status</th><th>Arrest Date</th></tr></thead>
              <tbody>
                {data.cases.map((c) => (
                  <tr key={c.id}>
                    <td className="mono">{c.caseNumber}</td>
                    <td>{c.crimeType}</td>
                    <td><StatusBadge status={c.status} /></td>
                    <td>{formatDate(c.arrestDate)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : <p className="text-muted">No cases.</p>}
      </Card>

      <Card title="Articles">
        {data.articles?.length ? (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Name</th><th>Qty</th><th>Return Status</th><th>Received</th></tr></thead>
              <tbody>
                {data.articles.map((a) => (
                  <tr key={a.id}>
                    <td>{a.name}</td>
                    <td>{a.quantity}</td>
                    <td><StatusBadge status={a.returnStatus} /></td>
                    <td>{formatDate(a.receivedDate)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : <p className="text-muted">No articles.</p>}
      </Card>

      <Card title="Releases">
        {data.releases?.length ? (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Expected Release</th><th>Type</th><th>Status</th><th>Actual Release</th></tr></thead>
              <tbody>
                {data.releases.map((r) => (
                  <tr key={r.id}>
                    <td>{formatDate(r.expectedReleaseDate)}</td>
                    <td>{humanize(r.releaseType)}</td>
                    <td><StatusBadge status={r.status} /></td>
                    <td>{formatDate(r.actualReleaseDate)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : <p className="text-muted">No releases.</p>}
      </Card>

      <Card title="Movements">
        {data.movements?.length ? (
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th>Type</th><th>Destination</th><th>Date</th><th>Status</th></tr></thead>
              <tbody>
                {data.movements.map((m) => (
                  <tr key={m.id}>
                    <td>{humanize(m.movementType)}</td>
                    <td>{m.destination || '—'}</td>
                    <td>{formatDate(m.movementDate)}</td>
                    <td><StatusBadge status={m.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : <p className="text-muted">No movements.</p>}
      </Card>
    </div>
  );
}
