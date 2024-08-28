package com.meliodas.plantitotita.fragments;

import android.os.Bundle;
import android.text.Layout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.meliodas.plantitotita.R;

public class PlantGalleryFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_gallery, container, false);
        // Inflate the layout for this fragment


        return view;
    }
}