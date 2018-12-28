package com.leonxtp.fingerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.github.rubensousa.gravitysnaphelper.GravityPagerSnapHelper;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        final PagerViewAdapter adapter = new PagerViewAdapter(this);
        mRecyclerView.setAdapter(adapter);
        new GravityPagerSnapHelper(Gravity.TOP).attachToRecyclerView(mRecyclerView);
        OverScrollDecoratorHelper.setUpOverScroll(mRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                mRecyclerView.smoothScrollBy(0, (int) DisplayUtil.dpToPixel(350f, MainActivity.this));
//                mRecyclerView.smoothScrollToPosition(2);

                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(1);
                if (holder != null) {
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 0));
                    adapter.notifyItemChanged(1);
                }

            }
        }, 2000);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private float topViewHeight = DisplayUtil.dpToPixel(150f, MainActivity.this);
            private int offsetY = 0;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                offsetY += dy;
                Logger.d("RecyclerView", "onScrolled: dy = " + dy + ", offsetY = " + offsetY);

                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
                if (holder != null) {
                    float alpha = 1 - offsetY / topViewHeight;
                    holder.itemView.setAlpha(alpha);
                }

            }
        });
    }


}
