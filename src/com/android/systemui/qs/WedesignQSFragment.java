package com.android.systemui.qs;

import android.annotation.Nullable;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.IPowerManager;
import android.provider.Settings;
import android.support.annotation.VisibleForTesting;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.android.internal.telephony.TelephonyIntents;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.R.id;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.statusbar.phone.WedesignNotifQsSwitchListener;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.NetworkController;

import java.util.List;

public class WedesignQSFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "WedesignQSFragment";

    private Context mContext;

    private ImageButton mBtnHotspot;
    private ImageButton mBtnBluetooth;
    private ImageButton mBtnSilent;
    private ImageButton mBtnWifi;
    private ImageButton mBtnDataConn;
    private SeekBar mBarScreenBrightness;

    private HotspotController mHotspotController;
    private BluetoothController mBluetoothController;
    private AudioManager mAudioManager;
    private WifiManager mWifiManager;
    private TelephonyManager mTelephonyManager;
    private NetworkController mNetworkController;
    private IPowerManager mPower;
    private BrightnessObserver mBrightnessObserver;

    private boolean isHotspotEnabled;
    private boolean isBluetoothEnabled;
    private boolean isSilentMode;
    private boolean isWifiEnabled;
    private boolean isDataEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.wedesign_qs_panel, container, false);
        mContext = rootView.getContext();

        Button btnSwitchNotificatio = (Button) rootView.findViewById(R.id.btn_switch_notification);
        btnSwitchNotificatio.setOnClickListener(this);
        mBtnHotspot = (ImageButton) rootView.findViewById(R.id.btn_quick_set_hotspot);
        mBtnHotspot.setOnClickListener(this);
        mBtnBluetooth = (ImageButton) rootView.findViewById(R.id.btn_quick_set_bluetooth);
        mBtnBluetooth.setOnClickListener(this);
        mBtnSilent = (ImageButton) rootView.findViewById(R.id.btn_quick_set_mute);
        mBtnSilent.setOnClickListener(this);
        mBtnWifi = (ImageButton) rootView.findViewById(R.id.btn_quick_set_wifi);
        mBtnWifi.setOnClickListener(this);
        mBtnDataConn = (ImageButton) rootView.findViewById(R.id.btn_quick_set_data_conn);
        mBtnDataConn.setOnClickListener(this);
        mBarScreenBrightness = (SeekBar) rootView.findViewById(R.id.bar_screen_brightness);
        mBarScreenBrightness.setOnSeekBarChangeListener(mScreenBrightnessChangeListener);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Dependency.get(WedesignQSIconController.class).addIconGroup(view.findViewById(R.id.wedesign_quick_set_icons));

        mHotspotController = Dependency.get(HotspotController.class);
        mBluetoothController = Dependency.get(BluetoothController.class);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mNetworkController = Dependency.get(NetworkController.class);
        mPower = IPowerManager.Stub.asInterface(ServiceManager.getService(
                Context.POWER_SERVICE));
        mBrightnessObserver = new BrightnessObserver(null);

        updateHotspot(mHotspotController.isHotspotEnabled());
        updateBluetooth(mBluetoothController.isBluetoothEnabled());
        updateVolume();
        updateWifi();
        updateData(mTelephonyManager.isDataEnabled());
        mBarScreenBrightness.setMax(255);   // screen brightness value is 0-255.
        updateBrightness();

        mHotspotController.addCallback(mHotspotCallback);
        mBluetoothController.addCallback(mBluetoothCallback);
        mNetworkController.addCallback(mSignalCallback);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mIntentReceiver, filter);

        mBrightnessObserver.startObserving();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mHotspotController.removeCallback(mHotspotCallback);
        mBluetoothController.removeCallback(mBluetoothCallback);
        mNetworkController.removeCallback(mSignalCallback);

        mContext.unregisterReceiver(mIntentReceiver);

        mBrightnessObserver.stopObserving();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        switch (v.getId()) {
            case R.id.btn_switch_notification:
                mNotifQsSwitchListener.switchToNotification();
                break;
            case R.id.btn_quick_set_hotspot:
                mHotspotController.setHotspotEnabled(!isHotspotEnabled);
                break;
            case R.id.btn_quick_set_bluetooth:
                mBluetoothController.setBluetoothEnabled(!isBluetoothEnabled);
                break;
            case R.id.btn_quick_set_mute:
                if(!isSilentMode){
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
                break;
            case R.id.btn_quick_set_wifi:
                mWifiManager.setWifiEnabled(!isWifiEnabled);
                break;
            case R.id.btn_quick_set_data_conn:
                mTelephonyManager.setDataEnabled(!isDataEnabled);
                break;
        }
    }

    private HotspotController.Callback mHotspotCallback = new HotspotController.Callback() {
        @Override
        public void onHotspotChanged(boolean enabled) {
            updateHotspot(enabled);
        }
    };

    private BluetoothController.Callback mBluetoothCallback = new BluetoothController.Callback() {
        @Override
        public void onBluetoothStateChange(boolean enabled) {
            updateBluetooth(enabled);
        }

        @Override
        public void onBluetoothDevicesChanged() {
        }
    };

    private NetworkController.SignalCallback mSignalCallback = new NetworkController.SignalCallback(){

        @Override
        public void setWifiIndicators(boolean enabled, NetworkController.IconState statusIcon, NetworkController.IconState qsIcon, boolean activityIn, boolean activityOut, String description, boolean isTransient) {

        }

        @Override
        public void setMobileDataIndicators(NetworkController.IconState statusIcon, NetworkController.IconState qsIcon, int statusType, int qsType, boolean activityIn, boolean activityOut, String typeContentDescription, String description, boolean isWide, int subId, boolean roaming) {

        }

        @Override
        public void setSubs(List<SubscriptionInfo> subs) {

        }

        @Override
        public void setNoSims(boolean show) {

        }

        @Override
        public void setEthernetIndicators(NetworkController.IconState icon) {

        }

        @Override
        public void setIsAirplaneMode(NetworkController.IconState icon) {

        }

        @Override
        public void setMobileDataEnabled(boolean enabled) {
            updateData(enabled);
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION) ||
                    action.equals(AudioManager.INTERNAL_RINGER_MODE_CHANGED_ACTION)) {
                updateVolume();
            } else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                updateWifi();
            }
        }
    };

    /** ContentObserver to watch brightness **/
    private class BrightnessObserver extends ContentObserver {
        private final Uri BRIGHTNESS_URI =
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);

        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (selfChange) return;

            if (BRIGHTNESS_URI.equals(uri)) {
                updateBrightness();
            }
        }

        public void startObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(
                    BRIGHTNESS_URI,
                    false, this, UserHandle.USER_ALL);
        }

        public void stopObserving() {
            final ContentResolver cr = mContext.getContentResolver();
            cr.unregisterContentObserver(this);
        }
    }

    private void updateHotspot(boolean enabled) {
        isHotspotEnabled = enabled;
        if(enabled) {
            mBtnHotspot.setImageResource(R.drawable.ic_quick_set_hotspot_on);
        } else {
            mBtnHotspot.setImageResource(R.drawable.ic_quick_set_hotspot_off);
        }
    }

    private void updateBluetooth(boolean enabled) {
        isBluetoothEnabled = enabled;
        if(enabled) {
            mBtnBluetooth.setImageResource(R.drawable.ic_quick_set_bluetooth_on);
        } else {
            mBtnBluetooth.setImageResource(R.drawable.ic_quick_set_bluetooth_off);
        }
    }

    private void updateVolume(){
        if(mAudioManager.isSilentMode()){
            isSilentMode = true;
            mBtnSilent.setImageResource(R.drawable.ic_quick_set_mute_on);
        } else {
            isSilentMode = false;
            mBtnSilent.setImageResource(R.drawable.ic_quick_set_mute_off);
        }
    }

    private void updateWifi() {
        if(mWifiManager.isWifiEnabled()) {
            isWifiEnabled = true;
            mBtnWifi.setImageResource(R.drawable.ic_quick_set_wifi_on);
        } else {
            isWifiEnabled = false;
            mBtnWifi.setImageResource(R.drawable.ic_quick_set_wifi_off);
        }
    }

    private void updateData(boolean enabled) {
        if(enabled) {
            isDataEnabled = true;
            mBtnDataConn.setImageResource(R.drawable.ic_quick_set_data_conn_on);
        } else {
            isDataEnabled = false;
            mBtnDataConn.setImageResource(R.drawable.ic_quick_set_data_conn_off);
        }
    }

    private SeekBar.OnSeekBarChangeListener mScreenBrightnessChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, "onProgressChanged: progress is " + progress);
            setBrightness(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStartTrackingTouch: progress is " + seekBar.getProgress());
            setBrightness(seekBar.getProgress());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStopTrackingTouch: progress is " + seekBar.getProgress());
            setBrightness(seekBar.getProgress());

            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, seekBar.getProgress(),
                    UserHandle.USER_CURRENT);
        }
    };

    private void setBrightness(int brightness) {
        try {
            mPower.setTemporaryScreenBrightnessSettingOverride(brightness);
        } catch (RemoteException ex) {
        }
    }

    private void updateBrightness() {
        try {
            int screenBrightness = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, UserHandle.USER_CURRENT);
            mBarScreenBrightness.setProgress(screenBrightness);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private WedesignNotifQsSwitchListener mNotifQsSwitchListener;

    public void setNotifQsSwitchListener(WedesignNotifQsSwitchListener notifQsSwitchListener) {
        mNotifQsSwitchListener = notifQsSwitchListener;
    }
}
