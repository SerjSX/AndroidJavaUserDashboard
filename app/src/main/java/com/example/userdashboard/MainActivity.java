package com.example.userdashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.userdashboard.activities.RegisterActivity;
import com.example.userdashboard.activities.dashboard.DashboardActivity;
import com.example.userdashboard.database.DatabaseHelper;
import com.example.userdashboard.exceptions.InvalidEmailException;
import com.example.userdashboard.exceptions.NoInputException;
import com.example.userdashboard.exceptions.WrongCredentialsException;
import com.example.userdashboard.fragments.TopMenu;
import com.example.userdashboard.utilities.AlertDialogHelper;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();

        Button registerButton = findViewById(R.id.mainRegisterButton);
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);

            startActivity(intent);
        });


        Button mainLoginButton = findViewById(R.id.mainLoginButton);

        mainLoginButton.setOnClickListener(v -> {
            try {
                loginClick();
            } catch (NoInputException | WrongCredentialsException | InvalidEmailException e ) {
                AlertDialogHelper.showErrorDialog(this, e);
            }
        });
    }

    private void loginClick() throws NoInputException, WrongCredentialsException, InvalidEmailException{
        EditText loginEmail = findViewById(R.id.loginEmail);
        EditText loginPassword = findViewById(R.id.loginPassword);

        String email = String.valueOf(loginEmail.getText());
        String password = String.valueOf(loginPassword.getText());

        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            throw new NoInputException();
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new InvalidEmailException();
        }

        int userId = dbHelper.loginUser(email, password);

        if (userId > 0) {
            SharedPreferences data = getSharedPreferences("UserSession", MODE_PRIVATE);
            data.edit().putInt("user_id", userId).apply();

            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
        } else {
            throw new WrongCredentialsException();
        }
    }
}