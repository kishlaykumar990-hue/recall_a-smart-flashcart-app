import { apiClient } from './apiClient';
import type { AuthResponse } from '../types/api';

export const authService = {
  async register(username: string, email: string, password: string, displayName?: string) {
    const { data } = await apiClient.post<AuthResponse>('/auth/register', {
      username, email, password, displayName,
    });
    return data;
  },

  async login(email: string, password: string) {
    const { data } = await apiClient.post<AuthResponse>('/auth/login', { email, password });
    return data;
  },
};
