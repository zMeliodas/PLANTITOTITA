package com.meliodas.plantitotita;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

    }

    public void onClickLogout(View v){
        Button Btn = findViewById(R.id.btnLogout);
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }
}