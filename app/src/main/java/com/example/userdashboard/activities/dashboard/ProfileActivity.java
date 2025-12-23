package com.example.userdashboard.activities.dashboard;

import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.userdashboard.R;
import com.example.userdashboard.database.DatabaseHelper;
import com.example.userdashboard.exceptions.EmailAlreadyExistsException;
import com.example.userdashboard.exceptions.ImproperLoginException;
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

public class ProfileActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private String chosenDistrict;
    private String chosenCity;

    private EditText updateUsername;
    private EditText updateEmail;
    private EditText updatePassword;

    private RadioGroup updateGenderGroup;
    private RadioButton updateGenderMale;
    private RadioButton updateGenderFemale;
    private RadioButton updateGenderOther;

    private TextView currentDistrictText;
    private TextView currentCityText;

    private EditText updateStreet;
    private EditText updateBuilding;
    private DatePicker updateDateOfBirth;

    private int currentUserId;
    private String currentUserPassword;


    private SharedPreferences data;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) throws ImproperLoginException {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();

        data = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = data.getInt("user_id", -1);
        
        dbHelper = new DatabaseHelper(this);

        updateUsername = findViewById(R.id.updateUsername);
        updateEmail = findViewById(R.id.updateEmail);
        updatePassword = findViewById(R.id.updatePassword);

        updateGenderGroup = findViewById(R.id.updateGenderGroup);
        updateGenderMale = findViewById(R.id.updateGenderMale);
        updateGenderFemale = findViewById(R.id.updateGenderFemale);
        updateGenderOther = findViewById(R.id.updateGenderOther);

        currentDistrictText = findViewById(R.id.currentDistrictText);
        currentCityText = findViewById(R.id.currentCityText);
        updateStreet = findViewById(R.id.updateStreet);
        updateBuilding = findViewById(R.id.updateBuilding);
        updateDateOfBirth = findViewById(R.id.updateDateOfBirth);

        ListView districtList = findViewById(R.id.updateDistrictList);
        ListView citiesList = findViewById(R.id.updateCityList);

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
        
        if (currentUserId != -1) {
            Cursor profile = dbHelper.getUserProfile(currentUserId);

            if (profile != null) {
                try {
                    if (profile.moveToFirst()) {
                        String name = profile.getString(profile.getColumnIndexOrThrow("name"));
                        String email = profile.getString(profile.getColumnIndexOrThrow("email"));
                        char gender = (char) Integer.parseInt(profile.getString(profile.getColumnIndexOrThrow("gender")));
                        String district = profile.getString(profile.getColumnIndexOrThrow("district"));
                        String city = profile.getString(profile.getColumnIndexOrThrow("city"));
                        String street = profile.getString(profile.getColumnIndexOrThrow("street"));
                        String building = profile.getString(profile.getColumnIndexOrThrow("building"));
                        String dateOfBirth = profile.getString(profile.getColumnIndexOrThrow("date_of_birth"));
                        currentUserPassword = profile.getString(profile.getColumnIndexOrThrow("password"));

                        chosenDistrict = district;
                        chosenCity = city;

                        updateUsername.setText(name);
                        updateEmail.setText(email);

                        System.out.println("GENDER: " + gender);

                        if (gender == 'M') {
                            updateGenderMale.setChecked(true);
                        } else if (gender == 'F') {
                            updateGenderFemale.setChecked(true);
                        } else {
                            updateGenderOther.setChecked(true);
                        }

                        currentDistrictText.setText(String.format("%s%s", currentDistrictText.getText(), district));
                        currentCityText.setText(String.format("%s%s", currentCityText.getText(), city));

                        updateStreet.setText(street);
                        updateBuilding.setText(building);

                        String[] dateSplit = dateOfBirth.split("-");
                        updateDateOfBirth.updateDate(
                                Integer.parseInt(dateSplit[0]),
                                Integer.parseInt(dateSplit[1]) - 1,
                                Integer.parseInt(dateSplit[2]));
                    } else {
                        throw new ImproperLoginException();
                    }
                } finally {
                    profile.close();
                }
            }
        } else {
            throw new ImproperLoginException();
        }

        Button updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(l -> {
            try {
                updateProfile();
            } catch (NoInputException | InvalidEmailException |
                     UnexpectedRegisterException | EmailAlreadyExistsException e ) {
                AlertDialogHelper.showErrorDialog(this, e);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateProfile() throws NoInputException, InvalidEmailException, UnexpectedRegisterException, EmailAlreadyExistsException {
        String username = updateUsername.getText().toString();
        String email = updateEmail.getText().toString();
        String password = updatePassword.getText().toString();
        String street = updateStreet.getText().toString();
        String building = updateBuilding.getText().toString();
        String dateOfBirth = updateDateOfBirth.getDayOfMonth() + "/" + (updateDateOfBirth.getMonth() + 1) + "/" + updateDateOfBirth.getYear();

        if (username.trim().isEmpty() || email.trim().isEmpty() || street.trim().isEmpty() || building.trim().isEmpty() || dateOfBirth.trim().isEmpty()) {
            throw new NoInputException();
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new InvalidEmailException();
        }

        if (password.isEmpty()) {
            password = "";
        }

        int selectedGenderId = updateGenderGroup.getCheckedRadioButtonId();
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        boolean result = dbHelper.updateUserProfile(currentUserId, username, email, password, gender, chosenDistrict, chosenCity, street, building, dateOfBirth);
        if (result) {
            data.edit().putString("email", email).apply();
            Toast.makeText(this, "You successfully updated your profile!", Toast.LENGTH_LONG).show();
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