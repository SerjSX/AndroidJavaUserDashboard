package com.example.userdashboard.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.userdashboard.R;
import com.example.userdashboard.database.DatabaseHelper;
import com.example.userdashboard.exceptions.EmailAlreadyExistsException;
import com.example.userdashboard.exceptions.InvalidEmailException;
import com.example.userdashboard.exceptions.NoGenderSelectedException;
import com.example.userdashboard.exceptions.NoInputException;
import com.example.userdashboard.exceptions.UnexpectedRegisterException;
import com.example.userdashboard.fragments.TopMenu;
import com.example.userdashboard.utilities.AlertDialogHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private String chosenDistrict;
    private String chosenCity;

    private EditText registerUsername;
    private EditText registerEmail;
    private EditText registerPassword;

    private RadioGroup registerGenderGroup;

    private EditText registerStreet;
    private EditText registerBuilding;
    private DatePicker registerDateOfBirth;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        registerUsername = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);

        registerGenderGroup = findViewById(R.id.registerGenderGroup);

        registerStreet = findViewById(R.id.registerStreet);
        registerBuilding = findViewById(R.id.registerBuilding);
        registerDateOfBirth = findViewById(R.id.registerDateOfBirth);

        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();

        ListView districtList = findViewById(R.id.registerDistrictList);
        ListView citiesList = findViewById(R.id.registerCityList);
        chosenDistrict = "";
        chosenCity = "";

        ArrayList<String> districtNames = new ArrayList<>();
        HashMap<String, ArrayList<String>> districtData = new HashMap<>();

        try {
            JSONArray jsonArray = new JSONArray(loadLocationData());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String districtName = obj.getString("district");
                districtNames.add(districtName);

                JSONArray citiesArray = obj.getJSONArray("cities");
                ArrayList<String> cityNames = new ArrayList<>();

                for (int j = 0; j < citiesArray.length(); j++) {
                    cityNames.add(citiesArray.getString(j));
                }

                districtData.put(districtName, cityNames);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, districtNames
        );
        districtList.setAdapter(districtAdapter);

        districtList.setOnItemClickListener((parent, view, position, id) -> {
            chosenDistrict = districtNames.get(position);

            ArrayList<String> cities = districtData.get(chosenDistrict);

            assert cities != null;
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, cities
            );
            citiesList.setAdapter(cityAdapter);

            citiesList.setOnItemClickListener((parentTwo, viewTwo, positionTwo, idTwo) -> {
                chosenCity = cities.get(positionTwo);

                Toast.makeText(this, "You chose the district " + chosenDistrict + " and the city " + chosenCity + ".", Toast.LENGTH_LONG).show();
            });
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(l -> {
            try {
                registerClick();
            } catch (NoInputException | InvalidEmailException | NoGenderSelectedException |
                     UnexpectedRegisterException | EmailAlreadyExistsException e) {
                AlertDialogHelper.showErrorDialog(this, e);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void registerClick() throws NoInputException, InvalidEmailException, NoGenderSelectedException, UnexpectedRegisterException, EmailAlreadyExistsException {
        String username = registerUsername.getText().toString();
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();

        String street = registerStreet.getText().toString();
        String building = registerBuilding.getText().toString();

        int day = registerDateOfBirth.getDayOfMonth();
        int month = registerDateOfBirth.getMonth() + 1;
        int year = registerDateOfBirth.getYear();
        String dateOfBirth = day + "/" + month + "/" + year;

        if (username.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty() || chosenDistrict.trim().isEmpty() || chosenCity.trim().isEmpty() || street.trim().isEmpty() || building.trim().isEmpty() || dateOfBirth.trim().isEmpty()) {
            throw new NoInputException();
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new InvalidEmailException();
        }

        int selectedGenderId = registerGenderGroup.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            throw new NoGenderSelectedException();
        }

        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        boolean result = dbHelper.registerUser(username, email, password, gender, chosenDistrict, chosenCity, street, building, dateOfBirth);

        if (result) {
            Toast.makeText(this, "You successfully registered, you can now login!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            throw new UnexpectedRegisterException();
        }
    }

    public String loadLocationData() {
        String json = null;
        try {
            InputStream is = getAssets().open("location_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException e) {
            AlertDialogHelper.showErrorDialog(this, "Error!", "Failed to show the location related data (district and cities), make sure the location_data.json file exists in assets.");
        }

        return json;
    }
}