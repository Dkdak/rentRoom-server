package com.mteam.sleerenthome.exception;

public class InvalidBookingRequestException extends RuntimeException{

    public InvalidBookingRequestException(String message) {
        super(message);
    }
}
