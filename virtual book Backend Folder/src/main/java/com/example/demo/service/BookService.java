package com.example.demo.service;

import com.example.demo.entites.Book;
import com.example.demo.entites.User;

import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> getAllBooks();
    Optional<Book> getBookById(Long id);
    List<Book> findByCategory(String category);
    List<Book> getBooksBySeller(User seller);
    List<Book> searchBooks(String query);
    Book saveBook(Book book);
    void deleteBook(Long id);
    Book updateBookQuantity(Long id, Integer quantity);
}