package com.xing.mita.movie.activity.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.xing.mita.movie.R;
import com.xing.mita.movie.dao.option.UserOption;
import com.xing.mita.movie.entity.User;
import com.xing.mita.movie.utils.AnimUtils;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.FileUtils;
import com.xing.mita.movie.view.IconFontView;
import com.xing.mita.movie.x5.X5WebView;

import java.util.Random;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Mita
 * @date 2019/1/9
 * @Description 建议反馈
 */
public class FeedbackActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.x5_webView)
    X5WebView webView;
    @BindView(R.id.pb_load)
    ProgressBar progressBar;
    @BindView(R.id.ifv_setting)
    IconFontView mIfvSetting;

    private ValueCallback<Uri> mUploadMessage;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_feedback;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTvTitle.setText(R.string.advice_feedback);

        initLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放webView资源
        webView.destroy();
        webView = null;
    }

    @Override
    public void onBackPressed() {
        if (backHtml()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Uri uri = intent.getData();
            if (uri == null) {
                return;
            }
            String sendFilePath = FileUtils.getPath(this, uri);
            mUploadMessage.onReceiveValue(Uri.parse(sendFilePath));
            mUploadMessage = null;
        } else {
            mUploadMessage.onReceiveValue(null);
        }
    }

    @OnClick({R.id.ifv_back, R.id.ifv_close, R.id.ifv_setting})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                if (backHtml()) {
                    return;
                }
                finish();
                break;

            case R.id.ifv_close:
                finish();
                break;

            case R.id.ifv_setting:
                AnimUtils.loadClickAnim(mIfvSetting);
                startNoIntent(FeedbackSetActivity.class);
                break;

            default:
                break;
        }
    }

    private boolean backHtml() {
        if (webView.canGoBack()) {
            //返回上个页面
            webView.goBack();
            return true;
        }
        return false;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initLoad() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        //网页加载进度
        webView.setWebChromeClient(new X5WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == progressBar.getMax()) {
                    //加载完网页进度条消失
                    progressBar.setVisibility(View.GONE);
                    mIfvSetting.setVisibility(View.VISIBLE);
                } else {
                    //设置进度值
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (TextUtils.isEmpty(title) || title.length() > 10) {
                    return;
                }
                mTvTitle.setText(title);
            }

        });
        //允许拉起微信
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                super.shouldOverrideUrlLoading(view, url);
                if (url == null) {
                    return false;
                }
                try {
                    if (url.startsWith("weixin://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
                view.loadUrl(url);
                return true;
            }
        });
        User user = getUserInfo();
        // 准备post参数
        String postData = "nickname=" + user.getNick()
                + "&avatar=" + user.getHeadIcon()
                + "&openid=" + user.getOpenId();
        webView.postUrl(Constant.TUCAO_URL, postData.getBytes());
    }

    /**
     * 获取用户信息
     *
     * @return User
     */
    private User getUserInfo() {
        User user = UserOption.getUser();
        if (user == null) {
            user = new User();
            // 用户的openid
            String openid = UUID.randomUUID().toString().replace("-", "");
            // 用户的nickname
            String nickname = "XM_" + randomDoubleDigit();
            // 用户的头像url
            String avatar = "https://tucao.qq.com/static/desktop/img/products/def-product-logo.png";
            user.setNick(nickname)
                    .setHeadIcon(avatar)
                    .setOpenId(openid);
            UserOption.saveUser(user);
        }
        return user;
    }

    @SuppressLint("DefaultLocale")
    private String randomDoubleDigit() {
        Random random = new Random();
        int ends = random.nextInt(1000);
        //如果不足两位，前面补0
        return String.format("%03d", ends);
    }

    public class X5WebChromeClient extends WebChromeClient {

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback,
                                         FileChooserParams fileChooserParams) {
            return super.onShowFileChooser(webView, valueCallback, fileChooserParams);
        }

        /**
         * Android > 4.1.1 调用这个方法
         */
        @Override
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType,
                                    String capture) {
            mUploadMessage = uploadMsg;
            choosePicture();
        }

        private void choosePicture() {
            FileUtils.showPicVideoChooser(FeedbackActivity.this);
        }
    }
}
