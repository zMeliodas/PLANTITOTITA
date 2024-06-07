package com.meliodas.plantitotita.mainmodule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.meliodas.plantitotita.R;
import com.squareup.picasso.Picasso;

public class ViewProfilePage extends AppCompatActivity {

    FirebaseUser firebaseUser;
    StorageReference storageReference;
    FirebaseFirestore fStore;
    private String imageViewPhoto;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_page);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();

        insertInitialValue();
        imageView.setOnClickListener(v -> showResourceImagePopup(this, imageViewPhoto));
    }

    public void insertInitialValue(){
        imageView = findViewById(R.id.viewProfileImageView);
        DocumentReference documentReference = fStore.collection("users").document(firebaseUser.getUid());
        documentReference.addSnapshotListener(this, (value, error) -> {
            if (value != null) {
                imageViewPhoto = value.getString("profile_picture");
                Picasso.get().load(imageViewPhoto).into(imageView);
            }
        });
    }

    public static void showResourceImagePopup(Context context, String imageResource) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        View popupView = LayoutInflater.from(context).inflate(R.layout.image_popup_template, null);
        ImageView popupImageView = popupView.findViewById(R.id.imageView88);
        Picasso.get().load(imageResource).into(popupImageView);
        AlertDialog dialog = adb.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setView(popupView);
        dialog.show();
    }

    public void onClickEditProfile(View view) {
        startActivity(new Intent(this, EditProfilePage.class));
        finish();
    }

    public void onClickReturn2(View view) {
        startActivity(new Intent(this, HomePage.class));
        finish();
    }
}