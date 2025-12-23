package com.example.userdashboard.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;


import androidx.annotation.RequiresApi;

import com.example.userdashboard.exceptions.EmailAlreadyExistsException;
import com.example.userdashboard.exceptions.InvalidNumericInputException;
import com.example.userdashboard.exceptions.NoInputException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

/**
 * USE SEPARATE DATABASEHELPER FOR THE OTHER TABLES, BUT USING THE SAME DATABASE NAME
 * AND CALL FROM ACTIVITY THE HELPER NEEDED
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDashboardDB";
    private static final int DATABASE_VERSION = 1;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating user table
        String createUserTable = "CREATE TABLE IF NOT EXISTS " + DatabaseTables.Users.USER_TABLE_NAME + " (" +
                DatabaseTables.Users.USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DatabaseTables.Users.USER_NAME + " VARCHAR(30), " +
                DatabaseTables.Users.USER_EMAIL + " VARCHAR(50) UNIQUE, " +
                DatabaseTables.Users.USER_PASSWORD + " VARCHAR(255), " +
                DatabaseTables.Users.USER_GENDER + " CHAR(1), " +
                DatabaseTables.Users.USER_DISTRICT + " VARCHAR(50), " +
                DatabaseTables.Users.USER_CITY + " VARCHAR(50), " +
                DatabaseTables.Users.USER_STREET + " VARCHAR(50), " +
                DatabaseTables.Users.USER_BUILDING + " VARCHAR(50), " +
                DatabaseTables.Users.USER_DOB + " DATE)";

        // Create Accounts table
        String createAccountTable = "CREATE TABLE IF NOT EXISTS " + DatabaseTables.Account.ACCOUNT_TABLE_NAME + " (" +
                DatabaseTables.Account.ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseTables.Account.ACCOUNT_USER_ID + " INTEGER, " +
                DatabaseTables.Account.ACCOUNT_OPENING_DATE + " DATE, " +
                DatabaseTables.Account.ACCOUNT_INITIAL_BALANCE + " REAL, " +
                DatabaseTables.Account.ACCOUNT_INTEREST_PERCENTAGE + " REAL, " +
                DatabaseTables.Account.ACCOUNT_STATUS + " VARCHAR(20), " +
                "FOREIGN KEY(" + DatabaseTables.Account.ACCOUNT_USER_ID + ") REFERENCES " +
                DatabaseTables.Users.USER_TABLE_NAME + "(" + DatabaseTables.Users.USER_ID + "))";

        // Create Transactions table
        String createTransactionTable = "CREATE TABLE IF NOT EXISTS " + DatabaseTables.Transactions.TRANSACTION_TABLE_NAME + " (" +
                DatabaseTables.Transactions.TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseTables.Transactions.TRANSACTION_ACCOUNT_ID + " INTEGER, " +
                DatabaseTables.Transactions.TRANSACTION_DATE + " DATETIME, " +
                DatabaseTables.Transactions.TRANSACTION_TYPE + " VARCHAR(20), " +
                DatabaseTables.Transactions.TRANSACTION_AMOUNT + " REAL, " +
                DatabaseTables.Transactions.TRANSACTION_BALANCE_BEFORE + " REAL, " +
                DatabaseTables.Transactions.TRANSACTION_BALANCE_AFTER + " REAL, " +
                "FOREIGN KEY(" + DatabaseTables.Transactions.TRANSACTION_ACCOUNT_ID + ") REFERENCES " +
                DatabaseTables.Account.ACCOUNT_TABLE_NAME + "(" + DatabaseTables.Account.ACCOUNT_ID + "))";

        // Creates the notes table
        String createNotesTable = "CREATE TABLE IF NOT EXISTS " + DatabaseTables.Notes.NOTE_TABLE_NAME + " (" +
                DatabaseTables.Notes.NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseTables.Notes.NOTE_USER_ID + " INTEGER, " +
                DatabaseTables.Notes.NOTE_CREATED_DATE + " INTEGER, " +
                DatabaseTables.Notes.NOTE_MODIFIED_DATE + " INTEGER, " +
                DatabaseTables.Notes.NOTE_TITLE + " TEXT, " +
                DatabaseTables.Notes.NOTE_BODY + " TEXT, " +
                DatabaseTables.Notes.NOTE_IS_DELETED + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + DatabaseTables.Notes.NOTE_USER_ID + ") REFERENCES " +
                DatabaseTables.Users.USER_TABLE_NAME + "(" + DatabaseTables.Users.USER_ID + "))";

        db.execSQL(createUserTable);
        db.execSQL(createAccountTable);
        db.execSQL(createTransactionTable);
        db.execSQL(createNotesTable);
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.Users.USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.Account.ACCOUNT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.Transactions.TRANSACTION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.Notes.NOTE_TABLE_NAME);
        onCreate(db);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.Users.USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.Account.ACCOUNT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.Transactions.TRANSACTION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.Notes.NOTE_TABLE_NAME);
        onCreate(db);
    }

    // BELOW ARE USER RELATED OPERATIONS WITH THE USER
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean registerUser(String username, String email, String password, String gender, String district, String city, String street, String building, String dateOfBirth) throws EmailAlreadyExistsException, NoInputException {
        if (username.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty() || gender.trim().isEmpty() || 
                district.trim().isEmpty() || city.trim().isEmpty() || street.trim().isEmpty() || building.trim().isEmpty() || 
                dateOfBirth.trim().isEmpty()) {
            throw new NoInputException();
        }

        Cursor cursor = getUserByEmail(email);

        // If email exists cant create another one
        if (cursor.getCount() > 0) {
            cursor.close();
            throw new EmailAlreadyExistsException();
        }

        cursor.close();

        // Parse and format date consistently
        String[] dateSplit = dateOfBirth.split("/");
        int day = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        int year = Integer.parseInt(dateSplit[2]);

        LocalDate date = LocalDate.of(year, month, day);
        String formattedDate = date.toString(); // YYYY-MM-DD

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String hashedPassword = hashPassword(password);

        values.put(DatabaseTables.Users.USER_NAME, username);
        values.put(DatabaseTables.Users.USER_EMAIL, email);
        values.put(DatabaseTables.Users.USER_PASSWORD, hashedPassword);
        values.put(DatabaseTables.Users.USER_GENDER, (byte) gender.charAt(0));
        values.put(DatabaseTables.Users.USER_DISTRICT, district);
        values.put(DatabaseTables.Users.USER_CITY, city);
        values.put(DatabaseTables.Users.USER_STREET, street);
        values.put(DatabaseTables.Users.USER_BUILDING, building);
        values.put(DatabaseTables.Users.USER_DOB, formattedDate);

        long result = db.insert(DatabaseTables.Users.USER_TABLE_NAME, null, values);
        return result != -1; // true if successful
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " +  DatabaseTables.Users.USER_TABLE_NAME + " WHERE " + DatabaseTables.Users.USER_EMAIL + " = ?",
                new String[]{email});
    }

    public Cursor getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " +  DatabaseTables.Users.USER_TABLE_NAME + " WHERE " + DatabaseTables.Users.USER_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int loginUser(String email, String password) {
        Cursor user = getUserByEmail(email);

        if (user.getCount() < 1) {
            user.close();
            return -1;
        }
        
        user.moveToFirst();
        int userId = user.getInt(user.getColumnIndexOrThrow(DatabaseTables.Users.USER_ID));
        String storedPassword = user.getString(user.getColumnIndexOrThrow(DatabaseTables.Users.USER_PASSWORD));
        user.close();

        if (checkPassword(password, storedPassword)) {
            return userId;
        }

        return -1;
    }

    public Cursor getUserProfile(int id) {
        Cursor cursor = getUserById(id);

        if (cursor != null && cursor.moveToFirst()) {
            return cursor;
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean updateUserProfile(int userId, String username, String email, String password, String gender, String district, String city, String street, String building, String dateOfBirth) throws EmailAlreadyExistsException, NoInputException {
        if (username.trim().isEmpty() || email.trim().isEmpty() || gender.trim().isEmpty() ||
                district.trim().isEmpty() || city.trim().isEmpty() || street.trim().isEmpty() || building.trim().isEmpty() ||
                dateOfBirth.trim().isEmpty()) {
            throw new NoInputException();
        }

        Cursor user = getUserById(userId);
        user.moveToFirst();
        String currentEmail = user.getString(user.getColumnIndexOrThrow("email"));

        if (!currentEmail.equals(email)) {
            Cursor anotherUserWithSameEmailCheck = getUserByEmail(email);

            // If email exists cant create another one. Replace with exception later
            if (anotherUserWithSameEmailCheck.getCount() > 0) {
                anotherUserWithSameEmailCheck.close();
                throw new EmailAlreadyExistsException();
            }

            anotherUserWithSameEmailCheck.close();
        }

        String hashedPassword;

        if (!password.trim().isEmpty()) {
            hashedPassword = hashPassword(password);
        } else {
            hashedPassword = user.getString(user.getColumnIndexOrThrow("password"));

            user.close();
        }


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String[] dateSplit = dateOfBirth.split("/");
        int year = Integer.parseInt(dateSplit[2]);
        int month = Integer.parseInt(dateSplit[1]);
        int day = Integer.parseInt(dateSplit[0]);

        LocalDate date = LocalDate.of(year, month, day);
        String formattedDate = date.toString();

        values.put(DatabaseTables.Users.USER_NAME, username);
        values.put(DatabaseTables.Users.USER_EMAIL, email);
        values.put(DatabaseTables.Users.USER_PASSWORD, hashedPassword);
        values.put(DatabaseTables.Users.USER_GENDER, (byte) gender.charAt(0));
        values.put(DatabaseTables.Users.USER_DISTRICT, district);
        values.put(DatabaseTables.Users.USER_CITY, city);
        values.put(DatabaseTables.Users.USER_STREET, street);
        values.put(DatabaseTables.Users.USER_BUILDING, building);
        values.put(DatabaseTables.Users.USER_DOB, formattedDate);

        int result = db.update(DatabaseTables.Users.USER_TABLE_NAME, values, DatabaseTables.Users.USER_EMAIL + " = ?", new String[]{currentEmail});
        return result > 0; // true if successful
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkPassword(String inputPassword, String storedHashedPassword) {
        String hashedUserPassword = hashPassword(inputPassword);

        return hashedUserPassword.equals(storedHashedPassword);
    }


    // BELOW ARE WALLET RELATED FUNCTIONALITIES
    private void interact(int accountId, double amount, boolean isDeposit) throws InvalidNumericInputException {
        SQLiteDatabase db = this.getWritableDatabase();

        double currentBalance = getCurrentBalance(accountId);

        if (amount <= 0) {
            throw new InvalidNumericInputException();
        }

        String transactionType;
        double newBalance;
        if (isDeposit) {
            newBalance = currentBalance + amount;
            transactionType = "deposit";
        } else {
            newBalance = currentBalance - amount;
            if (newBalance < 0) {
                newBalance = 0;
            }

            transactionType = "withdraw";
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseTables.Transactions.TRANSACTION_ACCOUNT_ID, accountId);
        values.put(DatabaseTables.Transactions.TRANSACTION_DATE, System.currentTimeMillis());
        values.put(DatabaseTables.Transactions.TRANSACTION_TYPE, transactionType);
        values.put(DatabaseTables.Transactions.TRANSACTION_AMOUNT, amount);
        values.put(DatabaseTables.Transactions.TRANSACTION_BALANCE_BEFORE, currentBalance);
        values.put(DatabaseTables.Transactions.TRANSACTION_BALANCE_AFTER, newBalance);

        db.insert(DatabaseTables.Transactions.TRANSACTION_TABLE_NAME, null, values);
    }

    public void deposit(int accountId, double amount) throws InvalidNumericInputException {
        interact(accountId, amount, true);
    }

    public void withdraw(int accountId, double amount) throws InvalidNumericInputException {
        interact(accountId, amount, false);
    }
    public int newAccount(int userId, double initialBalance) {
        deactivateActiveAccount(userId);

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseTables.Account.ACCOUNT_USER_ID, userId);
        values.put(DatabaseTables.Account.ACCOUNT_OPENING_DATE, System.currentTimeMillis());
        values.put(DatabaseTables.Account.ACCOUNT_INITIAL_BALANCE, initialBalance);
        values.put(DatabaseTables.Account.ACCOUNT_INTEREST_PERCENTAGE, 0);
        values.put(DatabaseTables.Account.ACCOUNT_STATUS, "active");

        // The row id is the same as the autoincremented primary key = account id. So we return it.
        long newAccountId = db.insert(DatabaseTables.Account.ACCOUNT_TABLE_NAME, null, values);
        return (int) newAccountId;
    }

    public int getActiveAccountId(int userId) {
        SQLiteDatabase readableDb = this.getReadableDatabase();

        Cursor activeAccount = readableDb.rawQuery(
                "SELECT " +
                        DatabaseTables.Account.ACCOUNT_ID + " AS account_id "  +
                        "FROM " + DatabaseTables.Account.ACCOUNT_TABLE_NAME + " " +
                        "WHERE " + DatabaseTables.Account.ACCOUNT_USER_ID + " = ? " +
                        "AND " + DatabaseTables.Account.ACCOUNT_STATUS + " = 'active' " +
                        "ORDER BY " + DatabaseTables.Account.ACCOUNT_OPENING_DATE + " DESC LIMIT 1",
                new String[]{String.valueOf(userId)}
        );

        int accountId = -1;
        if (activeAccount.moveToFirst()) {
            accountId =  activeAccount.getInt(activeAccount.getColumnIndexOrThrow("account_id"));
        }

        activeAccount.close();

        if (accountId == -1) {
            return newAccount(userId, 0);
        }

        return accountId;
    }

    private void deactivateActiveAccount(int userId) {
        SQLiteDatabase readableDb = this.getReadableDatabase();

        Cursor activeAccount = readableDb.rawQuery(
                "SELECT " + DatabaseTables.Account.ACCOUNT_ID +
                        " FROM " + DatabaseTables.Account.ACCOUNT_TABLE_NAME +
                        " WHERE " + DatabaseTables.Account.ACCOUNT_USER_ID + " = ? " +
                        "AND " + DatabaseTables.Account.ACCOUNT_STATUS + " = 'active'",
                new String[]{String.valueOf(userId)}
        );

        if (activeAccount.moveToFirst()) {
            int accountId = activeAccount.getInt(0);
            activeAccount.close();

            SQLiteDatabase writableDb = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseTables.Account.ACCOUNT_STATUS, "inactive");

            writableDb.update(DatabaseTables.Account.ACCOUNT_TABLE_NAME, values, DatabaseTables.Account.ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)});
        } else {
            activeAccount.close();
        }
    }

    public Cursor getAccountsInformation(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT " +
                        "a." + DatabaseTables.Account.ACCOUNT_OPENING_DATE + " AS opening_date, " +
                        "a." + DatabaseTables.Account.ACCOUNT_ID + " AS account_id, "  +
                        "a." + DatabaseTables.Account.ACCOUNT_INITIAL_BALANCE + " AS initial_balance, " +
                        // In case there is no transaction in an account, we use the initial balance.
                        "COALESCE(t." + DatabaseTables.Transactions.TRANSACTION_BALANCE_AFTER + ", a." + DatabaseTables.Account.ACCOUNT_INITIAL_BALANCE + ") AS current_balance, " +
                        "a." + DatabaseTables.Account.ACCOUNT_INTEREST_PERCENTAGE + " AS interest_rate " +
                        "FROM " + DatabaseTables.Account.ACCOUNT_TABLE_NAME + " a " +
                        "LEFT JOIN " + DatabaseTables.Transactions.TRANSACTION_TABLE_NAME + " t " +
                        "ON a." + DatabaseTables.Account.ACCOUNT_ID + " = t." + DatabaseTables.Transactions.TRANSACTION_ACCOUNT_ID + " " +
                        "AND t." + DatabaseTables.Transactions.TRANSACTION_ID + " = (" +
                        "SELECT " + DatabaseTables.Transactions.TRANSACTION_ID + " " +
                        "FROM " + DatabaseTables.Transactions.TRANSACTION_TABLE_NAME + " " +
                        "WHERE " + DatabaseTables.Transactions.TRANSACTION_ACCOUNT_ID + " = a." + DatabaseTables.Account.ACCOUNT_ID + " " +
                        "ORDER BY " + DatabaseTables.Transactions.TRANSACTION_DATE + " DESC LIMIT 1) " +
                        "WHERE a." + DatabaseTables.Account.ACCOUNT_USER_ID + " = ? " +
                        " ORDER BY a." + DatabaseTables.Account.ACCOUNT_OPENING_DATE + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public double getCurrentBalance(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseTables.Transactions.TRANSACTION_BALANCE_AFTER +
                        " FROM " + DatabaseTables.Transactions.TRANSACTION_TABLE_NAME +
                        " WHERE " + DatabaseTables.Transactions.TRANSACTION_ACCOUNT_ID + " = ?" +
                        " ORDER BY " + DatabaseTables.Transactions.TRANSACTION_ID + " DESC LIMIT 1",
                new String[]{String.valueOf(accountId)}
        );

        double balance = 0;
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(0);
        } else {
            balance = getInitialBalance(accountId);
        }

        cursor.close();
        return balance;
    }

    public double getInitialBalance(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseTables.Account.ACCOUNT_INITIAL_BALANCE +
                        " FROM " + DatabaseTables.Account.ACCOUNT_TABLE_NAME +
                        " WHERE " + DatabaseTables.Account.ACCOUNT_ID + " = ?",
                new String[]{String.valueOf(accountId)}
        );

        double balance = 0;
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(0);
        }

        cursor.close();
        return balance;
    }

    public double getInterestRate(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + DatabaseTables.Account.ACCOUNT_INTEREST_PERCENTAGE +
                        " FROM " + DatabaseTables.Account.ACCOUNT_TABLE_NAME +
                        " WHERE " + DatabaseTables.Account.ACCOUNT_ID + " = ?",
                new String[]{String.valueOf(accountId)}
        );

        double rate = 0.00;
        if (cursor.moveToFirst()) {
            rate = cursor.getDouble(0);
        }

        cursor.close();
        return rate;
    }

    public void setInterestRate(int accountId, double interestRate) {
        SQLiteDatabase writableDb = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseTables.Account.ACCOUNT_INTEREST_PERCENTAGE, interestRate);

        writableDb.update(DatabaseTables.Account.ACCOUNT_TABLE_NAME, values, DatabaseTables.Account.ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)});
    }


    // BELOW ARE NOTES RELATED FUNCTIONALITIES
    public int addNote(int userId, String title, String body) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        long currentTime = System.currentTimeMillis();

        values.put(DatabaseTables.Notes.NOTE_USER_ID, userId);
        values.put(DatabaseTables.Notes.NOTE_CREATED_DATE, currentTime);
        values.put(DatabaseTables.Notes.NOTE_MODIFIED_DATE, currentTime);
        values.put(DatabaseTables.Notes.NOTE_TITLE, title);
        values.put(DatabaseTables.Notes.NOTE_BODY, body);
        values.put(DatabaseTables.Notes.NOTE_IS_DELETED, 0);

        return (int) db.insert(DatabaseTables.Notes.NOTE_TABLE_NAME, null, values);
    }

    public Cursor getUserNotes(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + DatabaseTables.Notes.NOTE_TABLE_NAME +
                        " WHERE " + DatabaseTables.Notes.NOTE_USER_ID + " = ? " +
                        "AND " + DatabaseTables.Notes.NOTE_IS_DELETED + " = 0 " +
                        "ORDER BY " + DatabaseTables.Notes.NOTE_MODIFIED_DATE + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getUserNote(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + DatabaseTables.Notes.NOTE_TABLE_NAME +
                        " WHERE " + DatabaseTables.Notes.NOTE_ID + " = ? ",
                new String[]{String.valueOf(noteId)}
        );
    }

    public Cursor getDeletedNotes(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + DatabaseTables.Notes.NOTE_TABLE_NAME +
                        " WHERE " + DatabaseTables.Notes.NOTE_USER_ID + " = ? " +
                        "AND " + DatabaseTables.Notes.NOTE_IS_DELETED + " = 1 " +
                        "ORDER BY " + DatabaseTables.Notes.NOTE_MODIFIED_DATE + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean updateNote(int noteId, String title, String body) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseTables.Notes.NOTE_TITLE, title);
        values.put(DatabaseTables.Notes.NOTE_BODY, body);
        values.put(DatabaseTables.Notes.NOTE_MODIFIED_DATE, System.currentTimeMillis());

        int result = db.update(DatabaseTables.Notes.NOTE_TABLE_NAME, values,
                DatabaseTables.Notes.NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});

        return result > 0;
    }

    public boolean deleteNote(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseTables.Notes.NOTE_IS_DELETED, 1);
        values.put(DatabaseTables.Notes.NOTE_MODIFIED_DATE, System.currentTimeMillis());

        int result = db.update(DatabaseTables.Notes.NOTE_TABLE_NAME, values,
                DatabaseTables.Notes.NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});

        return result > 0;
    }

    public boolean restoreNote(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseTables.Notes.NOTE_IS_DELETED, 0);
        values.put(DatabaseTables.Notes.NOTE_MODIFIED_DATE, System.currentTimeMillis());

        int result = db.update(DatabaseTables.Notes.NOTE_TABLE_NAME, values,
                DatabaseTables.Notes.NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});

        return result > 0;
    }

    public boolean permanentlyDeleteNote(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(DatabaseTables.Notes.NOTE_TABLE_NAME,
                DatabaseTables.Notes.NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});

        return result > 0;
    }


}
