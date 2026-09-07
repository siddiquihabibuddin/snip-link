import { describe, expect, it } from 'vitest'
import { formatShortUrl, timeAgo } from '../format'

describe('formatShortUrl', () => {
  it('strips http and https protocols', () => {
    expect(formatShortUrl('https://short.test/aB3x9')).toBe('short.test/aB3x9')
    expect(formatShortUrl('http://short.test/aB3x9')).toBe('short.test/aB3x9')
  })

  it('strips a trailing slash', () => {
    expect(formatShortUrl('https://short.test/')).toBe('short.test')
  })

  it('leaves a protocol-less string untouched', () => {
    expect(formatShortUrl('short.test/x')).toBe('short.test/x')
  })
})

describe('timeAgo', () => {
  const now = new Date('2026-01-01T12:00:00Z')

  it('reports recent timestamps as "just now"', () => {
    expect(timeAgo('2026-01-01T11:59:30Z', now)).toBe('just now')
  })

  it('reports minutes, hours and days', () => {
    expect(timeAgo('2026-01-01T11:30:00Z', now)).toBe('30m ago')
    expect(timeAgo('2026-01-01T09:00:00Z', now)).toBe('3h ago')
    expect(timeAgo('2025-12-30T12:00:00Z', now)).toBe('2d ago')
  })

  it('returns an empty string for an unparseable value', () => {
    expect(timeAgo('not-a-date', now)).toBe('')
  })
})
