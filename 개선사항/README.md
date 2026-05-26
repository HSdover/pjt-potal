# 개선사항 정리

프로젝트 진행 중 확인한 개선사항과 작업 방향을 정리하는 폴더다.

현재 기준은 기능목록 엑셀 기준으로 화면 도메인을 나누고, 실제 프론트엔드/백엔드/DB 로직 설계는 프로젝트 착수 이후 진행하는 방향이다.

## 문서 목록

- `작업방향.md`: 현재 작업 원칙과 보류할 작업 범위
- `frontend-si-template-completed.md`: 프론트엔드 템플릿 기준 요약
- `saml-sso-연동가이드.md`: SAML SSO 연동 및 운영 반영 기준
- `운영환경-유틸파일-체크리스트.md`: Oracle Linux 운영 세팅에 필요한 유틸과 필수 배포 파일
- `oci-oracle-linux-운영구조-설명.md`: Windows PC에서 OCI Oracle Linux 서버를 운영 세팅하는 구조 설명
- `운영작업문서-서버실행-AZ.md`: 운영 서버 설치, 배포, 실행을 순서대로 수행하는 작업 문서
- `예외처리-로깅-전략.txt`: 백엔드/프론트 예외 처리와 AOP 기반 로깅 기준
- `외부RESTAPI-연계전략.md`: OCI/외부 모듈 REST API 호출 기준과 로깅 전략
- `프로젝트-컨텍스트-시스템프롬프트.md`: 소스 없이도 현재 프로젝트 기준 답변을 받기 위한 컨텍스트 프롬프트
- `../docs/governance/screen-domain-template.md`: 기능목록 기준 화면 도메인/템플릿 분류
- `../docs/governance/project-start-strategy.md`: 내부망 형상관리와 Oracle Linux 운영 배포 전략

## 현재 우선순위

1. 기능목록 기준 화면 도메인과 템플릿 유형 정리
2. 개발 참고 화면을 재사용 가능한 템플릿/공통 컴포넌트로 유지
3. 운영 투입 전 OCI Oracle Linux 배포 절차, 보안 파일, Redis/Oracle/SAML env 값을 현장 정보로 치환
4. 프로젝트 착수 후 내부망 형상관리 전략 확정
5. SAML SSO는 IAM/KNOX 연동 정보가 확정되면 `saml` 프로파일로 검증
6. 외부 REST API는 시스템별 endpoint/인증/timeout이 확정되면 공통 `RestClient + HTTP Interface` 구조로 구현
