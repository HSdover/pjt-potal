# Offline Build Bundle

이 폴더는 내부망 반입용 오프라인 빌드 자원을 모으는 위치입니다.

## 폴더 역할

- `gradle-home`: Gradle 플러그인과 Maven 의존성 캐시
- `npm-cache`: npm 패키지 캐시
- `nodejs`: Gradle Node Plugin이 내려받은 Node.js/npm 런타임
- `npm`: Gradle Node Plugin npm 작업용 런타임 폴더
- `gradle`: 선택 사항, Gradle 배포본을 직접 넣는 위치

## 준비 흐름

1. 외부망 PC에서 `scripts/prepare-offline-bundle.ps1` 실행
2. 생성/다운로드된 `offline` 폴더를 프로젝트와 함께 내부망으로 반입
3. 내부망 PC에서 `scripts/build-offline-war.ps1` 실행

운영 서버에는 Node.js 서버가 실행되지 않습니다. Node/npm은 WAR 빌드 단계에서만 사용됩니다.
