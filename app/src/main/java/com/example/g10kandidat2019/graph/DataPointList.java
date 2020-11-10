package com.example.g10kandidat2019.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * List containing DataPoint objects
 */
public class DataPointList {

    private List<DataPoint> list;

    public DataPointList() {
        this.list = new ArrayList<>();
    }

    /**
     * Adds DataPoint object to list
     * @param p Object to add
     */
    public void add(DataPoint p) {
        list.add(p);
    }

    /**
     * Returns the DataPoint element at the specified index
     * @param i index
     * @return DataPoint
     */
    public DataPoint get(int i) {
        if (list.size() > i ) {
            return list.get(i);
        }
        return null;
    }

    /**
     * Returns list of DataPoint objects as a List
     * @return List of DataPoints
     */
    public List<DataPoint> getDataPointList() {
        return list;
    }

    public double getAverageDiameter(int i) {
        List<Double> diameters = new ArrayList<>();
        for (DataPoint p : list) {
            diameters.add(p.getWidth());
        }
        diameters.sort(new Comparator<Double>() {
            @Override
            public int compare(Double t1, Double t2) {
                if (t1 > t2) {
                    return -1;
                }
                else if (t1 < t2) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        });

        double total = 0;
        for (int j = 0; j < i; j++) {
            total += diameters.get(j);
        }
        return total/i;
    }

    /**
     * Returns the size of the list
     * @return size of the list
     */
    public int size() {
        return list.size();
    }

}
