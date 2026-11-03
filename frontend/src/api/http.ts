import axios, { AxiosError, type AxiosInstance } from 'axios';
import type { ApiError } from '@/types';

const TOKEN_KEY = 'cdp.token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

// Callback invoked when the server rejects our token (401). Wired by AuthProvider
// so an expired session cleanly bounces the user back to the login screen.
let onUnauthorized: (() => void) | null = null;
export function setUnauthorizedHandler(fn: () => void): void {
  onUnauthorized = fn;
}

export const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      // Don't bounce on the login attempt itself — let the form show the error.
      const url = error.config?.url || '';
      if (!url.includes('/auth/login')) {
        clearToken();
        onUnauthorized?.();
      }
    }
    return Promise.reject(error);
  },
);

/** Normalize any thrown error into a human-readable message for the UI. */
export function toErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ApiError | undefined;
    if (data?.fieldErrors?.length) {
      return data.fieldErrors.map((f) => `${f.field}: ${f.message}`).join('; ');
    }
    if (data?.message) return data.message;
    if (err.code === 'ERR_NETWORK') return 'Cannot reach the server. Is the backend running?';
    return err.message;
  }
  if (err instanceof Error) return err.message;
  return 'An unexpected error occurred.';
}
