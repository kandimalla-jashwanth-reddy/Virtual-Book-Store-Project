package com.example.demo.service;

import com.example.demo.dto.OrderRequestDTO;
import com.example.demo.entites.*;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Order placeOrder(User user, List<OrderItem> items) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        // Associate order with items
        for (OrderItem item : items) {
            item.setOrder(order);
            // Update book quantity
            Book book = item.getBook();
            if (book.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient quantity for book: " + book.getTitle());
            }
            book.setQuantity(book.getQuantity() - item.getQuantity());
            bookRepository.save(book);
        }

        order.setItems(items);
        order.setTotalAmount(calculateTotal(items));

        return orderRepository.save(order);
    }

    @Override
    public Order placeOrderByIds(OrderRequestDTO orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<OrderItem> items = new ArrayList<>();
        for (Long bookId : orderRequest.getBookIds()) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
            OrderItem item = new OrderItem(book, 1); // default quantity 1
            items.add(item);
        }

        return placeOrder(user, items);
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Order cancelOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if the user is the owner or an admin
        if (!order.getUser().getId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("You are not authorized to cancel this order");
        }

        // Check if order can be cancelled
        if (!order.getStatus().equals("PENDING") && !order.getStatus().equals("PROCESSING")) {
            throw new RuntimeException("Order cannot be cancelled in its current state");
        }

        // Restore book quantities
        for (OrderItem item : order.getItems()) {
            Book book = item.getBook();
            book.setQuantity(book.getQuantity() + item.getQuantity());
            bookRepository.save(book);
        }

        order.setStatus("CANCELLED");
        return orderRepository.save(order);
    }

    private Double calculateTotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }
}