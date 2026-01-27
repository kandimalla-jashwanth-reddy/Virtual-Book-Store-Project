package com.example.demo.entites;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (items != null) {
            this.totalAmount = items.stream()
                    .mapToDouble(OrderItem::getTotalPrice)
                    .sum();
        } else {
            this.totalAmount = 0.0;
        }
    }
}