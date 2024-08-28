package com.meliodas.plantitotita.fragments;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.meliodas.plantitotita.R;

public class PlantGalleryFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_gallery, container, false);

        LinearLayout plantGalleryLayout = view.findViewById(R.id.plantGalleryLayout);

        for (int i = 0; i < 10; i++) {
            plantGalleryLayout.addView(createPlantGalleryItem(inflater, plantGalleryLayout, "Plant " + i, R.drawable.custom_plant_gallery_container));
        }

        return view;
    }

    public View createPlantGalleryItem(LayoutInflater inflater, ViewGroup container, String plantName, @DrawableRes int plantImage) {
        View plantGalleryItem = inflater.inflate(R.layout.custom_container_plantgallery, container, false);

        TextView plantNameTextView = plantGalleryItem.findViewById(R.id.plantIDName);
        plantNameTextView.setText(plantName);

        ShapeableImageView plantImageView = plantGalleryItem.findViewById(R.id.plantGalleryContainerImgView);
        plantImageView.setImageResource(plantImage);

        return plantGalleryItem;
    }
}