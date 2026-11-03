import { useAsync } from '@/hooks/useAsync';
import { staffApi } from '@/api/endpoints';

/** Staff dropdown backed by staffApi.list(); value/onChange are string (staff id). */
export function StaffSelect({
  value,
  onChange,
  includeBlank = true,
  blankLabel = 'All',
  id,
}: {
  value: string;
  onChange: (v: string) => void;
  includeBlank?: boolean;
  blankLabel?: string;
  id?: string;
}) {
  const { data } = useAsync(() => staffApi.list(), []);
  return (
    <select id={id} className="select" value={value} onChange={(e) => onChange(e.target.value)}>
      {includeBlank && <option value="">{blankLabel}</option>}
      {(data ?? []).map((s) => (
        <option key={s.id} value={String(s.id)}>
          {s.fullName} ({s.staffNumber})
        </option>
      ))}
    </select>
  );
}
