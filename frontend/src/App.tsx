import { useEffect, useState } from 'react'
import './App.css'
import { createLink, listLinks } from './api'
import { formatShortUrl, timeAgo } from './format'
import type { Link } from './types'

export default function App() {
  const [url, setUrl] = useState('')
  const [links, setLinks] = useState<Link[]>([])
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [copied, setCopied] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    listLinks()
      .then((loaded) => {
        if (active) setLinks(loaded)
      })
      .catch((e: unknown) => {
        if (active) setError(e instanceof Error ? e.message : 'Could not load links')
      })
    return () => {
      active = false
    }
  }, [])

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const created = await createLink(url.trim())
      setLinks((prev) => [created, ...prev.filter((l) => l.code !== created.code)])
      setUrl('')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong')
    } finally {
      setSubmitting(false)
    }
  }

  async function copy(shortUrl: string) {
    await navigator.clipboard.writeText(shortUrl)
    setCopied(shortUrl)
    setTimeout(() => setCopied((c) => (c === shortUrl ? null : c)), 1500)
  }

  return (
    <main className="app">
      <header>
        <h1>sniplink</h1>
        <p className="tagline">Paste a long URL, get a short one.</p>
      </header>

      <form onSubmit={onSubmit}>
        <input
          type="url"
          name="url"
          placeholder="https://example.com/a/very/long/path"
          aria-label="URL to shorten"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          required
        />
        <button type="submit" disabled={submitting || url.trim() === ''}>
          {submitting ? 'Shortening…' : 'Shorten'}
        </button>
      </form>

      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}

      <ul className="links">
        {links.map((link) => (
          <li key={link.code}>
            <a href={link.shortUrl} target="_blank" rel="noreferrer" className="short">
              {formatShortUrl(link.shortUrl)}
            </a>
            <span className="original" title={link.url}>
              {link.url}
            </span>
            <span className="age">{timeAgo(link.createdAt)}</span>
            <button type="button" className="copy" onClick={() => void copy(link.shortUrl)}>
              {copied === link.shortUrl ? 'Copied' : 'Copy'}
            </button>
          </li>
        ))}
        {links.length === 0 && !error && <li className="empty">No links yet.</li>}
      </ul>
    </main>
  )
}
