package com.example.maxlab3;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AddUserActivity extends AppCompatActivity implements DataStore.OnDataChangedListener {

    private final DataStore store = DataStore.getInstance();
    private EditText etUsername, etEmail, etPassword;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnSaveUser = findViewById(R.id.btnSaveUser);
        ListView listUsers = findViewById(R.id.listUsers);
        listUsers.setEmptyView(findViewById(R.id.tvNoUsers));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listUsers.setAdapter(adapter);

        // Event: กดปุ่มบันทึก -> สร้าง User เก็บลง DataStore
        btnSaveUser.setOnClickListener(v -> saveUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        store.addListener(this);
        refreshUsers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        store.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        refreshUsers();
    }

    private void saveUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (username.isEmpty()) {
            etUsername.setError(getString(R.string.error_required));
            return;
        }
        if (store.isUsernameTaken(username)) {
            etUsername.setError(getString(R.string.error_username_taken));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_email));
            return;
        }
        if (password.length() < 4) {
            etPassword.setError(getString(R.string.error_password));
            return;
        }

        store.createUser(username, email, sha256(password));
        Toast.makeText(this, R.string.user_saved, Toast.LENGTH_SHORT).show();

        etUsername.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etUsername.requestFocus();
    }

    private void refreshUsers() {
        List<String> rows = new ArrayList<>();
        for (User u : store.getUsers()) {
            rows.add(getString(R.string.user_row_format,
                    u.getUserId(), u.getUsername(), u.getEmail(), u.getUserNotes().size()));
        }
        adapter.clear();
        adapter.addAll(rows);
        adapter.notifyDataSetChanged();
    }

    private static String sha256(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return text; // ไม่น่าเกิดขึ้น
        }
    }
}
