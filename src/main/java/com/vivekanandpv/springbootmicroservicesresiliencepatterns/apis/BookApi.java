package com.vivekanandpv.springbootmicroservicesresiliencepatterns.apis;

import com.vivekanandpv.springbootmicroservicesresiliencepatterns.models.Book;
import com.vivekanandpv.springbootmicroservicesresiliencepatterns.services.BookService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    public BookApi(RestTemplate restTemplate, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.restTemplate = restTemplate;
        logger = LoggerFactory.getLogger(this.getClass());

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> {
            c.getEventPublisher()
                    .onSuccess(e -> logger.info(String.format("Success: %s", e)))
                    .onError(e -> logger.info(String.format("Error: %s", e)))
                    .onFailureRateExceeded(e -> logger.info(String.format("Failure Rate Exceeded: %s", e)))
                    .onStateTransition(e -> logger.info(String.format("State Transition: %s", e)))
                    .onReset(e -> logger.info(String.format("Reset: %s", e)));
        });
    }


    @CircuitBreaker(name = "downstream", fallbackMethod = "getFallback")
    @GetMapping
    public ResponseEntity<Map<String, String>> get() {
        ResponseEntity<Book> responseEntity = restTemplate.getForEntity("/api/v1/books", Book.class);
        return ResponseEntity.ok(Map.of("book", responseEntity.getBody().toString()));
    }

    public ResponseEntity<Map<String, String>> getFallback(RuntimeException exception) {
        return ResponseEntity.status(503).body(Map.of("upstream-error", exception.getMessage()));
    }
}
