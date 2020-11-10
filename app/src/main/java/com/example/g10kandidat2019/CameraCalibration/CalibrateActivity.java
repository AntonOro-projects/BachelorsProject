package com.example.g10kandidat2019.CameraCalibration;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import android.widget.TextView;
import android.widget.Toast;

import com.example.g10kandidat2019.R;
import com.google.gson.Gson;

public class CalibrateActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private String LOG_TAG = "CALIBRATOR_TAG";


    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat mat1;
    private final static Scalar RED = new Scalar(255, 0, 0);
    private int width;
    private int height;
    private CameraCalibrator mCalibrator;
    private int mCameraId;
    private TextView mTextView;
    private SharedPreferences mSharedPreferences;
    private boolean mIsCalibrated;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calib3d);
        //load previous calibratior
        mSharedPreferences = getSharedPreferences(getString(R.string.SHARED_PREFERENCES_CALIBRATOR), Context.MODE_PRIVATE);
        //check if previous calibrator exists
        mIsCalibrated = getSaveStatus();


        mTextView = findViewById(R.id.isCalibratedTextView);


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
            5);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);



        // TODO FIX MULTIPLE CAMERAS

        updateStatus();

        findViewById(R.id.startCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            mCalibrator.addCorners();

            }
        });

        findViewById(R.id.clearCalib).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                clearCalibrator();
                updateStatus();
            }
        });

        findViewById(R.id.Calibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(mCalibrator.canCalibrate()){
                    mCalibrator.calibrate();

                    if(!saveCalibrator()){
                        Toast.makeText(getBaseContext(),"Calibration not saved",Toast.LENGTH_SHORT);
                    }
                    updateStatus();


                    Mat distort = mCalibrator.getDistortionCoefficients();
                    Log.i(LOG_TAG,distort.dump());

                }
                else{
                    Toast.makeText(getBaseContext(),"Cannot calibrate",Toast.LENGTH_LONG);
                }

            }
        });


    cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.myCalibView);



    cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
    cameraBridgeViewBase.setCvCameraViewListener(this);
    cameraBridgeViewBase.setScaleX((float)1.35);
    cameraBridgeViewBase.setScaleY((float)1.85);



    }



    @Override
    public void onCameraViewStarted(int width, int height) {
        this.width = width;
        this.height = height;
        mat1 = new Mat(width, height, CvType.CV_8UC4);
        mCalibrator = new CameraCalibrator(width, height);

    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status){
            Log.i("CVTAG", "S" + status);
            switch(status) {
                case BaseLoaderCallback.SUCCESS:
                    cameraBridgeViewBase.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public void onCameraViewStopped() {
        mat1.release();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mOpenCVCallBack);
        }
        else {
            Log.i("CVTAG", "1");
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            Log.i("CVTAG", "2");
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mat1 = inputFrame.rgba();
        Mat mat2 = inputFrame.gray();
        mCalibrator.processFrame(mat2,mat1);

        return mat1;
    }



    /**
     * Gets status of previously existing calibrations.
     * @return true if such exists false if not.
     */

    public boolean getSaveStatus(){
        boolean isCalibrated = mSharedPreferences.getBoolean(getString(R.string.hasSavedCalibrator),false);
        if(!isCalibrated){
            Toast.makeText(getBaseContext(),"No previous calibrations exists",Toast.LENGTH_SHORT).show();
        }
        return isCalibrated;
    }

    /**
     * Saves the status of the current calibration to disk
     * @return true if saving was successful
     */

    private boolean saveCalibrator(){
        if(!mCalibrator.isCalibrated()){
            return false;
        }
        boolean saveSuccessful = false;
        SharedPreferences.Editor preferenceEditor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String calibratorObj = gson.toJson(mCalibrator);
        preferenceEditor.putString(getString(R.string.sharedCalibrator),calibratorObj);
        preferenceEditor.putBoolean(getString(R.string.hasSavedCalibrator),true);
        saveSuccessful = preferenceEditor.commit();
        return true;
    }

    /**
     * Clear the current calibration from memory
     */
    private void clearCalibrator(){
        SharedPreferences.Editor preferenceEditor = mSharedPreferences.edit();
        preferenceEditor.remove(getString(R.string.sharedCalibrator));
        preferenceEditor.putBoolean(getString(R.string.hasSavedCalibrator),false);
        preferenceEditor.commit();
    }


    /**
     * Method used to extract previous calibrations.
     * @return previously saved Camera Calibration object
     */

    public CameraCalibrator getSavedCalibrator(){
        CameraCalibrator cameraCalibrator = null;

        String jsonCalibrator = mSharedPreferences.getString(getString(R.string.sharedCalibrator),null);
        Gson gson = new Gson();
        if(jsonCalibrator != null){
            cameraCalibrator = gson.fromJson(jsonCalibrator,CameraCalibrator.class);
        }
        return cameraCalibrator;
    }

    /*
     * Updates text bar status if a saved calibration exists on disk.
     */
    private void updateStatus(){
        boolean previousCalibration = getSaveStatus();
        String output = "Not Calibrated";
        if(previousCalibration) {
            output = "Calibrated ";
            mTextView.setTextColor(this.getResources().getColor(R.color.CalibOK, this.getTheme()));
        }
        else{
            mTextView.setTextColor(this.getResources().getColor(R.color.CalibNOK, this.getTheme()));
        }
        mTextView.setText(output);
    }


    ////////////////// UNUSED ///////////////////

    /**
     * Create a bitmap of the current screen
     * @return bitmap representation
     */
    private Bitmap takeSnapShot() {
        Bitmap btm = null;
        Mat rgb = new Mat();
        Imgproc.cvtColor(mat1, rgb, Imgproc.COLOR_BGR2RGB);

        try{
            btm = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb, btm);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());
        }
        return btm;
    }

    /**
     * Saves a bitmap of the current screen to disk
     * @param bitmap
     * @throws IOException
     */
    private void saveBitmap(Bitmap bitmap) throws IOException{
        Log.i("xx","bf");
        File mediaFile = new File(Environment.getExternalStorageDirectory().getPath(),"snapshot.jpg");

        FileOutputStream fileOutputStream = new FileOutputStream(mediaFile);
        Log.i("xx","af");
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

        fileOutputStream.flush();
        fileOutputStream.close();

    }

    /**
     * Swaps the used camera.
     */
    private void swapCamera() {
        mCameraId = mCameraId^1; //bitwise not operation to flip 1 to 0 and vice versa
        cameraBridgeViewBase.disableView();
        cameraBridgeViewBase.setCameraIndex(mCameraId);
        cameraBridgeViewBase.enableView();
    }

}

