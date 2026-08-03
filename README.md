# Email Delivery Platform

A production-ready email campaign delivery platform: manage templates, contacts, campaigns and API keys, with tracking analytics and a clean dashboard UI.

## Features

- **Campaign management** — create, schedule, send and track email campaigns
- **Contact management** — segmented contact lists with metadata
- **Template studio** — reusable, variable-powered email templates
- **Analytics** — per-campaign open / click / sent metrics
- **API keys** — programmatic access with scoped keys
- **JWT authentication & roles** — stateless auth with ADMIN / USER roles
- **Security hardened** — Helmet-equivalent secure headers, rate limiting, CORS allow-lists, input validation, environment validation
- **Swagger/OpenAPI docs** at `/swagger`
- **Health & readiness endpoints** for orchestrators and uptime monitors
- **PostgreSQL** (production) with H2 for local development and tests
- **Automated CI/CD** — lint, test, build, containerize and deploy on every push to `main`

## Architecture

```
Browser (React/Vite on Vercel)
        │  HTTPS
        ▼
Backend API (Spring Boot on Render/Railway)
        │  JPA / HikariCP
        ▼
PostgreSQL (Neon / Render Postgres)
        │
        ├─ SMTP provider (campaign sends)
        └─ Optional object storage for attachments (future)
```

- **Frontend** — React + Vite + MUI, deployed separately to Vercel
- **Backend** — Spring Boot 3 (Java 17), stateless JWT security
- **Database** — PostgreSQL in production; embedded H2 for local development
- **Deployment** — Docker multi-stage image; GitHub Actions pipeline; Render (primary) / Railway (secondary)

## Folder Structure

```
email-delivery-platform/
├── backend/
│   ├── src/main/java/com/emailplatform/
│   │   ├── config/        # Security, CORS, JWT, rate limiting, OpenAPI, DB, seeder
│   │   ├── controller/    # REST endpoints + health/readiness
│   │   ├── dto/           # Request/response contracts
│   │   ├── model/         # JPA entities
│   │   ├── repository/    # Spring Data repositories
│   │   └── service/       # Business logic
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   ├── sql/               # V1__init.sql migration + seed.sql
│   ├── scripts/           # migrate.sh / seed.sh
│   ├── checkstyle.xml     # Lint rules
│   ├── Dockerfile
│   ├── .env.example
│   └── pom.xml
├── frontend/
│   ├── src/               # React components, pages, services
│   ├── vercel.json
│   └── .env.example
├── .github/workflows/ci.yml
├── docker-compose.yml
├── render.yaml
├── .env.example
└── README.md / DEPLOYMENT.md
```

## Tech Stack

| Layer      | Technology                                        |
|------------|---------------------------------------------------|
| Backend    | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database   | PostgreSQL (prod), H2 (dev/test), HikariCP pool   |
| Auth       | JWT (jjwt 0.12), BCrypt                           |
| API docs   | springdoc-openapi (Swagger UI)                    |
| Frontend   | React 18, Vite 5, MUI, Axios                      |
| Ops        | Docker, GitHub Actions, Render, Railway, Vercel   |

## Installation

Prerequisites: JDK 17, Maven 3.9+, Node.js 20+.

```bash
# Backend
cd backend
mvn -B clean package

# Frontend
cd frontend
npm ci
```

## Environment Variables

Copy the templates and fill in values — never commit `.env`:

```bash
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
cp .env.example .env          # for docker compose
```

| Variable                  | Required | Description                                        |
|---------------------------|----------|----------------------------------------------------|
| `DATABASE_URL`            | prod     | `postgres://user:pass@host:5432/db?sslmode=require` |
| `JWT_SECRET`              | prod     | ≥32 chars random string (e.g. `openssl rand -base64 48`) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | no | Bootstraps an admin account on first start      |
| `CORS_ALLOWED_ORIGINS`    | prod     | Comma-separated browser origins                     |
| `SMTP_HOST/PORT/USER/PASSWORD` | for sending | SMTP relay credentials                    |
| `PORT`                    | no       | Port to listen on (injected by Render/Railway)      |
| `VITE_API_URL` (frontend) | prod     | Public backend base URL, e.g. `https://<app>.onrender.com/api` |

See `backend/.env.example`, `frontend/.env.example` and `.env.example` for the full list.

## Running Locally

```bash
# Terminal 1 - backend (uses embedded H2, no database setup)
cd backend
mvn -B spring-boot:run

# Terminal 2 - frontend (Vite dev server proxies /api -> :5000)
cd frontend
npm run dev
```

Open http://localhost:5173. Backend API on http://localhost:5000.

### Local full stack with PostgreSQL (Docker)

```bash
cp .env.example .env
docker compose up --build
```

The Postgres container applies `backend/sql/*.sql` automatically on first boot.

## Deployment

Full guide in [DEPLOYMENT.md](./DEPLOYMENT.md). TL;DR:

1. **Backend → Render**: connect the GitHub repo; `render.yaml` provisions the service + managed Postgres automatically.
2. **Frontend → Vercel**: import the repo, set `VITE_API_URL`, framework is auto-detected (`vercel.json`).
3. **One command per push**: the GitHub Actions workflow lints, tests, builds the Docker image and triggers deploys.

### Live URLs (portfolio)

| What            | URL                                       |
|-----------------|-------------------------------------------|
| Frontend        | `https://email-delivery-platform.vercel.app` |
| Backend API     | `https://email-delivery-platform-api.onrender.com` |
| Swagger         | `https://email-delivery-platform-api.onrender.com/swagger` |
| GitHub          | `https://github.com/<org>/email-delivery-platform` |

Run `../project_host/scripts/portfolio-urls.ps1` (Windows) or `.sh` (Unix) after deploying to print this table.

## API Documentation

Interactive Swagger UI is exposed at:

```
https://<backend-url>/swagger
```

Raw spec: `https://<backend-url>/v3/api-docs`. Authenticated endpoints take a JWT bearer token (`POST /api/auth/login`).

## Monitoring

- `GET /api/health` — liveness probe
- `GET /api/ready` — readiness probe (validates the database connection)
- Structured JSON logs; request + error logging wired in the pipeline config

## Screenshots

> Screenshots placeholder — replace with dashboard, campaign and analytics captures.

## Future Improvements

- Email attachment support with object storage (S3/R2)
- Background queue (e.g. RabbitMQ) for bulk campaign sends
- Per-tenant API key scopes and quotas
- Webhook delivery receipts
- Multi-region database read replicas

## License

Private project.
