package com.example.demo.controller;

import com.example.demo.entites.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/bank-details")
    public ResponseEntity<?> updateBankDetails(@PathVariable Long id,
            @RequestBody java.util.Map<String, String> details) {
        try {
            userService.updateBankDetails(id, details.get("accountNumber"), details.get("ifscCode"),
                    details.get("bankName"));
            return ResponseEntity.ok("Bank details updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating bank details: " + e.getMessage());
        }
    }
}