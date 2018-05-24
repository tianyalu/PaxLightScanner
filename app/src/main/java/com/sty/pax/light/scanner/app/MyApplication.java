package com.sty.pax.light.scanner.app;

import android.app.Application;

import com.pax.dal.IDAL;
import com.pax.neptunelite.api.NeptuneLiteUser;

/**
 * Created by Steven.S on 2018/5/21/0021.
 */
public class MyApplication extends Application {
    private static MyApplication mApp;
    private static IDAL idal;

    @Override
    public void onCreate() {
        MyApplication.mApp = this;
        init();
        LeakUtils.init(this);

        super.onCreate();
    }

    private static void init(){
        try {
            idal = NeptuneLiteUser.getInstance().getDal(getmApp());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MyApplication getmApp() {
        return mApp;
    }

    public static IDAL getIdal() {
        return idal;
    }
}
