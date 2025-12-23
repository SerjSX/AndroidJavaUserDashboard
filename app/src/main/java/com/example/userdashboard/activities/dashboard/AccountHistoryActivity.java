package com.example.userdashboard.activities.dashboard;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.userdashboard.R;
import com.example.userdashboard.database.DatabaseHelper;
import com.example.userdashboard.fragments.TopMenu;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccountHistoryActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_history);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();

        dbHelper = new DatabaseHelper(this);
        SharedPreferences data = getSharedPreferences("UserSession", MODE_PRIVATE);
        int currentUserId = data.getInt("user_id", -1);

        Cursor accounts = dbHelper.getAccountsInformation(currentUserId);

        if (accounts != null && accounts.moveToFirst()) {
            do {
                long timestamp = accounts.getLong(accounts.getColumnIndexOrThrow("opening_date"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String date = sdf.format(new Date(timestamp));

                String accountNumber = accounts.getString(accounts.getColumnIndexOrThrow("account_id"));
                String openingBalance = String.format(Locale.getDefault(), "%.2f", accounts.getDouble(accounts.getColumnIndexOrThrow("initial_balance")));
                String closingBalance = String.format(Locale.getDefault(), "%.2f", accounts.getDouble(accounts.getColumnIndexOrThrow("current_balance")));
                String interest = String.format(Locale.getDefault(), "%.2f", accounts.getDouble(accounts.getColumnIndexOrThrow("interest_rate")));

                addRowToTable(date, accountNumber, openingBalance, closingBalance, interest);
            } while (accounts.moveToNext());

            accounts.close();
        }
    }

    private void addRowToTable(String date, String accountNumber, String openingBalance, String closingBalance, String interest) {
        TableLayout tableLayout = findViewById(R.id.tableData);

        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        tableRow.setBackgroundColor(Color.BLACK);

        // Date column
        TextView dateView = createTableCell(date);
        tableRow.addView(dateView);

        // Account number column
        TextView accountView = createTableCell(accountNumber);
        tableRow.addView(accountView);

        // Opening balance column
        TextView obView = createTableCell(openingBalance);
        tableRow.addView(obView);

        // Closing balance column
        TextView cbView = createTableCell(closingBalance);
        tableRow.addView(cbView);

        // Interest column
        TextView interestView = createTableCell(interest);
        tableRow.addView(interestView);

        tableLayout.addView(tableRow);
    }

    private TextView createTableCell(String text) {
        TextView textView = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(1,1,1,1);
        textView.setLayoutParams(params);
        textView.setBackgroundColor(Color.WHITE);
        textView.setPadding(8,8,8,8);
        textView.setText(text);
        textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Medium);

        return textView;
    }
}