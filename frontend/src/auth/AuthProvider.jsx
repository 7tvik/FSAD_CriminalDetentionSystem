import { useCallback, useEffect, useMemo, useState } from 'react';
import { authApi } from '@/api/endpoints';
import { getToken, setToken, clearToken, setUnauthorizedHandler } from '@/api/http';
import { AuthContext } from './AuthContext';

const USER_KEY = 'cdp.user';

function loadStoredUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw || !getToken()) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(loadStoredUser);
  const [loading, setLoading] = useState(false);

  const logout = useCallback(() => {
    clearToken();
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  // If any request 401s, the http layer calls this to force a clean logout.
  useEffect(() => {
    setUnauthorizedHandler(() => {
      localStorage.removeItem(USER_KEY);
      setUser(null);
    });
  }, []);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    try {
      const res = await authApi.login({ username, password });
      setToken(res.token);
      localStorage.setItem(USER_KEY, JSON.stringify(res));
      setUser(res);
    } finally {
      setLoading(false);
    }
  }, []);

  const hasPermission = useCallback(
    (permission) => !!user?.permissions?.includes(permission),
    [user],
  );
  const hasAnyPermission = useCallback(
    (...permissions) => permissions.some((p) => user?.permissions?.includes(p)),
    [user],
  );
  const hasRole = useCallback((role) => !!user?.roles?.includes(role), [user]);

  const value = useMemo(
    () => ({ user, loading, login, logout, hasPermission, hasAnyPermission, hasRole }),
    [user, loading, login, logout, hasPermission, hasAnyPermission, hasRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
