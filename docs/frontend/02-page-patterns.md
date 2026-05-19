# Page Patterns

작성일: 2026-05-12

## SearchGridPage

검색 조건, 조회/초기화 버튼, 목록 그리드, 페이징을 가진 표준 목록 화면이다.

기본 구성:

- `SearchPanel`에 검색 조건과 액션 버튼을 배치한다.
- `BaseGrid`에 목록 데이터, 컬럼, 로딩, 페이징 값을 전달한다.
- 검색, 페이지 변경, 페이지 크기 변경, 정렬 변경은 서버 목록 API를 다시 호출한다.
- 버튼은 `AuthButton`을 사용해 화면 권한 기준과 맞춘다.

## SearchGridDetailPage

목록 행을 클릭했을 때 상세 패널을 함께 보여주는 유형이다.

현재 운영 라우터에는 기준 구현체가 없다. RFP 기준 데이터셋 상세 화면을 신규 설계할 때 다시 정의한다.

## FormDialogPage

목록 화면 안에서 등록/수정 다이얼로그를 제공하는 유형이다. 현재 기준 구현체는 `SampleListPage.vue`, `SampleListJpaPage.vue`다.

- 다이얼로그 입력 상태는 화면 전용 form 타입으로 관리한다.
- Vuelidate 검증 규칙은 `shared/validation/vuelidate.ts`의 공통 헬퍼를 사용한다.
- 저장 전에 `v$.value.$validate()`를 호출한다.
- 다이얼로그를 열 때 이전 검증 상태가 남지 않도록 `v$.value.$reset()`을 호출한다.

## 새 유형 추가 기준

생성기는 현재 `search-grid`만 지원한다. `form`, `detail`, `search-grid-detail` 같은 유형은 반복 구현이 2개 이상 생겼을 때 추가한다.
