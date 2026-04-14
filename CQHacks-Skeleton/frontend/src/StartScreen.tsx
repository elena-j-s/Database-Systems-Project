import { useCallback, useEffect, useRef, useState } from "react";
import "./StartScreen.css";

const INTRO_TOTAL_MS = 5000;
const INTRO_FADE_MS = 800;

type Props = {
  /** Fires after intro duration (with fade), or when the user skips with Enter / Space. */
  onIntroComplete: () => void;
};

/** Files live in frontend/public/asset/images/ — Vite serves them at BASE_URL + asset/images/... */
const SLIDE_FILES = ["1.jpg", "2.jpg", "3.jpg"] as const;

function publicImageUrl(file: string): string {
  const base = import.meta.env.BASE_URL;
  const prefix = base.endsWith("/") ? base : `${base}/`;
  return `${prefix}asset/images/${file}`;
}

const SLIDE_INTERVAL_MS = 1500;
const SLIDE_FADE_MS = 450;

export default function StartScreen({ onIntroComplete }: Props) {
  const [slide, setSlide] = useState(0);
  const [exiting, setExiting] = useState(false);
  const finishedRef = useRef(false);
  const fadeTimerRef = useRef<ReturnType<typeof setTimeout>>();
  const doneTimerRef = useRef<ReturnType<typeof setTimeout>>();

  const finishIntro = useCallback(() => {
    if (finishedRef.current) {
      return;
    }
    finishedRef.current = true;
    if (fadeTimerRef.current) {
      clearTimeout(fadeTimerRef.current);
    }
    if (doneTimerRef.current) {
      clearTimeout(doneTimerRef.current);
    }
    setExiting(true);
    onIntroComplete();
  }, [onIntroComplete]);

  useEffect(() => {
    const id = window.setInterval(() => {
      setSlide((s) => (s + 1) % SLIDE_FILES.length);
    }, SLIDE_INTERVAL_MS);
    return () => window.clearInterval(id);
  }, []);

  useEffect(() => {
    const fadeAt = Math.max(0, INTRO_TOTAL_MS - INTRO_FADE_MS);
    fadeTimerRef.current = setTimeout(() => {
      if (!finishedRef.current) {
        setExiting(true);
      }
    }, fadeAt);
    doneTimerRef.current = setTimeout(() => {
      if (!finishedRef.current) {
        finishedRef.current = true;
        onIntroComplete();
      }
    }, INTRO_TOTAL_MS);
    return () => {
      if (fadeTimerRef.current) {
        clearTimeout(fadeTimerRef.current);
      }
      if (doneTimerRef.current) {
        clearTimeout(doneTimerRef.current);
      }
    };
  }, [onIntroComplete]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        finishIntro();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [finishIntro]);

  return (
    <div className={`start-screen ${exiting ? "start-screen--exiting" : ""}`}>
      <div className="start-screen-media" aria-hidden>
        <div className="start-art-slides">
          {SLIDE_FILES.map((file, i) => (
            <img
              key={file}
              src={publicImageUrl(file)}
              alt=""
              loading={i === 0 ? "eager" : "lazy"}
              decoding="async"
              className={`start-art-slide ${i === slide ? "start-art-slide--visible" : ""}`}
              style={{ transitionDuration: `${SLIDE_FADE_MS}ms` }}
            />
          ))}
        </div>
        <div className="start-screen-media-overlay" />
      </div>

      <div className="start-layout">
        <div className="start-menu-panel">
          <p className="start-brand">Season 2025 F1 Viewer</p>
        </div>

        <div className="start-art-column" />
      </div>
    </div>
  );
}
