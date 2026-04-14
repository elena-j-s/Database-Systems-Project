import "./sessionComponents.css";

type Props = {
  number: number;
  abbreviation: string;
  firstName: string;
  lastName: string;
  team: string;
  country: string;
  headshotUrl?: string;
  teamColor?: string;
};

export default function FeaturedDriver({
  number,
  abbreviation,
  firstName,
  lastName,
  team,
  country,
  headshotUrl,
  teamColor = "#999",
}: Props) {
  return (
    <div className="session-featured">
      <div className="session-featured__row">
        {headshotUrl ? (
          <img className="session-featured__photo" src={headshotUrl} alt="" />
        ) : (
          <div className="session-featured__photo session-featured__photo--placeholder" style={{ borderColor: teamColor }} />
        )}
        <div className="session-featured__info">
          <div className="session-featured__topline">
            <div
              className="session-featured__badge"
              style={{
                borderColor: teamColor,
                boxShadow: `inset 0 0 0 1px rgba(255,255,255,0.06), 0 4px 14px ${teamColor}33`,
              }}
            >
              <span className="session-featured__num" style={{ color: teamColor }}>
                {number}
              </span>
            </div>
            <span className="session-featured__abbr">{abbreviation}</span>
          </div>
          <span className="session-featured__fullname">
            {firstName} {lastName}
          </span>
          <span className="session-featured__team">{team}</span>
          <span className="session-featured__country">{country}</span>
        </div>
      </div>
    </div>
  );
}
