package com.mteam.sleerenthome.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message)  {
        super(message);
    }
}
