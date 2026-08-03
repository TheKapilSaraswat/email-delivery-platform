#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Seeds reference data into the target PostgreSQL database.
#
# Usage:
#   DATABASE_URL="postgres://user:pass@host:5432/dbname" ./seed.sh
#
# The admin account is created automatically by the application itself
# (DataSeeder) when ADMIN_EMAIL / ADMIN_PASSWORD are configured, so no
# credentials are handled by this script.
# ---------------------------------------------------------------------------
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_FILE="$(dirname "$SCRIPT_DIR")/sql/seed.sql"

if command -v psql >/dev/null 2>&1; then
  PSQL="psql"
elif [ -n "${PSQL_PATH:-}" ]; then
  PSQL="$PSQL_PATH"
else
  echo "ERROR: psql not found. Install the PostgreSQL client or set PSQL_PATH." >&2
  exit 1
fi

if [ -n "${DATABASE_URL:-}" ]; then
  CONN="${DATABASE_URL}"
elif [ -n "${DB_URL:-}" ]; then
  CONN="postgres:${DB_URL#jdbc:}"
  export PGPASSWORD="${DB_PASSWORD:-}"
  export PGUSER="${DB_USERNAME:-postgres}"
else
  echo "ERROR: Set DATABASE_URL or DB_URL/DB_USERNAME/DB_PASSWORD." >&2
  exit 1
fi

echo "Seeding reference data from $(basename "$SEED_FILE")"
"$PSQL" "$CONN" -v ON_ERROR_STOP=1 -q -f "$SEED_FILE"
echo "Seed complete."
