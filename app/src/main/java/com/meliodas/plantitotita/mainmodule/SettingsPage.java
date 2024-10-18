package com.meliodas.plantitotita.mainmodule;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.meliodas.plantitotita.R;

public class SettingsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
    }

    public void onClickChangePassword(View view) {
        startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
    }

    public void onClickReturnSettings(View view) {
        startActivity(new Intent(getApplicationContext(), HomePage.class));
    }
}