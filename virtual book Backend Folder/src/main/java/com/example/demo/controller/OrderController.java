package com.example.demo.controller;

import com.example.demo.dto.OrderRequestDTO;
import com.example.demo.entites.Order;
import com.example.demo.entites.User;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private User getUserFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing token");
        }
        String jwtToken = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(jwtToken); // Using extractUsername
        return userService.getUserByEmail(username);
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody Order order,
                                        @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            order.setUser(user);
            Order savedOrder = orderService.placeOrder(user, order.getItems());
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/place-by-ids")
    public ResponseEntity<?> placeOrderByIds(@RequestBody OrderRequestDTO orderRequest,
                                             @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);

            if (!user.getId().equals(orderRequest.getUserId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(403).body(error);
            }

            Order order = orderService.placeOrderByIds(orderRequest);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId,
                                             @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);

            if (!user.getId().equals(userId) && !"ADMIN".equals(user.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(403).body(error);
            }

            List<Order> orders = orderService.getOrdersByUser(user);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId,
                                          @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Check authorization
            if (!order.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(403).body(error);
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            Order cancelledOrder = orderService.cancelOrder(orderId, user);
            return ResponseEntity.ok(cancelledOrder);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}