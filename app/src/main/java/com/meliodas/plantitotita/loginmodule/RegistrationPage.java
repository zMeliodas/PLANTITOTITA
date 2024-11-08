package com.meliodas.plantitotita.loginmodule;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;
import com.meliodas.plantitotita.R;
import java.util.HashMap;
import java.util.Map;

public class RegistrationPage extends AppCompatActivity {

    private EditText editTextFirstName,editTextLastName, editTextEmailAddress, editTextMobileNumber, editTextPassword, editTextConfirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String userID;
    CountryCodePicker ccp;
    private BroadcastReceiver networkReceiver;
    private android.app.AlertDialog noInternetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        editTextFirstName = findViewById(R.id.editTxtName);
        editTextLastName = findViewById(R.id.editTxtLastName);
        editTextEmailAddress = findViewById(R.id.editTxtEmailAddress);
        editTextMobileNumber = findViewById(R.id.editTxtMobileNumber);
        editTextPassword = findViewById(R.id.editTxtPassword);
        editTextConfirmPassword = findViewById(R.id.editTxtConfirmPassword);
        ccp = findViewById(R.id.countryCodePicker);

        ccp.registerCarrierNumberEditText(editTextMobileNumber);

        TextView textViewName = findViewById(R.id.regNameTxtView);
        TextView textViewLastName = findViewById(R.id.regLastNameTxtView);
        TextView textViewEmailAddress = findViewById(R.id.regEmailTxtView);
        TextView textViewMobileNumber = findViewById(R.id.regMobileNumTxtView);
        TextView textViewPassword = findViewById(R.id.regPasswordTxtView);
        TextView textViewConfirmPassword = findViewById(R.id.regConfirmPasswordTxtView);

        makeTextYellow();

        updateAsterisk(editTextFirstName, textViewName, "First Name", R.string.NameTxtView);
        updateAsterisk(editTextLastName, textViewLastName, "Last Name", R.string.LastNameTxtView);
        updateAsterisk(editTextEmailAddress, textViewEmailAddress, "Email Address", R.string.EmailTxtView);
        updateAsterisk(editTextMobileNumber, textViewMobileNumber, "Mobile Number", R.string.MobileNumTxtView);
        updateAsterisk(editTextPassword, textViewPassword, "Password", R.string.PasswordTxtView);
        updateAsterisk(editTextConfirmPassword, textViewConfirmPassword, "Confirm Password", R.string.ConfirmPasswordTxtView);

        setupTextWatchers();
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

    public void onClickCreateAccount(View v) {

        validateFirstName(editTextFirstName.getText().toString());
        validateLastName(editTextLastName.getText().toString());
        validateEmail(editTextEmailAddress.getText().toString());
        validateMobileNumber(editTextMobileNumber.getText().toString());
        validatePassword(editTextPassword.getText().toString());
        validateConfirmPassword(editTextConfirmPassword.getText().toString());

        if (editTextFirstName.getError() != null ||
                editTextLastName.getError() != null ||
                editTextEmailAddress.getError() != null ||
                editTextMobileNumber.getError() != null ||
                editTextPassword.getError() != null ||
                editTextConfirmPassword.getError() != null) {
            return;
        }

        String name = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String email = editTextEmailAddress.getText().toString();
        String mobileNumber = ccp.getFullNumber();
        String formattedMobileNumber = ccp.getFormattedFullNumber();
        String password = editTextPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            userID = user.getUid();
                            sendEmailVerification(user);
                            saveUserDataToFirestore(name, lastName, email, mobileNumber, formattedMobileNumber);
                        }
                    } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        editTextEmailAddress.setError("Email already exists");
                        editTextEmailAddress.requestFocus();
                    } else {
                        showDialog(EnumLayout.ERROR);
                    }
                });
    }

    private void setupTextWatchers() {
        editTextFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateFirstName(s.toString());
            }
        });

        editTextLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateLastName(s.toString());
            }
        });

        editTextEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateEmail(s.toString());
            }
        });

        editTextMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateMobileNumber(s.toString());
            }
        });

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
            }
        });

        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateConfirmPassword(s.toString());
            }
        });
    }

    private void validateFirstName(String firstName) {
        if (firstName.isEmpty()) {
            editTextFirstName.setError("First Name can't be blank.");
        } else if (firstName.length() < 2) {
            editTextFirstName.setError("First Name should be at least 2 characters.");
        } else {
            editTextFirstName.setError(null);
        }
    }

    private void validateLastName(String lastName) {
        if (lastName.isEmpty()) {
            editTextLastName.setError("Last Name can't be blank.");
        } else if (lastName.length() < 2) {
            editTextLastName.setError("Last Name should be at least 2 characters.");
        } else if (!lastName.matches("^[A-Z][a-z ,.'-]+$")) {
            editTextLastName.setError("Please enter a valid last name. It should start with an uppercase letter and may include letters, spaces, commas, apostrophes, or hyphens.");
        } else {
            editTextLastName.setError(null);
        }
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) {
            editTextEmailAddress.setError("Email can't be blank.");
        } else if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            editTextEmailAddress.setError("Invalid email address Ex.abcdefg123@gmail.com");
        } else {
            editTextEmailAddress.setError(null);
        }
    }

    private void validateMobileNumber(String mobileNumber) {
        if (mobileNumber.isEmpty()) {
            editTextMobileNumber.setError("Mobile Number can't be blank.");
        } else if (!ccp.isValidFullNumber()) {
            editTextMobileNumber.setError("Invalid mobile number format");
        } else {
            editTextMobileNumber.setError(null);
        }
    }

    private void validatePassword(String password) {
        if (password.isEmpty()) {
            editTextPassword.setError("Password can't be blank.");
        } else if (password.length() < 8) {
            editTextPassword.setError("Your Password is too short. Use at least 8 or more characters");
        } else if (!password.matches("^(?=.*[A-Z])(?=.*[0-9]).{8,}$")) {
            editTextPassword.setError("Your Password must contain at least one uppercase letter and one digit.");
        } else {
            editTextPassword.setError(null);
        }

        // Validate confirm password when password changes
        if (!editTextConfirmPassword.getText().toString().isEmpty()) {
            validateConfirmPassword(editTextConfirmPassword.getText().toString());
        }
    }

    private void validateConfirmPassword(String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Confirm Password can't be blank.");
        } else if (!confirmPassword.equals(editTextPassword.getText().toString())) {
            editTextConfirmPassword.setError("Those passwords didn't match. Try again.");
        } else {
            editTextConfirmPassword.setError(null);
        }
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                                showDialog(EnumLayout.SUCCESS);
                    } else {
                        showDialog(EnumLayout.ERROR);
                    }
                });
    }

    private void saveUserDataToFirestore(String name, String lastName, String email, String mobileNumber, String formattedMobileNumber) {
        DocumentReference documentReference = fStore.collection("users").document(userID);
        Map<String, Object> user = new HashMap<>();
        user.put("user_name", name);
        user.put("last_name", lastName);
        user.put("email_address", email);
        user.put("mobile_number", mobileNumber);
        user.put("formatted_mobile_number", formattedMobileNumber);
        documentReference.set(user).addOnSuccessListener(unused -> {
            // Data saved successfully
        }).addOnFailureListener(e -> {
            Toast.makeText(RegistrationPage.this, "Error saving user data", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDialog(EnumLayout layout) {
        View view = LayoutInflater.from(this).inflate(
        switch (layout) {
            case SUCCESS -> R.layout.custom_alert_dialog_success;
            case ERROR -> R.layout.custom_alert_dialog_error;
        },  null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();

        Button continueButton = view.findViewById(R.id.dialogContinueButton);

        continueButton.setOnClickListener(view1 -> {
            switch (layout) {
                case SUCCESS -> {
                    startActivity(new Intent(RegistrationPage.this, LoginPage.class));
                    finish();
                }
                case ERROR -> alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void showTermsPrivacyDialog() {
        // Create and set up the custom dialog
        View view = LayoutInflater.from(this).inflate(R.layout.custom_alert_dialog_terms_and_conditions, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Make the dialog non-cancelable

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        TextView termsTextView = view.findViewById(R.id.termsTextView);
        TextView privacyTextView = view.findViewById(R.id.privacyTextView);
        Button btnOk = view.findViewById(R.id.btn_ok);
        ScrollView scrollView = view.findViewById(R.id.scrollView); // Assuming the ScrollView ID is scrollView

        // Set fonts
        Typeface madaBlack = ResourcesCompat.getFont(this, R.font.mada_black);
        Typeface madaMedium = ResourcesCompat.getFont(this, R.font.mada_medium);

        // Retrieve and format text
        String termsText = getString(R.string.terms_of_service);
        String privacyText = getString(R.string.privacy_policy);
        String formattedTermsText = HtmlCompat.fromHtml(termsText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        String formattedPrivacyText = HtmlCompat.fromHtml(privacyText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString();

        // Apply styling to the terms and privacy text
        SpannableString styledTermsText = new SpannableString(formattedTermsText);
        styledTermsText.setSpan(new CustomTypefaceSpan(madaBlack), 0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledTermsText.setSpan(new CustomTypefaceSpan(madaMedium), 21, formattedTermsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString styledPrivacyText = new SpannableString(formattedPrivacyText);
        styledPrivacyText.setSpan(new CustomTypefaceSpan(madaBlack), 0, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledPrivacyText.setSpan(new CustomTypefaceSpan(madaMedium), 24, formattedPrivacyText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set text in TextViews
        termsTextView.setText(styledTermsText);
        privacyTextView.setText(styledPrivacyText);

        // Initially hide the Continue button
        btnOk.setVisibility(View.GONE);

        // Listen for scroll events on the ScrollView
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            // Check if the user has scrolled to the bottom
            if (!scrollView.canScrollVertically(1)) {
                // Show the Continue button when user reaches the bottom
                btnOk.setVisibility(View.VISIBLE);
            }
        });

        // Set button to close the dialog when clicked
        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void updateAsterisk(EditText editTxt, TextView txtView, String newText, int resourceID){
        editTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    txtView.setText(newText);
                }

                if (charSequence.toString().isEmpty()) {
                    txtView.setText(resourceID);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // Custom TypefaceSpan class to apply different fonts
    public class CustomTypefaceSpan extends TypefaceSpan {
        private final Typeface newType;

        public CustomTypefaceSpan(Typeface type) {
            super("sans-serif");
            newType = type;
        }

        @Override
        public void updateDrawState(android.text.TextPaint textPaint) {
            applyCustomTypeFace(textPaint, newType);
        }

        @Override
        public void updateMeasureState(android.text.TextPaint paint) {
            applyCustomTypeFace(paint, newType);
        }

        private void applyCustomTypeFace(android.text.TextPaint paint, Typeface tf) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            oldStyle = (old != null) ? old.getStyle() : 0;

            int fake = oldStyle & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            paint.setTypeface(tf);
        }
    }

    public void makeTextYellow() {
        // Get reference to the TextView
        TextView termsAndPoliciesTextView = findViewById(R.id.termsAndPolicies);

        // Set the original text
        String text = "By clicking Create Account, you agree to our \nTerms and Privacy Policy";

        // Create a SpannableString with the full text
        SpannableString spannableString = new SpannableString(text);

        // Find the start and end index of the "Terms and Privacy Policy" text
        int start = text.indexOf("Terms and Privacy Policy");
        int end = start + "Terms and Privacy Policy".length();

        int yellowColor = ContextCompat.getColor(this, R.color.PLANTITOyellow);

        // Apply a yellow color to the "Terms and Privacy Policy" text
        spannableString.setSpan(new ForegroundColorSpan(yellowColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the styled text to the TextView
        termsAndPoliciesTextView.setText(spannableString);
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
            if (isConnected()) {
                noInternetDialog.dismiss();
            }
        });

        noInternetDialog.show();
    }

    public void onClickTermsAndPrivacyPolicy(View v) {
        showTermsPrivacyDialog();
    }

    public void onClickReturn(View v) {
        startActivity(new Intent(this, WelcomePage.class));
        finish();
    }

    public void onClickSignIn1(View v) {
        startActivity(new Intent(this, LoginPage.class));
        finish();
    }
}