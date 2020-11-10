package com.example.g10kandidat2019.CameraCalibration;


import java.util.ArrayList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.util.Log;
import android.widget.Toast;


public class CameraCalibrator {
    private static final String LOG_TAG = "CameraCalibrator";
    //Default to set mPatternSize to final
    private Size mPatternSize = new Size(7, 7);
    private final int mCornersSize = (int)(mPatternSize.width * mPatternSize.height);
    private boolean mPatternWasFound = false;
    private MatOfPoint2f mCorners = new MatOfPoint2f();
    private List<Mat> mCornersBuffer = new ArrayList<Mat>();
    private boolean mIsCalibrated = false;

    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();
    private int mFlags;
    private double mRms;
    private double mSquareSize = 0.0181;
    private Size mImageSize;

    /**
     * Default camera calibrator constructor
     * @param width of the image
     * @param height of the image
     */
    public CameraCalibrator(int width, int height) {
        mImageSize = new Size(width, height);
        mFlags = Calib3d.CALIB_FIX_PRINCIPAL_POINT +
                Calib3d.CALIB_ZERO_TANGENT_DIST +
                Calib3d.CALIB_FIX_ASPECT_RATIO +
                Calib3d.CALIB_FIX_K4 +
                Calib3d.CALIB_FIX_K5;
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
        mCameraMatrix.put(0, 0, 1.0);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);
        Log.i(LOG_TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Custom constructor for other chessboard patterns
     * UNUSED
     * @param width of the image
     * @param height of the image
     * @param chessboardWidth width - number of chessboard crosses
     * @param chessboardHeight height - number of chessboard crosses
     */
    public CameraCalibrator(int width, int height, int chessboardWidth,int chessboardHeight) {
        mImageSize = new Size(width, height);
        mPatternSize = new Size(chessboardWidth, chessboardHeight);
        mFlags = Calib3d.CALIB_FIX_PRINCIPAL_POINT +
                Calib3d.CALIB_ZERO_TANGENT_DIST +
                Calib3d.CALIB_FIX_ASPECT_RATIO +
                Calib3d.CALIB_FIX_K4 +
                Calib3d.CALIB_FIX_K5;
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
        mCameraMatrix.put(0, 0, 1.0);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);
        Log.i(LOG_TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Process the current frame
     * @param grayFrame gray scale image to find patterns
     * @param rgbaFrame rgb image to add visuals
     */
    public void processFrame(Mat grayFrame, Mat rgbaFrame){
        findPattern(grayFrame);
        renderFrame(rgbaFrame);
    }

    /**
     * Constructing homogeneous coordinates from camera coordinates
     * @param corners Vector of saved corner positions
     */
    private void calcBoardCornerPositions(Mat corners) {
        final int cn = 3;
        float positions[] = new float[mCornersSize * cn];

        for (int i = 0; i < mPatternSize.height; i++) {
            for (int j = 0; j < mPatternSize.width * cn; j += cn) {
                positions[(int) (i * mPatternSize.width * cn + j + 0)] =
                        (2 * (j / cn) + i % 2) * (float) mSquareSize;
                positions[(int) (i * mPatternSize.width * cn + j + 1)] =
                        i * (float) mSquareSize;
                positions[(int) (i * mPatternSize.width * cn + j + 2)] = 0;
            }
        }
        corners.create(mCornersSize, 1, CvType.CV_32FC3);
        corners.put(0, 0, positions);
    }


    /**
     * Finds chessboard pattern in the current image
     * @param grayFrame gray scale representation of current frame
     */
    private void findPattern(Mat grayFrame) {
        mPatternWasFound = Calib3d.findChessboardCorners(grayFrame, mPatternSize,
                mCorners, Calib3d.CALIB_CB_ASYMMETRIC_GRID);

    }

    /**
     * Adds corner positions to buffer
     */
    public void addCorners() {
        if (mPatternWasFound) {
            Log.i(LOG_TAG, "Corners are " + mCorners.toString());
            mCornersBuffer.add(mCorners.clone());
        }
    }

    /**
     * Draw points on the current image
     * @param rgbaFrame current processed image
     */
    private void drawPoints(Mat rgbaFrame) {
        Calib3d.drawChessboardCorners(rgbaFrame, mPatternSize, mCorners, mPatternWasFound);
    }

    /**
     * Renders the current image frame by adding corners and text
     * @param rgbaFrame current processed image
     */

    private void renderFrame(Mat rgbaFrame) {

        drawPoints(rgbaFrame);
        rotateImage(rgbaFrame,270);
        Imgproc.putText(rgbaFrame, "Captured: " + mCornersBuffer.size(), new Point(rgbaFrame.rows()*0.2,rgbaFrame.rows() * 0.05),
                Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 0));

    }

    /**
     * Rotates the image
     * @param src Image to be rotated
     * @param angle degrees of rotation
     */
    private void rotateImage(Mat src, int angle) {
        Mat matrot = Imgproc.getRotationMatrix2D(new Point(src.width()/2., src.height()/2.), angle, 1);
        Imgproc.warpAffine(src, src, matrot, new Size(src.width(), src.height()),
                Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS);

    }


    /**
     * Creates calibration matrix and distortion coefficients and saves them to this object
     */
    public void calibrate() {
        ArrayList<Mat> rvecs = new ArrayList<Mat>();
        ArrayList<Mat> tvecs = new ArrayList<Mat>();
        Mat reprojectionErrors = new Mat();
        ArrayList<Mat> objectPoints = new ArrayList<Mat>();
        objectPoints.add(Mat.zeros(mCornersSize, 1, CvType.CV_32FC3));
        calcBoardCornerPositions(objectPoints.get(0));
        for (int i = 1; i < mCornersBuffer.size(); i++) {
            objectPoints.add(objectPoints.get(0));
        }

        Calib3d.calibrateCamera(objectPoints, mCornersBuffer, mImageSize,
                mCameraMatrix, mDistortionCoefficients, rvecs, tvecs, mFlags);

        mIsCalibrated = Core.checkRange(mCameraMatrix)
                && Core.checkRange(mDistortionCoefficients);

        mRms = computeReprojectionErrors(objectPoints, rvecs, tvecs, reprojectionErrors);
        Log.i(LOG_TAG, String.format("Average re-projection error: %f", mRms));
        Log.i(LOG_TAG, "Camera matrix: " + mCameraMatrix.dump());
        Log.i(LOG_TAG, "Distortion coefficients: " + mDistortionCoefficients.dump());
    }


    /**
     * The function returns the average re-projection error.
     * This number gives a good estimation of precision of the found parameters.
     * This should be as close to zero as possible.
     * @param objectPoints Image points of the calibrations
     * @param rvecs Rotation vector
     * @param tvecs Vector of translation vectors estimated for each pattern view.
     * @param perViewErrors Vector of the RMS re-projection error estimated for each pattern view.
     * @return reprojection error
     */
    private double computeReprojectionErrors(List<Mat> objectPoints,
                                             List<Mat> rvecs, List<Mat> tvecs, Mat perViewErrors) {
        MatOfPoint2f cornersProjected = new MatOfPoint2f();
        double totalError = 0;
        double error;
        float viewErrors[] = new float[objectPoints.size()];

        MatOfDouble distortionCoefficients = new MatOfDouble(mDistortionCoefficients);
        int totalPoints = 0;
        for (int i = 0; i < objectPoints.size(); i++) {
            MatOfPoint3f points = new MatOfPoint3f(objectPoints.get(i));
            Calib3d.projectPoints(points, rvecs.get(i), tvecs.get(i),
                    mCameraMatrix, distortionCoefficients, cornersProjected);
            error = Core.norm(mCornersBuffer.get(i), cornersProjected, Core.NORM_L2);

            int n = objectPoints.get(i).rows();
            viewErrors[i] = (float) Math.sqrt(error * error / n);
            totalError  += error * error;
            totalPoints += n;
        }
        perViewErrors.create(objectPoints.size(), 1, CvType.CV_32FC1);
        perViewErrors.put(0, 0, viewErrors);

        return Math.sqrt(totalError / totalPoints);
    }


    /**
     * Cleans corner buffer
     */
    public void clearCorners() {
        mCornersBuffer.clear();
    }

    /**
     * Public method for extracting distortion coefficients
     * @return Distortion Coefficients
     */
    public Mat getDistortionCoefficients() {
        return mDistortionCoefficients;
    }

    /**
     * Method for extracting camera matrix
     * @return camera matrix
     */
    public Mat getCameraMatrix() {
        return mCameraMatrix;
    }


    public int getCornersBufferSize() {
        return mCornersBuffer.size();
    }

    /**
     * Public method for extracting reprojection error
     * @return reprojection error
     */
    public double getAvgReprojectionError() {
        return mRms;
    }

    /**
     * Public method to determine if calibration can be done
     * @return true if corners are stored in buffer
     */
    public boolean canCalibrate(){ return (mCornersBuffer.size() > 0); }

    /**
     * Public method to get calibration status
     * @return true if calibration exists
     */
    public boolean isCalibrated() {
        return mIsCalibrated;
    }

    /**
     * Public method to set calibration status
     */
    public void setCalibrated() {
        mIsCalibrated = true;
    }

}
