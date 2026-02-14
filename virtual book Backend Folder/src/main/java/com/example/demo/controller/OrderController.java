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

            Order savedOrder = orderService.placeOrder(order);

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
            orderRequest.setUserId(user.getId()); // Ensure the user from token is used

            Order savedOrder = orderService.placeOrderByIds(orderRequest);

            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return orderService.getOrdersByUser(user);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            // Verify ownership first then cancel
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!order.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
                throw new RuntimeException("Unauthorized");
            }

            String reason = (body != null && body.containsKey("reason")) ? body.get("reason") : "Cancelled by user";
            Order cancelledOrder = orderService.cancelOrder(orderId, reason);

            return ResponseEntity.ok(cancelledOrder);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}