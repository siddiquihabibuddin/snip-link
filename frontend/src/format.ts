/**
 * Display helpers for links. Pure functions, no React - easy to unit test.
 */

/** Strip the protocol from a short URL for a compact display, e.g. "short.test/aB3x9". */
export function formatShortUrl(shortUrl: string): string {
  return shortUrl.replace(/^https?:\/\//, '').replace(/\/$/, '')
}

/** Coarse "time ago" label from an ISO timestamp. */
export function timeAgo(iso: string, now: Date = new Date()): string {
  const then = new Date(iso).getTime()
  if (Number.isNaN(then)) return ''
  const seconds = Math.max(0, Math.round((now.getTime() - then) / 1000))
  if (seconds < 45) return 'just now'
  const minutes = Math.round(seconds / 60)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.round(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.round(hours / 24)
  return `${days}d ago`
}
