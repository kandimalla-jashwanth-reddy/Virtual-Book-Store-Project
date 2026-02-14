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
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "payment_signature")
    private String paymentSignature;

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