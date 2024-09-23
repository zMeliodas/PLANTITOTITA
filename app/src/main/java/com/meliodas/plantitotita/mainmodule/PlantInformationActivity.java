package com.meliodas.plantitotita.mainmodule;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.fragments.PlantInformationFragment;

public class PlantInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_information);

        if (savedInstanceState == null) {
            PlantInformationFragment fragment = new PlantInformationFragment();

            // Get all plant information from the intent
            Bundle args = new Bundle();
            args.putString("plantName", getIntent().getStringExtra("plantName"));
            args.putString("scientificName", getIntent().getStringExtra("scientificName"));
            args.putString("description", getIntent().getStringExtra("description"));
            args.putString("image", getIntent().getStringExtra("image"));

            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.plant_information_container, fragment)
                    .commit();
        }
    }
}