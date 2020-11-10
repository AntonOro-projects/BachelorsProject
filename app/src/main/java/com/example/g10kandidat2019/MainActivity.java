package com.example.g10kandidat2019;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.g10kandidat2019.graph.VideoListActivity;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(getDrawable(R.mipmap.logo));
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //getSupportActionBar().setBackgroundDrawable(getDrawable(R.mipmap.logo));
        //getSupportActionBar().setSplitBackgroundDrawable(getDrawable(R.mipmap.logo));

        Button cameraBtn = findViewById(R.id.mainCameraBtn);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CameraActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.mainGraphBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), VideoListActivity.class);
                intent.putExtra("Debug", false);
                startActivity(intent);
            }
        });




        Button settingBtn = findViewById(R.id.mainSettingsbtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),SettingsActivity.class);
                startActivity(intent);
            }
        });

    }
}
