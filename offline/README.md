# Offline Build Cache

이 폴더는 내부망 오프라인 빌드를 위한 캐시를 보관합니다.

오프라인 빌드 스크립트는 시스템 PATH의 Node/npm/Gradle을 사용하지 않고, 이 프로젝트 하위의 도구와 캐시를 우선 사용합니다.

## 사용 흐름

1. 외부망 PC에서 캐시 준비

```powershell
.\scripts\prepare-offline-bundle.ps1
```

2. 프로젝트 전체를 내부망 PC로 이동

   `offline` 하위 캐시는 `.gitignore` 대상이므로 git clone만으로는 이동되지 않습니다. 폴더 전체를 복사하거나 별도 압축 파일로 전달해야 합니다.

3. 내부망 PC에서 오프라인 빌드

```powershell
.\scripts\build-offline-jar.ps1
```

## 주요 캐시

- `offline/gradle-home`: Gradle dependency cache
- `offline/npm-cache`: npm package cache
- `offline/nodejs`: 내부망 빌드에서 사용할 Node.js Windows 배포본. `node-*-win-x64/npm.cmd` 형태가 필요합니다.

## 산출물

- Frontend: `frontend/dist`
- Backend: `backend/build/libs/governance-portal-backend.jar`
