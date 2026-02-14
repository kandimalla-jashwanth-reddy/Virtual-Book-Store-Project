package com.example.demo.service;

import com.example.demo.entites.Address;
import com.example.demo.entites.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAddressesByUserId() {
        Long userId = 1L;
        List<Address> addresses = new ArrayList<>();
        addresses.add(new Address());
        when(addressRepository.findByUserId(userId)).thenReturn(addresses);

        List<Address> result = addressService.getAddressesByUserId(userId);

        assertEquals(1, result.size());
        verify(addressRepository, times(1)).findByUserId(userId);
    }

    @Test
    void addAddress() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        Address address = new Address();
        address.setCity("Test City");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        Address result = addressService.addAddress(userId, address);

        assertNotNull(result);
        assertEquals("Test City", result.getCity());
        verify(userRepository, times(1)).findById(userId);
        verify(addressRepository, times(1)).save(address);
    }
}
