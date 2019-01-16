package com.leonxtp.fingerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.leonxtp.fingerview.custom.MyScrollView;
import com.leonxtp.fingerview.custom.MyVerticalPagerLayout;
import com.leonxtp.fingerview.util.DisplayUtil;

public class CustomActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        initView();
    }

    private void initView() {
        MyScrollView myScrollView = findViewById(R.id.my_scroll_view);
        myScrollView.setScrollingEnabled(false);

        MyVerticalPagerLayout verticalPagerLayout = findViewById(R.id.vertical_pager_layout);

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
