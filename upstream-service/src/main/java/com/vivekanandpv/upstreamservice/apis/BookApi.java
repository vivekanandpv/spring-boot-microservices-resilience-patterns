package com.vivekanandpv.upstreamservice.apis;

import com.vivekanandpv.upstreamservice.models.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/books")
public class BookApi {
    @GetMapping
    public ResponseEntity<?> get() {
        //  408, 429, 502, 503, 504 are retried
        //  other error codes are not
        //  return ResponseEntity.status(501).build();
        return ResponseEntity.ok(new Book(14, "Learning Spring", 800, 1024));
    }
}
