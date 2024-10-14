package com.meliodas.plantitotita.fragments;

import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.Plant;
import com.meliodas.plantitotita.mainmodule.PlantIdApi;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class PlantSearchResultsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plant_search_results, container, false);
        LinearLayout plantGallery = view.findViewById(R.id.plantSearchLayout);

        Bundle bundle = getArguments();
        assert bundle != null;
        String searchQuery = bundle.getString("searchQuery") != null ? bundle.getString("searchQuery") : "No search query found";

        new Thread(() -> {
            PlantIdApi plantIdApi = new PlantIdApi();
            try {
                List<HashMap<String, String>> plants = plantIdApi.searchAndGetAccessTokens(searchQuery);
                for (HashMap<String, String> plant : plants) {
                    getActivity().runOnUiThread(() -> plantGallery.addView(resultView(plant.get("name"), plant.get("matched_in_type"), plant.get("access_token"), plant.get("image"))));
                }
            } catch (IOException e) {
                if (e.getMessage().startsWith("No plants found for query")) {
                    getActivity().runOnUiThread(() -> {
                        // TODO - Add a TextView to the actual layout instead of programatically creating one
                        TextView noResults = new TextView(getContext());
                        noResults.setText("No results found for query: " + searchQuery);
                        plantGallery.addView(noResults);
                    });
                } else {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

        return view;
    }

    private View resultView(String plantName, String matchType, String accessToken, String image) {
        View view = getLayoutInflater().inflate(R.layout.custom_container_plantgallery, null);
        TextView plantNameTextView = view.findViewById(R.id.plantIDName);
        TextView plantScientificNameTextView = view.findViewById(R.id.plantIDScientificName);
        ShapeableImageView plantImageView = view.findViewById(R.id.plantGalleryContainerImgView);

        plantNameTextView.setText(plantName);
        plantScientificNameTextView.setText("");

        Glide.with(getContext()).load(Base64.decode(image, Base64.DEFAULT)).into(plantImageView);

        view.setOnClickListener(v -> {
            PlantIdApi plantIdApi = new PlantIdApi();

            new Thread(() -> {
                Plant plant = null;
                try {
                    plant = plantIdApi.getPlantDetailFromAccessToken(accessToken);
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }

                Bundle bundle = new Bundle();
                bundle.putString("plantName", plant.name());
                bundle.putString("plantScientificName", plant.scientificName());
                bundle.putString("plantDescription", plant.description());
                bundle.putString("plantImageUrl", plant.image());
                bundle.putStringArrayList("edibleParts", plant.edibleParts());
                bundle.putStringArrayList("propagationMethods", plant.propagationMethods());
                bundle.putString("commonUses", plant.commonUses());
                bundle.putString("culturalSignificance", plant.culturalSignificance());
                bundle.putString("toxicity", plant.toxicity());
                bundle.putString("bestLightCondition", plant.bestLightCondition());
                bundle.putString("bestSoilType", plant.bestSoilType());
                bundle.putString("bestWatering", plant.bestWatering());
                bundle.putSerializable("taxonomy", plant.taxonomy());
                bundle.putBoolean("allowSaveToGallery", true);

                Fragment targetFragment = new PlantInformationFragment();
                targetFragment.setArguments(bundle);
                getActivity().runOnUiThread(() -> {
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.frameLayout, targetFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                });
            }).start();

        });

        return view;
    }

    private String cleanMatchType(String matchType) {
        return switch (matchType) {
            case "common_name", "entity_name" -> "Common Name";
            case "scientific_name" -> "Scientific Name";
            case "family" -> "Family";
            case "genus" -> "Genus";
            default -> "Unknown";
        };
    }
}