package com.leonxtp.fingerview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.leonxtp.library.Logger;
import com.leonxtp.library.OnItemScrollListener;
import com.leonxtp.library.VerticalPagerLayout;
import com.leonxtp.library.LockableScrollView;

/**
 * Created by LeonXtp on 2019/3/6 下午9:26
 */
public class VerticalPagerAdapter extends PagerAdapter implements View.OnClickListener {

    private static final String TAG = "VerticalPagerAdapter";

    private VerticalPagerLayout verticalPagerLayout;
    private Context context;

    public VerticalPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ViewGroup rootView = (ViewGroup) LayoutInflater.from(container.getContext()).inflate(
                R.layout.layout_vertical_view_pager, null);
        initView(rootView);
        container.addView(rootView);
        return rootView;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    private void initView(ViewGroup rootView) {

        LockableScrollView lockableScrollView = rootView.findViewById(R.id.lockableScrollView);

        verticalPagerLayout = rootView.findViewById(R.id.vertical_pager_layout);

        verticalPagerLayout.setCrossItemDragEnabled(true);
        verticalPagerLayout.setDefaultSelectedItem(3);
        verticalPagerLayout.setKeepContentOnItemVisibilityChanged(true);

        verticalPagerLayout.addOnScrollListener(new OnItemScrollListener() {
            @Override
            public void onItemScrolled(View firstVisibleItem, int firstVisibleItemIndex, float firstVisibleItemOffset) {
                Logger.w(TAG, "onItemScrolled, " + firstVisibleItem.hashCode() + ", " + firstVisibleItemIndex + ", " +
                        firstVisibleItemOffset);
                if (firstVisibleItemIndex == 0) {
//                    firstVisibleItem.setAlpha(firstVisibleItemOffset);
                }
            }

            @Override
            public void onItemSelected(View selectedItem, int selectedIndex) {
                Logger.w(TAG, "onItemSelected, " + selectedItem.hashCode() + ", " + selectedIndex);
                Toast.makeText(context, "onItemSelected, " + selectedIndex, Toast.LENGTH_SHORT).show();
            }
        });

        View viewTop1 = rootView.findViewById(R.id.id0);
        View viewTop = rootView.findViewById(R.id.id1);
        View viewTop2 = rootView.findViewById(R.id.id2);
        View viewMiddle = rootView.findViewById(R.id.id3);
        View viewBottom = rootView.findViewById(R.id.id4);

        viewTop1.setOnClickListener(this);
        viewTop.setOnClickListener(this);
        viewTop2.setOnClickListener(this);
        viewMiddle.setOnClickListener(this);
        viewBottom.setOnClickListener(this);

    }

    private HorizontalScrollView scrollView;
    private boolean isScrollViewAdded = false;

    @Override
    public void onClick(View v) {

        // 动态设置第二个View高度
        if (v.getId() == R.id.id0) {
            View setSizeView = verticalPagerLayout.findViewById(R.id.id1);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) setSizeView.getLayoutParams();
            if (layoutParams.height == 500) {
                layoutParams.height = 100;
            } else {
                layoutParams.height = 500;
            }
            setSizeView.setLayoutParams(layoutParams);
            Toast.makeText(context, "2nd view size changed", Toast.LENGTH_SHORT).show();
        }

        // 设置第三个view的可见行
        if (v.getId() == R.id.id1) {
            if (verticalPagerLayout.findViewById(R.id.id2).getVisibility() == View.VISIBLE) {
                verticalPagerLayout.findViewById(R.id.id2).setVisibility(View.GONE);
            } else {
                verticalPagerLayout.findViewById(R.id.id2).setVisibility(View.VISIBLE);
            }
            Toast.makeText(context, "3rd view visibility changed", Toast.LENGTH_SHORT).show();
        }

        // 动态增删ScrollView
        if (v.getId() == R.id.id2) {

            if (scrollView == null) {
                scrollView = (HorizontalScrollView) LayoutInflater.from(context).inflate(R.layout
                        .layout_scroll_content, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                        .MATCH_PARENT, 550);
                scrollView.setLayoutParams(layoutParams);
                if (Build.VERSION.SDK_INT >= 23) {
                    scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            verticalPagerLayout.setVerticalMoveEnabled(false);
                        }
                    });
                }
                ((ViewGroup) verticalPagerLayout.findViewById(R.id.vertical_pager_layout)).addView(scrollView, 3);
                isScrollViewAdded = true;
                Toast.makeText(context, "ScrollView added", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isScrollViewAdded) {
                verticalPagerLayout.removeView(scrollView);
                verticalPagerLayout.setVerticalMoveEnabled(true);
                Toast.makeText(context, "ScrollView removed", Toast.LENGTH_SHORT).show();
                isScrollViewAdded = false;
            } else {
                verticalPagerLayout.addView(scrollView, 3);
                Toast.makeText(context, "ScrollView added", Toast.LENGTH_SHORT).show();
                isScrollViewAdded = true;
            }

        }

        // 第一个View可见行
        if (v.getId() == R.id.id3) {
            if (verticalPagerLayout.findViewById(R.id.id0).getVisibility() == View.VISIBLE) {
                verticalPagerLayout.findViewById(R.id.id0).setVisibility(View.GONE);
            } else {
                verticalPagerLayout.findViewById(R.id.id0).setVisibility(View.VISIBLE);
            }
            Toast.makeText(context, "1st view visibility changed", Toast.LENGTH_SHORT).show();
        }

    }

}
