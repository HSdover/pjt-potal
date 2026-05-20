package com.example.governanceportal.user.service;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.governanceportal.user.config.LocalDevUserProperties;
import com.example.governanceportal.user.dto.CurrentUser;

@Service
public class CurrentUserService {

    private final LocalDevUserProperties localDevUserProperties;

    public CurrentUserService(LocalDevUserProperties localDevUserProperties) {
        this.localDevUserProperties = localDevUserProperties;
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

        // IAM/KNOX claim-to-permission mapping will replace this once the authority policy is fixed.
        List<String> permissions = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        return new CurrentUser(authentication.getName(), authentication.getName(), true, permissions);
    }
}
