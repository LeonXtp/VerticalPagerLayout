package com.leonxtp.fingerview.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.leonxtp.fingerview.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeonXtp on 2018/12/28 下午9:01
 * 垂直方向的、Page大小任意的PagerLayout
 * <p>
 * 1。 拖动、松开、立即按下问题：
 * 情形1：一只手按住，滑动，然后另一只手指按下，这时，前一只手指抬起，View出现快速变动
 * 情形2：一只手按住，滑动，然后另一只手指按下滑动，这时，前一只手指轻触屏幕
 * 都出现View跳动都情况
 * 原因：
 * 多点触碰引起的ACTION_MOVE的eventY因手指变化出现跳动
 * 解决：
 * 在多点触碰事件触发的时候，下一次ACTION_MOVE事件忽略
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

    // 回弹时，是否可跨越子View
    private boolean isOverMovable = false;

    // 自然状态下，View在自动滚动过程中，再次点按屏幕，滚动过程将不会停下，
    // 需要在自动滚动过程中，继续处理ACTION_DOWN事件，拦截余下事件
    private boolean mIsBeingDragged = false;

    // 当ACTION_POINTER_DOWN 或者 ACTION_POINTER_UP事件触发时，将忽略下一次Move事件
    private boolean isPointerActionTriggered = false;

    private List<Integer> mChildHeightsList = new ArrayList<>();
    // 子view可滚动的高度，如：父view高度100，子View高度加起来200，那么可滚动的区域就是200-100=100
    private int mScrollableHeight = 0;
    // 上次停留状态的y方向偏移
    private int lastStayScrollY = 0;
    // View滑动超出本身的滑动范围时，弹性效果的阻尼系数
    private static final float OVER_SCROLL_DAMPING_COEFFICIENT = 0.2f;

    private OnItemScrollListener mOnItemScrollListener = null;

    private void init(Context context) {
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Logger.e(TAG, "onGlobalLayout...");
                initChildrenHeights();
            }
        });
    }

    public void addOnScrollListener(OnItemScrollListener listener) {
        this.mOnItemScrollListener = listener;
    }

    public void removeScrollListener() {
        this.mOnItemScrollListener = null;
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
        if (!changed) {
            return;
        }
        initChildrenHeights();
    }

    private void initChildrenHeights() {
        mChildHeightsList.clear();
        int mContentHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childHeight = 0;
            if (child.getVisibility() == View.VISIBLE) {
                childHeight = child.getHeight();
            }
            mChildHeightsList.add(childHeight);
            mContentHeight += childHeight;
            Logger.d(TAG, "child " + i + ", height: " + childHeight);
        }
        mScrollableHeight = mContentHeight - getHeight();
    }

    private float previousTouchX, previousTouchY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                Logger.w(TAG, "onInterceptTouchEvent ACTION_DOWN");

                intercept = mIsBeingDragged;
                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                break;
            case MotionEvent.ACTION_MOVE:

                Logger.w(TAG, "onInterceptTouchEvent ACTION_MOVE");

                float moveX = previousTouchX - ev.getX();
                float moveY = previousTouchY - ev.getY();

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
                isPointerActionTriggered = false;

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                onActionDown();

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isPointerActionTriggered = true;
                Logger.w(TAG, "onTouchEvent ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Logger.w(TAG, "onTouchEvent ACTION_MOVE");

                float moveX = previousTouchX - ev.getX();
                float moveY = previousTouchY - ev.getY();

                Logger.w(TAG, "moveY: " + moveY);

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                if (Math.abs(moveY) > Math.abs(moveX)) {
                    Logger.w(TAG, "onTouchEvent move vertically");
                    mIsBeingDragged = true;
                    Logger.d(TAG, "onTouchEvent moving mIsBeingDragged = true");
                    if (!isPointerActionTriggered) {
                        onMoveVertical(moveY);
                    }
                    isPointerActionTriggered = false;
                }

                break;
            case MotionEvent.ACTION_CANCEL:
                Logger.w(TAG, "onTouchEvent ACTION_CANCEL");
            case MotionEvent.ACTION_UP:
                Logger.w(TAG, "onTouchEvent ACTION_UP");
                onActionUp();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                isPointerActionTriggered = true;
                Logger.w(TAG, "onTouchEvent ACTION_POINTER_UP");
                break;
            default:
                break;
        }

        return true;
    }

    private void onActionDown() {
        if (!mScroller.isFinished()) {
            Logger.d(TAG, "onActionDown, mScroller.abortAnimation()");
            mScroller.abortAnimation();
        }
    }

    /**
     * 单次MotionEvent事件中，手指垂直滑动时的处理
     *
     * @param moveY 手指滑动的距离，向上为+， 向下为-。
     */
    private void onMoveVertical(float moveY) {

        int scrollY = getScrollY();
        Logger.d(TAG, "onMoveVertical, scrollY = " + scrollY + ", moveY = " + moveY);
        if (getScrollY() <= 0 && moveY < 0) {// 下拉超出
            onMoveOverScroll(moveY);
        } else if (mScrollableHeight >= 0 && getScrollY() >= mScrollableHeight && moveY > 0) {// 上拉超出
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
        // 调完scrollBy马上再重新getScrollY()，将不会立即见效，因此只能通过本次scrollY+moveY的方式回调滑动状态
        handleMoveListener((int) moveY);
    }

    private void onActionUp() {
        int dy = computeAutoScrollDy();
        if (dy == 0) {
            onAutoScrollFinished();
        } else {
            Logger.d(TAG, "startScroll...dy = " + dy);
            mScroller.startScroll(0, getScrollY(), 0, dy, 300);
            postInvalidateOnAnimation();
        }
    }

    private int computeAutoScrollDy() {

        if (getScrollY() < 0) {// 下拉超出
            return -getScrollY();
        } else if (mScrollableHeight > 0 && getScrollY() > mScrollableHeight) {// 上拉超出
            return -(getScrollY() - mScrollableHeight);
        } else {
            return computeAutoScrollDyInside();
        }
    }

    private int computeAutoScrollDyInside() {
        int scrollY = getScrollY();
        int topY = 0;
        int bottomY = 0;
        for (int i = 0; i < mChildHeightsList.size(); i++) {
            bottomY += mChildHeightsList.get(i);
            if (scrollY <= bottomY) {
                break;
            }
            topY = bottomY;
        }

        if (scrollY - topY < (bottomY - topY) / 2) {// 不到一半，需要回弹
            return topY - scrollY;
        } else { // 等于或超过一半， 需要进击
            return bottomY - scrollY;
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        // 在mScroller没有startScroll的时候，它也会执行
        if (!mScroller.computeScrollOffset()) {
            return;
        }

        final int oldY = getScrollY();
        final int currY = mScroller.getCurrY();
        final int finalY = mScroller.getFinalY();
        Logger.d(TAG, "computeScroll: oldY = " + oldY + ", currY = " + currY + ", finalY = " + finalY +
                ", isFinished = " + mScroller.isFinished());

        // 存在currY == finalY，但是isFinished = false，这时候还会继续调用1～2次computeScroll()的情况
        if (!mScroller.isFinished() || oldY != currY || currY != finalY) {
            scrollTo(0, currY);
            postInvalidateOnAnimation();
            handleAutoScrollListener(currY);
        }

        if (mScroller.isFinished()) {
            onAutoScrollFinished();
        }
    }

    private void onAutoScrollFinished() {
        mIsBeingDragged = false;
        Logger.d(TAG, "mScroller.isFinished(), mIsBeingDragged = false. ");
        if (mOnItemScrollListener != null) {
            float[] firstVisibleItemInfo = findFirstVisibleItem(getScrollY());
            mOnItemScrollListener.onItemSelected(getChildAt((int) firstVisibleItemInfo[0]),
                    (int) firstVisibleItemInfo[0]);
        }
    }

    /**
     * 处理手动滑动过程中的滑动监听
     *
     * @param moveY 本次手指滑动的y方向距离
     */
    private void handleMoveListener(int moveY) {
        int targetScrollY = getScrollY() + moveY;
        handleAutoScrollListener(targetScrollY);
    }

    private void handleAutoScrollListener(int currY) {
        if (currY < 0) {
            // 拉出顶部时的回弹，不处理
            return;
        }
        if (mScrollableHeight > 0 && currY > mScrollableHeight) {
            // 拉出底部的回弹，不处理
            return;
        }

        if (mOnItemScrollListener != null) {
            float[] firstVisibleItemInfo = findFirstVisibleItem(currY);
            mOnItemScrollListener.onItemScrolled(getChildAt((int) firstVisibleItemInfo[0]),
                    (int) firstVisibleItemInfo[0], firstVisibleItemInfo[1]);
        }
    }

    /**
     * 根据目标scrollY找到所应该显示的第一个子View的index和显示的百分比
     *
     * @param targetScrollY 目标scrollY
     * @return float数组，第0个是第一个显示的子View的下标
     * 第1个是第一个子View显示高度占其自身高度的百分比，float类型，[0, 1]
     */
    private float[] findFirstVisibleItem(int targetScrollY) {
        float itemIdex = 0;
        int itemHeight = 0;
        int bottomY = 0;
        for (int i = 0; i < mChildHeightsList.size(); i++) {
            bottomY += mChildHeightsList.get(i);
            if (targetScrollY < bottomY) {
                itemIdex = i;
                itemHeight = mChildHeightsList.get(i);
                break;
            }
        }

        int visibleHeight = bottomY - targetScrollY;
        float visibleOffset = visibleHeight * 1.0f / itemHeight;
        return new float[]{itemIdex, visibleOffset};

    }

}
