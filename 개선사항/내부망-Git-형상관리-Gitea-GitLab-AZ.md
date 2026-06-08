# 내부망 Git 형상관리 구성 A-Z

## 결론

현재 조건은 개발자 10명 내외, 우선 개발 레포 1개만 유지, 나중에 운영 레포 분리입니다.

권장 판단:

| 조건 | 선택 |
|---|---|
| 운영 서버에 Git 서버를 같이 올려야 함 | Gitea |
| Git 전용 서버를 둘 수 있음 | GitLab CE |
| 리소스가 작고 단순한 형상관리 우선 | Gitea |
| MR, 권한, 이슈, CI/CD까지 표준화 | GitLab CE |

운영 포털과 Git 서버를 같은 서버에 올려야 한다면 GitLab은 피하고 Gitea를 사용합니다. GitLab은 별도 VM이 있을 때 선택합니다.

## 준비된 폴더

```text
tools/git-server-kits/
  gitea/
    downloads/
    config/
    scripts/
    README.md
  gitlab/
    downloads/
    config/
    scripts/
    README.md
```

## 다운로드 완료 파일

| 구분 | 파일 | 비고 |
|---|---|---|
| Gitea | `gitea-1.26.2-linux-amd64` | Linux amd64 실행 파일 |
| Gitea | `gitea-1.26.2-linux-amd64.asc` | 서명 파일 |
| GitLab | `gitlab-ce-18.6.6-ce.0.el9.x86_64.rpm` | Oracle Linux 9용 RPM |
| GitLab | `gitlab-ce-gpgkey` | RPM 검증용 GPG key |
| GitLab | `gitlab-ce-script.rpm.sh` | 공식 repository setup script |

대용량 파일인 Gitea 바이너리와 GitLab RPM은 Git 커밋 대상에서 제외했습니다. 내부망 반입 시에는 실제 파일이 있는 `downloads/` 폴더를 같이 복사해야 합니다.

## Gitea 방식

### 서버 구조

```text
https://portal.company.local -> Nginx -> Spring Boot :18080
https://git.company.local    -> Nginx -> Gitea :3000
ssh://git@git.company.local:2222/... -> Gitea built-in SSH
```

### 설치 절차

```bash
cd /opt/install-kits/gitea
chmod +x scripts/*.sh
sudo ./scripts/install-gitea-oracle-linux.sh
```

이후:

```bash
sudo -u git /usr/local/bin/gitea generate secret SECRET_KEY
sudo -u git /usr/local/bin/gitea generate secret INTERNAL_TOKEN
sudo -u git /usr/local/bin/gitea generate secret LFS_JWT_SECRET
sudo vi /etc/gitea/app.ini
sudo cp config/nginx-gitea.conf /etc/nginx/conf.d/gitea.conf
sudo nginx -t
sudo systemctl reload nginx
sudo systemctl enable --now gitea
```

### 레포 운영

```text
Organization: governance-portal
Repository: governance-portal-dev
```

나중에 운영 분리:

```text
Repository: governance-portal-ops
```

`main`은 보호 브랜치로 설정하고, 개발자는 feature/fix 브랜치에서 Pull Request로만 반영합니다.

### 백업

```bash
sudo /opt/install-kits/gitea/scripts/backup-gitea.sh
```

백업 파일은 `/data/backups/gitea`에 생성됩니다.

## GitLab 방식

### 서버 구조

```text
https://git.company.local -> GitLab CE 전용 VM
```

GitLab은 운영 포털과 같은 서버에 올리지 않는 것이 원칙입니다. 같은 서버에 올리면 443/22 포트 충돌, 메모리 사용량, 업그레이드 영향도가 커집니다.

### 설치 절차

```bash
cd /opt/install-kits/gitlab
chmod +x scripts/*.sh
sudo GITLAB_EXTERNAL_URL="https://git.company.local" ./scripts/install-gitlab-oracle-linux.sh
sudo vi /etc/gitlab/gitlab.rb
sudo gitlab-ctl reconfigure
sudo gitlab-ctl status
```

초기 비밀번호:

```bash
sudo cat /etc/gitlab/initial_root_password
```

### OL8 서버일 경우

현재 다운로드된 RPM은 OL9용입니다. Oracle Linux 8 서버면 외부망 PC에서 아래를 실행해 OL8 RPM을 다시 다운로드합니다.

```powershell
cd C:\workspace\governance-portal\tools\git-server-kits\gitlab
.\scripts\download-gitlab-ce-rpm.ps1 -OracleLinuxMajor 8
```

### 레포 운영

```text
Group: governance-portal
Project: governance-portal-dev
```

브랜치 보호:

| 항목 | 설정 |
|---|---|
| Allowed to merge | Maintainers |
| Allowed to push and merge | No one |
| Force push | Disabled |
| Merge Request | 필수 |

### 백업

```bash
sudo /opt/install-kits/gitlab/scripts/backup-gitlab.sh
```

반드시 함께 보관:

```text
/var/opt/gitlab/backups
/etc/gitlab/gitlab.rb
/etc/gitlab/gitlab-secrets.json
```

## 개발자 PC 공통 설정

```bash
git config --global user.name "홍길동"
git config --global user.email "hong@company.local"
git config --global credential.helper manager
```

SSH 사용 시:

```bash
ssh-keygen -t ed25519 -C "hong@company.local"
```

## 개발 흐름

```bash
git checkout main
git pull
git checkout -b feature/123-dashboard-template
git add .
git commit -m "feat: dashboard template update"
git push -u origin feature/123-dashboard-template
```

웹 화면에서 Pull Request 또는 Merge Request를 생성하고 리뷰 후 `main`에 반영합니다.

## 운영 레포 분리 시 원칙

운영 레포에 넣는 것:

```text
infra/nginx/*.template
infra/systemd/*.service
infra/env/*.template
scripts/deploy/*.sh
docs/operation/*.md
```

운영 레포에 넣지 않는 것:

```text
실제 governance-portal.env
DB 비밀번호
SAML private key
TLS private key
Oracle Wallet 실제 파일
운영 서버 SSH key
```
