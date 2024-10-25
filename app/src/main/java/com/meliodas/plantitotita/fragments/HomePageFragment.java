package com.meliodas.plantitotita.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.*;
import com.squareup.picasso.Picasso;

import java.util.*;

public class HomePageFragment extends Fragment {

    private TextView helloTxtView;
    private ViewGroup recentScansContainer;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore fStore;
    private DatabaseManager databaseManager;
    private LayoutInflater layoutInflater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);

        helloTxtView = view.findViewById(R.id.helloTxtView);
        recentScansContainer = view.findViewById(R.id.recentScansContainer);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        databaseManager = new DatabaseManager();
        layoutInflater = LayoutInflater.from(getContext());
        Button button1 = view.findViewById(R.id.getStartedContainerIcon1);
        Button button2 = view.findViewById(R.id.getStartedContainerIcon2);
        Button button3 = view.findViewById(R.id.getStartedContainerIcon3);
        ConstraintLayout button4 = view.findViewById(R.id.getStartedLayout1);
        ConstraintLayout button5 = view.findViewById(R.id.getStartedLayout2);
        ConstraintLayout button6 = view.findViewById(R.id.getStartedLayout3);

        EditText searchEditText = view.findViewById(R.id.editTxtSearch);

//        searchEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // Do nothing
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Do nothing
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String searchQuery = s.toString();
//
//            }
//        });

        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                String searchQuery = searchEditText.getText().toString();
                PlantSearchResultsFragment fragment = new PlantSearchResultsFragment();
                Bundle args = new Bundle();
                args.putString("searchQuery", searchQuery);
                fragment.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
            return false;
        });

        button1.setOnClickListener(v -> onClickGetStartedScanPlant());
        button2.setOnClickListener(v -> onClickGetStartedPlantGallery());
        button3.setOnClickListener(v -> onClickGetStartedProfileSetup());
        button4.setOnClickListener(v -> onClickGetStartedScanPlant());
        button5.setOnClickListener(v -> onClickGetStartedPlantGallery());
        button6.setOnClickListener(v -> onClickGetStartedProfileSetup());

        fetchUserName();
        fetchRecentScans();

        return view;
    }

    private void fetchUserName() {
        DocumentReference documentReference = fStore.collection("users").document(firebaseUser.getUid());
        if (getActivity() != null) {
            documentReference.addSnapshotListener(getActivity(), (value, error) -> {
                if (value != null) {
                    String displayName = value.getString("user_name");
                    helloTxtView.setText("Hello, " + displayName + "!");
                }
            });
        }
    }

    private void fetchRecentScans() {
        databaseManager.getPlantIdentifications(firebaseUser.getUid(), identifications -> {
            Collections.reverse(identifications);
            updateRecentScansUI(identifications);
        });
    }

    private void updateRecentScansUI(List<Map<String, Object>> recentScans) {
        int limit = Math.min(recentScans.size(), 3);
        recentScansContainer.removeAllViews(); // Clear existing views

        for (int i = 0; i < limit; i++) {
            Map<String, Object> scan = recentScans.get(i);
            Plant plant = new Plant.Builder()
                    .identification((String) scan.getOrDefault("identification", "Unknown Identification"))
                    .name((String) scan.getOrDefault("name", "Unknown Plant"))
                    .scientificName((String) scan.getOrDefault("scientificName", "Unknown Scientific Name"))
                    .family((String) scan.getOrDefault("family", "Unknown Family"))
                    .genus((String) scan.getOrDefault("genus", "Unknown Genus"))
                    .image((String) scan.getOrDefault("image", ""))
                    .description((String) scan.getOrDefault("description", "No Description"))
                    .wikiUrl((String) scan.getOrDefault("wikiUrl", ""))
                    .edibleParts((ArrayList<String>) scan.getOrDefault("edibleParts", new ArrayList<>()))
                    .propagationMethods((ArrayList<String>) scan.getOrDefault("propagationMethods", new ArrayList<>()))
                    .bestLightCondition((String) scan.getOrDefault("bestLightCondition", "Unknown Light Condition"))
                    .bestSoilType((String) scan.getOrDefault("bestSoilType", "Unknown Soil Type"))
                    .bestWatering((String) scan.getOrDefault("bestWatering", "Unknown Watering"))
                    .toxicity((String) scan.getOrDefault("toxicity", "Unknown Toxicity"))
                    .culturalSignificance((String) scan.getOrDefault("culturalSignificance", "Unknown Cultural Significance"))
                    .commonUses((String) scan.getOrDefault("commonUses", "Unknown Uses"))
                    .taxonomy((HashMap<String, String>) scan.getOrDefault("taxonomy", new HashMap<>()))
                    .build();

            View scanView = createScanView(plant);
            recentScansContainer.addView(scanView);
            TextView textView = new TextView(getContext());
            recentScansContainer.addView(textView);
        }

        TextView textView = new TextView(getContext());
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 100);
        textView.setLayoutParams(layoutParams);
        recentScansContainer.addView(textView);
    }

    private View createScanView(Plant plant) {
        View scanView = layoutInflater.inflate(R.layout.custom_recent_scans_layout, recentScansContainer, false);

        ShapeableImageView imageView = scanView.findViewById(R.id.plantGalleryContainerImgView);
        TextView nameView = scanView.findViewById(R.id.plantIDName);
        Typeface customFont = ResourcesCompat.getFont(requireContext(), R.font.mada_medium);

        // Load image using Picasso
        if (plant.image() != null && !plant.image().isEmpty()) {
            Picasso.get().load(plant.image()).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.sad); // fallback image
        }

        // Set plant name and scientific name
        String capitalizedName = StringUtils.capitalize(plant.name());
        String displayName = capitalizedName;
        if (plant.scientificName() != null && !plant.scientificName().isEmpty()) {
            String capitalizedScientificName = StringUtils.capitalize(plant.scientificName());
            displayName += " (" + capitalizedScientificName + ")";
        }

        // Set the custom font to the TextView
        nameView.setTypeface(customFont);
        nameView.setText(displayName);

        scanView.setOnClickListener(v -> {
            PlantInformationFragment plantInfoFragment = new PlantInformationFragment();
            Bundle args = new Bundle();
            args.putString("plantName", capitalizedName);
            args.putString("plantScientificName", StringUtils.capitalize(plant.scientificName()));
            args.putString("plantDescription", plant.description());
            args.putString("plantImageUrl", plant.image());
            args.putStringArrayList("edibleParts", plant.edibleParts());
            args.putStringArrayList("propagationMethods", plant.propagationMethods());
            args.putString("commonUses", plant.commonUses());
            args.putString("culturalSignificance", plant.culturalSignificance());
            args.putString("toxicity", plant.toxicity());
            args.putString("bestLightCondition", plant.bestLightCondition());
            args.putString("bestSoilType", plant.bestSoilType());
            args.putString("bestWatering", plant.bestWatering());
            args.putSerializable("taxonomy", new HashMap<>(plant.taxonomy()));
            plantInfoFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, plantInfoFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return scanView;
    }

    private void onClickGetStartedScanPlant() {
        Intent intent = new Intent(getActivity(), ArSceneActivity.class);
        startActivity(intent);
    }

    private void onClickGetStartedPlantGallery() {
        PlantGalleryFragment fragment = new PlantGalleryFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment) // Replace with your fragment container ID
                .addToBackStack(null)
                .commit();
    }

    private void onClickGetStartedProfileSetup() {
        Intent intent = new Intent(getActivity(), EditProfilePage.class);
        startActivity(intent);
    }
}