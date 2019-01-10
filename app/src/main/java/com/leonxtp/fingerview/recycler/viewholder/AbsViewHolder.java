package com.leonxtp.fingerview.recycler.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by LeonXtp on 2018/12/28 下午11:04
 */
public abstract class AbsViewHolder extends RecyclerView.ViewHolder {

    public AbsViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBindViewHolder(int position);

}
