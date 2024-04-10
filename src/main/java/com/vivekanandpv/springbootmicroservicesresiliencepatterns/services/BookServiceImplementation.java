package com.vivekanandpv.springbootmicroservicesresiliencepatterns.services;

import com.vivekanandpv.springbootmicroservicesresiliencepatterns.models.Book;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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


        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindow(10, 3, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.of(15, ChronoUnit.SECONDS))
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        CircuitBreaker circuitBreaker = registry.circuitBreaker("downstream");

        CircuitBreaker.EventPublisher eventPublisher = circuitBreaker.getEventPublisher();
        eventPublisher.onStateTransition(e -> logger.info(String.format("Circuit Breaker: state transition: %s", e)));
        eventPublisher.onError(e -> logger.info(String.format("Circuit Breaker: operation error: %s", e)));
        eventPublisher.onSuccess(e -> logger.info(String.format("Circuit Breaker: operation succeeded: %s", e)));
        eventPublisher.onReset(e -> logger.info(String.format("Circuit Breaker: reset: %s", e)));
        eventPublisher.onFailureRateExceeded(e -> logger.info(String.format("Circuit Breaker: failure rate exceeded: %s", e)));


        Supplier<ResponseEntity<Book>> responseEntitySupplier = CircuitBreaker.decorateSupplier(circuitBreaker, this::getFromService);


        return responseEntitySupplier.get().getBody();
    }

    private ResponseEntity<Book> getFromService() {
        return restTemplate.getForEntity("/api/v1/books", Book.class);
    }
}
