package com.meliodas.plantitotita;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginPage extends AppCompatActivity {

    private EditText editTextEmailAddressLogin, editTextPasswordLogin;
    private FirebaseAuth mAuth;
    AlertDialog dialog;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(this, HomePage.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        mAuth = FirebaseAuth.getInstance();
        editTextEmailAddressLogin = findViewById(R.id.editTxtEmailAddressLogin);
        editTextPasswordLogin = findViewById(R.id.editTxtPasswordLogin);
    }

    public void onClickLogin(View v){
        Button Btn = findViewById(R.id.btnLogin1);
        String email, password;
        email = editTextEmailAddressLogin.getText().toString();
        password = editTextPasswordLogin.getText().toString();

        if(editTextEmailAddressLogin.getText() == null || editTextEmailAddressLogin.getText().toString().isEmpty()){
            editTextEmailAddressLogin.setError("Email can't be blank.");
            return;
        }

        if(editTextPasswordLogin.getText() == null || editTextPasswordLogin.getText().toString().isEmpty()){
            editTextPasswordLogin.setError("Password can't be blank.");
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(LoginPage.this, HomePage.class));
                            finish();
                            System.exit(0);

                        } else {
                            // If sign in fails, display a message to the user.
                            showDialog("Error", "Incorrect Email or Password","OK", () -> dialog.dismiss());

                        }
                    }
                });
    }

    private void showDialog(String title, String message, String positiveButtonText, Runnable onClickRunnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
            if (onClickRunnable != null) {
                onClickRunnable.run();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void onClickReturn1(View v){
        Button Btn = findViewById(R.id.btnReturn1);
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }

    public void onClickRegister(View v){
        Button Btn = findViewById(R.id.btnRegister);
        startActivity(new Intent(this, RegistrationPage.class));
        finish();
    }
}