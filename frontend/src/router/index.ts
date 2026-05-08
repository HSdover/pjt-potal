import { createRouter, createWebHistory } from "vue-router";
import DashboardView from "@/views/DashboardView.vue";
import MetadataView from "@/views/MetadataView.vue";
import SourceSampleView from "@/views/SourceSampleView.vue";
import LineageView from "@/views/LineageView.vue";
import TableDetailSampleView from "@/views/TableDetailSampleView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/",              component: DashboardView },
    { path: "/dashboard",     redirect: "/" },
    { path: "/metadata",      component: MetadataView },
    { path: "/source-sample", component: SourceSampleView },
    { path: "/lineage",       component: LineageView },
    { path: "/table-detail-sample", component: TableDetailSampleView },
  ],
});

export default router;
