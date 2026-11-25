import { useState } from 'react';
import { notificationApi } from '@/api/endpoints';
import { toErrorMessage } from '@/api/http';
import { useAsync } from '@/hooks/useAsync';
import { useAuth } from '@/auth/AuthContext';
import { P } from '@/auth/permissions';
import { PageHeader } from '@/components/layout/PageHeader';
import {
  Badge, EmptyState, ErrorAlert, LoadingBlock, SuccessAlert,
} from '@/components/ui/Primitives';
import { Pagination } from '@/components/ui/Pagination';
import { formatDateTime, humanize } from '@/utils/format';

export function NotificationsPage() {
  const { hasPermission } = useAuth();
  const [page, setPage] = useState(0);
  const [actionError, setActionError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [generating, setGenerating] = useState(false);

  const { data, loading, error, reload } = useAsync(
    () => notificationApi.list({ page, size: 15, sort: 'createdAt,desc' }),
    [page],
  );

  const markRead = async (id) => {
    setActionError(null);
    try {
      await notificationApi.markRead(id);
      reload();
    } catch (err) {
      setActionError(toErrorMessage(err));
    }
  };

  const generate = async () => {
    setActionError(null);
    setSuccess(null);
    setGenerating(true);
    try {
      const res = await notificationApi.generate();
      setSuccess(`Created ${res.created} new alert(s).`);
      reload();
    } catch (err) {
      setActionError(toErrorMessage(err));
    } finally {
      setGenerating(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Notifications"
        subtitle="Alerts about releases, movements, interviews and court hearings"
        actions={
          hasPermission(P.NOTIFICATION_READ) && (
            <button className="btn btn-primary" onClick={generate} disabled={generating}>
              {generating ? 'Generating…' : 'Generate alerts now'}
            </button>
          )
        }
      />

      <SuccessAlert message={success} />
      <ErrorAlert message={actionError} />
      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading notifications…" />
      ) : !data || data.content.length === 0 ? (
        <EmptyState message="No notifications." />
      ) : (
        <div className="stack-sm">
          {data.content.map((n) => {
            const unread = n.readFlag === false;
            return (
              <div
                key={n.id}
                className="card"
                style={unread ? { borderLeft: '3px solid var(--color-primary)' } : undefined}
              >
                <div className="card-body">
                  <div className="row row-wrap" style={{ justifyContent: 'space-between' }}>
                    <div className="row row-wrap">
                      <Badge variant="badge-neutral">{humanize(n.type)}</Badge>
                      {unread && <Badge variant="badge-info">NEW</Badge>}
                      <strong style={unread ? { fontWeight: 700 } : { fontWeight: 500 }}>{n.title}</strong>
                    </div>
                    <div className="row row-wrap">
                      <span className="text-muted text-sm">{formatDateTime(n.createdAt)}</span>
                      {unread && (
                        <button className="btn btn-ghost btn-sm" onClick={() => markRead(n.id)}>
                          Mark read
                        </button>
                      )}
                    </div>
                  </div>
                  <p className="text-muted" style={{ margin: '0.35rem 0 0' }}>{n.message}</p>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <Pagination page={data} onPageChange={setPage} />
    </>
  );
}
