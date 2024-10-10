package com.meliodas.plantitotita.mainmodule;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.*;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.FixedHeightViewSizer;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseAuth;
import com.meliodas.plantitotita.R;
import kotlin.io.ByteStreamsKt;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static android.content.ContentValues.TAG;

public class ArSceneActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ViewRenderable textRenderable;
    private AnchorNode currentAnchorNode;
    private TransformableNode textNode;
    private final Handler handler = new Handler();
    private static final long UPDATE_INTERVAL_MS = 3000;
    private boolean isUpdatingTextPosition = true;

    private PlantIdApi plantIdApi;
    private DatabaseManager databaseManager;

    private String plantName = "Plant Name";
    private String plantScientificName = "Plant Scientific Name";
    private Plant plant;

    private FusedLocationProviderClient fusedLocationClient;
    private AlertDialog locationDialog;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arscene);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        plantIdApi = new PlantIdApi();
        databaseManager = new DatabaseManager();
        plant = new Plant.Builder().
                name("").
                scientificName("").
                image("").
                identification("").
                description("").
                edibleParts(new ArrayList<>()).
                propagationMethods(new ArrayList<>()).
                commonUses("").
                toxicity("").
                culturalSignificance("").
                bestLightCondition("").
                bestSoilType("").
                bestWatering("").
                taxonomy(new HashMap<>()).
                build();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkIfLocationIsEnabled();
        requestLocation();
        enableInstantPlacement();

        ViewRenderable.builder()
                .setView(this, createView(plantName, plantScientificName))
                .setSizer(new FixedHeightViewSizer(0.15f))
                .build()
                .thenAccept(renderable -> {
                    textRenderable = renderable;
                    textRenderable.setShadowCaster(false);
                    textRenderable.setShadowReceiver(false);
                    startUpdatingTextPosition();
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Unable to load text renderable", throwable);
                    return null;
                });

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Re-check if location is enabled when the user comes back from the settings screen
        checkIfLocationIsEnabled();

        // Re-check if location is enabled when the user comes back from the settings screen
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGpsEnabled || isNetworkEnabled) {
            // Location is enabled, dismiss the dialog if it's showing
            if (locationDialog != null && locationDialog.isShowing()) {
                locationDialog.dismiss();
            }
            // Continue with the app's functionality
            requestLocation();
        } else {
            // Location is still disabled, show the dialog again
            showLocationAlertDialog();
        }
    }

    private View createView(String plantName, String scientificName) {
        View view = getLayoutInflater().inflate(R.layout.aadisplay, null);
        TextView plantNameText = view.findViewById(R.id.plantName);
        TextView scientificNameText = view.findViewById(R.id.plantScientificName);
        plantNameText.setText(StringUtils.capitalize(plantName));
        scientificNameText.setText(StringUtils.capitalize(scientificName));
        view.setOnClickListener(v -> {
            isUpdatingTextPosition = false; // Stop updating text position
            Intent intent = new Intent(this, PlantInformationActivity.class);
            intent.putExtra("plantName", this.plantName != null ? this.plantName : "");
            intent.putExtra("plantScientificName", plant.scientificName() != null ? plant.scientificName() : "");
            intent.putExtra("image", plant.image() != null ? plant.image() : "");
            intent.putExtra("identification", plant.identification() != null ? plant.identification() : "");
            intent.putExtra("description", plant.description() != null ? plant.description() : "");
            intent.putExtra("edibleParts", plant.edibleParts() != null ? plant.edibleParts() : new ArrayList<>());
            intent.putExtra("propagationMethods", plant.propagationMethods() != null ? plant.propagationMethods() : new ArrayList<>());
            intent.putExtra("commonUses", plant.commonUses() != null ? plant.commonUses() : "");
            intent.putExtra("toxicity", plant.toxicity() != null ? plant.toxicity() : "");
            intent.putExtra("culturalSignificance", plant.culturalSignificance() != null ? plant.culturalSignificance() : "");
            intent.putExtra("bestLightCondition", plant.bestLightCondition() != null ? plant.bestLightCondition() : "");
            intent.putExtra("bestSoilType", plant.bestSoilType() != null ? plant.bestSoilType() : "");
            intent.putExtra("bestWatering", plant.bestWatering() != null ? plant.bestWatering() : "");
            intent.putExtra("taxonomy", new HashMap<>(plant.taxonomy()));

            startActivity(intent);
        });
        return view;
    }

    private void startUpdatingTextPosition() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isUpdatingTextPosition) return;
                updateTextPosition();
                handler.postDelayed(this, UPDATE_INTERVAL_MS);

                Session session = arFragment.getArSceneView().getSession();
                Config config = session.getConfig();
                config.setFocusMode(Config.FocusMode.AUTO);
                config.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);

                session.configure(config);
            }
        }, UPDATE_INTERVAL_MS);
    }

    private void enableInstantPlacement() {
        Session session = arFragment.getArSceneView().getSession();
        if (session != null) {
            Config config = session.getConfig();
            // Enable Instant Placement mode
            config.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
            session.configure(config);
        }
    }

    // Update text position based on hitTestInstantPlacement
    private void updateTextPosition() {
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame == null || textRenderable == null) return;

        // Perform Instant Placement hit test at the center of the screen
        Point center = new Point(
                arFragment.getView().getWidth() / 2,
                arFragment.getView().getHeight() / 2
        );

        float approximateDistanceMeters = 2.0f; // Approximate distance for initial placement
        List<HitResult> hitResults = frame.hitTestInstantPlacement(center.x, center.y, approximateDistanceMeters);

        if (hitResults.isEmpty()) return;

        HitResult hit = hitResults.get(0); // Get the nearest hit result

        // Update AR object based on the Instant Placement hit result
        if (hit.getTrackable() instanceof InstantPlacementPoint instantPlacementPoint) {
            Pose hitPose = instantPlacementPoint.getPose();
            placeOrUpdateText(hitPose);
        }
    }

    // Place or update text position with Instant Placement
    private void placeOrUpdateText(Pose hitPose) {
        if (currentAnchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(currentAnchorNode);
            if (currentAnchorNode.getAnchor() != null) {
                currentAnchorNode.getAnchor().detach();
            }
            currentAnchorNode = null;
        }

        Session session = arFragment.getArSceneView().getSession();

        if (session == null) return;

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(hitPose);
        currentAnchorNode = new AnchorNode(anchor);
        textNode = new TransformableNode(arFragment.getTransformationSystem());
        textNode.setRenderable(textRenderable);
        textNode.setParent(currentAnchorNode);
        arFragment.getArSceneView().getScene().addChild(currentAnchorNode);
        textNode.select();

        updateTextRotation();
    }

    private void updateTextRotation() {
        if (textNode != null && arFragment != null &&
                arFragment.getArSceneView() != null &&
                arFragment.getArSceneView().getScene() != null &&
                arFragment.getArSceneView().getScene().getCamera() != null) {

            Vector3 cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
            Vector3 textPosition = textNode.getWorldPosition();
            Vector3 direction = Vector3.subtract(cameraPosition, textPosition);
            Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
            textNode.setWorldRotation(lookRotation);
        }
    }

    private void onSceneUpdate(FrameTime frameTime) {
        updateTextRotation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickCamera(View view) {
        ArSceneView sceneView = arFragment.getArSceneView();

        Bitmap bitmap = Bitmap.createBitmap(sceneView.getWidth(), sceneView.getHeight(), Bitmap.Config.ARGB_8888);

        HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        PixelCopy.request(sceneView, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    // Save the bitmap and get the URI
                    Uri imageUri = saveBitmapToDisk(bitmap);
                    runOnUiThread(() ->
                            Toast.makeText(ArSceneActivity.this, "Screenshot saved successfully", Toast.LENGTH_SHORT).show()
                    );

                    // Process the image with PlantID API using the saved URI
                    processImageWithPlantId(imageUri);

                } catch (IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ArSceneActivity.this, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
                    );
                    Log.e(TAG, "Failed to save bitmap", e);
                }
            } else {
                runOnUiThread(() ->
                        Toast.makeText(ArSceneActivity.this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
                );
                Log.e(TAG, "Failed to copyPixels: " + copyResult);
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    private Uri saveBitmapToDisk(Bitmap bitmap) throws IOException {
        String fileName = "AR_SCREENSHOT_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        }

        // Insert the image into MediaStore and get the Uri
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                Toast.makeText(this, "Screenshot saved to gallery", Toast.LENGTH_SHORT).show();
                return uri; // Return the URI of the saved image
            }
        }

        throw new IOException("Failed to save bitmap.");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processImageWithPlantId(Uri imageUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    throw new IOException("Failed to open InputStream from Uri.");
                }

                byte[] imageBytes = ByteStreamsKt.readBytes(inputStream);
                Plant plant = plantIdApi.identifyAndGetDetails(imageBytes, latitude, longitude);

                // Store plant identification data in the database
                databaseManager.addIdentification(plant, FirebaseAuth.getInstance().getUid());

                // Update the plant name and refresh the AR text
                plantName = plant.name();
                plantScientificName = plant.scientificName();
                this.plant = plant;
                Log.i("lopit", "Plant identified: " + plant.toString());
                runOnUiThread(() -> {
                    updateArText(plantName, plantScientificName);
                    Toast.makeText(ArSceneActivity.this, plant.toString(), Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error processing image with PlantID API", e);
                runOnUiThread(() -> Toast.makeText(ArSceneActivity.this, "Failed to identify plant", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateArText(String plantName, String plantScientificName) {
        ViewRenderable.builder()
                .setView(this, createView(plantName, plantScientificName))
                .setSizer(new FixedHeightViewSizer(0.15f))
                .build()
                .thenAccept(renderable -> {
                    textRenderable = renderable;
                    textRenderable.setShadowCaster(false);
                    textRenderable.setShadowReceiver(false);
                    if (textNode != null) {
                        textNode.setRenderable(textRenderable);
                    }
                    arFragment.getArSceneView().invalidate();
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Unable to update text renderable", throwable);
                    return null;
                });
    }

    private void requestLocation() {
        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Get the last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.d(TAG, "Current Location: Latitude = " + latitude + ", Longitude = " + longitude);
                    } else {
                        Log.e(TAG, "Location is null");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }

    private void checkIfLocationIsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            showLocationAlertDialog();
        } else {
            requestLocation();
        }
    }

    private void showLocationAlertDialog() {
        if (locationDialog != null && locationDialog.isShowing()) {
            return; // Don't create a new dialog if one is already showing
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Location")
                .setMessage("Location services are required for this feature. Please enable location services.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    // Launch the location settings
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Dismiss the dialog and redirect to the HomePage activity
                    dialog.dismiss();
                    Toast.makeText(ArSceneActivity.this, "Location services are disabled", Toast.LENGTH_SHORT).show();
                    // Redirect user to HomePage activity
                    Intent intent = new Intent(ArSceneActivity.this, HomePage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish ArSceneActivity so the user cannot go back to it without enabling location
                })
                .setCancelable(false);

        locationDialog = builder.create();
        locationDialog.show();
    }

    public void onClickReturnAR(View v) {
        startActivity(new Intent(getApplicationContext(), HomePage.class));
        finish();
    }

}