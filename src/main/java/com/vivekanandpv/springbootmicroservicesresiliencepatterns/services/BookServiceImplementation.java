package com.vivekanandpv.springbootmicroservicesresiliencepatterns.services;

import com.vivekanandpv.springbootmicroservicesresiliencepatterns.models.Book;
import io.github.resilience4j.core.functions.CheckedFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
public class BookServiceImplementation implements BookService {
    private final RestTemplate restTemplate;
    private final Logger logger;

    public BookServiceImplementation(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public Book getBook(String url) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.of(5, ChronoUnit.SECONDS))
                .build();

        RetryRegistry registry = RetryRegistry.of(config);

        Retry retry = registry.retry("downstream");

        Retry.EventPublisher eventPublisher = retry.getEventPublisher();
        eventPublisher.onRetry(e -> logger.info(String.format("Operation failed, retrying: %s", e)));
        eventPublisher.onError(e -> logger.info(String.format("Operation error: %s", e)));
        eventPublisher.onSuccess(e -> logger.info(String.format("Operation succeeded: %s", e)));


        Supplier<ResponseEntity<Book>> responseEntitySupplier = Retry.decorateSupplier(retry, this::getFromService);


        return responseEntitySupplier.get().getBody();
    }

    private ResponseEntity<Book> getFromService() {
        return restTemplate.getForEntity("/api/v1/books", Book.class);
    }
}
