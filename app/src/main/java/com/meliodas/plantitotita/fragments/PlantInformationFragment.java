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
    import android.widget.LinearLayout;
    import android.widget.ScrollView;
    import androidx.cardview.widget.CardView;
    import androidx.fragment.app.Fragment;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;
    import android.widget.ImageView;
    import androidx.fragment.app.FragmentTransaction;
    import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
    import com.bumptech.glide.Glide;
    import com.meliodas.plantitotita.R;
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
        private TextView descriptionTitle, ediblePartsTitle, propagationMethodsTitle, commonUsesTitle, culturalSignificanceTitle, toxicityTitle, bestLightConditionTitle, bestSoilTypeTitle, bestWateringTitle, taxonomyTitle;
        private LinearLayout layout1, layout2, layout3, layout4, layout5, layout6, layout7, layout8, layout9, layout10;
        private CardView cardView1, cardView2, cardView3, cardView4, cardView5, cardView6, cardView7, cardView8, cardView9, cardView10;


        private static final List<String> TAXONOMY_ORDER = Arrays.asList(
                "kingdom", "phylum", "class", "order", "family", "genus", "species"
        );

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

            setOnClickListener(description, layout1, cardView1);
            setOnClickListener(taxonomy, layout2, cardView2);
            setOnClickListener(commonUses, layout3, cardView3);
            setOnClickListener(culturalSignificance, layout4, cardView4);
            setOnClickListener(toxicity, layout5, cardView5);
            setOnClickListener(propagationMethods, layout6, cardView6);
            setOnClickListener(edibleParts, layout7, cardView7);
            setOnClickListener(bestLightCondition, layout8, cardView8);
            setOnClickListener(bestSoilType, layout9, cardView9);
            setOnClickListener(bestWatering, layout10, cardView10);

            Bundle args = getArguments();

            if (args != null) {
                String name = args.getString("plantName", "");
                String sciName = args.getString("scientificName", "");
                String desc = args.getString("description", "");
                String imageUrl = args.getString("image", "");
                ArrayList<String> ediblePartsArray = args.getStringArrayList("edibleParts");
                ArrayList<String> propagationMethodsArray = args.getStringArrayList("propagationMethods");
                String commonUses = args.getString("commonUses", "");
                String culturalSignificance = args.getString("culturalSignificance", "");
                String toxicity = args.getString("toxicity", "");
                String bestLightCondition = args.getString("bestLightCondition", "");
                String bestSoilType = args.getString("bestSoilType", "");
                String bestWatering = args.getString("bestWatering", "");
                HashMap<String, String> taxonomyMap = (HashMap<String, String>) args.getSerializable("taxonomy");

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

        private void setOnClickListener(TextView textView, LinearLayout linearLayout, CardView cardViews) {
            linearLayout.setOnClickListener(v -> {
                expandView(textView, linearLayout);
            });

            cardViews.setOnClickListener(v -> {
                expandView(textView, linearLayout);
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