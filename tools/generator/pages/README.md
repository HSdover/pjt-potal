# 페이지 설정 파일

이 폴더는 통합 생성기에서 사용하는 JSON 설정 파일을 보관합니다.

## 작업 순서

1. `sample-list.json`을 복사해서 새 페이지 이름으로 변경합니다.
2. `feature`, `title`, `description`, `auth`, `apiPath`를 업무 화면에 맞게 수정합니다.
3. 같은 화면에 대응되는 Spring Boot 패키지가 필요하면 `backend` 섹션을 수정합니다.
4. `../generator.config.json`의 `spec` 값을 새 파일명으로 변경합니다.
5. `tools\generator\generate-feature.cmd`를 실행합니다.

## 명령어

생성 대상 확인:

```powershell
tools\generator\generate-feature.cmd --dry-run
```

기존 파일까지 포함해서 생성 대상 확인:

```powershell
tools\generator\generate-feature.cmd --dry-run --force
```

프론트엔드만 생성:

```powershell
tools\generator\generate-feature.cmd --frontend-only
```

백엔드만 생성:

```powershell
tools\generator\generate-feature.cmd --backend-only
```

## 필드

- `feature`: 프론트엔드 feature 폴더명입니다. kebab-case를 사용합니다.
- `type`: 현재는 `search-grid`만 지원합니다.
- `title`: 화면 제목입니다.
- `description`: 화면 설명입니다.
- `auth`: 라우트와 버튼에서 사용하는 권한 코드입니다.
- `apiPath`: 목록 조회 API 경로입니다. 보통 `/api/{resource}/search` 형식을 사용합니다.
- `backend.packageName`: `com.example.governanceportal` 아래에 생성할 Java 패키지 구간입니다.
- `backend.className`: Java 클래스명 접두어입니다.
- `backend.tableName`: JPA 엔티티가 매핑될 DB 테이블명입니다.
- `backend.idColumn`: `@Id` 필드에 매핑될 DB 컬럼명이며 기본 정렬 기준입니다.
- `backend.fields`: Java 엔티티/DTO 필드명과 DB 컬럼 매핑입니다.
- `backend.keywordFields`: QueryDSL 키워드 검색에 포함할 Java 필드명 목록입니다.
- `backend.keywordColumns`: 기존 설정 호환용입니다. `fields[].column` 또는 `fields[].name`과 매칭해 QueryDSL 검색 필드로 변환합니다.
