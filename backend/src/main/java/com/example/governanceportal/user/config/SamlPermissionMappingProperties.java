package com.example.governanceportal.user.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth.saml")
public class SamlPermissionMappingProperties {

    private List<String> directPermissionAttributes = new ArrayList<>();
    private List<String> groupAttributes = new ArrayList<>(List.of("roles", "groups", "memberOf"));
    private Map<String, List<String>> groupPermissionMappings = new LinkedHashMap<>();

    public List<String> getDirectPermissionAttributes() {
        return directPermissionAttributes;
    }

    public void setDirectPermissionAttributes(List<String> directPermissionAttributes) {
        this.directPermissionAttributes = directPermissionAttributes == null
            ? new ArrayList<>()
            : new ArrayList<>(directPermissionAttributes);
    }

    public List<String> getGroupAttributes() {
        return groupAttributes;
    }

    public void setGroupAttributes(List<String> groupAttributes) {
        this.groupAttributes = groupAttributes == null ? new ArrayList<>() : new ArrayList<>(groupAttributes);
    }

    public Map<String, List<String>> getGroupPermissionMappings() {
        return groupPermissionMappings;
    }

    public void setGroupPermissionMappings(Map<String, List<String>> groupPermissionMappings) {
        this.groupPermissionMappings = groupPermissionMappings == null
            ? new LinkedHashMap<>()
            : new LinkedHashMap<>(groupPermissionMappings);
    }
}
