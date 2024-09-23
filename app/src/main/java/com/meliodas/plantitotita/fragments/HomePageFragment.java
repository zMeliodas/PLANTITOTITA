package com.meliodas.plantitotita.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.DatabaseManager;
import com.meliodas.plantitotita.mainmodule.Plant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        recentScansContainer.removeAllViews(); // Clear existing views

        for (Map<String, Object> scan : recentScans) {
            Plant plant = new Plant(
                    (String) scan.get("identification"),
                    (String) scan.getOrDefault("name", "Unknown Plant"),
                    (String) scan.get("scientificName"),
                    (String) scan.get("family"),
                    (String) scan.get("genus"),
                    (String) scan.get("image"),
                    (String) scan.get("description"),
                    (String) scan.get("wikiUrl")
            );
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

        // Load image using Picasso
        if (plant.image() != null && !plant.image().isEmpty()) {
            Picasso.get().load(plant.image()).into(imageView);
        } else {
            imageView.setImageResource(R.drawable.sad); // fallback image
        }

        // Set plant name and scientific name
        String displayName = plant.name();
        if (plant.scientificName() != null && !plant.scientificName().isEmpty()) {
            displayName += " (" + plant.scientificName() + ")";
        }
        nameView.setText(displayName);

        scanView.setOnClickListener(v -> {
            PlantInformationFragment plantInfoFragment = new PlantInformationFragment();
            Bundle args = new Bundle();
            args.putString("plantName", plant.name());
            args.putString("scientificName", plant.scientificName());
            args.putString("description", plant.description());
            args.putString("image", plant.image());
            plantInfoFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .add(R.id.frameLayout, plantInfoFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return scanView;
    }
}