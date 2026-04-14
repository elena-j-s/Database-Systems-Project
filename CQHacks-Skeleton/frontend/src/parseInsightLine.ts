/** Split API insight strings like "Fastest lap: 1:23.4 by …" into label + value for stat cards. */
export function parseInsightLine(line: string): { label: string; value: string } {
  const t = line.trim();
  const idx = t.indexOf(":");
  if (idx <= 0 || idx >= t.length - 1) {
    return { label: "Insight", value: t };
  }
  const label = t.slice(0, idx).trim();
  const value = t.slice(idx + 1).trim();
  if (!label) {
    return { label: "Insight", value: t };
  }
  return { label, value };
}
