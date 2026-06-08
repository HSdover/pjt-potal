#!/usr/bin/env bash
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-/data/backups/gitea}"
STAMP="$(date +%Y%m%d-%H%M%S)"

mkdir -p "${BACKUP_DIR}"
chown git:git "${BACKUP_DIR}"
chmod 750 "${BACKUP_DIR}"

sudo -u git /usr/local/bin/gitea dump \
  --config /etc/gitea/app.ini \
  --work-path /var/lib/gitea \
  --file "${BACKUP_DIR}/gitea-dump-${STAMP}.zip"

tar -czf "${BACKUP_DIR}/gitea-config-${STAMP}.tar.gz" /etc/gitea/app.ini
chmod 600 "${BACKUP_DIR}/gitea-"*

find "${BACKUP_DIR}" -type f -mtime +14 -delete

echo "Gitea backup completed: ${BACKUP_DIR}"
