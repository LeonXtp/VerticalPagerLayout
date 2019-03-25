package com.leonxtp.verticalpagerlayout;

import android.support.v4.view.ViewCompat;
import android.view.VelocityTracker;
import android.widget.Scroller;

import java.util.List;

/**
 * Created by LeonXtp on 2019/3/24 下午12:24
 */
class AutoScrollHelper {

    private static final String TAG = "VerticalPagerLayout";

    /**
     * 处理action_up
     */
    static void onActionUp(VerticalPagerLayout verticalPagerLayout,
                           VelocityTracker velocityTracker,
                           int maximumFlingVelocity,
                           boolean isCrossItemDragEnabled,
                           List<Integer> mCurrentChildrenHeights,
                           int mScrollableHeight,
                           int mLastSelectedItemIndex) {

        final float velocityY = velocityTracker.getYVelocity();
        final float velocityX = velocityTracker.getXVelocity();
        Logger.d(TAG, "velocityY=" + velocityY + ", velocityX=" + velocityX + ", maximumFlingVelocity=" +
                maximumFlingVelocity);
        if ((Math.abs(velocityY) > maximumFlingVelocity * 0.1f)
                && (Math.abs(velocityY) > Math.abs(velocityX) * 0.5f)) {
            Logger.d(TAG, "onFling...");
            onActionUp(verticalPagerLayout, isCrossItemDragEnabled, mCurrentChildrenHeights, mScrollableHeight,
                    mLastSelectedItemIndex, true, velocityY);
        } else {
            Logger.d(TAG, "not onFling...");
            onActionUp(verticalPagerLayout, isCrossItemDragEnabled, mCurrentChildrenHeights, mScrollableHeight,
                    mLastSelectedItemIndex, false, velocityY);
        }
    }

    private static void onActionUp(VerticalPagerLayout verticalPagerLayout,
                                   boolean isCrossItemDragEnabled,
                                   List<Integer> mCurrentChildrenHeights,
                                   int mScrollableHeight,
                                   int mLastSelectedItemIndex,
                                   boolean isFling,
                                   float velocityY) {
        int dy;
        if (!isCrossItemDragEnabled) {
            dy = ComputeUtil.computeNonCrossItemAutoScrollDy(verticalPagerLayout.getScrollY(), mCurrentChildrenHeights,
                    mScrollableHeight, mLastSelectedItemIndex, isFling, velocityY);
        } else {
            dy = ComputeUtil.computeCrossItemAutoScrollDy(verticalPagerLayout.getScrollY(), mCurrentChildrenHeights,
                    mScrollableHeight);
        }

        if (dy == 0) {
            verticalPagerLayout.onAutoScrollFinished();
        } else {
            Logger.d(TAG, "startScroll...dy = " + dy);
            verticalPagerLayout.smoothScrollBy(dy);
        }
    }

    static void computeScroll(VerticalPagerLayout verticalPagerLayout, Scroller scroller) {
        // 在mScroller没有startScroll的时候，它也会执行
        if (!scroller.computeScrollOffset()) {
            return;
        }

        final int oldY = verticalPagerLayout.getScrollY();
        final int currY = scroller.getCurrY();
        final int finalY = scroller.getFinalY();
//        Logger.d(TAG, "computeScroll: oldY = " + oldY + ", currY = " + currY + ", finalY = " + finalY +
//                ", isFinished = " + mScroller.isFinished());

        // 存在currY == finalY，但是isFinished = false，这时候还会继续调用1～2次computeScroll()的情况
        if (!scroller.isFinished() || oldY != currY || currY != finalY) {
            verticalPagerLayout.scrollTo(0, currY);
            ViewCompat.postInvalidateOnAnimation(verticalPagerLayout);
            verticalPagerLayout.handleAutoScrollListener(currY);
        }

        if (scroller.isFinished()) {
            verticalPagerLayout.onAutoScrollFinished();
        }
    }


}
