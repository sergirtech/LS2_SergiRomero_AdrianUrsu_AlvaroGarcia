import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { getEnv } from './utils/Env';

export interface User {
  id: number;
  email: string;
  username: string;
}

export interface RegisterData {
  email: string;
  username: string;
  password: string;
}

type AuthStatus = 'loading' | 'ready';

interface AuthContextValue {
  user: User | null;
  status: AuthStatus;
  register: (data: RegisterData) => Promise<User>;
  login: (email: string, password: string) => Promise<User>;
  logout: () => Promise<void>;
}

// Error from the auth API: 400 has field errors, 401 bad credentials, 409 duplicate user, 0 network failure.
export class AuthError extends Error {
  readonly status: number;
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = 'AuthError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

const AUTH_URL = `${getEnv().API_BASE_URL}/auth`;

// The session lives in the JSESSIONID cookie, so every call sends credentials (docs/decisions.md D17).
const request = async (method: 'GET' | 'POST', path: string, body?: unknown): Promise<Response> => {
  try {
    return await fetch(`${AUTH_URL}${path}`, {
      method,
      credentials: 'include',
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new AuthError(0, 'Could not reach the server. Try again later.');
  }
};

const readUser = async (response: Response): Promise<User> => {
  if (response.ok) {
    return (await response.json()) as User;
  }
  const data = (await response.json().catch(() => ({}))) as { message?: string; errors?: Record<string, string> };
  throw new AuthError(response.status, data.message ?? 'Something went wrong. Try again.', data.errors);
};

const AuthContext = createContext<AuthContextValue | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [status, setStatus] = useState<AuthStatus>('loading');

  // Restore the session after a page reload: 200 means signed in, anything else signed out.
  useEffect(() => {
    request('GET', '/me')
      .then((response) => (response.ok ? readUser(response) : null))
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setStatus('ready'));
  }, []);

  const register = useCallback(async (data: RegisterData) => {
    const newUser = await readUser(await request('POST', '/register', data));
    setUser(newUser);
    return newUser;
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const loggedUser = await readUser(await request('POST', '/login', { email, password }));
    setUser(loggedUser);
    return loggedUser;
  }, []);

  const logout = useCallback(async () => {
    await request('POST', '/logout');
    setUser(null);
  }, []);

  const value = useMemo(() => ({ user, status, register, login, logout }), [user, status, register, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextValue => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside an AuthProvider');
  }
  return context;
};
