package com.forgroundtest.RIS_DSM;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class InfoViewFactory {
    public static View newErrorDialogView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.error_dialog, null, false);
        return view;
    }
}
