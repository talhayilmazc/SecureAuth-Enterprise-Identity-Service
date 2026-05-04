package com.secureauth.secureauth.domain.enums;

public enum AuditAction {
    LOGIN,
    LOGOUT,
    REGISTER,
    PASSWORD_CHANGE,
    PASSWORD_RESET_REQUEST,
    PASSWORD_RESET_COMPLETE,
    EMAIL_VERIFICATION,
    TOKEN_REFRESH,
    TOKEN_REVOKE,
    ACCOUNT_LOCK,
    ACCOUNT_UNLOCK,
    ROLE_CHANGE,
    PROFILE_UPDATE
}