package com.android.systemui.statusbar.phone;

import android.view.ViewGroup;

public interface WedesignStatusBarIconController {
    public void addIconGroup(ViewGroup root);
    public void setIcon(int viewId, int resourceId);
    public void setIconVisibility(int viewId, boolean b);
}
