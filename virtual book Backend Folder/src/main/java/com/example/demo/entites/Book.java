package com.example.demo.entites;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private Double price;
    private String category;
    private String isbn;
    private String imageUrl;
    private String description;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
}