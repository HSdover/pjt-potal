# Offline Build Cache

이 폴더는 내부망 오프라인 빌드를 위한 캐시를 보관합니다.

## 사용 흐름

1. 외부망 PC에서 캐시 준비

```powershell
.\scripts\prepare-offline-bundle.ps1
```

2. 프로젝트 전체를 내부망 PC로 이동

3. 내부망 PC에서 오프라인 빌드

```powershell
.\scripts\build-offline-jar.ps1
```

## 주요 캐시

- `offline/gradle-home`: Gradle dependency cache
- `offline/npm-cache`: npm package cache

## 산출물

- Frontend: `frontend/dist`
- Backend: `backend/build/libs/governance-portal-backend.jar`
