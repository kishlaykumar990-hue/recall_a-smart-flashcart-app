import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('demo@example.com');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch {
      setError('Those credentials did not match an account. Check your email and password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-6">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <h1 className="font-display text-3xl font-semibold text-ledger-ink">Recall</h1>
          <p className="font-mono text-xs uppercase tracking-widest text-ledger-faint mt-1">
            Adaptive Spaced Repetition
          </p>
        </div>
        <form onSubmit={handleSubmit} className="bg-ledger-card border border-ledger-rule rounded-card shadow-card p-8 space-y-5">
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20"
            />
          </div>
          {error && <p className="text-sm text-forget">{error}</p>}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-ledger-ink text-ledger-paper rounded-md py-2.5 font-medium hover:bg-ledger-ink/90 transition-colors disabled:opacity-60"
          >
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
          <p className="text-sm text-center text-ledger-faint">
            New here?{' '}
            <Link to="/register" className="text-ledger-ink underline underline-offset-2">
              Create an account
            </Link>
          </p>
        </form>
        <p className="text-xs text-center text-ledger-faint mt-4 font-mono">
          demo account: demo@example.com / Password123!
        </p>
      </div>
    </div>
  );
}
