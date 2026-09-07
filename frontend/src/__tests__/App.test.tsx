import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import App from '../App'
import type { Link } from '../types'

const sample: Link = {
  code: 'aB3x9Z',
  url: 'https://example.com/a/long/path',
  shortUrl: 'http://localhost:8080/aB3x9Z',
  createdAt: new Date().toISOString(),
}

beforeEach(() => {
  vi.restoreAllMocks()
})

afterEach(() => {
  vi.unstubAllGlobals()
})

function stubFetch(impl: typeof fetch) {
  vi.stubGlobal('fetch', vi.fn(impl))
}

describe('<App>', () => {
  it('shows the empty state when there are no links', async () => {
    stubFetch(async () => new Response('[]', { status: 200 }))
    render(<App />)
    expect(await screen.findByText('No links yet.')).toBeInTheDocument()
  })

  it('creates a link and shows it in the list', async () => {
    stubFetch(async (_input, init) => {
      if (init?.method === 'POST') {
        return new Response(JSON.stringify(sample), { status: 201 })
      }
      return new Response('[]', { status: 200 })
    })

    render(<App />)
    await screen.findByText('No links yet.')

    await userEvent.type(
      screen.getByLabelText('URL to shorten'),
      'https://example.com/a/long/path',
    )
    await userEvent.click(screen.getByRole('button', { name: 'Shorten' }))

    expect(await screen.findByText('localhost:8080/aB3x9Z')).toBeInTheDocument()
  })

  it('surfaces a backend error message', async () => {
    stubFetch(async (_input, init) => {
      if (init?.method === 'POST') {
        return new Response(JSON.stringify({ message: 'url must be an absolute http(s) URL' }), {
          status: 400,
        })
      }
      return new Response('[]', { status: 200 })
    })

    render(<App />)
    await screen.findByText('No links yet.')

    await userEvent.type(screen.getByLabelText('URL to shorten'), 'https://bad.example')
    await userEvent.click(screen.getByRole('button', { name: 'Shorten' }))

    await waitFor(() =>
      expect(screen.getByRole('alert')).toHaveTextContent('url must be an absolute http(s) URL'),
    )
  })
})
