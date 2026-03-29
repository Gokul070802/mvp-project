package com.mvp.mvp_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateTokenAndValidate_shouldReturnTrueForValidToken() {
        String email = "test@example.com";

        String token = jwtUtil.generateToken(email);

        assertNotNull(token, "Token should not be null");
        assertEquals(email, jwtUtil.extractEmail(token));
        assertTrue(jwtUtil.validateToken(token, email));
    }
}
