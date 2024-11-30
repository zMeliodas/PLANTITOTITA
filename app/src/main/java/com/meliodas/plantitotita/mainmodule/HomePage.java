package com.meliodas.plantitotita.mainmodule;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.fragments.HomePageFragment;
import com.meliodas.plantitotita.fragments.PlantGalleryFragment;
import com.meliodas.plantitotita.fragments.PlantHealthAssessmentGalleryFragment;
import com.meliodas.plantitotita.fragments.ReminderFragment;
import com.meliodas.plantitotita.loginmodule.WelcomePage;
import com.squareup.picasso.Picasso;

public class HomePage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore fStore;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView displayName;
    private String name, userID, imageViewPhoto;
    private View inflatedView;
    private ImageView imageView;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        NotificationHelper.createNotificationChannel(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        inflatedView = getLayoutInflater().inflate(R.layout.drawer_header, null);
        displayName = inflatedView.findViewById(R.id.navDrawerDisplayName);
        imageView = inflatedView.findViewById(R.id.navDrawerImageView);
        navigationView.addHeaderView(inflatedView);
        navigationView.bringToFront();
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        Menu menu = navigationView.getMenu();

        MenuItem dashboard = menu.findItem(R.id.dashboardNavMenuTitle);
        MenuItem feedBackAboutUs = menu.findItem(R.id.feedBackAndAboutUsNavMenuTitle);
        MenuItem exit = menu.findItem(R.id.exitNavMenuTitle);
        SpannableString s = new SpannableString(dashboard.getTitle());
        SpannableString s1 = new SpannableString(feedBackAboutUs.getTitle());
        SpannableString s2 = new SpannableString(exit.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceTitle), 0, s.length(), 0);
        s1.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceTitle), 0, s1.length(), 0);
        s2.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceTitle), 0, s2.length(), 0);
        dashboard.setTitle(s);
        feedBackAboutUs.setTitle(s1);
        exit.setTitle(s2);


        if (user == null) {
            startActivity(new Intent(getApplicationContext(), WelcomePage.class));
            finish();
            return;
        }

        userID = mAuth.getCurrentUser().getUid();

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPress();
            }
        });

        insertInitialValue();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NotificationHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.showNotification(this, "Permission Granted", "You can now receive notifications.");
            } else {
                NotificationHelper.showNotification(this, "Permission Denied", "You need to grant the permission to receive notifications.");
            }
        }
    }

    public void onBackPress() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
            return;
        }

        showDialog("Are you sure you want to exit the app?",
                "Yes",
                "No",
                this::finish);
    }

    public void insertInitialValue() {
        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, (value, error) -> {
            Picasso.get().load(R.mipmap.default_profile).into(imageView);

            if (value != null) {
                name = value.getString("user_name");
                imageViewPhoto = value.getString("profile_picture");
                displayName.setText(name);

                if (imageViewPhoto == null || imageViewPhoto.isEmpty()) {
                    return;
                }

                Picasso.get().load(imageViewPhoto).into(imageView);
            }
        });

        replaceFragment(new HomePageFragment());
    }

    public void onClickNavBurger(View v) {
        drawerLayout.open();
    }

    public void onClickCamera(View v) {
        startActivity(new Intent(getApplicationContext(), ArSceneActivity.class));
    }

    public void onClickHome(View view) {
        replaceFragment(new HomePageFragment());
        drawerLayout.close();
    }

    public void onClickViewProfile(View v) {
        startActivity(new Intent(getApplicationContext(), ViewProfilePage.class));
        drawerLayout.close();
    }

    public void onClickSettings(MenuItem item) {
        startActivity(new Intent(getApplicationContext(), SettingsPage.class));
    }

    public void onClickFeedback(MenuItem item) {
        startActivity(new Intent(getApplicationContext(), FeedbackPage.class));
    }

    public void onClickAboutUs(MenuItem item) {
        startActivity(new Intent(getApplicationContext(), AboutUsPage.class));
    }

    public void onClickPlantGallery(MenuItem item) {
        replaceFragment(new PlantGalleryFragment());
        drawerLayout.close();
    }

    public void onClickPlantHealthAssessment(MenuItem item) {
        replaceFragment(new PlantHealthAssessmentGalleryFragment());
        drawerLayout.close();
    }

    public void onClickReminder(MenuItem item) {
        replaceFragment(new ReminderFragment());
        drawerLayout.close();
    }

    public void onClickLogout(MenuItem item) {
        showDialog("Are you sure you want to log out? You will need to log in again to access your account.",
                "Yes",
                "No",
                () -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, WelcomePage.class));
                    finish();
                });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("my_fragment");
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void showDialog(String message, String positiveButtonText, String negativeButtonText, Runnable positiveAction) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog_log_out, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        TextView dialogMessage = view.findViewById(R.id.dialogMessage);
        Button continueButton = view.findViewById(R.id.dialogContinueButton);
        Button continueButton1 = view.findViewById(R.id.dialogContinueButton1);

        // Set the message
        dialogMessage.setText(message);

        // Set the positive button action
        continueButton.setText(positiveButtonText);
        continueButton.setOnClickListener(view1 -> {
            positiveAction.run();
            alertDialog.dismiss();
        });

        // Set the negative button action
        continueButton1.setText(negativeButtonText);
        continueButton1.setOnClickListener(view1 -> {
            alertDialog.dismiss();
            drawerLayout.close();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }
}