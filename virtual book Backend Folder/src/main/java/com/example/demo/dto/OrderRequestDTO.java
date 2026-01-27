package com.example.demo.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

public class OrderRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Book IDs are required")
    private List<Long> bookIds;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public List<Long> getBookIds() {
        return bookIds;
    }
    public void setBookIds(List<Long> bookIds) {
        this.bookIds = bookIds;
    }
}