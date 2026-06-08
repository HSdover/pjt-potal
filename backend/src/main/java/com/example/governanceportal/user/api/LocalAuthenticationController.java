package com.example.governanceportal.user.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.governanceportal.user.dto.CurrentUser;
import com.example.governanceportal.user.dto.LocalLoginRequest;
import com.example.governanceportal.user.service.LocalAuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class LocalAuthenticationController {

    private final LocalAuthenticationService localAuthenticationService;

    public LocalAuthenticationController(LocalAuthenticationService localAuthenticationService) {
        this.localAuthenticationService = localAuthenticationService;
    }

    @PostMapping("/login")
    CurrentUser login(@Valid @RequestBody LocalLoginRequest request, HttpServletRequest servletRequest) {
        return localAuthenticationService.login(request, servletRequest);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        localAuthenticationService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
