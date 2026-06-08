#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KIT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
GITLAB_EXTERNAL_URL="${GITLAB_EXTERNAL_URL:-https://git.company.local}"

RPM_FILE="${1:-}"
if [[ -z "${RPM_FILE}" ]]; then
  RPM_FILE="$(find "${KIT_DIR}/downloads" -maxdepth 1 -name 'gitlab-ce-*.rpm' | sort | head -n 1)"
fi

if [[ -z "${RPM_FILE}" || ! -f "${RPM_FILE}" ]]; then
  echo "Missing GitLab CE RPM under ${KIT_DIR}/downloads"
  exit 1
fi

dnf install -y curl policycoreutils openssh-server openssh-clients perl
systemctl enable --now sshd

if [[ -f "${KIT_DIR}/downloads/gitlab-ce-gpgkey" ]]; then
  rpm --import "${KIT_DIR}/downloads/gitlab-ce-gpgkey" || true
fi

rpm --checksig "${RPM_FILE}" || true

env EXTERNAL_URL="${GITLAB_EXTERNAL_URL}" dnf install -y "${RPM_FILE}"

if [[ -f "${KIT_DIR}/config/gitlab.rb.template" && ! -f /etc/gitlab/gitlab.rb.template.local ]]; then
  cp "${KIT_DIR}/config/gitlab.rb.template" /etc/gitlab/gitlab.rb.template.local
fi

echo "GitLab package installed."
echo "Next:"
echo "1. Copy TLS certificates to /etc/gitlab/ssl."
echo "2. Review /etc/gitlab/gitlab.rb."
echo "3. Run: gitlab-ctl reconfigure"
echo "4. Initial root password: /etc/gitlab/initial_root_password"
