package com.example.userdashboard.activities.dashboard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

public class ViewNoteActivity extends AppCompatActivity {
    private int noteId;
    private TextView noteDate;
    private EditText noteTitle;
    private EditText noteBody;
    private Button saveButton;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();


        dbHelper = new DatabaseHelper(this);

        noteDate = findViewById(R.id.noteDate);
        noteTitle = findViewById(R.id.noteTitle);
        noteBody = findViewById(R.id.noteBody);
        saveButton = findViewById(R.id.saveButton);

        Bundle extras = getIntent().getExtras();
        noteId = -1;

        if (extras != null) {
            noteId = extras.getInt("id");
            String title = extras.getString("title");
            String body = extras.getString("body");
            long date = extras.getLong("date");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            noteDate.setText(sdf.format(date));

            noteTitle.setText(title);
            noteBody.setText(body);
        }

        saveButton.setOnClickListener(l -> saveButtonClick());
    }

    private void saveButtonClick() {
        if (noteId != -1) {
            dbHelper.updateNote(noteId, noteTitle.getText().toString(), noteBody.getText().toString());
            setResult(RESULT_OK); //sending the 100 code so it refreshes the main page in DiaryActivity
            finish();
        } else {
            Toast.makeText(this, "An error occurred and could not update the note.", Toast.LENGTH_LONG).show();
        }
    }
}