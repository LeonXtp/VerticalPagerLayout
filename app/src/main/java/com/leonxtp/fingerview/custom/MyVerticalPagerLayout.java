package com.leonxtp.fingerview.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.leonxtp.fingerview.util.Logger;

/**
 * Created by LeonXtp on 2018/12/28 下午9:01
 * 垂直方向的、Page大小任意的PagerLayout
 */
public class MyVerticalPagerLayout extends LinearLayout {

    private final String TAG = getClass().getSimpleName();

    public MyVerticalPagerLayout(Context context) {
        super(context);
    }

    public MyVerticalPagerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float previousTouchX, previousTouchY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                Logger.w(TAG, "onInterceptTouchEvent ACTION_DOWN");

                intercept = false;
                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                break;
            case MotionEvent.ACTION_MOVE:

                Logger.w(TAG, "onInterceptTouchEvent ACTION_MOVE");

                float moveX = ev.getX() - previousTouchX;
                float moveY = ev.getY() - previousTouchY;

                Logger.w(TAG, "moveX: " + moveX);
                Logger.w(TAG, "moveY: " + moveY);

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                if (Math.abs(moveY) > Math.abs(moveX)) {
                    Logger.w(TAG, "onTouchEvent move vertically");
                    intercept = true;
                } else {
                    Logger.w(TAG, "onTouchEvent move horizontally");
                }


                break;
            case MotionEvent.ACTION_CANCEL:
                Logger.w(TAG, "onInterceptTouchEvent ACTION_CANCEL");
            case MotionEvent.ACTION_UP:
                Logger.w(TAG, "onInterceptTouchEvent ACTION_UP");
                intercept = false;
                break;
            default:
                break;
        }
        Logger.w(TAG, "onInterceptTouchEvent: " + intercept);
        return intercept;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                Logger.w(TAG, "onTouchEvent ACTION_DOWN");

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                Logger.w(TAG, "onTouchEvent ACTION_MOVE");

                float moveX = ev.getX() - previousTouchX;
                float moveY = ev.getY() - previousTouchY;

                Logger.w(TAG, "moveX: " + moveX);
                Logger.w(TAG, "moveY: " + moveY);

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                if (Math.abs(moveY) > Math.abs(moveX)) {
                    Logger.w(TAG, "onTouchEvent move vertically");
                    onMoveVertical(moveY);
                } else {
                    Logger.w(TAG, "onTouchEvent move horizontally");
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                Logger.w(TAG, "onTouchEvent ACTION_CANCEL");
                break;
            case MotionEvent.ACTION_UP:
                Logger.w(TAG, "onTouchEvent ACTION_UP");
                break;
            default:
                break;
        }

        return true;
    }

    private void onMoveVertical(float moveY) {
        scrollBy(0, -(int) moveY);
    }


    private void onActionUp() {

    }

}
