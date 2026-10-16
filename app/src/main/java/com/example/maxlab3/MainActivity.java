package com.example.maxlab3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DataStore.OnDataChangedListener {

    private final DataStore store = DataStore.getInstance();
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnAddNote = findViewById(R.id.btnAddNote);
        Button btnAddUser = findViewById(R.id.btnAddUser);
        ListView listNotes = findViewById(R.id.listNotes);

        adapter = new NoteAdapter(this);
        listNotes.setAdapter(adapter);
        listNotes.setEmptyView(findViewById(R.id.tvEmpty));

        // Event: กดปุ่ม -> เปลี่ยนไปหน้า AddNoteActivity
        btnAddNote.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddNoteActivity.class)));

        // Event: กดปุ่ม -> เปลี่ยนไปหน้า AddUserActivity
        btnAddUser.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddUserActivity.class)));

        // Event: แตะที่โน้ต -> แสดงรายละเอียดพร้อมเจ้าของ
        listNotes.setOnItemClickListener((parent, view, position, id) ->
                showNoteDetail(adapter.getItem(position)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        store.addListener(this);   // เริ่มฟังอีเวนต์ข้อมูลเปลี่ยน
        refreshNotes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        store.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        refreshNotes();
    }

    private void refreshNotes() {
        adapter.clear();
        adapter.addAll(store.getAllNotes());
        adapter.notifyDataSetChanged();
    }

    private void showNoteDetail(Note note) {
        if (note == null) return;
        User owner = note.getOwner();
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(note.getCreatedDate());
        String header = getString(R.string.detail_owner, owner != null ? owner.getUsername() : "-")
                + "\n" + getString(R.string.detail_email, owner != null ? owner.getEmail() : "-")
                + "\n" + getString(R.string.detail_date, date);

        new AlertDialog.Builder(this)
                .setTitle(note.getTitle())
                .setMessage(header + "\n\n" + note.getDetail())
                .setPositiveButton(R.string.close, null)
                .show();
    }
}
