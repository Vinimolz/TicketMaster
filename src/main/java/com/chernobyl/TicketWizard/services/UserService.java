package com.chernobyl.TicketWizard.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.chernobyl.TicketWizard.DTOs.userDTOs.ResponseUserDTO;
import com.chernobyl.TicketWizard.exceptions.ResourceNotFoundException;
import com.chernobyl.TicketWizard.model.User;
import com.chernobyl.TicketWizard.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<ResponseUserDTO> fetchAll() {
        return listToDto(userRepository.findAll());
    }

    public ResponseUserDTO fetchById(Long userId) {
        return userToDto(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with id: " + userId)));
    }

    private ResponseUserDTO userToDto(User user) {
        return new ResponseUserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(),
                user.getCreatedAt());
    }

    private List<ResponseUserDTO> listToDto(List<User> users) {
        return users.stream().map(this::userToDto).collect(Collectors.toList());
    }
}
