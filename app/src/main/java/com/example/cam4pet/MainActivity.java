package com.example.cam4pet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageButton;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Session mSession;
    private boolean mUserRequestInstall = true;

    ImageButton buttonPet;

    private static final int SELECT_PICTURE = 1;
    private static final int SELECT_VIDEO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPet = findViewById(R.id.btnCamera);

        buttonPet.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PetActivity.class);
            startActivity(intent);
        });

        // Connecting gallery
        ((ImageButton) findViewById(R.id.btnAlbum))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("video/*");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        try {
                            startActivityForResult(Intent.createChooser(intent,"Select Video"), SELECT_VIDEO);
                        } catch(android.content.ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_VIDEO) {
                Uri uri = intent.getData();

                Intent newIntent = new Intent(MainActivity.this, VideoActivity.class);
                newIntent.putExtra("uri", uri.toString());
                startActivity(newIntent);
            }
        }
    }

    @Override
    protected  void onResume() {
        super.onResume();
        requestCameraPermission();

        try {
            if(mSession == null) {
                switch(ArCoreApk.getInstance().requestInstall(this, mUserRequestInstall)) {
                    case INSTALLED:
                        mSession = new Session(this);
                        break;
                    case INSTALL_REQUESTED:
                        mUserRequestInstall = false;
                        return;
                }
            }
        } catch(Exception e) {
            return;
        }
    }

    private void requestCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
    }
}