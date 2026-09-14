import { useEffect, useState, type FormEvent } from 'react';
import { useParams, Link } from 'react-router-dom';
import { NavShell } from '../components/NavShell';
import { deckService } from '../services/deckService';
import { cardService } from '../services/cardService';
import type { Deck, FlashCard } from '../types/api';

export function DeckDetailPage() {
  const { deckId } = useParams<{ deckId: string }>();
  const [deck, setDeck] = useState<Deck | null>(null);
  const [cards, setCards] = useState<FlashCard[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [front, setFront] = useState('');
  const [back, setBack] = useState('');
  const [hint, setHint] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = () => {
    if (!deckId) return;
    setLoading(true);
    setError(null);
    Promise.all([deckService.get(deckId), cardService.listByDeck(deckId)])
      .then(([d, c]) => { setDeck(d); setCards(c); })
      .catch((err) => {
        const message = err?.response?.data?.message ?? 'Something went wrong loading this deck.';
        setError(message);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, [deckId]);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    if (!deckId) return;
    setSubmitting(true);
    try {
      await cardService.create({ deckId, front, back, hint: hint || undefined });
      setFront(''); setBack(''); setHint('');
      setShowForm(false);
      load();
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    await cardService.remove(id);
    load();
  };

  if (loading) return <NavShell><p className="text-ledger-faint">Loading…</p></NavShell>;
  if (error) return <NavShell><p className="text-forget">{error}</p></NavShell>;
  if (!deck) return <NavShell><p className="text-ledger-faint">Deck not found.</p></NavShell>;

  return (
    <NavShell>
      <Link to="/decks" className="text-sm text-ledger-faint hover:text-ledger-ink mb-4 inline-block">&larr; All decks</Link>
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="font-display text-3xl font-semibold text-ledger-ink">{deck.name}</h1>
          {deck.description && <p className="text-ledger-faint mt-1">{deck.description}</p>}
        </div>
        <button
          onClick={() => setShowForm((s) => !s)}
          className="bg-ledger-ink text-ledger-paper rounded-full px-4 py-2 text-sm font-medium hover:bg-ledger-ink/90 transition-colors shrink-0"
        >
          {showForm ? 'Cancel' : '+ New card'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-ledger-card border border-ledger-rule rounded-card shadow-card p-6 mb-8 space-y-4">
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Front (question / prompt)</label>
            <textarea required value={front} onChange={(e) => setFront(e.target.value)} rows={2}
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20 font-display" />
          </div>
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Back (answer)</label>
            <textarea required value={back} onChange={(e) => setBack(e.target.value)} rows={2}
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20 font-display" />
          </div>
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Hint (optional)</label>
            <input value={hint} onChange={(e) => setHint(e.target.value)}
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20" />
          </div>
          <button type="submit" disabled={submitting}
            className="bg-recall text-white rounded-md px-4 py-2 text-sm font-medium hover:bg-recall/90 transition-colors disabled:opacity-60">
            {submitting ? 'Adding…' : 'Add card'}
          </button>
        </form>
      )}

      {cards.length === 0 ? (
        <div className="text-center py-16 border border-dashed border-ledger-rule rounded-card">
          <p className="font-display text-lg text-ledger-ink">No cards in this deck yet</p>
        </div>
      ) : (
        <div className="space-y-3">
          {cards.map((card) => (
            <div key={card.id} className="bg-ledger-card border border-ledger-rule rounded-card shadow-card p-4 flex items-start justify-between gap-4">
              <div className="min-w-0">
                <p className="font-display text-ledger-ink truncate">{card.front}</p>
                <p className="text-sm text-ledger-faint truncate mt-0.5">{card.back}</p>
                <div className="flex items-center gap-3 mt-2 font-mono text-[11px] text-ledger-faint">
                  <span>EF {card.easeFactor.toFixed(2)}</span>
                  <span>reps {card.repetitions}</span>
                  <span>interval {card.intervalDays}d</span>
                  {card.due && <span className="text-signal">due</span>}
                </div>
              </div>
              <button onClick={() => handleDelete(card.id)} className="text-xs text-ledger-faint hover:text-forget shrink-0">
                Delete
              </button>
            </div>
          ))}
        </div>
      )}
    </NavShell>
  );
}
