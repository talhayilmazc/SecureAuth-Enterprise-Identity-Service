package com.secureauth.secureauth.service;

import com.secureauth.secureauth.domain.entity.RefreshToken;
import com.secureauth.secureauth.domain.entity.User;

public interface TokenService {
    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);
    RefreshToken validateRefreshToken(String token);
    void revokeRefreshToken(String token);
    void revokeAllUserTokens(Long userId);
    void cleanupExpiredTokens();
}