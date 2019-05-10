package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.R;

public class WedesignStatusBarIconControllerImpl implements WedesignStatusBarIconController{
    private final ArrayMap<Integer, ImageView> mImageViews = new ArrayMap<>();

    public WedesignStatusBarIconControllerImpl(Context context) {
    }

    @Override
    public void addIconGroup(ViewGroup root) {
        mImageViews.put(R.id.img_status_usb_conn, (ImageView)root.findViewById(R.id.img_status_usb_conn));
        mImageViews.put(R.id.img_status_4g, (ImageView)root.findViewById(R.id.img_status_4g));
        mImageViews.put(R.id.img_status_mobile_conn, (ImageView)root.findViewById(R.id.img_status_mobile_conn));
        mImageViews.put(R.id.img_status_bluetooth, (ImageView)root.findViewById(R.id.img_status_bluetooth));
        mImageViews.put(R.id.img_status_wifi, (ImageView)root.findViewById(R.id.img_status_wifi));
        mImageViews.put(R.id.img_status_hotspot, (ImageView)root.findViewById(R.id.img_status_hotspot));
        mImageViews.put(R.id.img_status_volume, (ImageView)root.findViewById(R.id.img_status_volume));
    }

    @Override
    public void setIcon(int viewId, int resourceId) {
        ImageView imgView = mImageViews.get(viewId);
        if(imgView != null) {
            imgView.setImageResource(resourceId);
        }
    }

    @Override
    public void setIconVisibility(int viewId, boolean b) {
        ImageView imgView = mImageViews.get(viewId);
        if(imgView != null) {
            if(b)
                imgView.setVisibility(View.VISIBLE);
            else
                imgView.setVisibility(View.GONE);
        }
    }
}
