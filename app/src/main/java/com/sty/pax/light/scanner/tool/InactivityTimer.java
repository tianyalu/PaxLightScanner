package com.sty.pax.light.scanner.tool;

import android.app.Activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Finishes an activity after a period of inactivity.
 * Created by Steven.S on 2018/5/24/0024.
 */
public final class InactivityTimer {
    private int INACTIVITY_DELAY_SECONDS = 2 * 60;
    private final ScheduledExecutorService inactivityTimer =
            Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
    private final Activity activity;
    private ScheduledFuture<?> inactivityFuture = null;

    public InactivityTimer(Activity activity){
        this.activity = activity;
        onActivity();
    }

    public InactivityTimer(Activity activity, int timeout){
        this.activity = activity;
        this.INACTIVITY_DELAY_SECONDS = timeout;
        onActivity();
    }

    public void onActivity(){
        cancel();
        inactivityFuture = inactivityTimer.schedule(new FinishListener(activity),
                INACTIVITY_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void cancel(){
        if(inactivityFuture != null){
            inactivityFuture.cancel(true);
            inactivityFuture = null;
        }
    }

    public void shutdown(){
        cancel();
        inactivityTimer.shutdown();
    }

    private static final class  DaemonThreadFactory implements ThreadFactory{
        public Thread newThread(Runnable runnable){
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    }
}
