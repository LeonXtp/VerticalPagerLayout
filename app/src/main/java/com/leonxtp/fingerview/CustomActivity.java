package com.leonxtp.fingerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.leonxtp.fingerview.custom.MyScrollView;
import com.leonxtp.fingerview.util.DisplayUtil;

public class CustomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        initView();
    }

    private void initView() {
        MyScrollView myScrollView = findViewById(R.id.my_scroll_view);
        myScrollView.setScrollingEnabled(false);
    }


}
