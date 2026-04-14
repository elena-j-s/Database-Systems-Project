import { useEffect, useMemo, useState } from "react";
import { apiUrl } from "./apiBase.ts";
import "./App.css";

type Row = Record<string, string>;

async function fetchJson<T>(url: string): Promise<T> {
  const res = await fetch(url);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }
  return res.json() as Promise<T>;
}

function parseRound(row: Row): number | null {
  const r = row["Round"]?.trim();
  if (!r) return null;
  const n = Number.parseFloat(r);
  return Number.isFinite(n) ? n : null;
}

type Props = {
  onBack: () => void;
};

export default function RaceLiveView({ onBack }: Props) {
  const [rows, setRows] = useState<Row[]>([]);
  const [lapRows, setLapRows] = useState<Row[]>([]);
  const [loading, setLoading] = useState(true);
  const [lapLoading, setLapLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lapError, setLapError] = useState<string | null>(null);
  const [roundFilter, setRoundFilter] = useState<string>("all");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    fetchJson<Row[]>(apiUrl("/api/race-results"))
      .then((data) => {
        if (!cancelled) setRows(Array.isArray(data) ? data : []);
      })
      .catch((e: Error) => {
        if (!cancelled) setError(e.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const rounds = useMemo(() => {
    const s = new Set<number>();
    for (const row of rows) {
      const r = parseRound(row);
      if (r != null) s.add(r);
    }
    return [...s].sort((a, b) => a - b);
  }, [rows]);

  const filtered = useMemo(() => {
    if (roundFilter === "all") return rows;
    const want = Number.parseInt(roundFilter, 10);
    return rows.filter((row) => parseRound(row) === want);
  }, [rows, roundFilter]);

  useEffect(() => {
    let cancelled = false;
    if (roundFilter === "all") {
      setLapRows([]);
      return;
    }
    const round = Number.parseInt(roundFilter, 10);
    setLapLoading(true);
    setLapError(null);
    fetchJson<Row[]>(apiUrl(`/api/lap-times?round=${round}&limit=200`))
      .then((data) => {
        if (!cancelled) setLapRows(Array.isArray(data) ? data : []);
      })
      .catch((e: Error) => {
        if (!cancelled) {
          setLapError(e.message);
          setLapRows([]);
        }
      })
      .finally(() => {
        if (!cancelled) setLapLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [roundFilter]);

  return (
    <div className="app race-live-app">
      <button type="button" className="back-link" onClick={onBack}>
        ← Back to Race Selector
      </button>
      <header className="header">
        <h1 className="title">Race results viewer</h1>
        <p className="subtitle">Joined data from data/*.csv (Driver, Team, Race, Race Results)</p>
      </header>

      <section className="toolbar">
        <label className="field">
          <span>Round</span>
          <select
            value={roundFilter}
            onChange={(e) => setRoundFilter(e.target.value)}
          >
            <option value="all">All rounds</option>
            {rounds.map((r) => (
              <option key={r} value={String(r)}>
                Round {r}
              </option>
            ))}
          </select>
        </label>
      </section>

      {loading && <p className="muted">Loading race results…</p>}
      {error && <p className="err">Error: {error}</p>}

      {!loading && !error && (
        <section className="panel">
          <h2>Race results</h2>
          <div className="table-wrap">
            <table className="grid">
              <thead>
                <tr>
                  <th>Pos</th>
                  <th>Driver</th>
                  <th>Team</th>
                  <th>Time / gap</th>
                  <th>Event</th>
                  <th>Rnd</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((row, i) => (
                  <tr key={`${row["DriverId"] ?? "d"}-${row["Round"] ?? ""}-${i}`}>
                    <td>{row["ClassifiedPosition"] ?? row["Position"] ?? ""}</td>
                    <td>{row["BroadcastName"] ?? row["Abbreviation"] ?? ""}</td>
                    <td>
                      <span
                        className="team"
                        style={{
                          borderLeftColor: `#${row["TeamColor"] ?? "666"}`,
                        }}
                      >
                        {row["TeamName"] ?? ""}
                      </span>
                    </td>
                    <td className="mono">{row["Time"] ?? row["ElapsedTime"] ?? ""}</td>
                    <td>{row["Event Name"] ?? ""}</td>
                    <td>{row["Round"] ?? ""}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="muted small">
            Showing {filtered.length} row{filtered.length === 1 ? "" : "s"}
            {roundFilter !== "all" ? ` (round ${roundFilter})` : ""}.
          </p>
        </section>
      )}

      {roundFilter !== "all" && (
        <section className="panel">
          <h2>Lap times (sample)</h2>
          {lapLoading && <p className="muted">Loading lap times…</p>}
          {lapError && <p className="err">Error: {lapError}</p>}
          {!lapLoading && !lapError && lapRows.length === 0 && (
            <p className="muted small">
              No per-lap telemetry in the current dataset (normalized CSVs have no lap-level table yet).
            </p>
          )}
          {!lapLoading && !lapError && lapRows.length > 0 && (
            <>
              <div className="table-wrap">
                <table className="grid compact">
                  <thead>
                    <tr>
                      <th>Lap</th>
                      <th>Driver</th>
                      <th>Lap time</th>
                      <th>Compound</th>
                      <th>Pos</th>
                    </tr>
                  </thead>
                  <tbody>
                    {lapRows.map((row, i) => (
                      <tr key={`${row["Driver"]}-${row["LapNumber"]}-${i}`}>
                        <td>{row["LapNumber"] ?? ""}</td>
                        <td>{row["Driver"] ?? ""}</td>
                        <td className="mono">{row["LapTime"] ?? ""}</td>
                        <td>{row["Compound"] ?? ""}</td>
                        <td>{row["Position"] ?? ""}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <p className="muted small">
                Showing up to 200 laps for round {roundFilter} (see API limit).
              </p>
            </>
          )}
        </section>
      )}
    </div>
  );
}
