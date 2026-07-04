package com.chernobyl.TicketWizard.DTOs.userDTOs;

import java.time.LocalDateTime;

import com.chernobyl.TicketWizard.Enums.UserRole;

public record ResponseUserDTO(
        Long id,
        String name,
        String email,
        String phone,
        UserRole role,
        LocalDateTime createdAt) {

}
