package com.meliodas.plantitotita.mainmodule;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.Console;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import okhttp3.*;

public class PlantIdApi {
    private final OkHttpClient client;
    private final String apiKey;
    private String plantIdApiUrl;

    public PlantIdApi() {
        client = new OkHttpClient().newBuilder().build();
        apiKey = "6OiCRxZTV4lbFrYORUSyYJSGtVGSXusAOPGUjqj01GEd25XfIu";  // Initially used for the identification
        plantIdApiUrl = "https://plant.id/api/v3/identification";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Plant identifyAndGetDetails(byte[] imageData, double latitude, double longitude) throws IOException, JSONException {
        plantIdApiUrl = "https://plant.id/api/v3/identification";
        String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageData);

        JSONObject jsonBody = new JSONObject()
                .put("images", new JSONArray().put(base64Image))
                .put("latitude", latitude)
                .put("longitude", longitude)
                .put("similar_images", true);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());

        Request request = new Request.Builder()
                .url(plantIdApiUrl)
                .post(body)
                .addHeader("Api-Key", apiKey)  // Use API key for the first request
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.e("API Error", "Unexpected code " + response + " Response Body: " + responseBody);
                throw new IOException("Unexpected code " + response + " Response Body: " + responseBody);
            }

            // Read the body once and process it
            String responseBody = response.body().string();

            StringUtils.largeLog("API Response", responseBody);

            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject result = jsonResponse.getJSONObject("result");

            if (result.getJSONObject("is_plant").getDouble("probability") < 0.5) {
                throw new IOException("The image does not contain a plant");
            }

            // Extract the plant ID from the first suggestion in the classification array
            JSONObject suggestion = result.getJSONObject("classification").getJSONArray("suggestions").getJSONObject(0);
            String name = suggestion.getString("name");

            // Fetch plant details using the plant ID
            return getPlantDetailFromAccessToken(searchPlantByName(name));
        } catch (IOException e) {
            Log.e("API Error", "Error occurred while making API request", e);
            throw e;
        }
    }

    // Method to search plant by name and return the access token for plant details
    public String searchPlantByName(String plantName) throws IOException, JSONException {
        plantIdApiUrl = "https://plant.id/api/v3/kb/plants/name_search?q=" + plantName;

        Request request = new Request.Builder()
                .url(plantIdApiUrl)
                .get()
                .addHeader("Api-Key", apiKey)  // Use API key for authorization
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.e("API Error", "Unexpected code " + response + " Response Body: " + responseBody);
                throw new IOException("Unexpected code " + response + " Response Body: " + responseBody);
            }

            // Read the response body and extract access token
            String responseBody = response.body().string();
            Log.i("Plant Search Response", "Response Body: " + responseBody);
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Ensure at least one plant is found and an access token is available
            JSONArray plantsArray = jsonResponse.getJSONArray("entities");
            if (plantsArray.length() == 0) {
                throw new IOException("No plants found for query: " + plantName);
            }

            JSONObject firstPlant = plantsArray.getJSONObject(0);
            String accessToken = firstPlant.getString("access_token");

            Log.d("AccessToken", "Access token from plant search: " + accessToken);
            return accessToken;
        } catch (IOException e) {
            Log.e("API Error", "Error occurred while making API request", e);
            throw e;
        }
    }

    // Method to fetch plant details using the access token
    public Plant getPlantDetailFromAccessToken(String accessToken) throws IOException, JSONException {
        plantIdApiUrl = "https://plant.id/api/v3/kb/plants/" + accessToken +
                "?details=common_names,url,description,taxonomy,rank,gbif_id,inaturalist_id,image,synonyms,edible_parts,watering,toxicity,cultural_significance,propagation_methods,best_watering,best_light_condition,best_soil_type,common_uses&language=en";

        Request request = new Request.Builder()
                .url(plantIdApiUrl)
                .get()
                .addHeader("Api-Key", apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.e("API Error", "Unexpected code " + response + " Response Body: " + responseBody);
                throw new IOException("Unexpected code " + response + " Response Body: " + responseBody);
            }

            // Read the body once and process it
            String responseBody = response.body().string();

            StringUtils.largeLog("API Response", responseBody);

            JSONObject plantData = new JSONObject(responseBody);

            String scientificName = "";
            if (plantData.has("name")) {
                scientificName = plantData.getString("name");
            }

            String family = "";
            if (plantData.getJSONObject("taxonomy").has("family")) {
                family = plantData.getJSONObject("taxonomy").getString("family");
            }

            String phylum = "";
            if (plantData.getJSONObject("taxonomy").has("phylum")) {
                phylum = plantData.getJSONObject("taxonomy").getString("phylum");
            }

            String kingdom = "";
            if (plantData.getJSONObject("taxonomy").has("kingdom")) {
                kingdom = plantData.getJSONObject("taxonomy").getString("kingdom");
            }

            String order = "";
            if (plantData.getJSONObject("taxonomy").has("order")) {
                order = plantData.getJSONObject("taxonomy").getString("order");
            }

            String rank = "";
            if (plantData.getJSONObject("taxonomy").has("rank")) {
                rank = plantData.getJSONObject("taxonomy").getString("rank");
            }

            String genus = "";
            if (plantData.getJSONObject("taxonomy").has("genus")) {
                genus = plantData.getJSONObject("taxonomy").getString("genus");
            }

            String plantClass = "";
            if (plantData.getJSONObject("taxonomy").has("class")) {
                plantClass = plantData.getJSONObject("taxonomy").getString("class");
            }

            String image = plantData.getJSONObject("image").getString("value");
            String bestLightCondition = plantData.getString("best_light_condition");
            String bestWatering = plantData.getString("best_watering");
            String bestSoilType = plantData.getString("best_soil_type");
            String commonUses = plantData.getString("common_uses");
            String toxicity = plantData.getString("toxicity");
            String culturalSignificance = plantData.getString("cultural_significance");

            HashMap<String, String> taxonomy = new HashMap<>();
            taxonomy.put("rank", rank);
            taxonomy.put("genus", genus);
            taxonomy.put("family", family);
            taxonomy.put("order", order);
            taxonomy.put("class", plantClass);
            taxonomy.put("phylum", phylum);
            taxonomy.put("kingdom", kingdom);

            String description;
            if (plantData.isNull("description")) {
                description = "No description available";
            } else {
                JSONObject descriptionObject = plantData.getJSONObject("description");
                description = descriptionObject.isNull("value") ? "No description available" : descriptionObject.getString("value");
            }

            String wikiUrl = plantData.getString("url");

            List<String> commonNames = new ArrayList<>();
            String commonName = "";

            if (plantData.has("common_names")) {
                JSONArray commonNamesArray = plantData.getJSONArray("common_names");

                for (int i = 0; i < commonNamesArray.length(); i++) {
                    commonNames.add(commonNamesArray.getString(i));
                }

                commonName = commonNames.isEmpty() ? "No Common Name" : commonNames.get(0);
            }

            ArrayList<String> edibleParts = new ArrayList<>();
            if (plantData.isNull("edible_parts")) {
                edibleParts.add("No edible parts available");
            } else {
                JSONArray ediblePartsJson = plantData.getJSONArray("edible_parts");
                for (int i = 0; i < ediblePartsJson.length(); i++) {
                    edibleParts.add(ediblePartsJson.getString(i));
                }
            }

            ArrayList<String> propagationMethods = new ArrayList<>();
            if (plantData.isNull("propagation_methods")) {
                propagationMethods.add("No propagation methods available");
            } else {
                JSONArray propagationMethodsJson = plantData.getJSONArray("propagation_methods");
                for (int i = 0; i < propagationMethodsJson.length(); i++) {
                    propagationMethods.add(propagationMethodsJson.getString(i));
                }
            }

            return new Plant.Builder()
                    .identification(accessToken)
                    .name(commonName)
                    .scientificName(scientificName)
                    .family(family)
                    .genus(genus)
                    .taxonomy(taxonomy)
                    .image(image)
                    .description(description)
                    .wikiUrl(wikiUrl)
                    .commonNames(commonNames)
                    .edibleParts(edibleParts)
                    .propagationMethods(propagationMethods)
                    .bestLightCondition(bestLightCondition)
                    .bestSoilType(bestSoilType)
                    .bestWatering(bestWatering)
                    .toxicity(toxicity)
                    .culturalSignificance(culturalSignificance)
                    .commonUses(commonUses)
                    .build();
        }
    }

    public Plant searchAndGetPlantDetails(String plantName, String plantScientificName) throws IOException, JSONException {
        String accessToken;
        try {
            accessToken = searchPlantByName(plantName);
        } catch (IOException e) {
            return searchAndGetPlantDetails(plantScientificName);
        }
        return getPlantDetailFromAccessToken(accessToken);
    }

    public Plant searchAndGetPlantDetails(String plantScientificName) throws IOException, JSONException {
        String accessToken = searchPlantByName(plantScientificName);

        return getPlantDetailFromAccessToken(accessToken);
    }

    public List<HashMap<String, String>> searchAndGetAccessTokens(String plantName) throws IOException, JSONException {
        plantIdApiUrl = "https://plant.id/api/v3/kb/plants/name_search?q=" + plantName + "&thumbnails=true";

        Request request = new Request.Builder()
                .url(plantIdApiUrl)
                .get()
                .addHeader("Api-Key", apiKey)  // Use API key for authorization
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.e("API Error", "Unexpected code " + response + " Response Body: " + responseBody);
                throw new IOException("Unexpected code " + response + " Response Body: " + responseBody);
            }

            // Read the response body and extract access token
            String responseBody = response.body().string();
            Log.i("Plant Search Response", "Response Body: " + responseBody);
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Ensure at least one plant is found and an access token is available
            JSONArray plantsArray = jsonResponse.getJSONArray("entities");
            if (plantsArray.length() == 0) {
                throw new IOException("No plants found for query: " + plantName);
            }

            List<HashMap<String, String>> accessTokens = new ArrayList<>();
            for (int i = 0; i < plantsArray.length(); i++) {
                JSONObject plant = plantsArray.getJSONObject(i);
                HashMap<String, String> plantDetails = new HashMap<>();
                plantDetails.put("name", plant.getString("entity_name"));
                plantDetails.put("access_token", plant.getString("access_token"));
                plantDetails.put("matched_in_type", plant.getString("matched_in_type"));
                plantDetails.put("image", plant.getString("thumbnail"));
                accessTokens.add(plantDetails);
            }

            return accessTokens;
        } catch (IOException e) {
            Log.e("API Error", "Error occurred while making API request", e);
            throw e;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public HealthIdentification identifyHealth(byte[] imageData, double longitude, double latitude) throws IOException, JSONException {
        plantIdApiUrl = "https://plant.id/api/v3/health_assessment?details=local_name,description,url,treatment,classification,common_names,cause";
        String base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageData);

        JSONObject jsonBody = new JSONObject()
                .put("images", new JSONArray().put(base64Image))
                .put("longitude", longitude)
                .put("similar_images", true);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());

        Request request = new Request.Builder()
                .url(plantIdApiUrl)
                .post(body)
                .addHeader("Api-Key", apiKey)  // Use API key for the first request
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                Log.e("API Error", "Unexpected code " + response + " Response Body: " + responseBody);
                throw new IOException("Unexpected code " + response + " Response Body: " + responseBody);
            }

            // Read the body once and process it
            String responseBody = response.body().string();

            StringUtils.largeLog("API Response", responseBody);

            Plant plant = identifyAndGetDetails(imageData, latitude, longitude);

            JSONObject jsonResponse = new JSONObject(responseBody);
            return new HealthIdentification.Builder()
                    .result(HealthIdentificationParser.parseHealthIdentification(jsonResponse).getResult())
                    .plant(plant)
                    .build();

        } catch (Exception e) {
            Log.e("API Error", "Error occurred while parsing JSON response", e);
            try {
                throw e;
            } catch (ExecutionException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}