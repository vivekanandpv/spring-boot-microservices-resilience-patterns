package com.vivekanandpv.springbootmicroservicesresiliencepatterns.config;

import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {
    private final String upstreamUrl;

    public ApplicationConfiguration(@Value("${app.upstream.url}")String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplateBuilder().rootUri(upstreamUrl).build();
        return restTemplate;
    }
}
