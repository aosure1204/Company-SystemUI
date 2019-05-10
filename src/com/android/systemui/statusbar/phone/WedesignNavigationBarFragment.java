package com.android.systemui.statusbar.phone;

import java.io.IOException;
import java.lang.Override;
import java.util.List;
import java.util.ArrayList;
import java.lang.*;

import android.annotation.Nullable;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.internal.util.CharSequences;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.fragments.FragmentHostManager;
import android.annotation.Nullable;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.qs.tiles.DataUsageDetailView;
import com.android.systemui.recents.Recents;
import com.android.systemui.stackdivider.Divider;
import com.wd.airdemo.module.DataCarbus;
import com.wd.airdemo.module.DataUtil;
import com.wd.airdemo.module.FinalCanbus;
import com.wd.airdemo.module.RemoteTools;
import com.wd.airdemo.util.IUiNotify;

import com.android.systemui.R;

public class WedesignNavigationBarFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    public static final String TAG = "WDNavigationBarFragment";
    private static final boolean DEBUG = false;

    private StatusBar mStatusBar;
    private Recents mRecents;
    private Divider mDivider;

    private WindowManager mWindowManager;

    private WedesignNavigationBarView mWedesignNavigationBarView;
    private ImageButton mBtnNavAirDirection;
    private Button mBtnNavAirTemperature;

    private List<View> mAirWindowViews = new ArrayList<View>();

    //********************* 亮屏功能 ****************************
    private boolean isScreenOff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusBar = SysUiServiceProvider.getComponent(getContext(), StatusBar.class);
        mRecents = SysUiServiceProvider.getComponent(getContext(), Recents.class);
        mDivider = SysUiServiceProvider.getComponent(getContext(), Divider.class);

        RemoteTools.setOnScreenStateChangeListener(mOnScreenStateChangeListener);
    }

    private RemoteTools.OnScreenStateChangeListener mOnScreenStateChangeListener = new RemoteTools.OnScreenStateChangeListener() {
        @Override
        public void OnScreenStateChange(int screenState){
            Log.d(TAG, "OnScreenStateChange: screenState = " + screenState);
            isScreenOff = false;
            if (screenState == RemoteTools.SCREEN_STATE_OFF) {
                isScreenOff = true;
            }
            mWedesignNavigationBarView.setScreenState(isScreenOff);
        }
    };
    //********************* 亮屏功能 ****************************

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        mWedesignNavigationBarView = (WedesignNavigationBarView) inflater.inflate(R.layout.wedesign_navigation_bar, container, false);
        WedesignNavigationBarView view = mWedesignNavigationBarView;

        ImageButton btnNavAirSwitch = (ImageButton) view.findViewById(R.id.btn_nav_air_switch);
        btnNavAirSwitch.setOnClickListener(this);
        ImageButton btnNavAirVolume = (ImageButton) view.findViewById(R.id.btn_nav_air_volume);
        btnNavAirVolume.setOnClickListener(this);
        mBtnNavAirDirection = (ImageButton) view.findViewById(R.id.btn_nav_air_direction);
        mBtnNavAirDirection.setOnClickListener(this);
        ImageButton btnNavAirAuto = (ImageButton) view.findViewById(R.id.btn_nav_air_auto);
        btnNavAirAuto.setOnClickListener(this);
        mBtnNavAirTemperature = (Button) view.findViewById(R.id.btn_nav_air_temperature);
        mBtnNavAirTemperature.setOnClickListener(this);
        ImageButton btnNavHome = (ImageButton) view.findViewById(R.id.btn_nav_home);
        btnNavHome.setOnClickListener(this);
        btnNavHome.setOnLongClickListener(this);
        ImageButton btnNavVehicleControl = (ImageButton) view.findViewById(R.id.btn_nav_vehicle_control);
        btnNavVehicleControl.setOnClickListener(this);
        ImageButton btnNavNavigation = (ImageButton) view.findViewById(R.id.btn_nav_navigation);
        btnNavNavigation.setOnClickListener(this);
        ImageButton btnNavMusic = (ImageButton) view.findViewById(R.id.btn_nav_music);
        btnNavMusic.setOnClickListener(this);
        ImageButton btnNavVoice = (ImageButton) view.findViewById(R.id.btn_nav_voice);
        btnNavVoice.setOnClickListener(this);
        ImageButton btnNavPower = (ImageButton) view.findViewById(R.id.btn_nav_power);
        btnNavPower.setOnClickListener(this);

        return mWedesignNavigationBarView;
    }

    @Override
    public boolean onLongClick(View v) {
        boolean result = false;

        switch (v.getId()) {
            case R.id.btn_nav_home:
                onLongPressRecents();
                result = true;
                break;
        }

        return result;
    }

    private boolean onLongPressRecents() {
        if (mRecents == null || !ActivityManager.supportsMultiWindow(getContext())
                || !mDivider.getView().getSnapAlgorithm().isSplitScreenFeasible()) {
            return false;
        }

        return mStatusBar.toggleSplitScreenMode(MetricsEvent.ACTION_WINDOW_DOCK_LONGPRESS,
                MetricsEvent.ACTION_WINDOW_UNDOCK_LONGPRESS);
    }

    @java.lang.Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: event.getAction() = " + event.getAction());
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            clearAllAirWindow();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: enter");
        clearAllAirWindow();
        switch (v.getId()) {
            case R.id.btn_nav_air_switch:
                Log.d(TAG, "onClick: air_switch");
                if(mOnoff == DataUtil.AirOff) {
                    sendCmd(FinalCanbus.C_Onoff, DataUtil.TRANSFER_VALUE_01);   //0x00：OFF， 0x01：ON
                    Toast.makeText(getContext(), R.string.air_conditioner_turn_on, Toast.LENGTH_LONG).show();
                } else if(mOnoff == DataUtil.AirOn) {
                    sendCmd(FinalCanbus.C_Onoff, DataUtil.TRANSFER_VALUE_00);   //0x00：OFF， 0x01：ON
                    Toast.makeText(getContext(), R.string.air_conditioner_turn_off, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_nav_air_volume:
                View airVolumeView = createAirWindow(v.getContext(), R.layout.air_volume_window, -578, 0);
                handlerAirVolumeWindow(airVolumeView);
                break;
            case R.id.btn_nav_air_direction:
                View airDirectionView = createAirWindow(v.getContext(), R.layout.air_direction_window, -415, 0);
                handlerAirDirectionWindow(airDirectionView);
                break;
            case R.id.btn_nav_air_auto:
                Log.d(TAG, "onClick: air_switch");
                if(isAutoMode) {
                    sendCmd(FinalCanbus.C_AutoMode, DataUtil.TRANSFER_VALUE_00);   //0x00：非AUTO， 0x01：AUTO状态
                    Toast.makeText(getContext(), R.string.air_auto_turn_off, Toast.LENGTH_LONG).show();
                } else {
                    sendCmd(FinalCanbus.C_AutoMode, DataUtil.TRANSFER_VALUE_01);   //0x00：非AUTO， 0x01：AUTO状态
                    sendCmd(FinalCanbus.C_AcMode, DataUtil.TRANSFER_VALUE_01);   //0x00：无动作， 0x01：AC功能
                    Toast.makeText(getContext(), R.string.air_auto_turn_on, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_nav_air_temperature:
                View airTempView = createAirWindow(v.getContext(), R.layout.air_temp_window, -315, 0);
                handlerAirTempWindow(airTempView);
                break;
            case R.id.btn_nav_home:
                simulateKeyEvent(KeyEvent.KEYCODE_HOME);
                break;
            case R.id.btn_nav_vehicle_control:
                startApp(v.getContext(), "com.wd.carsetting");
                break;
            case R.id.btn_nav_navigation:
                startApp(v.getContext(), "cn.jyuntech.map");
                break;
            case R.id.btn_nav_music:
                startApp(v.getContext(), "com.wd.music");
                break;
            case R.id.btn_nav_voice:
                startApp(v.getContext(), "com.txznet.adapter");
                break;
            case R.id.btn_nav_power:
//                simulateKeyEvent(KeyEvent.KEYCODE_POWER);
                if(!isScreenOff) {
                    RemoteTools.screenOff();
                }
                break;
        }
    }

    private void startApp(Context context, String pkg){
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(pkg);
        if(intent == null) {
            Toast.makeText(context, R.string.app_is_not_installed, Toast.LENGTH_SHORT).show();
        } else {
            startActivity(intent);
        }
    }

    //模拟按键事件
    public void simulateKeyEvent(final int KeyCode) {
        new Thread(new Runnable() {
            public void run() {
                // 开线程调用方法
                try {
                    Instrumentation inst=new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 异常catch
                }
            }
        }).start();
    }

    //添加、删除空调弹窗 ******************

    private void clearAllAirWindow() {
        for(View view : mAirWindowViews) {
            mWindowManager.removeView(view);
        }
        mAirWindowViews.clear();
    }

    private View createAirWindow(Context context, int resourceId, int xpos, int ypos) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                xpos, ypos,
                WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,
                WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                        | WindowManager.LayoutParams.FLAG_SLIPPERY,
                PixelFormat.TRANSLUCENT);
        lp.token = new Binder();
        lp.gravity = Gravity.BOTTOM;
        // this will allow the navbar to run in an overlay on devices that support this
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }
        lp.setTitle("AirWindow");
        lp.windowAnimations = 0;

        View airView = LayoutInflater.from(context).inflate(
                resourceId, null);
        airView.setOnTouchListener(this);
        mAirWindowViews.add(airView);
//        View airView = initWindowView(context, resourceId);

        if (DEBUG) Log.v(TAG, "addAirWindow: about to add " + airView);

        mWindowManager = context.getSystemService(WindowManager.class);
        mWindowManager.addView(airView, lp);

        return airView;
    }

    // 处理弹窗中View的逻辑；显示及事件 ************************************

    //风量弹窗：界面显示及事件处理
    private void handlerAirVolumeWindow(View rootView){
        SeekBar mBarAirVolume = (SeekBar) rootView.findViewById(R.id.bar_air_volume);
        mBarAirVolume.setMax(mAirLevelMax);
        mBarAirVolume.setProgress(mAirLevel);

        TextView mTextAirVolume = (TextView) rootView.findViewById(R.id.text_air_volume);
        mTextAirVolume.setText(Integer.toString(mAirLevel));

        mBarAirVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @java.lang.Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAirLevel = progress;
                mTextAirVolume.setText(Integer.toString(progress));
            }

            @java.lang.Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @java.lang.Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendCmd(FinalCanbus.C_AirLevel, mAirLevel);
            }
        });
    }

    //出风模式弹窗：界面显示及事件处理
    private void handlerAirDirectionWindow(View rootView){
        ImageButton btnAirDirFace = (ImageButton) rootView.findViewById(R.id.btn_air_dir_face);
        btnAirDirFace.setOnClickListener(airDirectionClickListener);
        ImageButton btnAirDirFoot = (ImageButton) rootView.findViewById(R.id.btn_air_dir_foot);
        btnAirDirFoot.setOnClickListener(airDirectionClickListener);
        ImageButton btnAirDirFaceFoot = (ImageButton) rootView.findViewById(R.id.btn_air_dir_face_foot);
        btnAirDirFaceFoot.setOnClickListener(airDirectionClickListener);
        ImageButton btnAirDirFootDefrost = (ImageButton) rootView.findViewById(R.id.btn_air_dir_foot_defrost);
        btnAirDirFootDefrost.setOnClickListener(airDirectionClickListener);
        ImageButton btnAirDirDefrost = (ImageButton) rootView.findViewById(R.id.btn_air_dir_defrost);
        btnAirDirDefrost.setOnClickListener(airDirectionClickListener);
    }

    private View.OnClickListener airDirectionClickListener = new View.OnClickListener() {
        @java.lang.Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_air_dir_face:
                    sendCmd(FinalCanbus.C_OutletMode, DataUtil.TRANSFER_VALUE_01);
                    onOutletModeChange(DataUtil.TRANSFER_VALUE_01);
                    break;
                case R.id.btn_air_dir_foot:
                    sendCmd(FinalCanbus.C_OutletMode, DataUtil.TRANSFER_VALUE_02);
                    onOutletModeChange(DataUtil.TRANSFER_VALUE_02);
                    break;
                case R.id.btn_air_dir_face_foot:
                    sendCmd(FinalCanbus.C_OutletMode, DataUtil.TRANSFER_VALUE_03);
                    onOutletModeChange(DataUtil.TRANSFER_VALUE_03);
                    break;
                case R.id.btn_air_dir_foot_defrost:
                    sendCmd(FinalCanbus.C_OutletMode, DataUtil.TRANSFER_VALUE_04);
                    onOutletModeChange(DataUtil.TRANSFER_VALUE_04);
                    break;
                case R.id.btn_air_dir_defrost:
                    sendCmd(FinalCanbus.C_OutletMode, DataUtil.TRANSFER_VALUE_05);
                    onOutletModeChange(DataUtil.TRANSFER_VALUE_05);
                    break;
            }
        }
    };

    //空调温度弹窗：界面显示及事件处理
    private void handlerAirTempWindow(View rootView) {
        SeekBar mBarAirTemp = (SeekBar) rootView.findViewById(R.id.bar_air_temp);
        mBarAirTemp.setMax(mTempLevelMax);
        mBarAirTemp.setProgress(mTempLevel);

        final TextView mTextAirTemp = (TextView) rootView.findViewById(R.id.text_air_temp);
        int tempValue = mTempMinValue + mTempLevel * DataUtil.TEMP_STEP;
        mTextAirTemp.setText(getTempStr(tempValue));

        mBarAirTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @java.lang.Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTempLevel = progress;
                int tempValue = mTempMinValue + mTempLevel * DataUtil.TEMP_STEP;
                mTextAirTemp.setText(getTempStr(tempValue));
            }

            @java.lang.Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @java.lang.Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int tempValue = mTempMinValue + mTempLevel * DataUtil.TEMP_STEP;
                sendCmd(FinalCanbus.C_Temp, tempValue);
                onTemperatureChange(tempValue);
            }
        });
    }

    // 添加NavigationBar窗口 *******************************

    public static View create(Context context, FragmentHostManager.FragmentListener listener) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                        | WindowManager.LayoutParams.FLAG_SLIPPERY,
                PixelFormat.TRANSLUCENT);
        lp.token = new Binder();
        // this will allow the navbar to run in an overlay on devices that support this
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }
        lp.setTitle("NavigationBar");
        lp.windowAnimations = 0;

        View navigationBarView = LayoutInflater.from(context).inflate(
                R.layout.wedesign_navigation_bar_window, null);

        if (DEBUG) Log.v(TAG, "addNavigationBar: about to add " + navigationBarView);
        if (navigationBarView == null) return null;

        context.getSystemService(WindowManager.class).addView(navigationBarView, lp);
        FragmentHostManager fragmentHost = FragmentHostManager.get(navigationBarView);
        WedesignNavigationBarFragment fragment = new WedesignNavigationBarFragment();
        fragmentHost.getFragmentManager().beginTransaction()
                .replace(R.id.wedesign_navigation_bar_frame, fragment, TAG)
                .commit();
        fragmentHost.addTagListener(TAG, listener);
        return navigationBarView;
    }

    // 响应空调逻辑 *******************************

    //空调工作模式
    private int mRunMode;
    //空调开关
    private int mOnoff;
    //自动模式
    private boolean isAutoMode;
    //空调风量最大值，不同工作模式有2、4、8档。
    private int mAirLevelMax;
    //空调风量当前值
    private int mAirLevel;
    //空调温度最大档，不同工作模式有32、12档
    private int mTempLevelMax;
    //空调温度最低值，不同工作模式有165、240.
    private int mTempMinValue;
    //空调温度当前档
    private int mTempLevel;

    @Override
    public void onResume() {
        super.onResume();

        registerDataListener();

        // 注意以下代码是为了没有收到lib层数据时，应用依然能正常显示而写。正式发布时需要去掉这行代码。
        onRunModeChange(DataUtil.TRANSFER_VALUE_00);
        //****************************
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterDataListener();
    }

    private void sendCmd(int i, int id) {
        RemoteTools.cmd(i, id);
    }

    /**
     * 添加数据监听
     * */
    private void registerDataListener(){
        DataCarbus.NOTIFY[FinalCanbus.U_RunMode].addNotify(mNotify, 1);
        DataCarbus.NOTIFY[FinalCanbus.U_Onoff].addNotify(mNotify, 1);
        DataCarbus.NOTIFY[FinalCanbus.U_AirLevel].addNotify(mNotify, 1);
        DataCarbus.NOTIFY[FinalCanbus.U_OutletMode].addNotify(mNotify, 1);
        DataCarbus.NOTIFY[FinalCanbus.U_AutoMode].addNotify(mNotify, 1);
        DataCarbus.NOTIFY[FinalCanbus.U_Temp].addNotify(mNotify, 1);
    }

    /**
     * 移除数据监听
     * */
    private void unregisterDataListener(){
        DataCarbus.NOTIFY[FinalCanbus.U_RunMode].removeNotify(mNotify);
        DataCarbus.NOTIFY[FinalCanbus.U_Onoff].removeNotify(mNotify);
        DataCarbus.NOTIFY[FinalCanbus.U_AirLevel].removeNotify(mNotify);
        DataCarbus.NOTIFY[FinalCanbus.U_OutletMode].removeNotify(mNotify);
        DataCarbus.NOTIFY[FinalCanbus.U_AutoMode].removeNotify(mNotify);
        DataCarbus.NOTIFY[FinalCanbus.U_Temp].removeNotify(mNotify);
    }

    private IUiNotify mNotify = new IUiNotify() {
        @Override
        public void onNotify(int updateCode, int[] ints, float[] flts, String[] strs) {
            int value = DataCarbus.DATA[updateCode];
            System.out.println("airdemo:onNotify  " + updateCode + ":" + value);
            switch (updateCode) {
                case FinalCanbus.U_RunMode:
                    onRunModeChange(value);
                    break;
                case FinalCanbus.U_Onoff:
                    onOnoffChange(value);
                    break;
                case FinalCanbus.U_AirLevel:
                    onAirLevelChange(value);
                    break;
                case FinalCanbus.U_OutletMode:
                    onOutletModeChange(value);
                    break;
                case FinalCanbus.U_AutoMode:
                    onAutoModeChange(value);
                    break;
                case FinalCanbus.U_Temp:
                    onTemperatureChange(value);
                    break;
            }
        }
    };

    /**
     * 空调工作模式
     *
     * 返回值有以下几种：
     * 0x00：行车模式
     * 0x01: 驻车白天模式
     * 0x02: 驻车夜晚模式
     * 0x03：否定应答
     * */
    public void onRunModeChange(int data) {
        switch (data) {
            case DataUtil.TRANSFER_VALUE_00:
                mRunMode = DataUtil.RUN_MODE_DRIVING;
                mAirLevelMax = DataUtil.AIR_LEVEL_MAX_DRIVING;
                mTempLevelMax = DataUtil.TEMP_LEVEL_MAX_DRIVING;
                mTempMinValue = DataUtil.TEMP_MIN_VALUE_DRIVING;
                break;
            case DataUtil.TRANSFER_VALUE_01:
                mRunMode = DataUtil.RUN_MODE_PARKING_DAYTIME;
                mAirLevelMax = DataUtil.AIR_LEVEL_MAX_PARKING_DAYTIME;
                mTempLevelMax = DataUtil.TEMP_LEVEL_MAX_PARKING_DAYTIME;
                mTempMinValue = DataUtil.TEMP_MIN_VALUE_PARKING_DAYTIME;
                break;
            case DataUtil.TRANSFER_VALUE_02:
                mRunMode = DataUtil.RUN_MODE_PARKING_NIGHT;
                mAirLevelMax = DataUtil.AIR_LEVEL_MAX_PARKING_NIGHT;
                mTempLevelMax = DataUtil.TEMP_LEVEL_MAX_PARKING_NIGHT;
                mTempMinValue = DataUtil.TEMP_MIN_VALUE_PARKING_NIGHT;
                break;
        }
    }

    /**
     * 空调开/关
     *
     * 0x00：OFF
     * 0x01：ON
     * 0x02：否定应答
     * */
    private void onOnoffChange(int data) {
        switch (data) {
            case DataUtil.TRANSFER_VALUE_00:
                mOnoff = DataUtil.AirOff;
                break;
            case DataUtil.TRANSFER_VALUE_01:
                mOnoff = DataUtil.AirOn;
                break;
        }
    }

    /**
     * 空调风量
     *
     * 0x00：预留  0x01：1档  0x02：2档   0x03：3档   0x04：4档  0x05：5档   0x06：6档   0x07：7档 0x08：8档  0x09：否定应答
     *
     * */
    private void onAirLevelChange(int data) {
        if(data < 0 || data > mAirLevelMax) {
            Toast.makeText(getContext(), R.string.air_is_out_of_range, Toast.LENGTH_LONG).show();
            return;
        }
        mAirLevel = data;
    }

    /**
     * 空调出风模式
     *
     * 0x01：吹面
     * 0x02：吹面吹脚
     * 0x03：吹脚
     * 0x04：吹脚除霜
     * 0x05：前除霜
     * 0x06：否定应答
     * */
    private void onOutletModeChange(int data) {
        switch (data) {
            case DataUtil.TRANSFER_VALUE_01:
                mBtnNavAirDirection.setImageResource(R.drawable.ic_nav_air_dir_face);
                break;
            case DataUtil.TRANSFER_VALUE_02:
                mBtnNavAirDirection.setImageResource(R.drawable.ic_nav_air_dir_foot);
                break;
            case DataUtil.TRANSFER_VALUE_03:
                mBtnNavAirDirection.setImageResource(R.drawable.ic_nav_air_dir_face_foot);
                break;
            case DataUtil.TRANSFER_VALUE_04:
                mBtnNavAirDirection.setImageResource(R.drawable.ic_nav_air_dir_foot_defrost);
                break;
            case DataUtil.TRANSFER_VALUE_05:
                mBtnNavAirDirection.setImageResource(R.drawable.ic_nav_air_dir_defrost);
                break;
        }
    }

    /**
     * 空调Auto模式
     *
     * 0x00：非AUTO
     * 0x01：AUTO状态
     * 0x02：否定应答
     * */
    private void onAutoModeChange(int data) {
        switch (data) {
            case DataUtil.TRANSFER_VALUE_00:
                isAutoMode = false;
                break;
            case DataUtil.TRANSFER_VALUE_01:
                isAutoMode = true;
                break;
        }
    }

    /**
     * 空调温度
     *
     * 范围:16.5~32.5℃
     * 例如，234表示23.4℃
     * */
    private void onTemperatureChange(int data) {
        String tempStr = getTempStr(data);
        mBtnNavAirTemperature.setText(tempStr);

        mTempLevel = (data - mTempMinValue) / DataUtil.TEMP_STEP;
    }

    //将温度值 165 转换为 16.5°C
    private String getTempStr(int data) {
        if(data < mTempMinValue || data > (mTempMinValue + DataUtil.TEMP_STEP * mTempLevelMax)) {
            Toast.makeText(getContext(), R.string.temperature_is_out_of_range, Toast.LENGTH_LONG).show();
            return "27°C";
        }

        String tempStr;
        if(data%10 == 0) {
            tempStr = data/10 + "°C";
        } else {
            tempStr = data/10 + "." + data%10 + "°C";
        }
        return tempStr;
    }
}
