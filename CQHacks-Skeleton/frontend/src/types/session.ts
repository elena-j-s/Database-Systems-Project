export type SessionStandingsRow = {
  position: number;
  name: string;
  points: number;
  driverNumber: number;
  teamColor: string;
  abbreviation: string;
  firstName: string;
  lastName: string;
  team: string;
  countryCode: string;
  headshotUrl: string;
};

export type SessionFeaturedDriver = {
  driverNumber: number;
  abbreviation: string;
  firstName: string;
  lastName: string;
  team: string;
  countryCode: string;
  headshotUrl: string;
  teamColor: string;
};

export type SessionPayload = {
  round: number;
  eventName: string;
  location: string;
  country: string;
  standings: SessionStandingsRow[];
  insights: string[];
  featuredDriver: SessionFeaturedDriver;
};
