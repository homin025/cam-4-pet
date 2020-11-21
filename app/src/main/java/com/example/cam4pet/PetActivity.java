package com.example.cam4pet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PetActivity extends AppCompatActivity implements DetectFragment.DetectEventListener {
    private static final String TAG = "PetActivity";

    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private DetectFragment detectFragment;

    public ModelRenderable dogbowlRenderable;
    public ModelRenderable ballRenderable;
    public ModelRenderable boneRenderable;

    public ArrayList<Node> objects;

    public int viewWidth, viewHeight;
    public int imageWidth, imageHeight;
    private boolean isProcessingObject = false;

    private boolean isInitialized = false;

    // UI
    private ImageButton button[] = new ImageButton[3];
    private ImageButton btnReset, btnBack;

    public boolean isBtnPressed = false;

    // Put extra
    public int btnNum = 2;
    public int checkNum; // 0-food, 1-snack, 2-toy

    private ImageView ad_imageView;

    private Node model = null;

    public boolean isCreated = false;

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

        objects = new ArrayList<>();

        setUpModel();

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);

        // 레이아웃을 위에 겹쳐서 올리는 부분
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 광고 레이아웃
        // 레이아웃 객체 생성
        RelativeLayout ad_layout = (RelativeLayout) inflater.inflate(R.layout.layout_advertisement, null);
        // 레이아웃 배경 투명도 주기
        ad_layout.setBackgroundColor(Color.parseColor("#00000000"));
        // 레이아웃 위에 겹치기
        RelativeLayout.LayoutParams paramll = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        addContentView(ad_layout, paramll);

        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Node object : objects) {
                    arFragment.getArSceneView().getScene().removeChild(object);
                    object.setParent(null);
                }
                isCreated = false;
                objects.clear();
            }
        });

        ad_imageView = findViewById(R.id.ad_imageView);
        btnBack = findViewById(R.id.btnBack);

        ad_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
                intent.putExtra("kinds", checkNum);
                intent.putExtra("num", btnNum);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button[0].setImageResource(R.drawable.img_dog_bowl);
                button[1].setImageResource(R.drawable.img_dog_snack);
                button[2].setImageResource(R.drawable.img_dog_toy);
                btnBack.setVisibility(View.INVISIBLE);
                isBtnPressed = false;
            }
        });

        for(int i = 0; i<3; i++){
            switch(i){
                case 0: button[i] = findViewById(R.id.btn01); break;
                case 1: button[i] = findViewById(R.id.btn02); break;
                case 2: button[i] = findViewById(R.id.btn03); break;
            }
        }

      //  ad_imageView.setVisibility(View.INVISIBLE);
        btnBack.setVisibility(View.INVISIBLE);

        // checkNum 0: food, 1: snack, 2: toy
        // btnNum 0: 1번째 버튼, 1: 2번째 버튼, 2: 3번째 버튼
        button[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null && !isBtnPressed) {
                    model.setRenderable(dogbowlRenderable);
                    model.setLocalScale(new Vector3(0.8f, 0.8f, 0.8f));
                }

                if(!isBtnPressed)
                {
                    button[0].setImageResource(R.drawable.img_dog_food_01);
                    button[1].setImageResource(R.drawable.img_dog_food_02);
                    button[2].setImageResource(R.drawable.img_dog_food_03);
                    btnBack.setVisibility(View.VISIBLE);
                    isBtnPressed = true;
                    checkNum = 0;
                }
                else
                {
                    btnNum = 0;
                    switch (checkNum){ // 각 종류의 1번 상품-> 우측 상단 이미지 변환
                        case 0: ad_imageView.setImageResource(R.drawable.img_dog_food_01); break;
                        case 1: ad_imageView.setImageResource(R.drawable.img_dog_snack_01); break;
                        case 2: ad_imageView.setImageResource(R.drawable.img_dog_toy_01); break;
                    }
                }
            }
        });

        button[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null && !isBtnPressed) {
                    model.setRenderable(boneRenderable);
                    model.setLocalScale(new Vector3(1.2f, 1.2f, 1.2f));
                }

                if(!isBtnPressed)
                {
                    button[0].setImageResource(R.drawable.img_dog_snack_01);
                    button[1].setImageResource(R.drawable.img_dog_snack_02);
                    button[2].setImageResource(R.drawable.img_dog_snack_03);
                    btnBack.setVisibility(View.VISIBLE);
                    isBtnPressed = true;
                    checkNum = 1;
                }
                else
                {
                    btnNum = 1;
                    switch (checkNum){ // 각 종류의 2번 상품-> 우측 상단 이미지 변환
                        case 0: ad_imageView.setImageResource(R.drawable.img_dog_food_02); break;
                        case 1: ad_imageView.setImageResource(R.drawable.img_dog_snack_02); break;
                        case 2: ad_imageView.setImageResource(R.drawable.img_dog_toy_02); break;
                    }
                }
            }
        });

        button[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null && !isBtnPressed) {
                    model.setRenderable(ballRenderable);
                    model.setLocalScale(new Vector3(1.2f, 1.2f, 1.2f));
                }

                if(!isBtnPressed){
                    button[0].setImageResource(R.drawable.img_dog_toy_01);
                    button[1].setImageResource(R.drawable.img_dog_toy_02);
                    button[2].setImageResource(R.drawable.img_dog_toy_03);
                    btnBack.setVisibility(View.VISIBLE);
                    isBtnPressed = true;
                    checkNum = 2;
                }
                else
                {
                    btnNum = 2;
                    switch (checkNum){ // 각 종류의 3번 상품-> 우측 상단 이미지 변환
                        case 0: ad_imageView.setImageResource(R.drawable.img_dog_food_03); break;
                        case 1: ad_imageView.setImageResource(R.drawable.img_dog_snack_03); break;
                        case 2: ad_imageView.setImageResource(R.drawable.img_dog_toy_03); break;
                    }

                }
            }
        });
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

        int wOffset = viewWidth / 10;
        int hOffset = viewHeight / 20;

        int l = (int) location.left / wOffset;
        int r = (int) location.right / wOffset;
        int t = (int) location.top / hOffset;
        int b = (int) location.bottom / hOffset;

        int w = (int) (Math.random() * ((r + 1) - (l - 1))) + (l - 1);
        int max_b = b + 5 > 20 ? 20 : b + 5;
        int h = (int) (Math.random() * (max_b - (b))) + (b);

        if(w <= 0 || h <= 0 || w >= 10 || h >= 20) return;

        List<HitResult> hits = arFragment.getArSceneView().getArFrame().hitTest(w * wOffset, h * hOffset);

        Log.i("DEBUG", "<RAY POINT> x: " + w * wOffset + ", y: " + h * hOffset);

        if(hits.size() != 0) {
            HitResult hit = hits.get(0);

            Anchor anchor = hit.createAnchor();
            AnchorNode mAnchorNode = new AnchorNode(anchor);
            mAnchorNode.setParent(arFragment.getArSceneView().getScene());

            if(isCreated){
                Vector3 start = model.getWorldPosition();
                Vector3 end = mAnchorNode.getWorldPosition();

                Vector3 v = Vector3.subtract(end, start);

                if(Vector3.dot(v, v) >= 1 * 1){
                    model.setParent(null);
                }
                else{
                    mAnchorNode.setParent(null);
                    return;
                }
            }

            Node n = new Node();
            model = n;
            n.setRenderable(dogbowlRenderable);
            n.setParent(mAnchorNode);

            n.setLocalScale(new Vector3(0.8f, 0.8f, 0.8f));

            Vector3 v = Quaternion.rotateVector(n.getWorldRotation(), new Vector3(0, 1, 0));

            if(Math.abs(Vector3.angleBetweenVectors(v, new Vector3(0, 1, 0))) > 10){
                Log.i("DEBUG", "DELETE theta: " + Math.abs(Vector3.angleBetweenVectors(v, new Vector3(0, 1, 0))));

                n.setParent(null);
                mAnchorNode.setParent(null);
                return;
            }

            Log.i("DEBUG", "theta: " + Math.abs(Vector3.angleBetweenVectors(v, new Vector3(0, 1, 0))));

            n.setOnTapListener(new Node.OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    Intent intent = new Intent(PetActivity.this, PopupActivity.class);
                    intent.putExtra("kinds", checkNum);
                    intent.putExtra("num", btnNum);
                    startActivity(intent);
                }
            });

            isCreated = true;
            Log.i("DEBUG", "<CREATE>");
            objects.add(n);
        }
    }

    private void setUpModel() {
        ModelRenderable.builder()
                .setSource(this, R.raw.bowl)
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

        ModelRenderable.builder()
                .setSource(this, R.raw.ball)
                .build()
                .thenAccept(modelRenderable -> ballRenderable = modelRenderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        ModelRenderable.builder()
                .setSource(this, R.raw.bone)
                .build()
                .thenAccept(modelRenderable -> boneRenderable = modelRenderable)
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
//            Log.i(TAG, "onUpdate: No image available");
            e.printStackTrace();
            return;
        } finally {
//            Log.i(TAG, "onUpdate: Image available");
        }

        if(!isInitialized) {
            viewWidth = arFragment.getArSceneView().getWidth();
            viewHeight = arFragment.getArSceneView().getHeight();

            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            detectFragment.setDesiredPreviewFrameSize(new Size(imageWidth, imageHeight));
            detectFragment.onPreviewSizeChosen(new Size(imageWidth, imageHeight), 90);
            detectFragment.setDesiredRatio((float) viewWidth / imageWidth, (float) viewHeight / imageHeight);

            Log.i(TAG, "View Size: " + viewWidth + " X " + viewHeight + " Image Size: " + imageWidth + " X " + imageHeight);

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
        if(isProcessingObject) {
            return;
        }

        isProcessingObject = true;
        createObject(location);
        isProcessingObject = false;
    }
}