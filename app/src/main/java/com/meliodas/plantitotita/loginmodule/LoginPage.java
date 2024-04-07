package com.meliodas.plantitotita.loginmodule;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
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
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.HomePage;

public class LoginPage extends AppCompatActivity {

    private EditText editTextEmailAddressLogin, editTextPasswordLogin;
    private FirebaseAuth mAuth;
    AlertDialog dialog;

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
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            startActivity(new Intent(getApplicationContext(), HomePage.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            showDialog();
                        }
                    }
                });
    }

    private void showDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog_incorrect_user_or_pass, null);

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
}