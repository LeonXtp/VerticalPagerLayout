package com.leonxtp.library;

import android.view.ViewGroup;

import java.util.List;

/**
 * Created by LeonXtp on 2019/3/5 下午3:11
 */
public class ScrollComputeUtil {

    /**
     * 计算手指松开后自动滚动的距离
     *
     * @param scrollY            父容器的scrollY, 通过{@link ViewGroup#getScrollY()}取得
     * @param childrenHeightList 父容器的所有子View的高度列表，必须考虑不可见的子View，其高度为0
     * @param scrollableHeight   父容器高度-所有子View的高度得到的一个可滚动的范围
     * @return 手指松开后自动滚动的距离
     */
    public static int computeAutoScrollDy(int scrollY, List<Integer> childrenHeightList, int scrollableHeight) {

        if (scrollY < 0) {
            // 下拉超出
            return -scrollY;
        } else if (scrollableHeight > 0 && scrollY > scrollableHeight) {
            // 上拉超出，且内容高度超过父容器高度
            return -(scrollY - scrollableHeight);
        } else if (scrollableHeight < 0 && scrollY > 0) {
            // 上拉超出，且内容高度小于父容器高度
            return -scrollY;
        } else {
            return computeAutoScrollDyInside(scrollY, childrenHeightList, scrollableHeight);
        }
    }

    /**
     * 计算手指松开后的自动滚动距离
     * 这里在看第一个可见View是否超过了一半的同时，也要看最后一个View是否已经到底了
     */
    private static int computeAutoScrollDyInside(int scrollY, List<Integer> childrenHeightList, int scrollableHeight) {
        int topY = 0;
        int bottomY = 0;
        for (int i = 0; i < childrenHeightList.size(); i++) {
            bottomY += childrenHeightList.get(i);
            if (scrollY <= bottomY) {
                break;
            }
            topY = bottomY;
        }

        if (scrollY - topY < (bottomY - topY) / 2) {
            // 不到一半，需要回弹
            return topY - scrollY;
        } else if (bottomY > scrollableHeight) {
            // 等于或超过一半， 并且最后一个子View已经到底，否则将进击过多，导致底部空白
            return scrollableHeight - scrollY;
        } else {
            // 等于或超过一半， 并且最后一个子View没有到底
            return bottomY - scrollY;
        }
    }

    /**
     * 根据目标scrollY找到所应该显示的第一个子View的index和显示的百分比
     *
     * @param targetScrollY 目标scrollY
     * @return float数组，第0个是第一个显示的子View的下标
     * 第1个是第一个子View显示高度占其自身高度的百分比，float类型，[0, 1]
     */
    public static float[] findFirstVisibleItem(List<Integer> childrenHeightList, int targetScrollY) {
        float itemIdex = 0;
        int itemHeight = 0;
        int bottomY = 0;
        for (int i = 0; i < childrenHeightList.size(); i++) {
            bottomY += childrenHeightList.get(i);
            if (targetScrollY < bottomY) {
                itemIdex = i;
                itemHeight = childrenHeightList.get(i);
                break;
            }
        }

        int visibleHeight = bottomY - targetScrollY;
        float visibleOffset = visibleHeight * 1.0f / itemHeight;
        return new float[]{itemIdex, visibleOffset};

    }

    /**
     * 判断当手指在拖动View滚动的时候，是否需要加阻尼
     *
     * @param scrollY           父容器的scrollY, 通过{@link ViewGroup#getScrollY()}取得
     * @param moveY             当次{@link android.view.MotionEvent}移动的垂直方向距离
     * @param mScrollableHeight 父容器高度-所有子View的高度得到的一个可滚动的范围
     */
    public static boolean isMoveOverScroll(int scrollY, float moveY, int mScrollableHeight) {
        if (scrollY <= 0 && moveY < 0) {
            // 下拉超出
            return true;
        } else if (mScrollableHeight >= 0 && scrollY >= mScrollableHeight && moveY > 0) {
            // 上拉超出
            return true;
        } else if (mScrollableHeight < 0) {
            // 子View高度都不够填充满父View，直接回弹
            return true;
        } else {
            return false;
        }
    }


}
