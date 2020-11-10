package com.example.g10kandidat2019.graph;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.Serializable;


/**
 * Class representing data points of an detected object in an image.
 */
public class DataPoint implements Serializable {

    private final int x;
    private final int y;
    private final double width;
    private final long us;
    private final Bitmap img;

    /**
     * Data point representing a detection in an image
     * @param x x position of center pixel in image
     * @param y y position of center pixel in image
     * @param img Image used for detection.
     */
    public DataPoint(int x, int y, double width, long us, @Nullable Bitmap img) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.us = us;
        this.img = img;
    }

    /**
     * Gets pixel x-position.
     * @return x pixel position
     */
    int getX() {
        return x;
    }

    /**
     * Gets pixel y-position.
     * @return y pixel position
     */
    int getY() {
        return y;
    }

    /**
     * Gets the timestamp of the dataPoint in microseconds
     * @return timestamp in microseconds
     */
    long getUs() {
        return us;
    }

    double getWidth() {
        return width;
    }

    /**
     * Gets image used for detection. Can be null.
     * @return Image used for detection.
     */
    Bitmap getImg() {
        return img;
    }
}
