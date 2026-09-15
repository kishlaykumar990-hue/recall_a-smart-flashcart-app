import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  ResponsiveContainer,
  Tooltip,
  CartesianGrid,
} from "recharts";
import { NavShell } from "../components/NavShell";
import { statsService } from "../services/statsService";
import { useAuth } from "../context/AuthContext";
import type { DashboardStats } from "../types/api";

export function DashboardPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    statsService
      .dashboard()
      .then(setStats)
      .finally(() => setLoading(false));
  }, []);

  const chartData = (stats?.last14Days ?? []).map((d) => ({
    date: d.date.slice(5), // MM-DD
    reviews: d.reviewsCount,
    correct: d.correctCount,
  }));

  return (
    <NavShell>
      <div className="mb-8">
        <p className="font-mono text-xs uppercase tracking-widest text-ledger-faint">
          {new Date().toLocaleDateString(undefined, {
            weekday: "long",
            month: "long",
            day: "numeric",
          })}
        </p>
        <h1 className="font-display text-3xl font-semibold text-ledger-ink mt-1">
          Welcome back, {user?.username}
        </h1>
      </div>

      {loading ? (
        <p className="text-ledger-faint">Loading your ledger…</p>
      ) : stats ? (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
            <StatCard
              label="Due today"
              value={stats.dueToday}
              accent="signal"
            />
            <StatCard
              label="Total cards"
              value={stats.totalCards}
              accent="ink"
            />
            <StatCard
              label="Current streak"
              value={`${stats.currentStreak}d`}
              accent="recall"
            />
            <StatCard
              label="Retention"
              value={`${stats.retentionRate}%`}
              accent="recall"
            />
          </div>

          {stats.dueToday > 0 && (
            <Link
              to="/study"
              className="block mb-10 rounded-card border border-signal/40 bg-signal/10 px-6 py-4 hover:bg-signal/15 transition-colors"
            >
              <p className="font-display text-lg text-ledger-ink">
                {stats.dueToday} card{stats.dueToday === 1 ? "" : "s"} stamped
                due today
              </p>
              <p className="text-sm text-ledger-faint mt-0.5">
                Start your review session →
              </p>
            </Link>
          )}

          <div className="bg-ledger-card border border-ledger-rule rounded-card shadow-card p-6">
            <h2 className="font-display text-lg text-ledger-ink mb-1">
              Last 14 days
            </h2>
            <p className="text-xs text-ledger-faint font-mono mb-4">
              reviews completed &middot; correct in green
            </p>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={chartData}>
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke="#D9CFB8"
                  vertical={false}
                />
                <XAxis
                  dataKey="date"
                  tick={{ fontSize: 11, fill: "#8A8172" }}
                  axisLine={{ stroke: "#D9CFB8" }}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: "#8A8172" }}
                  axisLine={false}
                  tickLine={false}
                  allowDecimals={false}
                />
                <Tooltip
                  contentStyle={{
                    background: "#FFFDF7",
                    border: "1px solid #D9CFB8",
                    borderRadius: 8,
                    fontSize: 12,
                  }}
                />
                <Bar
                  dataKey="reviews"
                  fill="#D9CFB8"
                  radius={[4, 4, 0, 0]}
                  name="Total reviews"
                />
                <Bar
                  dataKey="correct"
                  fill="#2F5D50"
                  radius={[4, 4, 0, 0]}
                  name="Correct"
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </>
      ) : (
        <p className="text-ledger-faint">Could not load stats.</p>
      )}
    </NavShell>
  );
}

function StatCard({
  label,
  value,
  accent,
}: {
  label: string;
  value: string | number;
  accent: "signal" | "ink" | "recall";
}) {
  const accentClass = {
    signal: "text-signal",
    ink: "text-ledger-ink",
    recall: "text-recall",
  }[accent];
  return (
    <div className="bg-ledger-card border border-ledger-rule rounded-card shadow-card px-4 py-4">
      <p className="font-mono text-[11px] uppercase tracking-widest text-ledger-faint">
        {label}
      </p>
      <p className={`font-display text-3xl font-semibold mt-1 ${accentClass}`}>
        {value}
      </p>
    </div>
  );
}
