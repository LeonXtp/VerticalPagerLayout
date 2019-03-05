package com.leonxtp.fingerview;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leonxtp.fingerview.custom.MyVerticalPagerLayout;
import com.leonxtp.fingerview.custom.OnItemScrollListener;
import com.leonxtp.fingerview.util.Logger;

public class CustomActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CustomActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        initView();
    }

    private void initView() {
        MyVerticalPagerLayout verticalPagerLayout = findViewById(R.id.vertical_pager_layout);
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

        View viewTop = findViewById(R.id.view_top);
        View viewTop2 = findViewById(R.id.view_top2);
        View viewMiddle = findViewById(R.id.view_middle);
        View viewBottom = findViewById(R.id.view_bottom);

        viewTop.setOnClickListener(this);
        viewTop2.setOnClickListener(this);
        viewMiddle.setOnClickListener(this);
        viewBottom.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, String.valueOf(v.getId()), Toast.LENGTH_SHORT).show();
        if (v.getId() == R.id.view_top) {
            if (findViewById(R.id.view_top2).getVisibility() == View.VISIBLE) {
                findViewById(R.id.view_top2).setVisibility(View.GONE);
            } else {
                findViewById(R.id.view_top2).setVisibility(View.VISIBLE);
            }
        }
        if (v.getId() == R.id.view_top2) {
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    400);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
            textView.setText("Added TextView");
            textView.setBackgroundColor(Color.GRAY);
            ((ViewGroup) findViewById(R.id.vertical_pager_layout)).addView(textView, 2);
        }
        if (v.getId() == R.id.view_middle) {

            View scrollView = getLayoutInflater().inflate(R.layout.layout_scroll_content, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    450);
            scrollView.setLayoutParams(layoutParams);
            ((ViewGroup) findViewById(R.id.vertical_pager_layout)).addView(scrollView, 3);

        }
    }
}
