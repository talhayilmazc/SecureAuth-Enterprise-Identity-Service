package com.secureauth.secureauth.service;

import com.secureauth.secureauth.dto.request.*;
import com.secureauth.secureauth.dto.response.AuthResponse;
import com.secureauth.secureauth.dto.response.MessageResponse;
import com.secureauth.secureauth.dto.response.TokenValidationResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request, String ipAddress, String userAgent);
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);
    AuthResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent);
    MessageResponse logout(String refreshToken);
    MessageResponse logoutAll(String username);
    TokenValidationResponse validateToken(String token);
    MessageResponse forgotPassword(ForgotPasswordRequest request);
    MessageResponse resetPassword(PasswordResetRequest request);
    MessageResponse changePassword(String username, ChangePasswordRequest request);
    MessageResponse verifyEmail(String token);
}