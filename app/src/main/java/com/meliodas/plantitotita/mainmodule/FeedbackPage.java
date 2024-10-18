package com.meliodas.plantitotita.mainmodule;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.Rating;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.meliodas.plantitotita.R;
import com.meliodas.plantitotita.loginmodule.LoginPage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FeedbackPage extends AppCompatActivity {

    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private EditText feedbackText;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_page);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        feedbackText = findViewById(R.id.editTextTextMultiLine2);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setRating(1);
    }

    public void onClickReturnFeedback(View view) {
        startActivity(new Intent(getApplicationContext(), HomePage.class));
    }

    public void onClickSubmitFeedback(View view) {
        String feedback = feedbackText.getText().toString();
        float rating = ratingBar.getRating();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userID = currentUser.getUid();
            String userEmail = currentUser.getEmail();

            FirebaseFirestore.getInstance().collection("users").document(userID)
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String firstName = task.getResult().getString("user_name");

                            if (!feedback.isEmpty()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                String currentDate = sdf.format(new Date());

                                Map<String, Object> feedbackData = new HashMap<>();
                                feedbackData.put("feedback", feedback);
                                feedbackData.put("rating", rating);
                                feedbackData.put("user_id", userID);
                                feedbackData.put("email", userEmail);
                                feedbackData.put("first_name", firstName);
                                feedbackData.put("date", currentDate);

                                FirebaseFirestore.getInstance().collection("feedbacks").add(feedbackData)
                                        .addOnSuccessListener(documentReference -> {
                                            showCustomDialog();
                                            feedbackText.setText("");
                                            ratingBar.setRating(1);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FeedbackSubmission", "Error adding feedback", e);
                                            Toast.makeText(FeedbackPage.this, "Failed to submit feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(this, "Please provide feedback before submitting.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("FeedbackSubmission", "Failed to fetch user data", task.getException());
                            Toast.makeText(FeedbackPage.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(FeedbackPage.this, "You need to be logged in to submit feedback.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginPage.class));
        }
    }

    private void showCustomDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog_feedback_thankyou, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        Button continueButton = view.findViewById(R.id.dialogContinueButton);

        continueButton.setOnClickListener(view1 -> {
            alertDialog.dismiss();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }
}