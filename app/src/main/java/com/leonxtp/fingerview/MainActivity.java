package com.leonxtp.fingerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.Gravity;

import com.github.rubensousa.gravitysnaphelper.GravityPagerSnapHelper;
import com.leonxtp.fingerview.recycler.PagerViewAdapter;
import com.leonxtp.fingerview.util.DisplayUtil;

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
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {


                mRecyclerView.smoothScrollBy(0, (int) DisplayUtil.dpToPixel(350f, MainActivity.this));
                mRecyclerView.smoothScrollToPosition(2);

//                startActivity(new Intent(MainActivity.this, CustomActivity.class));

//                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(1);
//                if (holder != null) {
//                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams
// .MATCH_PARENT, 0));
//                    adapter.notifyItemChanged(1);
//                }

            }
        }, 300);

        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                mRecyclerView.smoothScrollBy(0, (int) DisplayUtil.dpToPixel(350f, MainActivity.this));
//                mRecyclerView.smoothScrollToPosition(2);

                RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(1);
                if (holder != null) {
//                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams
// .MATCH_PARENT, 1));
                    adapter.resumeSecondItem();
                }

            }
        }, 4000);

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
                Log.d("RecyclerView", "onScrolled: dy = " + dy + ", offsetY = " + offsetY);

                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
                if (holder != null) {
                    float alpha = 1 - offsetY / topViewHeight;
                    holder.itemView.setAlpha(alpha);
                }

            }
        });
    }


}
