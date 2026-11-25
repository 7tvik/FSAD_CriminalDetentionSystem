import { useAsync } from '@/hooks/useAsync';
import { staffApi } from '@/api/endpoints';

/** Staff dropdown backed by staffApi.list(); value/onChange are string (staff id). */
export function StaffSelect({ value, onChange, includeBlank = true, blankLabel = 'All', id }) {
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
