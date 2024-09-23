package com.meliodas.plantitotita.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.DatabaseManager;
import com.meliodas.plantitotita.mainmodule.PlantIdApi;
import com.meliodas.plantitotita.mainmodule.Plant;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class PlantGalleryFragment extends Fragment {
    private DatabaseManager dbManager = new DatabaseManager(); // Database Manager to fetch data
    private PlantIdApi plantIdApi = new PlantIdApi(); // PlantIdApi to retrieve plant information

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_gallery, container, false);
        LinearLayout plantGalleryLayout = view.findViewById(R.id.plantGalleryLayout);

        // Fetch plant identifications for the current user
        dbManager.getUserDoc(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_identifications")) {
                List<Map<String, Object>> identifications = (List<Map<String, Object>>) documentSnapshot.get("plant_identifications");

                if (identifications == null) {
                    Toast.makeText(getContext(), "No plant identifications found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Loop through the list of identifications and create views
                for (Map<String, Object> plantData : identifications) {
                    String identification = (String) plantData.get("identification");
                    String plantName = (String) plantData.get("name");
                    String plantImage = (String) plantData.get("image");
                    String plantScientificName = (String) plantData.get("scientificName");
                    String description = (String) plantData.get("description");
                    String family = (String) plantData.get("family");
                    String genus = (String) plantData.get("genus");
                    String wikiUrl = (String) plantData.get("wikiUrl");

                    Plant plant = new Plant(identification, plantName, plantScientificName, family, genus, plantImage, description, wikiUrl);

                    plantGalleryLayout.addView(createPlantGalleryItem(inflater, plantGalleryLayout, plant));
                }
            } else {
                Toast.makeText(getContext(), "No plant identifications found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load identifications", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    // Dynamically create a gallery item
    public View createPlantGalleryItem(LayoutInflater inflater, ViewGroup container, Plant plant) {
        View plantGalleryItem = inflater.inflate(R.layout.custom_container_plantgallery, container, false);

        TextView plantNameTextView = plantGalleryItem.findViewById(R.id.plantIDName);
        plantNameTextView.setText(plant.name());

        TextView plantScientificNameTextView = plantGalleryItem.findViewById(R.id.plantIDScientificName);
        plantScientificNameTextView.setText(plant.scientificName());

        ShapeableImageView plantImageView = plantGalleryItem.findViewById(R.id.plantGalleryContainerImgView);

        // Load image from URL using Glide or another image loading library
        Glide.with(this)
                .load(plant.image())
                .placeholder(R.drawable.sad)
                .error(R.drawable.custom_dialog_layout_error_icon)
                .into(plantImageView);

        plantGalleryItem.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    Bundle args = new Bundle();
                    args.putString("plantName", plant.name());
                    args.putString("image", plant.image());
                    args.putString("description", plant.description());
                    args.putString("wikiUrl", plant.wikiUrl());
                    args.putString("scientificName", plant.scientificName());
                    args.putString("family", plant.family());
                    args.putString("genus", plant.genus());
                    args.putString("wikiUrl", plant.wikiUrl());

                    Fragment targetFragment = new PlantInformationFragment();
                    targetFragment.setArguments(args);

                    getActivity().runOnUiThread(() -> {
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.add(R.id.frameLayout, targetFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    });
                } catch (Exception e) {
                    Log.e("PlantGalleryFragment", "Error retrieving plant information", e);
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error retrieving plant information", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        return plantGalleryItem;
    }
}