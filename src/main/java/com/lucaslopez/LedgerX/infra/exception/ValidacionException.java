package com.lucaslopez.LedgerX.infra.exception;

public class ValidacionException extends RuntimeException {
    public ValidacionException(String message) {
        super(message);
    }
}
