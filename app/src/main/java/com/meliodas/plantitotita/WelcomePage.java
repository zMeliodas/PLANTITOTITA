package com.meliodas.plantitotita;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class WelcomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
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