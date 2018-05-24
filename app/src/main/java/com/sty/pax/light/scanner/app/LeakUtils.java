package com.sty.pax.light.scanner.app;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Steven.S on 2018/5/24/0024.
 */
public class LeakUtils {
    private static RefWatcher refWatcher;

    public static void init(Application application){
        if(LeakCanary.isInAnalyzerProcess(application)){
            return;
        }
        refWatcher = LeakCanary.install(application);
    }

    public static void watch(Object watchedReference){
        if(null == refWatcher){
            return;
        }
        refWatcher.watch(watchedReference);
    }
}
