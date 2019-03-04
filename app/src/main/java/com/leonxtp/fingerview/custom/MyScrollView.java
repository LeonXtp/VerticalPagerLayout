package com.leonxtp.fingerview.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.leonxtp.fingerview.util.Logger;

/**
 * Created by LeonXtp on 2018/12/28 下午9:01
 * 不能自己上下滑动的ScrollView， 作为最顶层的父容器
 */
@Deprecated
public class MyScrollView extends ScrollView {

    private final String TAG = getClass().getSimpleName();

    // true if we can scroll (not locked)
    // false if we cannot scroll (locked)
    private boolean mScrollable = true;

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        boolean intercept = mScrollable && super.onInterceptTouchEvent(ev);
        Logger.d(TAG, "onInterceptTouchEvent: " + intercept);
        return intercept;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Logger.d(TAG, "onTouchEvent ACTION_DOWN");
                // if we can scroll pass the event to the superclass
                break;
            case MotionEvent.ACTION_MOVE:
                Logger.d(TAG, "onTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_CANCEL:
                Logger.d(TAG, "onTouchEvent ACTION_CANCEL");
                break;
            case MotionEvent.ACTION_UP:
                Logger.d(TAG, "onTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return mScrollable && super.onTouchEvent(ev);
    }

}
