package com.example.demo.service;

import com.example.demo.entites.Book;
import com.example.demo.entites.Review;
import com.example.demo.entites.User;

import java.util.List;

public interface ReviewService {
    Review addReview(User user, Book book, Review review);
    List<Review> getReviewsForBook(Long bookId);
    Double getAverageRatingForBook(Long bookId);
}