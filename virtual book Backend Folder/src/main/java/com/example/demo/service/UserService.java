package com.example.demo.service;

import com.example.demo.entites.User;

import java.util.Optional;

public interface UserService {
    User getUserById(Long id);
    User getUserByEmail(String email);
    Optional<User> findByUsername(String username);
}