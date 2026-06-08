import { defineStore } from "pinia";
import { fetchCurrentUser, loginLocal, logoutLocal, type LocalLoginRequest } from "@/shared/api/session";
import { logClientError } from "@/shared/api/error-handler";

type AuthSession = {
  userId: string;
  userName: string;
  authenticated: boolean;
  permissions: string[];
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
    userId: "",
    userName: "",
    authenticated: false,
    sessionLoaded: false,
    permissions: [] as string[],
  }),

  getters: {
    isAuthenticated: (state) => state.authenticated,
    isSessionReady: (state) => state.sessionLoaded,
  },

  actions: {
    hasPermission(permission: string) {
      return this.permissions.includes(permission);
    },

    hasPermissions(permissions: string[]) {
      return permissions.every((permission) => this.hasPermission(permission));
    },

    setSession(session: AuthSession) {
      this.userId = session.userId;
      this.userName = session.userName;
      this.authenticated = session.authenticated;
      this.permissions = [...session.permissions];
      this.sessionLoaded = true;
    },

    async loadSession() {
      try {
        const user = await fetchCurrentUser();
        this.setSession({
          userId: user.userId,
          userName: user.displayName,
          authenticated: user.authenticated,
          permissions: user.permissions,
        });
      } catch (error) {
        logClientError(error, "Failed to load current session.");
        this.clearSession();
      } finally {
        this.sessionLoaded = true;
      }
    },

    async login(request: LocalLoginRequest) {
      const user = await loginLocal(request);
      this.setSession({
        userId: user.userId,
        userName: user.displayName,
        authenticated: user.authenticated,
        permissions: user.permissions,
      });
      return user;
    },

    async logout() {
      try {
        await logoutLocal();
      } finally {
        this.clearSession();
      }
    },

    clearSession() {
      this.userId = "";
      this.userName = "";
      this.authenticated = false;
      this.permissions = [];
      this.sessionLoaded = true;
    },
  },
});
