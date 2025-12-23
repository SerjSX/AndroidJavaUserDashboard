package com.example.userdashboard.exceptions;

import org.jetbrains.annotations.NotNull;

public class NoGenderSelectedException extends RuntimeException {
    public NoGenderSelectedException() {
        super("You need to select a gender!");
    }

    @Override
    @NotNull
    public String toString() {
        return "You need to select a gender!";
    }
}
