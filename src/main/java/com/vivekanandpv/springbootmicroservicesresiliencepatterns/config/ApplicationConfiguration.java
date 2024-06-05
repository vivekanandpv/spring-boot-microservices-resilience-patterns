package com.vivekanandpv.springbootmicroservicesresiliencepatterns.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {
    private final String upstreamUrl;

    public ApplicationConfiguration(@Value("${app.upstream.url}")String upstreamUrl) {
        this.upstreamUrl = upstreamUrl;
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplateBuilder().rootUri(upstreamUrl).build();
    }
}
