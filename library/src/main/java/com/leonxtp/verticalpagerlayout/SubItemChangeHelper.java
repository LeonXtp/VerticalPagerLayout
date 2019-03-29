package com.leonxtp.verticalpagerlayout;

import android.view.View;

import java.util.List;

/**
 * Created by @author LeonXtp on 2019/3/24 上午11:21
 * <p>
 * 原则上，不在各种工具类中对工具使用者中的数据进行变更，以提升代码可读性
 */
class SubItemChangeHelper {

    /**
     * 每次layout完成后，如子View有变化，
     * 如之前滑出了可见区域的子View变为visible，或者之前不可见的子View显示在了之前第一个显示在可见区域的上方时，
     * 需要保存当前view的内容scrollY不变，否则将出现内容"跳动"
     *
     * @param verticalPagerLayout                  父容器
     * @param isKeepContentOnItemVisibilityChanged 内容区域是否保持不变
     * @param lastChildrenHeights                  {@link View#onLayout(boolean, int, int, int, int)}中isChanged为true时，
     *                                             保存的上次子item高度
     * @param currentChildrenHeights               当前的各子item高度
     * @param lastSelectedItemIndex                {@link View#onLayout(boolean, int, int, int, int)}中isChanged为true时，
     *                                             之前选中的item下标
     */
    static void handleSubItemChanged(VerticalPagerLayout verticalPagerLayout,
                                     boolean isKeepContentOnItemVisibilityChanged,
                                     List<Integer> lastChildrenHeights,
                                     List<Integer> currentChildrenHeights,
                                     int lastSelectedItemIndex) {

        if (!isKeepContentOnItemVisibilityChanged) {
            return;
        }
        // 处理子View可见性变化
        if (!handleUnShownViewGone(verticalPagerLayout, lastChildrenHeights, currentChildrenHeights,
                lastSelectedItemIndex)) {
            handleUnShownViewBecomeVisible(verticalPagerLayout, lastChildrenHeights, currentChildrenHeights);
        }
    }

    /**
     * 处理当滑出可见区域的子item变为不可见时的情形
     * 期望结果：view不出现上移
     */
    private static boolean handleUnShownViewGone(VerticalPagerLayout verticalPagerLayout,
                                                 List<Integer> lastChildrenHeights,
                                                 List<Integer> currentChildrenHeights,
                                                 int lastSelectedItemIndex) {
        // 找到设置为gone的item下标
        List<Integer> goneItemIndexes = ComputeUtil.findGoneViewWhenNotShown(currentChildrenHeights,
                lastChildrenHeights);
        int goneViewHeight = 0;
        // 确定变为gone的view是否在滑出可见区域的时候变的
        for (int i = 0; i < goneItemIndexes.size(); i++) {
            int index = goneItemIndexes.get(i);
            if (index != -1 && index < lastSelectedItemIndex) {
                goneViewHeight += lastChildrenHeights.get(index);
            }
        }

        if (goneViewHeight != 0) {
            verticalPagerLayout.quickScrollBy(-goneViewHeight);
            return true;
        }
        return false;
    }

    /**
     * 处理当滑出可见区域的子item变为可见时的情形
     * 期望结果：view不出现下移
     */
    private static void handleUnShownViewBecomeVisible(VerticalPagerLayout verticalPagerLayout,
                                                       List<Integer> lastChildrenHeights,
                                                       List<Integer> currentChildrenHeights) {
        int visibleItemIndex = ComputeUtil.findBecomeVisibleViewWhenNotShown(currentChildrenHeights,
                lastChildrenHeights);

        int visibleViewHeight = 0;
        // 确定变为gone的view是否在滑出可见区域的时候变的
        if (visibleItemIndex != -1) {
            visibleViewHeight = currentChildrenHeights.get(visibleItemIndex);
        }

        if (visibleViewHeight != 0) {
            verticalPagerLayout.quickScrollBy(visibleViewHeight);
        }
    }

}
