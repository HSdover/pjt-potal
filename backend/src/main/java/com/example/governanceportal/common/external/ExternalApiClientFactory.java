package com.example.governanceportal.common.external;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component
public class ExternalApiClientFactory {

    private final RestClient restClient;

    public ExternalApiClientFactory(RestClient restClient) {
        this.restClient = restClient;
    }

    public <T> T createClient(Class<T> clientType) {
        return createClient(clientType, null);
    }

    public <T> T createClient(Class<T> clientType, String baseUrl) {
        RestClient targetRestClient = StringUtils.hasText(baseUrl)
            ? restClient.mutate().baseUrl(baseUrl).build()
            : restClient;

        return HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(targetRestClient))
            .build()
            .createClient(clientType);
    }
}
