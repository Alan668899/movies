package com.xing.mita.movie.upnp.ui;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xing.mita.movie.R;
import com.xing.mita.movie.upnp.entity.ClingDevice;

import java.util.List;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/28 15:50
 */

public class DevicesAdapter extends BaseQuickAdapter<ClingDevice, BaseViewHolder> {

    public DevicesAdapter(int layoutResId, @Nullable List<ClingDevice> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ClingDevice item) {
        helper.setText(R.id.tv_device_name, item.getDevice().getDetails().getFriendlyName());

    }
}