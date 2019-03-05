package com.leonxtp.library;

import android.view.View;

/**
 * Created by LeonXtp on 2019/3/5 上午10:26
 */
public interface OnItemScrollListener {

    /**
     * 滑动过程监听
     *
     * @param firstVisibleItem       第一个可见的子View
     * @param firstVisibleItemIndex  第一个可见的item下标
     * @param firstVisibleItemOffset 第一个可见item的可见高度占其自身高度百分比，范围[0, 1]
     */
    void onItemScrolled(View firstVisibleItem, int firstVisibleItemIndex, float firstVisibleItemOffset);

    /**
     * 当滑动结束，Item选中的回调
     *
     * @param selectedItem  首个可见的Item
     * @param selectedIndex 首个可见的item的位置
     */
    void onItemSelected(View selectedItem, int selectedIndex);

}
