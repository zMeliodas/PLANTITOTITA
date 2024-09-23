package com.meliodas.plantitotita.mainmodule;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private static @NotNull Map<String, Object> getPlantData(Plant plant) {
        Map<String, Object> plantData = new HashMap<>();
        plantData.put("identification", plant.identification());
        plantData.put("name", plant.name());
        plantData.put("scientificName", plant.scientificName());
        plantData.put("family", plant.family());
        plantData.put("genus", plant.genus());
        plantData.put("image", plant.image());
        plantData.put("description", plant.description());
        plantData.put("wikiUrl", plant.wikiUrl());
        return plantData;
    }

    public List<Map<String, Object>> getPlantIdentifications(String userId) {
        List<Map<String, Object>> data = new ArrayList<>();
        getUserDoc(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_identifications")) {
                data.addAll((List<Map<String, Object>>) documentSnapshot.get("plant_identifications"));
            }
        }).addOnFailureListener(e -> {
            Log.e("DatabaseManager", "Error getting document", e);
        });
        return data;
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
            callback.onFetchComplete(new ArrayList<>()); // Return empty list on failure
        });
    }
}