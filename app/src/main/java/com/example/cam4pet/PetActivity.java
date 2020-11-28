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
import android.widget.TextView;
import android.widget.Toast;

import com.example.cam4pet.util.Logger;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;

import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.collision.Ray;
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

    private Toast toast;

    public ModelRenderable dogbowlRenderable;
    public ModelRenderable ballRenderable;
    public ModelRenderable boneRenderable;
    public ModelRenderable canRenderable;
    public ModelRenderable boxRenderable;
    public ModelRenderable mouseRenderable;

    public ArrayList<Node> objects;

    public int viewWidth, viewHeight;
    public int imageWidth, imageHeight;
    private boolean isProcessingObject = false;

    private boolean isInitialized = false;
    public boolean dogDetected = false;
    public boolean catDetected = false;
    public int isChanged = 0;

    // UI
    private ImageButton button[] = new ImageButton[3];
    private ImageButton btnReset;
    private TextView toastView;
    private ImageView icon;
    private ImageView reset;

    public boolean isBtnPressed = false;

    // Put extra
    public int btnNum = 2;
    public int checkNum; // 0-food, 1-snack, 2-toy
    public int checkDogCat; //dog - 0, cat -1


    private ImageView ad_imageView;

    private Node model = null;
    private AnchorNode modelParent = null;

    public boolean isCreated = false;
    private Vector3 offset;
    private Vector3 target = null;

    private RectF anchorBox = null;


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

        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

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

        toastView = findViewById(R.id.toastView);

        icon = findViewById(R.id.ic_dog);
        reset = findViewById(R.id.img_reset);

        icon.setVisibility(View.INVISIBLE);
        reset.setVisibility(View.INVISIBLE);

        btnReset = findViewById(R.id.btnReset);
        btnReset.setVisibility(View.INVISIBLE);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Node object : objects) {
                    arFragment.getArSceneView().getScene().removeChild(object);
                    object.setParent(null);
                }
                isCreated = false;

                objects.clear();

                ad_imageView.setImageResource(0);
                checkNum = 3;
            }
        });

        ad_imageView = findViewById(R.id.ad_imageView);

        ad_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkNum != 3){
                    Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
                    intent.putExtra("kinds", checkNum);
                    intent.putExtra("num", btnNum);
                    intent.putExtra("checkDogCat", checkDogCat);
                    startActivity(intent);
                }
            }
        });
        //button 생성 & 인식 전엔 invisible
        for(int i = 0; i<3; i++){
            switch(i){
                case 0: button[i] = findViewById(R.id.btn01);break;
                case 1: button[i] = findViewById(R.id.btn02); break;
                case 2: button[i] = findViewById(R.id.btn03); break;
            }
            button[i].setVisibility(View.INVISIBLE);
        }

        // checkNum 0: food, 1: snack, 2: toy
        // btnNum 0: 1번째 버튼, 1: 2번째 버튼, 2: 3번째 버튼
        // checkDogCat 0: dog, 1: cat // dog, cat 구별

        button[0].setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(model != null && !isBtnPressed && dogDetected) {
                    model.setRenderable(dogbowlRenderable);
                    model.setLocalScale(new Vector3(0.8f, 0.8f, 0.8f));
                }
                else if(model != null && !isBtnPressed && catDetected){
                    model.setRenderable(canRenderable);
                    model.setLocalScale(new Vector3(1.3f, 1.3f, 1.3f));
                }

                checkNum = 0;
                btnNum =(int) (Math.random() * 3); // 3개 상품 랜덤 전환
                setUpAdView(checkDogCat, checkNum, btnNum);

            }
        });

        button[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null && !isBtnPressed && dogDetected) {
                    model.setRenderable(boneRenderable);
                    model.setLocalScale(new Vector3(1.2f, 1.2f, 1.2f));
                }
                else if(model != null && !isBtnPressed && catDetected){
                    model.setRenderable(mouseRenderable);
                    model.setLocalScale(new Vector3(0.8f, 0.8f, 0.8f));
                }


                checkNum = 1;
                btnNum =(int) (Math.random() * 3); // 3개 상품 랜덤 전환
                setUpAdView(checkDogCat, checkNum, btnNum);
            }
        });

        button[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null && !isBtnPressed && dogDetected) {
                    model.setRenderable(ballRenderable);
                    model.setLocalScale(new Vector3(1.2f, 1.2f, 1.2f));
                }
                else if(model != null && !isBtnPressed && catDetected){
                    model.setRenderable(boxRenderable);
                }

                checkNum = 2;
                btnNum =(int) (Math.random() * 3); // 3개 상품 랜덤 전환

                setUpAdView(checkDogCat, checkNum, btnNum);

            }
        });
    }

    private void createObject(RectF location) {
        anchorBox = location;

        Log.i("DEBUG", "<DETECTED>");

        if(arFragment.getArSceneView().getArFrame() == null) {
            Log.d("DEBUG", "onUpdate: No frame available");
            return;
        }

        if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            Log.d("DEBUG", "onUpdate: Tracking not started yet");
            return;
        }

        //button visible & cat,dog-> Btn contents 결정
        toastView.setVisibility(View.INVISIBLE);
        btnReset.setVisibility(View.VISIBLE);
        reset.setVisibility(View.VISIBLE);
        icon.setVisibility(View.VISIBLE);

        for(int i = 0; i<3; i++) {
            button[i].setVisibility(View.VISIBLE);
            if (dogDetected) {
                icon.setImageResource(R.drawable.ic_dog);
                switch (i) {
                    case 0:
                        button[i].setImageResource(R.drawable.img_dog_bowl); break;
                    case 1:
                        button[i].setImageResource(R.drawable.img_dog_snack); break;
                    case 2:
                        button[i].setImageResource(R.drawable.img_dog_toy); break;
                }
            } else {
                icon.setImageResource(R.drawable.ic_cat);
                switch (i) {
                    case 0:
                        button[i].setImageResource(R.drawable.img_cat_food); break;
                    case 1:
                        button[i].setImageResource(R.drawable.img_cat_toy); break;
                    case 2:
                        button[i].setImageResource(R.drawable.img_cat_house); break;
                }
            }
        }

        // 수정쓰!
        if(!isCreated) {

            float w = (location.left + location.right) / 2f;
            float h = (location.bottom + 75);
            h = h > viewHeight ? viewHeight : h;

            List<HitResult> hits = arFragment.getArSceneView().getArFrame().hitTest(w, h);

            if(hits.size() != 0) {
                //Log.i("DEBUG", "TEST");
                HitResult hit = hits.get(0);

                Anchor anchor = hit.createAnchor();
                AnchorNode mAnchorNode = new AnchorNode(anchor);
                mAnchorNode.setParent(arFragment.getArSceneView().getScene());

                // test : 평면에 anchor를 생성했기에 점프하는 것일 수 있다 => 뮤직노트때는 안그랬으므로

                Vector3 position = mAnchorNode.getWorldPosition();
                mAnchorNode.setParent(null);

                Pose pose = Pose.makeTranslation(position.x, position.y, position.z);
                anchor = arFragment.getArSceneView().getSession().createAnchor(pose);

                mAnchorNode = new AnchorNode(anchor);
                mAnchorNode.setParent(arFragment.getArSceneView().getScene());

                Node n = new Node();
                model = n;

                //dog & cat => 처음 자동 생성 ModelRenderable
                if(dogDetected){
                    n.setRenderable(dogbowlRenderable);
                    n.setLocalScale(new Vector3(0.7f, 0.7f, 0.7f));
                }
                else{//cat
                    n.setRenderable(canRenderable);
                    n.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 30f));
                }
                n.setParent(mAnchorNode);
                modelParent = mAnchorNode;

               // n.setLocalScale(new Vector3(1.5f, 1.5f, 1.5f));

                Vector3 v = Quaternion.rotateVector(n.getWorldRotation(), new Vector3(0, 1, 0));

                if(Math.abs(Vector3.angleBetweenVectors(v, new Vector3(0, 1, 0))) > 10){
                    //Log.i("DEBUG", "DELETE theta: " + Math.abs(Vector3.angleBetweenVectors(v, new Vector3(0, 1, 0))));

                    n.setParent(null);
                    mAnchorNode.setParent(null);
                    return;
                }

                //Log.i("DEBUG", "theta: " + Math.abs(Vector3.angleBetweenVectors(v, new Vector3(0, 1, 0))));

                n.setOnTapListener(new Node.OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        Intent intent = new Intent(PetActivity.this, PopupActivity.class);
                        intent.putExtra("kinds", checkNum);
                        intent.putExtra("num", btnNum);
                        intent.putExtra("checkDogCat", checkDogCat);
                        startActivity(intent);
                    }
                });

                w = (location.left + location.right) / 2f;
                h = (location.bottom);
                h = h > viewHeight ? viewHeight : h;

                hits = arFragment.getArSceneView().getArFrame().hitTest(w, h);

                if(hits.size() != 0) {
                    hit = hits.get(0);

                    anchor = hit.createAnchor();
                    mAnchorNode = new AnchorNode(anchor);
                    mAnchorNode.setParent(arFragment.getArSceneView().getScene());

                    Vector3 start = mAnchorNode.getWorldPosition();
                    Vector3 end = model.getWorldPosition();
                    offset = Vector3.subtract(end, start);
                }
                else{
                    mAnchorNode.setParent(null);
                    model.setParent(null);
                    model = null;
                    Log.i("DEBUG", "<NOT CREATED>");
                    return;
                }

                isCreated = true;
                Log.i("DEBUG", "<CREATE>");
                objects.add(n);
            }
        }
        else{
            // model이 갑자기 사라졌다면
            if(modelParent.getAnchor().getTrackingState() != TrackingState.TRACKING
                    && arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() == TrackingState.TRACKING){
                Log.i("DEBUG", "<NOT TRACKING>");
                // Detach the old anchor
                /*
                AnchorNode m = (AnchorNode)model.getParent();
                m.removeChild(model);
                model.setParent(null);
                arFragment.getArSceneView().getScene().removeChild(m);
                m.getAnchor().detach();
                m.setParent(null);*/

                /*Vector3 pos = model.getWorldPosition();*/
            }

            // target 위치 이동
            float w = (location.left + location.right) / 2f;
            float h = location.bottom;

            List<HitResult> hits = arFragment.getArSceneView().getArFrame().hitTest(w, h);

            if(hits.size() != 0) {
                HitResult hit = hits.get(0);

                Anchor anchor = hit.createAnchor();
                AnchorNode mAnchorNode = new AnchorNode(anchor);
                mAnchorNode.setParent(arFragment.getArSceneView().getScene());

                Vector3 temp;
                temp = mAnchorNode.getWorldPosition();
                temp = Vector3.add(temp, offset);

                mAnchorNode.setParent(null);

                Vector3 currentPos = model.getWorldPosition();
                float distance = Vector3.subtract(temp, currentPos).length();

                if(distance >= 0.25){ // 25cm이상 움직였으면 target위치를 바꿈, 안움직였으면 위치 그대로
                    target = temp;
                    //model.getParent().setWorldPosition(target);

                    Log.i("DEBUG", "<CHANGE TARGET>");
                }
                else{
                    Log.i("DEBUG", "<STAY TARGET>");
                }
            }
            else{ // hit한 평면이 없다면 이를 고려해서 대충이라도 target 설정
                Camera camera = arFragment.getArSceneView().getScene().getCamera();
                Ray ray =  camera.screenPointToRay(w, h);

                Vector3 cam2obj = Vector3.subtract(model.getWorldPosition(), camera.getWorldPosition());
                float distance = cam2obj.length();

                Vector3 point = ray.getPoint(distance);
                Vector3 temp = Vector3.add(point, offset);

                Vector3 currentPos = model.getWorldPosition();
                distance = Vector3.subtract(temp, currentPos).length();

                if(distance >= 0.25){ // 25cm이상 움직였으면 target위치를 바꿈, 안움직였으면 위치 그대로
                    target = temp;
                    Log.i("DEBUG", "<CHANGE TARGET>");
                }
                else{
                    Log.i("DEBUG", "<STAY TARGET>");
                }
            }
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

        ModelRenderable.builder()
                .setSource(this, R.raw.catfood)
                .build()
                .thenAccept(modelRenderable -> canRenderable = modelRenderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        ModelRenderable.builder()
                .setSource(this, R.raw.emptybox)
                .build()
                .thenAccept(modelRenderable -> boxRenderable = modelRenderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );
        ModelRenderable.builder()
                .setSource(this, R.raw.mouse)
                .build()
                .thenAccept(modelRenderable -> mouseRenderable = modelRenderable)
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
        // 이진영이 추가했으~!
        if(isCreated){
            if(target != null){
                Vector3 currentPos = model.getWorldPosition();

                float speed;

                float distance = Vector3.subtract(target, currentPos).length();

                if(distance <= 0.1f){ // 남은 거리가 10cm 이하면 그냥 target위치로 설정
                    model.setWorldPosition(target);
                    //model.getParent().setWorldPosition(target);
                    Log.i("DEBUG", "<TELEPORT>");
                }else{
                    speed = Math.min(Math.max(2f, distance), 5f);

                    Vector3 dir = Vector3.subtract(target, currentPos).normalized().scaled(speed * frameTime.getDeltaSeconds());

                    model.setWorldPosition(Vector3.add(currentPos, dir));
                    //model.getParent().setWorldPosition(Vector3.add(currentPos, dir));
                    Log.i("DEBUG", "<ONLY MOVE>");
                }
            }

            /*
            Camera camera = arFragment.getArSceneView().getScene().getCamera();
            Vector3 cameraPos = camera.getWorldPosition();
            Vector3 forward = camera.getForward();
            Vector3 pos = Vector3.add(cameraPos, forward);
            modelParent.setWorldPosition(pos);
            */

            /**
            if(anchorBox != null){
                Vector3 screenPoint = arFragment.getArSceneView().getScene().getCamera().worldToScreenPoint(model.getWorldPosition());

                // anchor box와 model이 겹치면 ---> target위치 변경?
                if(anchorBox.left <= screenPoint.x && screenPoint.x <= anchorBox.right
                && anchorBox.top <= screenPoint.y && screenPoint.y <= anchorBox.bottom){

                }
            }*/
        }


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

            //Log.i(TAG, "View Size: " + viewWidth + " X " + viewHeight + " Image Size: " + imageWidth + " X " + imageHeight);

            isInitialized = true;
        }

        if(image != null) {
            detectFragment.getImagefromCamera(image);
        }
        //dog->cat cat->dog change moment
        if(isChanged != 0){
            for (Node object : objects) {
                arFragment.getArSceneView().getScene().removeChild(object);
                object.setParent(null);
            }
            isCreated = false;

            objects.clear();

            ad_imageView.setImageResource(0);
            checkNum = 3;
            isChanged = 0;
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
    public void onPetDetected(String result, RectF location) {
        if(isProcessingObject) {
            return;
        }

        isProcessingObject = true;

        toast.setText(result + " 가 인식되었습니다.");
        toast.show();
        //dog -> cat 1
        //cat -> dog 2

        if(result.equals("dog")){
            if(catDetected && isCreated){ //cat->dog
                isChanged = 2;
            }
            dogDetected = true;
            catDetected = false;
            checkDogCat = 0;
        }
        else if(result.equals("cat")){
            if(dogDetected && isCreated){//dog ->cat
                isChanged = 1;
            }
            catDetected = true;
            dogDetected = false;
            checkDogCat = 1;
        }

        createObject(location);
        isProcessingObject = false;
    }

    private void setUpAdView(int checkDogCat, int checkNum,int btnNum){
        //checkDogCat 0: dog, 1: cat
        if(checkDogCat == 0){//dog
            if(checkNum == 0) {//dog->food button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_dog_food_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_dog_food_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_dog_food_03); break;
                }
            }
            else if(checkNum == 1){//dog->snack button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_dog_snack_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_dog_snack_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_dog_snack_03); break;
                }
            }
            else{//dog->toy button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_dog_toy_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_dog_toy_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_dog_toy_03); break;
                }
            }
        }
        else{//cat
            if(checkNum == 0) {//cat->food button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_cat_food_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_cat_food_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_cat_food_03); break;
                }
            }
            else if(checkNum == 1){//cat->toy button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_cat_toy_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_cat_toy_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_cat_toy_03); break;
                }
            }
            else{//cat->house button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_cat_house_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_cat_house_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_cat_house_03); break;
                }
            }
        }
    }
}