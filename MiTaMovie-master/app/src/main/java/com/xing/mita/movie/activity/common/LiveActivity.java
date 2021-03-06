package com.xing.mita.movie.activity.common;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.popup.QMUIListPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.xing.mita.movie.R;
import com.xing.mita.movie.adapter.ParadeAdapter;
import com.xing.mita.movie.dao.option.ProgramOption;
import com.xing.mita.movie.entity.Hdp;
import com.xing.mita.movie.entity.Parade;
import com.xing.mita.movie.entity.Program;
import com.xing.mita.movie.player.LiveVideoPlayer;
import com.xing.mita.movie.player.PlayerUtils;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.AnimUtils;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.utils.ScreenUtils;
import com.xing.mita.movie.utils.SpUtils;
import com.xing.mita.movie.view.OwnWebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2019/1/17
 * @Description ??????
 */
public class LiveActivity extends BaseActivity {

    @BindView(R.id.web_program)
    OwnWebView webProgram;
    @BindView(R.id.video_player)
    LiveVideoPlayer videoPlayer;
    @BindView(R.id.rv_program)
    RecyclerView mRvProgram;
    @BindView(R.id.tv_title)
    TextView mTvName;
    @BindView(R.id.iv_refresh)
    ImageView mIvRefresh;
    @BindView(R.id.tv_definition)
    TextView mTvDefinition;

    private OrientationUtils orientationUtils;

    private ParadeAdapter adapter;

    private List<Hdp> list = new ArrayList<>();
    private Hdp hdp;
    private int notifyItemCount = 3;
    private int currentHdp;
    private String name;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_live;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        initBlackBar();
        initRv();
        //??????????????????
        initPlayer();
        //?????????WebView
        initProgramWeb();
        getProgram();
    }

    @Override
    protected void onStart() {
        super.onStart();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //????????????
        videoPlayer.setVideoAllCallBack(null);
        GSYVideoManager.releaseAllVideos();
        //??????webView??????
        webProgram.destroy();
        webProgram = null;
    }

    @Override
    public void onBackPressed() {
        //????????????????????????
        boolean isLock = ((LiveVideoPlayer) videoPlayer.getCurrentPlayer()).isLock();
        if (isLock) {
            return;
        }
        //?????????????????????
        if (ScreenUtils.getScreenIsLand(this)) {
            videoPlayer.getCurrentPlayer().getFullscreenButton().performClick();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //????????????????????????
        videoPlayer.onConfigurationChanged(this, newConfig, orientationUtils,
                true, true);
        if (!ScreenUtils.getScreenIsLand(this)) {
            AutoSize.autoConvertDensityOfGlobal(this);
        }
    }

    @OnClick({R.id.ifv_back, R.id.iv_refresh, R.id.tv_definition})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            case R.id.iv_refresh:
                AnimUtils.loadClickAnim(mIvRefresh);
                if (hdp != null) {
                    play(hdp.getUrl());
                }
                break;

            case R.id.tv_definition:
                AnimUtils.loadClickAnim(mTvDefinition);
                initDefinitionListPopup();
                mListPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
                mListPopup.setPreferredDirection(QMUIPopup.DIRECTION_TOP);
                mListPopup.show(v);
                break;

            default:
                break;
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    List<Parade> list = (List<Parade>) msg.obj;
                    adapter.setNewData(list);
                    break;

                case 1:
                    // ??????????????????
                    if (webProgram != null) {
                        webProgram.loadUrl("javascript:window.java_obj.showSource("
                                + "document.getElementsByTagName('html')[0].innerHTML);");
                    }
                    break;

                case 4:
                    if (adapter != null) {
                        if (notifyItemCount > 0) {
                            notifyItemCount--;
                            adapter.notifyDataSetChanged();
                            handler.sendEmptyMessageDelayed(4, 150);
                        }
                    }
                    break;

                case 10:
                    EmptyViewUtils.setEmptyView(LiveActivity.this, adapter,
                            R.layout.empty_content);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * ??????????????????
     */
    private void getProgram() {
        Long id = getIntent().getLongExtra("programId", 0);
        Program program = ProgramOption.getById(id);
        if (program != null) {
            name = program.getName();
            mTvName.setText(name);

            String source = program.getSource();
            if (!TextUtils.isEmpty(source)) {
                String splitStr = ",";
                if (source.contains(splitStr)) {
                    String[] sources = source.split(splitStr);
                    splitStr = "\\|";
                    for (String sou : sources) {
                        if (TextUtils.isEmpty(sou) || !sou.contains("|")) {
                            continue;
                        }
                        String[] type = sou.split(splitStr);
                        Hdp hdp = new Hdp(type[0], type[1]);
                        list.add(hdp);
                    }
                } else {
                    Hdp hdp = new Hdp("", source);
                    list.add(hdp);
                }
            }
            if (list.size() > 0) {
                hdp = list.get(0);
                play(hdp.getUrl());
                loadUrl(program.getParadeUrl());
                if (list.size() > 0) {
                    mTvDefinition.setVisibility(View.VISIBLE);
                    mTvDefinition.setText(hdp.getName());
                }
            }
        }
    }

    private void initRv() {
        List<Parade> list = new ArrayList<>();
        adapter = new ParadeAdapter(this, R.layout.adapter_parade, list);
        mRvProgram.setLayoutManager(new LinearLayoutManager(this));
        mRvProgram.setAdapter(adapter);
    }

    /**
     * ??????
     *
     * @param url String
     */
    private void play(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Log.w(TAG, "play ??????url: " + url);
        //????????????url
        videoPlayer.setUp(url, false, name);
        //????????????url
        videoPlayer.getCurrentPlayer().setUp(url, false, name);
        videoPlayer.getCurrentPlayer().startPlayLogic();
    }

    /**
     * ??????????????????
     */
    private void initPlayer() {
        try {
            //??????????????????
            PlayerUtils.initPlayer(videoPlayer, playStateListener);
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);

            //????????????
            orientationUtils = new OrientationUtils(this, videoPlayer);

            //????????????????????????,????????????????????????????????????????????????
            videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orientationUtils.resolveByClick();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "initPlayer ???????????????????????????" + e.getMessage());
        }
    }

    private VideoAllCallBack playStateListener = new GSYSampleCallBack() {

        @Override
        public void onPrepared(String url, Object... objects) {
            super.onPrepared(url, objects);
        }

        @Override
        public void onQuitFullscreen(String url, Object... objects) {
            super.onQuitFullscreen(url, objects);
            initBlackBar();
            notifyItemCount = 3;
            handler.sendEmptyMessageDelayed(4, 100);
        }

        @Override
        public void onEnterFullscreen(String url, Object... objects) {
            super.onEnterFullscreen(url, objects);
            setTranslucentBar(true);
        }

        @Override
        public void onPlayError(String url, Object... objects) {
            super.onPlayError(url, objects);
        }

    };

    @SuppressLint("SetJavaScriptEnabled")
    private void initProgramWeb() {
        // ??????JavaScript??????
        webProgram.getSettings().setJavaScriptEnabled(true);
        webProgram.addJavascriptInterface(new LiveActivity.InJavaScriptLocalObj(), "java_obj");
        // ??????WebView???????????????????????????????????????????????????????????????true???????????????
        webProgram.getSettings().setSupportZoom(true);
        // ??????WebView??????????????????????????????????????????????????????????????????????????????????????????false?????????????????????????????????
        webProgram.getSettings().setBuiltInZoomControls(true);
        // ??????????????????DOM??????API???????????????false????????????????????????true???WebView????????????DOM storage API
        webProgram.getSettings().setDomStorageEnabled(true);
        // ?????????????????????.??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        webProgram.requestFocus();
        // ???????????????,?????????????????????,??????webview?????????????????????
        webProgram.getSettings().setUseWideViewPort(true);
        //listView,webView????????????????????????????????????????????????
        webProgram.setOverScrollMode(View.OVER_SCROLL_NEVER);
        // ??????webview????????????????????????,????????????????????????
        webProgram.getSettings().setLoadWithOverviewMode(true);
        webProgram.setWebViewClient(webViewClient);
        webProgram.setWebChromeClient(webChromeClient);
    }

    /**
     * ??????url
     *
     * @param url String
     */
    private void loadUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            handler.sendEmptyMessage(10);
            return;
        }
        if (webProgram != null) {
            // ????????????
            webProgram.loadUrl(url);
            //??????????????????
            EmptyViewUtils.setEmptyView(this, adapter, R.layout.empty_request_loading);
        }
    }

    WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // ?????????????????????????????????
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // ?????? url ??????,?????????????????????????????????????????????
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // ??????????????????
            view.loadUrl("javascript:window.java_obj.showSource("
                    + "document.getElementsByTagName('html')[0].innerHTML);");
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // ?????????????????????????????????????????????????????????????????????????????????????????????????????????404???????????????
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          WebResourceRequest request) {
            // ???????????????????????????????????????????????????????????????
            return super.shouldInterceptRequest(view, request);
        }
    };

    WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (progress > 50) {
                handler.removeMessages(1);
                handler.sendEmptyMessageDelayed(1, 2500);
            }
        }
    };

    public final class InJavaScriptLocalObj {

        @JavascriptInterface
        public void showSource(final String html) {
            ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (TextUtils.isEmpty(html)) {
                            throw new Exception("has no html");
                        }
                        List<Parade> newData = new ArrayList<>();
                        Document doc = Jsoup.parse(html);
                        Element element = doc.selectFirst("ul#pgrow");
                        if (element != null) {
                            Elements liElement = element.select("div.over_hide");
                            if (liElement == null) {
                                throw new Exception("has no root element");
                            }
                            for (Element e : liElement) {
                                String text = e.text();
                                if (TextUtils.isEmpty(text)) {
                                    throw new Exception("has no sub element");
                                }
                                if (text.contains(" ")) {
                                    String[] strings = text.split(" ");
                                    Parade parade = new Parade(strings[0], strings[1]);
                                    newData.add(parade);
                                }
                            }
                        } else {
                            element = doc.select("table.timetable").select(".font15").first();
                            if (element == null) {
                                throw new Exception("has no root element");
                            }
                            Elements trElements = element.select("tr.trd");
                            if (trElements == null) {
                                throw new Exception("has no root element");
                            }
                            for (Element e : trElements) {
                                String text = e.text();
                                if (TextUtils.isEmpty(text)) {
                                    throw new Exception("has no sub element");
                                }
                                if (text.contains(" ")) {
                                    String[] strings = text.split(" ");
                                    Parade parade = new Parade(strings[0], strings[1]);
                                    newData.add(parade);
                                }
                            }
                        }
                        Message msg = Message.obtain();
                        msg.what = 0;
                        msg.obj = newData;
                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        Log.e(TAG, "run ????????????????????????: " + e.getMessage());
                        handler.removeMessages(10);
                        handler.sendEmptyMessageDelayed(10, 5 * 1000);
                    }
                }
            });
        }

    }

    private QMUIListPopup mListPopup;

    private void initDefinitionListPopup() {
        if (mListPopup == null) {
            List<String> data = new ArrayList<>();
            for (Hdp hdp : list) {
                data.add(hdp.getName());
            }
            ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.pop_souce_choose, data);
            mListPopup = new QMUIListPopup(this, QMUIPopup.DIRECTION_NONE, adapter);
            mListPopup.create(QMUIDisplayHelper.dp2px(this, 150),
                    QMUIDisplayHelper.dp2px(this, 100),
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view,
                                                int index, long l) {
                            mListPopup.dismiss();
                            if (currentHdp == index) {
                                return;
                            }
                            currentHdp = index;
                            hdp = list.get(index);
                            mTvDefinition.setText(hdp.getName());
                            play(hdp.getUrl());
                        }
                    });
        }
    }


}