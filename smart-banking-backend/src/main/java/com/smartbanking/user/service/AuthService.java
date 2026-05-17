package com.smartbanking.user.service;

import com.smartbanking.user.dto.AuthResponse;
import com.smartbanking.user.dto.LoginRequest;
import com.smartbanking.user.dto.RegisterRequest;
import com.smartbanking.user.dto.UserProfileResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getProfile(String email);
}