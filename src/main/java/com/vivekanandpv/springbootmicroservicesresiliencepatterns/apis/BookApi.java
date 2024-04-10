package com.vivekanandpv.springbootmicroservicesresiliencepatterns.apis;

import com.vivekanandpv.springbootmicroservicesresiliencepatterns.models.Book;
import com.vivekanandpv.springbootmicroservicesresiliencepatterns.services.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("api/v1/books")
public class BookApi {
    private final BookService bookService;

    public BookApi(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> get() {
        return ResponseEntity.ok(Map.of("book", bookService.getBook("/api/v1/books").toString()));
    }

    public ResponseEntity<Map<String, String>> getFallback(RuntimeException exception) {
        return ResponseEntity.ok(Map.of("upstream-error", exception.getMessage()));
    }
}
