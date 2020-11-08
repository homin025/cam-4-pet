package com.example.cam4pet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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


public class PetActivity extends AppCompatActivity {
    private static final String TAG = "PetActivity";

    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ArSceneView arSceneView;
    private AnchorNode mAnchorNode;

    private boolean isObjectCreated = false;

    public ModelRenderable andyRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!checkIsSupportedDevice(this)) {
            return;
        }

        setContentView(R.layout.activity_pet);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        arSceneView = arFragment.getArSceneView();

        setUpModel();

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
    }

    private void createObject() {
        if(arSceneView.getArFrame() == null){
            Log.d(TAG, "onUpdate: No frame available");
            return;
        }

        if (arSceneView.getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            Log.d(TAG, "onUpdate: Tracking not started yet");
            return;
        }

        int width = arSceneView.getWidth();
        int height = arSceneView.getHeight();

        //Ray ray = arSceneView.getScene().getCamera().screenPointToRay(width / 2, height / 2);
        //Vector3 p = ray.getPoint(1f);

        List<HitResult> hits = arSceneView.getArFrame().hitTest(width/2f, height/2f);
        if(hits.size() != 0){
            HitResult hit = hits.get(0);

            Anchor anchor = hit.createAnchor();
            mAnchorNode = new AnchorNode(anchor);
            mAnchorNode.setParent(arSceneView.getScene());

            Node n = new Node();
            n.setRenderable(andyRenderable);
            n.setParent(mAnchorNode);

            isObjectCreated = true;
        }
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
    }

    private void onSceneUpdate(FrameTime frameTime) {
        if(!isObjectCreated){
            createObject();
        }

//        try {
//            final Image image = arFragment.getArSceneView().getArFrame().acquireCameraImage();
//        } catch (NotYetAvailableException e) {
//            e.printStackTrace();
//            Log.i(TAG, "onUpdate: No image available");
//        } finally {
//            Log.i(TAG, "onUpdate: Image available");
//        }
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
}