package com.example.userdashboard.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("This email is associated with another account! Try a different one.");
    }

    @Override
    public String toString() {
        return "This email is associated with another account! Try a different one.";
    }
}
