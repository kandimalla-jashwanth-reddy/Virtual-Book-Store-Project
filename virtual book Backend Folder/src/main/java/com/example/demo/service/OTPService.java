package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OTPService {

    // In-memory storage for OTPs: Email -> OTP
    // In production, use Redis or Database with expiration
    private final Map<String, String> otpStorage = new HashMap<>();

    public String generateOTP(String email) {
        // Generate a 6-digit random number
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        return otp;
    }

    public boolean validateOTP(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        return storedOtp != null && storedOtp.equals(otp);
    }

    public void clearOTP(String email) {
        otpStorage.remove(email);
    }
}
