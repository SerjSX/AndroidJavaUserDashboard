package com.example.userdashboard.exceptions;

import org.jetbrains.annotations.NotNull;

public class WrongCredentialsException extends RuntimeException {
    public WrongCredentialsException() {
        super("The email or password you entered is incorrect!");
    }

    @Override
    @NotNull
    public String toString() {
        return "The email or password you entered is incorrect!";
    }
}
