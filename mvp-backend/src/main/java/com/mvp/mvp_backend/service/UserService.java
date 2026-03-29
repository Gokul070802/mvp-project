package com.mvp.mvp_backend.service;

import com.mvp.mvp_backend.model.User;
import com.mvp.mvp_backend.repository.UserRepository;
import com.mvp.mvp_backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new com.mvp.mvp_backend.exception.BadRequestException("Email already registered");
        }

        if (newUser.getRole() == null || newUser.getRole().isBlank()) {
            newUser.setRole("ROLE_USER");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        return userRepository.save(newUser);
    }

    // LOGIN USER
    public User login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent() &&
                passwordEncoder.matches(password, user.get().getPassword())) {
            return user.get();
        }

        throw new com.mvp.mvp_backend.exception.UnauthorizedException("Invalid email or password");
    }

    // GET ALL USERS
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ADMIN: UPDATE USER ROLE
    public User updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!newRole.equals("ROLE_USER") && !newRole.equals("ROLE_ADMIN")) {
            throw new com.mvp.mvp_backend.exception.BadRequestException("Invalid role");
        }
        
        user.setRole(newRole);
        return userRepository.save(user);
    }

    // ADMIN: DELETE USER
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    // ADMIN: GET USER BY ID
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
