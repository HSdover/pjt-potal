package com.example.governanceportal.config;

import com.example.governanceportal.common.external.ExternalApiLoggingInterceptor;
import com.example.governanceportal.common.external.ExternalApiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ExternalApiProperties.class)
public class ApiClientConfig {

    @Bean
    RestClient restClient(
        RestClient.Builder builder,
        ExternalApiProperties properties,
        ExternalApiLoggingInterceptor loggingInterceptor
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(toMillis(properties.getConnectTimeout()));
        requestFactory.setReadTimeout(toMillis(properties.getReadTimeout()));

        return builder
            .requestFactory(requestFactory)
            .requestInterceptor(loggingInterceptor)
            .build();
    }

    private int toMillis(java.time.Duration duration) {
        long millis = duration.toMillis();
        return millis > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) millis;
    }
}
