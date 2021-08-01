package com.xing.mita.movie.activity.common;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.adapter.ShareAdapter;
import com.xing.mita.movie.entity.Share;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.FileUtils;
import com.xing.mita.movie.utils.ImageUtils;
import com.xing.mita.movie.utils.ShareUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2018/11/21
 * @Description 分享应用
 */
public class ShareAppActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.rv_share)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_share_tip)
    TextView mTvTip;
    @BindView(R.id.iv_qr)
    ImageView mIvQr;
    @BindView(R.id.cl_share)
    ConstraintLayout constraintLayout;

    private List<Share> list = new ArrayList<>();

    private Bitmap bmQR,
            bmLogo;
    private String imgSavePath;
    private boolean needSave;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_share_app;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText(R.string.share_app);

        SpannableString msp = new SpannableString(getString(R.string.share_app_tip));
        msp.setSpan(new SuperscriptSpan(), 8, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        msp.setSpan(new SubscriptSpan(), 13, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvTip.setText(msp);

        mIvQr.post(new Runnable() {
            @Override
            public void run() {
                ImageUtils.createQRCode(Constant.APK_DOWNLOAD_URL, mIvQr, bmQR, bmLogo, true);
            }
        });
        initShareRv();
    }

    @OnClick(R.id.ifv_back)
    public void click(View v) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(imgSavePath) || needSave) {
            return;
        }
        FileUtils.deleteFile(imgSavePath);
    }

    @Override
    protected void onDestroy() {
        if (bmQR != null && !bmQR.isRecycled()) {
            bmQR.recycle();
            bmQR = null;
        }
        if (bmLogo != null && !bmLogo.isRecycled()) {
            bmLogo.recycle();
            bmLogo = null;
        }
        System.gc();
        super.onDestroy();
    }

    private void initShareRv() {
        initShareData();
        ShareAdapter adapter = new ShareAdapter(R.layout.adapter_share, list);
        adapter.setOnItemClickListener(this);
        LinearLayoutManager llm = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(adapter);
    }

    private void initShareData() {
        Share share = new Share("朋友圈", R.string.icon_share_moments, "#7fbc60");
        list.add(share);
        share = new Share("微信好友", R.string.icon_share_wechat, "#29b24a");
        list.add(share);
        share = new Share("QQ", R.string.icon_share_qq, "#68c6f9");
        list.add(share);
        share = new Share("微博", R.string.icon_share_sina, "#db7346");
        list.add(share);
        share = new Share("保存", R.string.icon_share_save, "#448fed");
        list.add(share);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        bmQR = ImageUtils.convertViewToBitmap(constraintLayout);
        String fileName = "xmMovie.jpg";
        boolean saved = ImageUtils.saveImg(this, bmQR, fileName);
        String packageName, className = null;
        imgSavePath = Constant.DIR_IMAGE + fileName;

        switch (position) {
            case 0:
                ShareUtils.shareImagesToWeiXin(this, imgSavePath);
                return;

            case 1:
                packageName = "com.tencent.mm";
                className = "com.tencent.mm.ui.tools.ShareImgUI";
                break;

            case 2:
                packageName = "com.tencent.mobileqq";
                className = "com.tencent.mobileqq.activity.JumpActivity";
                break;

            case 3:
                packageName = "com.sina.weibo";
                break;

            default:
                if (saved) {
                    Toast.makeText(this, "图片已保存", Toast.LENGTH_SHORT).show();
                }
                needSave = true;
                return;
        }
        ShareUtils.share(this, packageName, className, imgSavePath);
    }
}
