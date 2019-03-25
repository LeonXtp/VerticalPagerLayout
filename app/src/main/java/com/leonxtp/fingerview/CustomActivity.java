package com.leonxtp.fingerview;

import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.leonxtp.verticalpagerlayout.Logger;
import com.leonxtp.verticalpagerlayout.OnItemScrollListener;
import com.leonxtp.verticalpagerlayout.VerticalPagerLayout;

public class CustomActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CustomActivity";

    private ViewPager mViewPager;

    private VerticalPagerLayout verticalPagerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        mViewPager = findViewById(R.id.view_pager);
        VerticalPagerAdapter adapter = new VerticalPagerAdapter(this);
        mViewPager.setAdapter(adapter);
//        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("ScreenSize", "onResume " + getResources().getDisplayMetrics().heightPixels);
    }

    private void initView() {
        verticalPagerLayout = findViewById(R.id.vertical_pager_layout);

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
                Toast.makeText(CustomActivity.this, "onItemSelected, " + selectedIndex, Toast.LENGTH_SHORT).show();
            }
        });

        View viewTop = findViewById(R.id.id1);
        View viewTop2 = findViewById(R.id.id2);
        View viewMiddle = findViewById(R.id.id3);
        View viewBottom = findViewById(R.id.id4);

        viewTop.setOnClickListener(this);
        viewTop2.setOnClickListener(this);
        viewMiddle.setOnClickListener(this);
        viewBottom.setOnClickListener(this);

    }

    private HorizontalScrollView scrollView;
    private boolean isScrollViewAdded = false;

    @Override
    public void onClick(View v) {
        Toast.makeText(this, String.valueOf(v.getId()), Toast.LENGTH_SHORT).show();
        if (v.getId() == R.id.id0) {
            View setSizeView = findViewById(R.id.id1);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) setSizeView.getLayoutParams();
            if (layoutParams.height == 500) {
                layoutParams.height = 100;
            } else {
                layoutParams.height = 500;
            }
            setSizeView.setLayoutParams(layoutParams);
        }

        if (v.getId() == R.id.id1) {
            if (findViewById(R.id.id2).getVisibility() == View.VISIBLE) {
                findViewById(R.id.id2).setVisibility(View.GONE);
            } else {
                findViewById(R.id.id2).setVisibility(View.VISIBLE);
            }
        }

        if (v.getId() == R.id.id2) {

            if (scrollView == null) {
                scrollView = (HorizontalScrollView) getLayoutInflater().inflate(R.layout.layout_scroll_content, null);
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
                ((ViewGroup) findViewById(R.id.vertical_pager_layout)).addView(scrollView, 3);
                isScrollViewAdded = true;
                return;
            }

            if (isScrollViewAdded) {
                verticalPagerLayout.removeView(scrollView);
                isScrollViewAdded = false;
            } else {
                verticalPagerLayout.addView(scrollView, 3);
                isScrollViewAdded = true;
            }


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
