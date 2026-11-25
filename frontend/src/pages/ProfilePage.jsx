import { useState } from 'react';
import { userApi, authApi } from '@/api/endpoints';
import { toErrorMessage } from '@/api/http';
import { useAsync } from '@/hooks/useAsync';
import { useAuth } from '@/auth/AuthContext';
import {
  Badge, Card, Detail, ErrorAlert, LoadingBlock, StatusBadge, SuccessAlert,
} from '@/components/ui/Primitives';
import { PageHeader } from '@/components/layout/PageHeader';
import { Field } from '@/components/ui/Field';
import { humanize } from '@/utils/format';

const PW_HINT = 'Min 8 characters, must contain a letter and a digit.';
const isValidPassword = (pw) => pw.length >= 8 && /[A-Za-z]/.test(pw) && /\d/.test(pw);

export function ProfilePage() {
  const { user } = useAuth();
  const { data, loading, error } = useAsync(() => userApi.me(), []);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState(null);
  const [success, setSuccess] = useState(null);

  const submit = async (e) => {
    e.preventDefault();
    setFormError(null);
    setSuccess(null);
    if (!isValidPassword(newPassword)) {
      setFormError(PW_HINT);
      return;
    }
    if (newPassword !== confirmNewPassword) {
      setFormError('New password and confirmation do not match.');
      return;
    }
    setSaving(true);
    try {
      await authApi.changePassword({ currentPassword, newPassword });
      setSuccess('Password changed.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmNewPassword('');
    } catch (err) {
      setFormError(toErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageHeader title="My Profile" subtitle="Your account, access and password" />

      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading profile…" />
      ) : (
        <div className="stack">
          <Card title="Account">
            <div className="detail-grid">
              <Detail label="Username">{data?.username}</Detail>
              <Detail label="Full Name">{data?.fullName}</Detail>
              <Detail label="Email">{data?.email}</Detail>
              <Detail label="Status">
                <StatusBadge status={data?.active ? 'ACTIVE' : 'INACTIVE'} />
              </Detail>
              <Detail label="Roles">
                <div className="row row-wrap">
                  {(data?.roles ?? []).map((r) => <Badge key={r} variant="badge-primary">{r}</Badge>)}
                </div>
              </Detail>
            </div>
          </Card>

          <Card title="My Access">
            <p className="text-muted" style={{ marginTop: 0 }}>Permissions granted to your account.</p>
            <div className="row row-wrap">
              {(user?.permissions ?? []).length === 0 ? (
                <span className="text-muted">No permissions.</span>
              ) : (
                [...(user?.permissions ?? [])].sort().map((perm) => (
                  <Badge key={perm} variant="badge-neutral">
                    <span className="mono">{perm}</span> — {humanize(perm)}
                  </Badge>
                ))
              )}
            </div>
          </Card>

          <Card title="Change Password">
            <form className="form-grid" onSubmit={submit}>
              <SuccessAlert message={success} />
              <ErrorAlert message={formError} />
              <Field label="Current Password" required full>
                <input className="input" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required autoComplete="current-password" />
              </Field>
              <Field label="New Password" required hint={PW_HINT}>
                <input className="input" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required autoComplete="new-password" />
              </Field>
              <Field label="Confirm New Password" required>
                <input className="input" type="password" value={confirmNewPassword} onChange={(e) => setConfirmNewPassword(e.target.value)} required autoComplete="new-password" />
              </Field>
              <div className="field full">
                <button className="btn btn-primary" type="submit" disabled={saving}>
                  {saving ? 'Saving…' : 'Change Password'}
                </button>
              </div>
            </form>
          </Card>
        </div>
      )}
    </>
  );
}
