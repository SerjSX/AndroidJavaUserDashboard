package com.example.userdashboard.exceptions;

import org.jetbrains.annotations.NotNull;

public class InterestInputException extends RuntimeException {
    public InterestInputException() {
        super("Interest must be greater than or equal to 0%!");
    }

    @Override
    @NotNull
    public String toString() {
        return "Interest must be greater than or equal to 0%!";
    }
}
