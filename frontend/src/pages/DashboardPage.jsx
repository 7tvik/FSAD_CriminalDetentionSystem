import {
  Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { Link } from 'react-router-dom';
import { dashboardApi, reportApi } from '@/api/endpoints';
import { useAsync } from '@/hooks/useAsync';
import { PageHeader } from '@/components/layout/PageHeader';
import { Card, ErrorAlert, LoadingBlock } from '@/components/ui/Primitives';

const PIE_COLORS = ['#1f4e79', '#15803d', '#b45309', '#b91c1c', '#64748b'];

const KPIS = [
  { label: 'Active Prisoners', key: 'activePrisoners', to: '/prisoners' },
  { label: 'Total Prisoners', key: 'totalPrisoners', to: '/prisoners' },
  { label: 'Open Cases', key: 'activeCases', to: '/prisoners' },
  { label: "Today's Releases", key: 'todaysReleases', to: '/release-diary', accent: 'accent-success' },
  { label: 'Releases This Week', key: 'thisWeekReleases', to: '/release-diary', accent: 'accent-success' },
  { label: 'Upcoming Releases (30d)', key: 'upcomingReleases', to: '/releases' },
  { label: 'Pending Interviews', key: 'pendingInterviews', to: '/interviews', accent: 'accent-warning' },
  { label: 'Currently Out', key: 'currentlyOut', to: '/movements' },
  { label: 'Overdue Movements', key: 'overdueMovements', to: '/movements', accent: 'accent-danger' },
  { label: "Today's Court Movements", key: 'todaysCourtMovements', to: '/court' },
];

export function DashboardPage() {
  const stats = useAsync(() => dashboardApi.get(), []);
  const prisonerStats = useAsync(() => reportApi.prisonerStats(), []);
  const caseStats = useAsync(() => reportApi.caseStats(), []);

  return (
    <>
      <PageHeader title="Dashboard" subtitle="Operational overview of the facility" />
      <ErrorAlert message={stats.error} />

      {stats.loading || !stats.data ? (
        <LoadingBlock label="Loading dashboard…" />
      ) : (
        <div className="stack">
          <div className="stat-grid">
            {KPIS.map((kpi) => {
              const value = stats.data[kpi.key];
              const inner = (
                <>
                  <div className="stat-label">{kpi.label}</div>
                  <div className="stat-value">{value ?? 0}</div>
                </>
              );
              return kpi.to ? (
                <Link key={kpi.key} to={kpi.to} className={`stat-card ${kpi.accent ?? ''}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                  {inner}
                </Link>
              ) : (
                <div key={kpi.key} className={`stat-card ${kpi.accent ?? ''}`}>{inner}</div>
              );
            })}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '1rem' }}>
            <Card title="Prisoners by Status">
              {prisonerStats.data && prisonerStats.data.some((s) => s.value > 0) ? (
                <ResponsiveContainer width="100%" height={260}>
                  <PieChart>
                    <Pie
                      data={prisonerStats.data.filter((s) => s.value > 0)}
                      dataKey="value"
                      nameKey="label"
                      cx="50%"
                      cy="50%"
                      outerRadius={90}
                      label={(e) => `${e.label}: ${e.value}`}
                    >
                      {prisonerStats.data.filter((s) => s.value > 0).map((_, i) => (
                        <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <p className="text-muted">No data.</p>
              )}
            </Card>

            <Card title="Cases by Status">
              {caseStats.data && caseStats.data.some((s) => s.value > 0) ? (
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={caseStats.data}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="label" tick={{ fontSize: 11 }} angle={-15} textAnchor="end" height={60} />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="value" name="Cases" fill="#1f4e79" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <p className="text-muted">No data.</p>
              )}
            </Card>
          </div>
        </div>
      )}
    </>
  );
}
