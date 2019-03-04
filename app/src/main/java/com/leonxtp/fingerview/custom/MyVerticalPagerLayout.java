package com.leonxtp.fingerview.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.leonxtp.fingerview.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeonXtp on 2018/12/28 下午9:01
 * 垂直方向的、Page大小任意的PagerLayout
 */
public class MyVerticalPagerLayout extends LinearLayout {

    private final String TAG = getClass().getSimpleName();

    private Scroller mScroller;

    public MyVerticalPagerLayout(Context context) {
        super(context);
        init(context);
    }

    public MyVerticalPagerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private List<Integer> mChildHeightsList = new ArrayList<>();
    // 内容View的高度
    private int mContentHeight = 0;
    // 子view可滚动的高度，如：父view高度100，子View高度加起来200，那么可滚动的区域就是200-100=100
    private int mScrollableHeight = 0;

    private static final float OVER_SCROLL_DAMPING_COEFFICIENT = 0.2f;

    private void init(Context context) {
        mScroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Logger.d(TAG, "onMeasure");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        Logger.d(TAG, "onLayout, changed = " + changed);
        if (changed) {
            mChildHeightsList.clear();
            mContentHeight = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                mChildHeightsList.add(child.getHeight());
                mContentHeight += child.getHeight();
                Logger.d(TAG, "child " + i + ", height: " + child.getHeight());
            }
            mScrollableHeight = mContentHeight - getHeight();
        }
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

                float moveX = previousTouchX - ev.getX();
                float moveY = previousTouchY - ev.getY();

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

                float moveX = previousTouchX - ev.getX();
                float moveY = previousTouchY - ev.getY();

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
            case MotionEvent.ACTION_UP:
                Logger.w(TAG, "onTouchEvent ACTION_UP");
                onActionUp();
                break;
            default:
                break;
        }

        return true;
    }

    private void onActionDown() {

    }

    /**
     * 单次MotionEvent事件中，手指垂直滑动时的处理
     *
     * @param moveY 手指滑动的距离，向上为+， 向下为-。
     */
    private void onMoveVertical(float moveY) {
        int scrollY = getScrollY();
        Logger.d(TAG, "onMoveVertical, scrollY = " + scrollY + ", moveY = " + moveY);
        if (getScrollY() <= 0 && moveY < 0) {
            onMoveOverScroll(moveY);
        } else if (mScrollableHeight >= 0 && getScrollY() >= mScrollableHeight && moveY > 0) {
            onMoveOverScroll(moveY);
        } else {
            onMoveInside(moveY);
        }
    }

    private void onMoveOverScroll(float moveY) {
        scrollBy(0, (int) (moveY * OVER_SCROLL_DAMPING_COEFFICIENT));
    }

    private void onMoveInside(float moveY) {
        scrollBy(0, (int) moveY);
    }

    private void onActionUp() {
        mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), 200);
        postInvalidateOnAnimation();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {

            final int oldX = getScrollX();
            final int oldY = getScrollY();
            final int x = mScroller.getCurrX();
            final int y = mScroller.getCurrY();

            Logger.d(TAG, "computeScroll: " + y);
            if (oldX != x || oldY != y) {
                scrollTo(x, y);

                // Keep on drawing until the animation has finished.
                postInvalidateOnAnimation();
            }
        } else {
            Logger.d(TAG, "not computeScroll");
        }

    }

}
