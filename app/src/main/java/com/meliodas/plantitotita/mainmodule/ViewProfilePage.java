package com.meliodas.plantitotita.mainmodule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
    private static String imageViewPhoto;
    private String firstName;
    private String lastName;
    private String eMail;
    private String mobileNum;
    private TextView viewProfileFirstName, viewProfileLastName, viewProfileEmail, viewProfileMobileNum;
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
        viewProfileFirstName = findViewById(R.id.viewProfileFirstName);
        viewProfileLastName = findViewById(R.id.viewProfileLastName);
        viewProfileEmail = findViewById(R.id.viewProfileEmail);
        viewProfileMobileNum = findViewById(R.id.viewProfileContactNumber);

        DocumentReference documentReference = fStore.collection("users").document(firebaseUser.getUid());
        documentReference.addSnapshotListener(this, (value, error) -> {
            Picasso.get().load(R.mipmap.default_profile).into(imageView);

            if (value != null) {
                imageViewPhoto = value.getString("profile_picture");
                firstName = value.getString("user_name");
                lastName = value.getString("last_name");
                eMail = value.getString("email_address");
                mobileNum = value.getString("formatted_mobile_number");

                viewProfileFirstName.setText(firstName);
                viewProfileLastName.setText(lastName);
                viewProfileEmail.setText(maskEmail(eMail));
                viewProfileMobileNum.setText(mobileNum);

                if (imageViewPhoto == null || imageViewPhoto.isEmpty()) {
                    return;
                }

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

        if (imageViewPhoto == null || imageViewPhoto.isEmpty()) {
            return;
        }

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

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.length() > 3) {
            String firstChar = localPart.substring(0, 1);
            String lastChars = localPart.substring(localPart.length() - 2);

            StringBuilder masked = new StringBuilder();
            for (int i = 0; i < localPart.length() - 3; i++) {
                masked.append("*");
            }

            return firstChar + masked.toString() + lastChars + "@" + domainPart;

        } else {
            return email;
        }
    }
}