package com.meliodas.plantitotita.mainmodule;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
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

        if (user == null){
            startActivity(new Intent(getApplicationContext(), WelcomePage.class));
            finish();
            return;
        }

        userID = mAuth.getCurrentUser().getUid();

        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, (value, error) -> {
            if (value != null) {
                name = value.getString("user_name");
                imageViewPhoto = value.getString("profile_picture");
                Picasso.get().load(imageViewPhoto).into(imageView);
            }
            displayName.setText(name);
        });

        replaceFragment(new HomePageFragment());
    }

    public void onClickNavBurger(View v){
        drawerLayout.open();
    }

    public void onClickCamera(View v){
        Toast.makeText(this, "CLICK!", Toast.LENGTH_SHORT).show();
    }

    public void onClickHome(View view) {
        replaceFragment(new HomePageFragment());
        drawerLayout.close();
    }

    public void onClickViewProfile(View v){
        startActivity(new Intent(getApplicationContext(), ViewProfilePage.class));
        drawerLayout.close();
    }

    public void onClickSettings(MenuItem item) {
        Toast.makeText(this, "CLICK!", Toast.LENGTH_SHORT).show();
        drawerLayout.close();
    }

    public void onClickLogout(MenuItem item) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}