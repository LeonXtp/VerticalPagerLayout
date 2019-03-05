package com.leonxtp.fingerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
//        MyScrollView myScrollView = findViewById(R.id.my_scroll_view);
//        myScrollView.setScrollingEnabled(false);

        MyVerticalPagerLayout verticalPagerLayout = findViewById(R.id.vertical_pager_layout);
        verticalPagerLayout.addOnScrollListener(new OnItemScrollListener() {
            @Override
            public void onItemScrolled(View firstVisibleItem, int firstVisibleItemIndex, float firstVisibleItemOffset) {
                Logger.w(TAG, "onItemScrolled, " + firstVisibleItem.hashCode() + ", " + firstVisibleItemIndex + ", " +
                        firstVisibleItemOffset);
            }

            @Override
            public void onItemSelected(View selectedItem, int selectedIndex) {
                Logger.w(TAG, "onItemSelected, " + selectedItem.hashCode() + ", " + selectedIndex);
            }
        });

        View viewTop = findViewById(R.id.view_top);
        View viewMiddle = findViewById(R.id.view_middle);
        View viewBottom = findViewById(R.id.view_bottom);

        viewTop.setOnClickListener(this);
        viewMiddle.setOnClickListener(this);
        viewBottom.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        Toast.makeText(this, String.valueOf(v.getId()), Toast.LENGTH_SHORT).show();
    }
}
