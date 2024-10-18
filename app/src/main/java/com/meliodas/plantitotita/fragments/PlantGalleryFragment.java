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

import java.util.*;

public class PlantGalleryFragment extends Fragment {
    private final DatabaseManager dbManager = new DatabaseManager();
    private List<Map<String, Object>> identifications;
    private LinearLayout plantGalleryLayout;
    private final List<Plant> plantList = new ArrayList<>();
    private static final int CAMERA_REQUEST_CODE = 1001;

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

                    String identification = (String) plantData.getOrDefault("identification", "");
                    String plantName = (String) plantData.getOrDefault("name", "");
                    String plantImage = (String) plantData.getOrDefault("image", "");
                    String plantScientificName = (String) plantData.getOrDefault("scientificName", "");
                    String description = (String) plantData.getOrDefault("description", "");
                    String family = (String) plantData.getOrDefault("family", "");
                    String genus = (String) plantData.getOrDefault("genus", "");
                    String wikiUrl = (String) plantData.getOrDefault("wikiUrl", "");
                    String bestLightCondition = (String) plantData.getOrDefault("bestLightCondition", "");
                    String bestSoilType = (String) plantData.getOrDefault("bestSoilType", "");
                    String bestWatering = (String) plantData.getOrDefault("bestWatering", "");
                    String toxicity = (String) plantData.getOrDefault("toxicity", "");
                    String culturalSignificance = (String) plantData.getOrDefault("culturalSignificance", "");
                    String commonUses = (String) plantData.getOrDefault("commonUses", "");

                    HashMap<String, String> taxonomy = (HashMap<String, String>) plantData.getOrDefault("taxonomy", new HashMap<String, String>());
                    ArrayList<String> edibleParts = (ArrayList<String>) plantData.getOrDefault("edibleParts", new ArrayList<>());
                    ArrayList<String> propagationMethods = (ArrayList<String>) plantData.getOrDefault("propagationMethods", new ArrayList<>());

                    Plant plant = new Plant.Builder()
                            .identification(identification)
                            .name(plantName)
                            .scientificName(plantScientificName)
                            .family(family)
                            .genus(genus)
                            .image(plantImage)
                            .description(description)
                            .wikiUrl(wikiUrl)
                            .edibleParts(edibleParts)
                            .propagationMethods(propagationMethods)
                            .bestLightCondition(bestLightCondition)
                            .bestSoilType(bestSoilType)
                            .bestWatering(bestWatering)
                            .toxicity(toxicity)
                            .culturalSignificance(culturalSignificance)
                            .commonUses(commonUses)
                            .taxonomy(taxonomy)
                            .build();

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
            // Handle null values using safe comparisons
            String plantName = plant.name() != null ? plant.name().toLowerCase() : "";
            String scientificName = plant.scientificName() != null ? plant.scientificName().toLowerCase() : "";
            String family = plant.family() != null ? plant.family().toLowerCase() : "";
            String genus = plant.genus() != null ? plant.genus().toLowerCase() : "";

            if (plantName.contains(searchText.toLowerCase()) ||
                    scientificName.contains(searchText.toLowerCase()) ||
                    family.contains(searchText.toLowerCase()) ||
                    genus.contains(searchText.toLowerCase())) {
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

        StringBuilder taxonomyBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : plant.taxonomy().entrySet()) {
            taxonomyBuilder.append(StringUtils.capitalize(entry.getKey()))
                    .append(": ")
                    .append(StringUtils.capitalize(entry.getValue()))
                    .append("\n");
        }


        plantGalleryItem.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    Bundle args = new Bundle();
                    args.putString("plantName", capitalizedPlantName);
                    args.putString("plantImageUrl", plant.image());
                    args.putString("plantDescription", plant.description());
                    args.putString("wikiUrl", plant.wikiUrl());
                    args.putString("plantScientificName", capitalizedScientificName);
                    args.putString("family", StringUtils.capitalize(plant.family()));
                    args.putString("genus", StringUtils.capitalize(plant.genus()));
                    args.putString("wikiUrl", plant.wikiUrl());
                    args.putStringArrayList("edibleParts", plant.edibleParts());
                    args.putStringArrayList("propagationMethods", plant.propagationMethods());
                    args.putString("commonUses", plant.commonUses());
                    args.putString("culturalSignificance", plant.culturalSignificance());
                    args.putString("toxicity", plant.toxicity());
                    args.putString("bestLightCondition", plant.bestLightCondition());
                    args.putString("bestSoilType", plant.bestSoilType());
                    args.putString("bestWatering", plant.bestWatering());
                    args.putSerializable("taxonomy", new HashMap<>(plant.taxonomy()));

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

                // Add null checks and safe comparison
                identifications.removeIf(map -> {
                    Object mapIdentification = map.get("identification");
                    String plantIdentification = plant.identification();
                    return mapIdentification != null && plantIdentification != null
                            && mapIdentification.equals(plantIdentification);
                });

                dbManager.getUserDoc(userId).update("plant_identifications", identifications)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), StringUtils.capitalize(plant.name()) + " identification deleted!", Toast.LENGTH_SHORT).show();
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
