package com.mvp.mvp_backend.controller;

import com.mvp.mvp_backend.exception.BadRequestException;
import com.mvp.mvp_backend.exception.ResourceNotFoundException;
import com.mvp.mvp_backend.model.User;
import com.mvp.mvp_backend.model.dto.ProfileDTO;
import com.mvp.mvp_backend.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;

@Tag(name = "Profile", description = "User profile management endpoints")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get user profile", description = "Retrieve the logged-in user's profile information")
    @GetMapping
    public ProfileDTO getProfile(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProfileDTO dto = new ProfileDTO();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setGender(user.getGender());
        dto.setDob(user.getDob());
        dto.setCountry(user.getCountry());
        dto.setProfilePhoto(user.getProfilePhoto());

        return dto;
    }

    @Operation(summary = "Update user profile", description = "Update the logged-in user's profile information")
    @PutMapping
    public ProfileDTO updateProfile(@RequestBody @Valid ProfileDTO profileDTO,
                                    Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (profileDTO.getDob() != null && profileDTO.getDob().isAfter(LocalDate.now())) {
            throw new BadRequestException("DOB cannot be future date");
        }

        user.setFirstName(profileDTO.getFirstName());
        user.setLastName(profileDTO.getLastName());
        user.setGender(profileDTO.getGender());
        user.setDob(profileDTO.getDob());
        user.setCountry(profileDTO.getCountry());
        user.setProfilePhoto(profileDTO.getProfilePhoto());

        userRepository.save(user);

        return profileDTO;
    }
}