package com.example.demo.entites;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_earnings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerEarning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    private Double amount;
    private Double commissionAmount;
    private Double netAmount;

    private String status;
    private LocalDateTime createdAt;
}
