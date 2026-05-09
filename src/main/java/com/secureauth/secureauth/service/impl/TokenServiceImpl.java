package com.secureauth.secureauth.service.impl;

import com.secureauth.secureauth.domain.entity.RefreshToken;
import com.secureauth.secureauth.domain.entity.User;
import com.secureauth.secureauth.domain.repository.RefreshTokenRepository;
import com.secureauth.secureauth.exception.TokenException;
import com.secureauth.secureauth.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException(
                        "Refresh token bulunamadı", "TOKEN_NOT_FOUND"));

        if (refreshToken.getRevoked()) {
            throw new TokenException("Refresh token iptal edilmiş", "TOKEN_REVOKED");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenException("Refresh token süresi dolmuş", "TOKEN_EXPIRED");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
        log.info("Expired and revoked tokens cleaned up");
    }
}