# Permission Convention

작성일: 2026-05-12
수정일: 2026-06-08

## 라우트 권한

화면 권한 코드는 라우터 `meta.auth`에 둔다. `meta.menu`가 `true`인 라우트만 메뉴에 노출되며, 로그인 사용자가 `meta.auth` 권한을 갖고 있을 때만 메뉴와 화면 접근이 허용된다.

```ts
{
  path: "/sample-list",
  component: () => import("@/features/sample-list/pages/SampleListPage.vue"),
  meta: { title: "샘플 CRUD", menu: true, order: 20, auth: "SAMPLE_READ" },
}
```

현재 메뉴는 1~3레벨 메타를 함께 사용한다.

```ts
meta: {
  menu: true,
  menuLevel1Key: "system",
  menuLevel1Title: "시스템 관리",
  menuLevel2Key: "permissions",
  menuLevel2Title: "권한관리",
  auth: "PERMISSION_MANAGE",
}
```

## 버튼 권한

업무 버튼은 `AuthButton`을 사용한다.

```vue
<AuthButton auth="SAMPLE_READ" type="primary" @click="search">조회</AuthButton>
```

프론트 권한 제어는 사용자 경험을 위한 노출 제어다. 실제 보안은 백엔드 API 권한 검증으로 보장해야 한다.

## 서버단 권한

프론트에서 버튼을 숨기더라도 API를 직접 호출할 수 있으므로, 백엔드 컨트롤러 또는 서비스 진입점에 서버단 권한 검사를 같이 둔다.

```java
@PreAuthorize("@portalPermissionService.hasPermission(authentication, 'SAMPLE_JPA_EXPORT')")
@PostMapping("/excel/download")
public ResponseEntity<StreamingResponseBody> downloadExcel(...) {
    ...
}
```

주요 권한 예:

| 권한 | 용도 |
|---|---|
| `DASHBOARD_READ` | 대시보드 조회 |
| `META_VIEW` | 메타관리 조회 |
| `PERMISSION_MANAGE` | 시스템 관리 > 권한관리 |
| `SAMPLE_JPA_EXPORT` | JPA 샘플 다운로드 |
| `SAMPLE_JPA_IMPORT` | JPA 샘플 업로드 |

## 권한관리 화면

권한관리 화면은 `frontend/src/features/permission-management/pages/PermissionManagementPage.vue`에 있다.

백엔드 기준:

- `portal_permission`: 권한 코드 정의
- `portal_iam_role`: IAM 역할 정의
- `portal_permission_assignment`: 사용자/그룹/IAM 역할별 권한 할당

상세 내용은 `docs/permission-management-guide.md`를 따른다.
