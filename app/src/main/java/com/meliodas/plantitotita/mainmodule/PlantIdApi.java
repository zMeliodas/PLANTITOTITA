package com.meliodas.plantitotita.mainmodule;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.Console;
import java.io.IOException;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

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
            Log.i("API Response", "Response Body: " + responseBody);
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject result = jsonResponse.getJSONObject("result");

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

    public Plant getPlantDetailFromIdentification(String plantId) throws IOException, JSONException {
        plantIdApiUrl = "https://plant.id/api/v3/kb/plants/" + plantId +
                "?details=common_names,url,description,taxonomy,rank,gbif_id,inaturalist_id,image,synonyms,edible_parts,watering,propagation_methods&language=en";

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
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject plantData = jsonResponse.getJSONObject("result");

            String name = plantData.getString("name");
            String scientificName = plantData.getString("scientific_name");
            String family = plantData.getJSONObject("taxonomy").getString("family");
            String genus = plantData.getJSONObject("taxonomy").getString("genus");
            String image = plantData.getJSONObject("image").getString("value");
            String description = plantData.getJSONObject("description").getString("value");
            String wikiUrl = plantData.getJSONObject("url").getString("value");
            String edibleParts = plantData.getJSONObject("edible_parts").getString("value");

            List<String> commonNames = new ArrayList<>();
            JSONArray commonNamesArray = plantData.getJSONObject("common_names").getJSONArray("value");
            for (int i = 0; i < commonNamesArray.length(); i++) {
                commonNames.add(commonNamesArray.getString(i));
            }

            String commonName = commonNames.isEmpty() ? "No Common Name" : commonNames.get(0);

            return new Plant(plantId, commonName, scientificName, family, genus, image, description, wikiUrl, commonNames, edibleParts);
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
                "?details=common_names,url,description,taxonomy,rank,gbif_id,inaturalist_id,image,synonyms,edible_parts,watering,propagation_methods&language=en";

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

            Log.i("API Response", "Response Body: " + responseBody);

            JSONObject jsonResponse = new JSONObject(responseBody);

            String scientificName = jsonResponse.getString("name");
            String family = jsonResponse.getJSONObject("taxonomy").getString("family");
            String genus = jsonResponse.getJSONObject("taxonomy").getString("genus");
            String image = jsonResponse.getJSONObject("image").getString("value");

            String description;
            if (jsonResponse.isNull("description")) {
                description = "No description available";
            } else {
                JSONObject descriptionObject = jsonResponse.getJSONObject("description");
                description = descriptionObject.isNull("value") ? "No description available" : descriptionObject.getString("value");
            }

            String wikiUrl = jsonResponse.getString("url");

            List<String> commonNames = new ArrayList<>();
            JSONArray commonNamesArray = jsonResponse.getJSONArray("common_names");
            for (int i = 0; i < commonNamesArray.length(); i++) {
                commonNames.add(commonNamesArray.getString(i));
            }

            String commonName = commonNames.isEmpty() ? "No Common Name" : commonNames.get(0);

            String edibleParts = jsonResponse.getJSONObject("edible_parts").getString("value");

            return new Plant(accessToken, commonName, scientificName, family, genus, image, description, wikiUrl, commonNames,edibleParts);
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
}
