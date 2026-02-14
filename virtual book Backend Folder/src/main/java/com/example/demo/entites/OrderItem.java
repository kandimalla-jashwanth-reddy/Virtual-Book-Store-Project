package com.example.demo.entites;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;


    public OrderItem() {
        this.quantity = 1;
    }

    public OrderItem(Book book, Integer quantity) {
        this.book = book;
        this.quantity = quantity;
        this.price = book.getPrice() * quantity;
    }

    // Getters and Setters
    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Order getOrder() {

        return order;
    }

    public void setOrder(Order order) {

        this.order = order;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
        if (book != null && price == null) {
            this.price = book.getPrice() * (quantity != null ? quantity : 1);
        }
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (book != null && quantity != null) {
            this.price = book.getPrice() * quantity;
        }
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getTotalPrice() {
        if (price != null) {
            return price;
        }
        if (book != null && quantity != null) {
            return book.getPrice() * quantity;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}