package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @PostMapping("/manual-upi")
    public ResponseEntity<?> recordManualPayment(@RequestBody Map<String, Object> data) {
        return ResponseEntity.ok(Map.of("message", "Payment recorded, pending verification"));
    }

    /**
     * Stub for Razorpay order creation. Replace with real Razorpay API when keys are configured.
     * Frontend expects: { id, amount } with amount in paise (INR).
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        Object amountObj = body.get("amount");
        double amountDollars = amountObj instanceof Number ? ((Number) amountObj).doubleValue() : 0.0;
        long amountPaise = Math.round(amountDollars * 100); // assume amount is in INR; if already paise, adjust
        if (amountPaise < 100) amountPaise = 100;
        String orderId = "order_stub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return ResponseEntity.ok(Map.of(
            "id", orderId,
            "amount", amountPaise
        ));
    }

    /**
     * Stub for Razorpay payment verification. Replace with real signature verification when using Razorpay.
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of("verified", true, "message", "Payment verified"));
    }
}
