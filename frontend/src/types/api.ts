export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  email: string;
  role: string;
}

export interface Deck {
  id: string;
  name: string;
  description: string | null;
  subject: string | null;
  archived: boolean;
  totalCards: number;
  dueCards: number;
  createdAt: string;
  updatedAt: string;
}

export interface FlashCard {
  id: string;
  deckId: string;
  front: string;
  back: string;
  hint: string | null;
  tags: string[];
  easeFactor: number;
  repetitions: number;
  intervalDays: number;
  dueDate: string | null;
  due: boolean;
  totalReviews: number;
  lapses: number;
  lastReviewedAt: string | null;
}

export interface ReviewResponse {
  cardId: string;
  quality: number;
  newEaseFactor: number;
  newRepetitions: number;
  newIntervalDays: number;
  newDueDate: string;
}

export interface DailyReviewCount {
  date: string;
  reviewsCount: number;
  correctCount: number;
}

export interface DashboardStats {
  totalCards: number;
  dueToday: number;
  currentStreak: number;
  longestStreak: number;
  retentionRate: number;
  last14Days: DailyReviewCount[];
}

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}
