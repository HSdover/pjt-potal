package com.example.governanceportal.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

class SpaCsrfTokenRequestHandlerTest {

    private final SpaCsrfTokenRequestHandler handler = new SpaCsrfTokenRequestHandler();

    @Test
    void resolvesHeaderTokenAsPlainValue() {
        CsrfToken csrfToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "raw-token");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-XSRF-TOKEN", "raw-token");

        String resolved = handler.resolveCsrfTokenValue(request, csrfToken);

        assertThat(resolved).isEqualTo("raw-token");
    }

    @Test
    void handleForcesTokenLoadingForCookieGeneration() {
        CsrfToken csrfToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "raw-token");
        AtomicBoolean loaded = new AtomicBoolean(false);

        handler.handle(
            new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            () -> {
                loaded.set(true);
                return csrfToken;
            }
        );

        assertThat(loaded).isTrue();
    }
}
