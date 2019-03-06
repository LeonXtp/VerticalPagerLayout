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

        verticalPagerLayout = rootView.findViewById(R.id.vertical_pager_layout);
        verticalPagerLayout.addOnScrollListener(new OnItemScrollListener() {
            @Override
            public void onItemScrolled(View firstVisibleItem, int firstVisibleItemIndex, float firstVisibleItemOffset) {
                Logger.w(TAG, "onItemScrolled, " + firstVisibleItem.hashCode() + ", " + firstVisibleItemIndex + ", " +
                        firstVisibleItemOffset);
                if (firstVisibleItemIndex == 0) {
                    firstVisibleItem.setAlpha(firstVisibleItemOffset);
                }
            }

            @Override
            public void onItemSelected(View selectedItem, int selectedIndex) {
                Logger.w(TAG, "onItemSelected, " + selectedItem.hashCode() + ", " + selectedIndex);
                Toast.makeText(context, "onItemSelected, " + selectedIndex, Toast.LENGTH_SHORT).show();
            }
        });

        View viewTop = rootView.findViewById(R.id.view_top);
        View viewTop2 = rootView.findViewById(R.id.view_top2);
        View viewMiddle = rootView.findViewById(R.id.view_middle);
        View viewBottom = rootView.findViewById(R.id.view_bottom);

        viewTop.setOnClickListener(this);
        viewTop2.setOnClickListener(this);
        viewMiddle.setOnClickListener(this);
        viewBottom.setOnClickListener(this);

    }

    private HorizontalScrollView scrollView;

    @Override
    public void onClick(View v) {
        Toast.makeText(context, String.valueOf(v.getId()), Toast.LENGTH_SHORT).show();
        if (v.getId() == R.id.view_top) {
//            if (findViewById(R.id.view_top2).getVisibility() == View.VISIBLE) {
//                findViewById(R.id.view_top2).setVisibility(View.GONE);
//            } else {
//                findViewById(R.id.view_top2).setVisibility(View.VISIBLE);
//            }
            View setSizeView = verticalPagerLayout.findViewById(R.id.viewSetSize);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) setSizeView.getLayoutParams();
            layoutParams.height = 900;
            setSizeView.setLayoutParams(layoutParams);
        }
        if (v.getId() == R.id.view_top2) {

            verticalPagerLayout.setMoveEnabled(true);

//            TextView textView = new TextView(this);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams
// .MATCH_PARENT,
//                    400);
//            textView.setLayoutParams(layoutParams);
//            textView.setGravity(Gravity.CENTER);
//            textView.setText("Added TextView");
//            textView.setBackgroundColor(Color.GRAY);
//            ((ViewGroup) findViewById(R.id.vertical_pager_layout)).addView(textView, 2);

        }
        if (v.getId() == R.id.view_middle) {

            if (scrollView != null) {
                verticalPagerLayout.setMoveEnabled(true);
                return;
            }
            scrollView = (HorizontalScrollView) LayoutInflater.from(context).inflate(R.layout.layout_scroll_content,
                    null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    550);
            scrollView.setLayoutParams(layoutParams);
            ((ViewGroup) verticalPagerLayout.findViewById(R.id.vertical_pager_layout)).addView(scrollView, 3);

            if (Build.VERSION.SDK_INT >= 23) {
                scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                        verticalPagerLayout.setMoveEnabled(false);

                    }
                });
            }

        }
    }

}
