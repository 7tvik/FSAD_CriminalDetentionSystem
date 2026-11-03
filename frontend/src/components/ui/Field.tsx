import type { ReactNode } from 'react';

interface FieldProps {
  label: string;
  htmlFor?: string;
  error?: string;
  hint?: string;
  full?: boolean;
  required?: boolean;
  children: ReactNode;
}

export function Field({ label, htmlFor, error, hint, full, required, children }: FieldProps) {
  return (
    <div className={`field ${full ? 'full' : ''}`}>
      <label htmlFor={htmlFor}>
        {label} {required && <span style={{ color: 'var(--color-danger)' }}>*</span>}
      </label>
      {children}
      {hint && !error && <span className="hint">{hint}</span>}
      {error && <span className="error-text">{error}</span>}
    </div>
  );
}

interface Option {
  value: string;
  label: string;
}

export function EnumSelect({
  value,
  onChange,
  options,
  includeBlank,
  blankLabel = 'All',
  id,
}: {
  value: string;
  onChange: (v: string) => void;
  options: readonly string[] | Option[];
  includeBlank?: boolean;
  blankLabel?: string;
  id?: string;
}) {
  const normalized: Option[] = options.map((o) =>
    typeof o === 'string' ? { value: o, label: o.replace(/_/g, ' ') } : o,
  );
  return (
    <select id={id} className="select" value={value} onChange={(e) => onChange(e.target.value)}>
      {includeBlank && <option value="">{blankLabel}</option>}
      {normalized.map((o) => (
        <option key={o.value} value={o.value}>
          {o.label}
        </option>
      ))}
    </select>
  );
}
