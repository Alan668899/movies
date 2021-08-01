package com.xing.mita.movie.adapter.kk3;

import android.app.Activity;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Video;
import com.xing.mita.movie.utils.Constant;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/12/3
 * @Description
 */
public class Kk3VideoAdapter extends BaseSectionQuickAdapter<Video, BaseViewHolder> {

    public Kk3VideoAdapter(int layoutResId, int sectionHeadResId, List<Video> data) {
        super(layoutResId, sectionHeadResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Video item) {
        helper.setText(R.id.tv_movie_name, item.getName());
        helper.setText(R.id.tv_movie_date, item.getScore());
    }

    @Override
    protected void convertHead(BaseViewHolder helper, Video item) {
        helper.setText(R.id.tv_title, item.getTitle());
        helper.setImageResource(R.id.iv_img, item.getTitleImg());
        String link = item.getSource();
        boolean hasLink = false;
        if (TextUtils.equals(link, Constant.SOURCE_GQZY)) {
            hasLink = true;
        }
        helper.setGone(R.id.tv_more, hasLink);
        helper.setGone(R.id.ifv_next, hasLink);
        helper.itemView.setClickable(hasLink);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AutoSize.autoConvertDensityOfGlobal((Activity) holder.itemView.getContext());
        super.onBindViewHolder(holder, position);
    }
}
