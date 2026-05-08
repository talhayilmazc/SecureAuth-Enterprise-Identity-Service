package com.secureauth.secureauth.exception;

import org.springframework.http.HttpStatus;

public class TokenException extends BaseException {

    public TokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "TOKEN_ERROR");
    }

    public TokenException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}