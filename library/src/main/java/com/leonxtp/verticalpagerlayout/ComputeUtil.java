package com.leonxtp.verticalpagerlayout;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeonXtp on 2019/3/5 下午3:11
 * <p>
 * 原则上，不在各种工具类中对工具使用者中的数据进行变更，以提升代码可读性
 * 本类中mChildHeightsList例外，因其只在这里会发生改变
 */
class ComputeUtil {

    private static final String TAG = "VerticalPagerLayout";

    /**
     * 初始化所有子View的高度
     * 注意：暂时不考虑各个子View存在margin的情况
     *
     * @param mChildHeightsList 存储子View高度列表的List
     * @param parent            父容器
     * @return 所有子View的高度总和
     */
    static int initContentHeights(List<Integer> mChildHeightsList, LinearLayout parent) {
        mChildHeightsList.clear();
        int mContentHeight = 0;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int childHeight = 0;
            if (child.getVisibility() == View.VISIBLE) {
                int marginTop = ((LinearLayout.LayoutParams) child.getLayoutParams()).topMargin;
                int marginBottom = ((LinearLayout.LayoutParams) child.getLayoutParams()).bottomMargin;
                childHeight = child.getHeight() + marginTop + marginBottom;
            }
            Logger.w(TAG, "initContentHeights child " + i + " , Height:" + childHeight);
            mChildHeightsList.add(childHeight);
            mContentHeight += childHeight;
        }
        return mContentHeight;
    }

    /**
     * 计算可以跨item拖动时手指松开后自动滚动的距离
     *
     * @param scrollY            父容器的scrollY, 通过{@link ViewGroup#getScrollY()}取得
     * @param childrenHeightList 父容器的所有子View的高度列表，必须考虑不可见的子View，其高度为0
     * @param scrollableHeight   父容器高度-所有子View的高度得到的一个可滚动的范围
     * @return 手指松开后自动滚动的距离
     */
    static int computeCrossItemAutoScrollDy(int scrollY, List<Integer> childrenHeightList,
                                            int scrollableHeight) {

        if (scrollY < 0) {
            // 下拉超出
            return -scrollY;
        } else if (scrollableHeight > 0 && scrollY > scrollableHeight) {
            // 上拉超出，且容器内容高度超过父容器高度（即有自由滚动的余地）
            return -(scrollY - scrollableHeight);
        } else if (scrollableHeight <= 0 && scrollY > 0) {
            // 上拉超出，且容器内容高度小于等于父容器高度（即本没有自由滚动的余地）
            return -scrollY;
        } else {
            // 正常的内容滚动
            return computeAutoScrollDyInside(scrollY, childrenHeightList, scrollableHeight);
        }
    }

    /**
     * 计算手指松开后的自动滚动距离
     * 这里在看第一个可见View是否超过了一半的同时，也要看最后一个View是否已经到底了
     */
    private static int computeAutoScrollDyInside(int scrollY, List<Integer> childrenHeightList, int scrollableHeight) {

        int[] targetItemInfo = findNormalItemInfo(scrollY, childrenHeightList);
        int topY = targetItemInfo[0];
        int bottomY = targetItemInfo[1];

        if (scrollY - topY < (bottomY - topY) / 2) {
            // 不到一半，需要回弹
            return topY - scrollY;
        } else if (bottomY > scrollableHeight) {
            // 等于或超过一半， 需要进击，但是如果完全进击，那么最底下的item已经不够高度了，
            // 只能进击一部分，当最后的item显示完全就行
            return scrollableHeight - scrollY;
        } else {
            // 等于或超过一半， 并且最后一个子View没有到底
            return bottomY - scrollY;
        }
    }

    /**
     * 得到松开手指后将要自动回弹到的目标item的top、bottom信息
     */
    private static int[] findNormalItemInfo(int scrollY, List<Integer> childrenHeightList) {

        int[] result = new int[2];

        // 将要选中的目标item的top
        int topY = 0;
        // 将要选中的目标item的bottom
        int bottomY = 0;

        for (int i = 0; i < childrenHeightList.size(); i++) {
            bottomY += childrenHeightList.get(i);
            if (scrollY <= bottomY) {
                break;
            }
            topY = bottomY;
        }
        result[0] = topY;
        result[1] = bottomY;

        return result;
    }

    /**
     * 计算不可以跨item拖动都自动回弹距离
     *
     * @param scrollY               父容器的scrollY, 通过{@link ViewGroup#getScrollY()}取得
     * @param childrenHeightList    父容器的所有子View的高度列表，必须考虑不可见的子View，其高度为0
     * @param scrollableHeight      父容器高度-所有子View的高度得到的一个可滚动的范围
     * @param lastSelectedItemIndex 上次选中的item的下标
     * @return 手指松开后自动滚动的距离
     */
    static int computeNonCrossItemAutoScrollDy(int scrollY, List<Integer> childrenHeightList,
                                               int scrollableHeight, int lastSelectedItemIndex,
                                               boolean isFling, float velocityY) {

        if (lastSelectedItemIndex >= childrenHeightList.size()) {
            throw new IllegalArgumentException("astSelectedItemIndex >= childrenHeightList.size()");
        }

        // 内容高度不及容器高度时，都回弹至第一个item
        if (scrollableHeight <= 0) {
            return -scrollY;
        }

        int lastSelectedTopY = 0, lastSelectedBottomY = 0;
        int shownTopY = 0, shownBottomY = 0;

        int currShownIndex = 0;

        int result = 0;

        // 找到上次选中的item的top和bottom和index
        for (int i = 0; i < childrenHeightList.size(); i++) {
            if (i > lastSelectedItemIndex) {
                break;
            }
            lastSelectedTopY = lastSelectedBottomY;
            lastSelectedBottomY += childrenHeightList.get(i);
        }
        Logger.d("VerticalPagerLayout", "computeAutoNonCrossItemScrollDy, lastSelectedTopY=" + lastSelectedTopY
                + ", lastSelectedBottomY=" + lastSelectedBottomY);

        // 找到当前选中的item
        if (scrollY <= 0) {
            // 当前选中的item也不一定就是第0个，而是从0开始第一个visible的item
            shownTopY = 0;
            for (int i = 0; i < childrenHeightList.size(); i++) {
                if (childrenHeightList.get(i) > 0) {
                    shownBottomY = childrenHeightList.get(i);
                    currShownIndex = i;
                    break;
                }
            }
        } else {

            // 找到当前显示的item的top和bottom和index
            for (int i = 0; i < childrenHeightList.size(); i++) {
                shownTopY = shownBottomY;
                shownBottomY += childrenHeightList.get(i);
                currShownIndex = i;
                if (scrollY < shownBottomY) {
                    break;
                }
            }
        }

        Logger.d("VerticalPagerLayout", "computeAutoNonCrossItemScrollDy, currShownIndex=" + currShownIndex
                + ", shownTopY=" + shownTopY + ", shownBottomY=" + shownBottomY);

        // 当前的item在之前的item上方
        if (currShownIndex < lastSelectedItemIndex) {
            // 之前选中的item上方与当前选中的item之间有不可见的item
            int firstVisibleItemIndexAbove = findFirstVisibleItemAbove(lastSelectedItemIndex, childrenHeightList);
            int aboveItemHeight = childrenHeightList.get(firstVisibleItemIndexAbove);
            if ((currShownIndex == firstVisibleItemIndexAbove && lastSelectedTopY - scrollY < aboveItemHeight / 2) &&
                    !isFling) {
                // 当前显示的是紧挨着上一个item的，且少于1/2，则回弹至原来的item
                result = lastSelectedTopY - scrollY;
            } else {
                // 达到或超过一半，则显示其上方的一个item
                result = -(scrollY - (lastSelectedTopY - childrenHeightList.get(firstVisibleItemIndexAbove)));
            }

        }
        // 当前的item在之前item的下方，直接弹至其下方的item即可
        else if (currShownIndex > lastSelectedItemIndex) {
            if (shownBottomY > scrollableHeight && currShownIndex == lastSelectedItemIndex + 1) {
                // 等于或超过一半， 需要进击，但是如果完全进击，那么最底下的item已经不够高度了，
                // 只能进击一部分，当最后的item显示完全就行
                result = scrollableHeight - scrollY;
            } else {
                result = -(scrollY - lastSelectedBottomY);
            }
        }
        // 当前的item也正是之前的item
        else {
            if (isFling) {
                result = onFling(velocityY, scrollY, shownTopY, shownBottomY, scrollableHeight);
            } else {
                result = onNormalActionUp(scrollY, shownTopY, childrenHeightList, currShownIndex, shownBottomY,
                        scrollableHeight);
            }
        }

        return result;
    }

    private static int onFling(float velocityY, int scrollY, int shownTopY, int shownBottomY, int scrollableHeight) {

        int result = 0;

        if (velocityY >= 0) {
            // 下拉
            if (scrollY <= 0) {
                // 超出（首个item）
                result = -scrollY;
            } else {
                result = -(scrollY - shownTopY);
            }
        } else if (shownBottomY > scrollableHeight) {
            // 底部已经超出， 需要进击，但是如果完全进击，那么最底下的item已经不够高度了，
            // 只能进击一部分，当最后的item显示完全就行
            result = scrollableHeight - scrollY;
        } else {
            result = shownBottomY - scrollY;
        }

        return result;
    }

    private static int onNormalActionUp(int scrollY, int shownTopY, List<Integer> childrenHeightList, int
            currShownIndex,
                                        int shownBottomY, int scrollableHeight) {
        int result = 0;

        if ((scrollY - shownTopY < childrenHeightList.get(currShownIndex) / 2)) {
            // 不是fling时的不到一半才回弹
            result = -(scrollY - shownTopY);
        } else if (shownBottomY > scrollableHeight) {
            // 底部已经超出， 需要进击，但是如果完全进击，那么最底下的item已经不够高度了，
            // 只能进击一部分，当最后的item显示完全就行
            result = scrollableHeight - scrollY;
        } else {
            result = shownBottomY - scrollY;
        }

        return result;
    }


    /**
     * 找到当前item上方第一个visible（可见，高度不为0）的子View的下标
     *
     * @param index 当前的item下标
     */
    private static int findFirstVisibleItemAbove(int index, List<Integer> childrenHeights) {
        if (index == 0) {
            return -1;
        }
        for (int i = index - 1; i >= 0; i--) {
            if (childrenHeights.get(i) == 0) {
                continue;
            }
            return i;
        }
        return -1;
    }


    /**
     * 判断当手指在拖动View滚动的时候，是否需要加阻尼
     *
     * @param scrollY          父容器的scrollY, 通过{@link ViewGroup#getScrollY()}取得
     * @param moveY            当次{@link android.view.MotionEvent}移动的垂直方向距离
     * @param scrollableHeight 父容器高度-所有子View的高度得到的一个可滚动的范围
     */
    static boolean isMoveOverScroll(int scrollY, float moveY, int scrollableHeight) {
        if (scrollY <= 0 && moveY < 0) {
            // 下拉超出
            return true;
        } else if (scrollableHeight >= 0 && scrollY >= scrollableHeight && moveY > 0) {
            // 上拉超出
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取要滑动到指定的item所需要的距离偏移量，需要考虑最后一个子View已经到底到情况
     */
    static int getDyForScrollToIndex(List<Integer> childrenHeightList, int targetItemIndex,
                                     int scrollY, int scrollableHeight) {
        if (targetItemIndex >= childrenHeightList.size()) {
            return 0;
        }
        if (targetItemIndex == 0) {
            return -scrollY;
        }
        int dy = 0;
        for (int i = 0; i < targetItemIndex; i++) {
            dy += childrenHeightList.get(i);
        }
        if (dy > scrollableHeight) {
            dy = scrollableHeight;
        }
        return -(scrollY - dy);
    }

    /**
     * 找到第一个{@link View#getVisibility()}为{@link View#VISIBLE}的子View下标及该view的高度（包括上下margin）
     *
     * @return 如果未找到，返回-1
     */
    public static int[] findFirstVisibleItemIndex(LinearLayout parent) {
        int[] result = new int[]{-1, 0};
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                result[0] = i;
                int marginTop = ((LinearLayout.LayoutParams) child.getLayoutParams()).topMargin;
                int marginBottom = ((LinearLayout.LayoutParams) child.getLayoutParams()).bottomMargin;
                result[1] = child.getHeight() + marginTop + marginBottom;
                return result;
            }
        }
        return result;
    }

    /**
     * 根据目标scrollY找到所应该显示的第一个子View的index和显示的百分比
     *
     * @param targetScrollY 目标scrollY
     * @return float数组，第0个是第一个显示的子View的下标
     * 第1个是第一个子View显示高度占其自身高度的百分比，float类型，[0, 1]
     */
    static float[] findFirstShownItem(List<Integer> childrenHeightList, int targetScrollY) {
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

    static int dyForUnShownHeightChange(List<Integer> currChildrenHeightList, List<Integer> lastChildrenHeightList,
                                        int lastSelectedItemIndex) {

        if (lastSelectedItemIndex >= lastChildrenHeightList.size() ||
                lastSelectedItemIndex >= currChildrenHeightList.size()) {
            return 0;
        }

        int lastHeightAboveSelectedItem = 0, currHeigtAboveSelectedItem = 0;
        for (int i = 0; i < lastSelectedItemIndex; i++) {
            lastHeightAboveSelectedItem += lastChildrenHeightList.get(i);
            currHeigtAboveSelectedItem += currChildrenHeightList.get(i);
        }
        return currHeigtAboveSelectedItem - lastHeightAboveSelectedItem;
    }

}
