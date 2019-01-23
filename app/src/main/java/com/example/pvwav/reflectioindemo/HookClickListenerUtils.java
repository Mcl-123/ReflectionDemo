package com.example.pvwav.reflectioindemo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HookClickListenerUtils {
    private static HookClickListenerUtils mHookClickListenerUtils;

    private HookClickListenerUtils() {
    }

    public static HookClickListenerUtils getInstance() {
        synchronized ("getInstance") {
            if (mHookClickListenerUtils == null) {
                mHookClickListenerUtils = new HookClickListenerUtils();
            }
        }
        return mHookClickListenerUtils;
    }

    public void hookDecorViewClick(View decorView) {
        if (decorView instanceof ViewGroup) {
            int count = ((ViewGroup) decorView).getChildCount();
            for (int i = 0; i < count; i++) {
                hookDecorViewClick(((ViewGroup) decorView).getChildAt(i));
            }
        } else {
            hookViewClick(decorView);
        }
    }

    public void hookViewClick(View view) {
        try {
            Class viewClass = Class.forName("android.view.View");
            Method getListenerInfoMethod = viewClass.getDeclaredMethod("getListenerInfo");
            if (!getListenerInfoMethod.isAccessible()) {
                getListenerInfoMethod.setAccessible(true);
            }
            Object listenerInfoObject = getListenerInfoMethod.invoke(view);// 反射view中的getListenerInfo方法

            Class mListenerInfoClass = Class.forName("android.view.View$ListenerInfo");
            Field mOnClickListenerField = mListenerInfoClass.getDeclaredField("mOnClickListener");
            mOnClickListenerField.setAccessible(true);
            mOnClickListenerField.set(listenerInfoObject, new HookClickListener((View.OnClickListener) mOnClickListenerField.get(listenerInfoObject)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class HookClickListener implements View.OnClickListener {

        long time = 0;

        private View.OnClickListener onClickListener;

        public HookClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        @Override public void onClick(View v) {

            if (time != 0 && (System.currentTimeMillis() - time) < 2000 ) {
                return;
            }
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
            time = System.currentTimeMillis();
        }
    }
}

