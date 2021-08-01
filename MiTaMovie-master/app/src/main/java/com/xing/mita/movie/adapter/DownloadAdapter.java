package com.xing.mita.movie.adapter;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Download;
import com.xing.mita.movie.utils.ImageUtils;

import java.util.List;

/**
 * @author Mita
 * @date 2019/2/19
 * @Description
 */
public class DownloadAdapter extends BaseQuickAdapter<Download, BaseViewHolder> {

    public DownloadAdapter(int layoutResId, @Nullable List<Download> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Download item) {
        ImageUtils.loadVideoThumb(helper.itemView.getContext(),
                (ImageView) helper.getView(R.id.iv_thumb), item.getThumb());
        helper.setText(R.id.tv_video_name, item.getName());
        helper.setText(R.id.tv_episode, item.getEpisode());
        helper.setText(R.id.tv_down_speed, item.getSpeed());
        String cacheSize = item.getCacheSize();
        if (!TextUtils.isEmpty(cacheSize)) {
            helper.setText(R.id.tv_cache_size, item.getCacheSize());
        }
        ((ProgressBar) helper.getView(R.id.pb_cache)).setProgress(item.getProgress());
        int type = item.getStatus();
        if (type == 0) {
            helper.setGone(R.id.ifv_check, false);
        } else if (type == 1) {
            helper.setGone(R.id.ifv_check, true);
            helper.setText(R.id.ifv_check, "");
        } else {
            helper.setGone(R.id.ifv_check, true);
            helper.setText(R.id.ifv_check, R.string.icon_checked);
        }
    }
}