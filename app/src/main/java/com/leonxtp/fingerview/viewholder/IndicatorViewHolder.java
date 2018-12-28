package com.leonxtp.fingerview.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leonxtp.fingerview.DisplayUtil;

/**
 * Created by LeonXtp on 2018/12/28 下午10:17
 */
public class IndicatorViewHolder extends AbsViewHolder {

    public IndicatorViewHolder(View itemView) {
        super(itemView);
    }

    public static View onCreateViewHolder(Context context, @NonNull ViewGroup parent, int position) {

        TextView textView = new TextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) DisplayUtil.dpToPixel(330, context));
        textView.setLayoutParams(layoutParams);
        textView.setText("IndicatorViewHolder");
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.parseColor("#123210"));

        return textView;
    }

    @Override
    public void onBindViewHolder(int position) {

    }

}
