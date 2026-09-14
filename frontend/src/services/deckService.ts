import { apiClient } from './apiClient';
import type { Deck } from '../types/api';

export const deckService = {
  async list() {
    const { data } = await apiClient.get<Deck[]>('/decks');
    return data;
  },
  async get(id: string) {
    const { data } = await apiClient.get<Deck>(`/decks/${id}`);
    return data;
  },
  async create(payload: { name: string; description?: string; subject?: string }) {
    const { data } = await apiClient.post<Deck>('/decks', payload);
    return data;
  },
  async update(id: string, payload: { name: string; description?: string; subject?: string }) {
    const { data } = await apiClient.put<Deck>(`/decks/${id}`, payload);
    return data;
  },
  async archive(id: string) {
    await apiClient.patch(`/decks/${id}/archive`);
  },
  async remove(id: string) {
    await apiClient.delete(`/decks/${id}`);
  },
};
