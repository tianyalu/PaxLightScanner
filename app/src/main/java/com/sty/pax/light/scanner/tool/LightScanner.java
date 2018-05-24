package com.sty.pax.light.scanner.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sty.pax.light.scanner.LightScannerActivity;

/**
 * Created by Steven.S on 2018/5/23/0023.
 */
public class LightScanner {
    public static final String SCAN_INTENT_ACTION = "android.intent.action.OpenLightScanner";
    public static final String FLAGS = "FLAGS";
    public static final String QR_CODE_STR = "QR_CODE_STR";
    public static final String TIMEOUT = "TIMEOUT";
    public static final int SUCCESS_FLAG = 0;
    public static final int CANCEL_FLAG = 1;
    public static final int ERROR_FLAG = 2;

    private Context context;
    private LightScannerListener listener;
    private int mTimeout = 2 * 60; //默认超时时间为2分钟
    private boolean isBroadcastRegistered = false;
    private String qrCodeStr;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SCAN_INTENT_ACTION)){
                int flag = intent.getIntExtra(FLAGS, CANCEL_FLAG);
                switch (flag){
                    case SUCCESS_FLAG:
                        qrCodeStr = intent.getStringExtra(QR_CODE_STR);
                        if(listener != null){
                            listener.onReadSuccess(qrCodeStr);
                        }
                        break;
                    case CANCEL_FLAG:
                        if(listener != null){
                            listener.onCancel();
                        }
                        break;
                    case ERROR_FLAG:
                    default:
                        if(listener != null){
                            listener.onReadError();
                        }
                        break;
                }
            }
        }
    };

    public LightScanner(Context context){
        this.context = context;
    }

    public LightScanner(Context context, int timeoutSec){
        this.context = context;
        this.mTimeout = timeoutSec;
    }

    public interface LightScannerListener{
        void onReadSuccess(String result);

        void onReadError();

        void onCancel();
    }

    public void start(LightScannerListener listener){
        this.listener = listener;
    }

    public void open(){
        Intent intent = new Intent(context, LightScannerActivity.class);
        intent.putExtra(TIMEOUT, mTimeout);
        context.startActivity(intent);

        registerOpenBroadcastReceiver();
    }

    public void close(){
        unRegisterMyBroadcastReceiver();

        Intent intent = new Intent(LightScannerActivity.CLOSE_SCANNER_INTENT_ACTION);
        context.sendBroadcast(intent);
    }

    private void registerOpenBroadcastReceiver(){
        if(!isBroadcastRegistered){
            isBroadcastRegistered = !isBroadcastRegistered;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SCAN_INTENT_ACTION);
            context.registerReceiver(receiver, intentFilter);
        }
    }

    private void unRegisterMyBroadcastReceiver(){
        if(isBroadcastRegistered){
            isBroadcastRegistered = !isBroadcastRegistered;
            context.unregisterReceiver(receiver);
        }
    }
}
