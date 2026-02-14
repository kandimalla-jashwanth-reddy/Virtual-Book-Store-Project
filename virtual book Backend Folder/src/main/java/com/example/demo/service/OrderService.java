package com.example.demo.service;

import com.example.demo.dto.OrderRequestDTO;
import com.example.demo.entites.Order;
import com.example.demo.entites.User;
import com.example.demo.entites.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order placeOrder(User user, List<OrderItem> items);

    Order placeOrder(Order order); // New overloaded method

    Order placeOrderByIds(OrderRequestDTO orderRequest);

    List<Order> getOrdersByUser(User user);

    Optional<Order> getOrderById(Long orderId);

    Order cancelOrder(Long orderId, User user);

    Order cancelOrder(Long orderId, String reason); // New overloaded method
}