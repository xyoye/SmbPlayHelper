package com.xyoye.smbplayhelper.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.xyoye.smbplayhelper.R;

public class LoadingDialog extends Dialog {
    private String msg;
    private TextView textView;

    public LoadingDialog(Context context) {
        super(context, R.style.LoadingDialog);
    }

    public LoadingDialog(Context context, String msg) {
        super(context, R.style.LoadingDialog);
        this.msg = msg;
    }

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loading);

        textView = this.findViewById(R.id.msg_tv);
        if (!TextUtils.isEmpty(msg)) {
            this.setCancelable(false);
            textView.setText(msg);
        }
    }

    public void updateText(String text) {
        textView.setText(text);
    }
}