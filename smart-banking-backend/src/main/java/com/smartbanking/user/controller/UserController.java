package com.smartbanking.user.controller;

import com.smartbanking.user.dto.AuthResponse;
import com.smartbanking.user.dto.LoginRequest;
import com.smartbanking.user.dto.RegisterRequest;
import com.smartbanking.user.dto.UserProfileResponse;
import com.smartbanking.user.service.AuthService;
import com.smartbanking.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResponse response = authService.login(request);

        return ResponseEntity
                .ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            Authentication authentication
    ) {

        String email = authentication.getName();

        UserProfileResponse response = authService.getProfile(email);

        return ResponseEntity
                .ok(ApiResponse.success("Profile fetched successfully", response));
    }
}