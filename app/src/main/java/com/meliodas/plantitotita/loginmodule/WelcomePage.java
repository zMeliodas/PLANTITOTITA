package com.meliodas.plantitotita.loginmodule;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.mainmodule.HomePage;

public class WelcomePage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BroadcastReceiver networkReceiver;
    private AlertDialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
        mAuth = FirebaseAuth.getInstance();

        // Initialize the network receiver
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Check connectivity status when network changes
                if (!isConnected()) {
                    showNoInternetDialog();
                } else if (noInternetDialog != null && noInternetDialog.isShowing()) {
                    noInternetDialog.dismiss(); // Dismiss dialog when internet is restored
                }
            }
        };

        // Register the network receiver
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Check internet connection
        if (!isConnected()) {
            showNoInternetDialog();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            if(currentUser.isEmailVerified()) {
                startActivity(new Intent(this, HomePage.class));
                finish();
            } else {
                mAuth.signOut();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }

    // Check if there is an internet connection
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    private void showNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            return; // Avoid showing the dialog multiple times
        }

        // Inflate the custom layout for no connection dialog
        View view = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog_no_connection, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(false); // Prevent dismissing by outside touches

        noInternetDialog = builder.create();

        // Set transparent background
        if (noInternetDialog.getWindow() != null) {
            noInternetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        Button continueButton = view.findViewById(R.id.dialogContinueButton);

        // Retry connection on "Continue" button click
        continueButton.setOnClickListener(view1 -> {
            if (isConnected()) {
                noInternetDialog.dismiss();
            }
        });

        noInternetDialog.show();
    }

    public void onClickSignIn(View v){
        startActivity(new Intent(this, LoginPage.class));
    }

    public void onClickSignUp(View v){
        startActivity(new Intent(this, RegistrationPage.class));
    }

}