package com.example.userdashboard.adapters;

import static androidx.core.content.ContextCompat.getDataDir;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.userdashboard.R;
import com.example.userdashboard.activities.dashboard.DiaryActivity;
import com.example.userdashboard.activities.dashboard.TrashNotesActivity;
import com.example.userdashboard.activities.dashboard.ViewNoteActivity;
import com.example.userdashboard.database.DatabaseHelper;
import com.example.userdashboard.entities.Note;
import com.example.userdashboard.utilities.AlertDialogHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends ArrayAdapter<Note> {
    private Context context;
    private List<Note> notes;
    private DatabaseHelper dbHelper;
    private boolean deletePerm;
    private OnDataChangedListener dataChangedListener;

    public NoteAdapter(Context context, List<Note> notes, boolean deletePerm, OnDataChangedListener listener) {
        super(context, 0, notes);
        this.context = context;
        this.notes = notes;
        this.dbHelper = new DatabaseHelper(context);
        this.deletePerm = deletePerm;
        this.dataChangedListener = listener;
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Note note = getItem(position);

        if (convertView == null) {
            int layoutId = deletePerm ? R.layout.list_item_trash_note : R.layout.list_item_note;
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }

        TextView noteTitle = convertView.findViewById(R.id.noteTitle);
        TextView noteDate = convertView.findViewById(R.id.noteDate);
        TextView noteBody = convertView.findViewById(R.id.noteBody);
        Button deleteButton = convertView.findViewById(R.id.deleteNoteButton);
        LinearLayout noteInformationLayout = convertView.findViewById(R.id.noteInformationWrapper);

        noteTitle.setText(note != null ? note.getTitle() : "No Title");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        noteDate.setText(sdf.format(note.getDate()));

        String body = note != null ? note.getBody() : "No Body";
        if (body.length() > 50) {
            body = body.substring(0, 47) + "...";
        }
        noteBody.setText(body);

        deleteButton.setOnClickListener(v -> {
            if (!this.deletePerm) {
                dbHelper.deleteNote(note.getId());
                Toast.makeText(context, "Moved note to trash.", Toast.LENGTH_LONG).show();
                remove(note);
                notifyDataSetChanged();

                if (dataChangedListener != null) {
                    dataChangedListener.onDataChanged();
                }
            } else {
                AlertDialogHelper.showConfirmDialog(
                        context,
                        "Delete Permanently",
                        "This will permanently delete the note. This cannot be undone!",
                        () -> {
                            dbHelper.permanentlyDeleteNote(note.getId());
                            Toast.makeText(context, "Deleted note completely.", Toast.LENGTH_LONG).show();
                            remove(note);
                            notifyDataSetChanged();

                            if (dataChangedListener != null) {
                                dataChangedListener.onDataChanged();
                            }
                        },
                        () -> {}
                );

            }


        });

        noteInformationLayout.setOnClickListener(v -> viewNoteButton(note));

        if (deletePerm) {
            noteInformationLayout.setOnLongClickListener(v -> restoreNoteClick(note));
        }

        return convertView;
    }

    private boolean restoreNoteClick(Note note) {
        new AlertDialog.Builder(context)
                .setTitle("Restore Note")
                .setMessage("Do you want to restore this note?")
                .setPositiveButton("Restore", (dialog, which) -> {
                    dbHelper.restoreNote(note.getId());
                    Toast.makeText(context, "Note restored.", Toast.LENGTH_LONG).show();
                    remove(note);
                    notifyDataSetChanged();

                    if (dataChangedListener != null) {
                        dataChangedListener.onDataChanged();
                    }

                    // Notify the parent activity that data changed
                    if (context instanceof TrashNotesActivity) {
                        ((TrashNotesActivity) context).setResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        return true;
    }

    private void viewNoteButton(Note note) {
        String title = note.getTitle();
        String body = note.getBody();
        long date = note.getDate();
        int id = note.getId();

        Intent intent = new Intent(context, ViewNoteActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.putExtra("date", date);
        intent.putExtra("id", id);

        // If the NoteAdapter is launched from the DiaryActivity (which it is in),
        // then make it such that DiaryActivity is accepting a request code in order to
        // refresh the list of notes on the main page. Same with trash page
        if (context instanceof DiaryActivity) {
            ((DiaryActivity) context).startActivityForResult(intent, 100);
        } else if (context instanceof TrashNotesActivity) {
            ((TrashNotesActivity) context).startActivityForResult(intent, 100);
        } else {
            context.startActivity(intent);
        }
    }
}
