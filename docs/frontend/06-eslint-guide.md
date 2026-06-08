# ESLint Guide

이 문서는 Governance Portal 프론트엔드에서 ESLint가 맡는 역할과 설정/운영 방법을 정리한다.

## 역할

ESLint는 `frontend` 소스의 정적 분석 도구다. 실행하지 않아도 알 수 있는 코드 문제를 개발 단계에서 잡는다.

- Vue SFC(`.vue`)와 TypeScript(`.ts`) 문법 오류 및 위험 패턴을 검출한다.
- 사용하지 않는 변수, 잘못된 import, 기본 JavaScript 오류를 검출한다.
- Vue 컴포넌트 템플릿/스크립트의 기본 규칙을 검증한다.
- 팀 공통 예외 규칙을 코드 리뷰 전에 자동으로 확인한다.
- `--max-warnings=0` 기준으로 경고도 실패 처리해 빌드 전 품질선을 맞춘다.

ESLint는 타입 전체 검증 도구가 아니다. 타입 검증은 `npm run build`에서 실행되는 `vue-tsc --noEmit`이 담당한다.

## 설정 파일

설정 파일은 [frontend/eslint.config.js](../../frontend/eslint.config.js)에 있다. 이 프로젝트는 ESLint flat config 방식을 사용한다.

현재 구성:

```js
import js from "@eslint/js";
import globals from "globals";
import tseslint from "typescript-eslint";
import vue from "eslint-plugin-vue";
```

적용되는 기본 규칙:

- `@eslint/js`의 `recommended`
- `typescript-eslint`의 `recommended`
- `eslint-plugin-vue`의 `flat/essential`

검사 제외:

```js
ignores: ["dist/**", "node_modules/**", "coverage/**"]
```

브라우저 소스 대상:

```js
files: ["**/*.{ts,vue}"]
```

이 대상에는 브라우저 전역 객체와 ES2022 전역 객체를 허용한다.

```js
globals: {
  ...globals.browser,
  ...globals.es2022,
}
```

Vue 파일은 `vue-eslint-parser`와 `typescript-eslint` parser 조합으로 분석한다.

```js
parserOptions: {
  parser: tseslint.parser,
  extraFileExtensions: [".vue"],
}
```

Node 환경 설정 파일과 테스트 파일은 Node 전역 객체를 허용한다.

```js
files: ["*.config.{js,ts}", "vite.config.ts", "src/**/*.test.ts"]
```

## 프로젝트 예외 규칙

현재 프로젝트에서 명시적으로 조정한 규칙은 세 가지다.

| 규칙 | 설정 | 이유 |
| --- | --- | --- |
| `no-console` | `warn`, 단 `console.warn/error` 허용 | 개발 중 로그 남용은 경고하되 오류 로그는 허용 |
| `vue/multi-word-component-names` | `off` | `App.vue`, 화면 단위 이름처럼 단일 단어 컴포넌트를 허용 |
| `vue/no-v-html` | `off` | Tiptap 게시글/리치 텍스트 렌더링 등 HTML 출력이 필요한 화면을 허용 |

`vue/no-v-html`을 꺼두었더라도 외부 입력 HTML은 반드시 신뢰 경계와 저장/출력 정책을 확인해야 한다.

## 실행 방법

프론트엔드 디렉터리에서 실행한다.

```powershell
cd frontend
npm.cmd run lint
```

자동 수정 가능한 항목은 아래 명령으로 처리한다.

```powershell
cd frontend
npm.cmd run lint:fix
```

관련 npm script는 [frontend/package.json](../../frontend/package.json)에 있다.

```json
{
  "lint": "eslint . --max-warnings=0",
  "lint:fix": "eslint . --fix"
}
```

`--max-warnings=0` 때문에 warning도 실패로 처리된다. 경고를 남긴 채 PR/반입을 진행하지 않는 기준이다.

## 빌드와의 관계

프론트 검증은 보통 아래 순서로 본다.

```powershell
cd frontend
npm.cmd run lint
npm.cmd run build
```

- `npm.cmd run lint`: 코드 스타일/정적 규칙 확인
- `npm.cmd run build`: `vue-tsc --noEmit` 타입 검증 후 Vite production build

ESLint가 통과해도 타입 오류가 남을 수 있고, 타입 빌드가 통과해도 ESLint 규칙 위반이 남을 수 있다. 둘 다 통과해야 프론트 변경 검증이 완료된다.

## 설정 변경 기준

규칙을 바꿀 때는 [frontend/eslint.config.js](../../frontend/eslint.config.js)를 수정한다.

권장 기준:

- 프로젝트 전체에 반복되는 합리적인 예외만 `rules`에 추가한다.
- 특정 파일 한두 곳만 필요한 예외는 가능한 코드 구조를 먼저 정리한다.
- 불가피한 경우에만 해당 라인 근처에 `eslint-disable` 주석을 둔다.
- 새 라이브러리나 실행 환경을 추가하면 `globals` 또는 대상 `files`를 함께 검토한다.
- 규칙을 완화한 뒤에는 `npm.cmd run lint`와 `npm.cmd run build`를 모두 실행한다.

피해야 할 변경:

- `dist`, `node_modules` 외의 실제 소스 디렉터리를 광범위하게 ignore 처리
- `typescript-eslint` 또는 `vue` recommended 설정 제거
- `--max-warnings=0` 제거
- 화면 단위 오류를 해결하지 않고 전역 규칙을 낮추는 방식

## 자주 보는 오류

| 증상 | 확인 위치 | 처리 |
| --- | --- | --- |
| unused variable/import 오류 | 해당 `.ts`/`.vue` 파일 | 사용하지 않는 선언 제거 |
| `no-console` 경고 | `console.log` 사용 위치 | 제거하거나 `console.warn/error`가 맞는지 판단 |
| Vue template 규칙 오류 | `.vue` template 영역 | 컴포넌트 속성/이벤트 바인딩 구조 수정 |
| Node 전역 객체 오류 | config/test 파일 | 대상 파일이 `*.config.{js,ts}`, `vite.config.ts`, `src/**/*.test.ts` 범위인지 확인 |
| 타입 오류는 없는데 lint 실패 | `npm.cmd run lint` 출력 | ESLint 규칙 위반을 수정 |
| lint는 통과하지만 build 실패 | `npm.cmd run build` 출력 | `vue-tsc` 타입 오류 또는 Vite build 오류 수정 |

## 신규 화면 작성 시 체크

- 새 화면 파일은 `frontend/src/features/{feature}/pages/*.vue` 구조를 따른다.
- import alias는 기존처럼 `@/`를 사용한다.
- 사용하지 않는 import는 남기지 않는다.
- 이벤트 핸들러, computed, ref 이름은 화면 의미가 드러나게 둔다.
- `v-html`을 사용할 때는 입력 HTML의 출처와 저장 정책을 확인한다.
- 제출 전 `npm.cmd run lint`와 `npm.cmd run build`를 실행한다.
