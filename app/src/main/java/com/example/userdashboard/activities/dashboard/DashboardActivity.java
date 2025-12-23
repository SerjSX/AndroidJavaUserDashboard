package com.example.userdashboard.activities.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.userdashboard.R;
import com.example.userdashboard.exceptions.ImproperLoginException;
import com.example.userdashboard.exceptions.InvalidEmailException;
import com.example.userdashboard.exceptions.NoGenderSelectedException;
import com.example.userdashboard.exceptions.NoInputException;
import com.example.userdashboard.exceptions.UnexpectedRegisterException;
import com.example.userdashboard.fragments.TopMenu;
import com.example.userdashboard.utilities.AlertDialogHelper;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        Button walletButton = findViewById(R.id.walletButton);
        walletButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, WalletActivity.class);
            startActivity(intent);
        });

        Button diaryButton = findViewById(R.id.diaryButton);
        diaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, DiaryActivity.class);
            startActivity(intent);
        });

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            AlertDialogHelper.showConfirmDialog(this,
                    "Logout",
                    "Are you sure you want to logout?",
                    () -> finish(),
                    () -> {}
            );
        });
    }
}