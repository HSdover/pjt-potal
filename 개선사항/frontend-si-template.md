# SI 프로젝트 프론트엔드 템플릿화 기준

작성일: 2026-05-11
갱신일: 2026-05-18

이 문서는 앞으로 검토할 템플릿화 후보만 관리한다.
이미 확정된 로컬 개발 기준은 `docs/frontend` 문서와 `tools/generator` 문서를 기준으로 한다.
완료된 기반 작업의 요약은 `frontend-si-template-completed.md`에 보관한다.

## 현재 기준 문서

- `docs/frontend/01-quick-start.md`: 실행, 구조, 첫 화면 생성
- `docs/frontend/02-page-patterns.md`: 화면 유형
- `docs/frontend/03-api-and-list-contract.md`: API와 목록 계약
- `docs/frontend/04-grid-convention.md`: AG Grid 기준
- `docs/frontend/05-permission-convention.md`: 라우터와 버튼 권한
- `docs/frontend/06-coding-rules.md`: 파일 배치와 작성 규칙
- `docs/frontend/07-quality-gate.md`: 빌드, 테스트, 화면 확인 기준
- `tools/generator/README.md`: 통합 생성기 사용법
- `tools/generator/pages/README.md`: 페이지 spec 작성법

## 현재 진행 방향

1. 신규 업무 화면은 `features/{feature}` 구조와 통합 생성기를 우선 사용한다.
2. 목록 화면은 공통 HTTP 클라이언트, `ListRequest`, `ListResponse`, `BaseGrid`, `SearchPanel` 기준을 따른다.
3. 운영 프로젝트 분리, 보안, 배포 설정은 사용자가 다시 요청하기 전까지 별도 우선순위로 분리한다.

## 남은 로컬 후보

현재 정리된 기준에서는 로컬 개발 우선순위 1차 작업이 완료된 상태다.
다음 후보는 실제 반복 요구가 확인될 때 별도 작업으로 잡는다.

| 후보 | 판단 기준 |
|---|---|
| 공통 코드 콤보 | 두 개 이상 화면에서 같은 코드 목록 조회 UI가 반복될 때 |
| 엑셀 다운로드 | 실제 목록 화면에서 다운로드 요구가 생기고 서버/클라이언트 방식이 정해질 때 |
| `app/` 구조 분리 | layout, provider 책임이 현재 구조에서 감당하기 어려워질 때 |
| Pinia auth store | 로그인, 사용자 정보, 서버 권한 API가 도입될 때 |

## 운영 분리 보류 항목

아래 항목은 운영 프로젝트 분리 단계에서 다시 다룬다.

- `application-local.yml`, `application-prod.yml` profile 분리
- 운영 H2 console 비활성화
- 운영 sample data initializer 비활성화
- 운영 datasource 환경변수 분리
- systemd `EnvironmentFile` 적용
- Spring Boot 네트워크 바인딩과 SecurityConfig 운영 정책 확정
- Nginx cache, 차단, TLS 정책 확정
- 운영 DB 전환 전략
- Swagger, Actuator 운영 노출 정책 확정
