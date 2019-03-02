package com.leonxtp.fingerview.recycler.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leonxtp.fingerview.util.DisplayUtil;

/**
 * Created by LeonXtp on 2018/12/28 下午10:17
 */
public class AdsViewHolder extends AbsViewHolder {

    public AdsViewHolder(View itemView) {
        super(itemView);
    }

    public static View onCreateViewHolder(Context context, @NonNull ViewGroup parent, int position) {

        TextView textView = new TextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) DisplayUtil.dpToPixel(200, context));
        textView.setLayoutParams(layoutParams);
        textView.setText("AdsViewHolder");
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.parseColor("#009be6"));

        return textView;
    }

    @Override
    public void onBindViewHolder(int position) {

    }

}
