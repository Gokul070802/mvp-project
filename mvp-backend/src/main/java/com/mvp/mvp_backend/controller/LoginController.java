package com.mvp.mvp_backend.controller;

import com.mvp.mvp_backend.model.User;
import com.mvp.mvp_backend.model.dto.AuthResponse;
import com.mvp.mvp_backend.model.dto.LoginRequest;
import com.mvp.mvp_backend.model.dto.RegisterRequest;
import com.mvp.mvp_backend.model.dto.UpdateUserRoleRequest;
import com.mvp.mvp_backend.model.dto.UserDTO;
import com.mvp.mvp_backend.service.UserService;
import com.mvp.mvp_backend.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest loginRequest) {

        User user = userService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    @Operation(summary = "User registration", description = "Register a new user account")
    @PostMapping("/register")
    public UserDTO register(@RequestBody @Valid RegisterRequest registerRequest) {

        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(registerRequest.getPassword());
        newUser.setName(registerRequest.getName());
        newUser.setRole("ROLE_USER");

        User savedUser = userService.register(newUser);

        return new UserDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    @Operation(summary = "List all users", description = "Get list of all users (ADMIN ONLY)", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(u -> new UserDTO(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole()
                ))
                .toList();
    }

    @Operation(summary = "Update user role", description = "Update user role to ROLE_ADMIN or ROLE_USER (ADMIN ONLY)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/users/{userId}/role")
    public UserDTO updateUserRole(@PathVariable Long userId,
                                  @RequestBody @Valid UpdateUserRoleRequest request) {
        User updatedUser = userService.updateUserRole(userId, request.getRole());
        return new UserDTO(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole()
        );
    }

    @Operation(summary = "Delete user", description = "Delete a user by ID (ADMIN ONLY)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/users/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return "User deleted successfully";
    }
}
