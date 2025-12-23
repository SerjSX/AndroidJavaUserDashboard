package com.example.userdashboard.activities.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.userdashboard.R;
import com.example.userdashboard.adapters.NoteAdapter;
import com.example.userdashboard.database.DatabaseHelper;
import com.example.userdashboard.database.DatabaseTables;
import com.example.userdashboard.entities.Note;
import com.example.userdashboard.fragments.TopMenu;

import java.util.ArrayList;
import java.util.List;

public class TrashNotesActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private NoteAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash_notes);

        dbHelper = new DatabaseHelper(this);

        SharedPreferences data = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = data.getInt("user_id", -1);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction topMenuTransaction = fragmentManager.beginTransaction();
        topMenuTransaction.replace(R.id.top_menu, new TopMenu());
        topMenuTransaction.commit();

        ListView trashListView = findViewById(R.id.trashNotesListView);
        List<Note> notes = new ArrayList<>();

        loadNotes(notes);

        adapter = new NoteAdapter(this, notes, true, this::updateEmptyState);
        trashListView.setAdapter(adapter);
        updateEmptyState();

        trashListView.setOnItemClickListener((parent, view, position, id) -> {
            Note clickedNote = notes.get(position);

            String title = clickedNote.getTitle();
            String body = clickedNote.getBody();
            long date = clickedNote.getDate();

            Intent intent = new Intent(TrashNotesActivity.this, ViewNoteActivity.class);
            intent.putExtra("TITLE", title);
            intent.putExtra("BODY", body);
            intent.putExtra("DATE", date);

            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            List<Note> notes = new ArrayList<>();
            loadNotes(notes);
            adapter.clear();
            adapter.addAll(notes);
            adapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }

    private void loadNotes(List<Note> notes) {
        Cursor cursor = dbHelper.getDeletedNotes(currentUserId);

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

    private void updateEmptyState() {
        TextView emptyMessage = findViewById(R.id.emptyTrashMessage);
        ListView trashListView = findViewById(R.id.trashNotesListView);

        if (adapter.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            trashListView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            trashListView.setVisibility(View.VISIBLE);
        }
    }
}