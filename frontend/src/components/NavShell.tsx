import { type ReactNode } from "react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const links = [
  { to: "/dashboard", label: "Ledger" },
  { to: "/decks", label: "Decks" },
  { to: "/study", label: "Study" },
];

export function NavShell({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const location = useLocation();

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b border-ledger-rule bg-ledger-card/70 backdrop-blur-sm sticky top-0 z-10">
        <div className="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between">
          <Link to="/dashboard" className="flex items-baseline gap-2">
            <span className="font-display text-2xl font-semibold tracking-tight text-ledger-ink">
              Recall
            </span>
          </Link>
          <nav className="flex items-center gap-1">
            {links.map((l) => {
              const active = location.pathname.startsWith(l.to);
              return (
                <Link
                  key={l.to}
                  to={l.to}
                  className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                    active
                      ? "bg-ledger-ink text-ledger-paper"
                      : "text-ledger-ink/70 hover:bg-ledger-rule/50"
                  }`}
                >
                  {l.label}
                </Link>
              );
            })}
            <div className="ml-4 flex items-center gap-3 pl-4 border-l border-ledger-rule">
              <span className="font-mono text-xs text-ledger-faint hidden sm:inline">
                {user?.username}
              </span>
              <button
                onClick={logout}
                className="text-sm text-ledger-faint hover:text-forget transition-colors"
              >
                Sign out
              </button>
            </div>
          </nav>
        </div>
      </header>
      <main className="flex-1 max-w-5xl mx-auto w-full px-6 py-10">
        {children}
      </main>
    </div>
  );
}
