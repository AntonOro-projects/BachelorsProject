package com.example.g10kandidat2019;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;


import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

/**
 * For temporary testing only
 */
public class ImageTesting extends AppCompatActivity {

    static {System.loadLibrary("opencv_java4");}

    private final static int REQUEST_PICK_IMAGE = 0;
    private final static int REQUEST_PICK_VIDEO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_testing);

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_VIDEO);

        ImageProcessing images = new ImageProcessing(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE) {
                Uri uri = data.getData();
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);
               // mat = images.test(mat);
                Utils.matToBitmap(mat, bitmap);
                ((ImageView)findViewById(R.id.imageView)).setImageBitmap(bitmap);
            }
            else if (requestCode == REQUEST_PICK_VIDEO && data != null) {
                System.out.println("comment");
              /*  Uri uri = data.getData();
                long start = Calendar.getInstance().getTimeInMillis();
                Point[] points = images.getVideoData(uri);
                int[] arrx = new int[points.length];
                int[] arry = new int[points.length];
                for (int i = 0; i < points.length; i++) {
                    Log.i("TESTIM", String.format("Point %d: (%f, %f)", i, points[i].x, points[i].y));
                    arrx[i] = (int)points[i].x;
                    arry[i] = (int)points[i].y;
                }
                Log.i("TESTIM", "Time: " + (Calendar.getInstance().getTimeInMillis() - start)/1000 + "s");

                Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
                intent.putExtra("x", arrx);
                intent.putExtra("y", arry);
                startActivity(intent);*/
            }
        }
    }
}
