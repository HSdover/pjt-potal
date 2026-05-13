# Permission Convention

작성일: 2026-05-12

## 라우트 권한

화면 권한 코드는 라우터 `meta.auth`에 둔다.

```ts
{
  path: "/metadata",
  component: () => import("@/features/metadata/pages/MetadataListPage.vue"),
  meta: { title: "메타데이터", menu: true, order: 20, auth: "METADATA_READ" },
}
```

`meta.menu`가 `true`인 라우트만 메뉴에 노출한다. 메뉴명은 `meta.title`, 정렬은 `meta.order`를 따른다.

## 버튼 권한

업무 버튼은 `AuthButton`을 사용한다.

```vue
<AuthButton auth="METADATA_READ" type="primary" @click="search">조회</AuthButton>
```

프론트 권한 제어는 사용자 경험을 위한 노출 제어다. 실제 보안은 백엔드 API 권한 검증으로 보장해야 한다.

## 현재 상태

현재 권한은 샘플 권한 목록을 기준으로 동작한다. 로그인과 사용자별 권한 API가 생기면 전역 auth store로 이동한다.
