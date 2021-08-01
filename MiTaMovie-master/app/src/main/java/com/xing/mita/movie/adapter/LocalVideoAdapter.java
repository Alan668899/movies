package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.LocalVideo;
import com.xing.mita.movie.utils.ImageUtils;

import java.util.List;

import me.jessyan.autosize.AutoSize;

public class LocalVideoAdapter extends BaseQuickAdapter<LocalVideo, BaseViewHolder> {

    public LocalVideoAdapter(int layoutResId, @Nullable List<LocalVideo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, LocalVideo item) {
        String name = item.getName();
        if (!TextUtils.isEmpty(name) && name.contains(".")) {
            name = name.substring(0, name.lastIndexOf("."));
        }
        helper.setText(R.id.tv_local_video_name, name);
        helper.setText(R.id.tv_local_video_size, item.getSize());
        helper.setText(R.id.tv_local_video_during, item.getDuring());
        ImageUtils.loadVideoThumb(helper.itemView.getContext(),
                (ImageView) helper.getView(R.id.iv_local_video_thumb),
                item.getPath());
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

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AutoSize.autoConvertDensityOfGlobal((Activity) holder.itemView.getContext());
        super.onBindViewHolder(holder, position);
    }
}
