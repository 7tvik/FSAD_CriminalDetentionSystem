import { useState } from 'react';
import {
  Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis,
} from 'recharts';
import { reportApi } from '@/api/endpoints';
import { http, toErrorMessage } from '@/api/http';
import { useAsync } from '@/hooks/useAsync';
import { PageHeader } from '@/components/layout/PageHeader';
import {
  Card, EmptyState, ErrorAlert, LoadingBlock, StatusBadge,
} from '@/components/ui/Primitives';
import { Field } from '@/components/ui/Field';
import { formatDate, humanize, todayISO } from '@/utils/format';

const TABS = [
  { key: 'daily', label: 'Daily Releases' },
  { key: 'monthly', label: 'Monthly Releases' },
  { key: 'upcoming', label: 'Upcoming Releases' },
];

function ReleaseTable({ rows }) {
  if (rows.length === 0) {
    return <EmptyState message="No releases for this selection." />;
  }

  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr><th>Prisoner</th><th>Expected Release</th><th>Type</th><th>Status</th></tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.id}>
              <td>{r.prisonerName}<div className="text-muted mono">{r.prisonerNumber}</div></td>
              <td>{formatDate(r.expectedReleaseDate)}</td>
              <td>{humanize(r.releaseType)}</td>
              <td><StatusBadge status={r.status} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function DailyTab() {
  const [date, setDate] = useState(todayISO());
  const { data, loading, error } = useAsync(() => reportApi.dailyReleases(date), [date]);
  const [exportError, setExportError] = useState(null);
  const [exporting, setExporting] = useState(false);

  const exportCsv = async () => {
    setExporting(true);
    setExportError(null);
    try {
      const res = await http.get('/reports/releases/daily/export', {
        params: { date },
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(res.data);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'daily-releases.csv';
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setExportError(toErrorMessage(err));
    } finally {
      setExporting(false);
    }
  };

  return (
    <Card
      title="Daily Releases"
      actions={<button className="btn btn-secondary btn-sm" onClick={exportCsv} disabled={exporting}>{exporting ? 'Exporting…' : 'Export Daily CSV'}</button>}
    >
      <div className="toolbar">
        <Field label="Date">
          <input className="input" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </Field>
        <span className="spacer" />
      </div>
      <ErrorAlert message={exportError} />
      <ErrorAlert message={error} />
      {loading ? <LoadingBlock label="Loading…" /> : <ReleaseTable rows={data ?? []} />}
    </Card>
  );
}

function MonthlyTab() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const { data, loading, error } = useAsync(() => reportApi.monthlyReleases(year, month), [year, month]);

  return (
    <Card title="Monthly Releases">
      <div className="toolbar">
        <Field label="Year">
          <input className="input" type="number" value={year} onChange={(e) => setYear(Number(e.target.value))} style={{ maxWidth: 120 }} />
        </Field>
        <Field label="Month">
          <input className="input" type="number" min={1} max={12} value={month} onChange={(e) => setMonth(Number(e.target.value))} style={{ maxWidth: 100 }} />
        </Field>
        <span className="spacer" />
      </div>
      <ErrorAlert message={error} />
      {loading ? <LoadingBlock label="Loading…" /> : <ReleaseTable rows={data ?? []} />}
    </Card>
  );
}

function UpcomingTab() {
  const [days, setDays] = useState(30);
  const { data, loading, error } = useAsync(() => reportApi.upcomingReleases(days), [days]);

  return (
    <Card title="Upcoming Releases">
      <div className="toolbar">
        <Field label="Within (days)">
          <input className="input" type="number" min={1} value={days} onChange={(e) => setDays(Number(e.target.value))} style={{ maxWidth: 120 }} />
        </Field>
        <span className="spacer" />
      </div>
      <ErrorAlert message={error} />
      {loading ? <LoadingBlock label="Loading…" /> : <ReleaseTable rows={data ?? []} />}
    </Card>
  );
}

function StatisticsSection() {
  const prisonerStats = useAsync(() => reportApi.prisonerStats(), []);
  const caseStats = useAsync(() => reportApi.caseStats(), []);

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '1rem' }}>
      <Card title="Prisoners by Status">
        <ErrorAlert message={prisonerStats.error} />
        {prisonerStats.data && prisonerStats.data.some((s) => s.value > 0) ? (
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={prisonerStats.data}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="label" tick={{ fontSize: 11 }} angle={-15} textAnchor="end" height={60} />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="value" name="Prisoners" fill="#1f4e79" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <p className="text-muted">No data.</p>
        )}
      </Card>
      <Card title="Cases by Status">
        <ErrorAlert message={caseStats.error} />
        {caseStats.data && caseStats.data.some((s) => s.value > 0) ? (
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={caseStats.data}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="label" tick={{ fontSize: 11 }} angle={-15} textAnchor="end" height={60} />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="value" name="Cases" fill="#15803d" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <p className="text-muted">No data.</p>
        )}
      </Card>
    </div>
  );
}

export function ReportsPage() {
  const [tab, setTab] = useState('daily');

  return (
    <>
      <PageHeader title="Reports" subtitle="Release reports and statistics" />

      <div className="stack">
        <div className="tabs">
          {TABS.map((t) => (
            <button key={t.key} className={`tab ${tab === t.key ? 'active' : ''}`} onClick={() => setTab(t.key)}>
              {t.label}
            </button>
          ))}
        </div>

        {tab === 'daily' && <DailyTab />}
        {tab === 'monthly' && <MonthlyTab />}
        {tab === 'upcoming' && <UpcomingTab />}

        <h2 style={{ marginTop: '1rem' }}>Statistics</h2>
        <StatisticsSection />
      </div>
    </>
  );
}
