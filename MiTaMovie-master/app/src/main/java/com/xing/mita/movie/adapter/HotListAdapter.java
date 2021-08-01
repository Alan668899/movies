package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.HotList;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/10/25
 * @Description
 */
public class HotListAdapter extends BaseQuickAdapter<HotList, BaseViewHolder> {

    public HotListAdapter(int layoutResId, @Nullable List<HotList> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, HotList item) {
        helper.setText(R.id.tv_hot_name, item.getName());
        int typeRes = R.string.icon_movie;
        switch (item.getType()) {
            case "电视剧":
                typeRes = R.string.icon_teleplay;
                break;

            case "动漫":
                typeRes = R.string.icon_cartoon;
                break;

            default:
                break;
        }
        helper.setText(R.id.ifv_hot, typeRes);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AutoSize.autoConvertDensityOfGlobal((Activity) holder.itemView.getContext());
        super.onBindViewHolder(holder, position);
    }
}