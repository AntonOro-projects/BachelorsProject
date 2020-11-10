package com.example.g10kandidat2019;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.example.g10kandidat2019.CameraCalibration.CameraCalibrator;
import com.example.g10kandidat2019.graph.DataPoint;
import com.example.g10kandidat2019.graph.DataPointList;
import com.google.gson.Gson;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains image processing related functions.
 */
public class ImageProcessing {

    static {System.loadLibrary("opencv_java4");}

    private final static int SEC_IN_MILLI = 1000;
    private final static Scalar RED = new Scalar(255, 0, 0);

    private Context context;
    private CascadeClassifier markerCascade;
    private final MatOfRect markers = new MatOfRect();
    private Mat greyMat = new Mat();

    public ImageProcessing(Context context) {
        this.context = context;
        initCascade();
    }

    /**
     * Draws any identified object on the Mat object using the preloaded CascadeClassifier object
     * @param src The Mat wished to be searched
     * @return The Mat with circles around the found objects
     */
    Mat detectAndDisplayFromCascade(Mat src) {
        for (Rect marker: findObjectRectFromCascade(src)) {
            Point center = new Point(marker.x + marker.width/2.0,
                    marker.y + marker.height/2.0);
            Imgproc.ellipse(src, center, new Size(marker.width/2.0, marker.height/2.0),
                    0,0,360, RED, 3);
        }
        return src;
    }

    /**
     * Filters out false positives detections from the CascadeClassifier
     * @param src Original image
     * @param rects List of Rect objects found by CascadeClassifier
     * @return Original list with without false positives
     */
     private List<Rect> filter(Mat src, List<Rect> rects) {
        return filterCross(src, filterSubRect(rects));
    }

    /**
     * Filters out all images where a cross can not be detected
     * @param src Image
     * @param rects area of image searched
     * @return List with images without cross(es) removed.
     */
    private List<Rect> filterCross(Mat src, List<Rect> rects) {
        List<Rect> res = new ArrayList<>();

        for (Rect r : rects) {
            if (hasCross(src, r)) {
                res.add(r);
            }
            /*else {
                // Searches smaller Rect in case first one is to big and caught a bit
                // of the background.
                Rect smaller = new Rect(r.x + r.width/4,
                        r.y + r.height/4,
                        r.width/2,
                        r.height/2);
                if (hasCross(src, smaller)) {
                    res.add(smaller);
                }
            }*/
        }
        return res;
    }

    /**
     * Removes all Rect in the list that are a sub Rect of another
     * @param rects List of Rect objects
     * @return List with sub Rect(s) removed
     */
    private List<Rect> filterSubRect(List<Rect> rects) {
        boolean[] arr = new boolean[rects.size()];
        for (int i = 0; i < rects.size() - 1; i++) {
            if (arr[i]) {break;} // If it already has been determined as sub Rect
            for (int j = i+1; j < rects.size(); j++) {
                Rect r1 = rects.get(i);
                Rect r2 = rects.get(j);
                if (isSubRect(r1, r2)) {
                    arr[i] = true;
                }
                else if (isSubRect(r2, r1)) {
                    arr[j] = true;
                    break;
                }
            }
        }
        List<Rect> res = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            if (!arr[i]) {
                res.add(rects.get(i));
            }
        }
        return res;
    }

    /**
     * Returns a list of Rect objects representing the area of the given Mat
     * where a marker has been detected using cascade classifier.
     * @param src Image to search
     * @return List of Rect objects
     */
    private List<Rect> findObjectRectFromCascade(Mat src) {
        Imgproc.cvtColor(src, greyMat, Imgproc.COLOR_BGR2GRAY);

        markerCascade.detectMultiScale(greyMat, markers);
        return filter(src, markers.toList());
    }

    /**
     * Returns list of the tracking data in the given video.
     * @param path Path to video.
     * @param interval Time difference between frames to be processed in milliseconds.
     * @param runnable ProgressRunnable that runs after each processed frame.
     *                 See {@link ProgressRunnable}
     * @return List of DataPoint objects representing detected markers for each frame
     */
    public DataPointList getVideoData(String path, int interval, @Nullable ProgressRunnable runnable) {

        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        return getVideoData(media, interval, runnable);
    }

    /**
     * Returns list of the tracking data in the given video.
     * @param uri Video Uri.
     * @param interval Time difference between frames to be processed in milliseconds.
     * @param runnable ProgressRunnable that runs after each processed frame.
     *      *                 See {@link ProgressRunnable}
     * @return List of DataPoint objects representing detected markers for each frame
     */
    public DataPointList getVideoData(Uri uri, int interval, @Nullable ProgressRunnable runnable) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(context, uri);
        return getVideoData(media, interval, runnable);
    }

    /**
     * Returns list of the tracking data in the given video.
     * @param mmr MediaMetaDataRetriever with a set source expected to be a video
     * @param interval Time difference between frames to be processed in milliseconds.
     * @param runnable ProgressRunnable that runs after each processed frame.
     *      *                 See {@link ProgressRunnable}
     * @return List of DataPoint objects representing detected markers for each frame
     */
    private DataPointList getVideoData(MediaMetadataRetriever mmr, int interval,
                                        @Nullable ProgressRunnable runnable) {

        Bitmap bitmap;
        Mat mat = new Mat();
        DataPointList dataPoints = new DataPointList();

        // amount of frames to process
        final int length = (Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))/interval);

        for (int i = 0; i < length + 1; i++) {

            bitmap = getBitmapFromVideo(mmr, interval*i*SEC_IN_MILLI);
            Utils.bitmapToMat(bitmap, mat);

            List<Rect> foundObjects = findObjectRectFromCascade(mat);
            if (foundObjects.isEmpty()) {
                dataPoints.add(new DataPoint(-1, -1, 0, interval*i*SEC_IN_MILLI, null));
            }
            else {
                Rect r = foundObjects.get(0);
                Imgproc.circle(mat, new Point(r.x + r.width/2.0, r.y + r.height/2.0), r.width/2, RED, 6);
                Utils.matToBitmap(mat, bitmap);

                // Since y=0 start at the top of the image we invert it.
                dataPoints.add( new DataPoint(r.x + r.width/2, mat.rows() - r.y + r.height/2,
                        r.width, interval*i*SEC_IN_MILLI, bitmap));
            }
            if (runnable != null) {
                runnable.setProgress((i * 100) / (length - 1));
                runnable.run();
            }
        }
        return dataPoints;
    }

    /**
     * Returns video length in milliseconds
     * @param path Path to the video
     * @return Length in milliseconds
     */
    public int getVideoLength(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        return  Integer.parseInt(media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
    }

    /**
     * Returns all paths to videos on the phone
     * @return List of paths
     */
    public List<String> getAllVideoPaths() {
        String[] projection = {MediaStore.Video.VideoColumns.DATA};
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        ArrayList<String> pathList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                pathList.add(cursor.getString(0));
            }
            cursor.close();
        }
        return pathList;
    }

    /**
     * Rotates the given Mat object
     * @param src Mat to rotate
     * @param angle Degrees to rotate
     */
    void rotateImage(Mat src, int angle) {
        Mat matrot = Imgproc.getRotationMatrix2D(new Point(src.width()/2., src.height()/2.), angle, 1);
        Imgproc.warpAffine(src, src, matrot, new Size(src.width(), src.height()),
                Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS);
    }

    /**
     * Initializes the cascade object
     */
    private void initCascade() {
        InputStream ls = context.getResources().openRawResource(R.raw.cascade);
        File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
        File mCascadeFile = new File(cascadeDir, "cascade");
        try {
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while((bytesRead = ls.read(buffer)) != -1 ) {
                os.write(buffer, 0, bytesRead);
            }
            ls.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        markerCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        markerCascade.load(mCascadeFile.getAbsolutePath());
    }

    /**
     * Returns Bitmap from video set as source in given MediaMetaDataRetriever. Bitmap represents
     * frame on the given time (microseconds) in the video. MediaMetaDataRetriever expects
     * to have source set when received.
     * @param mmr MediaMetaDataRetriever with video set as source.
     * @param time Time in the video to get fram in microseconds
     * @return Bitmap retrieved
     */
    private Bitmap getBitmapFromVideo(MediaMetadataRetriever mmr, long time) {
        Bitmap bitmap;

        // OPTION_CLOSEST performs poorly but no option always returns closest second and will
        // return identical images when processing > 1fps
        if (time / SEC_IN_MILLI % 1000 == 0) {
            bitmap = mmr.getFrameAtTime(time);
        }
        else {
            bitmap = mmr.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
        }
        return bitmap;
    }

    private List<Point> getLines(Mat src, Rect rect) {
        List<Point> points = new ArrayList<>();
        greyMat = new Mat();
        Mat canny = new Mat();
        Mat lines = new Mat();
        Mat sub = new Mat(src, rect);
        Imgproc.cvtColor(sub, greyMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(greyMat, canny, 10, 100);
        Imgproc.HoughLinesP(canny, lines, 1, Math.PI / 180, 50, 10 , 10);
        Mat houghLines = new Mat();
        houghLines.create(canny.rows(), canny.cols(), CvType.CV_8UC1);
        for (int i = 0; i < lines.rows(); i++) {
            double[] values = lines.get(i, 0);
            double x1, y1, x2, y2;
            x1 = rect.x + values[0];
            y1 = rect.y + values[1];
            x2 = rect.x + values[2];
            y2 = rect.y + values[3];

            points.add(new Point(x1, y1));
            points.add(new Point(x2, y2));
        }
        return points;
    }

    /**
     * Returns orientation of the line made up of the given points
     * @param p1 Point 1
     * @param p2 Point 2
     * @return Orientation of he line
     */
    private double getLineOrientation(Point p1, Point p2) {
        return Math.toDegrees(Math.atan2(p1.x - p2.x, p1.y - p2.y));
    }

    /**
     * Returns a List of orientation from the given List of Point objects. Points are paired in
     * the order of the list. List is assumed to have a even number of points.
     * @param points List of Point objects.
     * @return array of orientations of the Point pairs in the order of the given list.
     */
    private double[] getLineOrientations(List<Point> points) {
        double[] orientations = new double[points.size()/2];
        for (int i = 0; i < points.size() - 1; i+=2) {
            orientations[i/2] = (getLineOrientation(points.get(i), points.get(i+1)));
        }
        return orientations;
    }

    /*
        Gets lines in the image with HoughLinesP and guesses if there is a cross in it
        based on a approximation using the lines orientation.
        This is a temporary function used because it helps to filter out false positives
        but it can and SHOULD be improved or replaced.
    */
    private boolean hasCross(Mat src, Rect rect) {

        List<Point> linePoints = getLines(src, rect);
        if (linePoints.size() < 2) {return false;}
        double[] orientations = getLineOrientations(linePoints);

        // If line is determined to have same orientation as other
        // it is not checked again
        boolean[] checked = new boolean[orientations.length];

        // Amount of orientations shared with orientation on the same index in orientations
        int[] matches = new int[orientations.length];


        // Checking if lines are perpendicular +- this value. Set according to need.
        int angleRange = (rect.width < 200) ? 30 : 20;

        // What percentage the top 2 perpendicular orientations must make up out of all lines.
        int limitLinePercentage = (orientations.length < 10) ? 50 : 75;

        for (int i = 0; i < orientations.length; i++) {
            if (checked[i]) {continue;}
            matches[i]++;

            for (int j = i+1; j < orientations.length; j++) {
                if (checked[j]) {continue;}
                if (Math.abs(orientations[i] - orientations[j]) <= 10 ) {
                    checked[j] = true;
                    matches[i]++;
                }
            }
        }


        int largest = -1; // Largest values
        int secondLargest = -1;
        int largestInd = 0; // index of largest
        int secondInd = 0;
        int tot = 0; // total matches

        for (int i = 0; i < matches.length; i++) {
            if (checked[i]) {continue;}
            tot += matches[i];

            if (largest == -1) {
                largest = matches[i];
                largestInd = i;
            }else if (matches[i] > largest) {
                secondLargest = largest;
                secondInd = largestInd;
                largest = matches[i];
                largestInd = i;
            }
            else if (secondLargest == -1) {
                    secondLargest = matches[i];
                    secondInd = i;
            }
            else if (matches[i] > secondLargest) {
                secondLargest = matches[i];
                secondInd = i;
            }
        }
        if (tot == 0) {return false;}
        return (100*(secondLargest + largest))/tot >= limitLinePercentage &&
                Math.abs(orientations[largestInd] - orientations[secondInd]) < 90 + angleRange &&
                Math.abs(orientations[largestInd] - orientations[secondInd]) > 90 - angleRange ;
    }


    /**
     * Returns true if r2 is a sub Rect of r1
     * @param r1 Main Rect
     * @param r2 Possible sub Rect
     * @return True if r2 is a sub Rect of r1, false otherwise.
     */
    private boolean isSubRect(Rect r1, Rect r2) {
        return r1.x <= r2.x && (r1.x + r1.width) >= (r2.x + r2.width) &&
                (r1.y <= r2.y) && (r1.y + r1.height) >= (r2.y + r2.height);
    }

    /**
     * Used to update progress on UI thread when processing videos asynchronous. Override run()
     * with code publishing
     */
    public static class ProgressRunnable implements Runnable {

        private int progress;

        private void setProgress(int i) {
            this.progress = i;
        }

        /**
         * Get current progress status
         * @return Integer between 0-100
         */
        protected int getProgress() {return progress;}

        @Override
        public void run() {
            // To be overridden
        }
    }

    private boolean isCameraCalibrated(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.SHARED_PREFERENCES_CALIBRATOR), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(context.getString(R.string.hasSavedCalibrator),false);
    }

    public CameraCalibrator getSavedCalibrator(){
        CameraCalibrator cameraCalibrator = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.SHARED_PREFERENCES_CALIBRATOR), Context.MODE_PRIVATE);
        String jsonCalibrator = sharedPreferences.getString(context.getString(R.string.sharedCalibrator),null);
        Gson gson = new Gson();
        if(jsonCalibrator != null){
            cameraCalibrator = gson.fromJson(jsonCalibrator,CameraCalibrator.class);
        }
        return cameraCalibrator;
    }
}
