package com.example.userdashboard.exceptions;

import org.jetbrains.annotations.NotNull;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException() {
        super("You have to enter a valid email address!");
    }

    @Override
    @NotNull
    public String toString() {
        return "You have to enter a valid email address!";
    }
}
