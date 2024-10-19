package com.meliodas.plantitotita.mainmodule;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.meliodas.plantitotita.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextCurrentEmail, editTextCurrentPassword, editTextNewPassword;
    private TextView currentEmailTxtView, currentPasswordTxtView, newPasswordTxtView;
    private Button btnChangePasswordConfirm, btnChangePasswordSave;
    private ConstraintLayout newPasswordLayout;
    private String currentPassword;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextCurrentEmail = findViewById(R.id.editTxtCurrentEmail);
        editTextCurrentPassword = findViewById(R.id.editTxtCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTxtNewPassword);
        btnChangePasswordConfirm = findViewById(R.id.btnChangePasswordConfirm);
        btnChangePasswordSave = findViewById(R.id.btnChangePasswordSave);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);

        currentEmailTxtView = findViewById(R.id.changePasswordCurrentEmailTxtView);
        currentPasswordTxtView = findViewById(R.id.changePasswordCurrentPasswordTxtView);
        newPasswordTxtView = findViewById(R.id.textView354);

        updateAsterisk(editTextCurrentEmail, currentEmailTxtView, "Current Email Address", R.string.CurrentEmailTxtView);
        updateAsterisk(editTextCurrentPassword, currentPasswordTxtView, "Current Password", R.string.CurrentPasswordTxtView);
        updateAsterisk(editTextNewPassword, newPasswordTxtView, "New Password", R.string.EnterNewPasswordTxtView);

        setupListeners();
    }

    private void setupListeners() {
        btnChangePasswordConfirm.setOnClickListener(v -> validateCurrentCredentials());
        btnChangePasswordSave.setOnClickListener(v -> changePassword());
    }

    private void validateCurrentCredentials() {
        String inputEmail = editTextCurrentEmail.getText().toString().trim();
        currentPassword = editTextCurrentPassword.getText().toString().trim();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is currently logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String loggedInUserEmail = currentUser.getEmail();

        if (inputEmail.isEmpty() || currentPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!inputEmail.equals(loggedInUserEmail)) {
            Toast.makeText(this, "Current Email is incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(loggedInUserEmail, currentPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Current credentials are valid, show new password input
                        newPasswordLayout.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(this, "Current Password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void changePassword() {
        String newPassword = editTextNewPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.equals(currentPassword)) {
            Toast.makeText(this, "New password must be different from the current password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editTextNewPassword.getText() == null || editTextNewPassword.getText().toString().isEmpty()){
            editTextNewPassword.setError("Password can't be blank.");
            editTextNewPassword.requestFocus();
            return;
        }

        if (editTextNewPassword.length() < 8) {
            editTextNewPassword.setError("Your Password is too short. Use at least 8 or more characters");
            editTextNewPassword.requestFocus();
            return;
        }

        if (!editTextNewPassword.getText().toString().matches("^(?=.*[A-Z])(?=.*[0-9]).{8,}$")){
            editTextNewPassword.setError("Your Password must contain at least one uppercase letter and one digit.");
            editTextNewPassword.requestFocus();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void onClickReturnChangePassword(View v){
        startActivity(new Intent(getApplicationContext(), SettingsPage.class));
        finish();
    }

    public void updateAsterisk(EditText editTxt, TextView txtView, String newText, int resourceID){
        editTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    txtView.setText(newText);
                }

                if (charSequence.toString().isEmpty()) {
                    txtView.setText(resourceID);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
}