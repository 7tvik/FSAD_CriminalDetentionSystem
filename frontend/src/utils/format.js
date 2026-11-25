// Small formatting + label helpers shared across pages.

export function formatDate(value) {
  if (!value) return '—';
  // Accept both date (yyyy-MM-dd) and date-time ISO strings.
  const d = new Date(value.length <= 10 ? `${value}T00:00:00` : value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: '2-digit' });
}

export function formatDateTime(value) {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString(undefined, {
    year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit',
  });
}

export function formatTime(value) {
  if (!value) return '—';
  // LocalTime comes as HH:mm:ss
  return value.slice(0, 5);
}

/** Turn an ENUM_LIKE_VALUE into "Enum Like Value". */
export function humanize(value) {
  if (!value) return '—';
  return value
    .toLowerCase()
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ');
}

export function fullName(first, last) {
  return [first, last].filter(Boolean).join(' ') || '—';
}

export const todayISO = () => new Date().toISOString().slice(0, 10);

/** Map a domain status string to a badge CSS modifier. */
export function statusVariant(status) {
  switch (status) {
    case 'ACTIVE':
    case 'APPROVED':
    case 'RELEASED':
    case 'RETURNED':
    case 'COMPLETED':
    case 'ACQUITTED':
      return 'badge-success';
    case 'UPCOMING':
    case 'READY':
    case 'SCHEDULED':
    case 'PENDING':
    case 'UNDER_INVESTIGATION':
    case 'UNDER_TRIAL':
    case 'OUT':
    case 'DEPARTED':
    case 'AT_COURT':
    case 'HEARING_COMPLETED':
    case 'DEPARTED_COURT':
      return 'badge-info';
    case 'ON_HOLD':
    case 'HELD':
    case 'TRANSFERRED':
      return 'badge-warning';
    case 'CANCELLED':
    case 'REJECTED':
    case 'DECEASED':
    case 'INACTIVE':
    case 'CONVICTED':
      return 'badge-danger';
    default:
      return 'badge-neutral';
  }
}
