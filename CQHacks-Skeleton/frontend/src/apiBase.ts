/** Production: set VITE_API_BASE_URL in Vercel (e.g. https://your-api.example.com). Dev: leave unset so /api uses the Vite proxy. */
const base =
  (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, "") ?? "";

export function apiUrl(path: string): string {
  const p = path.startsWith("/") ? path : `/${path}`;
  return `${base}${p}`;
}
