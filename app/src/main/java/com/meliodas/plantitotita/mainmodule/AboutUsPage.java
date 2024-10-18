package com.meliodas.plantitotita.mainmodule;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.meliodas.plantitotita.R;

public class AboutUsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us_page);
    }

    public void onClickReturnAboutUs(View view) {
        startActivity(new Intent(getApplicationContext(), HomePage.class));
    }

}