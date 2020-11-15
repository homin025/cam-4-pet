package com.example.cam4pet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Pose;
import com.google.ar.core.Plane;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.List;
import java.util.Objects;


public class PetActivity extends AppCompatActivity implements DetectFragment.DetectEventListener {
    private static final String TAG = "PetActivity";

    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private DetectFragment detectFragment;

    private AnchorNode mAnchorNode;
    public ModelRenderable andyRenderable;
    public ModelRenderable dogbowlRenderable;

    public int viewWidth, viewHeight;
    public int imageWidth, imageHeight;

    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!checkIsSupportedDevice(this)) {
            return;
        }

        setContentView(R.layout.activity_pet);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        detectFragment = DetectFragment.newInstance(this);
        detectFragment.setDetectEventListener(this);

        setUpModel();

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
    }

    private void createObject(RectF location) {
        if(arFragment.getArSceneView().getArFrame() == null) {
            Log.d(TAG, "onUpdate: No frame available");
            return;
        }

        if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            Log.d(TAG, "onUpdate: Tracking not started yet");
            return;
        }

        location = transformRectF(location);

        //////////////////////////////



        //////////////////////////////

        List<HitResult> hits = arFragment.getArSceneView().getArFrame().hitTest(viewWidth/2f, viewHeight/2f);
        if(hits.size() != 0) {
            HitResult hit = hits.get(0);

            Anchor anchor = hit.createAnchor();
            mAnchorNode = new AnchorNode(anchor);
            mAnchorNode.setParent(arFragment.getArSceneView().getScene());

            Node n = new Node();
            n.setRenderable(dogbowlRenderable);
            n.setParent(mAnchorNode);

            n.setOnTapListener(new Node.OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    Intent intent = new Intent(PetActivity.this, PopupActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private RectF transformRectF(RectF location) {
        float left = location.left;
        float top = location.top;
        float right = location.right;
        float bottom = location.bottom;

        float ratioWidth = (float) viewWidth / imageWidth;
        float ratioHeight = (float) viewHeight / imageHeight;

        return new RectF(left * ratioWidth, top * ratioHeight, right * ratioWidth, bottom * ratioHeight);
    }

    private void setUpModel() {
        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build()
                .thenAccept(modelRenderable -> andyRenderable = modelRenderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );
        ModelRenderable.builder()
                .setSource(this, R.raw.dogbowl)
                .build()
                .thenAccept(modelRenderable -> dogbowlRenderable = modelRenderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );
    }

    private void onSceneUpdate(FrameTime frameTime) {
        Image image = null;
        try {
            image = Objects.requireNonNull(arFragment.getArSceneView().getArFrame()).acquireCameraImage();
        } catch (NotYetAvailableException e) {
            Log.i(TAG, "onUpdate: No image available");
            e.printStackTrace();
            return;
        } finally {
            Log.i(TAG, "onUpdate: Image available");
        }

        if(!isInitialized) {
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            detectFragment.setDesiredPreviewFrameSize(new Size(imageWidth, imageHeight));
            detectFragment.onPreviewSizeChosen(new Size(imageWidth, imageHeight), 90);

            viewWidth = arFragment.getArSceneView().getWidth();
            viewHeight = arFragment.getArSceneView().getHeight();

            isInitialized = true;
        }

        if(image != null) {
            detectFragment.getImagefromCamera(image);
        }
    }

    public static boolean checkIsSupportedDevice(final Activity activity) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            activity.finish();
            return false;
        }

        String openGLVersionString = ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                .getDeviceConfigurationInfo()
                .getGlEsVersion();

        if(Double.parseDouble(openGLVersionString) < MIN_OPENGL_VERSION) {
            activity.finish();
            return false;
        }

        return true;
    }

    @Override
    public void onPetDetected(RectF location) {
        createObject(location);
    }
}