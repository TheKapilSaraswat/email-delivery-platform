#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Applies SQL migrations to the target PostgreSQL database.
#
# Usage:
#   DATABASE_URL="postgres://user:pass@host:5432/dbname" ./migrate.sh
#   DB_URL="jdbc:postgresql://host:5432/dbname" DB_USERNAME=x DB_PASSWORD=y ./migrate.sh
#
# Requires: psql (PostgreSQL client)
# ---------------------------------------------------------------------------
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_DIR="$(dirname "$SCRIPT_DIR")/sql"

MIGRATIONS_DIR="${MIGRATIONS_DIR:-$SQL_DIR}"

if command -v psql >/dev/null 2>&1; then
  PSQL="psql"
elif [ -n "${PSQL_PATH:-}" ]; then
  PSQL="$PSQL_PATH"
else
  echo "ERROR: psql not found. Install the PostgreSQL client or set PSQL_PATH." >&2
  exit 1
fi

# Normalise the connection string: accept either a libpq URL or JDBC URL + creds.
if [ -n "${DATABASE_URL:-}" ]; then
  CONN="${DATABASE_URL}"
  export PGCONNECT_TIMEOUT="${PGCONNECT_TIMEOUT:-15}"
elif [ -n "${DB_URL:-}" ]; then
  # jdbc:postgresql://host:port/dbname -> postgres://host:port/dbname
  CONN="postgres:${DB_URL#jdbc:}"
  export PGPASSWORD="${DB_PASSWORD:-}"
  export PGUSER="${DB_USERNAME:-postgres}"
else
  echo "ERROR: Set DATABASE_URL or DB_URL/DB_USERNAME/DB_PASSWORD." >&2
  exit 1
fi

echo "Applying migrations from ${MIGRATIONS_DIR}"
for file in "${MIGRATIONS_DIR}"/V*__*.sql; do
  [ -e "$file" ] || { echo "No migrations found in ${MIGRATIONS_DIR}"; exit 0; }
  echo "==> Applying $(basename "$file")"
  "$PSQL" "$CONN" -v ON_ERROR_STOP=1 -q -f "$file"
done

echo "Migrations complete."
