<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElInput, ElMessage } from "element-plus";
import { Lock, User } from "@element-plus/icons-vue";
import PortalButton from "@/shared/components/tags/PortalButton.vue";
import { useAuthStore } from "@/stores/auth";
import { toErrorMessage } from "@/shared/api/error-handler";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);

const form = reactive({
  userId: "local-admin",
  password: "",
});

function redirectTarget() {
  const redirect = route.query.redirect;
  return typeof redirect === "string" && redirect.startsWith("/") ? redirect : "/";
}

async function submit() {
  if (!form.userId.trim() || !form.password) {
    ElMessage.error("아이디와 비밀번호를 입력하세요.");
    return;
  }

  loading.value = true;
  try {
    await auth.login({
      userId: form.userId.trim(),
      password: form.password,
    });
    await router.replace(redirectTarget());
  } catch (error) {
    ElMessage.error(toErrorMessage(error, "아이디 또는 비밀번호를 확인하세요."));
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-brand">
        <div class="portal-brand-mark">POP</div>
        <div>
          <h1>Governance Portal</h1>
          <p>AI DATA PLATFORM</p>
        </div>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <label class="portal-field">
          <span class="portal-field-label">아이디</span>
          <ElInput
            v-model="form.userId"
            :prefix-icon="User"
            clearable
            size="large"
            autocomplete="username"
          />
        </label>

        <label class="portal-field">
          <span class="portal-field-label">비밀번호</span>
          <ElInput
            v-model="form.password"
            :prefix-icon="Lock"
            type="password"
            show-password
            size="large"
            autocomplete="current-password"
            @keyup.enter="submit"
          />
        </label>

        <PortalButton type="submit" variant="primary" :disabled="loading">
          {{ loading ? "로그인 중" : "로그인" }}
        </PortalButton>
      </form>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background:
    radial-gradient(circle at 12% 0, rgba(0, 166, 214, 0.12), transparent 28rem),
    linear-gradient(180deg, #eef4ff 0, #f7f9fc 22rem, #f4f7fb 100%);
  padding: 24px;
}

.login-panel {
  width: min(100%, 420px);
  border: 1px solid var(--portal-line);
  border-radius: var(--portal-radius);
  background: #fff;
  box-shadow: var(--portal-shadow);
  padding: 28px;
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.login-brand h1 {
  margin: 0;
  color: var(--portal-ink);
  font-size: 22px;
  font-weight: 900;
  letter-spacing: 0;
}

.login-brand p {
  margin: 4px 0 0;
  color: var(--portal-blue);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

.login-form {
  display: grid;
  gap: 14px;
}

.login-form .portal-tag-button {
  width: 100%;
  min-height: 40px;
}
</style>
