package com.example.demo.controller;

import com.example.demo.entites.Book;
import com.example.demo.entites.Review;
import com.example.demo.entites.User;
import com.example.demo.service.BookService;
import com.example.demo.service.ReviewService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private JwtUtil jwtUtil;

    private User getUserFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing token");
        }
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwtToken);
        return userService.getUserByEmail(username);
    }

    @PostMapping("/{bookId}/add")
    public ResponseEntity<?> addReview(@PathVariable Long bookId,
            @Valid @RequestBody Review review,
            @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            Book book = bookService.getBookById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            if (review.getRating() < 1 || review.getRating() > 5) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Rating must be between 1 and 5");
                return ResponseEntity.badRequest().body(error);
            }

            Review savedReview = reviewService.addReview(user, book, review);
            return ResponseEntity.ok(savedReview);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long bookId) {
        try {
            List<Review> reviews = reviewService.getReviewsForBook(bookId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/average/{bookId}")
    public ResponseEntity<?> getAverageRating(@PathVariable Long bookId) {
        try {
            Double averageRating = reviewService.getAverageRatingForBook(bookId);
            Map<String, Object> response = new HashMap<>();
            response.put("averageRating", averageRating);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}