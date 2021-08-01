package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Share;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/11/3
 * @Description
 */
public class ShareAdapter extends BaseQuickAdapter<Share, BaseViewHolder> {

    public ShareAdapter(int layoutResId, @Nullable List<Share> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Share item) {
        helper.setText(R.id.tv_name, item.getName());
        helper.setText(R.id.ifv_share_icon, item.getIcon());
        helper.setTextColor(R.id.ifv_share_bg, Color.parseColor(item.getBgColor()));
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AutoSize.autoConvertDensityOfGlobal((Activity) holder.itemView.getContext());
        super.onBindViewHolder(holder, position);
    }
}
