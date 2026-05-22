# 운영작업문서: OCI Oracle Linux 서버 실행 A-Z

## 목적

이 문서는 프로젝트 투입 후 내부망/Windows PC에서 OCI Oracle Linux 운영 서버를 올리고, Governance Portal을 실행하기 위한 따라하기용 절차다.

기준 구조:

```text
Windows PC
-> 빌드, 배포 zip 생성, SFTP 업로드, SSH 작업

OCI Oracle Linux
-> Nginx 80/443
-> Spring Boot JAR 127.0.0.1:18080
```

## 0. 준비물

작업 전 아래 값을 확보한다.

| 항목 | 설명 |
|---|---|
| OCI 서버 IP | Public IP 또는 Bastion/VPN 경유 접속 주소 |
| SSH private key | Oracle Linux 접속용 |
| 접속 계정 | 보통 `opc` |
| 운영 도메인 | 없으면 초기에는 서버 IP로 확인 |
| DB 접속 정보 | JDBC URL, 계정, 비밀번호, Wallet 필요 여부 |
| SAML 정보 | IdP metadata URL, ACS URL 등록 가능 여부 |
| TLS 인증서 | HTTPS 적용 시 필요 |

작업 PC 유틸 위치:

```text
C:\workspace\governance-portal-operational-kit\01-client-tools
```

## 1. Windows PC에서 프로젝트 빌드

```powershell
cd C:\workspace\governance-portal
.\scripts\build-production.ps1
```

생성물:

```text
C:\workspace\governance-portal\frontend\dist
C:\workspace\governance-portal\backend\build\libs\governance-portal-backend.jar
```

빌드가 실패하면 운영 서버 작업을 진행하지 않는다.

## 2. 배포 패키지 생성

```powershell
$kit = "C:\workspace\governance-portal-operational-kit\02-release-package-template"

Remove-Item "$kit\frontend-dist\*" -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item C:\workspace\governance-portal\frontend\dist\* "$kit\frontend-dist" -Recurse -Force
Copy-Item C:\workspace\governance-portal\backend\build\libs\governance-portal-backend.jar "$kit\backend\" -Force

Compress-Archive -Path "$kit\*" -DestinationPath "C:\workspace\governance-portal-release.zip" -Force
```

주의:

- `governance-portal.env`는 일반 배포 zip에 넣지 않는다.
- DB 비밀번호, TLS private key, Oracle Wallet도 일반 배포 zip에 넣지 않는다.
- 보안 파일은 별도 승인된 경로로 전달한다.

## 3. OCI 네트워크 확인

OCI Console에서 Security List 또는 NSG를 확인한다.

| 포트 | 용도 | 허용 기준 |
|---|---|---|
| 22 | SSH | 작업 PC, VPN, Bastion에서만 허용 |
| 80 | HTTP | 필요 시 허용 |
| 443 | HTTPS | 운영 접속용 |
| 18080 | Spring Boot | 외부 허용 금지 |

`18080`은 Nginx가 내부에서만 접근해야 한다.

## 4. Oracle Linux SSH 접속

Windows PC에서 MobaXterm 또는 SSH로 접속한다.

```bash
ssh -i /path/to/private_key opc@서버IP
```

이후 서버 작업은 `sudo`를 사용한다.

## 5. 서버 기본 패키지 설치

```bash
sudo dnf install -y java-21-openjdk-headless nginx firewalld chrony logrotate unzip tar rsync curl
java -version

sudo systemctl enable --now nginx
sudo systemctl enable --now firewalld
sudo systemctl enable --now chronyd
```

firewalld 설정:

```bash
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 5-1. SELinux 허용 설정 (필수)

Oracle Linux는 SELinux가 enforcing 기본이라, 추가 설정 없이는 Nginx가 백엔드(`127.0.0.1:18080`)로 프록시할 때 차단되어 **502**가 발생한다. 이걸 먼저 풀어야 10~12장이 정상 동작한다.

상태 확인:

```bash
getenforce          # Enforcing 이면 아래 설정이 필요하다
```

Nginx → 백엔드 프록시 허용(영구 적용):

```bash
sudo setsebool -P httpd_can_network_connect 1
```

진단 도구(차단 원인 분석용, 선택):

```bash
sudo dnf install -y policycoreutils policycoreutils-python-utils setroubleshoot-server
# 차단 로그 확인:
#   sudo ausearch -m avc -ts recent
#   sudo sealert -a /var/log/audit/audit.log
```

## 6. 운영 계정과 디렉터리 생성

```bash
sudo useradd -r -m -d /opt/governance-portal -s /sbin/nologin governance || true

sudo mkdir -p /opt/governance-portal/releases
sudo mkdir -p /opt/governance-portal/logs
sudo mkdir -p /opt/governance-portal/config
sudo mkdir -p /var/www/governance-portal

sudo chown -R governance:governance /opt/governance-portal
sudo chown -R nginx:nginx /var/www/governance-portal
```

## 7. 파일 업로드

FileZilla 또는 MobaXterm SFTP로 업로드한다.

```text
C:\workspace\governance-portal-release.zip
-> /tmp/governance-portal-release.zip
```

보안 파일은 별도 업로드한다. (env 파일은 업로드하지 않고 9장에서 서버에서 직접 작성한다.)

```text
TLS 인증서/키 -> /tmp/tls            (HTTPS 적용 시)
Oracle Wallet -> /tmp/wallet         (ADB Wallet 접속 시)
SAML metadata 파일 -> /tmp/idp-metadata.xml   (외부 IdP egress가 막힌 경우)
```

업로드한 `/tmp` 보안 원본은 8-1에서 운영 경로로 옮기고, 동작 확인(12장) 후 12-1에서 삭제한다.

## 8. 배포 파일 해제와 반영

```bash
sudo mkdir -p /opt/governance-portal/releases/first
sudo unzip /tmp/governance-portal-release.zip -d /opt/governance-portal/releases/first
```

프론트 반영:

```bash
sudo rsync -a --delete /opt/governance-portal/releases/first/frontend-dist/ /var/www/governance-portal/
sudo chown -R nginx:nginx /var/www/governance-portal
```

백엔드 반영:

```bash
sudo cp /opt/governance-portal/releases/first/backend/governance-portal-backend.jar /opt/governance-portal/governance-portal-backend.jar
sudo chown governance:governance /opt/governance-portal/governance-portal-backend.jar
```

### 8-1. 보안 파일 배치 (Wallet / TLS / SAML metadata)

7장에서 올린 보안 원본을 운영 경로로 옮기고 권한을 제한한다. (해당되는 것만 수행)

Oracle Wallet (ADB Wallet 접속 시):

```bash
sudo mkdir -p /opt/governance-portal/config/wallet
sudo cp /tmp/wallet/* /opt/governance-portal/config/wallet/
sudo chown -R governance:governance /opt/governance-portal/config/wallet
sudo chmod 700 /opt/governance-portal/config/wallet
sudo chmod 600 /opt/governance-portal/config/wallet/*
```

ADB Wallet 접속을 쓰면 JAR에 포함된 `ojdbc11` 외에 `oraclepki`, `osdt_core`, `osdt_cert` companion 라이브러리가 추가로 필요한지 드라이버/Wallet 버전 기준으로 확인한다.

SAML IdP metadata 파일 (외부 IdP egress가 막혀 URL fetch가 불가한 경우):

```bash
sudo cp /tmp/idp-metadata.xml /opt/governance-portal/config/idp-metadata.xml
sudo chown governance:governance /opt/governance-portal/config/idp-metadata.xml
sudo chmod 640 /opt/governance-portal/config/idp-metadata.xml
```

TLS 인증서/키 (HTTPS 적용 시, 10-1에서 사용):

```bash
sudo mkdir -p /etc/pki/governance-portal
sudo cp /tmp/tls/fullchain.pem /etc/pki/governance-portal/fullchain.pem
sudo cp /tmp/tls/privkey.pem  /etc/pki/governance-portal/privkey.pem
sudo chown root:root /etc/pki/governance-portal/*
sudo chmod 644 /etc/pki/governance-portal/fullchain.pem
sudo chmod 600 /etc/pki/governance-portal/privkey.pem
```

### 8-2. SELinux 파일 컨텍스트 보정

rsync로 옮긴 정적 파일이 잘못된 SELinux 컨텍스트를 가지면 Nginx가 읽지 못한다(403/404). 표준 컨텍스트로 복원한다.

```bash
sudo restorecon -Rv /var/www/governance-portal
```

## 9. 운영 env 파일 작성

```bash
sudo vi /opt/governance-portal/config/governance-portal.env
```

Oracle DB/ADB + SAML 기준 예시:

```bash
SPRING_PROFILES_ACTIVE=saml,adb
SERVER_PORT=18080

GOVERNANCE_H2_CONSOLE_ENABLED=false
GOVERNANCE_SQL_INIT_MODE=never

# Spring Batch 메타테이블(BATCH_*)은 Oracle에서 자동 생성되지 않는다.
# 권장: DBA가 Spring Batch 공식 schema-oracle.sql로 BATCH_ 테이블을 선생성하고 아래는 never로 둔다.
# 임시: 선생성이 어려우면 최초 1회만 always로 기동해 생성한 뒤, 반드시 never로 바꿔 재기동한다.
#       (always 상태로 재기동하면 기존 테이블에 CREATE를 다시 시도해 오류가 난다)
GOVERNANCE_BATCH_SCHEMA_INITIALIZE=never

GOVERNANCE_DATASOURCE_URL=jdbc:oracle:thin:@//db-host:1521/service
GOVERNANCE_DATASOURCE_DRIVER=oracle.jdbc.OracleDriver
GOVERNANCE_DATASOURCE_USERNAME=appuser
GOVERNANCE_DATASOURCE_PASSWORD=change-me

# Redis는 사용할 때만 설정한다(미설정 시 캐시는 simple 기본값).
# GOVERNANCE_CACHE_TYPE=redis
# GOVERNANCE_REDIS_HOST=127.0.0.1
# GOVERNANCE_REDIS_PORT=6379

SAML_IDP_METADATA_URI=https://idp.example.com/metadata
SAML_SP_ENTITY_ID=https://portal.example.com/saml2/service-provider-metadata/knox
SAML_SP_ACS_LOCATION=https://portal.example.com/login/saml2/sso/knox
```

ADB Wallet 방식이면 datasource URL은 아래 형태를 사용한다.

```bash
GOVERNANCE_DATASOURCE_URL=jdbc:oracle:thin:@db_high?TNS_ADMIN=/opt/governance-portal/config/wallet
```

권한 설정:

```bash
sudo chown root:governance /opt/governance-portal/config/governance-portal.env
sudo chmod 640 /opt/governance-portal/config/governance-portal.env
```

운영망에서 외부 IdP metadata URL로 나가는 통신(egress)이 막혀 있으면, 8-1에서 올린 metadata 파일을 가리키도록 바꾼다.

```bash
SAML_IDP_METADATA_URI=file:/opt/governance-portal/config/idp-metadata.xml
```

서비스 기동 전에 DB 도달성을 미리 점검한다(실패 startup 방지).

```bash
nc -zv db-host 1521        # ADB Wallet 접속이면 tnsnames의 host/port 기준으로 점검
```

## 10. Nginx 설정

SAML을 사용하므로 `/api/`, `/login/`, `/saml2/`, `/actuator/`를 Spring Boot로 프록시해야 한다.

```bash
sudo vi /etc/nginx/conf.d/governance-portal.conf
```

기본 예시:

```nginx
upstream governance_portal_backend {
    server 127.0.0.1:18080;
}

server {
    listen 80;
    server_name _;

    root /var/www/governance-portal;
    index index.html;

    location /assets/ {
        try_files $uri =404;
        expires 30d;
        add_header Cache-Control "public, max-age=2592000, immutable";
    }

    location /api/ {
        proxy_pass http://governance_portal_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /login/ {
        proxy_pass http://governance_portal_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /saml2/ {
        proxy_pass http://governance_portal_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /actuator/ {
        proxy_pass http://governance_portal_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

검사와 reload:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

> 위 80 전용 구성은 초기 동작 확인용이다. SAML 운영은 아래 10-1(HTTPS)로 전환해야 한다.

### 10-1. HTTPS(443) 적용 (SAML 운영 시 사실상 필수)

IdP는 보통 https ACS(`/login/saml2/sso/knox`)로 SAMLResponse를 POST하므로, 80만 열려 있으면 실제 SAML 로그인이 완성되지 않는다. 운영에서는 443을 구성한다. (인증서는 8-1에서 `/etc/pki/governance-portal/`에 배치)

먼저 공통 프록시 헤더 파일을 만든다. `X-Forwarded-Proto`가 https로 전달되어야 Spring이 올바른 ACS/메타데이터 URL을 만든다.

```bash
sudo vi /etc/nginx/conf.d/governance-portal-proxy.inc
```

```nginx
proxy_http_version 1.1;
proxy_set_header Host $host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
proxy_set_header X-Forwarded-Host $host;
proxy_set_header X-Forwarded-Port $server_port;
```

그다음 `/etc/nginx/conf.d/governance-portal.conf`를 아래로 교체한다. 80은 443으로 리다이렉트하고, 정적 서빙과 프록시는 443 server에 둔다. `server_name`은 실제 운영 도메인으로 바꾼다.

```nginx
upstream governance_portal_backend {
    server 127.0.0.1:18080;
}

# HTTP -> HTTPS 리다이렉트
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    http2 on;
    server_name 포털도메인;

    ssl_certificate     /etc/pki/governance-portal/fullchain.pem;
    ssl_certificate_key /etc/pki/governance-portal/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    root /var/www/governance-portal;
    index index.html;

    location /assets/ {
        try_files $uri =404;
        expires 30d;
        add_header Cache-Control "public, max-age=2592000, immutable";
    }

    location /api/      { proxy_pass http://governance_portal_backend; include /etc/nginx/conf.d/governance-portal-proxy.inc; }
    location /login/    { proxy_pass http://governance_portal_backend; include /etc/nginx/conf.d/governance-portal-proxy.inc; }
    location /saml2/    { proxy_pass http://governance_portal_backend; include /etc/nginx/conf.d/governance-portal-proxy.inc; }
    location /actuator/ { proxy_pass http://governance_portal_backend; include /etc/nginx/conf.d/governance-portal-proxy.inc; }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

443은 OCI Security List/NSG(3장)와 firewalld(5장)에서 열려 있어야 한다. 검사와 reload:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

HTTPS 적용 후에는 env(9장)의 `SAML_SP_ENTITY_ID`/`SAML_SP_ACS_LOCATION`이 https 운영 도메인 기준인지 확인하고, 변경했다면 서비스를 재시작한다.

```bash
sudo systemctl restart governance-portal
```

## 11. systemd 서비스 등록

```bash
sudo vi /etc/systemd/system/governance-portal.service
```

내용:

```ini
[Unit]
Description=Governance Portal Spring Boot API
After=network.target

[Service]
User=governance
Group=governance
WorkingDirectory=/opt/governance-portal
EnvironmentFile=/opt/governance-portal/config/governance-portal.env
ExecStart=/usr/bin/java -jar /opt/governance-portal/governance-portal-backend.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

등록과 실행:

```bash
sudo systemctl daemon-reload
sudo systemctl enable governance-portal
sudo systemctl restart governance-portal
sudo systemctl status governance-portal
```

## 12. 실행 확인

백엔드 직접 확인:

```bash
curl http://127.0.0.1:18080/actuator/health
```

Nginx 경유 확인:

```bash
curl http://localhost/actuator/health
```

브라우저 확인:

```text
http://서버IP/
http://서버IP/actuator/health
```

SAML metadata 확인:

```text
http://서버IP/saml2/service-provider-metadata/knox
```

운영 도메인/HTTPS 적용 후 IAM/KNOX에 등록할 값:

```text
SP Metadata:
https://포털도메인/saml2/service-provider-metadata/knox

ACS URL:
https://포털도메인/login/saml2/sso/knox

Login Entry:
https://포털도메인/saml2/authenticate/knox
```

### 12-1. 보안 임시파일 정리 (필수)

동작 확인이 끝나면 `/tmp`에 남은 보안 원본을 삭제한다. (운영에서 실제로 참조하는 파일은 이미 `/opt`·`/etc/pki`로 옮겨졌다)

```bash
sudo rm -f /tmp/governance-portal-release.zip
sudo rm -rf /tmp/wallet /tmp/tls
sudo rm -f /tmp/idp-metadata.xml
```

## 13. 로그 확인

애플리케이션 로그는 기본적으로 systemd journald로 수집된다(별도 파일 아님). 6장에서 만든 `/opt/governance-portal/logs`는 파일 로깅을 따로 켠 경우에만 사용된다.

```bash
journalctl -u governance-portal -f
tail -f /var/log/nginx/error.log
tail -f /var/log/nginx/access.log
```

journald 보존 용량을 제한하려면 `/etc/systemd/journald.conf`에서 `SystemMaxUse`를 설정한다(예: `SystemMaxUse=500M`) 후 `sudo systemctl restart systemd-journald`.

포트 확인:

```bash
ss -lntp | grep 18080
ss -lntp | grep ':80\|:443'
```

## 14. 장애 확인 기준

| 증상 | 확인할 것 |
|---|---|
| SSH 접속 실패 | OCI NSG/Security List, Public IP, SSH key, `opc` 계정 |
| 브라우저 접속 실패 | OCI 80/443 허용, firewalld 허용, Nginx 상태 |
| 502 Bad Gateway | Spring Boot 실행 여부, `journalctl -u governance-portal`, SELinux `httpd_can_network_connect`(5-1) |
| 정적파일 403/404 | SELinux 컨텍스트(`restorecon`, 8-2), `/var/www` 권한 |
| DB 연결 실패 | datasource URL, 계정, 비밀번호, Wallet 경로, `nc -zv db-host 1521` |
| 배치 기동/실행 실패 | Oracle BATCH_ 테이블 존재 여부, `GOVERNANCE_BATCH_SCHEMA_INITIALIZE`(9장) |
| SAML 실패 | HTTPS/ACS https 여부(10-1), `/login/`·`/saml2/` 프록시, IdP metadata egress(`file:` 옵션), ACS URL, 서버 시간 |
| HTTPS 접속 실패 | 443 NSG/firewalld 허용, 인증서 경로/권한, `sudo nginx -t` |
| 권한 없음 | SAML group attribute와 내부 권한 매핑 설정 |

## 15. 롤백 준비

배포 전 기존 파일을 백업한다.

```bash
sudo mkdir -p /opt/governance-portal/releases/backup
sudo cp /opt/governance-portal/governance-portal-backend.jar /opt/governance-portal/releases/backup/ 2>/dev/null || true
sudo rsync -a /var/www/governance-portal/ /opt/governance-portal/releases/backup/frontend-dist/ 2>/dev/null || true
```

문제가 생기면 백업본을 되돌린다.

```bash
sudo cp /opt/governance-portal/releases/backup/governance-portal-backend.jar /opt/governance-portal/
sudo rsync -a --delete /opt/governance-portal/releases/backup/frontend-dist/ /var/www/governance-portal/
sudo systemctl restart governance-portal
sudo systemctl reload nginx
```

## 16. 최종 성공 기준

아래 네 가지가 모두 성공하면 서버는 실행된 상태다.

```bash
sudo systemctl status governance-portal
sudo nginx -t
curl http://127.0.0.1:18080/actuator/health
curl http://localhost/actuator/health
```

브라우저에서 아래 주소도 확인한다.

```text
http://서버IP/         (HTTPS 적용 전 초기 확인)
https://포털도메인/     (10-1 적용 후 운영 확인)
```

HTTPS·SAML까지 적용했다면 추가로 확인한다.

```bash
curl -I https://포털도메인/                                  # 200 또는 302
curl -I https://포털도메인/saml2/service-provider-metadata/knox   # 200, XML 반환
# 브라우저로 https://포털도메인/ 접속 시 IdP 로그인으로 이동 → 로그인 후 포털 진입 → 권한별 메뉴 노출 확인
```

## 참고 공식 문서

- OCI Linux SSH 접속: https://docs.oracle.com/iaas/Content/Compute/Tasks/connect-to-linux-instance.htm
- OCI Network Security Groups: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/networksecuritygroups.htm
- Oracle Linux firewalld: https://docs.oracle.com/en-us/iaas/oracle-linux/firewall/ol-firewall-configuring-the-firewall-with-firewalld.htm
- Oracle Linux systemd: https://docs.oracle.com/en-us/iaas/oracle-linux/systemd/systemd-service-management.htm
- Oracle Linux SELinux: https://docs.oracle.com/en/operating-systems/oracle-linux/selinux/
- Spring Batch 메타데이터 스키마: https://docs.spring.io/spring-batch/reference/schema-appendix.html
- Spring Security SAML2 로그인: https://docs.spring.io/spring-security/reference/servlet/saml2/login/index.html
