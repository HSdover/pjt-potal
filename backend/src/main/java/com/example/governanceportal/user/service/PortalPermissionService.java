package com.example.governanceportal.user.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import com.example.governanceportal.user.config.SamlPermissionMappingProperties;

@Service
public class PortalPermissionService {

    private final SamlPermissionMappingProperties samlProperties;

    public PortalPermissionService(SamlPermissionMappingProperties samlProperties) {
        this.samlProperties = samlProperties;
    }

    public boolean hasPermission(Authentication authentication, String permission) {
        if (authentication == null || permission == null || permission.isBlank()) {
            return false;
        }

        return permissions(authentication).contains(permission);
    }

    public List<String> permissions(Authentication authentication) {
        if (authentication == null) {
            return List.of();
        }

        if (authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal principal) {
            return samlPermissions(principal);
        }

        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(value -> value != null && !value.isBlank())
            .distinct()
            .toList();
    }

    private List<String> samlPermissions(Saml2AuthenticatedPrincipal principal) {
        Set<String> permissions = new LinkedHashSet<>();

        samlProperties.getDirectPermissionAttributes().stream()
            .flatMap(attributeName -> attributeValues(principal, attributeName).stream())
            .filter(value -> value != null && !value.isBlank())
            .forEach(permissions::add);

        samlProperties.getGroupAttributes().stream()
            .flatMap(attributeName -> attributeValues(principal, attributeName).stream())
            .flatMap(group -> mappedPermissions(group).stream())
            .filter(value -> value != null && !value.isBlank())
            .forEach(permissions::add);

        return List.copyOf(permissions);
    }

    private List<String> mappedPermissions(String externalGroup) {
        String normalized = normalizeGroup(externalGroup);
        List<String> mapped = samlProperties.getGroupPermissionMappings().get(normalized);
        if (mapped != null) {
            return mapped;
        }

        mapped = samlProperties.getGroupPermissionMappings().get(externalGroup);
        return mapped == null ? List.of() : mapped;
    }

    private String normalizeGroup(String externalGroup) {
        if (externalGroup == null) {
            return "";
        }

        String trimmed = externalGroup.trim();
        if (trimmed.regionMatches(true, 0, "CN=", 0, 3)) {
            int commaIndex = trimmed.indexOf(',');
            return commaIndex > 3 ? trimmed.substring(3, commaIndex).trim() : trimmed.substring(3).trim();
        }

        return trimmed;
    }

    private List<String> attributeValues(Saml2AuthenticatedPrincipal principal, String name) {
        List<?> values = principal.getAttribute(name);
        if (values == null) {
            return List.of();
        }

        return values.stream()
            .map(String::valueOf)
            .toList();
    }
}
