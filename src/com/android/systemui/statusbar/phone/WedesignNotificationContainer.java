package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.WedesignNotifQsSwitchListener;

public class WedesignNotificationContainer extends LinearLayout implements View.OnClickListener {
    public WedesignNotificationContainer(Context context) {
        super(context);
    }

    public WedesignNotificationContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WedesignNotificationContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WedesignNotificationContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private WedesignNotifQsSwitchListener mNotifQsSwitchListener;

    public void setNotifQsSwitchListener(WedesignNotifQsSwitchListener notifQsSwitchListener) {
        mNotifQsSwitchListener = notifQsSwitchListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Button btnSwitchQS = (Button) findViewById(R.id.btn_switch_qs);
        btnSwitchQS.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_qs:
                mNotifQsSwitchListener.switchToQs();
                break;
        }
    }
}
