package com.commerceops.admin.auth.service;

public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException() {
        super("Invalid email or password.");
    }
}
