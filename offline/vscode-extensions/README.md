# VS Code Offline Extensions

이 폴더는 Governance Portal 프로젝트를 내부망에서 VS Code로 개발할 때 필요한 확장 프로그램 VSIX 파일과 설치 스크립트를 보관한다.

## 폴더 구조

```text
offline/vscode-extensions/
  README.md
  extensions.json
  download-vsix.ps1
  install-vsix.ps1
  vsix/
    *.vsix
```

`vsix/` 폴더의 파일을 내부망 PC로 복사한 뒤 `install-vsix.ps1`을 실행하면 VS Code 확장을 오프라인 설치할 수 있다.

VSIX 파일은 대용량 바이너리이므로 Git 추적 대상에서 제외한다. 내부망 반입 시에는 `offline/vscode-extensions` 폴더 전체를 파일 복사 또는 압축 파일로 전달한다.

## 필수 확장

| Extension ID | 용도 |
| --- | --- |
| `Vue.volar` | Vue 3 SFC, `<script setup lang="ts">`, template 인식 |
| `dbaeumer.vscode-eslint` | ESLint 진단 표시와 자동 수정 |
| `redhat.java` | Java 21 language server |
| `vscjava.vscode-gradle` | Gradle task/build/test |
| `vscjava.vscode-java-debug` | Java debugging |
| `vscjava.vscode-java-test` | JUnit test discovery |
| `vscjava.vscode-java-dependency` | Java dependency/project explorer |
| `vmware.vscode-spring-boot` | Spring Boot 설정/메타데이터 지원 |

## 있으면 좋은 확장

| Extension ID | 용도 |
| --- | --- |
| `vscjava.vscode-spring-boot-dashboard` | Spring Boot 실행 대시보드 |
| `vscjava.vscode-spring-initializr` | Spring Boot 도구 연계 |
| `redhat.vscode-yaml` | `application.yml`, 배포 YAML 편집 |
| `redhat.vscode-xml` | SAML metadata 등 XML 편집 |
| `bradlc.vscode-tailwindcss` | Tailwind CSS class 자동완성 |
| `csstools.postcss` | PostCSS 문법 지원 |
| `ms-playwright.playwright` | Playwright e2e 테스트 |
| `vitest.explorer` | Vitest 단위 테스트 |
| `ms-vscode.PowerShell` | PowerShell 스크립트 편집 |
| `EditorConfig.EditorConfig` | EditorConfig 정책 연동 |
| `yzhang.markdown-all-in-one` | Markdown 문서 작성 |
| `humao.rest-client` | `.http` API 테스트 |
| `christian-kohler.path-intellisense` | 경로 자동완성 |
| `christian-kohler.npm-intellisense` | npm import 자동완성 |
| `eamodio.gitlens` | Git 이력 확인 |
| `Oracle.oracledevtools` | Oracle DB/ADB 도구 |

## 다운로드 방법

인터넷 가능한 PC에서 실행한다.

```powershell
cd C:\workspace\governance-portal
powershell -ExecutionPolicy Bypass -File .\offline\vscode-extensions\download-vsix.ps1
```

다운로드 결과는 `offline/vscode-extensions/vsix`에 저장된다.

다운로드 후 `checksums.sha256` 파일로 복사 무결성을 확인할 수 있다.

### 특정 확장 프로그램 제외 방법

일부 확장 프로그램이 필요하지 않은 경우, `download-vsix.ps1`을 실행하기 전에 `extensions.json` 파일을 수정하여 다운로드 대상에서 제외할 수 있습니다.

1.  `offline/vscode-extensions/extensions.json` 파일을 엽니다.
2.  제외하고자 하는 확장 프로그램의 객체( `{ ... }` )를 배열에서 삭제합니다.
3.  파일을 저장한 후 `download-vsix.ps1` 스크립트를 다시 실행합니다.

> **참고**: 이미 `vsix/` 폴더에 다운로드된 파일은 스크립트가 삭제하지 않으므로, 깔끔한 관리를 원하시면 `vsix/` 폴더 내의 해당 파일을 수동으로 삭제해 주세요.

## 내부망 설치 방법

내부망 PC에 `offline/vscode-extensions` 폴더를 복사한 뒤 실행한다.

```powershell
cd C:\workspace\governance-portal
powershell -ExecutionPolicy Bypass -File .\offline\vscode-extensions\install-vsix.ps1
```

VS Code CLI 명령이 `code`가 아닌 다른 이름이면 다음처럼 지정한다.

```powershell
powershell -ExecutionPolicy Bypass -File .\offline\vscode-extensions\install-vsix.ps1 -CodeCommand "C:\Users\user\AppData\Local\Programs\Microsoft VS Code\bin\code.cmd"
```

설치 후 VS Code를 재시작한다.

## 주의사항

- Vue 3 프로젝트에서는 `Vue.volar`를 사용한다. 구 확장인 Vetur는 같이 설치하지 않는다.
- `dev/prod` 서버 DB는 VS Code 확장과 별개로 env 프로파일 설정이 필요하다.
- Java/Spring 확장은 VS Code 내부에서 JDK 21을 찾을 수 있어야 정상 동작한다.
- 확장 버전을 갱신하려면 인터넷 가능한 PC에서 `download-vsix.ps1`을 다시 실행한다.
