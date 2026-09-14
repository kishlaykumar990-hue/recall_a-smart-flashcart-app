import { apiClient } from './apiClient';
import type { FlashCard } from '../types/api';

export const cardService = {
  async listByDeck(deckId: string) {
    const { data } = await apiClient.get<FlashCard[]>(`/cards/deck/${deckId}`);
    return data;
  },
  async due() {
    const { data } = await apiClient.get<FlashCard[]>('/cards/due');
    return data;
  },
  async create(payload: { deckId: string; front: string; back: string; hint?: string; tags?: string[] }) {
    const { data } = await apiClient.post<FlashCard>('/cards', payload);
    return data;
  },
  async update(id: string, payload: { deckId: string; front: string; back: string; hint?: string; tags?: string[] }) {
    const { data } = await apiClient.put<FlashCard>(`/cards/${id}`, payload);
    return data;
  },
  async remove(id: string) {
    await apiClient.delete(`/cards/${id}`);
  },
};
