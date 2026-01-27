package com.example.demo.service;

import com.example.demo.entites.Book;
import com.example.demo.entites.Review;
import com.example.demo.entites.User;
import com.example.demo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public Review addReview(User user, Book book, Review review) {
        review.setUser(user);
        review.setBook(book);
        review.setReviewDate(LocalDate.now());
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsForBook(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Override
    public Double getAverageRatingForBook(Long bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }
}