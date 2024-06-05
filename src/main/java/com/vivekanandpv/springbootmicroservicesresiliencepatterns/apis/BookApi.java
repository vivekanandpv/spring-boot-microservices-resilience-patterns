package com.vivekanandpv.springbootmicroservicesresiliencepatterns.apis;

import com.vivekanandpv.springbootmicroservicesresiliencepatterns.models.Book;
import com.vivekanandpv.springbootmicroservicesresiliencepatterns.services.BookService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("api/v1/books")
public class BookApi {
    private final RestTemplate restTemplate;
    private final Logger logger;

    public BookApi(RestTemplate restTemplate, RetryRegistry retryRegistry) {
        this.restTemplate = restTemplate;
        logger = LoggerFactory.getLogger(this.getClass());

        //  logging the retry events
        retryRegistry.getAllRetries().forEach(r -> {
            r.getEventPublisher()
                    .onRetry(e -> logger.info(String.format("Retry: %s", e)))
                    .onError(e -> logger.info(String.format("Error: %s", e)))
                    .onSuccess(e -> logger.info(String.format("Success: %s", e)));
        });
    }


    @Retry(name = "downstream", fallbackMethod = "getFallback")
    @GetMapping
    public ResponseEntity<Map<String, String>> get() {
        ResponseEntity<Book> responseEntity = restTemplate.getForEntity("/api/v1/books", Book.class);
        return ResponseEntity.ok(Map.of("book", responseEntity.getBody().toString()));
    }

    public ResponseEntity<Map<String, String>> getFallback(RuntimeException exception) {
        return ResponseEntity.status(503).body(Map.of("upstream-error", exception.getMessage()));
    }
}
