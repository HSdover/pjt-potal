# GitLab CE Internal Git Server Guide

## 목적

내부망에 GitLab CE Self-Managed를 설치해서 개발 레포를 운영하는 구성입니다.

GitLab은 PostgreSQL, Redis, Sidekiq 등 포함 구성이라 Gitea보다 무겁습니다. 운영 포털 애플리케이션과 같은 서버에 올리는 구성은 권장하지 않습니다.

권장 구조:

```text
개발자 PC -> https://git.company.local -> GitLab CE 전용 VM
운영 포털 -> https://portal.company.local -> 별도 운영 앱 서버
```

## 포함 파일

| 경로 | 설명 |
|---|---|
| `downloads/gitlab-ce-18.6.6-ce.0.el9.x86_64.rpm` | GitLab CE Oracle Linux 9 x86_64 RPM |
| `downloads/gitlab-ce-gpgkey` | GitLab package GPG key |
| `downloads/gitlab-ce-script.rpm.sh` | GitLab 공식 RPM repository setup script |
| `downloads/SHA256SUMS.txt` | 다운로드 파일 해시 |
| `config/gitlab.rb.template` | GitLab Omnibus 설정 템플릿 |
| `scripts/download-gitlab-ce-rpm.ps1` | OL8/OL9 RPM 다운로드 스크립트 |
| `scripts/install-gitlab-oracle-linux.sh` | Oracle Linux 설치 스크립트 |
| `scripts/backup-gitlab.sh` | 백업 스크립트 |

## 설치 전 확인

| 항목 | 권장 |
|---|---|
| 서버 | GitLab 전용 VM |
| OS | Oracle Linux 9 x86_64 권장, OL8도 가능 |
| CPU | 최소 4 vCPU, 권장 8 vCPU |
| RAM | 최소 8GB, 권장 16GB |
| Disk | 최소 100GB SSD |
| DNS | `git.company.local` |
| TLS | 내부 CA 인증서 |

## 설치 순서

1. 이 폴더 전체를 GitLab 서버로 복사합니다.

```bash
/opt/install-kits/gitlab
```

2. TLS 인증서를 준비합니다.

```bash
sudo mkdir -p /etc/gitlab/ssl
sudo cp git.company.local.crt /etc/gitlab/ssl/git.company.local.crt
sudo cp git.company.local.key /etc/gitlab/ssl/git.company.local.key
sudo chmod 600 /etc/gitlab/ssl/git.company.local.key
```

3. 설치 스크립트를 실행합니다.

```bash
cd /opt/install-kits/gitlab
chmod +x scripts/*.sh
sudo GITLAB_EXTERNAL_URL="https://git.company.local" ./scripts/install-gitlab-oracle-linux.sh
```

4. 설정을 확인합니다.

```bash
sudo vi /etc/gitlab/gitlab.rb
sudo gitlab-ctl reconfigure
sudo gitlab-ctl status
```

5. 초기 root 비밀번호를 확인합니다.

```bash
sudo cat /etc/gitlab/initial_root_password
```

6. 브라우저에서 접속합니다.

```text
https://git.company.local
```

## OL8 서버일 경우

현재 키트에는 OL9 RPM을 다운로드해 두었습니다. 서버가 Oracle Linux 8이면 외부망 PC에서 아래를 실행해 OL8 RPM을 다시 받아 반입합니다.

```powershell
cd C:\workspace\governance-portal\tools\git-server-kits\gitlab
.\scripts\download-gitlab-ce-rpm.ps1 -OracleLinuxMajor 8
```

## 레포 구조

처음에는 개발 레포만 생성합니다.

```text
Group: governance-portal
Project: governance-portal-dev
```

나중에 운영 레포 분리:

```text
Project: governance-portal-ops
```

운영 레포에는 실제 비밀번호, DB 접속정보, TLS private key, SAML private key, Oracle Wallet 실제 파일을 넣지 않습니다.

## 브랜치 전략

`main`을 protected branch로 설정합니다.

권장 설정:

| 항목 | 값 |
|---|---|
| Allowed to merge | Maintainers |
| Allowed to push and merge | No one |
| Force push | Disabled |
| Merge Request | 필수 |
| Review | 최소 1명 |

개발자는 feature/fix 브랜치만 push하고 Merge Request로 `main`에 반영합니다.

```bash
git checkout main
git pull
git checkout -b feature/123-dashboard-template
git add .
git commit -m "feat: dashboard template update"
git push -u origin feature/123-dashboard-template
```

## 사용자 권한

| 역할 | 대상 |
|---|---|
| Owner | 관리자 1~2명 |
| Maintainer | 리드 개발자 1~2명 |
| Developer | 일반 개발자 |
| Reporter | PM/QA/조회 사용자 |

## 백업

일일 백업:

```bash
sudo ./scripts/backup-gitlab.sh
```

반드시 함께 보관:

```text
/var/opt/gitlab/backups
/etc/gitlab/gitlab.rb
/etc/gitlab/gitlab-secrets.json
```

복구는 같은 GitLab 버전에서 먼저 테스트해야 합니다. 운영 전 최소 1회 복구 리허설을 수행합니다.

## 운영 주의사항

- GitLab은 운영 포털 서버와 분리하는 것이 원칙입니다.
- 같은 서버에 억지로 올리면 443, 22 포트와 자원 경합 문제가 생깁니다.
- 운영 앱 서버에 GitLab Runner를 올리지 않습니다.
- 백업은 GitLab 서버 로컬 디스크만 믿지 말고 별도 저장소로 복사합니다.
- 업그레이드 전에는 반드시 `gitlab-backup create`를 먼저 수행합니다.
