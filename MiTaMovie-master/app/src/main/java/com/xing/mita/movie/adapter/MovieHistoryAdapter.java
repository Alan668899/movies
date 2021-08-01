package com.xing.mita.movie.adapter;

import android.app.Activity;
import android.util.Log;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.MovieHistory;
import com.xing.mita.movie.utils.ImageUtils;

import java.util.List;

import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/11/1
 * @Description
 */
public class MovieHistoryAdapter extends BaseMultiItemQuickAdapter<MovieHistory, BaseViewHolder> {

    public MovieHistoryAdapter(List<MovieHistory> data) {
        super(data);
        addItemType(0, R.layout.adapter_movie_history);
        addItemType(2, R.layout.adapter_movie_history);
        addItemType(3, R.layout.adapter_movie_history);
        addItemType(1, R.layout.adapter_movie_history_header);
    }

    @Override
    protected void convert(BaseViewHolder helper, MovieHistory item) {
        int type = helper.getItemViewType();
        switch (helper.getItemViewType()) {
            case 0:
            case 2:
            case 3:
                int position = helper.getLayoutPosition();
                int size = getData().size();
                boolean lastItem = position == size - 1;
                helper.setVisible(R.id.view_half, lastItem);
                helper.setVisible(R.id.view, !lastItem);

                helper.setText(R.id.tv_name, item.getName());
                helper.setText(R.id.tv_episode, item.getEpisode());
                long during = item.getDuring();
                long progress = item.getProgress();
                String start;
                if (during == 0) {
                    start = "0%";
                } else {
                    start = (progress * 100 / during) + "%";
                }
                helper.setText(R.id.tv_progress, "观看至" + start);
                ImageUtils.loadImageWithGifThumbnail(helper.itemView.getContext(),
                        (ImageView) helper.getView(R.id.iv_thumb), item.getImage());
                if (type == 0) {
                    helper.setGone(R.id.ifv_check, false);
                } else if (type == 2) {
                    helper.setGone(R.id.ifv_check, true);
                } else {
                    helper.setGone(R.id.ifv_check, true);
                    helper.setText(R.id.ifv_check, R.string.icon_checked);
                }
                break;

            case 1:
                helper.setText(R.id.tv_name, item.getName());
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