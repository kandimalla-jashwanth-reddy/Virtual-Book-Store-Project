package com.example.demo.controller;

import com.example.demo.dto.BookRequest;
import com.example.demo.entites.Book;
import com.example.demo.entites.User;
import com.example.demo.service.BookService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/seller")
public class BookManagementController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private User getSellerFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing token");
        }
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwtToken); // This method exists now
        return userService.getUserByEmail(username);
    }

    @PostMapping("/books")
    public ResponseEntity<?> addBook(@RequestBody BookRequest bookRequest,
                                     @RequestHeader("Authorization") String token) {
        try {
            User seller = getSellerFromToken(token);

            if (!"SELLER".equals(seller.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only sellers can add books");
                return ResponseEntity.status(403).body(error);
            }

            Book book = new Book();
            book.setTitle(bookRequest.getTitle());
            book.setAuthor(bookRequest.getAuthor());
            book.setPrice(bookRequest.getPrice());
            book.setCategory(bookRequest.getCategory());
            book.setIsbn(bookRequest.getIsbn());
            book.setImageUrl(bookRequest.getImageUrl());
            book.setDescription(bookRequest.getDescription());
            book.setQuantity(bookRequest.getQuantity());
            book.setSeller(seller);

            Book savedBook = bookService.saveBook(book);
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/my-books")
    public ResponseEntity<?> getMyBooks(@RequestHeader("Authorization") String token) {
        try {
            User seller = getSellerFromToken(token);

            if (!"SELLER".equals(seller.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only sellers can view their books");
                return ResponseEntity.status(403).body(error);
            }

            List<Book> books = bookService.getBooksBySeller(seller);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id,
                                        @RequestBody BookRequest bookRequest,
                                        @RequestHeader("Authorization") String token) {
        try {
            User seller = getSellerFromToken(token);

            if (!"SELLER".equals(seller.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only sellers can update books");
                return ResponseEntity.status(403).body(error);
            }

            Book existingBook = bookService.getBookById(id)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            if (!existingBook.getSeller().getId().equals(seller.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You can only update your own books");
                return ResponseEntity.status(403).body(error);
            }

            // Update book fields
            existingBook.setTitle(bookRequest.getTitle());
            existingBook.setAuthor(bookRequest.getAuthor());
            existingBook.setPrice(bookRequest.getPrice());
            existingBook.setCategory(bookRequest.getCategory());
            existingBook.setDescription(bookRequest.getDescription());
            existingBook.setImageUrl(bookRequest.getImageUrl());
            existingBook.setQuantity(bookRequest.getQuantity());

            Book updatedBook = bookService.saveBook(existingBook);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id,
                                        @RequestHeader("Authorization") String token) {
        try {
            User seller = getSellerFromToken(token);

            if (!"SELLER".equals(seller.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only sellers can delete books");
                return ResponseEntity.status(403).body(error);
            }

            Book book = bookService.getBookById(id)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            if (!book.getSeller().getId().equals(seller.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You can only delete your own books");
                return ResponseEntity.status(403).body(error);
            }

            bookService.deleteBook(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Book deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/books/{id}/quantity")
    public ResponseEntity<?> updateQuantity(@PathVariable Long id,
                                            @RequestParam Integer quantity,
                                            @RequestHeader("Authorization") String token) {
        try {
            User seller = getSellerFromToken(token);

            if (!"SELLER".equals(seller.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only sellers can update quantity");
                return ResponseEntity.status(403).body(error);
            }

            Book updatedBook = bookService.updateBookQuantity(id, quantity);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}