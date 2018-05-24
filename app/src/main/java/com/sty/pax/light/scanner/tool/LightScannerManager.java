package com.sty.pax.light.scanner.tool;

import android.content.Context;
import android.util.Log;

import com.pax.dal.IScanCodec;
import com.pax.dal.entity.DecodeResult;
import com.sty.pax.light.scanner.app.MyApplication;

/**
 * Created by Steven.S on 2018/5/21/0021.
 */
public class LightScannerManager {
    private static final String TAG = LightScannerManager.class.getSimpleName();

    private static volatile LightScannerManager instance;
    public static final int WIDTH = 480;
    public static final int HEIGHT = 480;
    private IScanCodec scanCodec;

    private LightScannerManager(){}

    public static LightScannerManager getInstance(){
        if(instance == null){
            synchronized (LightScannerManager.class){
                if(instance == null){
                    instance = new LightScannerManager();
                    instance.scanCodec = MyApplication.getIdal().getScanCodec();
                }
            }
        }
        return instance;
    }

    public void disableFormat(int format){
        scanCodec.disableFormat(format);
        Log.i(TAG, "disableFormat");
    }

    public void enableFormat(int format){
        scanCodec.enableFormat(format);
        Log.i(TAG, "enableFormat");
    }

    public void init(Context context, int width, int height){
        scanCodec.init(context, width, height);
        Log.i(TAG, "init");
    }

    public DecodeResult decode(byte[] data){
        DecodeResult result = scanCodec.decode(data);
        Log.i(TAG, "decode");
        return result;
    }

    public void release(){
        scanCodec.release();
        Log.i(TAG, "release");
    }
}
