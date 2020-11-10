package com.example.g10kandidat2019.graph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.g10kandidat2019.ImageProcessing;
import com.example.g10kandidat2019.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Activity Handling the data visualization of the application
 */
public class GraphActivity extends AppCompatActivity {

    private Context context;
    private DataPointList dataPoints;
    private Handler handler;
    private Export export;

    private LineChart lineChart;
    private Bitmap[] bitmaps;

    private int msPerFrame;

    private int dataDisplayed;
    private int unitDisplayed;
    private int directionDisplayed;

    // Direction
    private final static int X_AXIS = 0;
    private final static int Y_AXIS = 1;

    // Data types
    private final static int POSITION = 0;
    private final static int DISTANCE = 1;

    // Units
    private final static int PIXELS = 0;
    private final static int CENTIMETERS = 1;
    private final static int METERS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        context = this;
        dataPoints = new DataPointList();
        handler = new Handler();
        msPerFrame = getApplicationContext().getSharedPreferences("SettingsData",
                Context.MODE_PRIVATE).getInt("SettingsData", 1000);

        if(!getIntent().getBooleanExtra("Debug", false)){
            export = new Export(getIntent().getStringExtra("path"),this);
            //Do all graph setups and initializations. From this function initView() is called
            initCalculateVideoData(getIntent().getStringExtra("path"));
        }
        else{
            dataPoints = rndDataPointList(100);
            initView();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getBooleanExtra("Debug", false)){
            return false;
        }
        else{
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.graph_menu, menu);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.export:
                String data = export.createStringFromList(dataPoints);
                Uri name = export.save(data);
                if(name == null || name.toString().startsWith("file://")){//name == null if we cant read external storage. startswith if we fail to create content uri which will fail cause exposing filestructure error
                    try {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, data);
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Choose how you want to export the data"));
                    }catch(Exception e) {//if data is too big to send as a parcel
                        Toast.makeText(this, "Unable to export data due too size of file", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                try {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND,name);
                    startActivity(Intent.createChooser(sendIntent, "Choose how you want to export the data"));
                }catch(Exception e){
                    e.printStackTrace();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private LineData convertToLineData(double[] values, int milliesBetweenXValues, String caption) {
        List<Entry> entries = new ArrayList<>();
        for(int i = 0; i < values.length; i++){
            // If a frame did not contain a marker, do not draw a point in graph but do
            // increase the x value which indicates time.
            if(values[i] != -1){
                entries.add(new Entry((0.001F * i * milliesBetweenXValues), (float) values[i]));
            }
        }
        LineDataSet lineDataSet = new LineDataSet(entries, caption);
        LineData lineData = new LineData(lineDataSet);
        //Set color of text next to blue rectangle in linechart
        lineData.setValueTextColor(getResources().getColor(R.color.graphAxisValuesColor, null));
        return lineData;
    }


    /**
     * Creates the array of data for the graphs
     * @param unit
     * @param dataType
     * @return
     */
    private double[] createData(int unit, int dataType, int dir) {

        double[] arr = new double[dataPoints.size()];
        if (dataType == POSITION) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (dir == X_AXIS) ? dataPoints.get(i).getX() : dataPoints.get(i).getY();
            }
        }
        else if (dataType == DISTANCE) {
            boolean running = true;
            int first = 0;

            // Get first point containing data
            while (dataPoints.get(first).getX() == -1) {
                arr[first] = -1;
                first++;
            }
            int second = first+1;
            arr[first] = 0;

            while (running) {
                if (second >= arr.length) {
                    running = false;

                } else {
                    if (dataPoints.get(second).getX() == -1) {
                        arr[second] = -1;
                        second++;

                    } else {
                        if (dataPoints.get(first).getX() != -1) {
                            arr[second] = (dir == X_AXIS) ?
                                    Math.abs(dataPoints.get(second).getX()
                                            - dataPoints.get(first).getX()) + arr[first] :
                                    Math.abs(dataPoints.get(second).getY()
                                            - dataPoints.get(first).getY()) + arr[first];
                            second++;
                            first++;
                        } else {
                            if (second == first + 1) {
                                second++;
                                first++;
                            } else {
                                first++;
                            }
                        }
                    }
                }
            }
        }
        return (unit == PIXELS) ?  arr : convertPixels(arr, unit);
    }


    private String createLegendText(int dataType, int dir) {
        String s = (dir == X_AXIS) ? "Horizontal ": "Vertical ";
        s += (dataType == POSITION) ? "Position": "Distance";
        return s;
    }

    private double[] convertPixels(double[] arr, int unit) {
        double pixelPerCenti = (dataPoints.getAverageDiameter(Math.min(dataPoints.size(), 5))/5.5);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == -1) {continue;}
            if (unit == CENTIMETERS) {
                arr[i] = (arr[i]/pixelPerCenti);
            }
            else if (unit == METERS) {
                arr[i] = (arr[i]/(pixelPerCenti* 100));
            }
        }
        return arr;
    }


    /**
     * Returns the String that will describe the unit on the graph
     * @param unit unit type
     * @return unit as String
     */
    private String getUnitDescription(int unit) {
        switch(unit) {
            case PIXELS:
                return getResources().getString(R.string.pixels);
            case CENTIMETERS:
                return getResources().getString(R.string.centimeters);
            case METERS:
                return getResources().getString(R.string.meters);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Initiates custom actionbar
     */
    private void initActionBar() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            View v = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(v, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            bar.setDisplayShowCustomEnabled(true);

            ((TextView)v.findViewById(R.id.custom_actionbar_title)).
                    setText(getResources().getString(R.string.graphs));

            (findViewById(R.id.custom_actionbar_back_arrow))
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });

        }
        else {
            Log.e("OldGraphActivity", "Actionbar not found");}
    }

    /**
     * Creates list of bitmap of the processed images
     */
    private void initBitmaps() {
        bitmaps = new Bitmap[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            bitmaps[i] = dataPoints.get(i).getImg();
        }
        // Init imageview to first available image
        int i = 0;
        while (i < bitmaps.length - 1 && bitmaps[i] == null) {i++;}
        ((ImageView)(findViewById(R.id.graph_image_view))).setImageBitmap(bitmaps[i]);
    }

    /**
     * Calculates the data of the video on the given path
     * @param path path to video
     */
    private void initCalculateVideoData(final String path) {
        AsyncLoadingTask.TaskAction action = new AsyncLoadingTask.TaskAction() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {

            }

            @Override
            public void onPostExecute() {

            }
        };
        AsyncCalculateData calculateData = new AsyncCalculateData(action, path);
        calculateData.setLoadingMessage(context.getResources()
                .getString(R.string.videolist_loading_graph_description));
        calculateData.execute();
    }

    /**
     * Creates the buttons used to switch between data types
     */
    private void initDataButtons() {
        LinearLayout ll = findViewById(R.id.graph_data_button_linear_layout);
        Button xButton = new Button(this);
        Button yButton = new Button(this);
        Button posButton = new Button(this);
        Button distButton = new Button(this);
        xButton.setText("Horizontal ");
        yButton.setText("Vertical");
        posButton.setText("Position");
        distButton.setText("Distance");
        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChartData(unitDisplayed, dataDisplayed, X_AXIS);
            }
        });
        yButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChartData(unitDisplayed, dataDisplayed, Y_AXIS);
            }
        });
        posButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChartData(unitDisplayed, POSITION, directionDisplayed);
            }
        });
        distButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChartData(unitDisplayed, DISTANCE, directionDisplayed);
            }
        });

        ll.addView(xButton);
        ll.addView(yButton);
        ll.addView(posButton);
        ll.addView(distButton);

    }

    /**
     * Creates the graph
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initChart() {
        lineChart = new LineChart(this);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                double i = (1000*(e.getX()))/msPerFrame;
                ((ImageView)(findViewById(R.id.graph_image_view)))
                        .setImageBitmap(bitmaps[(int)i]);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        // Zoom out button appears when zoomed in
        lineChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!lineChart.isFullyZoomedOut()) {
                    findViewById(R.id.graph_view_zoom_button).setVisibility(View.VISIBLE);
                }
                else {
                    findViewById(R.id.graph_view_zoom_button).setVisibility(View.INVISIBLE);
                }
                return false;
            }

        });
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxis(YAxis.AxisDependency.RIGHT).setEnabled(false);
        lineChart.getAxis(YAxis.AxisDependency.LEFT).setAxisMinimum(0);
        lineChart.getAxis(YAxis.AxisDependency.LEFT).setTextColor(getResources().getColor(R.color.graphAxisValuesColor, null));
        lineChart.getXAxis().setTextColor(getResources().getColor(R.color.graphAxisValuesColor, null));
        lineChart.getLegend().setTextColor(getResources().getColor(R.color.graphAxisValuesColor,null));
        updateChartDescription(PIXELS);
        ((FrameLayout)findViewById(R.id.graph_frame_layout)).addView(lineChart);
    }

    /**
     * Creates the unit spinner
     */
    private void initUnitSpinner() {
        final Spinner spinner = findViewById(R.id.graph_view_unit_spinner);
        spinner.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.graphUnits, R.layout.custom_spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = spinner.getItemAtPosition(i).toString();
                if (lineChart != null) {
                    if (item.equals(getResources().getString(R.string.pixels))) {
                        updateChartData(PIXELS, dataDisplayed, directionDisplayed);
                    } else if (item.equals(getResources().getString(R.string.centimeters))) {
                        updateChartData(CENTIMETERS, dataDisplayed, directionDisplayed);
                    }
                    else if (item.equals(getResources().getString(R.string.meters))) {
                        updateChartData(METERS, dataDisplayed, directionDisplayed);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Creates the view of the activity
     */
    private void initView() {
        initActionBar();
        initChart();
        initUnitSpinner();
        initBitmaps();
        initDataButtons();
        initZoomButton();
    }

    private void initZoomButton() {
        findViewById(R.id.graph_view_zoom_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while (!lineChart.isFullyZoomedOut()) {lineChart.zoomOut();}
                view.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Updates chart description
     * @param unit unit that will be displayed on update
     */
    private void updateChartDescription(int unit) {
        Description d = new Description();
        String s = "Y: " + getUnitDescription(unit) + " X: seconds";
        d.setText(s);
        d.setTextColor(getResources().getColor(R.color.graphAxisValuesColor, null));
        lineChart.setDescription(d);
    }

    /**
     * Updates graph
     * @param unit unit representing data
     * @param dataType data type to be displayed
     */
    private void updateChartData(int unit, int dataType, int dir) {
        LineData lineData = convertToLineData(createData(unit, dataType, dir),
                msPerFrame,
                createLegendText(dataType, dir));

        lineChart.getAxis(YAxis.AxisDependency.LEFT).setAxisMaximum(((float)(lineData.getYMax()*1.5)));
        lineChart.setData(lineData);

        updateChartDescription(unit);
        lineChart.invalidate();
        dataDisplayed = dataType;
        unitDisplayed = unit;
        directionDisplayed = dir;
    }

    private DataPointList rndDataPointList(int size){
        DataPointList dataPointList = new DataPointList();
        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.img_placeholder);
        for (int i = 0; i < size; i++){
            Random rnd = new Random();
            long us = 1000;
            int x = i * (int) us/1000;
            int y = i + rnd.nextInt(3);
            double width = 100;
            DataPoint dp = new DataPoint(x, y, width, us, img);
            dataPointList.add(dp);
        }
        return dataPointList;
    }

    // Need to extend AsyncLoadingTask to override doInBackground to be able to
    // display loading progress with publishProgress. This is cause by the structure of the
    // AsyncLoadingTask and how it lets you set tasks.
    private class AsyncCalculateData extends AsyncLoadingTask {


        private final String path;

        private AsyncCalculateData(TaskAction action, final String path) {
            super(context, action, true);
            this.path = path;
        }

        @Override
        protected String doInBackground(String... strings) {
            ImageProcessing imageProcessing = new ImageProcessing(context);
            ImageProcessing.ProgressRunnable r = new ImageProcessing.ProgressRunnable() {
                @Override
                public void run() {
                    publishProgress(getProgress());
                }
            };
            dataPoints = imageProcessing.getVideoData(path, msPerFrame, r);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Setup Graph UI etc
                    initView();
                }
            });
            return null;
        }
    }
}
