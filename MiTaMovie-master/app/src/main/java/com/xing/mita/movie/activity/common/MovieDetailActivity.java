package com.xing.mita.movie.activity.common;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.florent37.glidepalette.BitmapPalette;
import com.github.florent37.glidepalette.GlidePalette;
import com.gyf.barlibrary.ImmersionBar;
import com.xing.mita.movie.R;
import com.xing.mita.movie.utils.ImageUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2019/1/22
 * @Description 影片详情页
 */
public class MovieDetailActivity extends BaseActivity {

    @BindView(R.id.iv_film_bg)
    ImageView mIvFilm;
    @BindView(R.id.view_bg)
    View mVBg;
    @BindView(R.id.view_interim)
    View mVInterim;
    @BindView(R.id.iv_film_thumb)
    ImageView mIvThumb;
    @BindView(R.id.tv_film_name)
    TextView mTvName;
    @BindView(R.id.tv_film_detail)
    TextView mTvDetail;
    @BindView(R.id.tv_film_intro)
    TextView mTvIntro;
    @BindView(R.id.v_bg)
    View mVContentBg;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_film_detail;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        final String imgUrl = getIntent().getStringExtra("imgUrl");
        Glide.with(this).load(imgUrl)
                .listener(GlidePalette.with(imgUrl)
                        .use(GlidePalette.Profile.VIBRANT)
                        .intoBackground(mVBg)
                        .intoBackground(mVContentBg)
                        .intoCallBack(new BitmapPalette.CallBack() {
                            @Override
                            public void onPaletteLoaded(@Nullable Palette palette) {
                                if (palette != null) {
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if (swatch != null) {
                                        int rgb = swatch.getRgb();
                                        int[] colors = {Color.parseColor("#00000000"),
                                                rgb};
                                        GradientDrawable drawable = getNeedDrawable(colors);
                                        mVInterim.setBackground(drawable);
                                        int fontColor = swatch.getBodyTextColor();
                                        int titleColor = swatch.getTitleTextColor();
                                        mTvName.setTextColor(titleColor);
                                        mTvDetail.setTextColor(fontColor);
                                        mTvIntro.setTextColor(fontColor);
                                    } else {
                                        setDefault();
                                    }
                                } else {
                                    setDefault();
                                }
                            }
                        })
                )
                .into(mIvFilm);
        ImageUtils.loadImageWithGifThumbnail(this, mIvThumb, imgUrl);
        String name = getIntent().getStringExtra("name");
        mTvName.setText(name);

        String detail = getIntent().getStringExtra("detail");
        mTvDetail.setText(detail);

        String intro = getIntent().getStringExtra("intro");
        mTvIntro.setText(intro);
    }

    @OnClick({R.id.ifv_back})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            default:
                break;
        }
    }

    /**
     * 初始化状态栏
     */
    @Override
    protected void initWhiteBar() {
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.flymeOSStatusBarFontColor(R.color.main_color)
                .statusBarDarkFont(false)
                .init();
    }

    /**
     * @param colors 渐变的颜色
     * @return
     */
    private GradientDrawable getNeedDrawable(int[] colors) {
        GradientDrawable drawable;
        drawable = new GradientDrawable();
        drawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        drawable.setColors(colors);

        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        return drawable;
    }

    /**
     * 颜色值获取失败，使用默认配色
     */
    private void setDefault() {
        //获取失败，使用默认配色
        int defaultColor = Color.parseColor("#ffe8d848");
        int[] colors = {Color.parseColor("#00000000"),
                defaultColor};
        GradientDrawable drawable = getNeedDrawable(colors);
        mVInterim.setBackground(drawable);
        mVBg.setBackgroundColor(defaultColor);
        mVContentBg.setBackgroundColor(defaultColor);
    }

}
