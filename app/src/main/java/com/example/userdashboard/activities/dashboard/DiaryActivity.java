package com.example.userdashboard.activities.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.userdashboard.R;
import com.example.userdashboard.adapters.NoteAdapter;
import com.example.userdashboard.database.DatabaseHelper;
import com.example.userdashboard.database.DatabaseTables;
import com.example.userdashboard.entities.Note;
import com.example.userdashboard.fragments.TopMenu;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryActivity extends AppCompatActivity {
    private NoteAdapter adapter;
    private DatabaseHelper dbHelper;
    private int currentUserId;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences data = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = data.getInt("user_id", -1);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();

        ListView diaryListView = findViewById(R.id.diaryListView);
        List<Note> notes = new ArrayList<>();

        loadNotes(notes);

        adapter = new NoteAdapter(this, notes, false, this::updateEmptyState);
        diaryListView.setAdapter(adapter);
        updateEmptyState();

        Button newNoteButton = findViewById(R.id.newNoteButton);
        newNoteButton.setOnClickListener(v -> newNoteButtonClick());

        Button viewTrashButton = findViewById(R.id.viewTrashButton);
        viewTrashButton.setOnClickListener(v -> viewTrashButtonClick());

        diaryListView.setOnItemClickListener((parent, view, position, id) -> {
            Note clickedNote = notes.get(position);

            String title = clickedNote.getTitle();
            String body = clickedNote.getBody();
            long date = clickedNote.getDate();

            Intent intent = new Intent(DiaryActivity.this, ViewNoteActivity.class);
            intent.putExtra("TITLE", title);
            intent.putExtra("BODY", body);
            intent.putExtra("DATE", date);

            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == 100 || requestCode == 200) && resultCode == RESULT_OK) {
            List<Note> notes = new ArrayList<>();
            loadNotes(notes);
            adapter.clear();
            adapter.addAll(notes);
            adapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }

    private void viewTrashButtonClick() {
        Intent intent = new Intent(DiaryActivity.this, TrashNotesActivity.class);
        startActivityForResult(intent, 200); //200 is separate from 100 because we are already using 100 for the normal diary list
    }

    private void loadNotes(List<Note> notes) {
        Cursor cursor = dbHelper.getUserNotes(currentUserId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.Notes.NOTE_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.Notes.NOTE_TITLE));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseTables.Notes.NOTE_CREATED_DATE));
                String body = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.Notes.NOTE_BODY));

                notes.add(new Note(id, title, date, body));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void newNoteButtonClick() {
        int noteId = dbHelper.addNote(currentUserId, "New Note", "...");
        Cursor note = dbHelper.getUserNote(noteId);

        if (note != null && note.moveToFirst()) {

            String noteTitle = note.getString(note.getColumnIndexOrThrow(DatabaseTables.Notes.NOTE_TITLE));
            long noteCreationDate = Long.parseLong(note.getString(note.getColumnIndexOrThrow(DatabaseTables.Notes.NOTE_CREATED_DATE)));
            String noteBody = note.getString(note.getColumnIndexOrThrow(DatabaseTables.Notes.NOTE_BODY));

            note.close();

            Note newNote = new Note(noteId, noteTitle, noteCreationDate, noteBody);
            adapter.add(newNote);
            adapter.notifyDataSetChanged();
            updateEmptyState();
        }

    }

    private void updateEmptyState() {
        TextView emptyMessage = findViewById(R.id.emptyDiaryMessage);
        ListView diaryListView = findViewById(R.id.diaryListView);

        if (adapter.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            diaryListView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            diaryListView.setVisibility(View.VISIBLE);
        }
    }
}