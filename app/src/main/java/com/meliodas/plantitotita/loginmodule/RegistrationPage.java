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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.HomePage;
import java.util.HashMap;
import java.util.Map;

public class RegistrationPage extends AppCompatActivity {

    private EditText editTextFirstName,editTextLastName, editTextEmailAddress, editTextMobileNumber, editTextPassword, editTextConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        editTextFirstName = findViewById(R.id.editTxtName);
        editTextLastName = findViewById(R.id.editTxtLastName);
        editTextEmailAddress = findViewById(R.id.editTxtEmailAddress);
        editTextMobileNumber = findViewById(R.id.editTxtMobileNumber);
        editTextPassword = findViewById(R.id.editTxtPassword);
        editTextConfirmPassword = findViewById(R.id.editTxtConfirmPassword);

        TextView textViewName = findViewById(R.id.regNameTxtView);
        TextView textViewLastName = findViewById(R.id.regLastNameTxtView);
        TextView textViewEmailAddress = findViewById(R.id.regEmailTxtView);
        TextView textViewMobileNumber = findViewById(R.id.regMobileNumTxtView);
        TextView textViewPassword = findViewById(R.id.regPasswordTxtView);
        TextView textViewConfirmPassword = findViewById(R.id.regConfirmPasswordTxtView);

        updateAsterisk(editTextFirstName, textViewName, "First Name", R.string.NameTxtView);
        updateAsterisk(editTextLastName, textViewLastName, "Last Name", R.string.LastNameTxtView);
        updateAsterisk(editTextEmailAddress, textViewEmailAddress, "Email Address", R.string.EmailTxtView);
        updateAsterisk(editTextMobileNumber, textViewMobileNumber, "Mobile Number", R.string.MobileNumTxtView);
        updateAsterisk(editTextPassword, textViewPassword, "Password", R.string.PasswordTxtView);
        updateAsterisk(editTextConfirmPassword, textViewConfirmPassword, "Confirm Password", R.string.ConfirmPasswordTxtView);
    }

    public void onClickCreateAccount(View v) {
        if (editTextFirstName.getText() == null || editTextFirstName.getText().toString().isEmpty()){
            editTextFirstName.setError("First Name can't be blank.");
            editTextFirstName.requestFocus();
            return;
        }

        if (editTextFirstName.getText().length() < 2){
            editTextFirstName.setError("First Name should be at least 2 characters.");
            editTextFirstName.requestFocus();
            return;
        }

        if (editTextLastName.getText() == null || editTextFirstName.getText().toString().isEmpty()){
            editTextLastName.setError("Last Name can't be blank.");
            editTextLastName.requestFocus();
            return;
        }

        if (editTextLastName.getText().length() < 2){
            editTextLastName.setError("Last Name should be at least 2 characters.");
            editTextLastName.requestFocus();
            return;
        }

        if (!editTextLastName.getText().toString().matches("^[A-Z][a-z ,.'-]+$")){
            editTextLastName.setError("Please enter a valid last name. It should start with an uppercase letter and may include letters, spaces, commas, apostrophes, or hyphens.");
            editTextLastName.requestFocus();
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

        if (!editTextEmailAddress.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            editTextEmailAddress.setError("Invalid email address Ex.abcdefg123@gmail.com");
            editTextEmailAddress.requestFocus();
            return;
        }

        if (editTextMobileNumber.length() > 10 || editTextMobileNumber.length() < 10){
            editTextMobileNumber.setError("You can only enter 10 numbers");
            editTextMobileNumber.requestFocus();
            return;
        }

        if (!editTextMobileNumber.getText().toString().matches("^\\d{10}$")){
            editTextMobileNumber.setError("Invalid mobile number format");
            editTextMobileNumber.requestFocus();
            return;
        }

        if (editTextPassword.length() < 8) {
            editTextPassword.setError("Your Password is too short. Use at least 8 or more characters");
            editTextPassword.requestFocus();
            return;
        }

        if (!editTextPassword.getText().toString().matches("^(?=.*[A-Z])(?=.*[0-9]).{8,}$")){
            editTextPassword.setError("Your Password must contain at least one uppercase letter and one digit.");
            editTextPassword.requestFocus();
            return;
        }

        if (!editTextConfirmPassword.getText().toString().equals(editTextPassword.getText().toString())){
            editTextConfirmPassword.setError("Those passwords didn’t match. Try again.");
            editTextConfirmPassword.requestFocus();
            return;
        }

        String name = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String email = editTextEmailAddress.getText().toString();
        String mobileNumber = editTextMobileNumber.getText().toString();
        String password = editTextPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.getException() == null || task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information

                        if (mAuth.getCurrentUser() != null){
                            userID = mAuth.getCurrentUser().getUid();
                        }

                        DocumentReference documentReference = fStore.collection("users").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("user_name", name);
                        user.put("last_name", lastName);
                        user.put("email_address", email);
                        user.put("mobile_number", "0" + mobileNumber);
                        user.put("password", password);
                        documentReference.set(user).addOnSuccessListener(unused -> showDialog(EnumLayout.SUCCESS));
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