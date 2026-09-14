import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { NavShell } from '../components/NavShell';
import { deckService } from '../services/deckService';
import type { Deck } from '../types/api';

export function DecksPage() {
  const [decks, setDecks] = useState<Deck[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [subject, setSubject] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = () => {
    setLoading(true);
    deckService.list().then(setDecks).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await deckService.create({ name, subject: subject || undefined, description: description || undefined });
      setName(''); setSubject(''); setDescription('');
      setShowForm(false);
      load();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <NavShell>
      <div className="flex items-center justify-between mb-8">
        <h1 className="font-display text-3xl font-semibold text-ledger-ink">Your Decks</h1>
        <button
          onClick={() => setShowForm((s) => !s)}
          className="bg-ledger-ink text-ledger-paper rounded-full px-4 py-2 text-sm font-medium hover:bg-ledger-ink/90 transition-colors"
        >
          {showForm ? 'Cancel' : '+ New deck'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-ledger-card border border-ledger-rule rounded-card shadow-card p-6 mb-8 space-y-4">
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Deck name</label>
            <input
              required value={name} onChange={(e) => setName(e.target.value)}
              placeholder="e.g. French Vocabulary — Unit 3"
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-ledger-ink mb-1.5">Subject</label>
              <input
                value={subject} onChange={(e) => setSubject(e.target.value)}
                placeholder="Language, Chemistry…"
                className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-ledger-ink mb-1.5">Description</label>
              <input
                value={description} onChange={(e) => setDescription(e.target.value)}
                className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20"
              />
            </div>
          </div>
          <button
            type="submit" disabled={submitting}
            className="bg-recall text-white rounded-md px-4 py-2 text-sm font-medium hover:bg-recall/90 transition-colors disabled:opacity-60"
          >
            {submitting ? 'Creating…' : 'Create deck'}
          </button>
        </form>
      )}

      {loading ? (
        <p className="text-ledger-faint">Loading decks…</p>
      ) : decks.length === 0 ? (
        <div className="text-center py-16 border border-dashed border-ledger-rule rounded-card">
          <p className="font-display text-lg text-ledger-ink">No decks yet</p>
          <p className="text-sm text-ledger-faint mt-1">Create your first deck to start building your card catalog.</p>
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 gap-4">
          {decks.map((deck) => (
            <Link
              key={deck.id}
              to={`/decks/${deck.id}`}
              className="bg-ledger-card border border-ledger-rule rounded-card shadow-card p-5 hover:shadow-lg hover:-translate-y-0.5 transition-all"
            >
              <div className="flex items-start justify-between">
                <h2 className="font-display text-lg font-semibold text-ledger-ink">{deck.name}</h2>
                {deck.dueCards > 0 && (
                  <span className="font-mono text-xs bg-signal/15 text-signal px-2 py-0.5 rounded-full">
                    {deck.dueCards} due
                  </span>
                )}
              </div>
              {deck.subject && <p className="text-xs uppercase tracking-wide text-ledger-faint mt-1 font-mono">{deck.subject}</p>}
              {deck.description && <p className="text-sm text-ledger-ink/70 mt-2">{deck.description}</p>}
              <p className="text-xs text-ledger-faint mt-4">{deck.totalCards} card{deck.totalCards === 1 ? '' : 's'}</p>
            </Link>
          ))}
        </div>
      )}
    </NavShell>
  );
}
