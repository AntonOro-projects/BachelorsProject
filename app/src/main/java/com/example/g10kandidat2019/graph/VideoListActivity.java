package com.example.g10kandidat2019.graph;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.g10kandidat2019.ImageProcessing;
import com.example.g10kandidat2019.R;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        setupActionBar();
        handlePermissions();
    }


    /**
     * Gets result from requesting permissions and based on those results does some action.
     * @param requestCode The identifier for which request this is the result for
     * @param permissions Which permissions where requested
     * @param grantResults The result of the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 3:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                   updateList();
                } else {
                    // permission denied
                    this.onBackPressed();
                }
        }
    }


    /**
     * Initiates custom actionbar
     */
    private void setupActionBar() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            View v = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(v, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            bar.setDisplayShowCustomEnabled(true);

            ((TextView)v.findViewById(R.id.custom_actionbar_title)).
                    setText(getResources().getString(R.string.videos));

            (findViewById(R.id.custom_actionbar_back_arrow))
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        }
        else {
            Log.e("VideoListActivity", "Actionbar not found");}
    }

    /**
     * Checks that we have the permissions needed and if not asks for them.
     */
    private void handlePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    3);
        }
        else {
            updateList();
        }
    }

    /**
     * Updates the list of videos. Called after making sure we have the right to search through the phones videos
     */
    private void updateList() {
        ImageProcessing imageProcessing = new ImageProcessing(this);
        List<String> videoPaths = new ArrayList<>(imageProcessing.getAllVideoPaths());

        VideoListRecyclerViewAdapter adapter = new VideoListRecyclerViewAdapter(this, videoPaths);
        ((RecyclerView)findViewById(R.id.video_list_recycleview)).setAdapter(adapter);
        ((RecyclerView)findViewById(R.id.video_list_recycleview)).setLayoutManager(new LinearLayoutManager(this));
     }
}
