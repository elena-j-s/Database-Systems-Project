import "./sessionComponents.css";

type Props = {
  position: number;
  name: string;
  points: number;
  color?: string;
  headshotUrl?: string;
  selected?: boolean;
  onSelect: () => void;
};

export default function DriverCard({
  position,
  name,
  points,
  color = "#8c8c8c",
  headshotUrl,
  selected = false,
  onSelect,
}: Props) {
  return (
    <button
      type="button"
      className={`session-driver-card${selected ? " session-driver-card--selected" : ""}`}
      onClick={onSelect}
      aria-pressed={selected}
      aria-label={`${name}, position ${position}, ${points} points. Set as featured driver.`}
    >
      <div className="session-driver-card__inner">
        <div className="session-driver-card__left">
          <span className="session-driver-card__pos">{position}</span>
          {headshotUrl ? (
            <img className="session-driver-card__img" src={headshotUrl} alt="" />
          ) : (
            <span className="session-driver-card__swatch" style={{ background: color }} />
          )}
          <span className="session-driver-card__name">{name}</span>
        </div>
        <span className="session-driver-card__pts">{Number.isInteger(points) ? points : points.toFixed(1)}</span>
      </div>
    </button>
  );
}
