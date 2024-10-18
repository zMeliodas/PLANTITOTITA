package com.meliodas.plantitotita.loginmodule;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.HomePage;

public class LoginPage extends AppCompatActivity {

    private EditText editTextEmailAddressLogin, editTextPasswordLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        mAuth = FirebaseAuth.getInstance();
        editTextEmailAddressLogin = findViewById(R.id.editTxtEmailAddressLogin);
        editTextPasswordLogin = findViewById(R.id.editTxtPasswordLogin);
    }

    public void onClickLogin(View v){
        String email, password;
        email = editTextEmailAddressLogin.getText().toString();
        password = editTextPasswordLogin.getText().toString();

        if(editTextEmailAddressLogin.getText() == null || editTextEmailAddressLogin.getText().toString().isEmpty()){
            editTextEmailAddressLogin.setError("Email can't be blank.");
            return;
        }

        if(editTextPasswordLogin.getText() == null || editTextPasswordLogin.getText().toString().isEmpty()){
            editTextPasswordLogin.setError("Password can't be blank.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                // Proceed to HomePage
                                startActivity(new Intent(LoginPage.this, HomePage.class));
                                finish();
                            } else {
                                showCustomDialog(R.layout.custom_alert_verify_email_before_login);
                                FirebaseAuth.getInstance().signOut();
                            }
                        }
                    } else {
                        showCustomDialog(R.layout.custom_alert_dialog_incorrect_user_or_pass);
                    }
                });
    }

    private void showCustomDialog(int layoutResId) {
        // Inflate the layout passed as a parameter
        View view = LayoutInflater.from(this).inflate(layoutResId, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        Button continueButton = view.findViewById(R.id.dialogContinueButton2);

        continueButton.setOnClickListener(view1 -> {
            alertDialog.dismiss();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    public void onClickReturn1(View v){
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }

    public void onClickRegister(View v){
        startActivity(new Intent(this, RegistrationPage.class));
        finish();
    }

    public void onClickForgotPassword(View v){
        showForgotPasswordDialog();
    }

    public void showForgotPasswordDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog_forgot_pass, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        EditText emailEditText = view.findViewById(R.id.editTxtEmailAddress);
        TextView okayButton = view.findViewById(R.id.btnOkay);
        TextView cancelButton = view.findViewById(R.id.btnCancel);

        okayButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
                alertDialog.dismiss();
            } else {
                emailEditText.setError("Email cannot be empty");
            }
        });

        cancelButton.setOnClickListener(v -> alertDialog.dismiss());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginPage.this, "Password reset email sent", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginPage.this, "Failed to send reset email", Toast.LENGTH_LONG).show();
                    }
                });
    }
}