package com.meliodas.plantitotita.fragments;

import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.meliodas.plantitotita.R;

import java.util.ArrayList;

public class PlantInformationFragment extends Fragment {

    private TextView plantName;
    private TextView scientificName;
    private TextView description;
    private ImageView plantImage;
    private TextView edibleParts;
    private TextView propagationMethods;
    private TextView commonUses;
    private TextView culturalSignificance;
    private TextView toxicity;
    private TextView bestLightCondition;
    private TextView bestSoilType;
    private TextView bestWatering;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_information, container, false);

        View backButton = view.findViewById(R.id.btnReturnPlantInfo);
        backButton.setOnClickListener(v -> {
            PlantGalleryFragment plantGalleryFragment = new PlantGalleryFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, plantGalleryFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        plantName = view.findViewById(R.id.plantInformationName);
        scientificName = view.findViewById(R.id.plantInformationScientificName);
        description = view.findViewById(R.id.plantInformationDescription);
        plantImage = view.findViewById(R.id.plantInfoImage);
        edibleParts = view.findViewById(R.id.edibleParts);
        propagationMethods = view.findViewById(R.id.propagationMethods);
        commonUses = view.findViewById(R.id.commonUses);
        culturalSignificance = view.findViewById(R.id.culturalSignificance);
        toxicity = view.findViewById(R.id.toxicity);
        bestLightCondition = view.findViewById(R.id.bestLightCondition);
        bestSoilType = view.findViewById(R.id.bestSoilType);
        bestWatering = view.findViewById(R.id.bestWatering);

        Bundle args = getArguments();

        Log.d("PlantInformationFragment", "onCreateView: " + args);

        if (args != null) {
            String name = args.getString("plantName", "");
            String sciName = args.getString("scientificName", "");
            String desc = args.getString("description", "");
            String imageUrl = args.getString("image", "");
            ArrayList<String> ediblePartsArray = args.getStringArrayList("edibleParts");
            ArrayList<String> propagationMethodsArray = args.getStringArrayList("propagationMethods");
            String commonUses = args.getString("commonUses", "");
            String culturalSignificance = args.getString("culturalSignificance", "");
            String toxicity = args.getString("toxicity", "");
            String bestLightCondition = args.getString("bestLightCondition", "");
            String bestSoilType = args.getString("bestSoilType", "");
            String bestWatering = args.getString("bestWatering", "");

            plantName.setText(name);
            scientificName.setText(sciName);
            description.setText(desc);
            edibleParts.setText(arrayToString(ediblePartsArray));
            propagationMethods.setText(arrayToString(propagationMethodsArray));
            this.commonUses.setText(commonUses);
            this.culturalSignificance.setText(culturalSignificance);
            this.toxicity.setText(toxicity);
            this.bestLightCondition.setText(bestLightCondition);
            this.bestSoilType.setText(bestSoilType);
            this.bestWatering.setText(bestWatering);

            if (!imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.aaboutus)
                        .error(R.drawable.custom_dialog_layout_error_icon)
                        .into(plantImage);
            }
        }

        return view;
    }

    public String arrayToString(ArrayList<String> array) {
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
}