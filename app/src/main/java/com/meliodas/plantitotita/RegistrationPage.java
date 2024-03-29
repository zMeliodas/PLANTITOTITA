package com.meliodas.plantitotita;

import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class RegistrationPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);
    }

    public void OnClickReturn(View v){
        Button Btn = findViewById(R.id.btnReturn);
        startActivity(new Intent(RegistrationPage.this, WelcomePage.class));
    }

    public void OnClickLogin(View v){
        Button Btn = findViewById(R.id.btnLogin);
        startActivity(new Intent(RegistrationPage.this, LoginPage.class));
    }
}