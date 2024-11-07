package com.meliodas.plantitotita.mainmodule;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.fragments.PlantInformationFragment;

import java.util.HashMap;

public class PlantInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_information);

        if (savedInstanceState == null) {
            PlantInformationFragment fragment = new PlantInformationFragment();

            // Get all plant information from the intent
            Bundle args = new Bundle();
            args.putBoolean("isFromGallery", getIntent().getBooleanExtra("isFromGallery", false));
            args.putString("plantName", getIntent().getStringExtra("plantName"));
            args.putString("plantScientificName", getIntent().getStringExtra("plantScientificName"));
            args.putString("plantDescription", getIntent().getStringExtra("plantDescription"));
            args.putString("plantImageUrl", getIntent().getStringExtra("plantImageUrl"));
            args.putStringArrayList("edibleParts", getIntent().getStringArrayListExtra("edibleParts"));
            args.putStringArrayList("propagationMethods", getIntent().getStringArrayListExtra("propagationMethods"));
            args.putString("commonUses", getIntent().getStringExtra("commonUses"));
            args.putString("culturalSignificance", getIntent().getStringExtra("culturalSignificance"));
            args.putString("toxicity", getIntent().getStringExtra("toxicity"));
            args.putString("bestLightCondition", getIntent().getStringExtra("bestLightCondition"));
            args.putString("bestSoilType", getIntent().getStringExtra("bestSoilType"));
            args.putString("bestWatering", getIntent().getStringExtra("bestWatering"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.putSerializable("taxonomy", getIntent().getSerializableExtra("taxonomy", HashMap.class));
            }

            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.plant_information_container, fragment)
                    .commit();
        }
    }
}