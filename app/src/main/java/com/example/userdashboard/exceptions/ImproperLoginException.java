package com.example.userdashboard.exceptions;

import org.jetbrains.annotations.NotNull;

public class ImproperLoginException extends RuntimeException {
    public ImproperLoginException() {
        super("Are you sure you're properly logged in? Logout and try again.");
    }

    @Override
    @NotNull
    public String toString() {
        return "Are you sure you're properly logged in? Logout and try again.";
    }
}
