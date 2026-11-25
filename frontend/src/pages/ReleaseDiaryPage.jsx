import { useState } from 'react';
import { releaseApi } from '@/api/endpoints';
import { http, toErrorMessage } from '@/api/http';
import { useAsync } from '@/hooks/useAsync';
import { PageHeader } from '@/components/layout/PageHeader';
import {
  EmptyState, ErrorAlert, LoadingBlock, StatusBadge,
} from '@/components/ui/Primitives';
import { Pagination } from '@/components/ui/Pagination';
import { Field, EnumSelect } from '@/components/ui/Field';
import { formatDate, humanize, todayISO } from '@/utils/format';

const MODES = ['DAILY', 'WEEKLY', 'CUSTOM'];

export function ReleaseDiaryPage() {
  const [mode, setMode] = useState('DAILY');
  const [from, setFrom] = useState(todayISO());
  const [to, setTo] = useState(todayISO());
  const [page, setPage] = useState(0);
  const [exportError, setExportError] = useState(null);
  const [exporting, setExporting] = useState(false);

  const toParam = mode === 'CUSTOM' ? to : undefined;

  const { data, loading, error } = useAsync(
    () => releaseApi.diary({ mode, from, to: toParam, page, size: 10 }),
    [mode, from, to, page],
  );

  const exportCsv = async () => {
    setExporting(true);
    setExportError(null);
    try {
      const res = await http.get(releaseApi.diaryExportUrl, {
        params: { mode, from, to: toParam },
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(res.data);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'release-diary.csv';
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
    <>
      <PageHeader
        title="Release Diary"
        subtitle="Automated release diary generator"
        actions={<button className="btn btn-secondary" onClick={exportCsv} disabled={exporting}>{exporting ? 'Exporting…' : 'Export CSV'}</button>}
      />

      <div className="toolbar">
        <Field label="Mode">
          <EnumSelect value={mode} onChange={(v) => { setPage(0); setMode(v); }} options={MODES} />
        </Field>
        <Field label={mode === 'WEEKLY' ? 'Start Date (7-day window)' : mode === 'CUSTOM' ? 'From' : 'Date'}>
          <input className="input" type="date" value={from} onChange={(e) => { setPage(0); setFrom(e.target.value); }} />
        </Field>
        {mode === 'CUSTOM' && (
          <Field label="To">
            <input className="input" type="date" value={to} onChange={(e) => { setPage(0); setTo(e.target.value); }} />
          </Field>
        )}
        <span className="spacer" />
      </div>

      <ErrorAlert message={exportError} />
      <ErrorAlert message={error} />

      {loading ? (
        <LoadingBlock label="Loading release diary…" />
      ) : !data || data.content.length === 0 ? (
        <EmptyState message="No releases for the selected period." />
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Prisoner</th>
                <th>Expected Release</th>
                <th>Type</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((r) => (
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
      )}

      <Pagination page={data} onPageChange={setPage} />
    </>
  );
}
