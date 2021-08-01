package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Parade;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2019/1/24
 * @Description
 */
public class ParadeAdapter extends BaseQuickAdapter<Parade, BaseViewHolder> {

    private Activity activity;

    public ParadeAdapter(Activity activity, int layoutResId, @Nullable List<Parade> data) {
        super(layoutResId, data);
        this.activity = activity;
    }

    @Override
    protected void convert(BaseViewHolder helper, Parade item) {
        helper.setText(R.id.tv_time, item.getTime());
        helper.setText(R.id.tv_parade, item.getContent());
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AutoSize.autoConvertDensityOfGlobal((Activity) holder.itemView.getContext());
        super.onBindViewHolder(holder, position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AutoSize.autoConvertDensityOfGlobal(activity);
        return super.onCreateViewHolder(parent, viewType);
    }
}
