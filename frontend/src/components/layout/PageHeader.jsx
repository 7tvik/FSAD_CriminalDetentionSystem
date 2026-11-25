export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="page-header">
      <div>
        <h1 style={{ marginBottom: 4 }}>{title}</h1>
        {subtitle && <p className="subtitle">{subtitle}</p>}
      </div>
      {actions && <div className="row row-wrap">{actions}</div>}
    </div>
  );
}
