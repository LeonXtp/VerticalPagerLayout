package com.leonxtp.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeonXtp on 2019/03/02 下午9:01
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
 * <p>
 * 2. 全部子View不够父View高度，空白区域为何回调onItemSelected(.., 0)?
 * <p>
 * 3. 滑出可见区域的子item代码设置为gone时，整个会自动上移的问题
 */
public class VerticalPagerLayout extends LinearLayout {

    private final String TAG = getClass().getSimpleName();

    private Scroller mScroller;
    private int mTouchSlop;

    public VerticalPagerLayout(Context context) {
        super(context);
        init(context);
    }

    public VerticalPagerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 是否可以手指滑动。控制思路：
     * 1。当外部调用setMoveEnabled()方法禁用本View垂直方向当触摸事件时，不拦截所有触摸事件
     * 2。当没有子View处理触摸事件，该事件又回传到本View时，不做任何处理
     * 则达到禁用触摸事件目的，同时又不会拦截子View。
     */
    private boolean isMoveEnabled = true;
    /**
     * 当前是否正在滑动，包括手指拖动以及自动滚动两个阶段。
     * 自然状态下，View在自动滚动过程中，再次点按屏幕，滚动过程将不会停下，
     * 需要在自动滚动过程中，继续处理ACTION_DOWN事件，拦截余下事件
     */
    private boolean isScrolling = false;
    /**
     * 当ACTION_POINTER_DOWN 或者 ACTION_POINTER_UP事件触发时，将忽略下一次Move事件，原因：
     * 当多指触碰时，抬起一只手指，则下一次{@link MotionEvent#ACTION_MOVE}时，
     * 位置getY()将直接跳到另一只手指的位置，出现滑动闪跳
     */
    private boolean isPointerActionTriggered = false;
    /**
     * 回弹时，是否可跨越子View
     */
    private boolean isOverMovable = false;
    /**
     * 所有子View各自的高度
     */
    private List<Integer> mCurrentChildHeightsList = new ArrayList<>();
    /**
     * 本View的可见高度，因其可能放在一个禁用了滚动效果的ScrollView内部
     */
    private int mVisibleHeight = 0;
    /**
     * 子view可滚动的高度，如：父view高度100，子View高度加起来200，那么可滚动的区域就是200-100=100
     * 当内容View高度不足父容器高度时，此高度=0
     */
    private int mScrollableHeight = 0;
    /**
     * 上次选中的itemIndex，目的：
     * 1。防止当前显示的item没有变化时，一点微小的滑动也会回调
     * {@link OnItemScrollListener#onItemSelected(View, int)}
     */
    private int mLastSelectedItemIndex = 0;
    /**
     * View滑动超出本身的滑动范围时，弹性效果的阻尼系数
     */
    private static final float OVER_SCROLL_DAMPING_COEFFICIENT = 0.2f;

    private OnItemScrollListener mOnItemScrollListener = null;

    private void init(Context context) {
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * =================================================================================================================
     * Public Methods Start
     * =================================================================================================================
     */

    public boolean isScrolling() {
        return this.isScrolling;
    }

    public void addOnScrollListener(OnItemScrollListener listener) {
        this.mOnItemScrollListener = listener;
    }

    public void removeScrollListener() {
        this.mOnItemScrollListener = null;
    }

    public void setMoveEnabled(boolean movable) {
        this.isMoveEnabled = movable;
    }

    /**
     * 滚动到指定位置的item，当这个item不可见时，将会展示其下面当一个item
     */
    public void scrollToItemIndex(int index) {
        int dy = ScrollComputeUtil.getDyForScrollToIndex(mCurrentChildHeightsList, index, getScrollY(),
                mScrollableHeight);
        if (dy != 0) {
            Logger.d(TAG, "scrollToItemIndex, " + index + ", dy = " + dy);
            smoothScrollBy(dy);
        }
    }

    /**
     * 获取上次停止滚动后停留在的item的index
     */
    public int getLastSelectedItemIndex() {
        return mLastSelectedItemIndex;
    }

    public void onItemVisibilityChanged(int index, int visibility) {

    }

    /**
     * =================================================================================================================
     * Public Methods End
     * =================================================================================================================
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getParent() instanceof ScrollView) {
//            Logger.d(TAG, "getParent() instanceof ScrollView");
            // 这里默认其父容器ScrollView已全部可见
            mVisibleHeight = MeasureSpec.getSize(heightMeasureSpec);
//            Logger.d(TAG, "onMeasure: mVisibleHeight = heightMeasureSpec.size = " + mVisibleHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!changed) {
            return;
        }
        initChildrenHeights();
    }

    /**
     * 子View发生变化前所有子View各自的高度
     */
    private List<Integer> mLastChildHeightsList = new ArrayList<>();

    private void initChildrenHeights() {
        int contentHeight = ScrollComputeUtil.initContentHeights(mCurrentChildHeightsList, this);
        int myHeight = getHeight();
        Logger.w(TAG, "contentHeight = " + contentHeight + ", myHeight = " + myHeight);
        int myVisibleHeight = mVisibleHeight == 0 ? myHeight : mVisibleHeight;
        mScrollableHeight = contentHeight - myVisibleHeight > 0 ? contentHeight - myVisibleHeight : 0;
        Logger.w(TAG, "myVisibleHeight = " + myVisibleHeight + ", mScrollableHeight = " + mScrollableHeight);

        if (!handleUnShownViewGone()) {
            handleUnShownViewBecomeVisible();
        }

        mLastChildHeightsList.clear();
        mLastChildHeightsList.addAll(mCurrentChildHeightsList);
    }

    /**
     * 处理当滑出可见区域的子item变为不可见时的情形
     * 期望结果：view不出现上移
     */
    private boolean handleUnShownViewGone() {
        // 找到设置为gone的item下标
        int goneItemIndex = ScrollComputeUtil.findGoneViewWhenNotShown(mCurrentChildHeightsList, mLastChildHeightsList);
        int goneViewHeight = 0;
        // 确定变为gone的view是否在滑出可见区域的时候变的
        if (goneItemIndex != -1 && goneItemIndex < mLastSelectedItemIndex) {
            goneViewHeight = mLastChildHeightsList.get(goneItemIndex);
        }

        float[] firstVisibleItemInfo = ScrollComputeUtil.findFirstShownItem(mCurrentChildHeightsList,
                getScrollY() - goneViewHeight);
        mLastSelectedItemIndex = (int) firstVisibleItemInfo[1] > 0.5 ?
                (int) firstVisibleItemInfo[0] : (int) firstVisibleItemInfo[0] + 1;

        if (goneViewHeight != 0) {
            scrollBy(0, -goneViewHeight);
            return true;
        }
        return false;
    }

    /**
     * 处理当滑出可见区域的子item变为可见时的情形
     * 期望结果：view不出现下移
     */
    private void handleUnShownViewBecomeVisible() {
        int visibleItemIndex = ScrollComputeUtil.findBecomeVisibleViewWhenNotShown(mCurrentChildHeightsList,
                mLastChildHeightsList);

        int visibleViewHeight = 0;
        // 确定变为gone的view是否在滑出可见区域的时候变的
        if (visibleItemIndex != -1) {
            visibleViewHeight = mCurrentChildHeightsList.get(visibleItemIndex);
        }

        float[] firstVisibleItemInfo = ScrollComputeUtil.findFirstShownItem(mCurrentChildHeightsList,
                getScrollY() + visibleViewHeight);
        mLastSelectedItemIndex = (int) firstVisibleItemInfo[1] > 0.5 ?
                (int) firstVisibleItemInfo[0] : (int) firstVisibleItemInfo[0] + 1;

        if (visibleViewHeight != 0) {
            scrollBy(0, visibleViewHeight);
        }
    }

    private float previousTouchX, previousTouchY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                Logger.w(TAG, "onInterceptTouchEvent ACTION_DOWN");

                intercept = isScrolling && isMoveEnabled;
                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                break;
            case MotionEvent.ACTION_MOVE:

                float moveX = previousTouchX - ev.getX();
                float moveY = previousTouchY - ev.getY();
                // 避免点击时因为有一点小小的滑动导致被当成滑动事件，从而点击事件失效
                if (Math.abs(moveY) < mTouchSlop) {
                    break;
                }
//                Logger.w(TAG, "onInterceptTouchEvent ACTION_MOVE， moveY: " + moveY);

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                if (Math.abs(moveY) > Math.abs(moveX)) {
//                    Logger.w(TAG, "onInterceptTouchEvent move vertically");
                    intercept = true;
                } else {
//                    Logger.w(TAG, "onInterceptTouchEvent move horizontally");
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

        boolean handled = true;

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
//                Logger.w(TAG, "onTouchEvent ACTION_MOVE");
                if (!isMoveEnabled) {
                    // 当外部调用setMoveEnabled()方法禁用本View当触摸事件时
                    handled = false;
//                    Logger.w(TAG, "onTouchEvent isMoveEnabled false, break;");
                    break;
                }

                float moveX = previousTouchX - ev.getX();
                float moveY = previousTouchY - ev.getY();
//                Logger.w(TAG, "moveY: " + moveY);

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                if (Math.abs(moveY) > Math.abs(moveX)) {
//                    Logger.w(TAG, "onTouchEvent move vertically");
                    isScrolling = true;
//                    Logger.d(TAG, "onTouchEvent moving isScrolling = true");
                    if (!isPointerActionTriggered) {
                        // 当上次触摸事件中触发了
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

        return handled;
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
     * @param moveY 手指滑动的距离，向上为"+"， 向下为"-"。
     */
    private void onMoveVertical(float moveY) {
        int scrollY = getScrollY();
//        Logger.d(TAG, "onMoveVertical, scrollY = " + scrollY + ", moveY = " + moveY);
        if (ScrollComputeUtil.isMoveOverScroll(scrollY, moveY, mScrollableHeight)) {
            // 需要加阻尼
            handleMoveOverScroll(moveY);
        } else {
            handleMoveInside(moveY);
        }
    }

    private void handleMoveOverScroll(float moveY) {
        scrollBy(0, (int) (moveY * OVER_SCROLL_DAMPING_COEFFICIENT));
    }

    private void handleMoveInside(float moveY) {
        scrollBy(0, (int) moveY);
        // 调完scrollBy马上再重新getScrollY()，将不会立即见效，因此只能通过本次scrollY+moveY的方式回调滑动状态
        handleMoveListener((int) moveY);
    }

    private void onActionUp() {
        int dy = ScrollComputeUtil.computeAutoScrollDy(getScrollY(), mCurrentChildHeightsList, mScrollableHeight);
        if (dy == 0) {
            onAutoScrollFinished();
        } else {
            Logger.d(TAG, "startScroll...dy = " + dy);
            smoothScrollBy(dy);
        }
    }

    private void smoothScrollBy(int dy) {
        mScroller.startScroll(0, getScrollY(), 0, dy, 300);
        ViewCompat.postInvalidateOnAnimation(this);
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
            ViewCompat.postInvalidateOnAnimation(this);
            handleAutoScrollListener(currY);
        }

        if (mScroller.isFinished()) {
            onAutoScrollFinished();
        }
    }

    private void onAutoScrollFinished() {
        isScrolling = false;
        Logger.d(TAG, "mScroller.isFinished(), isScrolling = false. ");
        if (mOnItemScrollListener != null) {
            float[] firstVisibleItemInfo = ScrollComputeUtil.findFirstShownItem(mCurrentChildHeightsList, getScrollY());
            // 这里因为会存在一种情况，滑动停止时，它并没有完全滚动到位，就差那么几个像素
            int firstVisibleItemIndex = (int) firstVisibleItemInfo[1] > 0.5 ?
                    (int) firstVisibleItemInfo[0] : (int) firstVisibleItemInfo[0] + 1;
            if (firstVisibleItemIndex == mLastSelectedItemIndex) {
                // 防止itemIndex没有变化时多次回调
                return;
            }
            mLastSelectedItemIndex = firstVisibleItemIndex;
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
            float[] firstVisibleItemInfo = ScrollComputeUtil.findFirstShownItem(mCurrentChildHeightsList, currY);
            int firstVisibleItemIndex = (int) firstVisibleItemInfo[0];
            mOnItemScrollListener.onItemScrolled(getChildAt(firstVisibleItemIndex),
                    firstVisibleItemIndex, firstVisibleItemInfo[1]);
        }
    }

}