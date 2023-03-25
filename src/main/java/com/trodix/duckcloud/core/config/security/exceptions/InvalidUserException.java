package com.trodix.duckcloud.core.config.security.exceptions;

public class InvalidUserException extends RuntimeException {

    public InvalidUserException(String msg, Exception e) {
        super(msg, e);
    }

    public InvalidUserException(Exception e) {
        super(e);
    }

    public InvalidUserException(String msg) {
        super(msg);
    }

}
