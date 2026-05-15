import { createApp } from "vue";
import ElementPlus from "element-plus";
import { createPinia } from "pinia";
import router from "./router";
import "element-plus/dist/index.css";
import "ag-grid-community/styles/ag-grid.css";
import "ag-grid-community/styles/ag-theme-quartz.css";
import "@vue-flow/core/dist/style.css";
import "./styles.css";
import App from "./App.vue";

createApp(App)
  .use(createPinia())
  .use(ElementPlus)
  .use(router)
  .mount("#app");
