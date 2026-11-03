import { roleApi } from '@/api/endpoints';
import { useAsync } from '@/hooks/useAsync';
import { PageHeader } from '@/components/layout/PageHeader';
import {
  Badge, Card, ErrorAlert, LoadingBlock,
} from '@/components/ui/Primitives';
import { humanize } from '@/utils/format';

export function RolesPage() {
  const roles = useAsync(() => roleApi.list(), []);
  const permissions = useAsync(() => roleApi.permissions(), []);

  const loading = roles.loading || permissions.loading;

  return (
    <>
      <PageHeader
        title="Roles & Permissions"
        subtitle="Reference view of roles and the permissions they grant (read-only)"
      />

      <ErrorAlert message={roles.error} />
      <ErrorAlert message={permissions.error} />

      {loading ? (
        <LoadingBlock label="Loading roles…" />
      ) : (
        <div className="stack">
          {(roles.data ?? []).map((role) => (
            <Card key={role.id} title={role.name}>
              {role.description && <p className="text-muted" style={{ marginTop: 0 }}>{role.description}</p>}
              <div className="row row-wrap">
                {[...role.permissions].sort().map((perm) => (
                  <Badge key={perm} variant="badge-primary">{perm}</Badge>
                ))}
              </div>
            </Card>
          ))}

          <Card title="All Permissions">
            <p className="text-muted" style={{ marginTop: 0 }}>
              The complete set of permissions defined in the system.
            </p>
            <div className="row row-wrap">
              {[...(permissions.data ?? [])].sort().map((perm) => (
                <Badge key={perm} variant="badge-neutral">
                  <span className="mono">{perm}</span> — {humanize(perm)}
                </Badge>
              ))}
            </div>
          </Card>
        </div>
      )}
    </>
  );
}
