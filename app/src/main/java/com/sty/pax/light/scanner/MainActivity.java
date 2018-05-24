package com.sty.pax.light.scanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pax.dal.IDAL;
import com.pax.neptunelite.api.NeptuneLiteUser;
import com.sty.pax.light.scanner.tool.LightScanner;

public class MainActivity extends AppCompatActivity {
    private Button btStartScan;
    private TextView tvResult;
    public static IDAL idal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews(){
        btStartScan = findViewById(R.id.btn_start_scan);
        tvResult = findViewById(R.id.tv_result);
        btStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, LightScannerActivity.class);
//                startActivityForResult(intent, 0);
                startScanner();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK){
            String result = data.getStringExtra("SCAN_RESULT");
            tvResult.setText(result);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startScanner(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final LightScanner lightScanner = new LightScanner(MainActivity.this, 15);
                lightScanner.open();
                lightScanner.start(new LightScanner.LightScannerListener() {
                    @Override
                    public void onReadSuccess(String result) {
                        lightScanner.close();
                        Log.i("sty", "扫码成功：" + result);
                    }

                    @Override
                    public void onReadError() {
                        lightScanner.close();
                        Log.i("sty", "扫码失败");
                    }

                    @Override
                    public void onCancel() {
                        lightScanner.close();
                        Log.i("sty", "扫码被用户取消");
                    }
                });
            }
        }).start();
    }
}
