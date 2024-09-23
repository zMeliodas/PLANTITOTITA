package com.meliodas.plantitotita.mainmodule;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class ArSceneActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ViewRenderable textRenderable;
    private AnchorNode currentAnchorNode;
    private TransformableNode textNode;
    private final Handler handler = new Handler();
    private static final long UPDATE_INTERVAL_MS = 3000;

    private PlantIdApi plantIdApi;
    private DatabaseManager databaseManager;

    private String plantName = "Plant Name";
    private Plant plant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arscene);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        plantIdApi = new PlantIdApi();
        databaseManager = new DatabaseManager();

        if (arFragment == null) {
            throw new IllegalStateException("Cannot find AR fragment");
        }

        plant = new Plant("", "", "", "", "", "", "", "", List.of());
        arFragment.getPlaneDiscoveryController().hide();

        ViewRenderable.builder()
                .setView(this, createView(plantName))
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
    }

    private View createView(String text) {
        View view = getLayoutInflater().inflate(R.layout.aadisplay, null);
        TextView textView = view.findViewById(R.id.plantName);
        textView.setText(text);
        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlantInformationActivity.class);
            intent.putExtra("plantName", plantName);
            intent.putExtra("identification", plant.identification() != null ? plant.identification() : "");
            intent.putExtra("description", plant.description() != null ? plant.description() : "");
            startActivity(intent);
        });
        return view;
    }

    private void startUpdatingTextPosition() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTextPosition();
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        }, UPDATE_INTERVAL_MS);
    }

    private void updateTextPosition() {
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame == null || textRenderable == null) return;

        Point center = new Point(
                arFragment.getView().getWidth() / 2,
                arFragment.getView().getHeight() / 2
        );

        List<HitResult> hitResults = frame.hitTest(center.x, center.y);
        if (hitResults.isEmpty()) return;

        HitResult nearestFloorHit = null;
        float nearestDistance = Float.MAX_VALUE;

        for (HitResult hit : hitResults) {
            Plane plane = hit.getTrackable() instanceof Plane ? (Plane) hit.getTrackable() : null;
            if (plane != null && plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING
                    && plane.getTrackingState() == TrackingState.TRACKING) {
                float[] hitPoseTranslation = hit.getHitPose().getTranslation();
                float[] cameraPoseTranslation = frame.getCamera().getPose().getTranslation();

                float distance = calculateDistance(hitPoseTranslation, cameraPoseTranslation);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestFloorHit = hit;
                }
            }
        }

        if (nearestFloorHit != null) {
            Pose hitPose = nearestFloorHit.getHitPose();
            placeOrUpdateText(hitPose);
        }
    }

    private float calculateDistance(float[] point1, float[] point2) {
        float dx = point1[0] - point2[0];
        float dy = point1[1] - point2[1];
        float dz = point1[2] - point2[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void placeOrUpdateText(Pose hitPose) {
        if (currentAnchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(currentAnchorNode);
            currentAnchorNode.getAnchor().detach();
        }

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
        if (textNode != null && arFragment.getArSceneView().getScene().getCamera() != null) {
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
                Plant plant = plantIdApi.identifyAndGetDetails(imageBytes, 0, 0);

                // Store plant identification data in the database
                databaseManager.addIdentification(plant, FirebaseAuth.getInstance().getUid());

                // Update the plant name and refresh the AR text
                plantName = plant.name();
                this.plant = plant;
                runOnUiThread(() -> {
                    updateArText(plantName);
                    Toast.makeText(ArSceneActivity.this, plant.toString(), Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error processing image with PlantID API", e);
                runOnUiThread(() -> Toast.makeText(ArSceneActivity.this, "Failed to identify plant", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateArText(String newText) {
        ViewRenderable.builder()
                .setView(this, createView(newText))
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


}