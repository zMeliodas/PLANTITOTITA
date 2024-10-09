package com.meliodas.plantitotita.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
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
    private static final int CAPTURE_IMAGE_REQUEST = 101;
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

        plantGalleryLayout = view.findViewById(R.id.plantGalleryLayout);
        refreshPlantList();

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View uploadImageBtn = view.findViewById(R.id.PlantHealthAssessmentLayout);
        Button uploadImageBtnIcon = view.findViewById(R.id.PlantHealthAssessmentUploadIcon);
        Button takePhotoBtn = view.findViewById(R.id.PlantHealthAssessmentTakePhotoIcon);
        uploadImageBtn.setOnClickListener(v -> {
            selectImage();
        });

        uploadImageBtnIcon.setOnClickListener(v -> {
            selectImage();
        });

        takePhotoBtn.setOnClickListener(v -> takePhoto());
    }

    // Dynamically create and display plant items
    private void showPlants(List<Plant> plants, LayoutInflater inflater) {
        plantGalleryLayout.removeAllViews(); // Clear existing views
        if (plants.isEmpty()) {
            Toast.makeText(getContext(), "No plant health assessments found!", Toast.LENGTH_SHORT).show();
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

        Log.d("PlantHealthAssessmentGalleryFragment", "createPlantGalleryItem: " + plant.name() + " " + plant.scientificName());

        ShapeableImageView plantImageView = plantGalleryItem.findViewById(R.id.plantGalleryContainerImgView);

        // Load image from URL using Glide or another image loading library
        Glide.with(this).load(plant.image()).placeholder(R.drawable.sad).error(R.drawable.custom_dialog_layout_error_icon).into(plantImageView);

        plantGalleryItem.setOnClickListener(v -> {  // Open the PlantHealthAssessmentInformationFragment when the plant item is clicked
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

        plantGalleryItem.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(plant);
            return true;
        });

        return plantGalleryItem;
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        if (requestCode == PICK_IMAGE_REQUEST && data.getData() != null) {
            imageUri = data.getData();
            processImage(imageUri);
        } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            processImage(imageBitmap);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processImage(Uri imageUri) {
        new Thread(() -> {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                processImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processImage(Bitmap bitmap) {
        new Thread(() -> processImageBitmap(bitmap)).start();
    }

    private void onImageUploadComplete(String imageUrl) {
        getActivity().runOnUiThread(() -> {
            dbManager.addHealthAssessment(healthIdentification, FirebaseAuth.getInstance().getUid(), imageUrl);
            refreshPlantList();
            Toast.makeText(getContext(), "Plant health assessment added successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshPlantList() {
        String userId = FirebaseAuth.getInstance().getUid();
        dbManager.getHealthAssessments(userId, healthIdentifications -> {
            this.healthIdentifications = healthIdentifications;
            plantList.clear();

            for (Map<String, Object> healthIdentification : healthIdentifications) {
                try {
                    Map<String, Object> plantData = (Map<String, Object>) healthIdentification.get("plant");
                    Plant plant = new Plant.Builder()
                            .name((String) plantData.getOrDefault("name", ""))
                            .scientificName((String) plantData.getOrDefault("scientificName", ""))
                            .image((String) healthIdentification.getOrDefault("image", ""))
                            .build();
                    plantList.add(plant);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            getActivity().runOnUiThread(() -> showPlants(plantList, getLayoutInflater()));
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processImageBitmap(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            double longitude = 0.0;
            double latitude = 0.0;

            PlantIdApi plantIdApi = new PlantIdApi();
            healthIdentification = plantIdApi.identifyHealth(imageData, longitude, latitude);

            getActivity().runOnUiThread(() -> {
                    dbManager.uploadImage(FirebaseAuth.getInstance().getUid(), imageUri, imageUrl -> {
                    dbManager.addHealthAssessment(healthIdentification, FirebaseAuth.getInstance().getUid(), imageUrl);
                    // Refresh the plant list or update UI as needed
                    // You might want to call a method like refreshPlantList() here
                });
            });
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void showDeleteConfirmationDialog(Plant plant) {
        showDialog(
                "Are you sure you want to delete the health assessment for " + StringUtils.capitalize(plant.name()) + "?",
                () -> deletePlant(plant)
        );
    }

    private void deletePlant(Plant plant) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Remove the plant from the local list
        plantList.remove(plant);

        // Update the UI
        showPlants(plantList, getLayoutInflater());

        // Update the database
        dbManager.getUserDoc(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_health_assessments")) {
                List<Map<String, Object>> assessments = (List<Map<String, Object>>) documentSnapshot.get("plant_health_assessments");

                boolean removed = assessments.removeIf(assessment -> {
                    String assessmentImage = (String) assessment.get("image");
                    boolean matches = plant.image().equals(assessmentImage);

                    return matches;
                });

                if (!removed) {
                    Toast.makeText(getContext(), "No matching assessment found for " + plant.name(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the database
                dbManager.getUserDoc(userId).update("plant_health_assessments", assessments)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), plant.name() + " assessment deleted!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to delete from database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            // Revert the local change if the database update fails
                            plantList.add(plant);
                            showPlants(plantList, getLayoutInflater());
                        });
            } else {
                Toast.makeText(getContext(), "No assessments found in the database", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to access database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Revert the local change if database access fails
            plantList.add(plant);
            showPlants(plantList, getLayoutInflater());
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