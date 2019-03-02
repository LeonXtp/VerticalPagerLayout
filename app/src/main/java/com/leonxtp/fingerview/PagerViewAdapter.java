package com.leonxtp.fingerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.leonxtp.fingerview.viewholder.AbsViewHolder;
import com.leonxtp.fingerview.viewholder.AdsViewHolder;
import com.leonxtp.fingerview.viewholder.IndicatorViewHolder;
import com.leonxtp.fingerview.viewholder.ManageViewHolder;
import com.leonxtp.fingerview.viewholder.PagerViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeonXtp on 2018/12/28 下午9:43
 */
public class PagerViewAdapter extends RecyclerView.Adapter<AbsViewHolder> {

    private static final int TYPE_MANAGE = 0;
    private static final int TYPE_ADVERTISEMENT = 1;
    private static final int TYPE_VIEWPAGER = 2;
    private static final int TYPE_INDICATOR = 3;

    private Context mContext;

    public PagerViewAdapter(Context context) {
        this.mContext = context;
    }

    private int itemCount = 4;

    public void removeSecondItem() {
        itemCount = 3;
//        notifyItemRemoved(2);
        notifyDataSetChanged();
    }

    public void resumeSecondItem() {
        itemCount = 4;
//        notifyItemInserted(2);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AbsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_MANAGE) {
            return new ManageViewHolder(ManageViewHolder.onCreateViewHolder(mContext, parent, viewType));
        } else if (viewType == TYPE_ADVERTISEMENT) {
            return new AdsViewHolder(AdsViewHolder.onCreateViewHolder(mContext, parent, viewType));
        } else if (viewType == TYPE_VIEWPAGER) {
            return new PagerViewHolder(PagerViewHolder.onCreateViewHolder(mContext, parent, viewType));
        } else {
            return new IndicatorViewHolder(IndicatorViewHolder.onCreateViewHolder(mContext, parent, viewType));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbsViewHolder holder, int position) {
        holder.onBindViewHolder(position);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemCount == 4) {
            return position;
        } else {
            switch (position) {
                case 0:
                    return TYPE_MANAGE;
                case 1:
                    return TYPE_VIEWPAGER;
                case 2:
                    return TYPE_INDICATOR;
                default:
                    return 0;
            }
        }
    }

}
