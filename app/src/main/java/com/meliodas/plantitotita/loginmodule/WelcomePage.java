package com.meliodas.plantitotita.loginmodule;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.loginmodule.LoginPage;
import com.meliodas.plantitotita.loginmodule.RegistrationPage;
import com.meliodas.plantitotita.mainmodule.HomePage;

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
        startActivity(new Intent(this, LoginPage.class));
    }

    public void onClickSignUp(View v){
        startActivity(new Intent(this, RegistrationPage.class));
    }

}