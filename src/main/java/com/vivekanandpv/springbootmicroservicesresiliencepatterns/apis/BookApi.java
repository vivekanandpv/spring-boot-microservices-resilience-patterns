package com.vivekanandpv.springbootmicroservicesresiliencepatterns.apis;

import com.vivekanandpv.springbootmicroservicesresiliencepatterns.models.Book;
import com.vivekanandpv.springbootmicroservicesresiliencepatterns.services.BookService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    public BookApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @CircuitBreaker(name = "downstream", fallbackMethod = "getFallback")
    @GetMapping
    public ResponseEntity<Map<String, String>> get() {
        ResponseEntity<Book> responseEntity = restTemplate.getForEntity("/api/v1/books", Book.class);
        return ResponseEntity.ok(Map.of("book", responseEntity.getBody().toString()));
    }

    public ResponseEntity<Map<String, String>> getFallback(RuntimeException exception) {
        return ResponseEntity.ok(Map.of("upstream-error", exception.getMessage()));
    }
}
