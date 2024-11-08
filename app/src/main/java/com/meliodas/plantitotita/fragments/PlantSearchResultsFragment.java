package com.meliodas.plantitotita.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
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

    private TextView noResults;
    private AlertDialog noInternetDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plant_search_results, container, false);
        LinearLayout plantGallery = view.findViewById(R.id.plantSearchLayout);
        noResults = view.findViewById(R.id.noResultsTxtView);

        Bundle bundle = getArguments();
        assert bundle != null;
        String searchQuery = bundle.getString("searchQuery") != null ? bundle.getString("searchQuery") : "No search query found";

        // Check internet connection before making API call
        if (isInternetAvailable()) {
            new Thread(() -> {
                PlantIdApi plantIdApi = new PlantIdApi();
                try {
                    List<HashMap<String, String>> plants = plantIdApi.searchAndGetAccessTokens(searchQuery);
                    for (HashMap<String, String> plant : plants) {
                        getActivity().runOnUiThread(() -> plantGallery.addView(resultView(plant.get("name"), plant.get("access_token"), plant.get("image"))));
                    }
                } catch (IOException e) {
                    if (e.getMessage().startsWith("No plants found for query")) {
                        getActivity().runOnUiThread(() -> {
                            noResults.setText("No results found for query: " + "\n" + searchQuery);
                        });
                    } else {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showNoInternetDialog();
        }

        return view;
    }

    private View resultView(String plantName, String accessToken, String image) {
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

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
            HomePageFragment fragment = new HomePageFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment) // Replace with your fragment container ID
                    .addToBackStack(null)
                    .commit();
        });

        noInternetDialog.show();
    }
}