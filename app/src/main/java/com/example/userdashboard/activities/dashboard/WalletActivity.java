package com.example.userdashboard.activities.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.userdashboard.R;
import com.example.userdashboard.database.DatabaseHelper;
import com.example.userdashboard.exceptions.InterestInputException;
import com.example.userdashboard.exceptions.InvalidNumericInputException;
import com.example.userdashboard.fragments.TopMenu;
import com.example.userdashboard.utilities.AlertDialogHelper;

import java.time.LocalDate;

public class WalletActivity extends AppCompatActivity {
    private TextView balanceText, dateText;
    private EditText interestInput;
    private Button interestSaveButton, depositButton, withdrawButton, newAccountButton, viewAccountsButton;
    private DatabaseHelper dbHelper;

    private int currentUserId, currentAccountId;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences data = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = data.getInt("user_id", -1);
        currentAccountId = dbHelper.getActiveAccountId(currentUserId);

        balanceText = findViewById(R.id.balanceText);
        dateText = findViewById(R.id.dateText);
        interestInput = findViewById(R.id.interestInput);
        interestSaveButton = findViewById(R.id.interestSaveButton);
        depositButton = findViewById(R.id.depositButton);
        withdrawButton = findViewById(R.id.withdrawButton);
        newAccountButton = findViewById(R.id.newAccountButton);
        viewAccountsButton = findViewById(R.id.viewAccountsButton);

        refreshAmounts();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();

        viewAccountsButton.setOnClickListener(v -> {
            Intent intent = new Intent(WalletActivity.this, AccountHistoryActivity.class);
            startActivity(intent);
        });

        interestSaveButton.setOnClickListener(v -> {
            try {
                interestSaveButtonClick();
            } catch (InterestInputException e) {
                AlertDialogHelper.showErrorDialog(this, e);
            }
        });

        depositButton.setOnClickListener(v -> showInputDialog("Deposit"));

        withdrawButton.setOnClickListener(v -> showInputDialog("Withdraw"));

        newAccountButton.setOnClickListener(v -> createNewAccountDialog());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void refreshAmounts() {
        balanceText.setText(String.format("$%s", dbHelper.getCurrentBalance(currentAccountId)));
        dateText.setText(LocalDate.now().toString());
        interestInput.setText(String.valueOf(dbHelper.getInterestRate(currentAccountId)));
    }

    private void interestSaveButtonClick() throws InterestInputException {
        double interest = Double.parseDouble(interestInput.getText().toString());
        if (interest >= 0.0) {
            dbHelper.setInterestRate(this.currentAccountId, interest);
            Toast.makeText(this, "Saved interest %!", Toast.LENGTH_SHORT).show();
        } else {
            throw new InterestInputException();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showInputDialog(String methodText) throws InvalidNumericInputException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(methodText);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount");

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        builder.setView(input);

        builder.setPositiveButton(methodText, (dialog, which) -> {
            try {
                String amountStr = input.getText().toString();
                if (!amountStr.isEmpty()) {
                    double amount = Double.parseDouble(amountStr);

                    if (methodText.equals("Deposit")) {
                        dbHelper.deposit(currentAccountId, amount);
                        Toast.makeText(this, "Deposited the amount successfully!", Toast.LENGTH_SHORT).show();
                    } else if (methodText.equals("Withdraw")) {
                        dbHelper.withdraw(currentAccountId, amount);
                        Toast.makeText(this, "Withdrew the amount successfully!", Toast.LENGTH_SHORT).show();
                    }
                    refreshAmounts();
                } else {
                    throw new InvalidNumericInputException();
                }
            } catch (InvalidNumericInputException e) {
                AlertDialogHelper.showErrorDialog(this, e);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNewAccountDialog() throws InvalidNumericInputException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Account");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter initial balance of the account");

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String amountStr = input.getText().toString();

            try {
                if (!amountStr.isEmpty()) {
                    double amount = Double.parseDouble(amountStr);

                    this.currentAccountId = dbHelper.newAccount(this.currentUserId, amount);
                    refreshAmounts();
                    Toast.makeText(this, "Successfully created a new account.", Toast.LENGTH_SHORT).show();
                } else {
                    throw new InvalidNumericInputException();
                }
            } catch (InvalidNumericInputException e) {
                AlertDialogHelper.showErrorDialog(this, e);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }



}