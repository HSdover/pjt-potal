#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KIT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
GITEA_BINARY="${KIT_DIR}/downloads/gitea-1.26.2-linux-amd64"

if [[ ! -f "${GITEA_BINARY}" ]]; then
  echo "Missing ${GITEA_BINARY}"
  exit 1
fi

dnf install -y git nginx openssh-server policycoreutils
systemctl enable --now sshd

if ! getent group git >/dev/null; then
  groupadd --system git
fi

if ! id git >/dev/null 2>&1; then
  useradd --system --shell /bin/bash --comment "Git Version Control" --gid git --home-dir /home/git --create-home git
fi

mkdir -p /var/lib/gitea/custom /var/lib/gitea/data /var/lib/gitea/log
mkdir -p /data/gitea/repositories
mkdir -p /etc/gitea

install -m 0755 "${GITEA_BINARY}" /usr/local/bin/gitea

if [[ ! -f /etc/gitea/app.ini ]]; then
  install -m 0640 -o root -g git "${KIT_DIR}/config/app.ini.template" /etc/gitea/app.ini
fi

install -m 0644 "${KIT_DIR}/config/gitea.service" /etc/systemd/system/gitea.service

chown -R git:git /var/lib/gitea /data/gitea
chmod -R 750 /var/lib/gitea /data/gitea
chown root:git /etc/gitea
chmod 750 /etc/gitea
chmod 640 /etc/gitea/app.ini

systemctl daemon-reload

echo "Gitea files installed."
echo "Next:"
echo "1. Generate SECRET_KEY, INTERNAL_TOKEN, LFS_JWT_SECRET and edit /etc/gitea/app.ini."
echo "2. Copy TLS certificates under /etc/pki/gitea."
echo "3. Copy config/nginx-gitea.conf to /etc/nginx/conf.d/gitea.conf and reload nginx."
echo "4. Run: systemctl enable --now gitea"
