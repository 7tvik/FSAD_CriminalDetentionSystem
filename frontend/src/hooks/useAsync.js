import { useCallback, useEffect, useState } from 'react';
import { toErrorMessage } from '@/api/http';

/** Generic async loader with loading/error state and a manual refresh. */
export function useAsync(fn, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const memoFn = useCallback(fn, deps); // eslint-disable-line react-hooks/exhaustive-deps

  const reload = useCallback(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    memoFn()
      .then((res) => {
        if (!cancelled) setData(res);
      })
      .catch((err) => {
        if (!cancelled) setError(toErrorMessage(err));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [memoFn]);

  useEffect(() => reload(), [reload]);

  return { data, loading, error, reload, setData };
}
