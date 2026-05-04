package com.secureauth.secureauth.domain.enums;

public enum LoginResult {
    SUCCESS,
    FAILED_BAD_CREDENTIALS,
    FAILED_ACCOUNT_LOCKED,
    FAILED_ACCOUNT_DISABLED,
    FAILED_TOO_MANY_ATTEMPTS
}