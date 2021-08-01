package com.xing.mita.movie.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.entity.Connection;
import com.xing.mita.movie.utils.ImageUtils;

import java.util.List;

/**
 * @author Mita
 * @date 2018/10/31
 * @Description
 */
public class CollectionAdapter extends BaseItemDraggableAdapter<Connection, BaseViewHolder> {

    public CollectionAdapter(int layoutResId, List<Connection> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Connection item) {
        ImageUtils.loadCornerImage(helper.itemView.getContext(),
                (ImageView) helper.getView(R.id.iv_thumb), item.getImage(),60);
    }
}
