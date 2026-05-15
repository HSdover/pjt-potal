import { defineStore } from "pinia";

export const SAMPLE_PERMISSIONS = [
  "DASHBOARD_READ",
  "METADATA_READ",
  "METADATA_SAVE",
  "SOURCE_SAMPLE_READ",
  "LINEAGE_READ",
  "SAMPLE_READ",
  "SAMPLE_CREATE",
  "SAMPLE_UPDATE",
  "SAMPLE_DELETE",
  "SAMPLE_JPA_READ",
  "SAMPLE_JPA_CREATE",
  "SAMPLE_JPA_UPDATE",
  "SAMPLE_JPA_DELETE",
] as const;

type AuthSession = {
  userName: string;
  permissions: string[];
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
    userName: "Sample User",
    permissions: [...SAMPLE_PERMISSIONS] as string[],
  }),

  getters: {
    isAuthenticated: (state) => state.permissions.length > 0,
  },

  actions: {
    hasPermission(permission: string) {
      return this.permissions.includes(permission);
    },

    hasPermissions(permissions: string[]) {
      return permissions.every((permission) => this.hasPermission(permission));
    },

    setSession(session: AuthSession) {
      this.userName = session.userName;
      this.permissions = [...session.permissions];
    },

    clearSession() {
      this.userName = "";
      this.permissions = [];
    },
  },
});
