package com.example.demo.controller;

import com.example.demo.entites.Address;
import com.example.demo.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AddressControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();
    }

    @Test
    void getAddressesByUserId() throws Exception {
        when(addressService.getAddressesByUserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/addresses/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void addAddress() throws Exception {
        Address address = new Address();
        address.setCity("Test City");
        when(addressService.addAddress(eq(1L), any(Address.class))).thenReturn(address);

        mockMvc.perform(post("/api/addresses/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk());
    }

    @Test
    void updateAddress() throws Exception {
        Address address = new Address();
        address.setCity("Updated City");
        when(addressService.updateAddress(eq(1L), any(Address.class))).thenReturn(address);

        mockMvc.perform(put("/api/addresses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAddress() throws Exception {
        mockMvc.perform(delete("/api/addresses/1"))
                .andExpect(status().isOk());
    }
}
