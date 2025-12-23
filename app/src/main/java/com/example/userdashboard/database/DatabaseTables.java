package com.example.userdashboard.database;

public class DatabaseTables {
    
    public static class Users {
        public static final String USER_TABLE_NAME = "users";
        public static final String USER_ID = "id";
        public static final String USER_NAME = "name";
        public static final String USER_EMAIL = "email";
        public static final String USER_PASSWORD = "password";
        public static final String USER_GENDER = "gender";
        public static final String USER_DISTRICT = "district";
        public static final String USER_CITY = "city";
        public static final String USER_STREET = "street";
        public static final String USER_BUILDING = "building";
        public static final String USER_DOB = "date_of_birth";
    }

    public static class Account {
        public static final String ACCOUNT_TABLE_NAME = "accounts";
        public static final String ACCOUNT_ID = "id";
        public static final String ACCOUNT_USER_ID = "user_id";
        public static final String ACCOUNT_OPENING_DATE = "opening_date";
        public static final String ACCOUNT_INITIAL_BALANCE = "initial_balance";
        public static final String ACCOUNT_INTEREST_PERCENTAGE = "interest_percentage";
        public static final String ACCOUNT_STATUS = "status";
    }

    public static class Transactions {
        public static final String TRANSACTION_TABLE_NAME = "transactions";
        public static final String TRANSACTION_ID = "id";
        public static final String TRANSACTION_ACCOUNT_ID = "account_id";
        public static final String TRANSACTION_DATE = "date";
        public static final String TRANSACTION_TYPE = "type";
        public static final String TRANSACTION_AMOUNT = "amount";
        public static final String TRANSACTION_BALANCE_BEFORE =  "balance_before";
        public static final String TRANSACTION_BALANCE_AFTER = "balance_after";
    }

    public static class Notes {
        public static final String NOTE_TABLE_NAME = "notes";
        public static final String NOTE_ID = "id";
        public static final String NOTE_USER_ID = "user_id";
        public static final String NOTE_CREATED_DATE = "created_date";
        public static final String NOTE_MODIFIED_DATE = "modified_date";
        public static final String NOTE_TITLE = "title";
        public static final String NOTE_BODY = "body";
        public static final String NOTE_IS_DELETED = "is_deleted";
    }

}
