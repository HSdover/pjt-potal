# Gitea Internal Git Server Guide

## 목적

운영 서버 또는 내부망 소형 서버에 Gitea를 올려 10명 내외 개발자가 Git 형상관리를 할 수 있게 하는 구성입니다.

권장 URL:

```text
https://git.company.local
```

운영 포털과 같은 서버에 둘 경우:

```text
https://portal.company.local -> Nginx -> Spring Boot :18080
https://git.company.local    -> Nginx -> Gitea :3000
```

SSH clone을 사용할 경우 서버 관리 SSH와 충돌을 피하기 위해 Gitea SSH는 `2222`를 권장합니다.

## 포함 파일

| 경로 | 설명 |
|---|---|
| `downloads/gitea-1.26.2-linux-amd64` | Gitea Linux amd64 바이너리 |
| `downloads/gitea-1.26.2-linux-amd64.asc` | GPG 서명 파일 |
| `downloads/SHA256SUMS.txt` | 다운로드 파일 해시 |
| `config/app.ini.template` | Gitea 설정 템플릿 |
| `config/gitea.service` | systemd 서비스 템플릿 |
| `config/nginx-gitea.conf` | Nginx HTTPS reverse proxy 템플릿 |
| `scripts/install-gitea-oracle-linux.sh` | Oracle Linux 설치 스크립트 |
| `scripts/backup-gitea.sh` | 백업 스크립트 |

## 설치 순서

1. 이 폴더 전체를 운영 서버로 복사합니다.

```bash
/opt/install-kits/gitea
```

2. 설치 스크립트를 실행합니다.

```bash
cd /opt/install-kits/gitea
chmod +x scripts/*.sh
sudo ./scripts/install-gitea-oracle-linux.sh
```

3. 보안 값을 생성합니다.

```bash
sudo -u git /usr/local/bin/gitea generate secret SECRET_KEY
sudo -u git /usr/local/bin/gitea generate secret INTERNAL_TOKEN
sudo -u git /usr/local/bin/gitea generate secret LFS_JWT_SECRET
```

4. `/etc/gitea/app.ini`에서 아래 값을 수정합니다.

```text
DOMAIN = git.company.local
ROOT_URL = https://git.company.local/
SECRET_KEY = ...
INTERNAL_TOKEN = ...
LFS_JWT_SECRET = ...
```

5. TLS 인증서를 배치합니다.

```bash
sudo mkdir -p /etc/pki/gitea
sudo cp git.company.local.crt /etc/pki/gitea/fullchain.pem
sudo cp git.company.local.key /etc/pki/gitea/privkey.pem
sudo chmod 600 /etc/pki/gitea/privkey.pem
```

6. Nginx 설정을 반영합니다.

```bash
sudo cp config/nginx-gitea.conf /etc/nginx/conf.d/gitea.conf
sudo nginx -t
sudo systemctl reload nginx
```

7. Gitea를 시작합니다.

```bash
sudo systemctl enable --now gitea
sudo systemctl status gitea
```

8. 브라우저에서 접속합니다.

```text
https://git.company.local
```

최초 관리자 계정을 만든 뒤, 회원가입은 비활성화 상태를 유지합니다.

## 레포 생성

조직:

```text
governance-portal
```

개발 레포:

```text
governance-portal-dev
```

나중에 운영 분리 시:

```text
governance-portal-ops
```

## 브랜치 규칙

`main`은 보호 브랜치로 설정합니다.

권장 규칙:

| 항목 | 값 |
|---|---|
| Direct push | 금지 |
| Pull Request | 필수 |
| Review | 최소 1명 |
| Force push | 금지 |
| Delete branch | merge 후 허용 |

개발 브랜치 예시:

```text
feature/123-dashboard-template
fix/124-saml-csrf
docs/operation-guide
```

## 개발자 사용 흐름

```bash
git clone https://git.company.local/governance-portal/governance-portal-dev.git
cd governance-portal-dev

git checkout main
git pull
git checkout -b feature/123-dashboard-template

git add .
git commit -m "feat: dashboard template update"
git push -u origin feature/123-dashboard-template
```

Gitea 화면에서 Pull Request를 생성하고 리뷰 후 merge합니다.

## 백업

일일 백업:

```bash
sudo ./scripts/backup-gitea.sh
```

백업 대상:

| 경로 | 설명 |
|---|---|
| `/data/backups/gitea` | dump 파일 |
| `/etc/gitea/app.ini` | 보안 설정 포함 |
| `/data/gitea/repositories` | Git repository |
| `/var/lib/gitea` | Gitea 데이터 |

`app.ini`에는 secret 값이 있으므로 백업 파일 접근권한을 제한해야 합니다.

## 운영 주의사항

- Gitea는 `git` 전용 계정으로 실행합니다.
- Gitea 데이터와 포털 애플리케이션 데이터는 분리합니다.
- Git 서버는 내부망/VPN에서만 접근시킵니다.
- CI runner는 운영 서버에 같이 올리지 않습니다.
- Git 백업과 운영 포털 백업은 분리합니다.
- 나중에 서버 여유가 생기면 Gitea를 별도 VM으로 분리합니다.
