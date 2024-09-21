package com.meliodas.plantitotita.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.imageview.ShapeableImageView;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.HomePage;

public class PlantGalleryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_gallery, container, false);

        LinearLayout plantGalleryLayout = view.findViewById(R.id.plantGalleryLayout);
        int i;

        for (i = 0; i < 10; i++) {
            plantGalleryLayout.addView(createPlantGalleryItem(inflater, plantGalleryLayout, "Plant " + i, R.drawable.sad));
        }

        return view;
    }

    public View createPlantGalleryItem(LayoutInflater inflater, ViewGroup container, String plantName, @DrawableRes int plantImage) {
        View plantGalleryItem = inflater.inflate(R.layout.custom_container_plantgallery, container, false);

        TextView plantNameTextView = plantGalleryItem.findViewById(R.id.plantIDName);
        plantNameTextView.setText(plantName);

        ShapeableImageView plantImageView = plantGalleryItem.findViewById(R.id.plantGalleryContainerImgView);
        plantImageView.setImageResource(plantImage);

        plantGalleryItem.setOnClickListener(v -> {
            Fragment targetFragment = new PlantInformationFragment();

            // Pass data to the new fragment if needed
            Bundle args = new Bundle();
            args.putString("plantName", plantName);
            targetFragment.setArguments(args);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return plantGalleryItem;
    }
}