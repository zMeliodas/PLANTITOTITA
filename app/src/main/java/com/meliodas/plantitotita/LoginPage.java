package com.meliodas.plantitotita;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
    }

    public void OnClickReturn1(View v){
        Button Btn = findViewById(R.id.btnReturn1);
        startActivity(new Intent(LoginPage.this, WelcomePage.class));
    }

    public void OnClickRegister(View v){
        Button Btn = findViewById(R.id.btnRegister);
        startActivity(new Intent(LoginPage.this, RegistrationPage.class));
    }
}