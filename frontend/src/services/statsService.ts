import { apiClient } from './apiClient';
import type { DashboardStats } from '../types/api';

export const statsService = {
  async dashboard() {
    const { data } = await apiClient.get<DashboardStats>('/stats/dashboard');
    return data;
  },
};
