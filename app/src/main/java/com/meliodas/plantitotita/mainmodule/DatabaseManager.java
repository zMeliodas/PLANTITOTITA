package com.meliodas.plantitotita.mainmodule;

import android.net.Uri;
import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DatabaseManager {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public DocumentReference getDocumentReference(String collection, String document) {
        return fStore.collection(collection).document(document);
    }

    public DocumentReference getUserDoc(String document) {
        return getDocumentReference("users", document);
    }

    public void addIdentification(Plant plant, String userId) {
        DocumentReference userDoc = getUserDoc(userId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            List<Map<String, Object>> data;
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_identifications")) {
                data = (List<Map<String, Object>>) documentSnapshot.get("plant_identifications");
            } else {
                data = new ArrayList<>();
            }

            Map<String, Object> plantData = getPlantData(plant);

            data.add(plantData);

            userDoc.set(new HashMap<String, Object>() {{
                put("plant_identifications", data);
            }}, SetOptions.merge());
        }).addOnFailureListener(e -> {
            Log.e("DatabaseManager", "Error getting document", e);
        });
    }

    public void addHealthAssessment(HealthIdentification healthIdentification, String userId, String imageUrl) {
        DocumentReference userDoc = getUserDoc(userId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            List<Map<String, Object>> data;
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_health_assessments")) {
                data = (List<Map<String, Object>>) documentSnapshot.get("plant_health_assessments");
            } else {
                data = new ArrayList<>();
            }

            Map<String, Object> healthData = getHealthData(healthIdentification);
            healthData.put("image", imageUrl);

            data.add(healthData);


            userDoc.set(new HashMap<String, Object>() {{
                put("plant_health_assessments", data);
            }}, SetOptions.merge());
        }).addOnFailureListener(e -> {
            Log.e("DatabaseManager", "Error getting document", e);
        });
    }

    private static @NotNull Map<String, Object> getHealthData(HealthIdentification healthIdentification) {
        Map<String, Object> healthData = new HashMap<>();
        HealthIdentification.Result result = healthIdentification.getResult();

        healthData.put("isPlant", result.getIsPlant().isBinary());
        healthData.put("isHealthy", result.getIsHealthy().isBinary());

        // Add plant data
        Plant plant = healthIdentification.getPlant();
        if (plant != null) {
            healthData.put("plant", getPlantData(plant));
        }

        if (result.getDisease() != null) {
            List<Map<String, Object>> diseaseData = new ArrayList<>();
            for (HealthIdentification.Result.Disease.Suggestion suggestion : result.getDisease().getSuggestions()) {
                Map<String, Object> suggestionData = new HashMap<>();
                suggestionData.put("name", suggestion.getName());
                suggestionData.put("probability", suggestion.getProbability());

                // Adding similar images if available
                List<Map<String, Object>> similarImagesData = new ArrayList<>();
                for (HealthIdentification.Result.Disease.Suggestion.SimilarImage image : suggestion.getSimilarImages()) {
                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("url", image.getUrl());
                    imageData.put("similarity", image.getSimilarity());
                    similarImagesData.add(imageData);
                }
                suggestionData.put("similarImages", similarImagesData);

                suggestionData.put("details", getDetailsData(suggestion.getDetails()));
                diseaseData.add(suggestionData);
            }
            healthData.put("diseaseSuggestions", diseaseData);
        }

        return healthData;
    }

    private static @NotNull Map<String, Object> getDetailsData(HealthIdentification.Result.Disease.Suggestion.Details details) {
        Map<String, Object> detailsData = new HashMap<>();
        detailsData.put("localName", details.getLocalName());
        detailsData.put("description", details.getDescription());
        detailsData.put("url", details.getUrl());

        if (details.getTreatment() != null) {
            Map<String, Object> treatmentData = new HashMap<>();
            treatmentData.put("prevention", details.getTreatment().getPrevention());
            treatmentData.put("biological", details.getTreatment().getBiological());
            treatmentData.put("chemical", details.getTreatment().getChemical());
            detailsData.put("treatment", treatmentData);
        }

        detailsData.put("cause", details.getCause());
        detailsData.put("commonNames", details.getCommonNames());

        return detailsData;
    }

    private static @NotNull Map<String, Object> getPlantData(Plant plant) {
        Map<String, Object> plantData = new HashMap<>();
        plantData.put("identification", plant.identification());
        plantData.put("name", plant.name());
        plantData.put("scientificName", plant.scientificName());
        plantData.put("image", plant.image());
        plantData.put("description", plant.description());
        plantData.put("edibleParts", plant.edibleParts());
        plantData.put("family", plant.family());
        plantData.put("genus", plant.genus());
        plantData.put("propagationMethods", plant.propagationMethods());
        plantData.put("commonUses", plant.commonUses());
        plantData.put("culturalSignificance", plant.culturalSignificance());
        plantData.put("toxicity", plant.toxicity());
        plantData.put("bestLightCondition", plant.bestLightCondition());
        plantData.put("bestSoilType", plant.bestSoilType());
        plantData.put("bestWatering", plant.bestWatering());
        plantData.put("wikiUrl", plant.wikiUrl());
        return plantData;
    }

    public void getPlantIdentifications(String userId, FetchDataCallback callback) {
        getUserDoc(userId).get().addOnSuccessListener(documentSnapshot -> {
            List<Map<String, Object>> data = new ArrayList<>();
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_identifications")) {
                data.addAll((List<Map<String, Object>>) documentSnapshot.get("plant_identifications"));
            }
            callback.onFetchComplete(data);
        }).addOnFailureListener(e -> {
            Log.e("DatabaseManager", "Error getting document", e);
        });
    }

    public void getHealthAssessments(String userId, FetchDataCallback callback) {
        getUserDoc(userId).get().addOnSuccessListener(documentSnapshot -> {
            List<Map<String, Object>> data = new ArrayList<>();
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_health_assessments")) {
                data.addAll((List<Map<String, Object>>) documentSnapshot.get("plant_health_assessments"));
            }
            callback.onFetchComplete(data);
        }).addOnFailureListener(e -> {
            Log.e("DatabaseManager", "Error getting document", e);
        });
    }

    public void uploadImage(String userId, Uri imageUri, UploadCallback callback) {
        String imagePath = "images/" + userId + "/" + UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference().child(imagePath);
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            Log.d("DatabaseManager", "Image uploaded successfully");
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d("DatabaseManager", "Image URL: " + uri.toString());
                callback.onUploadComplete(uri.toString());
            });
        }).addOnFailureListener(e -> {
            Log.e("DatabaseManager", "Error uploading image", e);
            throw new RuntimeException("Error uploading image");
        });
    }

    public interface UploadCallback {
        void onUploadComplete(String imageUrl);
    }
}