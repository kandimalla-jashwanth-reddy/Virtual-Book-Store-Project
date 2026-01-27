package com.example.demo.service;

import com.example.demo.entites.Book;
import com.example.demo.entites.User;
import com.example.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<Book> findByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    @Override
    public List<Book> getBooksBySeller(User seller) {
        return bookRepository.findBySeller(seller);
    }

    @Override
    public List<Book> searchBooks(String query) {
        return bookRepository.searchBooks(query);
    }

    @Override
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public Book updateBookQuantity(Long id, Integer quantity) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        book.setQuantity(quantity);
        return bookRepository.save(book);
    }
}