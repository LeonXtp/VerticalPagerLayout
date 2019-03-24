package com.leonxtp.library;

import android.widget.Scroller;

/**
 * Created by LeonXtp on 2019/3/24 上午11:21
 */
public class MoveScrollHelper {

    private static final String TAG = "VerticalPagerLayout";

    /**
     * View滑动超出本身的滑动范围时，弹性效果的阻尼系数
     */
    private static final float OVER_SCROLL_DAMPING_COEFFICIENT = 0.2f;

    public static void onActionDown(Scroller scroller) {
        if (!scroller.isFinished()) {
            Logger.d(TAG, "onActionDown, mScroller.abortAnimation()");
            scroller.abortAnimation();
        }
    }

    /**
     * 单次MotionEvent事件中，手指垂直滑动时的处理
     *
     * @param moveY 手指滑动的距离，向上为"+"， 向下为"-"。
     */
    public static void onMoveVertical(VerticalPagerLayout verticalPagerLayout, int scrollableHeight, float moveY) {
        int scrollY = verticalPagerLayout.getScrollY();
        if (ComputeUtil.isMoveOverScroll(scrollY, moveY, scrollableHeight)) {
            // 需要加阻尼
            handleMoveOverScroll(verticalPagerLayout, moveY);
        } else {
            handleMoveInside(verticalPagerLayout, moveY);
        }
    }

    private static void handleMoveOverScroll(VerticalPagerLayout verticalPagerLayout, float moveY) {
        verticalPagerLayout.scrollBy(0, (int) (moveY * OVER_SCROLL_DAMPING_COEFFICIENT));
    }

    private static void handleMoveInside(VerticalPagerLayout verticalPagerLayout, float moveY) {
        verticalPagerLayout.scrollBy(0, (int) moveY);
        // 调完scrollBy马上再重新getScrollY()，将不会立即见效，因此只能通过本次scrollY+moveY的方式回调滑动状态
        verticalPagerLayout.handleMoveListener((int) moveY);
    }


}
