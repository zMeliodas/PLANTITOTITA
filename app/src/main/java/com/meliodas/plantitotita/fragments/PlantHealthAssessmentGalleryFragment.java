package com.meliodas.plantitotita.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.*;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class PlantHealthAssessmentGalleryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 100;
    private final DatabaseManager dbManager = new DatabaseManager();
    private Uri imageUri;
    private HealthIdentification healthIdentification;
    private List<Map<String, Object>> healthIdentifications;
    private LinearLayout plantGalleryLayout;
    private final List<Plant> plantList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_health_assessment, container, false);

        dbManager.getHealthAssessments(FirebaseAuth.getInstance().getUid(), healthIdentifications -> {
            this.healthIdentifications = healthIdentifications;

            StringUtils.largeLog("PlantHealthAssessmentGalleryFragment",  healthIdentifications.toString());

            for (Map<String, Object> healthIdentification : healthIdentifications) {
                try {

                   Map<String, Object> plantData = (Map<String, Object>) healthIdentification.get("plant");

                   Plant plant = new Plant.Builder()
                           .name((String) plantData.getOrDefault("name", ""))
                            .scientificName((String) plantData.getOrDefault("scientific_name", ""))
                            .image((String) healthIdentification.getOrDefault("image", ""))
                            .build();

                    plantList.add(plant);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            plantGalleryLayout = view.findViewById(R.id.plantGalleryLayout);
            showPlants(plantList, inflater);
        });

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View uploadImageBtn = view.findViewById(R.id.PlantHealthAssessmentLayout);
        Button uploadImageBtnIcon = view.findViewById(R.id.PlantHealthAssessmentUploadIcon);
        uploadImageBtn.setOnClickListener(v -> {
            selectImage();
        });

        uploadImageBtnIcon.setOnClickListener(v -> {
            selectImage();
        });
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

        Log.d("PlantHealthAssessmentGalleryFragment", "createPlantGalleryItem: " + plant.name() +" " + plant.scientificName());

        ShapeableImageView plantImageView = plantGalleryItem.findViewById(R.id.plantGalleryContainerImgView);

        // Load image from URL using Glide or another image loading library
        Glide.with(this).load(plant.image()).placeholder(R.drawable.sad).error(R.drawable.custom_dialog_layout_error_icon).into(plantImageView);

        plantGalleryItem.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    Bundle args = new Bundle();
                    args.putString("plantName", capitalizedPlantName);
                    args.putString("plantScientificName", capitalizedScientificName);
                    args.putString("plantImage", plant.image());

                    Fragment targetFragment = new PlantHealthAssessmentInformationFragment();
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

        /*plantGalleryItem.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(plant);
            return true;
        });*/

        return plantGalleryItem;
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PICK_IMAGE_REQUEST || resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        imageUri = data.getData();
        new Thread( () -> {
            try {
                // Convert image URI to Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);

                // Convert Bitmap to byte array
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();

                // Here you could get location data (latitude, longitude) if needed. For now, I'm passing dummy values.
                double longitude = 0.0;
                double latitude = 0.0;

                // Pass the image byte array to your API call
                PlantIdApi plantIdApi = new PlantIdApi();
                healthIdentification = plantIdApi.identifyHealth(imageData, longitude, latitude);

                //upload to firebase storage
                dbManager.uploadImage(FirebaseAuth.getInstance().getUid(), imageUri, imageUrl -> {
                    dbManager.addHealthAssessment(healthIdentification, FirebaseAuth.getInstance().getUid(), imageUrl);
                });
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }


}