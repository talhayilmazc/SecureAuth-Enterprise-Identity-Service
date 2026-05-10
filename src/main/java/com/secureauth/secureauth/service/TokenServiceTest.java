package com.secureauth.secureauth.service;

import com.secureauth.secureauth.domain.entity.RefreshToken;
import com.secureauth.secureauth.domain.entity.User;
import com.secureauth.secureauth.domain.repository.RefreshTokenRepository;
import com.secureauth.secureauth.exception.TokenException;
import com.secureauth.secureauth.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@DisplayName("Token Service Tests")
class TokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User testUser;
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "refreshExpiration", 604800000L);

        testUser = User.builder()
                .username("talha")
                .email("talha@test.com")
                .build();

        testToken = RefreshToken.builder()
                .token("valid-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
    }

    @Test
    @DisplayName("Refresh token başarıyla oluşturulmalı")
    void createRefreshToken_ShouldCreateToken_WhenValidUser() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(testToken);

        RefreshToken result = tokenService.createRefreshToken(
                testUser, "127.0.0.1", "TestAgent");

        assertThat(result).isNotNull();
        assertThat(result.getRevoked()).isFalse();
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Geçerli token doğrulanabilmeli")
    void validateRefreshToken_ShouldReturnToken_WhenValid() {
        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(testToken));

        RefreshToken result = tokenService.validateRefreshToken("valid-token");

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("valid-token");
    }

    @Test
    @DisplayName("İptal edilmiş token reddedilmeli")
    void validateRefreshToken_ShouldThrowException_WhenRevoked() {
        testToken.setRevoked(true);
        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(testToken));

        assertThatThrownBy(() -> tokenService.validateRefreshToken("valid-token"))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("iptal");
    }

    @Test
    @DisplayName("Süresi dolmuş token reddedilmeli")
    void validateRefreshToken_ShouldThrowException_WhenExpired() {
        testToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(testToken));

        assertThatThrownBy(() -> tokenService.validateRefreshToken("valid-token"))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("dolmuş");
    }

    @Test
    @DisplayName("Token başarıyla iptal edilmeli")
    void revokeRefreshToken_ShouldRevokeToken_WhenExists() {
        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(testToken));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenReturn(testToken);

        tokenService.revokeRefreshToken("valid-token");

        verify(refreshTokenRepository, times(1))
                .save(argThat(RefreshToken::getRevoked));
    }
}