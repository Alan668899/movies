package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Episode;

import java.util.List;

import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;


/**
 * @author Mita
 * @date 2018/10/16
 * @Description 剧集adapter
 */
public class EpisodeAdapter extends BaseQuickAdapter<Episode, BaseViewHolder> {

    private Activity activity;

    public EpisodeAdapter(Activity activity, int layoutResId, @Nullable List<Episode> data) {
        super(layoutResId, data);
        this.activity = activity;
    }

    @Override
    protected void convert(BaseViewHolder helper, Episode item) {
        helper.setText(R.id.tv_name, item.getTitle());
        helper.setTextColor(R.id.tv_name, ContextCompat.getColor(helper.itemView.getContext(),
                item.isSelect() ? R.color.colorPrimary : R.color.color_66));
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
