package com.chernobyl.TicketWizard.exceptions;

public class DuplicatedResourceException extends RuntimeException {
    public DuplicatedResourceException(String message) {
        super(message);
    }
}
