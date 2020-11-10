
package com.example.g10kandidat2019;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.util.Size;
import android.widget.ImageButton;
import android.widget.Toast;

//Original NoneNull-tagg
//@androidx.annotation.NonNull

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Camera2Activity extends AppCompatActivity  {

    private String LOG_TAG = "CAM2_DEBUG";
    private String STATUS_TAG = "STATUS_DEBUG";



    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private boolean frontCam = false;

    private String mPreviewCameraID;
    private Size mPreviewSize;

    private Size mVideoSize;
    private TextureView mTextureView;
    private CameraDevice mCameraDevice;

    private MediaRecorder mMediaRecorder;



    private CaptureRequest mCaptureRequest;
    private CaptureRequest.Builder mCaptureRequestBuilder;

    private CaptureRequest mRecordCaptureRequest;
    private CaptureRequest.Builder mRecordCaptureRequestBuilder;


    private HandlerThread mbackgroundThread;
    private Handler mBackgroundHandler;

    private ImageButton mRecordButton;
    private boolean mIsRecording = false;

    private File mVideoFolder;
    private String mVideoFileName;

    //Second camera
    private MediaRecorder mMediaRecorderFront;
    private String mVideoFileNameFront;
    private CaptureRequest mCaptureRequestFront;
    private CaptureRequest.Builder mCaptureRequestBuilderFront;
    private String mPreviewCameraIDFront;
    private Size mPreviewSizeFront;

    /* CAMERA SESSION CALLBACK
     * Callback object recvices uppdates about the progress of
     * a specific Capture Request submitted to the camera device
     *
     * This object is invoked when a request triggers a capture to start
     * and when a capture is compleate.
     *
     */
    private CameraCaptureSession mCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureSessionCallback =
            new CameraCaptureSession.CaptureCallback() {
        @Override
        //This method is called when the camera device has started capturing the output image for the request,
        //at the beginning of image exposure, or when the camera device has started processing an input image
        //for a reprocess request.
        public void onCaptureStarted( CameraCaptureSession session,  CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);

        }
    };


    /* CAMERA STATE CALLBACK
     * A callback object which recives uppdates about state of camera devices state
     * Needed to check if the camera session
     * is configured and ready to show preview/record.
     *
     */

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        //public void onOpened(@androidx.annotation.NonNull CameraDevice camera) {
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(STATUS_TAG,"onOpened called from mCameraDeviceStateCallback_______");
            if (!frontCam) {

                mCameraDevice = camera;

                mMediaRecorder = new MediaRecorder();
                if (mIsRecording) {
                    try {
                        createVideoFile(mPreviewCameraID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startRecord();
                    mMediaRecorder.start();
                } else {
                    startPreview();
                }

            }
        }


        @Override
        public void onDisconnected( CameraDevice camera) {
           // Toast.makeText(getApplicationContext(),"Camera disconnected",Toast.LENGTH_SHORT).show();
            Log.i(STATUS_TAG,"onDisconnected called from mCameraDeviceStateCallback_______");
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError( CameraDevice camera, int error) {
            camera.close();
            Log.i(STATUS_TAG, "Error on camera : " + error);
        }
    };

    //Device callback for second camera
    private CameraDevice.StateCallback mCameraDeviceStateCallbackFront = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(STATUS_TAG,"onOpened called from mCameraDeviceStateCallbackFront");
            if(frontCam) {
                mCameraDevice = camera;


            if(mIsRecording){
               // Toast.makeText(getApplicationContext(),mCameraDevice.getId() + "front camera Opened",Toast.LENGTH_SHORT).show();

                    try {
                        createVideoFile(mPreviewCameraIDFront);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startRecord();
                    mMediaRecorder.start();
            }
            else{
                startPreview();
            }
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.i(STATUS_TAG,"onDisconnected called from mCameraDeviceStateCallbackFront");
          // camera.close();
          //  mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

            camera.close();
        }
    };




    /*  SURFACE TEXTURE CALLBACK
     *  Texture View renders Image data.
     *  This listener is used to notified when the surface texture
     *  associated with this texture view is available.
     *  - Callback allows to view the camera inside this texture view.
     *
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListerner = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.i(STATUS_TAG,"onSurfaceTextureAvalible");
            setupCamera(width, height);
            connectCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

            Log.i(STATUS_TAG,"onSurfaceTextureDestroyed");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(STATUS_TAG, "ONCREATE!!!!!!!!!!!!" );
        setContentView(R.layout.activity_camera2activity);
        createVideoFolder();

        mTextureView = (TextureView) findViewById(R.id.Camera2_textureView);



        mRecordButton = (ImageButton) findViewById(R.id.Camera2_Video);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsRecording){
                    mIsRecording = false;
                    startPreview();
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mRecordButton.setImageResource(R.drawable.ic_videocam_black_65dp);

                }
                else{
                    mIsRecording = true;
                    mRecordButton.setImageResource(R.drawable.ic_launcher_background);
                    checkWriteStoragePermission();
                }
            }
        });

        //BUTTON FOR SNAPSHOT
        findViewById(R.id.Camera2_snapshot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frontCam = !frontCam;

                startPreview();
              //  Toast.makeText(getApplicationContext(),"fc = "+frontCam,Toast.LENGTH_SHORT).show();
            }
        });




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permission,grantResults);
        switch(requestCode){
            case REQUEST_STORAGE_PERMISSION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mIsRecording = true;
                    mRecordButton.setImageResource(R.drawable.ic_launcher_foreground);

                   // Toast.makeText(this,"permission granted",Toast.LENGTH_SHORT).show();
                }

                else{
                    Toast.makeText(this,"App cannot write video to storage",Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i(STATUS_TAG, "onResume called " );
        openBackgroundThread();


     //   Toast.makeText(getApplicationContext(),"On resume called",Toast.LENGTH_SHORT).show();
        if(mTextureView.isAvailable()){
            setupCamera(mTextureView.getWidth(),mTextureView.getHeight());
            connectCamera();
        }
        else{
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListerner);
        }
    }


    //TODO FIX PROBLEM WITH MULTIPLE CALLS TO ON PAUSE
    @Override
    public void onPause(){
        Log.i(STATUS_TAG, "onPause called " );
        // Toast.makeText(getApplicationContext(),"ON PAUSE CALLED",Toast.LENGTH_SHORT).show();

        closeCamera();

        closeBackgroundThread();

        super.onPause();
    }

    //TODO cleanup objects
    public void closeCamera(){
        Log.i(STATUS_TAG, "closeCamera called" );
        if(mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if(mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }

    }



    //
    private void setupCamera(int width, int height){
        CameraManager mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{

            for(String cameraId : mCameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                Log.i(LOG_TAG,cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT){

                    StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mPreviewSizeFront = getPreviewSize(map.getOutputSizes(SurfaceTexture.class),width,height);
                    mPreviewCameraIDFront = cameraId;

                }
                else{
                   // continue;
                //Get stream configuration map
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mPreviewSize = getPreviewSize(map.getOutputSizes(SurfaceTexture.class),width,height);
                mPreviewCameraID = cameraId;
                }
            }
        }
        catch (CameraAccessException e){
            Log.i(LOG_TAG,"Exception");
            e.printStackTrace();
        }
        Log.i(LOG_TAG,"setup done");
    }

    //Gets the smallest size for preview
    private Size getPreviewSize(Size[] mapSizes,int width, int height){
        List<Size> collectorSize = new ArrayList<Size>();
        for(Size option : mapSizes){
            if(width > height){
                if(option.getWidth() > width &&
                    option.getHeight() > height){
                    collectorSize.add(option);
                }
            }
            else{
                if(option.getWidth() > height &&
                    option.getHeight() > width){
                    collectorSize.add(option);
                }
            }
        }
        if(!collectorSize.isEmpty()){

            return Collections.min(collectorSize, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                }
            });
        }


        //Returns the highest matching value to our screen configurations in case we don't find anything
        return mapSizes[0];
    }


    //TODO add handler for multiple cameras
    private void connectCamera(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraManager.openCamera(mPreviewCameraIDFront,mCameraDeviceStateCallbackFront,null);
            cameraManager.openCamera(mPreviewCameraID,mCameraDeviceStateCallback,mBackgroundHandler);

        }
        catch (SecurityException e){
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    private void startRecord(){

    //TODO FIXA ERRORS MED SURFACES!!!!!!!!!!!!!!!!!!
        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
            Surface preveiwSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(preveiwSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            /* CAPTURE SESSION
             * @param
             * A CameraDevice must receive a frame configuration for each output raw frame via the CameraCaptureSession
             *
             * A CameraCaptureSession describes all the possible pipelines available to the CameraDevice.
             * Once a session is created, you cannot add or remove pipelines.
             * The CameraCaptureSession maintains a queue of CaptureRequests which become the active configuration.
             *
             * Used to create CaptureRequests
             *
             * OBS!!!!!!!!!
             * When an output frame is put into the target buffer(s), a capture callback is triggered.
            */

            // Creating a capture session for a mCameraDevice, used for capturing images, preprocessed
            // from the camera in the same session previously.

            // Once created, the session is active until a new session is created by the camera device,
            // or the camera device is closed.
            mCameraDevice.createCaptureSession(Arrays.asList(preveiwSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured( CameraCaptureSession session) {
                            try {
                                Log.i(STATUS_TAG, "onConfigured called from Record - camera " +mCameraDevice.getId() );
                                session.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(),null,null
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                                Log.i(LOG_TAG,"On Configure failed for capture session");
                        }
                    },null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (CameraAccessException e){
            e.printStackTrace();
        }

    }

    private void startPreview(){
        try{


            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            //Get requested hight and width for the buffer
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
            //Surface we're feeding the camera sensor
            Surface previewSurface = new Surface(surfaceTexture);

            //Setup capture request builder calling device
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            //TODO ADD multiple camera Sessions for multiple cameras
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                // @androidx.annotation.NonNull used on parameters
                public void onConfigured( CameraCaptureSession session) {
                    if(mCameraDevice == null){
                        return;
                    }
                    Log.i(STATUS_TAG, "onConfigured called from Record - camera " +mCameraDevice.getId() );
                    try{
                        //TODO CHANGE LISTENER FOR MULTIPLE THREADS?
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCaptureSession = session;
                        mCaptureSession.setRepeatingRequest(
                                mCaptureRequest,
                                mCaptureSessionCallback,
                                mBackgroundHandler //If no background thread use null
                        );
                    }
                    catch (CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed( CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(),"Create camera session failed",Toast.LENGTH_SHORT);
                    session.close();
                }
            },null);


        }
        catch(CameraAccessException e){
            e.printStackTrace();
        }

    }



    //TODO add parameters passed to handler
    public void openBackgroundThread(){
        mbackgroundThread = new HandlerThread("Camera2 background");
        mbackgroundThread.start();
        mBackgroundHandler = new Handler(mbackgroundThread.getLooper());

    }

    //TODO BLOCK threads until background thread closed
    private void closeBackgroundThread(){
        mbackgroundThread.quitSafely();
        try{
            mbackgroundThread.join();
            mbackgroundThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

//////////////////// File handler /////////////////////////////////////////////////////
    private void createVideoFolder(){
        File movieFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFolder,"camera2Video");
        if(!mVideoFolder.exists()){
            mVideoFolder.mkdir();
        }
    }

    private File createVideoFile(String cameraID) throws IOException{
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prefix = cameraID + "VIDEO_" + timestamp + "_" ;
        File outputFile = File.createTempFile(prefix, ".mp4",mVideoFolder);
        mVideoFileName = outputFile.getAbsolutePath();
        return outputFile;
    }




    private void checkWriteStoragePermission(){
        String camID;
        if(frontCam){
            camID = mPreviewCameraIDFront;
        }
        else{camID = mPreviewCameraID;}
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                mRecordButton.setImageResource(R.drawable.ic_videocam_black_65dp);
                try {

                    createVideoFile(camID);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startRecord();

                mMediaRecorder.start();
            }

            else{
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this,"App needs write permissions",Toast.LENGTH_SHORT).show();
                    }
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION);
            }
        else{
            //Essenitally unecessery app does not support older versions of android (>23)
            mIsRecording = true;
            mRecordButton.setImageResource(R.drawable.ic_launcher_foreground);
            try{
                createVideoFile(camID);
            }
            catch(IOException e){
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();
         }
    }

    private void setupMediaRecorder() throws IOException{
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(30);
      //  mMediaRecorder.setOrientationHint(0);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        mMediaRecorder.prepare();
    }

}
