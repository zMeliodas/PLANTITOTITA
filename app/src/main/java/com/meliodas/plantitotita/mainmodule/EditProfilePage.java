package com.meliodas.plantitotita.mainmodule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.meliodas.plantitotita.R;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfilePage extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int UCROP_REQUEST_CODE = 101;
    Uri imageUri;
    FirebaseUser firebaseUser;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseFirestore fStore;
    private String imageViewPhoto, firstName, lastName, eMail, mobileNum;
    private EditText editTextFirstName, editTextLastName, editTextMobileNum;
    private ImageView imageView;
    private boolean imageChanged = false;
    private BroadcastReceiver networkReceiver;
    private android.app.AlertDialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_page);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        fStore = FirebaseFirestore.getInstance();

        insertInitialValue();
    }

    @Override
    protected void onStart() {
        super.onStart();

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

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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
            startActivity(new Intent(getApplicationContext(), ViewProfilePage.class));
            finish();
        });

        noInternetDialog.show();
    }

    public void onClickReturn3(View v){
        startActivity(new Intent(getApplicationContext(), ViewProfilePage.class));
        finish();
    }

    public void onClickEditAvatar(View v){
        selectImage();
    }

    public void onClickSaveChanges(View v){
        showDialog();
    }

    public void insertInitialValue(){
        imageView = findViewById(R.id.editProfileImageView);
        editTextFirstName = findViewById(R.id.editProfileEditTxtFirstName);
        editTextLastName = findViewById(R.id.editProfileEditTxtLastName);
        editTextMobileNum = findViewById(R.id.editProfileEditTxtContactNumber);

        DocumentReference documentReference = fStore.collection("users").document(firebaseUser.getUid());
        documentReference.addSnapshotListener(this, (value, error) -> {
            Picasso.get().load(R.mipmap.default_profile).into(imageView);

            if (value != null) {
                imageViewPhoto = value.getString("profile_picture");
                firstName = value.getString("user_name");
                lastName = value.getString("last_name");
                mobileNum = value.getString("mobile_number");

                editTextFirstName.setText(firstName);
                editTextLastName.setText(lastName);
                editTextMobileNum.setText(mobileNum);

                if (imageViewPhoto == null || imageViewPhoto.isEmpty()) {
                    return;
                }

                Picasso.get().load(imageViewPhoto).into(imageView);
            }
        });
    }

    public void editInformation(){
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user_name", editTextFirstName.getText().toString());
        userInfo.put("last_name", editTextLastName.getText().toString());
        userInfo.put("mobile_number", editTextMobileNum.getText().toString());

        if(imageChanged) uploadImage(firebaseUser.getEmail());

        fStore.collection("users").document(firebaseUser.getUid()).set(userInfo, SetOptions.merge()).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Information updated!", Toast.LENGTH_SHORT).show();
        });
    }

    public void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            startUCropActivity(imageUri);
        } else if (requestCode == UCROP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    Glide.with(this)
                            .load(bitmap)
                            .into((ImageView)findViewById(R.id.editProfileImageView));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = resultUri;
                imageChanged = true;
            }
        }
    }

    private void startUCropActivity(Uri sourceUri) {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarTitle("Crop Image");
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        UCrop.of(sourceUri, Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg")))
                .withOptions(options)
                .start(this, UCROP_REQUEST_CODE);
    }

    public void uploadImage(String email){
        StorageReference imagesRef = storageRef.child("profilePictures/" + email + "/" + "profile");
        imagesRef.putFile(imageUri, new StorageMetadata.Builder().setContentType("image/jpeg").build()).addOnSuccessListener(taskSnapshot -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                return;
            }

            if (taskSnapshot.getMetadata() == null) {
                return;
            }

            if (taskSnapshot.getMetadata().getReference() == null) {
                return;
            }

            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(downloadURL -> {
                FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(downloadURL).build());
                fStore.collection("users").document(firebaseUser.getUid()).set(new HashMap<String, Object>() {
                    {
                        put("profile_picture", downloadURL);
                    }
                }, SetOptions.merge());
            });
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show());
    }

    private void showDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog_save_profile, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        Button continueButton = view.findViewById(R.id.dialogContinueButton);
        Button continueButton1 = view.findViewById(R.id.dialogContinueButton1);

        continueButton.setOnClickListener(view1 -> {
            if (editTextFirstName.getText() == null || editTextFirstName.getText().toString().isEmpty()){
                alertDialog.dismiss();
                editTextFirstName.setError("First name can't be blank");
                return;
            }

            if (editTextLastName.getText() == null || editTextLastName.getText().toString().isEmpty()){
                alertDialog.dismiss();
                editTextLastName.setError("Last name can't be blank");
                return;
            }

            if (editTextMobileNum.getText() == null || editTextMobileNum.getText().toString().isEmpty()){
                alertDialog.dismiss();
                editTextMobileNum.setError("Mobile number can't be blank");
                return;
            }

            if (editTextMobileNum.length() > 11 || editTextMobileNum.length() < 11){
                alertDialog.dismiss();
                editTextMobileNum.setError("You can only enter 11 numbers");
                editTextMobileNum.requestFocus();
                return;
            }

            if (!editTextMobileNum.getText().toString().matches("^(09)\\d{9}")){
                alertDialog.dismiss();
                editTextMobileNum.setError("Invalid mobile number format");
                editTextMobileNum.requestFocus();
                return;
            }

            editInformation();
            alertDialog.dismiss();
        });

        continueButton1.setOnClickListener(view1 -> {
            alertDialog.dismiss();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }
}