# Deployment Guide — Email Delivery Platform

This guide covers pushing the project to production with **Render (primary)**, **Railway (secondary)** and **Vercel (frontend)**, driven by an automated GitHub Actions pipeline.

## 0. Prerequisites

- A GitHub repository for this project (e.g. `your-org/email-delivery-platform`)
- Accounts on [Render](https://render.com), [Railway](https://railway.app) and [Vercel](https://vercel.com)
- A [Neon](https://neon.tech) project **or** use Render's managed Postgres (created automatically below)

## 1. One-time repository setup

```bash
git init
git add .
git commit -m "chore: production deployment pipeline"
git branch -M main
git remote add origin git@github.com:your-org/email-delivery-platform.git
git push -u origin main
```

Secrets (`.env`, `*.db`, build output) are already excluded by `.gitignore`.

## 2. Backend — Render (primary)

**Option A — Blueprint (recommended, fully automatic):**

1. In Render: **New → Blueprint** → select the GitHub repo.
2. Render reads `render.yaml`, creates `email-delivery-platform-api` (web) + `email-delivery-platform-db` (Postgres), and wires `DATABASE_URL` automatically.
3. Open **Env Groups** and set the required secrets (they have `sync: false` in the blueprint so Render will prompt):
   - `JWT_SECRET` — generate with `openssl rand -base64 48`
   - `ADMIN_EMAIL`, `ADMIN_PASSWORD`
   - `CORS_ALLOWED_ORIGINS` = `https://email-delivery-platform.vercel.app`
   - `APP_BASE_URL` = `https://email-delivery-platform-api.onrender.com`
   - `SMTP_*` for sending email
4. Every push to `main` auto-deploys. The workflow also fires an optional **Deploy Hook**:
   - Render → your service → **Deploy Hook** → copy URL → add as GitHub secret `RENDER_DEPLOY_HOOK`.

**Option B — Manual:** New → Web Service → connect repo → Root `./backend`, Dockerfile → add the env vars above.

> **Neon alternative:** create a Neon project, copy the pooled connection string into `DATABASE_URL` (`postgres://user:pass@host:5432/db?sslmode=require`). The app's `DatabaseConfig` converts it to a JDBC URL automatically.

## 3. Frontend — Vercel

1. **Add New Project** → import the GitHub repo.
2. Framework preset: **Vite** (picked up from `vercel.json`).
3. Environment variable: `VITE_API_URL` = `https://email-delivery-platform-api.onrender.com/api`.
4. Deploy. Every push to `main` triggers a new deployment automatically.
5. (Optional) Vercel → Settings → Git → **Deploy Hooks** → copy URL → GitHub secret `VERCEL_DEPLOY_HOOK`.

## 4. Backend — Railway (secondary)

1. **New Project** → Deploy from GitHub → point at the repo.
2. Add a **PostgreSQL** plugin, then set env vars on the service (reuse the same values as Render).
3. For pipeline deploys:
   - Create a Railway token (Account → Tokens) → GitHub secret `RAILWAY_TOKEN`.
   - GitHub secret `RAILWAY_SERVICE` = your service name/id.
   - The workflow runs `railway redeploy --service <RAILWAY_SERVICE>`.

## 5. Database migrations & seeds

The pipeline uses Hibernate `ddl-auto` plus committed SQL for explicit control:

- `backend/sql/V1__init.sql` — schema
- `backend/sql/seed.sql` — reference data (templates)
- Admin account — created automatically on first start via `ADMIN_EMAIL`/`ADMIN_PASSWORD`

Run manually against any environment:

```bash
DATABASE_URL="postgres://user:pass@host:5432/emailplatform" backend/scripts/migrate.sh
DATABASE_URL="postgres://user:pass@host:5432/emailplatform" backend/scripts/seed.sh
```

## 6. GitHub Actions pipeline

`.github/workflows/ci.yml` runs on every push to `main`:

1. **Gitleaks** secret scan
2. **Backend lint** — `mvn -B -Plint validate` (checkstyle)
3. **Backend test + build** — `mvn -B verify`
4. **Frontend build** — `npm ci && npm run build`
5. **Docker** — build multi-stage image, push to `ghcr.io/<repo>/email-platform-api`
6. **Deploy** — Render hook / Railway redeploy / Vercel hook (each skipped if its secret is absent)

### Required GitHub secrets (all optional — steps skip gracefully)

| Secret                 | Used for                       |
|------------------------|--------------------------------|
| `RENDER_DEPLOY_HOOK`   | Trigger Render blue deploy     |
| `RAILWAY_TOKEN`        | Railway CLI redeploy           |
| `RAILWAY_SERVICE`      | Railway service name           |
| `VERCEL_DEPLOY_HOOK`   | Trigger Vercel deploy          |

## 7. Local one-command run

```bash
docker compose up --build
```

## 8. Verify production

- Liveness: `curl https://email-delivery-platform-api.onrender.com/api/health`
- Readiness: `curl https://email-delivery-platform-api.onrender.com/api/ready`
- Docs: `https://email-delivery-platform-api.onrender.com/swagger`
- Frontend: `https://email-delivery-platform.vercel.app`

## 9. Portfolio URLs

After deployment, print the portfolio-ready link table:

```bash
# Windows
..\project_host\scripts\portfolio-urls.ps1
# Unix
../project_host/scripts/portfolio-urls.sh
```
