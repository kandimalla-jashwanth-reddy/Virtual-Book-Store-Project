package com.example.demo.service;

import com.example.demo.entites.Coupon;
import com.example.demo.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public Optional<Coupon> validateCoupon(String code, Double orderAmount) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            // Check expiry
            if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
                throw new RuntimeException("Coupon expired");
            }
            if (coupon.getMinPurchaseAmount() != null && orderAmount < coupon.getMinPurchaseAmount()) {
                throw new RuntimeException("Minimum purchase amount not met for this coupon");
            }
            return Optional.of(coupon);
        }
        return Optional.empty();
    }

    @Override
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }
}
