package com.meliodas.plantitotita.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentTransaction;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.*;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class PlantHealthAssessmentGalleryFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 1001;
    private final DatabaseManager dbManager = new DatabaseManager();
    private HealthIdentification healthIdentification;
    private List<Map<String, Object>> healthIdentifications;
    private LinearLayout plantGalleryLayout;
    private final List<Plant> plantList = new ArrayList<>();

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePhotoLauncher;
    private androidx.appcompat.app.AlertDialog processingDialog;
    private BroadcastReceiver networkReceiver;
    private AlertDialog noInternetDialog;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        processImage(imageUri);
                    }
                }
        );

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        processImage(photoUri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_health_assessment, container, false);

        plantGalleryLayout = view.findViewById(R.id.plantGalleryLayout);
        refreshPlantList(null);

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View uploadImageBtn = view.findViewById(R.id.PlantHealthAssessmentLayout);
        Button uploadImageBtnIcon = view.findViewById(R.id.PlantHealthAssessmentUploadIcon);
        Button takePhotoBtn = view.findViewById(R.id.PlantHealthAssessmentTakePhotoIcon);

        uploadImageBtn.setOnClickListener(v -> {
            if (isInternetAvailable()) {
                selectImage();
            } else {
                showNoInternetDialog();
            }
        });

        uploadImageBtnIcon.setOnClickListener(v -> {
            if (isInternetAvailable()) {
                selectImage();
            } else {
                showNoInternetDialog();
            }
        });

        takePhotoBtn.setOnClickListener(v -> {
            if (isInternetAvailable()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkCameraPermission();
                } else {
                    takePhoto();  // Older versions do not require runtime permissions
                }
            } else {
                showNoInternetDialog();
            }
        });
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

    private Uri imageUri;
    private Uri photoUri;

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void processImage(Uri uri) {
        showProcessingDialog();
        new Thread(() -> {
            try {
                Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().getContentResolver(), uri));
                processImageBitmap(bitmap, uri);
            } catch (IOException e) {
                e.printStackTrace();
                dismissProcessingDialog();
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processImageBitmap(Bitmap bitmap, Uri uri) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            double longitude = 0.0;
            double latitude = 0.0;

            PlantIdApi plantIdApi = new PlantIdApi();
            healthIdentification = plantIdApi.identifyHealth(imageData, longitude, latitude);

            requireActivity().runOnUiThread(() -> {
                if (uri != null) {
                    dbManager.uploadImage(FirebaseAuth.getInstance().getUid(), uri, imageUrl -> {
                        dbManager.addHealthAssessment(healthIdentification, FirebaseAuth.getInstance().getUid(), imageUrl, () -> {
                            refreshPlantList(this::dismissProcessingDialog);
                        });
                    });
                } else {
                    Toast.makeText(requireContext(), "Error: Image URI is null", Toast.LENGTH_SHORT).show();
                    dismissProcessingDialog();
                }
            });
        } catch (IOException | JSONException e) {
            if (e instanceof IOException && (e.getMessage() != null && e.getMessage().equalsIgnoreCase("The image does not contain a plant"))) {
                requireActivity().runOnUiThread(() -> {
                    dismissProcessingDialog();
                    showValidatorDialog(R.layout.custom_alert_dialog_not_plant);
                });
            }

            if (e instanceof IOException && (e.getMessage() != null && e.getMessage().equalsIgnoreCase("The image appears to have healthy plant"))) {
                requireActivity().runOnUiThread(() -> {
                    dismissProcessingDialog();
                    showValidatorDialog(R.layout.custom_alert_dialog_plant_is_healthy);
                });
            }

            e.printStackTrace();
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show();
                dismissProcessingDialog();
            });
        }
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void takePhoto() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(requireContext(), "com.meliodas.plantitotita.fileprovider", photoFile);
            takePhotoLauncher.launch(photoUri);
        } else {
            Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void refreshPlantList(Runnable onComplete) {
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

            getActivity().runOnUiThread(() -> {
                showPlants(plantList, getLayoutInflater());
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkCameraPermission() {
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePhoto();  // Permission is granted, proceed with taking the photo
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();  // Permission granted, proceed with taking the photo
            } else {
                Toast.makeText(getContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProcessingDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_alert_dialog_processing_plant_image, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setView(view);

        processingDialog = builder.create();

        if (processingDialog.getWindow() != null) {
            processingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        processingDialog.setCancelable(false);
        processingDialog.show();
    }

    private void dismissProcessingDialog() {
        if (processingDialog != null && processingDialog.isShowing()) {
            processingDialog.dismiss();
        }
    }

    private void showValidatorDialog(int resId) {
        View view = LayoutInflater.from(requireContext()).inflate(resId, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setView(view);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        Button continueButton = view.findViewById(R.id.dialogContinueButton);

        continueButton.setOnClickListener(view1 -> {
            alertDialog.dismiss();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }

    private void showNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            return; // Avoid showing the dialog multiple times
        }

        // Inflate the custom layout for no connection dialog
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_alert_dialog_no_connection, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);
        builder.setCancelable(false); // Prevent dismissing by outside touches

        noInternetDialog = builder.create();

        // Set transparent background
        if (noInternetDialog.getWindow() != null) {
            noInternetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        Button continueButton = view.findViewById(R.id.dialogContinueButton);

        // Retry connection on "Continue" button click
        continueButton.setOnClickListener(view1 -> {
            noInternetDialog.dismiss();
        });

        noInternetDialog.show();
    }
}