package com.mvp.mvp_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvp.mvp_backend.model.User;
import com.mvp.mvp_backend.model.dto.AuthResponse;
import com.mvp.mvp_backend.model.dto.LoginRequest;
import com.mvp.mvp_backend.model.dto.RegisterRequest;
import com.mvp.mvp_backend.repository.TaskRepository;
import com.mvp.mvp_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegisterNewUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Test@123456");
        request.setName("Test User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("test@example.com"));
        assertTrue(response.contains("ROLE_USER"));
    }

    @Test
    void testLoginWithCorrectCredentials() throws Exception {
        // Register first
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setName("Login Test");
        user.setRole("ROLE_USER");
        userRepository.save(user);

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("Password@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("token"));
        assertTrue(response.contains("login@example.com"));

        // Extract token
        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);
        jwtToken = authResponse.getToken();
        assertNotNull(jwtToken);
    }

    @Test
    void testLoginWithWrongPassword() throws Exception {
        User user = new User();
        user.setEmail("wrong@example.com");
        user.setPassword(passwordEncoder.encode("CorrectPassword@123"));
        user.setName("Wrong Pass Test");
        user.setRole("ROLE_USER");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrong@example.com");
        loginRequest.setPassword("WrongPassword@123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

        @Test
        void testGetAllUsersRequiresAuth() throws Exception {
                mockMvc.perform(get("/api/auth/users"))
                                .andExpect(status().isForbidden());
        }

    @Test
    void testGetAllUsersWithValidToken() throws Exception {
        // Create user and login
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword(passwordEncoder.encode("Admin@123"));
        user.setName("Admin");
        user.setRole("ROLE_ADMIN");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("Admin@123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthResponse.class
        );

        // Get all users
        mockMvc.perform(get("/api/auth/users")
                .header("Authorization", "Bearer " + authResponse.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    void testDuplicateEmailRegistration() throws Exception {
        RegisterRequest request1 = new RegisterRequest();
        request1.setEmail("duplicate@example.com");
        request1.setPassword("Pass@123");
        request1.setName("First");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        RegisterRequest request2 = new RegisterRequest();
        request2.setEmail("duplicate@example.com");
        request2.setPassword("Different@123");
        request2.setName("Second");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }
}
