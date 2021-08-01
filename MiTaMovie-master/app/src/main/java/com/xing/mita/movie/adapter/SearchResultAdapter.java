package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Video;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.ImageUtils;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/10/25
 * @Description
 */
public class SearchResultAdapter extends BaseMultiItemQuickAdapter<Video, BaseViewHolder> {

    public SearchResultAdapter(List<Video> data) {
        super(data);
        addItemType(0, R.layout.adapter_search_result_title);
        addItemType(1, R.layout.adapter_kk3_video);
        addItemType(2, R.layout.adapter_search_result);
    }

    @Override
    protected void convert(BaseViewHolder helper, Video item) {
        int type = helper.getItemViewType();
        switch (type) {
            case 0:
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
                break;

            case 1:
                helper.setText(R.id.tv_movie_name, item.getName());
                helper.setText(R.id.tv_movie_date, item.getScore());
                break;

            case 2:
                helper.setText(R.id.tv_movie_name, item.getName());
                helper.setText(R.id.tv_detail, item.getActor());
                ImageUtils.loadImageWithGifThumbnail(helper.getView(R.id.iv_image).getContext(),
                        (ImageView) helper.getView(R.id.iv_image),
                        item.getPicture());
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
