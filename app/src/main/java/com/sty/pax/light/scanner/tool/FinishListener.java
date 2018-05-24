package com.sty.pax.light.scanner.tool;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import com.sty.pax.light.scanner.LightScannerActivity;

/**
 * Created by Steven.S on 2018/5/24/0024.
 */
public class FinishListener implements DialogInterface.OnClickListener, OnCancelListener, Runnable {
    private final Activity activityToFinish;

    public FinishListener(Activity activityToFinish){
        this.activityToFinish = activityToFinish;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        run();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        run();
    }

    @Override
    public void run() {
        ((LightScannerActivity) activityToFinish).sendScanErrorBroadcast();
    }

}
