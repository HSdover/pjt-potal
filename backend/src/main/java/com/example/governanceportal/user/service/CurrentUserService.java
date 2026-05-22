package com.example.governanceportal.user.service;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import com.example.governanceportal.user.config.LocalDevUserProperties;
import com.example.governanceportal.user.dto.CurrentUser;

@Service
public class CurrentUserService {

    private final LocalDevUserProperties localDevUserProperties;
    private final PortalPermissionService portalPermissionService;

    public CurrentUserService(
        LocalDevUserProperties localDevUserProperties,
        PortalPermissionService portalPermissionService
    ) {
        this.localDevUserProperties = localDevUserProperties;
        this.portalPermissionService = portalPermissionService;
    }

    public CurrentUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            if (localDevUserProperties.isEnabled()) {
                return new CurrentUser(
                    localDevUserProperties.getUserId(),
                    localDevUserProperties.getDisplayName(),
                    true,
                    localDevUserProperties.getPermissions()
                );
            }

            return new CurrentUser("anonymous", "Anonymous", false, List.of());
        }

        if (authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal principal) {
            return fromSamlPrincipal(authentication, principal);
        }

        return new CurrentUser(
            authentication.getName(),
            authentication.getName(),
            true,
            portalPermissionService.permissions(authentication)
        );
    }

    private CurrentUser fromSamlPrincipal(Authentication authentication, Saml2AuthenticatedPrincipal principal) {
        String userId = firstNonBlank(
            firstAttribute(principal, "uid"),
            firstAttribute(principal, "employeeNumber"),
            firstAttribute(principal, "sAMAccountName"),
            principal.getName(),
            authentication.getName()
        );
        String displayName = firstNonBlank(
            firstAttribute(principal, "displayName"),
            firstAttribute(principal, "cn"),
            firstAttribute(principal, "name"),
            userId
        );
        List<String> permissions = portalPermissionService.permissions(authentication);

        return new CurrentUser(userId, displayName, true, permissions);
    }

    private String firstAttribute(Saml2AuthenticatedPrincipal principal, String name) {
        Object value = principal.getFirstAttribute(name);
        if (value == null) {
            return null;
        }

        String text = String.valueOf(value);
        return text.isBlank() ? null : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
