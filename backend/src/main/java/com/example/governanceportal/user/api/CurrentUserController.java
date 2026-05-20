package com.example.governanceportal.user.api;

import com.example.governanceportal.user.dto.CurrentUser;
import com.example.governanceportal.user.service.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class CurrentUserController {

    private final CurrentUserService currentUserService;

    public CurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public CurrentUser me() {
        return currentUserService.current();
    }
}
