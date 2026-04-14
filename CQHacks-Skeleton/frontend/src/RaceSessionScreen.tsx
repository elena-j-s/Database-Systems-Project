import { useCallback, useEffect, useMemo, useState } from "react";
import { apiUrl } from "./apiBase.ts";
import { raceForRound, type RaceCard } from "./racesData.ts";
import { parseInsightLine } from "./parseInsightLine.ts";
import type { SessionPayload } from "./types/session.ts";
import DriverCard from "./components/session/DriverCard.tsx";
import FeaturedDriver from "./components/session/FeaturedDriver.tsx";
import "./RaceSessionScreen.css";

type Props = {
  round: number;
  onBack: () => void;
  onOpenRaceLive: () => void;
};

type ModalTab = "code" | "output";

async function fetchText(url: string): Promise<string> {
  const res = await fetch(url);
  if (!res.ok) {
    const t = await res.text();
    throw new Error(t || res.statusText);
  }
  return res.text();
}

async function fetchSession(round: number): Promise<SessionPayload> {
  const res = await fetch(apiUrl(`/api/session/${encodeURIComponent(String(round))}`));
  if (!res.ok) {
    const t = await res.text();
    throw new Error(t || res.statusText);
  }
  return res.json() as Promise<SessionPayload>;
}

export default function RaceSessionScreen({ round, onBack, onOpenRaceLive }: Props) {
  const [calendarHint, setCalendarHint] = useState<RaceCard | undefined>(() => raceForRound(round));

  useEffect(() => {
    let cancelled = false;
    fetch(apiUrl("/api/races"))
      .then((r) => (r.ok ? r.json() : []))
      .then((data: unknown) => {
        if (cancelled || !Array.isArray(data)) return;
        const match = (data as RaceCard[]).find((c) => c.round === round);
        if (match) setCalendarHint(match);
        else setCalendarHint(raceForRound(round));
      })
      .catch(() => {
        if (!cancelled) setCalendarHint(raceForRound(round));
      });
    return () => {
      cancelled = true;
    };
  }, [round]);
  const [session, setSession] = useState<SessionPayload | null>(null);
  const [sessionError, setSessionError] = useState<string | null>(null);
  const [sessionLoading, setSessionLoading] = useState(true);

  const [modalOpen, setModalOpen] = useState(false);
  const [tab, setTab] = useState<ModalTab>("code");
  const [codeText, setCodeText] = useState<string | null>(null);
  const [outputText, setOutputText] = useState<string | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [selectedDriverNumber, setSelectedDriverNumber] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;
    setSessionLoading(true);
    setSessionError(null);
    fetchSession(round)
      .then((data) => {
        if (!cancelled) {
          setSession(data);
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setSessionError(e instanceof Error ? e.message : "Failed to load session");
          setSession(null);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setSessionLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [round]);

  useEffect(() => {
    if (!session?.standings?.length) {
      setSelectedDriverNumber(null);
      return;
    }
    setSelectedDriverNumber((prev) => {
      if (prev != null && session.standings.some((s) => s.driverNumber === prev)) {
        return prev;
      }
      return session.standings[0].driverNumber;
    });
  }, [session]);

  const openModal = useCallback(() => {
    setModalOpen(true);
    setLoadError(null);
    setCodeText(null);
    setOutputText(null);
    setTab("code");
    setLoading(true);
    Promise.all([
      fetchText(apiUrl("/api/demo/main-source")),
      fetchText(apiUrl(`/api/demo/main-output?round=${encodeURIComponent(String(round))}`)),
    ])
      .then(([code, out]) => {
        setCodeText(code);
        setOutputText(out);
      })
      .catch((e: unknown) => {
        setLoadError(e instanceof Error ? e.message : "Request failed");
      })
      .finally(() => setLoading(false));
  }, [round]);

  const closeModal = useCallback(() => {
    setModalOpen(false);
  }, []);

  const eventTitle = session?.eventName ?? calendarHint?.name ?? "Grand Prix";
  const eventCity = session?.location ?? calendarHint?.location ?? "";

  const selectedStanding = useMemo(() => {
    if (!session?.standings?.length || selectedDriverNumber == null) {
      return undefined;
    }
    return session.standings.find((s) => s.driverNumber === selectedDriverNumber);
  }, [session, selectedDriverNumber]);

  return (
    <div className="race-session race-session--expanded">
      <button type="button" className="race-session__back" onClick={onBack}>
        ← Back to Race Selector
      </button>

      <header className="race-session__header">
        <h1 className="race-session__brand">Season 2025 F1 Viewer</h1>
      </header>

      <div className="race-session__banner">
        <h2 className="race-session__banner-title">{eventTitle}</h2>
        <p className="race-session__banner-sub">
          {eventCity}
          {session?.country ? <span className="race-session__banner-dot"> · {session.country}</span> : null}
        </p>
      </div>

      <main className="race-session__layout">
        <section className="race-session__panel race-session__panel--results" aria-label="Race results">
          {sessionLoading && <p className="race-session__hint">Loading standings…</p>}
          {sessionError && (
            <p className="race-session__warn" role="alert">
              {sessionError}
            </p>
          )}
          {!sessionLoading && !sessionError && session && (
            <div className="race-session__grid">
              {session.standings.map((row) => (
                <DriverCard
                  key={`${row.driverNumber}-${row.position}`}
                  position={row.position}
                  name={row.name}
                  points={row.points}
                  color={row.teamColor}
                  headshotUrl={row.headshotUrl || undefined}
                  selected={selectedDriverNumber === row.driverNumber}
                  onSelect={() => setSelectedDriverNumber(row.driverNumber)}
                />
              ))}
            </div>
          )}
        </section>

        <aside className="race-session__aside" aria-label="Featured driver and insights">
          <div className="race-session__featured-wrap">
            <h3 className="race-session__panel-title">Featured driver</h3>
            {selectedStanding && (
              <FeaturedDriver
                number={selectedStanding.driverNumber}
                abbreviation={selectedStanding.abbreviation || "—"}
                firstName={selectedStanding.firstName}
                lastName={selectedStanding.lastName}
                team={selectedStanding.team}
                country={selectedStanding.countryCode}
                headshotUrl={selectedStanding.headshotUrl || undefined}
                teamColor={selectedStanding.teamColor}
              />
            )}
            {!sessionLoading && session && session.standings.length === 0 && (
              <p className="race-session__hint">No driver data for this session.</p>
            )}
          </div>

          <div className="race-session__insights">
            <h3 className="race-session__panel-title">Insights</h3>
            <div className="race-session__insights-scroll">
              <div className="race-session__insights-grid" role="list">
                {(session?.insights ?? []).map((line, i) => {
                  const { label, value } = parseInsightLine(line);
                  return (
                    <div key={`${i}-${line.slice(0, 20)}`} className="race-session__insight-stat" role="listitem">
                      <span className="race-session__insight-stat-label">{label}</span>
                      <span className="race-session__insight-stat-value">{value}</span>
                    </div>
                  );
                })}
              </div>
            </div>
            {!sessionLoading && session && session.insights.length === 0 && (
              <p className="race-session__hint">No insights generated.</p>
            )}
          </div>
        </aside>
      </main>

      <div className="race-session__actions">
        <button type="button" className="race-session__cta race-session__cta--secondary" onClick={openModal}>
          View team Main.java
        </button>
        <button type="button" className="race-session__cta" onClick={onOpenRaceLive}>
          CSV Viewer (simple)
        </button>
      </div>

      {modalOpen && (
        <div className="race-session-modal" role="dialog" aria-modal="true" aria-label="Backend Main.java demo">
          <div className="race-session-modal__backdrop" onClick={closeModal} aria-hidden />
          <div className="race-session-modal__panel">
            <header className="race-session-modal__head">
              <h2 className="race-session-modal__title">Backend team Main → Spring Boot</h2>
              <button type="button" className="race-session-modal__close" onClick={closeModal} aria-label="Close">
                ×
              </button>
            </header>
            <div className="race-session-modal__tabs" role="tablist">
              <button
                type="button"
                role="tab"
                aria-selected={tab === "code"}
                className={tab === "code" ? "race-session-modal__tab race-session-modal__tab--on" : "race-session-modal__tab"}
                onClick={() => setTab("code")}
              >
                Source code
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={tab === "output"}
                className={tab === "output" ? "race-session-modal__tab race-session-modal__tab--on" : "race-session-modal__tab"}
                onClick={() => setTab("output")}
              >
                Console-style output
              </button>
            </div>
            <div className="race-session-modal__body">
              {loading && <p className="race-session-modal__status">Loading…</p>}
              {loadError && <p className="race-session-modal__error">{loadError}</p>}
              {!loading && !loadError && tab === "code" && (
                <pre className="race-session-modal__pre">{codeText ?? ""}</pre>
              )}
              {!loading && !loadError && tab === "output" && (
                <pre className="race-session-modal__pre">{outputText ?? ""}</pre>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
