package com.example.governanceportal.user.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth.local-dev-user")
public class LocalDevUserProperties {

    private boolean enabled;
    private String userId = "local-dev";
    private String displayName = "Local Developer";
    private String password = "local1234!";
    private List<String> permissions = new ArrayList<>();
    private List<LocalUser> users = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions == null ? new ArrayList<>() : new ArrayList<>(permissions);
    }

    public List<LocalUser> getUsers() {
        return users;
    }

    public void setUsers(List<LocalUser> users) {
        this.users = users == null ? new ArrayList<>() : new ArrayList<>(users);
    }

    public List<LocalUser> effectiveUsers() {
        if (!users.isEmpty()) {
            return List.copyOf(users);
        }

        LocalUser user = new LocalUser();
        user.setUserId(userId);
        user.setDisplayName(displayName);
        user.setPassword(password);
        user.setPermissions(permissions);
        return List.of(user);
    }

    public Optional<LocalUser> findUser(String requestedUserId) {
        if (requestedUserId == null || requestedUserId.isBlank()) {
            return Optional.empty();
        }

        return effectiveUsers().stream()
            .filter(user -> requestedUserId.equals(user.getUserId()))
            .findFirst();
    }

    public static class LocalUser {

        private String userId = "";
        private String displayName = "";
        private String password = "";
        private List<String> permissions = new ArrayList<>();

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDisplayName() {
            return displayName == null || displayName.isBlank() ? userId : displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<String> permissions) {
            this.permissions = permissions == null ? new ArrayList<>() : new ArrayList<>(permissions);
        }
    }
}
