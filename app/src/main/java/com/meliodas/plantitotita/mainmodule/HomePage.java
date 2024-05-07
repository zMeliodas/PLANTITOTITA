package com.meliodas.plantitotita.mainmodule;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.loginmodule.WelcomePage;

public class HomePage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore fStore;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView displayName, sample;
    private String name, userID;
    private View inflatedView;
    private ViewGroup parent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        inflatedView = getLayoutInflater().inflate(R.layout.drawer_header, null);
        displayName = inflatedView.findViewById(R.id.navDrawerDisplayName);
        sample = findViewById(R.id.sampleTxt);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        userID = mAuth.getCurrentUser().getUid();
        navigationView.addHeaderView(inflatedView);

        if (user == null){
            startActivity(new Intent(getApplicationContext(), WelcomePage.class));
            finish();
        }

        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, (value, error) -> {
            name = value.getString("user_name");
            sample.setText(name);
            displayName.setText(name);
            Toast.makeText(HomePage.this.getApplicationContext(), displayName.getText(), Toast.LENGTH_SHORT).show();
        });
    }

    public void onClickNavBurger(View v){
        drawerLayout.open();
        navigationView.bringToFront();
    }

    public void onClickLogout(MenuItem item) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }

    public void onClickCamera(View v){
        Toast.makeText(this, "CLICK!", Toast.LENGTH_SHORT).show();
    }

    public void onClickViewProfile(View v){
        Toast.makeText(this, "CLICK!", Toast.LENGTH_SHORT).show();
        drawerLayout.close();
    }

    public void onClickSettings(MenuItem item) {
        Toast.makeText(this, "CLICK!", Toast.LENGTH_SHORT).show();
        drawerLayout.close();
    }
}