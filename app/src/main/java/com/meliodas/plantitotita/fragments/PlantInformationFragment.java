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
import android.widget.*;
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
import com.meliodas.plantitotita.mainmodule.Plant;
import com.meliodas.plantitotita.mainmodule.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.*;

public class PlantInformationFragment extends Fragment {

    private TextView plantName;
    private TextView scientificName;
    private TextView description;
    private ImageView plantImage;
    private TextView edibleParts;
    private TextView propagationMethods;
    private TextView commonUses;
    private TextView culturalSignificance;
    private TextView toxicity;
    private TextView bestLightCondition;
    private TextView bestSoilType;
    private TextView bestWatering;
    private TextView taxonomy;
    private static String imageViewPhoto;
    private LinearLayout layout1, layout2, layout3, layout4, layout5, layout6, layout7, layout8, layout9, layout10;
    private CardView cardView1, cardView2, cardView3, cardView4, cardView5, cardView6, cardView7, cardView8, cardView9, cardView10;
    private ImageView arrowImg, arrowImg1, arrowImg2, arrowImg3, arrowImg4, arrowImg5, arrowImg6, arrowImg7, arrowImg8, arrowImg9;

    private static final List<String> TAXONOMY_ORDER = Arrays.asList(
            "kingdom", "phylum", "class", "order", "family", "genus", "species"
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plant_information, container, false);

        View backButton = view.findViewById(R.id.btnReturnPlantInfo);
        Button saveButton = view.findViewById(R.id.btnSaveToGallery);

        plantImage = view.findViewById(R.id.plantInfoImage);
        plantName = view.findViewById(R.id.plantInformationName);
        scientificName = view.findViewById(R.id.plantInformationScientificName);

        description = view.findViewById(R.id.plantInformationDescription);
        edibleParts = view.findViewById(R.id.edibleParts);
        propagationMethods = view.findViewById(R.id.propagationMethods);
        commonUses = view.findViewById(R.id.commonUses);
        culturalSignificance = view.findViewById(R.id.culturalSignificance);
        toxicity = view.findViewById(R.id.toxicity);
        bestLightCondition = view.findViewById(R.id.bestLightCondition);
        bestSoilType = view.findViewById(R.id.bestSoilType);
        bestWatering = view.findViewById(R.id.bestWatering);
        taxonomy = view.findViewById(R.id.plantTaxonomy);

        layout1 = view.findViewById(R.id.layout1);
        layout2 = view.findViewById(R.id.layout2);
        layout3 = view.findViewById(R.id.layout3);
        layout4 = view.findViewById(R.id.layout4);
        layout5 = view.findViewById(R.id.layout5);
        layout6 = view.findViewById(R.id.layout6);
        layout7 = view.findViewById(R.id.layout7);
        layout8 = view.findViewById(R.id.layout8);
        layout9 = view.findViewById(R.id.layout9);
        layout10 = view.findViewById(R.id.layout10);

        cardView1 = view.findViewById(R.id.cardView1);
        cardView2 = view.findViewById(R.id.cardView2);
        cardView3 = view.findViewById(R.id.cardView3);
        cardView4 = view.findViewById(R.id.cardView4);
        cardView5 = view.findViewById(R.id.cardView5);
        cardView6 = view.findViewById(R.id.cardView6);
        cardView7 = view.findViewById(R.id.cardView7);
        cardView8 = view.findViewById(R.id.cardView8);
        cardView9 = view.findViewById(R.id.cardView9);
        cardView10 = view.findViewById(R.id.cardView10);

        arrowImg = view.findViewById(R.id.arrowImg);
        arrowImg1 = view.findViewById(R.id.arrowImg1);
        arrowImg2 = view.findViewById(R.id.arrowImg2);
        arrowImg3 = view.findViewById(R.id.arrowImg3);
        arrowImg4 = view.findViewById(R.id.arrowImg4);
        arrowImg5 = view.findViewById(R.id.arrowImg5);
        arrowImg6 = view.findViewById(R.id.arrowImg6);
        arrowImg7 = view.findViewById(R.id.arrowImg7);
        arrowImg8 = view.findViewById(R.id.arrowImg8);
        arrowImg9 = view.findViewById(R.id.arrowImg9);

        setOnClickListener(description, layout1, cardView1, arrowImg);
        setOnClickListener(taxonomy, layout2, cardView2, arrowImg1);
        setOnClickListener(commonUses, layout3, cardView3, arrowImg2);
        setOnClickListener(culturalSignificance, layout4, cardView4, arrowImg3);
        setOnClickListener(toxicity, layout5, cardView5, arrowImg4);
        setOnClickListener(propagationMethods, layout6, cardView6, arrowImg5);
        setOnClickListener(edibleParts, layout7, cardView7, arrowImg6);
        setOnClickListener(bestLightCondition, layout8, cardView8, arrowImg7);
        setOnClickListener(bestSoilType, layout9, cardView9, arrowImg8);
        setOnClickListener(bestWatering, layout10, cardView10, arrowImg9);

        Bundle args = getArguments();

        if (args != null) {
            boolean isFromGallery = args.getBoolean("isFromGallery", true);

            String name = args.getString("plantName", "");
            String sciName = args.getString("plantScientificName", "");
            String desc = args.getString("plantDescription", "");
            String imageUrl = args.getString("plantImageUrl", "");
            ArrayList<String> ediblePartsArray = args.getStringArrayList("edibleParts");
            ArrayList<String> propagationMethodsArray = args.getStringArrayList("propagationMethods");
            String commonUses = args.getString("commonUses", "");
            String culturalSignificance = args.getString("culturalSignificance", "");
            String toxicity = args.getString("toxicity", "");
            String bestLightCondition = args.getString("bestLightCondition", "");
            String bestSoilType = args.getString("bestSoilType", "");
            String bestWatering = args.getString("bestWatering", "");
            HashMap<String, String> taxonomyMap = (HashMap<String, String>) args.getSerializable("taxonomy");
            boolean allowSaveToGallery = args.getBoolean("allowSaveToGallery", false);

            plantName.setText(StringUtils.capitalize(name));
            scientificName.setText(StringUtils.capitalize(sciName));
            description.setText(desc);
            edibleParts.setText(StringUtils.arrayToString(ediblePartsArray));
            propagationMethods.setText(StringUtils.arrayToString(propagationMethodsArray));
            this.commonUses.setText(commonUses);
            this.culturalSignificance.setText(culturalSignificance);
            this.toxicity.setText(toxicity);
            this.bestLightCondition.setText(bestLightCondition);
            this.bestSoilType.setText(bestSoilType);
            this.bestWatering.setText(bestWatering);

            saveButton.setVisibility(allowSaveToGallery ? View.VISIBLE : View.GONE);
            Plant plant = new Plant.Builder()
                    .name(name)
                    .scientificName(sciName)
                    .description(desc)
                    .image(imageUrl)
                    .edibleParts(ediblePartsArray)
                    .propagationMethods(propagationMethodsArray)
                    .commonUses(commonUses)
                    .culturalSignificance(culturalSignificance)
                    .toxicity(toxicity)
                    .bestLightCondition(bestLightCondition)
                    .bestSoilType(bestSoilType)
                    .bestWatering(bestWatering)
                    .taxonomy(taxonomyMap)
                    .build();

            saveButton.setOnClickListener(v -> {
                DatabaseManager databaseManager = new DatabaseManager();
                databaseManager.addIdentification(plant, FirebaseAuth.getInstance().getCurrentUser().getUid());
            });

            backButton.setOnClickListener(v -> {
                if (isFromGallery) {
                    PlantGalleryFragment plantGalleryFragment = new PlantGalleryFragment();
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.frameLayout, plantGalleryFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    requireActivity().finish();
                }
            });

            if (taxonomyMap != null && !taxonomyMap.isEmpty()) {
                StringBuilder taxonomyBuilder = new StringBuilder();
                for (String level : TAXONOMY_ORDER) {
                    String value = taxonomyMap.get(level);
                    if (value != null && !value.isEmpty()) {
                        taxonomyBuilder.append(StringUtils.capitalize(level))
                                .append(": ")
                                .append(StringUtils.capitalize(value))
                                .append("\n");
                    }
                }
                this.taxonomy.setText(taxonomyBuilder.toString().trim());
            } else {
                this.taxonomy.setText("Taxonomy information not available");
            }

            if (!imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.aaboutus)
                        .error(R.drawable.custom_dialog_layout_error_icon)
                        .into(plantImage);

                imageViewPhoto = imageUrl;
                plantImage.setOnClickListener(v -> showResourceImagePopup(requireContext(), imageViewPhoto));
            }
        }

        return view;
    }

    private void setOnClickListener(TextView textView, LinearLayout linearLayout, CardView cardViews, ImageView arrowImg) {
        linearLayout.setOnClickListener(v -> {
            expandView(textView, linearLayout, arrowImg);
        });

        cardViews.setOnClickListener(v -> {
            expandView(textView, linearLayout, arrowImg);
        });
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

    public void expandView(TextView description, LinearLayout layout, ImageView arrowImg) {
        int visibility = (description.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;

        // Create an AutoTransition for the description's visibility
        AutoTransition transition = new AutoTransition();
        transition.setDuration(300);
        transition.setInterpolator(new FastOutSlowInInterpolator());
        layout.setLayoutTransition(null);

        // Rotate arrow based on visibility
        arrowImg.setRotation(visibility == View.VISIBLE ? 180f : 0f);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionEnd(Transition transition) {
                LayoutTransition layoutTransition = new LayoutTransition();
                layout.setLayoutTransition(layoutTransition);
            }
            @Override public void onTransitionStart(Transition transition) {}
            @Override public void onTransitionCancel(Transition transition) {}
            @Override public void onTransitionPause(Transition transition) {}
            @Override public void onTransitionResume(Transition transition) {}
        });

        TransitionManager.beginDelayedTransition(layout, transition);
        description.setVisibility(visibility);
    }
}