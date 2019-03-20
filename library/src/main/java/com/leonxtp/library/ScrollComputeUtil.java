package com.leonxtp.library;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by LeonXtp on 2019/3/5 下午3:11
 */
public class ScrollComputeUtil {

    private static final String TAG = "VerticalPagerLayout";

    /**
     * 初始化所有子View的高度
     * 注意：暂时不考虑各个子View存在margin的情况
     *
     * @param mChildHeightsList 存储子View高度列表的List
     * @param parent            父容器
     * @return 所有子View的高度总和
     */
    public static int initContentHeights(List<Integer> mChildHeightsList, LinearLayout parent) {
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
        } else if (scrollableHeight <= 0 && scrollY > 0) {
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
    public static float[] findFirstShownItem(List<Integer> childrenHeightList, int targetScrollY) {
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
     * @param scrollY          父容器的scrollY, 通过{@link ViewGroup#getScrollY()}取得
     * @param moveY            当次{@link android.view.MotionEvent}移动的垂直方向距离
     * @param scrollableHeight 父容器高度-所有子View的高度得到的一个可滚动的范围
     */
    public static boolean isMoveOverScroll(int scrollY, float moveY, int scrollableHeight) {
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
    public static int getDyForScrollToIndex(List<Integer> childrenHeightList, int targetItemIndex,
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
     * 找到在因滑出后看不见的，第一个设为了{@link View#GONE}的子View的高度
     */
    public static int findGoneViewWhenNotShown(List<Integer> childrenHeightList, List<Integer> lastChildrenHeightList) {
        if (childrenHeightList.size() != lastChildrenHeightList.size()) {
            return -1;
        }

        for (int i = 0; i < childrenHeightList.size(); i++) {
            if (childrenHeightList.get(i) != 0) {
                // 从上往下，只要遇到不等于0，就是visible的，就不用找了，没有
                break;
            }
            if (lastChildrenHeightList.get(i) == 0) {
                // 当前这个子view为gone，而如果之前也是gone，说明不是这个，继续
                continue;
            }
            // 当前遍历到的子View为gone，而之前不是gone，那么找到你了！
            return i;

        }
        return -1;
    }

    /**
     * 找到在因滑出后看不见的，第一个设为了{@link View#VISIBLE}的子View的高度
     */
    public static int findBecomeVisibleViewWhenNotShown(List<Integer> childrenHeightList,
                                                        List<Integer> lastChildrenHeightList) {
        if (childrenHeightList.size() != lastChildrenHeightList.size()) {
            return -1;
        }

        for (int i = 0; i < lastChildrenHeightList.size(); i++) {
            if (lastChildrenHeightList.get(i) != 0) {
                // 从上往下，只要遇到不等于0，就是visible的，就不用找了，没有
                break;
            }
            if (childrenHeightList.get(i) == 0) {
                // 当前这个子view为gone，而如果之前也是gone，说明不是这个，继续
                continue;
            }
            // 当前遍历到的子View为gone，而之前不是gone，那么找到你了！
            return i;

        }
        return -1;
    }

}
