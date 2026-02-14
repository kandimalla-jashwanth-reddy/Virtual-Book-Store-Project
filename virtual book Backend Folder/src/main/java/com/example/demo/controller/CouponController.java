package com.example.demo.controller;

import com.example.demo.entites.Coupon;
import com.example.demo.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestParam String code, @RequestParam Double amount) {
        try {
            return couponService.validateCoupon(code, amount)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Invalid coupon code");
                        return ResponseEntity.badRequest().body((Coupon) null);
                    });
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        return ResponseEntity.ok(couponService.createCoupon(coupon));
    }
}
