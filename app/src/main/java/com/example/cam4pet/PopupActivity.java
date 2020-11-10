package com.example.cam4pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

class PopupActivty extends AppCompatActivity {

    TextView textView;
    ImageView imageView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);


        button = findViewById(R.id.button);

        button.setOnClickListener(view -> {

            String url ="http://www.11st.co.kr/products/2859585364?utm_medium=%EA%B2%80%EC%83%89&gclid=CjwKCAiAkan9BRAqEiwAP9X6UXQgYaTMHn_E55oXuDRIqmAigkfr4s3K-2FqFtfBjdQCSAw_SJ1c_hoCNfQQAvD_BwE&utm_source=%EA%B5%AC%EA%B8%80_PC_S_%EC%87%BC%ED%95%91&utm_campaign=%EA%B5%AC%EA%B8%80%EC%87%BC%ED%95%91PC+%EC%B6%94%EA%B0%80%EC%9E%91%EC%97%85&utm_term=";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);


        });
    }
}

