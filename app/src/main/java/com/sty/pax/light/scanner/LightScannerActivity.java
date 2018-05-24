package com.sty.pax.light.scanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.pax.dal.entity.DecodeResult;
import com.sty.pax.light.scanner.tool.InactivityTimer;
import com.sty.pax.light.scanner.tool.LightScanner;
import com.sty.pax.light.scanner.tool.LightScannerManager;
import com.sty.pax.light.scanner.view.ViewfinderView;

import java.io.IOException;

public class LightScannerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, OnClickListener {
    public static final String CLOSE_SCANNER_INTENT_ACTION = "android.intent.action.CloseLightScanner";
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private boolean isOpen = false;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Camera camera;
    private byte[] data;
    private ViewfinderView viewfinderView;
    private ImageView ivHeaderBack;

    private int timeout;
    private InactivityTimer inactivityTimer;

    private BroadcastReceiver mDestroyActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(CLOSE_SCANNER_INTENT_ACTION)){
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_scanner);

        loadParameters();
        initViews();
        registerDestroyActivityReceiver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                openCamera();
            }
        }).start();
    }

    private void loadParameters(){
        timeout = getIntent().getIntExtra(LightScanner.TIMEOUT, 2 * 60);
    }

    private void initViews() {
        surfaceView = findViewById(R.id.surface_view);
        viewfinderView = findViewById(R.id.viewfinder_view);
        ivHeaderBack = findViewById(R.id.iv_header_back);
        ivHeaderBack.setOnClickListener(this);

        inactivityTimer = new InactivityTimer(this, timeout);
    }

    private void openCamera() {
        if (!isOpen) {
            LightScannerManager.getInstance().init(this, WIDTH, HEIGHT);

            holder = surfaceView.getHolder();
            holder.addCallback(this);
            initCamera();
            camera.addCallbackBuffer(data);
            camera.setPreviewCallbackWithBuffer(this);

            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
            //drawViewfinder();
            isOpen = !isOpen;
        } else {
            releaseRes();
            isOpen = !isOpen;
        }
    }

    private void initCamera() {
        camera = Camera.open(0);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(WIDTH, HEIGHT);
        parameters.setPictureSize(WIDTH, HEIGHT);
        parameters.setZoom(parameters.getZoom());
        camera.setParameters(parameters);
        setCameraDisplayOrientation(this, 0, camera);

        // For formats besides YV12, the size of the buffer is determined by multiplying the preview image width,
        // height, and bytes per pixel. The width and height can be read from Camera.Parameters.getPreviewSize(). Bytes
        // per pixel can be computed from android.graphics.ImageFormat.getBitsPerPixel(int) / 8, using the image format
        // from Camera.Parameters.getPreviewFormat().
        float bytesPerPixel = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / (float) 8;
        data = new byte[(int) (bytesPerPixel * WIDTH * HEIGHT)];

        Log.i("Test", "previewFormat:" + parameters.getPreviewFormat() + " bytesPerPixel:" + bytesPerPixel
                + " prewidth:" + parameters.getPreviewSize().width + " preheight:" + parameters.getPreviewSize().height);
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (data != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Log.i("Test", "dataLen:" + data.length);
                    long startTime = System.currentTimeMillis();
                    DecodeResult decodeResult = LightScannerManager.getInstance().decode(data);
                    long timeCost = System.currentTimeMillis() - startTime;
//                    String res = "timeCost:" + timeCost + "\nresult:" +
//                            ((decodeResult == null || decodeResult.getContent() == null) ? "null" : decodeResult.getContent());
//                    Log.i("Test", res);
                    camera.addCallbackBuffer(data);
//                    Intent intent = new Intent();
//                    intent.putExtra("SCAN_RESULT", res);
                    if(decodeResult.getContent() != null){
                        //setResult(RESULT_OK, intent);
                        sendSuccessBroadcast(decodeResult.getContent());
                        //finish();
                    }
                }
            }).start();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        if(isOpen) {
            releaseRes();
        }
        unregisterReceiver(mDestroyActivityReceiver);
        super.onDestroy();
    }

    private void releaseRes() {
        LightScannerManager.getInstance().release();
        camera.setPreviewCallbackWithBuffer(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera){
        //See android.hardware.Camera.setCameraDisplayOrientation for documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation(activity);
        Log.i("Test", "rotation:-->" + degrees);
        int result;
        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; //compensate the mirror
        }else{
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private int getDisplayRotation(Activity activity){
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation){
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }

    public void drawViewfinder(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewfinderView.drawViewfinder();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_header_back:
                sendCancelBroadcast();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            sendCancelBroadcast();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void registerDestroyActivityReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CLOSE_SCANNER_INTENT_ACTION);
        registerReceiver(mDestroyActivityReceiver, intentFilter);
    }

    private void sendSuccessBroadcast(String qrCodeStr){
        Intent intent = new Intent(LightScanner.SCAN_INTENT_ACTION);
        intent.putExtra(LightScanner.FLAGS, LightScanner.SUCCESS_FLAG);
        intent.putExtra(LightScanner.QR_CODE_STR, qrCodeStr);
        sendBroadcast(intent);
    }

    private void sendCancelBroadcast(){
        Intent intent = new Intent(LightScanner.SCAN_INTENT_ACTION);
        intent.putExtra(LightScanner.FLAGS, LightScanner.CANCEL_FLAG);
        sendBroadcast(intent);
    }

    public void sendScanErrorBroadcast(){
        Intent intent = new Intent(LightScanner.SCAN_INTENT_ACTION);
        intent.putExtra(LightScanner.FLAGS, LightScanner.ERROR_FLAG);
        sendBroadcast(intent);
    }
}
