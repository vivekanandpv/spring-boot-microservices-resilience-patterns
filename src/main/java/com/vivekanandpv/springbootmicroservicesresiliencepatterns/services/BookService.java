package com.vivekanandpv.springbootmicroservicesresiliencepatterns.services;

import com.vivekanandpv.springbootmicroservicesresiliencepatterns.models.Book;

public interface BookService {
    Book getBook(String url);
}
