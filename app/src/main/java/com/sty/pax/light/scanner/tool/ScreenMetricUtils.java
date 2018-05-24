package com.sty.pax.light.scanner.tool;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * Created by shity on 2017/8/28/0028.
 * 密度(dpi:dots per inch) = 120(160)(240) px/inch
 * density = (dpi px/inch) / (160 px/inch) = 1.x 无单位
 */


public class ScreenMetricUtils {

    private ScreenMetricUtils(){}

    /**
     * 获取屏幕density
     * @param cxt
     * @return
     */
    public static double getDensity(Context cxt){
        DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
        return dm.density;
    }

    /**
     * 分别获取屏幕X,Y的density
     * @param cxt
     * @return
     */
    public static PointF getDensityXY(Context cxt){
        DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
        PointF pointF = new PointF();
        pointF.set(dm.xdpi, dm.ydpi);
        return pointF;
    }

    /**
     * 获取屏幕densityDpi
     * @param cxt
     * @return
     */
    public static double getDensityDpi(Context cxt){
        DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
        return dm.densityDpi;
    }

    /**
     * 获取屏幕宽度和高度像素（打印）
     * @param cxt
     * @return
     */
    public static Point getPrintScreenPixel(Context cxt){
        DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
        Point point = new Point();
        point.set(dm.widthPixels, dm.heightPixels);
        return point;
    }

    /**
     * 获取屏幕真实宽度和高度像素
     * @param activity
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Point getRealScreenPixel(Activity activity){
        Point point = new Point();
        //PPI: Pixels per inch
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        return point;
    }

    /**
     * 获取屏幕的Inch
     * @param activity
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static double getScreenInchOfDevice(Activity activity){
        Point point = getRealScreenPixel(activity);
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        double x = Math.pow(point.x / dm.xdpi, 2);
        double y = Math.pow(point.y / dm.ydpi, 2);
        return Math.sqrt(x + y);
    }

    /**
     * 获取屏幕尺寸
     * @param context
     * @return
     */
    public static double getScreenInch(Activity context) {
        double mInch = 0.0;

        try {
            int realWidth = 0;
            int realHeight = 0;
            Display display = context.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            if (Build.VERSION.SDK_INT >= 17) {
                Point size = new Point();
                display.getRealSize(size);
                realWidth = size.x;
                realHeight = size.y;
            } else if (Build.VERSION.SDK_INT < 17
                    && Build.VERSION.SDK_INT >= 14) {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } else {
                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            }

            mInch =formatDouble(Math.sqrt((realWidth/metrics.xdpi) * (realWidth /metrics.xdpi) + (realHeight/metrics.ydpi) * (realHeight / metrics.ydpi)),1);


        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }

        return mInch;
    }
    /**
     * Double类型保留指定位数的小数，返回double类型（四舍五入）
     * newScale 为指定的位数
     */
    private static double formatDouble(double d,int newScale) {
        BigDecimal bd = BigDecimal.valueOf(d);
        return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * dp转换为像素 (pixel = dip * density)
     * @param cxt
     * @param dp ：dip,设备无关像素
     * @return
     */
    public static int convertDpToPixel(Context cxt, int dp){
        DisplayMetrics displayMetrics = cxt.getResources().getDisplayMetrics();
        return (int)(dp * displayMetrics.density);
    }

    /**
     * 像素转换为dp (dip = pixel / density)
     * @param cxt
     * @param pixel :像素
     * @return
     */
    public static int convertPixelToDp(Context cxt, int pixel){
        DisplayMetrics displayMetrics = cxt.getResources().getDisplayMetrics();
        return (int)(pixel/displayMetrics.density);
    }

    /**
     * 根据dip值转化成px值
     *
     * @param context
     * @param dip
     * @return
     */
    public static int dipToPix(Context context, int dip) {
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
        return size;
    }

    public static float spToPx(Context context, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    /**
     * 获取状态栏高度(像素)
     * @param cxt
     * @return
     */
    public static int getStatusBarHeight(Context cxt){
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = cxt.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            //根据资源ID获取相应的尺寸
            statusBarHeight = cxt.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 获取底部导航栏的高度(像素)
     * @param cxt
     * @return
     */
    public static int getNavigationBarHeight(Context cxt){
        int navigationBarHeight = -1;
        //获取navigation_bar_height资源的ID
        int resourceId = cxt.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if(resourceId > 0){
            //根据资源ID获取相应的尺寸
            navigationBarHeight = cxt.getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }

    /**
     * 获取屏幕实际宽度(像素)
     * @param activity
     * @return
     */
    public static int getScreenWidth(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕实际高度(像素)
     * @param activity
     * @return
     */
    public static int getScreenHeight(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 得到最短边的dip 注意不是pixel
     *
     * @param activity
     * @return
     */
    public static float getRealScreenShortSideDip(Activity activity) {
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return point.x < point.y ? point.x / displayMetrics.density : point.y / displayMetrics.density;
    }

}
