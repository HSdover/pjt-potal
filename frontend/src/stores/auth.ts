import { defineStore } from "pinia";
import { fetchCurrentUser } from "@/shared/api/session";

type AuthSession = {
  userName: string;
  authenticated: boolean;
  permissions: string[];
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
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
      this.userName = session.userName;
      this.authenticated = session.authenticated;
      this.permissions = [...session.permissions];
      this.sessionLoaded = true;
    },

    async loadSession() {
      try {
        const user = await fetchCurrentUser();
        this.setSession({
          userName: user.displayName,
          authenticated: user.authenticated,
          permissions: user.permissions,
        });
      } catch (error) {
        console.warn("Failed to load current session.", error);
        this.clearSession();
      } finally {
        this.sessionLoaded = true;
      }
    },

    clearSession() {
      this.userName = "";
      this.authenticated = false;
      this.permissions = [];
    },
  },
});
