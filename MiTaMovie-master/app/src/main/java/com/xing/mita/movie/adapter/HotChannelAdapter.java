package com.xing.mita.movie.adapter;

import android.app.Activity;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Channel;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2019/1/17
 * @Description
 */
public class HotChannelAdapter extends BaseMultiItemQuickAdapter<Channel, BaseViewHolder> {

    public HotChannelAdapter(List<Channel> data) {
        super(data);
        addItemType(0, R.layout.item_hot_channel_title);
        addItemType(1, R.layout.item_hot_channel_recommend);
        addItemType(2, R.layout.item_hot_channel);
    }

    @Override
    protected void convert(BaseViewHolder helper, Channel item) {
        switch (helper.getItemViewType()) {
            case 0:
                helper.setText(R.id.tv_channel_title, item.getTitle());
                break;

            case 1:
                helper.setText(R.id.tv_channel_name, item.getName());
                break;

            case 2:
                helper.setText(R.id.tv_channel_name, item.getName());
                break;

            default:
                break;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AutoSize.autoConvertDensityOfGlobal((Activity) holder.itemView.getContext());
        super.onBindViewHolder(holder, position);
    }
}
