package com.example.g10kandidat2019;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.g10kandidat2019.CameraCalibration.CalibrateActivity;
import com.example.g10kandidat2019.graph.DataPointList;
import com.example.g10kandidat2019.graph.GraphActivity;

public class SettingsActivity extends AppCompatActivity {
    private int seekBarValue;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        setContentView(R.layout.activity_settings);
        Switch autoDist = findViewById(R.id.settingsSwitchAutomaticDistance);
        SeekBar seekBar =  findViewById(R.id.seekBarSettings);
        textView =  findViewById(R.id.textViewSettings);
        autoDist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = buttonView.getContext().getSharedPreferences(
                        getResources().getString(R.string.currentSettings), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(getResources().getString(R.string.sharedPrefAutomateDistance),
                        isChecked);
                editor.apply();
               /* Toast.makeText(buttonView.getContext(), "autodist has " +
                        (success ? "successfully ":"failed its ")+ " changed",
                        Toast.LENGTH_SHORT).show();*/
            }
        });

        autoDist.setChecked(getSharedPreferences(getResources().getString(R.string.currentSettings),
                Context.MODE_PRIVATE).getBoolean(
                        getResources().getString(
                                R.string.sharedPrefAutomateDistance),false));

        /*
        Button btnDebugDualGraph = findViewById(R.id.btnDebugDualGraph);
        btnDebugDualGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GraphActivity.class);
                intent.putExtra("Debug", true);
                intent.putExtra("Dual", true);
                startActivity(intent);
            }
        });

        Button btnDebugSingleGraph = findViewById(R.id.btnDebugSingleGraph);
        btnDebugSingleGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GraphActivity.class);
                intent.putExtra("Debug", true);
                intent.putExtra("Single", true);
                startActivity(intent);
            }
        });
        */
        Button btnDebugVideoAndGraph = findViewById(R.id.btnDebugVideoAndGraph);
        btnDebugVideoAndGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GraphActivity.class);
                intent.putExtra("Debug", true);
                intent.putExtra("VideoAndGraph", true);
                startActivity(intent);
            }
        });

        Button calibration = findViewById(R.id.calib3d_button);
        calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CalibrateActivity.class);
                startActivity(intent);
            }
        });


        textView.setText("Choose capture rate (datapoints per second) " + seekBar.getProgress() + "/" + seekBar.getMax());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekBarValue = i;
                textView.setText("Choose capture rate (datapoints per second) " + seekBarValue + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Choose capture rate (datapoints per second) " + seekBarValue + "/" + seekBar.getMax());
                getSeekBarValue();
            }
        });

    }

    private void getSeekBarValue() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SettingsData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        System.out.println("SETTINSDATA: " + (1.0/seekBarValue)*1000);
        editor.putInt("SettingsData", (int)((1.0/seekBarValue)*1000));
        editor.apply();
    }





}
