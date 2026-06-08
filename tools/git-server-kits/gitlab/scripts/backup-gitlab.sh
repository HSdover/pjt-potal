#!/usr/bin/env bash
set -euo pipefail

BACKUP_CONFIG_DIR="${BACKUP_CONFIG_DIR:-/data/backups/gitlab-config}"
STAMP="$(date +%Y%m%d-%H%M%S)"

gitlab-backup create

mkdir -p "${BACKUP_CONFIG_DIR}"
tar -czf "${BACKUP_CONFIG_DIR}/gitlab-config-${STAMP}.tar.gz" \
  /etc/gitlab/gitlab.rb \
  /etc/gitlab/gitlab-secrets.json

chmod 600 "${BACKUP_CONFIG_DIR}/gitlab-config-${STAMP}.tar.gz"

echo "GitLab backup completed."
echo "Repository/data backup: /var/opt/gitlab/backups"
echo "Config backup: ${BACKUP_CONFIG_DIR}"
