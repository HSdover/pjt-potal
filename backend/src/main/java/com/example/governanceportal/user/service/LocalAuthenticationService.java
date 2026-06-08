package com.example.governanceportal.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.governanceportal.user.config.LocalDevUserProperties;
import com.example.governanceportal.user.dto.CurrentUser;
import com.example.governanceportal.user.dto.LocalAuthenticatedPrincipal;
import com.example.governanceportal.user.dto.LocalLoginRequest;

@Service
public class LocalAuthenticationService {

    private final LocalDevUserProperties localDevUserProperties;

    public LocalAuthenticationService(LocalDevUserProperties localDevUserProperties) {
        this.localDevUserProperties = localDevUserProperties;
    }

    public CurrentUser login(LocalLoginRequest loginRequest, HttpServletRequest servletRequest) {
        if (!localDevUserProperties.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Local login is disabled.");
        }

        LocalDevUserProperties.LocalUser user = localDevUserProperties.findUser(loginRequest.userId())
            .filter(candidate -> candidate.getPassword() != null && candidate.getPassword().equals(loginRequest.password()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호를 확인하세요."));

        List<String> permissions = List.copyOf(user.getPermissions());
        LocalAuthenticatedPrincipal principal = new LocalAuthenticatedPrincipal(
            user.getUserId(),
            user.getDisplayName(),
            permissions
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        servletRequest.getSession(true)
            .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        return new CurrentUser(principal.userId(), principal.displayName(), true, permissions);
    }

    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
