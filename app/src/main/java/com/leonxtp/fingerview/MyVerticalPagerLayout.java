package com.leonxtp.fingerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Created by LeonXtp on 2018/12/28 下午9:01
 * 垂直方向的、Page大小任意的PagerLayout
 */
public class MyVerticalPagerLayout extends LinearLayout {

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

    public MyVerticalPagerLayout(Context context) {
        super(context);
    }

    public MyVerticalPagerLayout(Context context, AttributeSet attrs) {
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
                Logger.d(TAG, "ACTION_DOWN");
                // if we can scroll pass the event to the superclass
                return mScrollable && super.onTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                Logger.d(TAG, "ACTION_MOVE");
                break;
            case MotionEvent.ACTION_CANCEL:
                Logger.d(TAG, "ACTION_CANCEL");
                break;
            case MotionEvent.ACTION_UP:
                Logger.d(TAG, "ACTION_UP");
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

}
