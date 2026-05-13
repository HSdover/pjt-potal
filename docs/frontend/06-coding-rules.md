# Coding Rules

작성일: 2026-05-12

## 파일 배치

- 업무 타입은 `features/{feature}/types.ts`에 둔다.
- 업무 API 함수는 `features/{feature}/api.ts`에 둔다.
- AG Grid 컬럼은 `features/{feature}/columns.ts`에 둔다.
- 화면은 `features/{feature}/pages`에 둔다.

## Vue 작성 규칙

- `<script setup lang="ts">`를 사용한다.
- 화면 상태는 우선 화면 내부 `ref`, `reactive`로 관리한다.
- 여러 화면이 공유하는 상태만 store 후보로 올린다.
- API 호출은 `try/catch/finally`로 로딩과 오류를 함께 처리한다.

## Import 규칙

- 공통 모듈은 `@/shared/...` alias를 사용한다.
- 같은 feature 내부 파일은 상대 경로를 사용한다.
- 타입 전용 import는 `import type`을 사용한다.

## 주석

주석은 반복 규칙을 설명하거나 기준 문서와 연결할 때만 짧게 남긴다. 코드가 그대로 말하는 내용은 주석으로 반복하지 않는다.
