import { useMemo, useState } from 'react';
import { userApi, roleApi } from '@/api/endpoints';
import { toErrorMessage } from '@/api/http';
import { useAsync } from '@/hooks/useAsync';
import { useAuth } from '@/auth/AuthContext';
import { P } from '@/auth/permissions';
import { PageHeader } from '@/components/layout/PageHeader';
import {
  Badge, EmptyState, ErrorAlert, LoadingBlock, StatusBadge,
} from '@/components/ui/Primitives';
import { Modal } from '@/components/ui/Modal';
import { Pagination } from '@/components/ui/Pagination';
import { Field } from '@/components/ui/Field';

const PW_HINT = 'Min 8 characters, must contain a letter and a digit.';
const isValidPassword = (pw) => pw.length >= 8 && /[A-Za-z]/.test(pw) && /\d/.test(pw);

const emptyForm = () => ({ username: '', email: '', password: '', fullName: '', roles: [] });

export function UsersPage() {
  const { hasPermission } = useAuth();
  const canWrite = hasPermission(P.USER_WRITE);
  const [page, setPage] = useState(0);

  const { data, loading, error, reload } = useAsync(
    () => userApi.list({ page, size: 10, sort: 'username,asc' }),
    [page],
  );
  const rolesData = useAsync(() => roleApi.list(), []);
  const roleNames = useMemo(() => (rolesData.data ?? []).map((r) => r.name), [rolesData.data]);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm());
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState(null);
  const [actionError, setActionError] = useState(null);

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm());
    setFormError(null);
    setModalOpen(true);
  };

  const openEdit = (u) => {
    setEditing(u);
    setForm({ username: u.username, email: u.email, password: '', fullName: u.fullName, roles: [...u.roles] });
    setFormError(null);
    setModalOpen(true);
  };

  const toggleRole = (name) =>
    setForm((f) => ({
      ...f,
      roles: f.roles.includes(name) ? f.roles.filter((r) => r !== name) : [...f.roles, name],
    }));

  const submit = async (e) => {
    e.preventDefault();
    setFormError(null);
    if (form.roles.length === 0) {
      setFormError('Select at least one role.');
      return;
    }
    if (!editing && !isValidPassword(form.password)) {
      setFormError(PW_HINT);
      return;
    }
    setSaving(true);
    try {
      if (editing) {
        const body = { email: form.email, fullName: form.fullName, roles: form.roles };
        await userApi.update(editing.id, body);
      } else {
        const body = {
          username: form.username,
          email: form.email,
          password: form.password,
          fullName: form.fullName,
          roles: form.roles,
        };
        await userApi.create(body);
      }
      setModalOpen(false);
      reload();
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const toggleActive = async (u) => {
    setActionError(null);
    try {
      if (u.active) await userApi.deactivate(u.id);
      else await userApi.activate(u.id);
      reload();
    } catch (err) {
      setActionError(toErrorMessage(err));
    }
  };

  return (
    <>
      <PageHeader
        title="User Management"
        subtitle="Accounts, roles and access"
        actions={
          canWrite && <button className="btn btn-primary" onClick={openCreate}>+ New User</button>
        }
      />

      <ErrorAlert message={actionError} />
      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading users…" />
      ) : !data || data.content.length === 0 ? (
        <EmptyState message="No users found." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Roles</th>
                <th>Status</th>
                <th className="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((u) => (
                <tr key={u.id}>
                  <td className="mono">{u.username}</td>
                  <td>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td>
                    <div className="row row-wrap">
                      {u.roles.map((r) => <Badge key={r} variant="badge-primary">{r}</Badge>)}
                    </div>
                  </td>
                  <td><StatusBadge status={u.active ? 'ACTIVE' : 'INACTIVE'} /></td>
                  <td className="actions text-right">
                    {canWrite && (
                      <>
                        <button className="btn btn-ghost btn-sm" onClick={() => openEdit(u)}>Edit</button>
                        <button
                          className={`btn btn-sm ${u.active ? 'btn-danger' : 'btn-secondary'}`}
                          onClick={() => toggleActive(u)}
                        >
                          {u.active ? 'Deactivate' : 'Activate'}
                        </button>
                      </>
                    )}
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
        title={editing ? `Edit User — ${editing.username}` : 'New User'}
        onClose={() => setModalOpen(false)}
        wide
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setModalOpen(false)} disabled={saving}>Cancel</button>
            <button className="btn btn-primary" form="user-form" type="submit" disabled={saving}>
              {saving ? 'Saving…' : editing ? 'Save Changes' : 'Create User'}
            </button>
          </>
        }
      >
        <form id="user-form" className="form-grid" onSubmit={submit}>
          <ErrorAlert message={formError} />
          {!editing && (
            <Field label="Username" required>
              <input className="input" value={form.username} onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))} required />
            </Field>
          )}
          <Field label="Full Name" required>
            <input className="input" value={form.fullName} onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))} required />
          </Field>
          <Field label="Email" required>
            <input className="input" type="email" value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} required />
          </Field>
          {!editing && (
            <Field label="Password" required hint={PW_HINT}>
              <input className="input" type="password" value={form.password} onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))} required />
            </Field>
          )}
          <Field label="Roles" required full hint="Select at least one role.">
            {rolesData.loading ? (
              <span className="text-muted">Loading roles…</span>
            ) : (
              <div className="row row-wrap">
                {roleNames.map((name) => (
                  <label key={name} className="row" style={{ gap: 6 }}>
                    <input
                      type="checkbox"
                      checked={form.roles.includes(name)}
                      onChange={() => toggleRole(name)}
                    />
                    {name}
                  </label>
                ))}
              </div>
            )}
          </Field>
        </form>
      </Modal>
    </>
  );
}
