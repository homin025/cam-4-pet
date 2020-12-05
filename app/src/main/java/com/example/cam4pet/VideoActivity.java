package com.example.cam4pet;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;

import static android.provider.CalendarContract.CalendarCache.URI;


public class VideoActivity extends AppCompatActivity implements DetectFragment.DetectEventListener {
    private static final String TAG = "PetActivity";

    private static final double MIN_OPENGL_VERSION = 3.0;

    private DetectFragment detectFragment;

    private Toast toast;

    public ModelRenderable bowlRenderable;
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
    public int checkDogCat; // 0-dog, 1-cat


    private ImageView ad_imageView;

    private Node model = null;
    private AnchorNode modelParent = null;

    public boolean isCreated = false;
    private Vector3 offset;
    private Vector3 target = null;


    private VideoView arFragment;
    private ArrayList<Bitmap> bitmaps;
    private long seconds;

    private MediaMetadataRetriever retriever = null;
    private MediaController controller;

    private int changeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!checkIsSupportedDevice(this)) {
            return;
        }

        setContentView(R.layout.activity_video);

        Intent intent = getIntent();

        Uri uri = URI.parse(intent.getStringExtra("uri"));
        arFragment = (VideoView) findViewById(R.id.ar_fragment);

        retriever = new MediaMetadataRetriever();
        retriever.setDataSource((Context)this, uri);

        // 비디오뷰의 재생, 일시정지 등을 할 수 있는 '컨트롤바'를 붙여주는 작업
        arFragment.setMediaController(new MediaController(this));

        // 비디오뷰가 보여줄 동영상의 경로 주소(Uri) 설정하기
        arFragment.setVideoURI(uri);

        controller = new MediaController(this);
        arFragment.setMediaController(controller);

        arFragment.requestFocus();
        arFragment.start();

        /*
        // 동영상을 읽어오는데 시간이 걸리므로..
        // 비디오 로딩 준비가 끝났을 때 실행하도록..
        // 리스너 설정
        arFragment.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // 비디오 시작
                arFragment.start();
            }
        });
        */


        // 비디오의 총 재생시간을 얻어오기위함
        MediaPlayer mediaPlayer = MediaPlayer.create(getBaseContext(), uri);

        seconds = mediaPlayer.getDuration(); // 단위: ms

        bitmaps = new ArrayList<Bitmap>();
        Bitmap bmFrame = retriever.getFrameAtTime(0); // 단위: ms
        bitmaps.add(bmFrame);

        new Thread(new Runnable() {
            @Override public void run() {
                for(int i = 1000; i < seconds; i += 1000){
                    // TODO Auto-generated method stub
                    int currentPosition = i; // 단위: ms
                    Bitmap bmFrame = retriever.getFrameAtTime(currentPosition * 1000); // 단위: ms
                    bitmaps.add(bmFrame.copy(Bitmap.Config.ARGB_8888, true));
                }
            }
        }).start();

        detectFragment = DetectFragment.newInstance(this);
        detectFragment.setDetectEventListener(this);
        detectFragment.setDetectModel("yolov4-416-fp32.tflite");

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

        // button 생성 & 인식 전엔 invisible
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
                    model.setRenderable(bowlRenderable);
                    model.setLocalScale(new Vector3(0.8f, 0.8f, 0.8f));
                }
                else if(model != null && !isBtnPressed && catDetected) {
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
                else if(model != null && !isBtnPressed && catDetected) {
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
                else if(model != null && !isBtnPressed && catDetected) {
                    model.setRenderable(boxRenderable);
                }

                checkNum = 2;
                btnNum =(int) (Math.random() * 3); // 3개 상품 랜덤 전환

                setUpAdView(checkDogCat, checkNum, btnNum);
            }
        });

        Timer timer = new Timer();
        final int[] i = { 0 };
        TimerTask TT = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 해당 작업을 처리함
                        if(i[0] * 100 >= seconds){
                            timer.cancel();
                            return;
                        }

                        // 반복 실행할 구문
                        onSceneUpdate();
                        i[0]++;
                    }
                });
            }
        };

        timer.schedule(TT, 0, 500); // Timer 1초 후 실행 0.5초마다
    }

    private void createObject(RectF location) {
        Log.i("DEBUG", "DETECTED");

        // button visible & cat,dog-> Btn contents 결정
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

        Random random = new Random();
        if(changeCount == 0) {
            checkNum = random.nextInt(4);
            btnNum = random.nextInt(3);
            setUpAdView(checkDogCat, checkNum, btnNum);
            changeCount += 1;
        }
        else if(changeCount < 3) {
            changeCount += 1;
        }
        else {
            changeCount = 0;
        }
    }


    public Bitmap takeScreenshot() {
        int currentPosition = arFragment.getCurrentPosition(); //in millisecond
        // Toast.makeText(this, "Current Position: " + currentPosition + " (ms)", Toast.LENGTH_LONG).show();
        Bitmap bmFrame = retriever.getFrameAtTime(currentPosition * 1000); //unit in microsecond
        if (bmFrame == null) {
            Toast.makeText(this, "null!", Toast.LENGTH_LONG).show();
        }

        return bmFrame;
    }

    /*
    public static Bitmap takeScreenshot(View v) {
        Bitmap b = Bitmap.createBitmap( v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }*/


    private void onSceneUpdate() {
        // Bitmap bitmap = takeScreenshot();

        int ms = arFragment.getCurrentPosition() / 1000; //in millisecond
        if(ms >= bitmaps.size()){
            ms = bitmaps.size() - 1;
        }

        Bitmap bitmap = null;

        if(ms >= 0) bitmap = bitmaps.get(ms);

        if(bitmap == null) {
            Log.e("DEBUG", "bitmap is null");
        }
        /*
        try {
            image = bitmap;
        } finally {
            Log.i(TAG, "onUpdate: Image available");
        }
        */

        if(!isInitialized) {
            viewWidth = arFragment.getWidth();
            viewHeight = arFragment.getHeight();

            //imageWidth = image.getWidth();
            //imageHeight = image.getHeight();
            imageWidth = bitmap.getWidth();
            imageHeight = bitmap.getHeight();

            detectFragment.setDesiredPreviewFrameSize(new Size(imageWidth, imageHeight));
            detectFragment.onPreviewSizeChosen(new Size(imageWidth, imageHeight), 90);
            detectFragment.setDesiredRatio((float) viewWidth / imageWidth, (float) viewHeight / imageHeight);

            //Log.i(TAG, "View Size: " + viewWidth + " X " + viewHeight + " Image Size: " + imageWidth + " X " + imageHeight);

            isInitialized = true;
        }

        if(bitmap != null) {
            detectFragment.getVideofromAlbum(bitmap);
        }

        // dog -> cat, cat -> dog change moment
        if(isChanged != 0){
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

        Log.i("DEBUG", "result: " + result);

        if(result.equals("dog")) {
            if(catDetected && isCreated){ // cat->dog
                isChanged = 2;
            }
            dogDetected = true;
            catDetected = false;
            checkDogCat = 0;
        }
        else if(result.equals("cat")) {
            if(dogDetected && isCreated){ // dog ->cat
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
        // checkDogCat 0: dog, 1: cat
        if(checkDogCat == 0) { // dog
            if(checkNum == 0) { // dog -> food button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_dog_food_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_dog_food_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_dog_food_03); break;
                }
            }
            else if(checkNum == 1) { // dog -> snack button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_dog_snack_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_dog_snack_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_dog_snack_03); break;
                }
            }
            else { // dog -> toy button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_dog_toy_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_dog_toy_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_dog_toy_03); break;
                }
            }
        }
        else { // cat
            if(checkNum == 0) { // cat -> food button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_cat_food_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_cat_food_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_cat_food_03); break;
                }
            }
            else if(checkNum == 1) { // cat -> toy button
                switch (btnNum)
                {
                    case 0: ad_imageView.setImageResource(R.drawable.img_cat_toy_01); break;
                    case 1: ad_imageView.setImageResource(R.drawable.img_cat_toy_02); break;
                    case 2: ad_imageView.setImageResource(R.drawable.img_cat_toy_03); break;
                }
            }
            else { // cat -> house button
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