package com.meliodas.plantitotita.mainmodule;
import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.loginmodule.WelcomePage;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
    }

    public void onClickLogout(View v){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }
}