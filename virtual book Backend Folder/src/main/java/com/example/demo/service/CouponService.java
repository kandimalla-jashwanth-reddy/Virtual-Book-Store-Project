package com.example.demo.service;

import com.example.demo.entites.Coupon;
import java.util.Optional;

public interface CouponService {
    Optional<Coupon> validateCoupon(String code, Double orderAmount);

    Coupon createCoupon(Coupon coupon);
}
