package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Home;
import com.xing.mita.movie.utils.ImageUtils;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/10/11
 * @Description
 */
public class HomeAdapter extends BaseSectionQuickAdapter<Home, BaseViewHolder> {

    public HomeAdapter(int layoutResId, int sectionHeadResId, List<Home> data) {
        super(layoutResId, sectionHeadResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Home item) {
        helper.setText(R.id.tv_name, item.getContentName());
        String label = item.getContentLabel();
        helper.setText(R.id.tv_label, label);
        helper.setGone(R.id.tv_label, !TextUtils.isEmpty(label));
        ImageUtils.loadImageWithGifThumbnail(helper.getView(R.id.iv_img).getContext(),
                (ImageView) helper.getView(R.id.iv_img),
                item.getContentPic());
    }

    @Override
    protected void convertHead(BaseViewHolder helper, Home item) {
        helper.setText(R.id.tv_title, item.getTitle());
        helper.setText(R.id.ifv_img, item.getTitleIcon());
        String link = item.getTitleLink();
        boolean hasLink;
        if (TextUtils.isEmpty(link)) {
            hasLink = false;
        } else {
            hasLink = item.getTitleLink().length() > 5;
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
