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
     *                                             之前选中的item的index
     * @param lastSelectedItemId                   {@link View#onLayout(boolean, int, int, int, int)}中isChanged为true时，
     *                                             之前选中的item的id
     */
    static void handleSubItemChanged(VerticalPagerLayout verticalPagerLayout,
                                     boolean isKeepContentOnItemVisibilityChanged,
                                     List<Integer> lastChildrenHeights,
                                     List<Integer> currentChildrenHeights,
                                     int lastSelectedItemIndex,
                                     int lastSelectedItemId) {

        if (!isKeepContentOnItemVisibilityChanged) {
            return;
        }
        // 处理子View可见性变化
        // 找到设置为gone的item下标
        int dy = ComputeUtil.dyForUnShownHeightChange(verticalPagerLayout, currentChildrenHeights, lastChildrenHeights,
                lastSelectedItemIndex, lastSelectedItemId);

        if (dy != 0) {
            verticalPagerLayout.quickScrollBy(dy);
        }
    }

}
