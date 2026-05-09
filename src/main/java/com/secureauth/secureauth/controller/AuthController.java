package com.secureauth.secureauth.controller;

import com.secureauth.secureauth.dto.request.*;
import com.secureauth.secureauth.dto.response.AuthResponse;
import com.secureauth.secureauth.dto.response.MessageResponse;
import com.secureauth.secureauth.dto.response.TokenValidationResponse;
import com.secureauth.secureauth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Kimlik doğrulama işlemleri")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Kullanıcı kaydı")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request,
                        getClientIp(httpRequest),
                        httpRequest.getHeader("User-Agent")));
    }

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı girişi")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request,
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Token yenileme")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.refreshToken(request,
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")));
    }

    @PostMapping("/logout")
    @Operation(summary = "Oturumu kapat")
    public ResponseEntity<MessageResponse> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.logout(request.getRefreshToken()));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Tüm oturumları kapat")
    public ResponseEntity<MessageResponse> logoutAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.logoutAll(userDetails.getUsername()));
    }

    @GetMapping("/validate")
    @Operation(summary = "Token doğrulama")
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestParam String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Şifre sıfırlama isteği")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Şifre sıfırlama")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Şifre değiştirme")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(
                userDetails.getUsername(), request));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Email doğrulama")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}