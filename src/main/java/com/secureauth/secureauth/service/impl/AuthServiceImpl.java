package com.secureauth.secureauth.service.impl;

import com.secureauth.secureauth.domain.entity.LoginAttempt;
import com.secureauth.secureauth.domain.entity.RefreshToken;
import com.secureauth.secureauth.domain.entity.User;
import com.secureauth.secureauth.domain.enums.AuditAction;
import com.secureauth.secureauth.domain.enums.LoginResult;
import com.secureauth.secureauth.domain.enums.Role;
import com.secureauth.secureauth.domain.repository.LoginAttemptRepository;
import com.secureauth.secureauth.domain.repository.UserRepository;
import com.secureauth.secureauth.dto.request.*;
import com.secureauth.secureauth.dto.response.AuthResponse;
import com.secureauth.secureauth.dto.response.MessageResponse;
import com.secureauth.secureauth.dto.response.TokenValidationResponse;
import com.secureauth.secureauth.exception.AccountLockedException;
import com.secureauth.secureauth.exception.AuthException;
import com.secureauth.secureauth.exception.BusinessException;
import com.secureauth.secureauth.exception.ResourceNotFoundException;
import com.secureauth.secureauth.security.JwtTokenProvider;
import com.secureauth.secureauth.security.UserPrincipal;
import com.secureauth.secureauth.service.AuditLogService;
import com.secureauth.secureauth.service.AuthService;
import com.secureauth.secureauth.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final AuditLogService auditLogService;

    @Value("${app.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration-minutes}")
    private int lockoutDurationMinutes;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request,
                                  String ipAddress, String userAgent) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    "Bu kullanıcı adı zaten kullanılıyor", "USERNAME_EXISTS");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "Bu email adresi zaten kayıtlı", "EMAIL_EXISTS");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of(Role.ROLE_USER))
                .active(true)
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = tokenService.createRefreshToken(
                user, ipAddress, userAgent);

        auditLogService.log(user.getUsername(), AuditAction.REGISTER,
                "AUTH", ipAddress, userAgent, "User registered", true);

        log.info("User registered: {}", request.getUsername());

        return buildAuthResponse(accessToken, refreshToken.getToken(),
                user, authentication);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request,
                               String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user != null && user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            saveLoginAttempt(request.getUsername(), ipAddress, userAgent,
                    LoginResult.FAILED_ACCOUNT_LOCKED, "Account is locked");
            throw new AccountLockedException(
                    "Hesabınız kilitlenmiştir. Lütfen daha sonra tekrar deneyin.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));

            if (user != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                user.setLastLoginAt(LocalDateTime.now());
                user.setLastLoginIp(ipAddress);
                userRepository.save(user);
            }

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            RefreshToken refreshToken = tokenService.createRefreshToken(
                    user, ipAddress, userAgent);

            saveLoginAttempt(request.getUsername(), ipAddress, userAgent,
                    LoginResult.SUCCESS, null);

            auditLogService.log(request.getUsername(), AuditAction.LOGIN,
                    "AUTH", ipAddress, userAgent, "Login successful", true);

            log.info("User logged in: {}", request.getUsername());

            return buildAuthResponse(accessToken, refreshToken.getToken(),
                    user, authentication);

        } catch (BadCredentialsException ex) {
            if (user != null) {
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= maxFailedAttempts) {
                    user.setLockedUntil(
                            LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
                    log.warn("Account locked due to failed attempts: {}",
                            request.getUsername());
                }
                userRepository.save(user);
            }
            saveLoginAttempt(request.getUsername(), ipAddress, userAgent,
                    LoginResult.FAILED_BAD_CREDENTIALS, "Invalid credentials");
            auditLogService.log(request.getUsername(), AuditAction.LOGIN,
                    "AUTH", ipAddress, userAgent, "Login failed", false);
            throw new AuthException("Kullanıcı adı veya şifre hatalı",
                    "INVALID_CREDENTIALS");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request,
                                      String ipAddress, String userAgent) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(
                request.getRefreshToken());
        User user = refreshToken.getUser();

        tokenService.revokeRefreshToken(request.getRefreshToken());
        RefreshToken newRefreshToken = tokenService.createRefreshToken(
                user, ipAddress, userAgent);

        String roles = user.getRoles().stream()
                .map(Enum::name).collect(Collectors.joining(","));
        String accessToken = jwtTokenProvider.generateAccessTokenFromUsername(
                user.getUsername(), roles);

        auditLogService.log(user.getUsername(), AuditAction.TOKEN_REFRESH,
                "AUTH", ipAddress, userAgent, "Token refreshed", true);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .build();
    }

    @Override
    @Transactional
    public MessageResponse logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
        return MessageResponse.success("Başarıyla çıkış yapıldı");
    }

    @Override
    @Transactional
    public MessageResponse logoutAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı: " + username));
        tokenService.revokeAllUserTokens(user.getId());
        auditLogService.log(username, AuditAction.LOGOUT,
                "AUTH", null, null, "All sessions logged out", true);
        return MessageResponse.success("Tüm oturumlardan çıkış yapıldı");
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return TokenValidationResponse.builder().valid(false).build();
        }
        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return TokenValidationResponse.builder().valid(false).build();
        }
        List<String> roles = user.getRoles().stream()
                .map(Enum::name).collect(Collectors.toList());
        return TokenValidationResponse.builder()
                .valid(true)
                .username(username)
                .roles(roles)
                .expiresAt(System.currentTimeMillis() + jwtExpiration)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            user.setPasswordResetToken(UUID.randomUUID().toString());
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            auditLogService.log(user.getUsername(),
                    AuditAction.PASSWORD_RESET_REQUEST, "AUTH",
                    null, null, "Password reset requested", true);
        });
        return MessageResponse.success(
                "Şifre sıfırlama bağlantısı email adresinize gönderildi");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new AuthException(
                        "Geçersiz şifre sıfırlama token'ı", "INVALID_RESET_TOKEN"));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AuthException(
                    "Şifre sıfırlama token'ı süresi dolmuş", "RESET_TOKEN_EXPIRED");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        tokenService.revokeAllUserTokens(user.getId());

        auditLogService.log(user.getUsername(),
                AuditAction.PASSWORD_RESET_COMPLETE, "AUTH",
                null, null, "Password reset completed", true);

        return MessageResponse.success("Şifreniz başarıyla sıfırlandı");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(String username,
                                           ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthException("Mevcut şifre hatalı", "WRONG_CURRENT_PASSWORD");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenService.revokeAllUserTokens(user.getId());

        auditLogService.log(username, AuditAction.PASSWORD_CHANGE,
                "AUTH", null, null, "Password changed", true);

        return MessageResponse.success("Şifreniz başarıyla değiştirildi");
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new AuthException(
                        "Geçersiz doğrulama token'ı", "INVALID_VERIFICATION_TOKEN"));
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        auditLogService.log(user.getUsername(), AuditAction.EMAIL_VERIFICATION,
                "AUTH", null, null, "Email verified", true);
        return MessageResponse.success("Email adresiniz başarıyla doğrulandı");
    }

    private void saveLoginAttempt(String username, String ipAddress,
                                   String userAgent, LoginResult result,
                                   String failureReason) {
        LoginAttempt attempt = LoginAttempt.builder()
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .result(result)
                .failureReason(failureReason)
                .build();
        loginAttemptRepository.save(attempt);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken,
                                            User user, Authentication authentication) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .build();
    }
}