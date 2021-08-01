package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Video;
import com.xing.mita.movie.utils.ImageUtils;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/10/12
 * @Description
 */
public class VideoAdapter extends BaseSectionQuickAdapter<Video, BaseViewHolder> {

    public VideoAdapter(int layoutResId, int sectionHeadResId, List<Video> data) {
        super(layoutResId, sectionHeadResId, data);
    }

    @Override
    protected void convertHead(BaseViewHolder helper, Video item) {
        helper.setText(R.id.tv_title, item.getTitle());
    }

    @Override
    protected void convert(BaseViewHolder helper, Video item) {
        helper.setText(R.id.tv_name, item.getName());
        String actor = item.getActor();
        if (!TextUtils.isEmpty(actor) && actor.contains(":")) {
            actor = actor.substring(actor.indexOf(":") + 1);
        }
        helper.setText(R.id.tv_actor, actor);
        String label = item.getTitle();
        helper.setText(R.id.tv_label, label);
        helper.setGone(R.id.tv_label, !TextUtils.isEmpty(label));
        helper.setText(R.id.tv_score, item.getScore());
        ImageUtils.loadImageWithGifThumbnail(helper.getView(R.id.iv_img).getContext(),
                (ImageView) helper.getView(R.id.iv_img),
                item.getPicture());
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AutoSize.autoConvertDensityOfGlobal((Activity) holder.itemView.getContext());
        super.onBindViewHolder(holder, position);
    }
}
