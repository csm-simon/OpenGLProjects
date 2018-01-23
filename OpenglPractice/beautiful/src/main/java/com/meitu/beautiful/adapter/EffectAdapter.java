package com.meitu.beautiful.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.meitu.beautiful.R;

import java.util.List;

/**
 * 滤镜效果的适配器
 * <p/>
 * Created by 周代亮 on 2018/1/14.
 */

public class EffectAdapter extends RecyclerView.Adapter {
    /**
     * 对应的数据集
     */
    private List<String> mData;
    /**
     * Item点击监听器
     */
    private OnItemClickListener mOnItemClickListener;
    /**
     * 当前被选中的位置
     */
    private int mSelectedPosition = 0;

    public EffectAdapter(@NonNull List<String> data) {
        this.mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_effect, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        VH vh = (VH) holder;
        vh.img.setImageResource(R.drawable.effect);
        vh.txt.setText(mData.get(position));

        vh.itemView.setBackgroundColor(position == mSelectedPosition ? 0xFFFFFF00 : 0);

        if (mOnItemClickListener != null) {
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedPosition = position;
                    mOnItemClickListener.onItemClickListener(position);
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private static class VH extends RecyclerView.ViewHolder {

        ImageView img;
        TextView txt;

        VH(View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.iv_item_img);
            txt = itemView.findViewById(R.id.tv_item_txt);
        }

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClickListener(int position);
    }
}
