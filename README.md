# sniplink

A tiny URL shortener built as a hands-on **CI/CD learning project**. The app is
deliberately small; the interesting part is `.github/workflows/`.

[![CI](https://github.com/siddiquihabibuddin/snip-link/actions/workflows/ci.yml/badge.svg)](https://github.com/siddiquihabibuddin/snip-link/actions/workflows/ci.yml)
[![CD](https://github.com/siddiquihabibuddin/snip-link/actions/workflows/cd.yml/badge.svg)](https://github.com/siddiquihabibuddin/snip-link/actions/workflows/cd.yml)
[![CodeQL](https://github.com/siddiquihabibuddin/snip-link/actions/workflows/codeql.yml/badge.svg)](https://github.com/siddiquihabibuddin/snip-link/actions/workflows/codeql.yml)

## What it does

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/links` `{ "url": "..." }` | create a short code |
| `GET`  | `/{code}` | 302 redirect to the original URL |
| `GET`  | `/api/links` | list created links (newest first) |
| `GET`  | `/actuator/health` | health probe for deploys / smoke tests |

Links are held in memory (no database yet — swapping in Postgres is a planned exercise).

## Stack

- **backend/** — Spring Boot 3.5 / Java 21, Maven, JUnit + MockMvc, JaCoCo. Multi-stage `Dockerfile` (Alpine JRE, non-root).
- **frontend/** — React 19 + TypeScript + Vite, Vitest + Testing Library, oxlint. Served as static files behind nginx.

## Run it locally

```bash
# whole stack (backend image + frontend behind nginx)
docker compose up --build
# open http://localhost:5173
```

Or run each side directly:

```bash
# backend  ->  http://localhost:8080
cd backend && ./mvnw spring-boot:run      # or: mvn spring-boot:run

# frontend ->  http://localhost:5173
cd frontend && cp .env.example .env && npm install && npm run dev
```

### Test / lint / build

```bash
cd backend  && mvn verify                 # unit + MockMvc tests + coverage
cd frontend && npm run lint && npm test && npm run build
```

## The pipeline

### CI — `.github/workflows/ci.yml` (on every PR and push to `main`)

- `changes` — path filter: skip backend or frontend jobs when that folder is untouched
- `backend` — `mvn verify` on a **JDK 21 + 25 matrix**, publishes a JUnit check + uploads the JaCoCo report
- `frontend` — `npm ci` → lint → test → build, uploads `dist/`
- `docker-build` — builds the backend image (no push) with a GHA layer cache
- `ci-ok` — aggregate gate so branch protection has one stable required check

Enable branch protection on `main` requiring `ci-ok` + 1 review (see setup below).

### CD — `.github/workflows/cd.yml` (on push to `main`)

1. `image` — build + push `ghcr.io/siddiquihabibuddin/snip-link-backend:{sha,latest}` to GitHub Container Registry (free)
2. `deploy-backend` — behind the **`production` environment** (manual approval): fire the Render deploy hook, then poll `/actuator/health` as a smoke test
3. `deploy-frontend` — build with the production `VITE_API_BASE`, deploy to Cloudflare Pages, curl the URL

### Extras

- `.github/dependabot.yml` — weekly Maven / npm / Actions / Docker update PRs
- `.github/workflows/codeql.yml` — CodeQL SAST for Java + TypeScript on PRs and weekly

## One-time setup

1. **Push to GitHub** (repo is public — free Actions minutes + CodeQL):
   ```bash
   git push -u origin main
   ```
2. **Branch protection** — Settings → Branches → add rule for `main`: require `ci-ok`, require a PR review, require linear history.
3. **Backend host (Render)** — New → Blueprint from this repo (`render.yaml`). After it exists:
   - copy its **Deploy Hook** URL → repo secret `RENDER_DEPLOY_HOOK`
   - set repo **variable** `BACKEND_URL` to its public URL
   - set the service env vars `APP_BASE_URL` (its own URL) and `APP_CORS_ALLOWED_ORIGINS` (the Pages URL)
   - make the GHCR package public (repo → Packages → package settings)
4. **Frontend host (Cloudflare Pages)** — create a Pages project named `snip-link` (Direct Upload). Then repo secrets `CLOUDFLARE_API_TOKEN` (scope: *Account · Cloudflare Pages · Edit*) and `CLOUDFLARE_ACCOUNT_ID`, and repo variable `FRONTEND_URL`.
5. **Environment gate** — Settings → Environments → `production` → add yourself as a required reviewer.

| Kind | Name | Where |
|---|---|---|
| secret | `RENDER_DEPLOY_HOOK` | repo → Settings → Secrets → Actions |
| secret | `CLOUDFLARE_API_TOKEN` | " |
| secret | `CLOUDFLARE_ACCOUNT_ID` | " |
| variable | `BACKEND_URL` | repo → Settings → Variables → Actions |
| variable | `FRONTEND_URL` | " |

## Try the CI gate

Open a PR that breaks a test (e.g. change an assertion in `ShortCodeGeneratorTest`).
`ci-ok` goes red and the merge button is blocked. Fix it, watch it go green, merge —
and CD takes it to production after you approve the `production` environment.
