export function Field({ label, htmlFor, error, hint, full, required, children }) {
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

export function EnumSelect({ value, onChange, options, includeBlank, blankLabel = 'All', id }) {
  const normalized = options.map((o) =>
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
