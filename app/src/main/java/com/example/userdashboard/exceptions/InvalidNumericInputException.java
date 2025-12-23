package com.example.userdashboard.exceptions;

import org.jetbrains.annotations.NotNull;

public class InvalidNumericInputException extends RuntimeException {
    public InvalidNumericInputException() {
        super("The number(s) entered must be greater than 0 to proceed.");
    }

    @Override
    @NotNull
    public String toString() {
        return "The number(s) entered must be greater than 0 to proceed.";
    }
}
