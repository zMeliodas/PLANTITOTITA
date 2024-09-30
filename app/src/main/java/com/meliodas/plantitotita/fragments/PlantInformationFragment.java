package com.meliodas.plantitotita.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.meliodas.plantitotita.R;

public class PlantInformationFragment extends Fragment {

    private TextView plantName;
    private TextView scientificName;
    private TextView description;
    private ImageView plantImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_information, container, false);

        View backButton = view.findViewById(R.id.btnReturnPlantInfo);
        backButton.setOnClickListener(v -> {
            PlantGalleryFragment plantGalleryFragment = new PlantGalleryFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, plantGalleryFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        plantName = view.findViewById(R.id.plantInformationName);
        scientificName = view.findViewById(R.id.plantInformationScientificName);
        description = view.findViewById(R.id.plantInformationDescription);
        plantImage = view.findViewById(R.id.plantInfoImage);

        Bundle args = getArguments();
        if (args != null) {
            String name = args.getString("plantName", "");
            String sciName = args.getString("scientificName", "");
            String desc = args.getString("description", "");
            String imageUrl = args.getString("image", "");
            String edibleParts = args.getString("edibleParts", "");

            plantName.setText(name);
            scientificName.setText(sciName);
            description.setText(desc);

            if (!imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.aaboutus)
                        .error(R.drawable.custom_dialog_layout_error_icon)
                        .into(plantImage);
            }
        }

        return view;
    }
}