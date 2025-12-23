package com.example.userdashboard.exceptions;

import org.jetbrains.annotations.NotNull;

public class NoInputValueWalletException extends RuntimeException {
    public NoInputValueWalletException() {
        super("You need to enter an amount first!");
    }

    @Override
    @NotNull
    public String toString() {
        return "You need to enter an amount first!";
    }
}
