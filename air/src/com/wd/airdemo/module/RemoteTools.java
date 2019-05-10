package com.wd.airdemo.module;

import java.util.List;
import java.util.ArrayList;

import android.os.RemoteException;

import com.wd.ms.ITaskBinder;
import com.wd.ms.ITaskCallback;

public class RemoteTools {
    private static ITaskBinder mModule;

    public static void setTaskBinder(ITaskBinder module) {
        mModule = module;
    }

    public static void register(ITaskCallback cb, int code, int update) {
        if (mModule == null) return;
        try {
            mModule.registerCallback(cb, code, update);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public static void unregister(ITaskCallback cb, int code) {
        if (mModule == null) return;
        try {
            mModule.unregisterCallback(cb, code);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void cmd(int cmd, int val) {
        cmd(cmd, new int[]{val}, null, null);
    }

    public static void cmd(int cmd, int val1, int val2) {
        cmd(cmd, new int[]{val1, val2}, null, null);
    }

    public static void cmd(int cmd, String str) {
        cmd(cmd, null, null, new String[]{str});
    }

    public static void cmd(int cmd, int[] ints, float[] flts, String[] strs) {
        System.out.println("mModule: " + mModule);
        if (mModule == null) return;
        System.out.println("cmd " + cmd +"-"+ (ints != null ? (ints[0]):0));
        try {
            mModule.cmd(cmd, ints, flts, strs);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //********************* 熄屏、亮屏功能 ****************************
    public static final int SCREEN_STATE_ON = 0;
    public static final int SCREEN_STATE_OFF = 1;

    private static ITaskBinder mMainModule;

    public static void setMainTaskBinder(ITaskBinder module) {
        mMainModule = module;
    }

    //下发熄屏命令:1灭屏   0亮屏
    public static void screenOff() {
        System.out.println("cmd screenOff");
        try {
            if(mMainModule != null) {
                mMainModule.cmd(FinalMain.C_BLACKOUT, new int[]{SCREEN_STATE_OFF}, null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //下发亮屏命令:1灭屏   0亮屏
    public static void screenOn() {
        System.out.println("cmd screenOn");
        try {
            if(mMainModule != null) {
                mMainModule.cmd(FinalMain.C_BLACKOUT, new int[]{SCREEN_STATE_ON}, null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //监听屏幕状态：当熄屏或亮屏时通知监听者。
    private static List<OnScreenStateChangeListener> mScreenStateListenerList = new ArrayList<>();

    public static interface OnScreenStateChangeListener {
        public void OnScreenStateChange(int screenState);
    }

    public static void setOnScreenStateChangeListener(OnScreenStateChangeListener listenerr){
        mScreenStateListenerList.add(listenerr);
    }

    public static void registerScreenStateListener() {
        try {
            if (mMainModule == null)
                return;

            /**
             * 第二个参数，表示监听U_BLACKOUT这个值的变化
             * 第三个参数，表示立即更新
             * */
            mMainModule.registerCallback(new ITaskCallback.Stub() {
                public void update(int updateCode, int[] ints, float[] flts, String[] strs) throws android.os.RemoteException {
                    if (updateCode == FinalMain.U_BLACKOUT) {
                        if (ints != null && ints.length > 0) {
                            int value = ints[0];    //这个就是需要的值。
                            System.out.println("update screenState = " + value);
                            // 通知所有监听者屏幕状态改变
                            for (OnScreenStateChangeListener listener : mScreenStateListenerList) {
                                listener.OnScreenStateChange(value);
                            }
                        }
                    }
                }
            }, FinalMain.U_BLACKOUT, 1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
