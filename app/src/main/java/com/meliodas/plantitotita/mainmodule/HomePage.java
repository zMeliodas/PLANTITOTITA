package com.meliodas.plantitotita.mainmodule;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.loginmodule.WelcomePage;

public class HomePage extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView displayName;
    String name;
    View inflatedView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        navigationView.bringToFront();
        inflatedView = getLayoutInflater().inflate(R.layout.drawer_header, null);
        displayName = inflatedView.findViewById(R.id.navDrawerDisplayName);
        displayName.setText(name);
    }

    public void onClickNavBurger(View v){
        drawerLayout.open();
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