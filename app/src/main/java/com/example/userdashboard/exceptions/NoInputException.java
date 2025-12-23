package com.example.userdashboard.exceptions;

import androidx.annotation.NonNull;

public class NoInputException extends RuntimeException {
    public NoInputException() {
        super("You have to enter all values!");
    }

    @NonNull
    @Override
    public String toString() {
        return "You have to enter all values!";
    }
}
