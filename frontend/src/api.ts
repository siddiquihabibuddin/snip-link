import type { Link } from './types'

// Base URL of the sniplink backend. Set at build time via VITE_API_BASE
// (see .env.example); falls back to the local dev server.
const API_BASE = (import.meta.env.VITE_API_BASE ?? 'http://localhost:8080').replace(/\/$/, '')

async function parseError(res: Response): Promise<string> {
  try {
    const body = await res.json()
    if (body && typeof body.message === 'string') return body.message
  } catch {
    /* ignore non-JSON error bodies */
  }
  return `Request failed (${res.status})`
}

export async function createLink(url: string): Promise<Link> {
  const res = await fetch(`${API_BASE}/api/links`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ url }),
  })
  if (!res.ok) throw new Error(await parseError(res))
  return res.json()
}

export async function listLinks(): Promise<Link[]> {
  const res = await fetch(`${API_BASE}/api/links`)
  if (!res.ok) throw new Error(await parseError(res))
  return res.json()
}
