package com.example.g10kandidat2019.graph;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.g10kandidat2019.R;

class LoadingDialog extends Dialog {

    private String msg;
    private final boolean horizontal;

    public LoadingDialog(@NonNull Context context, boolean horizontal) {
        this(context, "", horizontal);
    }

    private LoadingDialog(@NonNull Context context, String msg, boolean horizontal) {
        super(context);
        this.msg = msg;
        this.horizontal = horizontal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_dialog);
        ((TextView)findViewById(R.id.loading_dialog_text)).setText(msg);
        if (horizontal) {
            findViewById(R.id.progressbar_horizontal).setVisibility(View.VISIBLE);
        }
    }

    public void setMessage(String s) {this.msg = s;}

    public void setProgress(int i) {
        ((ProgressBar)findViewById(R.id.progressbar_horizontal)).setProgress(i);
    }

    public void setSize() {
        if (getWindow() != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = 450;
            getWindow().setAttributes(layoutParams);
        }
    }
}
