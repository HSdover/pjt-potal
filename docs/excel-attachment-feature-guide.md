# Excel and Attachment Feature Guide

이 문서는 샘플 JPA 화면에 추가된 엑셀 기능과 공통 첨부파일 기능의 구현 위치, 처리 흐름, 장애 확인 포인트를 정리한다.

## 1. 엑셀 기능

### 1.1 기능 범위

현재 엑셀 기능은 `sample-list-jpa` 화면 기준으로 구현되어 있다.

| 기능 | 프론트 API | 백엔드 API | 설명 |
| --- | --- | --- | --- |
| 일반 엑셀 다운로드 | `downloadExcel()` | `POST /api/samples-jpa/excel/download` | 현재 검색 조건 기준으로 최대 50,000건까지 동기 스트리밍 다운로드 |
| 대용량 엑셀 다운로드 | `requestLargeExcel()`, `fetchLargeExcel()`, `downloadLargeExcel()` | `POST /api/samples-jpa/excel/large-download`, `GET /api/samples-jpa/excel/large-download/{jobId}`, `GET /api/samples-jpa/excel/large-download/{jobId}/file` | 비동기 작업 생성 후 상태 폴링, 완료 파일 다운로드 |
| 엑셀 업로드 | `uploadExcel()` | `POST /api/samples-jpa/excel/upload` | `.xlsx` 파일 검증 후 신규/수정 데이터 반영 |

권한:

- 조회: `SAMPLE_JPA_READ`
- 등록/수정/삭제: `SAMPLE_JPA_CREATE`, `SAMPLE_JPA_UPDATE`, `SAMPLE_JPA_DELETE`
- 다운로드: `SAMPLE_JPA_EXPORT`
- 업로드: `SAMPLE_JPA_IMPORT`

프론트는 `AuthButton`으로 버튼을 숨기고, 백엔드는 `SampleJpaController`의 `@PreAuthorize`로 API를 차단한다.

### 1.2 주요 파일

프론트:

- `frontend/src/features/sample-list-jpa/pages/SampleListJpaPage.vue`
  - `downloadExcelFile()`: 일반 엑셀 다운로드 버튼 처리
  - `requestLargeDownloadFile()`: 대용량 다운로드 요청 및 완료 후 다운로드
  - `waitLargeDownload()`: 대용량 작업 상태 폴링
  - `openExcelUpload()`, `onExcelFileChange()`: 업로드 파일 선택 및 업로드
  - `importErrorMessage()`: 업로드 검증 오류 메시지 구성
- `frontend/src/features/sample-list-jpa/api.ts`
  - `/api/samples-jpa/excel/*` 호출 래퍼
- `frontend/src/shared/api/http.ts`
  - `downloadPost()`, `downloadGet()`: Blob 다운로드
  - `upload()`: multipart 업로드

백엔드:

- `backend/src/main/java/com/example/governanceportal/samplejpa/api/SampleJpaController.java`
  - 엑셀 API 엔드포인트 정의
- `backend/src/main/java/com/example/governanceportal/samplejpa/service/SampleJpaExcelService.java`
  - 엑셀 다운로드/대용량 다운로드/업로드 핵심 로직
- `backend/src/main/java/com/example/governanceportal/common/excel/ExcelExportSupport.java`
  - EasyExcel 기반 공통 export writer
- `backend/src/main/java/com/example/governanceportal/common/excel/ExcelFileValidator.java`
  - 업로드 파일 확장자와 `.xlsx` zip signature 검증
- `backend/src/main/java/com/example/governanceportal/common/excel/ExcelResponseHeaders.java`
  - 다운로드 응답 헤더와 파일명 인코딩
- `backend/src/main/java/com/example/governanceportal/common/excel/ExcelSecurityConfig.java`
  - Apache POI zip bomb/메모리 관련 보안 설정
- `backend/src/main/java/com/example/governanceportal/samplejpa/repository/SampleJpaQueryRepositoryImpl.java`
  - 검색 조건, 정렬, 카운트, chunk 조회
- `backend/src/main/java/com/example/governanceportal/samplejpa/service/SampleJpaSorts.java`
  - 프론트 정렬 필드와 JPA 정렬 매핑

### 1.3 일반 엑셀 다운로드 흐름

1. 사용자가 `SampleListJpaPage.vue`에서 `엑셀 다운로드` 버튼 클릭
2. `downloadExcelFile()`이 현재 목록 요청 조건 `request`를 `downloadSampleJpaExcel(request)`로 전달
3. `api.ts`의 `downloadExcel()`이 `http.downloadPost("/api/samples-jpa/excel/download", request)` 호출
4. `SampleJpaController.downloadExcel()` 진입
5. `SampleJpaExcelService.validateStandardExport()`에서 대상 건수 확인
   - `STANDARD_EXPORT_LIMIT = 50_000`
   - 초과 시 `400 Bad Request`
6. `SampleJpaExcelService.writeExport()`가 `StreamingResponseBody`로 엑셀 작성
7. `writeExportInternal()`이 `EXPORT_CHUNK_SIZE = 1_000` 단위로 DB 조회
8. `ExcelExportSupport.write()`가 EasyExcel writer로 sheet 작성
9. `ExcelResponseHeaders.attachment("sample-jpa.xlsx")`가 다운로드 헤더 설정

일반 다운로드 장애 확인 포인트:

| 증상 | 우선 확인 파트 | 확인 파일/코드 |
| --- | --- | --- |
| 버튼 클릭 후 아무 반응 없음 | 프론트 | `SampleListJpaPage.vue`의 `downloadExcelFile()`, `downloading` 상태, 버튼 권한 `SAMPLE_JPA_EXPORT` |
| 버튼이 보이지 않음 | 권한 | `/api/me` 응답의 `permissions`, `AuthButton auth="SAMPLE_JPA_EXPORT"`, 권한관리 DB 할당 |
| API 호출은 되지만 다운로드가 안 됨 | 프론트 공통 HTTP | `frontend/src/shared/api/http.ts`의 `downloadPost()`, `saveBlobResponse()`, `filenameFromContentDisposition()` |
| 403 Forbidden | 백엔드 권한 | `SampleJpaController.downloadExcel()`의 `@PreAuthorize`, `PortalPermissionService.hasPermission()` |
| 400 오류: 50,000건 초과 | 백엔드 | `SampleJpaExcelService.validateStandardExport()`, `STANDARD_EXPORT_LIMIT` |
| 다운로드 파일명이 깨짐 | 백엔드 | `ExcelResponseHeaders.attachment()`의 `Content-Disposition` 인코딩 |
| 엑셀 파일이 열리지 않음 | 백엔드 | `SampleJpaController.downloadExcel()`의 `StreamingResponseBody`, `ExcelExportSupport.write()`의 `excelWriter.finish()` |
| 데이터가 목록과 다름 | 백엔드 | `SampleJpaExcelService.writeExportInternal()`, `SampleJpaQueryRepositoryImpl.searchRows()`, `SampleJpaSorts.toSort()` |
| 정렬이 적용되지 않음 | 백엔드 | `SampleJpaSorts.toSort()`, `SampleJpaQueryRepositoryImpl.toOrderSpecifier()` |
| 수식처럼 시작하는 값이 엑셀에서 실행됨 | 백엔드 | `ExcelExportSupport.safeCellValue()`의 `=`, `+`, `-`, `@`, tab escaping |

### 1.4 대용량 엑셀 다운로드 흐름

1. 사용자가 `대용량 엑셀` 버튼 클릭
2. `SampleListJpaPage.vue`의 `requestLargeDownloadFile()` 실행
3. `api.ts`의 `requestLargeExcel()`이 `POST /api/samples-jpa/excel/large-download` 호출
4. `SampleJpaController.requestLargeDownload()` 진입
5. `SampleJpaExcelService.requestLargeExport()`에서 대상 건수 확인
   - `.xlsx` sheet 한계: `XLSX_SHEET_ROW_LIMIT = 1_048_576`
   - header 1행을 제외해야 하므로 `1_048_575`건 초과 시 오류
6. `jobId` 생성 후 `largeExportJobs` 인메모리 map에 작업 등록
7. `ExecutorService`가 `runLargeExport()` 비동기 실행
8. 생성 파일은 기본적으로 OS temp 아래 `governance-portal/sample-jpa-excel`에 저장
9. 프론트 `waitLargeDownload()`가 1초 간격으로 최대 60회 상태 조회
10. 상태가 `COMPLETED`이면 `downloadLargeExcel(jobId)`로 파일 다운로드

대용량 다운로드 장애 확인 포인트:

| 증상 | 우선 확인 파트 | 확인 파일/코드 |
| --- | --- | --- |
| 요청 직후 400 오류 | 백엔드 | `SampleJpaExcelService.requestLargeExport()`의 `XLSX_SHEET_ROW_LIMIT` 검증 |
| 403 Forbidden | 백엔드 권한 | `SampleJpaController.requestLargeDownload()`, `getLargeDownload()`, `downloadLargeExcel()`의 `SAMPLE_JPA_EXPORT` 권한 |
| 계속 진행 중이고 완료되지 않음 | 백엔드 | `SampleJpaExcelService.runLargeExport()`, `writeExportInternal()`, DB 조회 성능 |
| 프론트에서 timeout 발생 | 프론트/백엔드 | 프론트 `waitLargeDownload()`의 60초 제한, 백엔드 `runLargeExport()` 처리 시간 |
| 상태가 FAILED | 백엔드 | `SampleJpaExcelService.runLargeExport()` catch 구간, 서버 로그의 실제 예외 |
| 완료 상태인데 다운로드 404 | 백엔드 | `completedLargeExportFile()`, temp 파일 경로, 서버 재시작/임시 파일 삭제 여부 |
| 서버 재시작 후 jobId 조회 404 | 백엔드 | `largeExportJobs`가 인메모리 map임. 운영에서는 DB job table 또는 persistent queue 필요 |
| 메모리/성능 문제 | 백엔드 | `EXPORT_CHUNK_SIZE`, `SampleJpaQueryRepositoryImpl.searchRows()`, EasyExcel writer 처리 |

운영 확장 시 대용량 작업 상태는 인메모리 `largeExportJobs` 대신 DB 테이블에 저장하는 것이 안전하다.

예시:

```sql
create table excel_export_job (
    job_id varchar(36) primary key,
    job_type varchar(50) not null,
    status varchar(20) not null,
    total_rows bigint not null,
    file_path varchar(1000),
    message varchar(1000),
    created_at timestamp not null,
    started_at timestamp,
    completed_at timestamp
);
```

### 1.5 엑셀 업로드 흐름

1. 사용자가 숨겨진 file input으로 `.xlsx` 파일 선택
2. `SampleListJpaPage.vue`의 `onExcelFileChange()` 실행
3. `api.ts`의 `uploadExcel(file)`이 multipart로 `/api/samples-jpa/excel/upload` 호출
4. `SampleJpaController.uploadExcel()` 진입
5. `SampleJpaExcelService.importExcel()` 실행
6. `ExcelFileValidator.validateXlsx()`에서 파일 비어있음, 확장자, zip signature 검증
7. `readRows()`가 EasyExcel로 `SampleJpaExcelRow` 목록 파싱
8. `validateRows()`가 row 단위 업무 검증
   - 동기 업로드 한계: `IMPORT_ROW_LIMIT = 10_000`
   - `ID`가 있으면 기존 row 수정, 없으면 신규 생성
   - `Name` 필수/길이/수식형 문자열 검증
   - `Description` 길이/수식형 문자열 검증
   - 업로드 파일 내부 중복 ID 검증
9. 오류가 있으면 `ExcelImportResult.failed()`를 반환하고 DB 반영 안 함
10. 오류가 없으면 `sampleJpaRepository.saveAllAndFlush(samples)`로 반영

업로드 장애 확인 포인트:

| 증상 | 우선 확인 파트 | 확인 파일/코드 |
| --- | --- | --- |
| 파일 선택 창이 열리지 않음 | 프론트 | `SampleListJpaPage.vue`의 `openExcelUpload()`, `<input ref="uploadInput" type="file">` |
| 업로드 버튼이 보이지 않음 | 권한 | `/api/me` 응답의 `permissions`, `AuthButton auth="SAMPLE_JPA_IMPORT"`, 권한관리 DB 할당 |
| `.xlsx`인데 400 오류 | 백엔드 | `ExcelFileValidator.validateXlsx()`의 확장자와 `PK` signature 검증 |
| 403 Forbidden | 백엔드 권한 | `SampleJpaController.uploadExcel()`의 `@PreAuthorize`, `SAMPLE_JPA_IMPORT` 권한 |
| 413 Payload Too Large | 백엔드/인프라 | `backend/src/main/resources/application.yml`의 `spring.servlet.multipart.max-file-size`, `max-request-size`, Nginx `client_max_body_size` |
| 검증 오류 alert 표시 | 백엔드/프론트 | 백엔드 `SampleJpaExcelService.validateRows()`, 프론트 `importErrorMessage()` |
| 업로드 성공 메시지는 나오지만 목록 반영 안 됨 | 백엔드/프론트 | 백엔드 `importExcel()`의 `saveAllAndFlush()`, 프론트 `onExcelFileChange()`의 `void load()` |
| 기존 데이터 수정이 안 됨 | 백엔드 | `validateRows()`의 `existsById()`, `toEntity()`의 `findById()` 및 `sample.update()` |
| 수식 인젝션 방어가 안 됨 | 백엔드 | `SampleJpaExcelService.isFormulaLike()` |
| 업로드 row 수가 많아 실패 | 백엔드 | `IMPORT_ROW_LIMIT = 10_000`. 대량 import는 비동기 job 방식으로 별도 확장 필요 |

## 2. 첨부파일 기능

### 2.1 현재 구현 범위

현재 공통 첨부 모듈은 파일 본문을 파일시스템에 저장한다. 저장 성공 후 API 응답으로 `attachmentId`, `fileName`, `contentType`, `size`, `downloadUrl`을 반환한다.

현재 코드 기준:

- 파일 본문: `{storageRoot}/{attachmentId}.bin`
- 파일 메타데이터 보조 파일: `{storageRoot}/{attachmentId}.properties`
- `storageRoot`:
  - `GOVERNANCE_ATTACHMENT_STORAGE_ROOT` 또는 `app.attachment.storage-root`가 있으면 해당 경로
  - 없으면 `${java.io.tmpdir}/governance-portal/attachments`
- 게시판 샘플: 게시글은 `gov_ref_board` DB 테이블에 저장하고, 첨부 연결 메타데이터는 `attachment_id`, `attachment_file_name`, `attachment_size`, `attachment_content_type`, `attachment_download_url` 컬럼에 저장한다.
- 공통 첨부 저장소의 `.properties` 파일은 다운로드를 위해 실제 파일명, MIME type, size를 복원하는 보조 메타데이터다.
- 현재 구조는 게시글-첨부 연결 메타데이터는 DB에 저장하고, 파일 본문은 파일시스템에 저장한다. 별도 공통 `attachment_file` 테이블은 아직 만들지 않았다.

### 2.2 주요 파일

공통 백엔드:

- `backend/src/main/java/com/example/governanceportal/common/attachment/AttachmentStorageProperties.java`
  - 첨부파일 저장 root 결정
- `backend/src/main/java/com/example/governanceportal/common/attachment/AttachmentStorageService.java`
  - `store()`: 파일 저장 및 메타데이터 작성
  - `findById()`: `attachmentId` 검증 후 파일/메타데이터 조회
  - `safeFilename()`: 원본 파일명 정리
  - `resolveStoragePath()`: path traversal 방어
- `backend/src/main/java/com/example/governanceportal/common/attachment/AttachmentDownloadSupport.java`
  - 다운로드 응답 헤더 생성
  - `Content-Disposition`, `Cache-Control: no-store`, `X-Content-Type-Options: nosniff`
- `backend/src/main/java/com/example/governanceportal/common/attachment/AttachmentUploadResponse.java`
  - 업로드 응답 DTO
- `backend/src/main/java/com/example/governanceportal/common/attachment/StoredAttachment.java`
  - 저장된 파일 내부 표현 DTO

샘플/게시판 백엔드:

- `backend/src/main/java/com/example/governanceportal/reference/api/ReferenceAttachmentController.java`
  - 공통 태그 샘플 첨부 업로드/다운로드
- `backend/src/main/java/com/example/governanceportal/reference/board/api/RefBoardController.java`
  - 게시판 첨부 업로드/다운로드
- `backend/src/main/java/com/example/governanceportal/reference/board/dto/RefBoardAttachment.java`
  - 게시글에 연결되는 첨부 메타데이터
- `backend/src/main/java/com/example/governanceportal/reference/board/dto/RefBoardSaveRequest.java`
  - 게시글 저장 payload의 `attachment`
- `backend/src/main/java/com/example/governanceportal/reference/board/dto/RefBoardItem.java`
  - 게시글 조회 응답의 `attachment`
- `backend/src/main/java/com/example/governanceportal/reference/board/service/RefBoardService.java`
  - `normalizeAttachment()`: 게시글 저장 시 다운로드 URL 정규화

프론트:

- `frontend/src/shared/components/tags/PortalFilePicker.vue`
  - 파일 선택 공통 컴포넌트
  - `defer` 모드에서는 선택 즉시 v-model 반영하지 않고, 저장 버튼에서 `commit()` 호출 시 반영
- `frontend/src/shared/components/tags/PortalFileLink.vue`
  - 첨부파일 다운로드 링크와 제거 버튼
- `frontend/src/features/_ref-tags/api.ts`
  - 공통 태그 샘플 첨부 업로드 API
- `frontend/src/features/_ref-tags/pages/RefTagComponentsPage.vue`
  - 저장 버튼을 누르는 순간 첨부 업로드하는 샘플
- `frontend/src/features/_ref-board/api.ts`
  - 게시판 첨부 업로드 API `uploadBoardAttachment()`
- `frontend/src/features/_ref-board/pages/RefBoardPage.vue`
  - 게시글 저장 시 첨부 업로드 후 게시글 payload에 첨부 메타데이터 포함
  - 상세 모달에서 `PortalFileLink`로 다운로드 링크 표시

### 2.3 게시판 첨부 저장 흐름

1. 사용자가 게시글 등록/수정 모달에서 파일 선택
2. `PortalFilePicker`가 `defer` 모드로 파일명을 표시만 함
3. 사용자가 `저장` 버튼 클릭
4. `RefBoardPage.vue`의 `save()` 실행
5. `resolveAttachmentForSave()`가 `filePickerRef.commit()`으로 선택 파일 확정
6. 새 파일이 있으면 `uploadBoardAttachment(file)` 호출
7. `POST /api/reference/boards/attachments` 호출
8. `RefBoardController.uploadAttachment()` 실행
9. `AttachmentStorageService.store()`가 파일을 `{attachmentId}.bin`으로 저장하고 `.properties` 메타데이터 작성
10. `AttachmentUploadResponse` 반환
11. 프론트가 반환된 첨부 메타데이터를 게시글 저장 payload의 `attachment`에 포함
12. `POST /api/reference/boards` 또는 `PUT /api/reference/boards/{id}` 호출
13. `RefBoardService.create()` 또는 `update()`가 `normalizeAttachment()`로 `downloadUrl`을 게시판 다운로드 URL로 정규화
14. `RefBoardService`가 `gov_ref_board`의 첨부 컬럼에 메타데이터를 저장
15. 게시글 상세 조회 시 DB 컬럼을 `RefBoardItem.attachment`로 조립하고, 프론트가 `PortalFileLink`로 표시

수정 화면 동작:

- 기존 첨부가 있고 새 파일을 선택하지 않으면 기존 `formAttachment`가 그대로 저장된다.
- 기존 첨부를 제거하면 `formAttachment = null`이 되고 저장 시 게시글에서 첨부 연결이 제거된다.
- 새 파일을 선택하면 저장 시 새 파일 업로드 응답이 기존 첨부를 대체한다.

### 2.4 게시판 첨부 다운로드 흐름

1. 게시글 상세 모달에서 첨부 링크 클릭
2. 링크 URL: `/api/reference/boards/attachments/{attachmentId}`
3. `RefBoardController.downloadAttachment()` 진입
4. `AttachmentStorageService.findById(attachmentId)` 실행
5. `attachmentId` 형식 검증: UUID 36자 패턴
6. `{attachmentId}.bin`, `{attachmentId}.properties` 존재 확인
7. `.properties`에서 `fileName`, `contentType`, `size` 읽기
8. `AttachmentDownloadSupport.attachment()`가 `FileSystemResource`와 다운로드 헤더로 응답

첨부파일 장애 확인 포인트:

| 증상 | 우선 확인 파트 | 확인 파일/코드 |
| --- | --- | --- |
| 파일 선택 후 저장해도 업로드 호출이 없음 | 프론트 | `RefBoardPage.vue`의 `resolveAttachmentForSave()`, `PortalFilePicker`의 `defer`, `commit()` |
| 파일 선택 즉시 업로드됨 | 프론트 | `PortalFilePicker.vue`의 `defer` prop 사용 여부 |
| 업로드 400: empty file | 백엔드 | `AttachmentStorageService.store()`의 `file == null || file.isEmpty()` |
| 업로드 413 | 백엔드/인프라 | `application.yml`의 multipart 설정, Nginx `client_max_body_size` |
| 업로드 500: 저장 실패 | 백엔드/서버 | `AttachmentStorageService.store()`, `storageRoot()` 경로, 디렉터리 권한, 디스크 용량 |
| 다운로드 400: invalid attachment id | 백엔드 | `AttachmentStorageService.validateAttachmentId()` |
| 다운로드 404 | 백엔드/데이터 | `AttachmentStorageService.findById()`, `{attachmentId}.bin`, `{attachmentId}.properties`, `gov_ref_board.attachment_id` 연결 여부 |
| 파일명 깨짐 | 백엔드 | `AttachmentDownloadSupport.headers()`의 `ContentDisposition.filename(..., UTF_8)` |
| 상세 화면에 첨부 링크가 안 보임 | 프론트/백엔드 | 백엔드 `RefBoardItem.attachment`, 프론트 `RefBoardPage.vue` 상세 모달의 `selected.attachment` |
| 게시글 저장은 실패했는데 파일만 남음 | 백엔드/설계 | 현재 파일 업로드와 게시글 저장이 별도 API라 orphan 파일 가능. 운영에서는 임시 상태/정리 배치 필요 |
| 게시글 삭제 후 파일이 남음 | 백엔드/설계 | 현재 삭제 API는 게시글 연결만 삭제. 운영에서는 attachment 참조 수 확인 후 파일 삭제 또는 soft delete 정책 필요 |

### 2.5 현재 DB 연동 방식과 운영 확장 기준

현재 구현은 단건 첨부 기준으로 게시글 테이블에 첨부 메타데이터를 직접 저장한다.

```sql
CREATE TABLE gov_ref_board (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    writer_name VARCHAR(50) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    attachment_id VARCHAR(36),
    attachment_file_name VARCHAR(255),
    attachment_size BIGINT,
    attachment_content_type VARCHAR(255),
    attachment_download_url VARCHAR(500),
    view_count INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

조회 흐름:

1. 게시글 상세 API가 `gov_ref_board` row를 조회한다.
2. `attachment_id`가 있으면 `RefBoardAttachment` DTO를 만든다.
3. 프론트는 `selected.attachment.downloadUrl`로 다운로드 링크를 표시한다.
4. 다운로드 API는 `attachmentId`로 파일시스템의 `{attachmentId}.bin`과 `{attachmentId}.properties`를 읽는다.

현재 방식은 게시판 단건 첨부 샘플에는 충분하지만, 여러 업무에서 첨부를 공통으로 관리하려면 아래 구조로 확장하는 것이 좋다.

공통 운영 확장 시에는 `.properties` 파일 대신 DB attachment table에 메타데이터를 저장하는 구조를 권장한다.

권장 원칙:

- DB에는 파일 본문 전체를 넣지 않고, 메타데이터와 저장 위치를 저장한다.
- 파일 본문은 파일시스템, NAS, S3/OCI Object Storage 같은 외부 storage에 저장한다.
- DB row의 `attachment_id`가 실제 파일명 또는 object key와 연결된다.
- 업무 테이블은 `attachment_id`를 FK로 들고 있거나, 다건 첨부가 필요하면 mapping table을 둔다.

단건 첨부 예시:

```sql
create table attachment_file (
    attachment_id varchar(36) primary key,
    original_file_name varchar(255) not null,
    stored_file_name varchar(255) not null,
    content_type varchar(255) not null,
    file_size bigint not null,
    storage_type varchar(30) not null,
    storage_path varchar(1000) not null,
    created_at timestamp not null,
    created_by varchar(100),
    deleted_yn char(1) not null default 'N'
);

alter table ref_board
    add attachment_id varchar(36);

alter table ref_board
    add constraint fk_ref_board_attachment
    foreign key (attachment_id)
    references attachment_file (attachment_id);
```

다건 첨부 예시:

```sql
create table ref_board_attachment (
    board_id bigint not null,
    attachment_id varchar(36) not null,
    sort_order int not null default 0,
    primary key (board_id, attachment_id),
    foreign key (attachment_id) references attachment_file (attachment_id)
);
```

DB와 실제 파일을 연동해 가져오는 방식:

1. 업로드 시 `attachmentId = UUID.randomUUID().toString()` 생성
2. 실제 파일은 storage에 `{attachmentId}.bin` 또는 object key 기준으로 저장
3. DB `attachment_file`에 다음 값을 저장
   - `attachment_id`: UUID
   - `original_file_name`: 사용자가 올린 파일명
   - `stored_file_name`: `{attachmentId}.bin`
   - `content_type`: MIME type
   - `file_size`: byte size
   - `storage_path`: storage root 기준 상대 경로 또는 object key
4. 게시글 저장 시 `ref_board.attachment_id` 또는 `ref_board_attachment.attachment_id`에 같은 UUID 저장
5. 게시글 상세 조회 시 join 또는 별도 조회로 첨부 메타데이터를 `RefBoardAttachment` DTO로 구성
6. 다운로드 요청 시 URL의 `attachmentId`로 `attachment_file` row 조회
7. 권한 확인 후 `storage_path`로 실제 파일을 열어 `AttachmentDownloadSupport`로 stream 응답

DB 적용 시 코드 변경 기준:

- 새로 만들 파일
  - `backend/src/main/java/com/example/governanceportal/common/attachment/domain/AttachmentFile.java`
  - `backend/src/main/java/com/example/governanceportal/common/attachment/repository/AttachmentFileRepository.java`
- 변경할 파일
  - `AttachmentStorageService.store()`
    - 파일 저장 후 `attachment_file` insert
    - DB 저장 실패 시 방금 저장한 실제 파일 cleanup
  - `AttachmentStorageService.findById()`
    - `.properties` 파일 대신 DB row 조회
    - DB row의 `storage_path`로 실제 파일 경로 해석
  - `RefBoardService.create()`, `update()`, `findById()`
    - 인메모리 `RefBoardAttachment` 대신 board entity의 `attachmentId` 저장/조회
  - `RefBoardController.downloadAttachment()`
    - 현재 endpoint는 유지 가능
    - 다운로드 전 게시글/권한 연결 확인이 필요하면 service 계층으로 위임

파일 본문을 DB BLOB에 직접 저장하는 방식도 가능하지만 현재 공통 모듈 구조와는 다르다. BLOB 방식으로 바꾸려면 `AttachmentStorageService.store()`가 `MultipartFile.getBytes()`를 DB LOB 컬럼에 저장하고, `findById()`가 `ByteArrayResource` 또는 streaming LOB resource를 반환하도록 별도 구현해야 한다. 대용량 파일, 백업 크기, DB 부하를 고려하면 현재 구조처럼 DB에는 메타데이터만 저장하고 파일 본문은 별도 storage에 두는 방식을 우선 권장한다.

## 3. 검증 명령

백엔드:

```powershell
cd C:\workspace\governance-portal\backend
.\gradlew.bat test
```

프론트:

```powershell
cd C:\workspace\governance-portal\frontend
npm.cmd run lint
npm.cmd run build
```

관련 테스트:

- `GovernancePortalApplicationTests.referenceAttachmentCanBeUploadedAndDownloadedAgain`
- `GovernancePortalApplicationTests.referenceBoardAttachmentCanBeUploadedSavedAndDownloaded`
- `GovernancePortalApplicationTests.referenceBoardCrudWorks`
- Sample JPA CRUD/search 테스트는 엑셀 다운로드 대상 데이터와 QueryDSL 조회 경로를 함께 검증한다.
