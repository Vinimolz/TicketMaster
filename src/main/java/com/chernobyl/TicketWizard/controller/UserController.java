package com.chernobyl.TicketWizard.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chernobyl.TicketWizard.DTOs.userDTOs.ResponseUserDTO;
import com.chernobyl.TicketWizard.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<List<ResponseUserDTO>> fetchAll() {
        return ResponseEntity.ok(userService.fetchAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseUserDTO> fetchById(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(userService.fetchById(userId));
    }
}
