package com.leonxtp.verticalpagerlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
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
 */
public class VerticalPagerLayout extends LinearLayout {

    private final String TAG = "VerticalPagerLayout";

    private Scroller mScroller;
    private int mTouchSlop;
    /**
     * 是否在开启日志开关的情况下，是否打印move事件的日志
     */
    private boolean isLogForMoveEvents = false;
    /**
     * 本View尚未完成绘制时，外部可能就已经调用了其{@link #scrollToItem(int, boolean)}方法，
     * 此时是无效的，因此需要在layout完成时，继续完成外部在此前的一次调用
     */
    private boolean isFirstLayoutFinished = false;
    private int mIndex2ScrollBeforeLayout = -1;
    /**
     * 是否允许过度拖动，如，当前显示第0个item的时候，仍然继续下拉
     */
    private boolean isPullOverScrollEnabled = false;
    /**
     * 当前是否正在滑动，包括手指拖动以及自动滚动两个阶段。
     * 自然状态下，View在自动滚动过程中，再次点按屏幕，滚动过程将不会停下，
     * 需要在自动滚动过程中，继续处理ACTION_DOWN事件，拦截余下事件
     */
    private boolean isScrolling = false;
    /**
     * 是否可以手指滑动。控制思路：
     * 1。当外部调用setMoveEnabled()方法禁用本View垂直方向当触摸事件时，不拦截所有触摸事件
     * 2。当没有子View处理触摸事件，该事件又回传到本View时，不做任何处理
     * 则达到禁用触摸事件目的，同时又不会拦截子View。
     */
    private boolean isVerticalMoveEnabled = true;
    /**
     * 默认选中的item下标
     */
    private int mDefaultSelectedItemIndex = 0;
    /**
     * 当ACTION_POINTER_DOWN 或者 ACTION_POINTER_UP事件触发时，将忽略下一次Move事件，原因：
     * 当多指触碰时，抬起一只手指，则下一次{@link MotionEvent#ACTION_MOVE}时，
     * 位置getY()将直接跳到另一只手指的位置，出现滑动闪跳
     */
    private boolean isPointerActionTriggered = false;
    /**
     * 回弹时，是否可跨越子View拖动
     */
    private boolean isCrossItemDragEnabled = false;
    /**
     * 当已滑出可见区域之外的子item的从{@link View#VISIBLE}变为{@link View#GONE}时，
     * 或者当前第一个展示的item且上方的所有item都为{@link View#GONE}，其上方的item从{@link View#GONE}变为{@link View#VISIBLE}时
     * 内容区域是否保持不变
     */
    private boolean isKeepContentOnItemVisibilityChanged = true;
    /**
     * 所有子View各自的高度，每次onLayout时都会重新计算
     */
    private List<Integer> mCurrentChildrenHeights = new ArrayList<>();
    /**
     * 子View发生变化前所有子View各自的高度，
     * 变化包括：{@link View#getVisibility()}，子View高度变化，子View增加、删除等
     */
    private List<Integer> mLastChildrenHeights = new ArrayList<>();
    /**
     * 本View的肉眼可见高度，因其可能放在一个禁用了滚动效果的ScrollView内部
     */
    private int mVisibleHeight = 0;
    /**
     * 子view可滚动的高度，如：父view高度100，子View高度加起来200，那么可滚动的区域就是200-100=100
     * 当内容View高度不足父容器高度时，此高度=0
     */
    private int mScrollableHeight = 0;
    /**
     * 上次选中的itemIndex，目的：
     * 1. 防止当前显示的item没有变化时，一点微小的滑动也会回调
     * {@link OnItemScrollListener#onItemSelected(View, int)}
     * 2. 当不允许跨子item拖动时，用于记录上次的停留位置，当手指松开时计算回滚距离
     */
    private int mLastSelectedItemIndex = 0;

    private int mLastSelectedItemId = 0;

    private OnItemScrollListener mOnItemScrollListener = null;

    public VerticalPagerLayout(Context context) {
        super(context);
        init(context);
    }

    public VerticalPagerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    }

    //<editor-fold desc="public methods">

    /**
     * 设置是否允许过度拖动，如，当前显示第0个item的时候，仍然继续下拉，默认true
     */
    public void setPullOverScrollEnabled(boolean pullOverScrollEnabled) {
        this.isPullOverScrollEnabled = pullOverScrollEnabled;
    }

    /**
     * 设置是否可以跨越子View拖动，
     * 如当前显示第0个item，手指拖动至第3个（不管是否超过第3个的一半），松开时自动滑动至第1个，
     * 如当前显示第1个item，手指拖动至第0个，且不超过第0个的一半，则松开手指时自动回弹至第1个
     * 默认为true
     */
    public void setCrossItemDragEnabled(boolean isCrossItemMovable) {
        this.isCrossItemDragEnabled = isCrossItemMovable;
    }

    /**
     * 设置默认展示的item
     */
    public void setDefaultSelectedItem(int index) {
        this.mDefaultSelectedItemIndex = index;
    }

    /**
     * 见属性{@link VerticalPagerLayout#isKeepContentOnItemVisibilityChanged}注释
     */
    public void setKeepContentOnItemVisibilityChanged(boolean isKeepContentOnItemVisibilityChanged) {
        this.isKeepContentOnItemVisibilityChanged = isKeepContentOnItemVisibilityChanged;
    }

    /**
     * 当前是否增在滑动，包括手指拖动和自动滚动
     */
    public boolean isScrolling() {
        return this.isScrolling;
    }

    /**
     * 设置本View是否可以上下手指拖动
     */
    public void setVerticalMoveEnabled(boolean movable) {
        this.isVerticalMoveEnabled = movable;
    }

    public void addOnScrollListener(OnItemScrollListener listener) {
        this.mOnItemScrollListener = listener;
    }

    public void removeScrollListener() {
        this.mOnItemScrollListener = null;
    }

    /**
     * 获取上次停止滚动后停留在的item的index
     */
    public int getLastSelectedItemIndex() {
        return mLastSelectedItemIndex;
    }

    public void setLogging(boolean isLogging) {
        Logger.setLogging(isLogging);
    }

    /**
     * 设置在开启日志的情况下，是否打印move事件的日志
     */
    public void setLogMoveEvents(boolean isVerbose) {
        this.isLogForMoveEvents = isVerbose;
    }

    /**
     * 滚动到指定位置的item，当这个item不可见时（此时该item高度为0），将会展示其下面当一个item
     *
     * @param smoothly true：平滑地滑动 false：直接突然变过去
     */
    public void scrollToItem(int index, boolean smoothly) {
        int dy = ComputeUtil.getDyForScrollToIndex(mCurrentChildrenHeights, index, getScrollY(),
                mScrollableHeight);
        if (dy == 0) {
            if (!isFirstLayoutFinished) {
                // 本View尚未完成首次layout时调用scrollToItemIndex将会无效，在这里暂时先保存其意图，待layout完成时再执行
                mIndex2ScrollBeforeLayout = index;
            }
            return;
        }
        Logger.d(TAG, "scrollToItem, " + index + ", dy = " + dy);
        if (smoothly) {
            smoothScrollBy(dy);
        } else {
            quickScrollBy(dy);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getParent() instanceof ScrollView) {
            // 这里默认其父容器ScrollView已全部可见
            mVisibleHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!changed) {
            return;
        }
        initChildrenHeights();

        SubItemChangeHelper.handleSubItemChanged(this, isKeepContentOnItemVisibilityChanged,
                mLastChildrenHeights, mCurrentChildrenHeights, mLastSelectedItemIndex, mLastSelectedItemId);
        mLastChildrenHeights.clear();
        mLastChildrenHeights.addAll(mCurrentChildrenHeights);

        handleScrollToItemBeforeFirstLayout();

        isFirstLayoutFinished = true;

        // 此时选中的item可能也已经发生了变化
        onAutoScrollFinished();
    }

    /**
     * 计算各子View高度，以及可滚动范围
     */
    private void initChildrenHeights() {
        int contentHeight = ComputeUtil.initContentHeights(mCurrentChildrenHeights, this);
        int myVisibleHeight = mVisibleHeight == 0 ? getHeight() : mVisibleHeight;
        mScrollableHeight = contentHeight - myVisibleHeight > 0 ? contentHeight - myVisibleHeight : 0;
        Logger.w(TAG, "contentHeight = " + contentHeight + ", getHeight = " + getHeight());
        Logger.w(TAG, "VisibleHeight = " + myVisibleHeight + ", mScrollableHeight = " + mScrollableHeight);
    }

    private void handleScrollToItemBeforeFirstLayout() {
        if (isFirstLayoutFinished) {
            return;
        }
        if (mIndex2ScrollBeforeLayout >= 0) {
            // 在本View第一次layout之前调用了scrollToItem()方法，此时完成其"夙愿"，
            // 目的：如不记录之前的"夙愿"，那么第一次layout之前的调用将看不到效果
            scrollToItem(mIndex2ScrollBeforeLayout, false);
        } else {
            // 第一次layout，那么展示默认的item，
            // 目的：不设置将会在本次layout完成后，外部很快又调用了一次scrollToItem，感觉闪动
            scrollToItem(mDefaultSelectedItemIndex, false);
        }
    }

    void quickScrollBy(int dy) {
        scrollBy(0, dy);
        resetLastSelectedItem();
    }

    void smoothScrollBy(int dy) {
        mScroller.startScroll(0, getScrollY(), 0, dy, 300);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 用于在子item发生可见行变化时，重置设置当前可见的的首个item下标
     */
    private void resetLastSelectedItem() {
        float[] firstVisibleItemInfo = ComputeUtil.findFirstShownItem(mCurrentChildrenHeights, getScrollY());
        mLastSelectedItemIndex = getScrollY() != mScrollableHeight && firstVisibleItemInfo[1] < 0.1f ?
                (int) firstVisibleItemInfo[0] + 1 : (int) firstVisibleItemInfo[0];
        mLastSelectedItemId = getChildAt(mLastSelectedItemIndex).getId();
        Logger.d(TAG, "resetLastSelectedItem, mLastSelectedItemIndex = " + mLastSelectedItemIndex);
    }

    private float previousTouchX, previousTouchY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN ||
                action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL ||
                action == MotionEvent.ACTION_POINTER_UP) {
            ensureOutLockableScrollViewContentOffset(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 递归检查防止因外层不可滚动的ScrollView导致scrollY出现异常
     */
    private void ensureOutLockableScrollViewContentOffset(View child) {
        Object parent = child.getParent();
        if (parent instanceof LockableScrollView) {
            if (((LockableScrollView) parent).getScrollY() != 0) {
                ((LockableScrollView) parent).setScrollY(0);
            }
            ensureOutLockableScrollViewContentOffset((View) parent);
        } else if (parent instanceof View) {
            ensureOutLockableScrollViewContentOffset((View) parent);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Logger.w(TAG, "onInterceptTouchEvent ACTION_DOWN");
                intercept = isScrolling && isVerticalMoveEnabled;

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:

                float moveX = previousTouchX - ev.getX();
                float moveY = previousTouchY - ev.getY();
                // 避免点击时因为有一点小小的滑动导致被当成滑动事件，从而点击事件失效
                if (Math.abs(moveY) >= 1 && Math.abs(moveY) < mTouchSlop * 0.6f) {
                    break;
                }
                Logger.d(TAG, "onInterceptTouchEvent ACTION_MOVE， moveY: " + moveY, isLogForMoveEvents);

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                if (Math.abs(moveY) > Math.abs(moveX)) {

                    Logger.d(TAG, "onInterceptTouchEvent move vertically", isLogForMoveEvents);

                    // 判断是否允许OverScroll
                    boolean isPullDownOverScroll = getScrollY() <= 0 && moveY < 0;
                    boolean isPullUpOverScroll = getScrollY() >= mScrollableHeight && moveY > 0;
                    if (isPullOverScrollEnabled) {
                        intercept = true;
                    } else if (!isPullDownOverScroll && !isPullUpOverScroll) {
                        intercept = true;
                    } else {
                        Logger.w(TAG, "onInterceptTouchEvent move vertical overscroll, disabled", isLogForMoveEvents);
                    }

                } else {
                    Logger.w(TAG, "onInterceptTouchEvent move horizontally", isLogForMoveEvents);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Logger.w(TAG, "onInterceptTouchEvent ACTION_CANCEL");
            case MotionEvent.ACTION_UP:
                Logger.w(TAG, "onInterceptTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        Logger.w(TAG, "onInterceptTouchEvent: " + intercept);
        return intercept;
    }

    private VelocityTracker mVelocityTracker;
    private int mMaximumFlingVelocity;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        boolean handled = true;

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Logger.w(TAG, "onTouchEvent ACTION_DOWN");
                isPointerActionTriggered = false;

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                MoveScrollHelper.onActionDown(mScroller);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isPointerActionTriggered = true;
                Logger.w(TAG, "onTouchEvent ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Logger.w(TAG, "onTouchEvent ACTION_MOVE", isLogForMoveEvents);
                if (!isVerticalMoveEnabled) {
                    // 当外部调用setMoveEnabled()方法禁用本View当触摸事件时
                    handled = false;
                    Logger.w(TAG, "onTouchEvent isVerticalMoveEnabled false, break;", isLogForMoveEvents);
                    break;
                }

                float moveX = previousTouchX - ev.getX();
                float moveY = previousTouchY - ev.getY();
                Logger.w(TAG, "moveY: " + moveY, isLogForMoveEvents);

                previousTouchX = ev.getX();
                previousTouchY = ev.getY();

                if (Math.abs(moveY) <= Math.abs(moveX)) {
                    break;
                }

                Logger.d(TAG, "onTouchEvent move vertically", isLogForMoveEvents);
                isScrolling = true;
                Logger.d(TAG, "onTouchEvent moving isScrolling = true", isLogForMoveEvents);
                if (!isPointerActionTriggered) {

                    boolean isPullDownOverScroll = getScrollY() <= 0 && moveY < 0;
                    boolean isPullUpOverScroll = getScrollY() >= mScrollableHeight && moveY > 0;
                    if (isPullOverScrollEnabled) {
                        // 当上次触摸事件中触发了
                        MoveScrollHelper.onMoveVertical(this, mScrollableHeight, moveY);
                    } else if (!isPullDownOverScroll && !isPullUpOverScroll) {
                        // 当上次触摸事件中触发了
                        MoveScrollHelper.onMoveVertical(this, mScrollableHeight, moveY);
                    } else {
                        handled = false;
                        Logger.w(TAG, "onTouchEvent move vertical overscroll, disabled", isLogForMoveEvents);
                    }
                }
                isPointerActionTriggered = false;

                break;
            case MotionEvent.ACTION_CANCEL:
                Logger.w(TAG, "onTouchEvent ACTION_CANCEL");
            case MotionEvent.ACTION_UP:
                Logger.w(TAG, "onTouchEvent ACTION_UP");

                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                AutoScrollHelper.onActionUp(VerticalPagerLayout.this, velocityTracker, mMaximumFlingVelocity,
                        isCrossItemDragEnabled, mCurrentChildrenHeights, mScrollableHeight, mLastSelectedItemIndex);
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


    @Override
    public void computeScroll() {
        super.computeScroll();
        AutoScrollHelper.computeScroll(this, mScroller);
    }

    /**
     * 当手指松开，本View自动滚动完成，或者子item可见行发生变化
     */
    void onAutoScrollFinished() {
        Logger.d(TAG, "mScroller.isFinished(), isScrolling = false. ");
        isScrolling = false;

        float[] firstVisibleItemInfo = ComputeUtil.findFirstShownItem(mCurrentChildrenHeights, getScrollY());
        // 这里因为会存在一种情况，滑动停止时，它并没有完全滚动到位，就差那么几个像素
        // 但是又要排除是最后一个item的情况
        int firstVisibleItemIndex = getScrollY() != mScrollableHeight && firstVisibleItemInfo[1] < 0.1f ?
                (int) firstVisibleItemInfo[0] + 1 : (int) firstVisibleItemInfo[0];
        if (firstVisibleItemIndex == mLastSelectedItemIndex) {
            // 防止itemIndex没有变化时多次回调
            return;
        }
        mLastSelectedItemIndex = firstVisibleItemIndex;
        mLastSelectedItemId = getChildAt(mLastSelectedItemIndex).getId();
        Logger.d(TAG, "mLastSelectedItemIndex = " + mLastSelectedItemIndex);

        if (mOnItemScrollListener != null) {
            mOnItemScrollListener.onItemSelected(
                    getChildAt(firstVisibleItemIndex), (int) firstVisibleItemInfo[0]);
        }
    }

    /**
     * 处理手动滑动过程中的滑动监听
     *
     * @param moveY 本次手指滑动的y方向距离
     */
    void handleMoveListener(int moveY) {
        int targetScrollY = getScrollY() + moveY;
        handleAutoScrollListener(targetScrollY);
    }

    void handleAutoScrollListener(int currY) {
        if (currY < 0) {
            // 拉出顶部时的回弹，不处理
            return;
        }
        if (mScrollableHeight > 0 && currY > mScrollableHeight) {
            // 拉出底部的回弹，不处理
            return;
        }

        if (mOnItemScrollListener != null) {
            float[] firstVisibleItemInfo = ComputeUtil.findFirstShownItem(mCurrentChildrenHeights, currY);
            int firstVisibleItemIndex = (int) firstVisibleItemInfo[0];
            mOnItemScrollListener.onItemScrolled(getChildAt(firstVisibleItemIndex),
                    firstVisibleItemIndex, firstVisibleItemInfo[1]);
        }
    }

}