import { createApp } from "vue";
import ElementPlus from "element-plus";
import { createPinia } from "pinia";
import router from "./router";
import "element-plus/dist/index.css";
import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-quartz.css";
import "./styles.css";
import App from "./App.vue";
import { useAuthStore } from "./stores/auth";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia).use(ElementPlus);

// 세션을 먼저 동기화한 뒤 마운트. 실패(백엔드 미기동 등)해도 기본 권한으로 그대로 마운트한다.
const auth = useAuthStore(pinia);
auth.loadSession().finally(() => {
  app.use(router);
  app.mount("#app");
});
