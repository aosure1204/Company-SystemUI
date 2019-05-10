package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.wd.airdemo.module.RemoteTools;

//********************* 亮屏功能 ****************************
public class WedesignNavigationBarView extends LinearLayout {
    private static final String TAG = "WDNavigationBarView";

    private boolean isScreenOff;

    public WedesignNavigationBarView(Context context) {
        super(context);
    }

    public WedesignNavigationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WedesignNavigationBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WedesignNavigationBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.d(TAG, "onInterceptTouchEvent: isScreenOff = " + isScreenOff);
        boolean isIntercept = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(isScreenOff) {
                    isIntercept = true;
                } else {
                    isIntercept = false;
                }
                break;
            default:
                isIntercept = false;
                break;
        }
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent screenOn");
                RemoteTools.screenOn();
                break;
        }
        return true;
    }

    public void setScreenState(boolean isScreenOff) {
        Log.d(TAG, "setScreenState: isScreenOff = " + isScreenOff);
        this.isScreenOff = isScreenOff;
    }

}
