package com.example.demo.data;

import com.example.demo.entites.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev") // Only run in development profile
public class Data {

    @Bean
    CommandLineRunner initData(UserRepository userRepo,
                               BookRepository bookRepo,
                               ReviewRepository reviewRepo,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            // Only insert data if tables are empty
            if (userRepo.count() == 0) {

                // Create sample users
                User buyer = new User();
                buyer.setUsername("john_doe");
                buyer.setEmail("john@example.com");
                buyer.setPassword(passwordEncoder.encode("password123"));
                buyer.setRole("BUYER");
                userRepo.save(buyer);

                User seller = new User();
                seller.setUsername("book_seller");
                seller.setEmail("seller@example.com");
                seller.setPassword(passwordEncoder.encode("password123"));
                seller.setRole("SELLER");
                userRepo.save(seller);

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@bookstore.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                userRepo.save(admin);

                // Create sample books
                Book book1 = new Book();
                book1.setTitle("Clean Code");
                book1.setAuthor("Robert C. Martin");
                book1.setPrice(29.99);
                book1.setCategory("Technology");
                book1.setIsbn("9780132350884");
                book1.setDescription("A Handbook of Agile Software Craftsmanship");
                book1.setImageUrl("https://images-na.ssl-images-amazon.com/images/I/41zoxjP9jpL._SX218_BO1,204,203,200_QL40_FMwebp_.jpg");
                book1.setQuantity(10);
                book1.setSeller(seller);
                bookRepo.save(book1);

                Book book2 = new Book();
                book2.setTitle("Effective Java");
                book2.setAuthor("Joshua Bloch");
                book2.setPrice(34.99);
                book2.setCategory("Technology");
                book2.setIsbn("9780134685991");
                book2.setDescription("Best practices for Java programming");
                book2.setImageUrl("https://images-na.ssl-images-amazon.com/images/I/51p8L1GpKzL._SX218_BO1,204,203,200_QL40_FMwebp_.jpg");
                book2.setQuantity(5);
                book2.setSeller(seller);
                bookRepo.save(book2);

                Book book3 = new Book();
                book3.setTitle("The Great Gatsby");
                book3.setAuthor("F. Scott Fitzgerald");
                book3.setPrice(12.99);
                book3.setCategory("Fiction");
                book3.setIsbn("9780743273565");
                book3.setDescription("A classic novel of the Jazz Age");
                book3.setImageUrl("https://images-na.ssl-images-amazon.com/images/I/51MuZ9PpRgL._SX218_BO1,204,203,200_QL40_FMwebp_.jpg");
                book3.setQuantity(20);
                book3.setSeller(seller);
                bookRepo.save(book3);

                // Add review
                Review review = new Review();
                review.setBook(book1);
                review.setUser(buyer);
                review.setRating(5);
                review.setComment("One of the best books for clean coding!");
                review.setReviewDate(java.time.LocalDate.now());
                reviewRepo.save(review);

                System.out.println("✅ Sample data inserted into PostgreSQL!");
                System.out.println("Buyer: john@example.com / password123");
                System.out.println("Seller: seller@example.com / password123");
                System.out.println("Admin: admin@bookstore.com / admin123");
            }
        };
    }
}