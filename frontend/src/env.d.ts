/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_REFERENCE_MENU_ENABLED?: string;
  readonly VITE_API_PROXY_TARGET?: string;
  readonly VITE_LOGIN_ENTRY_PATH?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

declare module "*.vue" {
  import type { DefineComponent } from "vue";

  const component: DefineComponent<object, object, unknown>;
  export default component;
}
