import { useCallback, useEffect, useRef, useState } from "react";
import Aurora from "./components/Aurora.tsx";
import { apiUrl } from "./apiBase.ts";
import {
  HARDCODED_RACES,
  thumbGradientForRound,
  trackPhotoUrl,
  type RaceCard,
} from "./racesData.ts";
import "./RaceHubScreen.css";

const RACE_SELECTOR_AURORA_STOPS = ["#ec0004", "#dce5e9", "#f50600"] as const;

type Props = {
  /** Called after the fullscreen transition animation (session / next step). */
  onRaceConfirmed: (round: number) => void;
  onBackToIntro: () => void;
};

export default function RaceHubScreen({ onRaceConfirmed, onBackToIntro }: Props) {
  const [races, setRaces] = useState<RaceCard[]>(HARDCODED_RACES);
  const [selected, setSelected] = useState<number | null>(null);
  const [entered, setEntered] = useState(false);
  const [fsRace, setFsRace] = useState<RaceCard | null>(null);
  const [gridCols, setGridCols] = useState(5);
  const cardRefs = useRef<(HTMLButtonElement | null)[]>([]);

  useEffect(() => {
    setSelected((s) => (s != null && s >= races.length ? null : s));
  }, [races]);

  useEffect(() => {
    let cancelled = false;
    fetch(apiUrl("/api/races"))
      .then((r) => (r.ok ? r.json() : Promise.reject(new Error(String(r.status)))))
      .then((data: unknown) => {
        if (cancelled || !Array.isArray(data) || data.length === 0) return;
        const first = data[0] as Record<string, unknown>;
        if (typeof first?.round === "number" && typeof first?.name === "string") {
          setRaces(data as RaceCard[]);
        }
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const mq = window.matchMedia("(max-width: 720px)");
    const sync = () => setGridCols(mq.matches ? 2 : 5);
    sync();
    mq.addEventListener("change", sync);
    return () => mq.removeEventListener("change", sync);
  }, []);

  useEffect(() => {
    const id = requestAnimationFrame(() => setEntered(true));
    return () => cancelAnimationFrame(id);
  }, []);

  const scrollCardIntoView = useCallback((i: number) => {
    cardRefs.current[i]?.scrollIntoView({
      behavior: "smooth",
      block: "nearest",
      inline: "nearest",
    });
  }, []);

  useEffect(() => {
    if (selected !== null) {
      scrollCardIntoView(selected);
    }
  }, [selected, scrollCardIntoView]);

  const beginTransition = useCallback((race: RaceCard) => {
    setFsRace(race);
  }, []);

  useEffect(() => {
    if (!fsRace) return;
    const round = fsRace.round;
    const t = window.setTimeout(() => {
      onRaceConfirmed(round);
    }, 780);
    return () => clearTimeout(t);
  }, [fsRace, onRaceConfirmed]);

  useEffect(() => {
    const max = Math.max(0, races.length - 1);
    const onKey = (e: KeyboardEvent) => {
      if (fsRace) {
        if (e.key === "Escape") {
          e.preventDefault();
          setFsRace(null);
        }
        return;
      }
      if (e.key === "ArrowRight") {
        e.preventDefault();
        setSelected((s) => (s === null ? 0 : Math.min(s + 1, max)));
      } else if (e.key === "ArrowLeft") {
        e.preventDefault();
        setSelected((s) => (s === null ? max : Math.max(s - 1, 0)));
      } else if (e.key === "ArrowDown") {
        e.preventDefault();
        setSelected((s) => (s === null ? 0 : Math.min(s + gridCols, max)));
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        setSelected((s) => (s === null ? max : Math.max(s - gridCols, 0)));
      } else         if (e.key === "Enter") {
        e.preventDefault();
        if (selected !== null) {
          beginTransition(races[selected]);
        }
      } else if (e.key === "Escape") {
        e.preventDefault();
        onBackToIntro();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [selected, fsRace, gridCols, beginTransition, onBackToIntro, races]);

  /** Double-click: first selects (red glow); second on same card confirms. Hover glow is CSS-only. */
  const handleCardDoubleClick = (i: number) => {
    if (selected === i) {
      beginTransition(races[i]);
    } else {
      setSelected(i);
    }
  };

  const race = selected !== null ? races[selected] : null;

  return (
    <div className={`race-hub ${entered ? "race-hub--visible" : ""}`}>
      <div className="race-hub__aurora" aria-hidden>
        <Aurora
          colorStops={[...RACE_SELECTOR_AURORA_STOPS]}
          blend={1.08}
          amplitude={1.2}
          speed={2}
        />
      </div>
      <div className="race-hub__veil" aria-hidden />
      <div className="race-hub__stars" aria-hidden />

      <header className="race-hub__header">
        <div className="race-hub__title-block">
          <h1 className="race-hub__title">Race Selector</h1>
          <p className="race-hub__subtitle">Hover to preview · double-click to select · double-click again or OK to enter</p>
        </div>
      </header>

      <section className="race-hub__section" aria-label="Race selection">
        <div className="race-hub__grid-scroll">
          <div
            className="race-hub__grid"
            role="listbox"
            aria-label="Races"
            aria-activedescendant={race ? `race-card-${race.round}` : undefined}
          >
            {races.map((r: RaceCard, i) => {
              const isSel = selected !== null && i === selected;
              return (
                <button
                  key={r.round}
                  ref={(el) => {
                    cardRefs.current[i] = el;
                  }}
                  type="button"
                  role="option"
                  aria-selected={isSel}
                  id={`race-card-${r.round}`}
                  className={`race-card ${isSel ? "race-card--selected" : ""}`}
                  onDoubleClick={() => handleCardDoubleClick(i)}
                >
                  <div
                    className="race-card__thumb"
                    style={{ background: thumbGradientForRound(r.round) }}
                  >
                    <img
                      src={trackPhotoUrl(r.round)}
                      alt=""
                      className="race-card__thumb-photo"
                      loading="lazy"
                      decoding="async"
                      onError={(e) => e.currentTarget.remove()}
                    />
                  </div>
                  <div className="race-card__label">
                    <span className="race-card__name">{r.name}</span>
                    <span className="race-card__loc">{r.location}</span>
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      </section>

      {fsRace && (
        <div
          className="race-hub__fs"
          role="dialog"
          aria-modal="true"
          aria-label={`Opening ${fsRace.name}`}
        >
          <div className="race-hub__fs-backdrop" aria-hidden />
          <div className="race-hub__fs-panel">
            <div
              className="race-hub__fs-thumb"
              style={{ background: thumbGradientForRound(fsRace.round) }}
            >
              <img
                src={trackPhotoUrl(fsRace.round)}
                alt=""
                className="race-hub__fs-thumb-photo"
                onError={(e) => e.currentTarget.remove()}
              />
            </div>
            <div className="race-hub__fs-meta">
              <h2 className="race-hub__fs-name">{fsRace.name}</h2>
              <p className="race-hub__fs-loc">{fsRace.location}</p>
            </div>
          </div>
        </div>
      )}

      <footer className="race-hub__footer">
        <button type="button" className="race-hub__pill" onClick={onBackToIntro}>
          <span className="race-hub__pill-arrow">←</span>
          <span>Back</span>
        </button>
        <button
          type="button"
          className="race-hub__pill race-hub__pill--primary"
          disabled={race === null}
          onClick={() => race && beginTransition(race)}
        >
          <span>OK</span>
          <span className="race-hub__pill-arrow">→</span>
        </button>
      </footer>
    </div>
  );
}
