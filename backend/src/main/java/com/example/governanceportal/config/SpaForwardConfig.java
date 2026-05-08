package com.example.governanceportal.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

@Configuration
public class SpaForwardConfig implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(
        HttpServletRequest request,
        HttpStatus status,
        Map<String, Object> model
    ) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        if (status == HttpStatus.NOT_FOUND && isFrontendRoute(path)) {
            return new ModelAndView("forward:/index.html");
        }

        return null;
    }

    private boolean isFrontendRoute(String path) {
        return !path.startsWith("/api")
            && !path.startsWith("/api-docs")
            && !path.startsWith("/swagger-ui")
            && !path.startsWith("/actuator")
            && !path.startsWith("/h2-console")
            && !path.substring(path.lastIndexOf('/') + 1).contains(".");
    }
}
