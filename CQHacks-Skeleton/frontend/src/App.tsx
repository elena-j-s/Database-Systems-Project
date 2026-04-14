import { useCallback, useState } from "react";
import RaceHubScreen from "./RaceHubScreen.tsx";
import RaceLiveView from "./RaceLiveView.tsx";
import RaceSessionScreen from "./RaceSessionScreen.tsx";
import StartScreen from "./StartScreen.tsx";

type View = "intro" | "hub" | "session" | "race-live";

export default function App() {
  const [view, setView] = useState<View>("intro");
  const [sessionRound, setSessionRound] = useState<number | null>(null);

  const goHub = useCallback(() => setView("hub"), []);

  if (view === "intro") {
    return <StartScreen onIntroComplete={goHub} />;
  }

  if (view === "hub") {
    return (
      <RaceHubScreen
        onBackToIntro={() => setView("intro")}
        onRaceConfirmed={(round) => {
          setSessionRound(round);
          setView("session");
        }}
      />
    );
  }

  if (view === "session" && sessionRound !== null) {
    return (
      <RaceSessionScreen
        round={sessionRound}
        onBack={() => {
          setSessionRound(null);
          setView("hub");
        }}
        onOpenRaceLive={() => setView("race-live")}
      />
    );
  }

  return <RaceLiveView onBack={() => setView("hub")} />;
}
