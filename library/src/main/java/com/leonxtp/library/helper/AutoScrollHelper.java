package com.leonxtp.library.helper;

import android.support.v4.view.ViewCompat;
import android.widget.Scroller;

import com.leonxtp.library.Logger;
import com.leonxtp.library.VerticalPagerLayout;

import java.util.List;

/**
 * Created by LeonXtp on 2019/3/24 下午12:24
 */
public class AutoScrollHelper {

    private static final String TAG = "VerticalPagerLayout";

    public static void onActionUp(VerticalPagerLayout verticalPagerLayout,
                                  boolean isCrossItemDragEnabled,
                                  List<Integer> mCurrentChildrenHeights,
                                  int mScrollableHeight,
                                  int mLastSelectedItemIndex) {
        int dy;
        if (!isCrossItemDragEnabled) {
            dy = ComputeUtil.computeNonCrossItemAutoScrollDy(verticalPagerLayout.getScrollY(), mCurrentChildrenHeights,
                    mScrollableHeight, mLastSelectedItemIndex);
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

    public static void computeScroll(VerticalPagerLayout verticalPagerLayout, Scroller scroller) {
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
