package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Head;
import com.xing.mita.movie.utils.ImageUtils;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2019/1/9
 * @Description 头像adapter
 */
public class HeadIconAdapter extends BaseQuickAdapter<Head, BaseViewHolder> {

    public HeadIconAdapter(int layoutResId, @Nullable List<Head> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Head item) {
        ImageUtils.loadImage(
                (Activity) helper.itemView.getContext(),
                (ImageView) helper.getView(R.id.iv_img),
                item.getUrl()
        );
        if (item.isSelect()) {
            helper.setText(R.id.ifv_check, R.string.icon_checked);
        } else {
            helper.setText(R.id.ifv_check, "");
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        AutoSize.autoConvertDensityOfGlobal((Activity) holder.itemView.getContext());
        super.onBindViewHolder(holder, position);
    }
}
