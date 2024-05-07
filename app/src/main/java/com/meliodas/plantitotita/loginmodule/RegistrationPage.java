package com.meliodas.plantitotita.loginmodule;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.HomePage;

public class RegistrationPage extends AppCompatActivity {

    private EditText editTextName, editTextEmailAddress, editTextMobileNumber, editTextPassword, editTextConfirmPassword;
    private TextView textViewName, textViewEmailAddress, textViewMobileNumber, textViewPassword, textViewConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        mAuth = FirebaseAuth.getInstance();
        editTextName = findViewById(R.id.editTxtName);
        editTextEmailAddress = findViewById(R.id.editTxtEmailAddress);
        editTextMobileNumber = findViewById(R.id.editTxtMobileNumber);
        editTextPassword = findViewById(R.id.editTxtPassword);
        editTextConfirmPassword = findViewById(R.id.editTxtConfirmPassword);

        textViewName = findViewById(R.id.regNameTxtView);
        textViewEmailAddress = findViewById(R.id.regEmailTxtView);
        textViewMobileNumber = findViewById(R.id.regMobileNumTxtView);
        textViewPassword = findViewById(R.id.regPasswordTxtView);
        textViewConfirmPassword = findViewById(R.id.regConfirmPasswordTxtView);

        updateAsterisk(editTextName, textViewName, "Name", R.string.NameTxtView);
        updateAsterisk(editTextEmailAddress, textViewEmailAddress, "Email Address", R.string.EmailTxtView);
        updateAsterisk(editTextMobileNumber, textViewMobileNumber, "Mobile Number", R.string.MobileNumTxtView);
        updateAsterisk(editTextPassword, textViewPassword, "Password", R.string.PasswordTxtView);
        updateAsterisk(editTextConfirmPassword, textViewConfirmPassword, "Confirm Password", R.string.ConfirmPasswordTxtView);
    }

    public void onClickCreateAccount(View v) {
        if (editTextName.getText() == null || editTextName.getText().toString().isEmpty()){
            editTextName.setError("Name can't be blank.");
            editTextName.requestFocus();
            return;
        }

        if (!editTextName.getText().toString().matches("[A-Za-z]+")){
            editTextName.setError("You may only enter letters.");
            editTextName.requestFocus();
            return;
        }

        if (editTextEmailAddress.getText() == null || editTextEmailAddress.getText().toString().isEmpty()){
            editTextEmailAddress.setError("Email can't be blank.");
            editTextEmailAddress.requestFocus();
            return;
        }

        if (editTextMobileNumber.getText() == null || editTextMobileNumber.getText().toString().isEmpty()){
            editTextMobileNumber.setError("Mobile Number can't be blank.");
            editTextMobileNumber.requestFocus();
            return;
        }

        if (editTextPassword.getText() == null || editTextPassword.getText().toString().isEmpty()){
            editTextPassword.setError("Password can't be blank.");
            editTextPassword.requestFocus();
            return;
        }

        if (editTextConfirmPassword.getText() == null || editTextConfirmPassword.getText().toString().isEmpty()){
            editTextConfirmPassword.setError("Confirm Password can't be blank.");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (editTextName.getText().length() < 2){
            editTextName.setError("Name should be at least 2 characters.");
            editTextName.requestFocus();
            return;
        }

        if (!editTextEmailAddress.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            editTextEmailAddress.setError("Invalid email address Ex.abcdefg123@gmail.com");
            editTextEmailAddress.requestFocus();
            return;
        }

        if (editTextMobileNumber.length() > 11 || editTextMobileNumber.length() < 11){
            editTextMobileNumber.setError("You can only enter 11 numbers");
            editTextMobileNumber.requestFocus();
            return;
        }

        if (!editTextMobileNumber.getText().toString().matches("^(09)\\d{9}")){
            editTextMobileNumber.setError("Invalid mobile number format");
            editTextMobileNumber.requestFocus();
            return;
        }

        if (editTextPassword.length() < 8) {
            editTextPassword.setError("Your Password is too short. Use at least 8 or more characters");
            editTextPassword.requestFocus();
            return;
        }

        if (!editTextPassword.getText().toString().matches("[A-Za-z0-9]+")){
            editTextPassword.setError("You may only enter Alphanumeric Characters");
            editTextPassword.requestFocus();
            return;
        }

        if (!editTextConfirmPassword.getText().toString().equals(editTextPassword.getText().toString())){
            editTextConfirmPassword.setError("Those passwords didn’t match. Try again.");
            editTextConfirmPassword.requestFocus();
            return;
        }

        String email = editTextEmailAddress.getText().toString();
        String password = editTextPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.getException() == null || task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        showDialog(EnumLayout.SUCCESS);
                        return;
                    }

                    if (task.getException() instanceof FirebaseAuthUserCollisionException e) {
                        editTextEmailAddress.setError("Email already exists");
                        editTextEmailAddress.requestFocus();
                    }
                });
    }

    private void showDialog(EnumLayout layout) {
        View view = LayoutInflater.from(this).inflate(
        switch (layout) {
            case SUCCESS -> R.layout.custom_alert_dialog_success;
            case ERROR -> R.layout.custom_alert_dialog_error;
        },  null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        Button continueButton = view.findViewById(R.id.dialogContinueButton);

        continueButton.setOnClickListener(view1 -> {
            switch (layout) {
                case SUCCESS -> {
                    startActivity(new Intent(getApplicationContext(), HomePage.class));
                    finish();
                }
                case ERROR -> alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
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

    public void onClickReturn(View v) {
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }

    public void onClickSignIn1(View v) {
        startActivity(new Intent(this, LoginPage.class));
        finish();
    }
}