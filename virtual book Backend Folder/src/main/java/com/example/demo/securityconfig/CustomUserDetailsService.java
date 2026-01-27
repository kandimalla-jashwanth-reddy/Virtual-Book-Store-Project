package com.example.demo.securityconfig;

import com.example.demo.entites.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // Support login with either email OR username.
        User user = userRepository.findByEmail(login)
                .orElseGet(() -> userRepository.findByUsername(login).orElse(null));

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + login);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole()) // -> ROLE_BUYER / ROLE_SELLER / ROLE_ADMIN
                .build();
    }
}
