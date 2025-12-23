package com.example.userdashboard.utilities;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class AlertDialogHelper {

    public static void showErrorDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void showErrorDialog(Context context, Exception exception) {
        showErrorDialog(context, "Error", exception.toString());
    }

    public static void showMessageDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void showMessageDialog(Context context, String message) {
        showMessageDialog(context, "Message", message);
    }

    public static void showConfirmDialog(Context context, String title, String message,
                                         Runnable onPositive, Runnable onNegative) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Yes", (dialog, which) -> onPositive.run())
                .setNegativeButton("No", (dialog, which) -> onNegative.run())
                .show();
    }
}
