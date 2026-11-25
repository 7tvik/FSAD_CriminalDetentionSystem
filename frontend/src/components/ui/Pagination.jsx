export function Pagination({ page, onPageChange }) {
  if (!page || page.totalElements === 0) return null;
  const from = page.page * page.size + 1;
  const to = Math.min((page.page + 1) * page.size, page.totalElements);
  return (
    <div className="pagination">
      <span>
        Showing <strong>{from}</strong>–<strong>{to}</strong> of <strong>{page.totalElements}</strong>
      </span>
      <div className="pager">
        <button
          className="btn btn-secondary btn-sm"
          disabled={page.first}
          onClick={() => onPageChange(page.page - 1)}
        >
          ‹ Prev
        </button>
        <span>
          Page {page.page + 1} of {page.totalPages}
        </span>
        <button
          className="btn btn-secondary btn-sm"
          disabled={page.last}
          onClick={() => onPageChange(page.page + 1)}
        >
          Next ›
        </button>
      </div>
    </div>
  );
}
