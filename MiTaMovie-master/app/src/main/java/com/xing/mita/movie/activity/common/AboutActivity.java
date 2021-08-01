package com.xing.mita.movie.activity.common;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.xing.mita.movie.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2018/10/30
 * @Description
 */
public class AboutActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_about;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText("关于");
    }

    @OnClick(R.id.ifv_back)
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            default:
                break;
        }
    }
}
