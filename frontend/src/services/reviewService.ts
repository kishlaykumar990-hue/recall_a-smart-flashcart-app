import { apiClient } from './apiClient';
import type { ReviewResponse } from '../types/api';

export const reviewService = {
  async submit(cardId: string, quality: number, responseTimeMs?: number) {
    const { data } = await apiClient.post<ReviewResponse>('/reviews', {
      cardId, quality, responseTimeMs,
    });
    return data;
  },
};
