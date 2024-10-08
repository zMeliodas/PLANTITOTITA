package com.meliodas.plantitotita.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.DatabaseManager;
import com.meliodas.plantitotita.mainmodule.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;


public class PlantHealthAssessmentInformationFragment extends Fragment {

    private TextView plantName;
    private TextView scientificName;
    private TextView biologicalTreatments;
    private TextView chemicalTreatments;
    private TextView prevention;
    private TextView diseaseTitle;
    private TextView diseaseDescription;
    private ImageView plantImage;
    private DatabaseManager dbManager;
    private static String imageViewPhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_plant_health_assessment_information, container, false);
        View backButton = view.findViewById(R.id.btnReturnPlantHealthAssessmentInfo);
        backButton.setOnClickListener(v -> {
            PlantHealthAssessmentGalleryFragment plantGalleryFragment = new PlantHealthAssessmentGalleryFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, plantGalleryFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        plantName = view.findViewById(R.id.plantInformationName);
        scientificName = view.findViewById(R.id.plantInformationScientificName);
        plantImage = view.findViewById(R.id.plantInfoImage);
        biologicalTreatments = view.findViewById(R.id.biologicalTreatment);
        chemicalTreatments = view.findViewById(R.id.chemicalTreatment);
        prevention = view.findViewById(R.id.prevention);
        diseaseTitle = view.findViewById(R.id.diseaseTitle);
        diseaseDescription = view.findViewById(R.id.diseaseDescription);

        dbManager = new DatabaseManager();

        Bundle args = getArguments();

        if (args != null) {
            String name = args.getString("plantName", "");
            String sciName = args.getString("plantScientificName", "");
            String imageUrl = args.getString("plantImage", "");

            plantName.setText(name);
            scientificName.setText(sciName);

            if (!imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.aaboutus)
                        .error(R.drawable.custom_dialog_layout_error_icon)
                        .into(plantImage);

                imageViewPhoto = imageUrl;
                plantImage.setOnClickListener(v -> showResourceImagePopup(requireContext(), imageViewPhoto));
            }
            loadHealthAssessmentData(imageUrl);
        }

        return view;
    }

    private void loadHealthAssessmentData(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbManager.getHealthAssessments(userId, healthAssessments -> {
            for (Map<String, Object> assessment : healthAssessments) {
                if (imageUrl.equals(assessment.get("image"))) {
                    updateTreatmentInfo(assessment);
                    break;
                }
            }
        });
    }

    private void updateTreatmentInfo(Map<String, Object> assessment) {
        try {
            List<Map<String, Object>> diseaseSuggestions = (List<Map<String, Object>>) assessment.get("diseaseSuggestions");
            if (diseaseSuggestions != null && !diseaseSuggestions.isEmpty()) {
                Map<String, Object> suggestion = diseaseSuggestions.get(0);
                Map<String, Object> details = (Map<String, Object>) suggestion.get("details");
                if (details != null) {
                    StringUtils.updateTextView(diseaseTitle, details.get("localName"));
                    StringUtils.updateTextView(diseaseDescription, details.get("description"));

                    Map<String, Object> treatment = (Map<String, Object>) details.get("treatment");
                    if (treatment != null) {
                        StringUtils.updateTextView(biologicalTreatments, treatment.get("biological"));
                        StringUtils.updateTextView(chemicalTreatments, treatment.get("chemical"));
                        StringUtils.updateTextView(prevention, treatment.get("prevention"));
                    }
                }
            }
        } catch (ClassCastException e) {
            Log.e("PlantHealthAssessment", "Error parsing assessment data", e);
        }
    }

    public static void showResourceImagePopup(Context context, String imageResource) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        View popupView = LayoutInflater.from(context).inflate(R.layout.image_popup_template, null);
        ImageView popupImageView = popupView.findViewById(R.id.imageView88);

        Picasso.get().load(imageResource).into(popupImageView);
        AlertDialog dialog = adb.create();

        if (imageViewPhoto == null || imageViewPhoto.isEmpty()) {
            return;
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setView(popupView);
        dialog.show();
    }
}