package com.kt.cdp.dto.user;

public record StaffDto(
        Long id,
        String staffNumber,
        String fullName,
        String rankTitle,
        String contact,
        boolean active
) {}
