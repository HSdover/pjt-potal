# 통합 생성기

통합 생성기는 하나의 페이지 설정 파일로 프론트엔드 화면 파일과 백엔드 기본 패키지를 함께 생성합니다.

## 실행 파일

```powershell
tools\generator\generate-feature.cmd
```

기본 설정 파일은 실행 파일과 같은 폴더에 있습니다.

```text
tools/generator/generator.config.json
```

설정 파일의 `spec` 값은 아래 폴더의 JSON 파일을 가리킵니다.

```text
tools/generator/pages/{spec}.json
```

## 사용 예시

생성 대상만 확인:

```powershell
tools\generator\generate-feature.cmd --dry-run
```

이미 생성된 파일까지 포함해서 생성 대상을 확인:

```powershell
tools\generator\generate-feature.cmd --dry-run --force
```

프론트엔드 파일만 생성:

```powershell
tools\generator\generate-feature.cmd --frontend-only
```

백엔드 파일만 생성:

```powershell
tools\generator\generate-feature.cmd --backend-only
```

## 충돌 처리

기존 파일과 이름이 겹치면 기본 실행은 중단됩니다.
덮어쓰기가 필요한 경우에만 `--force`를 사용합니다.

통합 생성기는 실제 파일을 쓰기 전에 프론트엔드와 백엔드 생성 대상을 먼저 검사합니다.
따라서 한쪽만 생성되고 다른 쪽이 실패하는 상황을 줄입니다.

## 생성 범위

프론트엔드:

```text
frontend/src/features/{feature}/
  api.ts
  columns.ts
  types.ts
  pages/{PageName}.vue
```

백엔드:

```text
backend/src/main/java/com/example/governanceportal/{package}/
  api/{ClassName}Controller.java
  service/{ClassName}Service.java
  repository/{ClassName}Repository.java
  dto/{ClassName}Item.java
  dto/{ClassName}ListRequest.java
  dto/{ClassName}SearchFilter.java
```
