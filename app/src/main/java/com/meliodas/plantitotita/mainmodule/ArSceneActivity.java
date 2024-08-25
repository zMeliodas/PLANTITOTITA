package com.meliodas.plantitotita.mainmodule;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.FixedHeightViewSizer;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.meliodas.plantitotita.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arscene);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        if (arFragment == null) {
            throw new IllegalStateException("Cannot find AR fragment");
        }

        arFragment.getPlaneDiscoveryController().hide();

        ViewRenderable.builder()
                .setView(this, createView("PLANTITOTITA"))
                .setSizer(new FixedHeightViewSizer(0.15f))
                .build()
                .thenAccept(renderable -> {
                    textRenderable = renderable;
                    startUpdatingTextPosition();
                })
                .exceptionally(throwable -> {
                    Log.e("ArSceneActivity", "Unable to load text renderable", throwable);
                    return null;
                });

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
    }

    private View createView(String text) {
        View view = getLayoutInflater().inflate(R.layout.aadisplay, null);

        TextView textView = view.findViewById(R.id.plantName);
        textView.setText(text);

        view.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
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

        android.graphics.Point center = new android.graphics.Point(
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

    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 200;

    public void onClickCamera(View view) {
        if (arFragment.getArSceneView().getArFrame() == null) {
            return;
        }

        captureImageWithRetry(0);
    }

    private void captureImageWithRetry(int retryCount) {
        if (retryCount >= MAX_RETRIES) {
            Log.e(TAG, "Failed to capture image after " + MAX_RETRIES + " attempts");
            Toast.makeText(this, "Failed to capture image. Please try again.", Toast.LENGTH_SHORT).show();
            try {
                arFragment.getArSceneView().resume();
            } catch (CameraNotAvailableException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        try {
            Image image = arFragment.getArSceneView().getArFrame().acquireCameraImage();
            processAndSaveImage(image);
        } catch (NotYetAvailableException e) {
            Log.w(TAG, "Image not yet available, retrying...");
            new Handler().postDelayed(() -> captureImageWithRetry(retryCount + 1), RETRY_DELAY_MS);
        }
    }

    private void processAndSaveImage(Image image) {
        try (image) {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

            File imageFile = createImageFile();
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}