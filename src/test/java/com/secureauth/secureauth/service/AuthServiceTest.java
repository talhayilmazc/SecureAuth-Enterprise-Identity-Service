package com.secureauth.secureauth.service;

import com.secureauth.secureauth.domain.entity.RefreshToken;
import com.secureauth.secureauth.domain.entity.User;
import com.secureauth.secureauth.domain.enums.Role;
import com.secureauth.secureauth.domain.repository.LoginAttemptRepository;
import com.secureauth.secureauth.domain.repository.UserRepository;
import com.secureauth.secureauth.dto.request.LoginRequest;
import com.secureauth.secureauth.dto.request.RegisterRequest;
import com.secureauth.secureauth.dto.response.AuthResponse;
import com.secureauth.secureauth.exception.AccountLockedException;
import com.secureauth.secureauth.exception.AuthException;
import com.secureauth.secureauth.exception.BusinessException;
import com.secureauth.secureauth.security.JwtTokenProvider;
import com.secureauth.secureauth.service.AuditLogService;
import com.secureauth.secureauth.service.TokenService;
import com.secureauth.secureauth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private LoginAttemptRepository loginAttemptRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private TokenService tokenService;
    @Mock private AuditLogService auditLogService;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutDurationMinutes", 30);
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);

        testUser = User.builder()
                .username("talha")
                .password("encodedPassword")
                .email("talha@test.com")
                .firstName("Talha")
                .lastName("Yılmaz")
                .roles(new HashSet<>(Set.of(Role.ROLE_USER)))
                .active(true)
                .emailVerified(false)
                .failedLoginAttempts(0)
                .build();

        registerRequest = RegisterRequest.builder()
                .username("talha")
                .password("Password1!")
                .email("talha@test.com")
                .firstName("Talha")
                .lastName("Yılmaz")
                .build();

        loginRequest = LoginRequest.builder()
                .username("talha")
                .password("Password1!")
                .build();

        testRefreshToken = RefreshToken.builder()
                .token("test-refresh-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(any()))
                .thenReturn("test-access-token");
        when(tokenService.createRefreshToken(any(), any(), any()))
                .thenReturn(testRefreshToken);
        when(loginAttemptRepository.save(any())).thenReturn(null);
        doNothing().when(auditLogService).log(any(), any(), any(),
                any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Başarılı kayıt işlemi")
    void register_ShouldRegisterUser_WhenValidRequest() {
        when(userRepository.existsByUsername("talha")).thenReturn(false);
        when(userRepository.existsByEmail("talha@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.register(
                registerRequest, "127.0.0.1", "TestAgent");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("test-access-token");
        assertThat(response.getUsername()).isEqualTo("talha");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Var olan kullanıcı adıyla kayıt olunamamalı")
    void register_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByUsername("talha")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                registerRequest, "127.0.0.1", "TestAgent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("kullanıcı adı");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Var olan email ile kayıt olunamamalı")
    void register_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByUsername("talha")).thenReturn(false);
        when(userRepository.existsByEmail("talha@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                registerRequest, "127.0.0.1", "TestAgent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Başarılı giriş işlemi")
    void login_ShouldLoginUser_WhenValidCredentials() {
        when(userRepository.findByUsername("talha"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.login(
                loginRequest, "127.0.0.1", "TestAgent");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("test-access-token");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Kilitli hesapla giriş yapılamamalı")
    void login_ShouldThrowException_WhenAccountLocked() {
        testUser.setLockedUntil(LocalDateTime.now().plusHours(1));
        when(userRepository.findByUsername("talha"))
                .thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(
                loginRequest, "127.0.0.1", "TestAgent"))
                .isInstanceOf(AccountLockedException.class);

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Hatalı şifreyle giriş yapılamamalı")
    void login_ShouldThrowException_WhenBadCredentials() {
        when(userRepository.findByUsername("talha"))
                .thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertThatThrownBy(() -> authService.login(
                loginRequest, "127.0.0.1", "TestAgent"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("hatalı");
    }

    @Test
    @DisplayName("Başarısız girişlerde deneme sayısı artmalı")
    void login_ShouldIncrementFailedAttempts_WhenBadCredentials() {
        when(userRepository.findByUsername("talha"))
                .thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertThatThrownBy(() -> authService.login(
                loginRequest, "127.0.0.1", "TestAgent"))
                .isInstanceOf(AuthException.class);

        verify(userRepository, times(1)).save(argThat(u ->
                u.getFailedLoginAttempts() == 1));
    }
}