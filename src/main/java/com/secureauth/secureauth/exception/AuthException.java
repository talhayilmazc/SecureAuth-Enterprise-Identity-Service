package com.secureauth.secureauth.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends BaseException {

    public AuthException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTH_ERROR");
    }

    public AuthException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}