# Quality Gate

작성일: 2026-05-12

## 기본 검증

프론트 변경 후:

```powershell
cd frontend
npm.cmd run build
```

프론트 단위 테스트가 관련된 변경 후:

```powershell
cd frontend
npm.cmd run test:unit -- --run
```

백엔드 변경 후:

```powershell
cd backend
.\gradlew.bat test --rerun-tasks
```

## 화면 확인

- `/`, `/sample-list`, `/sample-list-jpa` 진입 여부
- 검색, 초기화, 페이지 변경, 페이지 크기 변경, 정렬 변경
- API 실패 시 오류 메시지 표시
- 권한 없는 화면 진입 시 `/forbidden` 이동
- 행 선택 상세가 있는 화면은 검색/페이지/정렬 변경 시 선택 상태 초기화

## 생성기 확인

프로젝트 루트에서 실행한다.

```powershell
tools\generator\generate-feature.cmd --dry-run
```

실제 생성 파일의 타입까지 확인해야 할 때만 임시 feature를 생성하고 빌드한 뒤, 테스트용 생성 파일은 제거한다.
