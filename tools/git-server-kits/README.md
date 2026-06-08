# Internal Git Server Kits

내부망 형상관리를 위한 Git 서버 설치 키트입니다.

## 구성

| 폴더 | 용도 |
|---|---|
| `gitea/` | 운영 서버에 가볍게 같이 올릴 수 있는 Gitea 구성 |
| `gitlab/` | 별도 형상관리 서버에 올리는 GitLab CE 구성 |

## 다운로드된 파일

대용량 실행 파일은 작업 폴더에는 존재하지만 Git에는 커밋하지 않도록 `.gitignore`에 등록했습니다.

| 파일 | 상태 |
|---|---|
| `gitea/downloads/gitea-1.26.2-linux-amd64` | 다운로드 완료, Git 추적 제외 |
| `gitea/downloads/gitea-1.26.2-linux-amd64.asc` | 다운로드 완료 |
| `gitlab/downloads/gitlab-ce-18.6.6-ce.0.el9.x86_64.rpm` | 다운로드 완료, Git 추적 제외 |
| `gitlab/downloads/gitlab-ce-gpgkey` | 다운로드 완료 |
| `gitlab/downloads/gitlab-ce-script.rpm.sh` | 다운로드 완료 |

## 선택 기준

| 조건 | 권장 |
|---|---|
| 운영 서버에 Git 서버를 같이 올려야 함 | Gitea |
| Git 전용 VM/서버를 별도로 둘 수 있음 | GitLab CE |
| 10명 내외, 개발 레포 1개 중심 | Gitea 가능 |
| MR, 이슈, CI/CD, 운영 레포 분리까지 표준화 | GitLab CE |

운영 애플리케이션 서버와 Git 서버를 분리할 수 있다면 분리하는 것이 원칙입니다. 같은 서버에 올려야 한다면 GitLab보다 Gitea가 적합합니다.
