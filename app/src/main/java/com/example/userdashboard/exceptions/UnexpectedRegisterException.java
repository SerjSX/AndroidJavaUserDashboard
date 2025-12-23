package com.example.userdashboard.exceptions;

import org.jetbrains.annotations.NotNull;

public class UnexpectedRegisterException extends RuntimeException {
    public UnexpectedRegisterException() {
        super("An unexpected error occurred when trying to register an account, make sure everything is entered correctly with a unique email address.");
    }

    @Override
    @NotNull
    public String toString() {
        return "An unexpected error occurred when trying to register an account, make sure everything is entered correctly with a unique email address.";
    }
}
