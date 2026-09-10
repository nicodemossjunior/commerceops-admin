package com.commerceops.admin.common.error;

public class RequestValidationException extends RuntimeException {

    public RequestValidationException(String message) {
        super(message);
    }
}
