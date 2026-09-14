import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await register(username, email, password);
      navigate('/dashboard');
    } catch (err: unknown) {
      const message =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Could not create your account. Please try again.';
      setError(message);
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
            Start your learning ledger
          </p>
        </div>
        <form onSubmit={handleSubmit} className="bg-ledger-card border border-ledger-rule rounded-card shadow-card p-8 space-y-5">
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Username</label>
            <input
              required minLength={3} maxLength={50}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Email</label>
            <input
              type="email" required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-ledger-ink mb-1.5">Password</label>
            <input
              type="password" required minLength={8}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 rounded-md border border-ledger-rule bg-ledger-paper/40 focus:outline-none focus:ring-2 focus:ring-ledger-ink/20"
            />
            <p className="text-xs text-ledger-faint mt-1">At least 8 characters.</p>
          </div>
          {error && <p className="text-sm text-forget">{error}</p>}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-ledger-ink text-ledger-paper rounded-md py-2.5 font-medium hover:bg-ledger-ink/90 transition-colors disabled:opacity-60"
          >
            {loading ? 'Creating account…' : 'Create account'}
          </button>
          <p className="text-sm text-center text-ledger-faint">
            Already have an account?{' '}
            <Link to="/login" className="text-ledger-ink underline underline-offset-2">
              Sign in
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}
