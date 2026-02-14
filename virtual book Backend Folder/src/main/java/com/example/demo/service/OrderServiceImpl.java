package com.example.demo.service;

import com.example.demo.dto.OrderRequestDTO;
import com.example.demo.entites.Book;
import com.example.demo.entites.Order;
import com.example.demo.entites.OrderItem;
import com.example.demo.entites.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private com.example.demo.repository.SellerEarningRepository sellerEarningRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Order placeOrder(User user, List<OrderItem> items) {
        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        return placeOrder(order);
    }

    @Override
    @Transactional
    public Order placeOrder(Order order) {
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        List<OrderItem> items = order.getItems();
        if (items == null)
            items = new ArrayList<>();

        for (OrderItem item : items) {
            if (item.getBook() == null || item.getBook().getId() == null) {
                throw new RuntimeException("Invalid book reference in order");
            }
            Long bookId = item.getBook().getId();
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            // Inventory Validation
            int stock = book.getQuantity() != null ? book.getQuantity() : 0;
            int requested = item.getQuantity() != null ? item.getQuantity() : 0;

            if (stock < requested) {
                throw new RuntimeException("Insufficient stock for book: " + book.getTitle() +
                        ". Available: " + stock +
                        ", Requested: " + requested);
            }

            // Deduct Stock
            book.setQuantity(stock - requested);
            bookRepository.save(book);

            item.setOrder(order);
            item.setBook(book);
            item.setPrice(book.getPrice() * item.getQuantity());
            orderItems.add(item);

            totalAmount += item.getPrice();
        }

        order.setItems(orderItems);

        // Apply discount if present
        if (order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
            totalAmount -= order.getDiscountAmount();
            if (totalAmount < 0)
                totalAmount = 0.0;
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Calculate Commission and Seller Earnings
        for (OrderItem item : savedOrder.getItems()) {
            User seller = item.getBook().getSeller();
            if (seller != null && "SELLER".equalsIgnoreCase(seller.getRole())) {
                com.example.demo.entites.SellerEarning earning = new com.example.demo.entites.SellerEarning();
                earning.setSeller(seller);
                earning.setOrder(savedOrder);
                earning.setOrderItem(item);

                Double itemTotal = item.getPrice(); // This is already price * qty
                Double commission = itemTotal * 0.02; // 2% commission
                Double netEarning = itemTotal - commission;

                earning.setAmount(itemTotal);
                earning.setCommissionAmount(commission);
                earning.setNetAmount(netEarning);
                earning.setStatus("PENDING");
                earning.setCreatedAt(LocalDateTime.now());

                sellerEarningRepository.save(earning);
            }
        }

        return savedOrder;
    }

    @Override
    @Transactional
    public Order placeOrderByIds(OrderRequestDTO orderRequest) {
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);

        List<OrderItem> orderItems = new ArrayList<>();

        for (Long bookId : orderRequest.getBookIds()) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

            OrderItem item = new OrderItem();
            item.setBook(book);
            item.setQuantity(1);
            item.setPrice(book.getPrice());
            orderItems.add(item);
        }

        order.setItems(orderItems);
        return placeOrder(order);
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
    @Transactional
    public Order cancelOrder(Long orderId, User user) {
        return cancelOrder(orderId, "Cancelled by user");
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new RuntimeException("Order is already cancelled");
        }

        order.setStatus("CANCELLED");
        order.setCancellationReason(reason);

        // Restore Stock
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Book book = item.getBook();
                book.setQuantity(book.getQuantity() + item.getQuantity());
                bookRepository.save(book);
            }
        }

        return orderRepository.save(order);
    }

}