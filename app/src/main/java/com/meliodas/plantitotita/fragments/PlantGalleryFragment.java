package com.meliodas.plantitotita.fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.DatabaseManager;
import com.meliodas.plantitotita.mainmodule.Plant;
import com.bumptech.glide.Glide;
import com.meliodas.plantitotita.mainmodule.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlantGalleryFragment extends Fragment {
    private final DatabaseManager dbManager = new DatabaseManager();
    private List<Map<String, Object>> identifications;
    private LinearLayout plantGalleryLayout;
    private final List<Plant> plantList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_gallery, container, false);
        plantGalleryLayout = view.findViewById(R.id.plantGalleryLayout);
        EditText searchEditText = view.findViewById(R.id.editTxtSearch);

        // Add TextWatcher to EditText for dynamic search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterPlants(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Fetch plant identifications for the current user
        dbManager.getUserDoc(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_identifications")) {
                identifications = (List<Map<String, Object>>) documentSnapshot.get("plant_identifications");

                if (identifications == null || identifications.isEmpty()) {
                    Toast.makeText(getContext(), "No plant identifications found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Loop through the list of identifications and create Plant objects
                for (Map<String, Object> plantData : identifications) {
                    String identification = (String) plantData.get("identification");
                    String plantName = (String) plantData.get("name");
                    String plantImage = (String) plantData.get("image");
                    String plantScientificName = (String) plantData.get("scientificName");
                    String description = (String) plantData.get("description");
                    String family = (String) plantData.get("family");
                    String genus = (String) plantData.get("genus");
                    String wikiUrl = (String) plantData.get("wikiUrl");
                    String edibleParts = (String) plantData.get("edibleParts");

                    Plant plant = new Plant(identification, plantName, plantScientificName, family, genus, plantImage, description, wikiUrl, edibleParts);
                    plantList.add(plant);
                }

                // Initially show all plants
                showPlants(plantList, inflater);
            } else {
                Toast.makeText(getContext(), "No plant identifications found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to load identifications", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    // Dynamically create and display plant items
    private void showPlants(List<Plant> plants, LayoutInflater inflater) {
        plantGalleryLayout.removeAllViews(); // Clear existing views
        if (plants.isEmpty()) {
            Toast.makeText(getContext(), "No matching plants found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a view for each plant
        for (Plant plant : plants) {
            plantGalleryLayout.addView(createPlantGalleryItem(inflater, plantGalleryLayout, plant));
        }
    }

    // Filter the plants based on the search input
    private void filterPlants(String searchText) {
        List<Plant> filteredPlants = new ArrayList<>();
        for (Plant plant : plantList) {
            if (plant.name().toLowerCase().contains(searchText.toLowerCase()) ||
                    plant.scientificName().toLowerCase().contains(searchText.toLowerCase()) ||
                    plant.family().toLowerCase().contains(searchText.toLowerCase()) ||
                    plant.genus().toLowerCase().contains(searchText.toLowerCase())) {
                filteredPlants.add(plant);
            }
        }
        showPlants(filteredPlants, getLayoutInflater());
    }

    // Dynamically create a gallery item for a plant
    public View createPlantGalleryItem(LayoutInflater inflater, ViewGroup container, Plant plant) {
        View plantGalleryItem = inflater.inflate(R.layout.custom_container_plantgallery, container, false);

        TextView plantNameTextView = plantGalleryItem.findViewById(R.id.plantIDName);
        // Apply comprehensive capitalization to the plant name
        String capitalizedPlantName = StringUtils.capitalize(plant.name());
        plantNameTextView.setText(capitalizedPlantName);

        TextView plantScientificNameTextView = plantGalleryItem.findViewById(R.id.plantIDScientificName);
        // Apply comprehensive capitalization to the scientific name
        String capitalizedScientificName = StringUtils.capitalize(plant.scientificName());
        plantScientificNameTextView.setText(capitalizedScientificName);

        ShapeableImageView plantImageView = plantGalleryItem.findViewById(R.id.plantGalleryContainerImgView);

        // Load image from URL using Glide or another image loading library
        Glide.with(this).load(plant.image()).placeholder(R.drawable.sad).error(R.drawable.custom_dialog_layout_error_icon).into(plantImageView);

        plantGalleryItem.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    Bundle args = new Bundle();
                    args.putString("plantName", capitalizedPlantName);
                    args.putString("image", plant.image());
                    args.putString("description", StringUtils.capitalize(plant.description()));
                    args.putString("wikiUrl", plant.wikiUrl());
                    args.putString("scientificName", capitalizedScientificName);
                    args.putString("family", StringUtils.capitalize(plant.family()));
                    args.putString("genus", StringUtils.capitalize(plant.genus()));
                    args.putString("wikiUrl", plant.wikiUrl());
                    args.putString("edibleParts", StringUtils.capitalize(plant.edibleParts()));

                    Fragment targetFragment = new PlantInformationFragment();
                    targetFragment.setArguments(args);

                    getActivity().runOnUiThread(() -> {
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.frameLayout, targetFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    });
                } catch (Exception e) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error retrieving plant information", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        plantGalleryItem.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(plant);
            return true;
        });

        return plantGalleryItem;
    }

    private void showDeleteConfirmationDialog(Plant plant) {
        showDialog(
                "Are you sure you want to delete the identification for " + StringUtils.capitalize(plant.name()) + "?",
                () -> deletePlant(plant)
        );
    }

    private void deletePlant(Plant plant) {
        plantList.remove(plant);

        showPlants(plantList, getLayoutInflater());

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbManager.getUserDoc(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_identifications")) {
                List<Map<String, Object>> identifications = (List<Map<String, Object>>) documentSnapshot.get("plant_identifications");
                identifications.removeIf(map -> map.get("identification").equals(plant.identification()));

                dbManager.getUserDoc(userId).update("plant_identifications", identifications)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), plant.name() + " identification deleted!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to delete from database", Toast.LENGTH_SHORT).show();
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to access database", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDialog(String message, Runnable positiveAction) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_alert_dialog_log_out, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setView(view);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        TextView dialogMessage = view.findViewById(R.id.dialogMessage);
        Button continueButton = view.findViewById(R.id.dialogContinueButton);
        Button continueButton1 = view.findViewById(R.id.dialogContinueButton1);

        // Set the custom message
        dialogMessage.setText(message);

        // Set the positive button action
        continueButton.setText("Yes");
        continueButton.setOnClickListener(view1 -> {
            positiveAction.run();
            alertDialog.dismiss();
        });

        // Set the negative button action
        continueButton1.setText("No");
        continueButton1.setOnClickListener(view1 -> {
            alertDialog.dismiss();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }
}
