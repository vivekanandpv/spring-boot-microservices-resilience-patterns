package com.vivekanandpv.springbootmicroservicesresiliencepatterns.config;

import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpStatusCodeException;

@Configuration
public class RetryConfiguration {
    private final Logger logger;

    public RetryConfiguration() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Bean
    public RetryConfigCustomizer getRetryConfig() {
        //  Retry only for certain errors
        //  https://learn.microsoft.com/en-us/azure/architecture/patterns/retry#when-to-use-this-pattern
        return RetryConfigCustomizer.of("downstream", b -> {
            b.retryOnException(e -> {
                if (e instanceof HttpStatusCodeException) {
                    int statusCode = ((HttpStatusCodeException)e).getStatusCode().value();
                    logger.info(String.format("retryOnException: status code: %d", statusCode));
                    return switch (statusCode) {
                        //  https://stackoverflow.com/a/51770411/3969961
                        case 408, 429, 502, 503, 504 -> true;
                        default -> false;
                    };
                }
                return false;
            }).build();
        });
    }
}
