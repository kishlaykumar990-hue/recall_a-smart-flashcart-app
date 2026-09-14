import { useEffect, useState, useRef } from 'react';
import { NavShell } from '../components/NavShell';
import { cardService } from '../services/cardService';
import { reviewService } from '../services/reviewService';
import type { FlashCard } from '../types/api';

const QUALITY_OPTIONS: { value: number; label: string; hint: string; tone: string }[] = [
  { value: 0, label: 'Blackout', hint: 'No memory at all', tone: 'bg-forget text-white' },
  { value: 1, label: 'Wrong', hint: 'Recognized on seeing it', tone: 'bg-forget/80 text-white' },
  { value: 2, label: 'Wrong, close', hint: 'Felt familiar', tone: 'bg-signal/70 text-white' },
  { value: 3, label: 'Hard', hint: 'Correct, with real effort', tone: 'bg-signal text-white' },
  { value: 4, label: 'Good', hint: 'Correct, brief hesitation', tone: 'bg-recall/70 text-white' },
  { value: 5, label: 'Easy', hint: 'Instant recall', tone: 'bg-recall text-white' },
];

export function StudyPage() {
  const [queue, setQueue] = useState<FlashCard[]>([]);
  const [index, setIndex] = useState(0);
  const [flipped, setFlipped] = useState(false);
  const [loading, setLoading] = useState(true);
  const [lastResult, setLastResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const cardShownAt = useRef<number>(Date.now());

  useEffect(() => {
    cardService.due()
      .then((cards) => setQueue(cards))
      .catch((err) => {
        const message = err?.response?.data?.message ?? 'Could not load your due cards. Please try again.';
        setError(message);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    cardShownAt.current = Date.now();
  }, [index]);

  const currentCard = queue[index];

  const handleRate = async (quality: number) => {
    if (!currentCard) return;
    const responseTimeMs = Date.now() - cardShownAt.current;
    const result = await reviewService.submit(currentCard.id, quality, responseTimeMs);
    setLastResult(
      `Next review in ${result.newIntervalDays} day${result.newIntervalDays === 1 ? '' : 's'} · ease ${result.newEaseFactor.toFixed(2)}`
    );
    setFlipped(false);
    setIndex((i) => i + 1);
  };

  if (loading) {
    return <NavShell><p className="text-ledger-faint">Fetching today's stack…</p></NavShell>;
  }

  if (error) {
    return <NavShell><p className="text-forget">{error}</p></NavShell>;
  }

  if (!currentCard) {
    return (
      <NavShell>
        <div className="text-center py-24">
          <p className="font-display text-2xl text-ledger-ink">You're caught up</p>
          <p className="text-ledger-faint mt-2">
            {lastResult ? `Last card scheduled — ${lastResult}` : 'No cards are due right now.'}
          </p>
        </div>
      </NavShell>
    );
  }

  return (
    <NavShell>
      <div className="max-w-xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <p className="font-mono text-xs uppercase tracking-widest text-ledger-faint">
            Card {index + 1} of {queue.length}
          </p>
          <div className="flex gap-1">
            {Array.from({ length: card_progress_dots(queue.length) }).map((_, i) => (
              <span
                key={i}
                className={`w-1.5 h-1.5 rounded-full ${i < index ? 'bg-recall' : 'bg-ledger-rule'}`}
              />
            ))}
          </div>
        </div>

        <div className="card-flip h-72" onClick={() => setFlipped((f) => !f)}>
          <div className={`card-flip-inner relative w-full h-full cursor-pointer ${flipped ? 'flipped' : ''}`}>
            <div className="card-face absolute inset-0 bg-ledger-card border border-ledger-rule rounded-card shadow-card flex flex-col items-center justify-center px-8 text-center">
              <p className="font-mono text-[10px] uppercase tracking-widest text-ledger-faint absolute top-4 left-4">Front</p>
              <p className="font-display text-2xl text-ledger-ink">{currentCard.front}</p>
              {currentCard.hint && <p className="text-sm text-ledger-faint mt-3 italic">Hint: {currentCard.hint}</p>}
              <p className="text-xs text-ledger-faint absolute bottom-4">Tap to reveal</p>
            </div>
            <div className="card-face card-face-back absolute inset-0 bg-recall-light border border-recall/30 rounded-card shadow-card flex flex-col items-center justify-center px-8 text-center">
              <p className="font-mono text-[10px] uppercase tracking-widest text-recall absolute top-4 left-4">Back</p>
              <p className="font-display text-2xl text-ledger-ink">{currentCard.back}</p>
            </div>
          </div>
        </div>

        {flipped ? (
          <div className="mt-8">
            <p className="text-sm text-ledger-faint text-center mb-3">How well did you recall this?</p>
            <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
              {QUALITY_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  onClick={() => handleRate(opt.value)}
                  className={`${opt.tone} rounded-md py-3 px-2 text-center transition-transform hover:scale-105`}
                  title={opt.hint}
                >
                  <span className="block font-display text-lg font-semibold">{opt.value}</span>
                  <span className="block text-[10px] uppercase tracking-wide mt-0.5">{opt.label}</span>
                </button>
              ))}
            </div>
          </div>
        ) : (
          <p className="text-center text-sm text-ledger-faint mt-8">Click the card to reveal the answer</p>
        )}
      </div>
    </NavShell>
  );
}

function card_progress_dots(total: number): number {
  return Math.min(total, 20);
}
