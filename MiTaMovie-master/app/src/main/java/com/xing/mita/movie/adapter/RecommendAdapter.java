package com.xing.mita.movie.adapter;

import android.app.Activity;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Recommend;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/10/9
 * @Description
 */
public class RecommendAdapter extends BaseMultiItemQuickAdapter<Recommend, BaseViewHolder> {

    public RecommendAdapter(List<Recommend> data) {
        super(data);
        addItemType(0, R.layout.adapter_recommend);
        addItemType(1, R.layout.adapter_recommend_title);
    }

    @Override
    protected void convert(BaseViewHolder helper, Recommend item) {
        switch (helper.getItemViewType()) {
            case 0:
                helper.setText(R.id.tv_type, item.getType());
                helper.setText(R.id.tv_name, item.getName());
                break;

            case 1:
                helper.setText(R.id.tv_title, item.getName());
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
