package com.meliodas.plantitotita;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomePage extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
        mAuth = FirebaseAuth.getInstance();
    }
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
    public void onClickSignIn(View v){
        Button Btn = findViewById(R.id.btnSignIn);
        startActivity(new Intent(this, LoginPage.class));
    }

    public void onClickSignUp(View v){
        Button Btn = findViewById(R.id.btnSignUp);
        startActivity(new Intent(this, RegistrationPage.class));
    }

}