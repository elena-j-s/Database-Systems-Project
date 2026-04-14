/** Hardcoded calendar (matches CSV-style rounds; not fetched from API). */
export type RaceCard = {
  round: number;
  name: string;
  location: string;
};

export const HARDCODED_RACES: RaceCard[] = [
  { round: 1, name: "Australian Grand Prix", location: "Melbourne" },
  { round: 2, name: "Chinese Grand Prix", location: "Shanghai" },
  { round: 3, name: "Japanese Grand Prix", location: "Suzuka" },
  { round: 4, name: "Bahrain Grand Prix", location: "Sakhir" },
  { round: 5, name: "Saudi Arabian Grand Prix", location: "Jeddah" },
  { round: 6, name: "Miami Grand Prix", location: "Miami" },
  { round: 7, name: "Emilia Romagna Grand Prix", location: "Imola" },
  { round: 8, name: "Monaco Grand Prix", location: "Monte Carlo" },
  { round: 9, name: "Spanish Grand Prix", location: "Barcelona" },
  { round: 10, name: "Canadian Grand Prix", location: "Montreal" },
  { round: 11, name: "Austrian Grand Prix", location: "Spielberg" },
  { round: 12, name: "British Grand Prix", location: "Silverstone" },
  { round: 13, name: "Belgian Grand Prix", location: "Spa" },
  { round: 14, name: "Hungarian Grand Prix", location: "Budapest" },
  { round: 15, name: "Dutch Grand Prix", location: "Zandvoort" },
  { round: 16, name: "Italian Grand Prix", location: "Monza" },
  { round: 17, name: "Azerbaijan Grand Prix", location: "Baku" },
  { round: 18, name: "Singapore Grand Prix", location: "Singapore" },
  { round: 19, name: "United States Grand Prix", location: "Austin" },
  { round: 20, name: "Mexico City Grand Prix", location: "Mexico City" },
  { round: 21, name: "São Paulo Grand Prix", location: "São Paulo" },
  { round: 22, name: "Las Vegas Grand Prix", location: "Las Vegas" },
  { round: 23, name: "Qatar Grand Prix", location: "Lusail" },
  { round: 24, name: "Abu Dhabi Grand Prix", location: "Yas Marina" },
];

export function raceForRound(round: number): RaceCard | undefined {
  return HARDCODED_RACES.find((r) => r.round === round);
}

export function thumbGradientForRound(round: number): string {
  const hue = (round * 47) % 360;
  const hue2 = (hue + 40) % 360;
  return `linear-gradient(145deg, hsl(${hue} 55% 32%) 0%, hsl(${hue2} 45% 22%) 50%, hsl(${hue} 30% 12%) 100%)`;
}

/** Photo for round N: put `frontend/public/asset/tracks/N.jpg` (see README in that folder). */
export function trackPhotoUrl(round: number): string {
  const base = import.meta.env.BASE_URL;
  const prefix = base.endsWith("/") ? base : `${base}/`;
  return `${prefix}asset/tracks/${round}.jpg`;
}
