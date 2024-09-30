package com.meliodas.plantitotita.mainmodule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class HealthIdentificationParser {

    public static HealthIdentification parseHealthIdentification(JSONObject jsonResponse) throws JSONException, ExecutionException, InterruptedException {
        JSONObject result = jsonResponse.optJSONObject("result");
        if (result == null) {
            throw new JSONException("Result object is missing");
        }

        // Parse isPlant
        JSONObject isPlantJson = result.optJSONObject("is_plant");
        HealthIdentification.Result.IsPlant isPlant = null;
        if (isPlantJson != null) {
            isPlant = new HealthIdentification.Result.IsPlant.Builder()
                    .probability(isPlantJson.optDouble("probability", 0.0))
                    .threshold(isPlantJson.optDouble("threshold", 0.0))
                    .binary(isPlantJson.optBoolean("binary", false))
                    .build();
        }

        // Parse isHealthy
        JSONObject isHealthyJson = result.optJSONObject("is_healthy");
        HealthIdentification.Result.IsHealthy isHealthy = null;
        if (isHealthyJson != null) {
            isHealthy = new HealthIdentification.Result.IsHealthy.Builder()
                    .binary(isHealthyJson.optBoolean("binary", false))
                    .threshold(isHealthyJson.optDouble("threshold", 0.0))
                    .probability(isHealthyJson.optDouble("probability", 0.0))
                    .build();
        }

        // Parse disease suggestions
        JSONObject diseaseJson = result.optJSONObject("disease");
        List<HealthIdentification.Result.Disease.Suggestion> suggestions = new ArrayList<>();
        if (diseaseJson != null) {
            JSONArray suggestionsJson = diseaseJson.optJSONArray("suggestions");
            if (suggestionsJson != null) {
                for (int i = 0; i < suggestionsJson.length(); i++) {
                    JSONObject suggestionJson = suggestionsJson.optJSONObject(i);
                    if (suggestionJson != null) {
                        // Parse similar images
                        JSONArray similarImagesJson = suggestionJson.optJSONArray("similar_images");
                        List<HealthIdentification.Result.Disease.Suggestion.SimilarImage> similarImages = new ArrayList<>();
                        if (similarImagesJson != null) {
                            for (int j = 0; j < similarImagesJson.length(); j++) {
                                JSONObject similarImageJson = similarImagesJson.optJSONObject(j);
                                if (similarImageJson != null) {
                                    HealthIdentification.Result.Disease.Suggestion.SimilarImage similarImage = new HealthIdentification.Result.Disease.Suggestion.SimilarImage.Builder()
                                            .id(similarImageJson.optString("id", ""))
                                            .url(similarImageJson.optString("url", ""))
                                            .similarity(similarImageJson.optDouble("similarity", 0.0))
                                            .urlSmall(similarImageJson.optString("url_small", ""))
                                            .build();
                                    similarImages.add(similarImage);
                                }
                            }
                        }

                        // Parse details
                        JSONObject detailsJson = suggestionJson.optJSONObject("details");
                        HealthIdentification.Result.Disease.Suggestion.Details details = null;
                        if (detailsJson != null) {
                            details = new HealthIdentification.Result.Disease.Suggestion.Details.Builder()
                                    .localName(detailsJson.optString("local_name", ""))
                                    .description(detailsJson.optString("description", ""))
                                    .url(detailsJson.optString("url", null))
                                    .classification(jsonArrayToList(detailsJson.optJSONArray("classification")))
                                    .commonNames(jsonArrayToList(detailsJson.optJSONArray("common_names")))
                                    .cause(detailsJson.optString("cause", null))
                                    .language(detailsJson.optString("language", ""))
                                    .entityId(detailsJson.optString("entity_id", ""))
                                    .treatment(parseTreatment(detailsJson.optJSONObject("treatment")))
                                    .build();
                        }

                        // Build suggestion
                        HealthIdentification.Result.Disease.Suggestion suggestion = new HealthIdentification.Result.Disease.Suggestion.Builder()
                                .id(suggestionJson.optString("id", ""))
                                .name(suggestionJson.optString("name", ""))
                                .probability(suggestionJson.optDouble("probability", 0.0))
                                .similarImages(similarImages)
                                .details(details)
                                .build();

                        suggestions.add(suggestion);
                    }
                }
            }
        }

        // Build disease
        HealthIdentification.Result.Disease disease = new HealthIdentification.Result.Disease.Builder()
                .suggestions(suggestions)
                .build();

        // Build result
        HealthIdentification.Result resultObj = new HealthIdentification.Result.Builder()
                .isPlant(isPlant)
                .isHealthy(isHealthy)
                .disease(disease)
                .build();

        // Build HealthIdentification
        return new HealthIdentification.Builder()
                .result(resultObj)
                .build();
    }

    private static List<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.optString(i, ""));
            }
        }
        return list;
    }

    private static HealthIdentification.Result.Disease.Suggestion.Details.Treatment parseTreatment(JSONObject treatmentJson) throws JSONException {
        if (treatmentJson == null) {
            return null;
        }
        return new HealthIdentification.Result.Disease.Suggestion.Details.Treatment.Builder()
                .prevention(jsonArrayToList(treatmentJson.optJSONArray("prevention")))
                .biological(jsonArrayToList(treatmentJson.optJSONArray("biological")))
                .chemical(jsonArrayToList(treatmentJson.optJSONArray("chemical")))
                .build();
    }
}