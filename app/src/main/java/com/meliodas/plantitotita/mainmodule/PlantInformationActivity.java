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
            args.putString("scientificName", getIntent().getStringExtra("plantScientificName"));
            args.putString("description", getIntent().getStringExtra("description"));
            args.putString("image", getIntent().getStringExtra("image"));
            args.putStringArrayList("edibleParts", getIntent().getStringArrayListExtra("edibleParts"));
            args.putStringArrayList("propagationMethods", getIntent().getStringArrayListExtra("propagationMethods"));
            args.putString("commonUses", getIntent().getStringExtra("commonUses"));
            args.putString("culturalSignificance", getIntent().getStringExtra("culturalSignificance"));
            args.putString("toxicity", getIntent().getStringExtra("toxicity"));
            args.putString("bestLightCondition", getIntent().getStringExtra("bestLightCondition"));
            args.putString("bestSoilType", getIntent().getStringExtra("bestSoilType"));
            args.putString("bestWatering", getIntent().getStringExtra("bestWatering"));
            args.putString("taxonomy", getIntent().getStringExtra("taxonomy"));

            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.plant_information_container, fragment)
                    .commit();
        }
    }
}