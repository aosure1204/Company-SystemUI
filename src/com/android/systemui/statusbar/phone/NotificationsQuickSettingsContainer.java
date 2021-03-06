/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.statusbar.phone;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.support.annotation.DimenRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.fragments.FragmentHostManager.FragmentListener;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.notification.AboveShelfObserver;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;

/**
 * The container with notification stack scroller and quick settings inside.
 */
public class NotificationsQuickSettingsContainer extends FrameLayout
        implements OnInflateListener, FragmentListener,
        AboveShelfObserver.HasViewAboveShelfChangedListener {

    private FrameLayout mQsFrame;
    private View mUserSwitcher;
    // Grace modify.
    //private NotificationStackScrollLayout mStackScroller;
    private LinearLayout mNotificationContainer;
    private View mKeyguardStatusBar;
    private boolean mInflated;
    private boolean mQsExpanded;
    private boolean mCustomizerAnimating;

    private int mBottomPadding;
    private int mStackScrollerMargin;
    private boolean mHasViewsAboveShelf;

    public NotificationsQuickSettingsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mQsFrame = (FrameLayout) findViewById(R.id.qs_frame);
        // Grace modify. Custom status bar expand.
//        mStackScroller = findViewById(R.id.notification_stack_scroller);
//        mStackScrollerMargin = ((LayoutParams) mStackScroller.getLayoutParams()).bottomMargin;
        mNotificationContainer = (LinearLayout) findViewById(R.id.notification_container);
        // Grace end.
        mKeyguardStatusBar = findViewById(R.id.keyguard_header);
        ViewStub userSwitcher = (ViewStub) findViewById(R.id.keyguard_user_switcher);
        userSwitcher.setOnInflateListener(this);
        mUserSwitcher = userSwitcher;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        FragmentHostManager.get(this).addTagListener(QS.TAG, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        FragmentHostManager.get(this).removeTagListener(QS.TAG, this);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadWidth(mQsFrame, R.dimen.qs_panel_width);
        // Grace modify. Custom status bar expand.
//        reloadWidth(mStackScroller, R.dimen.notification_panel_width);
        reloadWidth(mNotificationContainer, R.dimen.notification_panel_width);
        // Grace end.
    }

    /**
     * Loads the given width resource and sets it on the given View.
     */
    private void reloadWidth(View view, @DimenRes int width) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.width = getResources().getDimensionPixelSize(width);
        view.setLayoutParams(params);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mBottomPadding = insets.getStableInsetBottom();
        setPadding(0, 0, 0, mBottomPadding);
        return insets;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean userSwitcherVisible = mInflated && mUserSwitcher.getVisibility() == View.VISIBLE;
        boolean statusBarVisible = mKeyguardStatusBar.getVisibility() == View.VISIBLE;

        final boolean qsBottom = mHasViewsAboveShelf;
        // Grace modify. Custom status bar expand.
//        View stackQsTop = qsBottom ? mStackScroller : mQsFrame;
//        View stackQsBottom = !qsBottom ? mStackScroller : mQsFrame;
        View stackQsTop = mNotificationContainer;
        View stackQsBottom = mQsFrame;
        // Grace end.
        // Invert the order of the scroll view and user switcher such that the notifications receive
        // touches first but the panel gets drawn above.
        if (child == mQsFrame) {
            return super.drawChild(canvas, userSwitcherVisible && statusBarVisible ? mUserSwitcher
                    : statusBarVisible ? mKeyguardStatusBar
                    : userSwitcherVisible ? mUserSwitcher
                    : stackQsBottom, drawingTime);
        // Grace modify. Custom status bar expand.
//        } else if (child == mStackScroller) {
        } else if (child == mNotificationContainer) {
        // Grace end.
            return super.drawChild(canvas,
                    userSwitcherVisible && statusBarVisible ? mKeyguardStatusBar
                    : statusBarVisible || userSwitcherVisible ? stackQsBottom
                    : stackQsTop,
                    drawingTime);
        } else if (child == mUserSwitcher) {
            return super.drawChild(canvas,
                    userSwitcherVisible && statusBarVisible ? stackQsBottom
                    : stackQsTop,
                    drawingTime);
        } else if (child == mKeyguardStatusBar) {
            return super.drawChild(canvas,
                    stackQsTop,
                    drawingTime);
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    @Override
    public void onInflate(ViewStub stub, View inflated) {
        if (stub == mUserSwitcher) {
            mUserSwitcher = inflated;
            mInflated = true;
        }
    }

    @Override
    public void onFragmentViewCreated(String tag, Fragment fragment) {
        QS container = (QS) fragment;
        container.setContainer(this);
    }

    public void setQsExpanded(boolean expanded) {
        if (mQsExpanded != expanded) {
            mQsExpanded = expanded;
            invalidate();
        }
    }

    public void setCustomizerAnimating(boolean isAnimating) {
        if (mCustomizerAnimating != isAnimating) {
            mCustomizerAnimating = isAnimating;
            invalidate();
        }
    }

    public void setCustomizerShowing(boolean isShowing) {
        // Grace delete. Custom status bar expand.
        /*if (isShowing) {
            // Clear out bottom paddings/margins so the qs customization can be full height.
            setPadding(0, 0, 0, 0);
            setBottomMargin(mStackScroller, 0);
        } else {
            setPadding(0, 0, 0, mBottomPadding);
            setBottomMargin(mStackScroller, mStackScrollerMargin);
        }
        mStackScroller.setQsCustomizerShowing(isShowing);*/
        // Grace end.
    }

    private void setBottomMargin(View v, int bottomMargin) {
        LayoutParams params = (LayoutParams) v.getLayoutParams();
        params.bottomMargin = bottomMargin;
        v.setLayoutParams(params);
    }

    @Override
    public void onHasViewsAboveShelfChanged(boolean hasViewsAboveShelf) {
        mHasViewsAboveShelf = hasViewsAboveShelf;
        invalidate();
    }
}
