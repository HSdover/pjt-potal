# Offline Build Cache

이 폴더는 내부망 오프라인 빌드를 위한 캐시를 보관합니다.

오프라인 빌드 스크립트는 시스템 PATH의 Node/npm/Gradle을 사용하지 않고, 이 프로젝트 하위의 도구와 캐시를 우선 사용합니다.

## 주요 캐시

- `offline/gradle-home`: Gradle dependency cache
- `offline/npm-cache`: npm package cache
- `offline/nodejs`: 내부망 빌드에서 사용할 Node.js Windows 배포본. `node-*-win-x64/npm.cmd` 형태가 필요합니다.

## 참고

상세 절차는 `docs/offline_build_guide.txt`를 기준으로 관리합니다.
`offline` 하위 캐시는 대부분 `.gitignore` 대상이므로 git clone만으로는 내부망 빌드에 필요한 캐시가 전달되지 않습니다.
