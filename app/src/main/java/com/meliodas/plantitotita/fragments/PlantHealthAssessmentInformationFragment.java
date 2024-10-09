package com.meliodas.plantitotita.fragments;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentTransaction;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
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
    private LinearLayout diseaseLayout, biologicalLayout, chemicalLayout, preventionLayout;
    private CardView diseaseCard, biologicalCard, chemicalCard, preventionCard;

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

        diseaseLayout = view.findViewById(R.id.layout1);
        biologicalLayout = view.findViewById(R.id.layout2);
        chemicalLayout = view.findViewById(R.id.layout3);
        preventionLayout = view.findViewById(R.id.layout4);

        diseaseCard = view.findViewById(R.id.cardView1);
        biologicalCard = view.findViewById(R.id.cardView2);
        chemicalCard = view.findViewById(R.id.cardView3);
        preventionCard = view.findViewById(R.id.cardView4);

        setOnClickListener(diseaseDescription, diseaseLayout, diseaseCard);
        setOnClickListener(biologicalTreatments, biologicalLayout, biologicalCard);
        setOnClickListener(chemicalTreatments, chemicalLayout, chemicalCard);
        setOnClickListener(prevention, preventionLayout, preventionCard);

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

    private void setOnClickListener(TextView textView, LinearLayout linearLayout, CardView cardViews) {
        linearLayout.setOnClickListener(v -> {
            expandView(textView, linearLayout);
        });

        cardViews.setOnClickListener(v -> {
            expandView(textView, linearLayout);
        });
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

    public void expandView(TextView description, LinearLayout layout) {
        int visibility = (description.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;

        // Create an AutoTransition for the description's visibility
        AutoTransition transition = new AutoTransition();

        // Set a longer duration for the expansion/collapse of the card
        transition.setDuration(300); // Increase this duration for a slower expansion

        // Use a smooth interpolator
        transition.setInterpolator(new FastOutSlowInInterpolator());

        // Disable layout transitions to prevent overlap issues during the animation
        layout.setLayoutTransition(null);

        // Add a listener to manage the layout transitions after the description animation
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                // Nothing to do at the start of the transition
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                // Re-enable layout transitions after the description's animation ends
                LayoutTransition layoutTransition = new LayoutTransition();

                layout.setLayoutTransition(layoutTransition);
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                // Handle cancellation if needed
            }

            @Override
            public void onTransitionPause(Transition transition) {
                // Handle pause if needed
            }

            @Override
            public void onTransitionResume(Transition transition) {
                // Handle resume if needed
            }
        });

        // Begin the transition for the description's visibility
        TransitionManager.beginDelayedTransition(layout, transition);
        description.setVisibility(visibility);
    }
}