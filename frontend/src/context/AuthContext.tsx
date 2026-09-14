import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import { authService } from '../services/authService';

interface AuthUser {
  userId: string;
  username: string;
  email: string;
  role: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string, displayName?: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadStoredUser(): AuthUser | null {
  const raw = localStorage.getItem('recall_user');
  return raw ? (JSON.parse(raw) as AuthUser) : null;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadStoredUser());

  const persist = (u: AuthUser, token: string) => {
    localStorage.setItem('recall_token', token);
    localStorage.setItem('recall_user', JSON.stringify(u));
    setUser(u);
  };

  const login = useCallback(async (email: string, password: string) => {
    const res = await authService.login(email, password);
    persist({ userId: res.userId, username: res.username, email: res.email, role: res.role }, res.token);
  }, []);

  const register = useCallback(async (username: string, email: string, password: string, displayName?: string) => {
    const res = await authService.register(username, email, password, displayName);
    persist({ userId: res.userId, username: res.username, email: res.email, role: res.role }, res.token);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('recall_token');
    localStorage.removeItem('recall_user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
