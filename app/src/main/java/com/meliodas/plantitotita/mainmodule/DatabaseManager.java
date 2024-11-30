package com.meliodas.plantitotita.mainmodule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DatabaseManager {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public DocumentReference getDocumentReference(String collection, String document) {
        return fStore.collection(collection).document(document);
    }

    public DocumentReference getUserDoc(String document) {
        return getDocumentReference("users", document);
    }

    public void hasUserDoc(String userId, BooleanCallback callback) {
        getDocumentReference("users", userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onCallback(documentSnapshot.exists());
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseManager", "Error getting document", e);
                    callback.onCallback(false);
                });
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

    public void addHealthAssessment(HealthIdentification healthIdentification, String userId, String imageUrl, Runnable onComplete) {
        DocumentReference userDoc = getUserDoc(userId);

        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            List<Map<String, Object>> data;

            // Check if the document exists and contains the "plant_health_assessments" field
            if (documentSnapshot.exists() && documentSnapshot.contains("plant_health_assessments")) {
                data = (List<Map<String, Object>>) documentSnapshot.get("plant_health_assessments");
            } else {
                data = new ArrayList<>();
            }

            // Create a map for health data and add the image URL
            Map<String, Object> healthData = getHealthData(healthIdentification);
            healthData.put("image", imageUrl);

            // Add the new health data to the existing list
            data.add(healthData);

            // Update the user document with the new data
            userDoc.set(new HashMap<String, Object>() {{
                        put("plant_health_assessments", data);
                    }}, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        // Notify that the operation is complete
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Log error and notify failure
                        Log.e("DatabaseManager", "Error updating document", e);
                        if (onComplete != null) {
                            onComplete.run(); // Call onComplete even on failure
                        }
                    });

        }).addOnFailureListener(e -> {
            // Log error when fetching document fails
            Log.e("DatabaseManager", "Error getting document", e);
            if (onComplete != null) {
                onComplete.run(); // Call onComplete even on failure
            }
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
        plantData.put("taxonomy", plant.taxonomy());
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

    public void deleteReminder(String id, VoidCallback callback) {
        fStore.collection("reminders").document(id)
                .delete().addOnSuccessListener(
                        (unused) -> {
                            callback.onCallback();
                        });
    }

    public interface UploadCallback {
        void onUploadComplete(String imageUrl);
    }

    public void saveReminder(Reminder reminder, Context context, VoidCallback callback) {
        String id = fStore.collection("reminders").document().getId();
        reminder.setId(id);

        Map<String, Object> reminderMap = new HashMap<>();
        reminderMap.put("id", reminder.getId());
        reminderMap.put("title", reminder.getTitle());
        reminderMap.put("task", reminder.getTask());
        reminderMap.put("plant", reminder.getPlant());
        reminderMap.put("image", reminder.getImage());
        reminderMap.put("timeInMillis", reminder.getTimeInMillis());
        reminderMap.put("repeat", reminder.isRepeat());
        reminderMap.put("repeatDays", reminder.getRepeatDays());
        reminderMap.put("userId", FirebaseAuth.getInstance().getUid());

        fStore.collection("reminders").document(id)
                .set(reminderMap)
                .addOnSuccessListener(unused -> {
                    callback.onCallback();
                    scheduleNotification(reminder, context);
                })
                .addOnFailureListener(e -> Log.e("SaveReminder", "Error saving reminder", e));
    }

    private void scheduleNotification(Reminder reminder, Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("message", reminder.getTask());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.getTimeInMillis(), pendingIntent);
            } else {
                Log.e("DatabaseManager", "Cannot schedule exact alarms");
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.getTimeInMillis(), pendingIntent);
        }
    }

    public void getRemindersForDate(String userId, long dateInMillis, FetchDataCallback callback) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        fStore.collection("reminders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> data = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> reminderData = document.getData();
                        boolean isRepeat = (boolean) reminderData.get("repeat");
                        if (isRepeat) {
                            List<String> repeatDays = (List<String>) reminderData.get("repeatDays");
                            if (repeatDays != null && repeatDays.contains(getDayName(dayOfWeek))) {
                                data.add(reminderData);
                            }
                        } else {
                            long reminderTimeInMillis = (long) reminderData.get("timeInMillis");
                            Calendar reminderCalendar = Calendar.getInstance();
                            reminderCalendar.setTimeInMillis(reminderTimeInMillis);
                            if (isSameDay(calendar, reminderCalendar)) {
                                data.add(reminderData);
                            }
                        }
                    }
                    callback.onFetchComplete(data);
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseManager", "Error getting reminders", e);
                });
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String getDayName(int dayOfWeek) {
        return switch (dayOfWeek) {
            case Calendar.MONDAY -> "Monday";
            case Calendar.TUESDAY -> "Tuesday";
            case Calendar.WEDNESDAY -> "Wednesday";
            case Calendar.THURSDAY -> "Thursday";
            case Calendar.FRIDAY -> "Friday";
            case Calendar.SATURDAY -> "Saturday";
            case Calendar.SUNDAY -> "Sunday";
            default -> "";
        };
    }
}
