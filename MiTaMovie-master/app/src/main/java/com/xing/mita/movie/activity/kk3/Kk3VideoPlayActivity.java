package com.xing.mita.movie.activity.kk3;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.VideoAllCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.aaqq.AaqqVideoPlayActivity;
import com.xing.mita.movie.activity.common.BaseActivity;
import com.xing.mita.movie.activity.common.MovieDetailActivity;
import com.xing.mita.movie.activity.common.ShareMovieActivity;
import com.xing.mita.movie.activity.common.X5FullScreenActivity;
import com.xing.mita.movie.adapter.EpisodeAdapter;
import com.xing.mita.movie.dao.option.CollectionOption;
import com.xing.mita.movie.dao.option.DownloadOption;
import com.xing.mita.movie.dao.option.MovieHistoryOption;
import com.xing.mita.movie.entity.Connection;
import com.xing.mita.movie.entity.Download;
import com.xing.mita.movie.entity.Episode;
import com.xing.mita.movie.entity.MovieHistory;
import com.xing.mita.movie.entity.Source;
import com.xing.mita.movie.http.HttpUtils;
import com.xing.mita.movie.player.CoverVideoPlayer;
import com.xing.mita.movie.player.PlayerUtils;
import com.xing.mita.movie.service.DownLoadVideoService;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.AnimUtils;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.DateUtils;
import com.xing.mita.movie.utils.NetworkUtils;
import com.xing.mita.movie.utils.ScreenUtils;
import com.xing.mita.movie.utils.WebViewUtils;
import com.xing.mita.movie.view.IconFontView;
import com.xing.mita.movie.view.OwnWebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.jessyan.autosize.AutoSize;

/**
 * @author Mita
 * @date 2018/10/13
 * @Description ???????????????
 */
public class Kk3VideoPlayActivity extends BaseActivity implements
        BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.webView)
    OwnWebView webView;
    @BindView(R.id.video_player)
    CoverVideoPlayer videoPlayer;
    @BindView(R.id.rv_episode)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_video_name)
    TextView mTvName;
    @BindView(R.id.tv_origin)
    TextView mTvOrigin;
    @BindView(R.id.tv_origin_tip)
    TextView mTvOriginTip;
    @BindView(R.id.ifv_origin)
    IconFontView mIfvOrigin;
    @BindView(R.id.ifv_collect)
    IconFontView mIfvCollect;
    @BindView(R.id.ifv_download)
    IconFontView mIfvDownload;
    @BindView(R.id.ifv_share)
    IconFontView mIfvShare;
    @BindView(R.id.ifv_x5)
    IconFontView mIfvX5;

    private OrientationUtils orientationUtils;

    private EpisodeAdapter adapter;
    private List<Source> sources = new ArrayList<>();

    private int currentSource, playSource, playEpisode;
    private String originUrl;
    private String name;
    private String episodeName;
    private String detail;
    private String imgUrl;
    private String intro;
    private String finalUrl;
    private String historySourceId;
    private String historyEpisodeName;
    /**
     * ??????url
     */
    private String subLink;

    private long historyProgress;

    /**
     * ??????????????????
     */
    private long currentPlayPosition;
    /**
     * ????????????
     */
    private long videoDuring;

    private boolean isCollect;
    /**
     * ???????????????????????????item
     */
    private boolean needNotifyItem;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_video_detail;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        if (!TextUtils.isEmpty(originUrl)) {
            return;
        }
        originUrl = getIntent().getStringExtra("url");
        imgUrl = getIntent().getStringExtra("imgUrl");
        initData();
    }

    private void initData() {
        //????????????
        videoPlayer.loadCoverImage(imgUrl);
        //????????????????????????
        isCollect = CollectionOption.hasCollection(originUrl);
        changeCollectState();
        //?????????WebView
        WebViewUtils.initWebSettings(webView, webViewClient);
        //??????????????????
        initPlayer();
        //????????????
        MovieHistory history = MovieHistoryOption.getHistoryByUrl(originUrl);
        if (history != null) {
            historyEpisodeName = history.getEpisode();
            historySourceId = history.getSource();
            historyProgress = history.getProgress();
            finalUrl = history.getFinalPlayUrl();
            name = history.getName();
            episodeName = history.getEpisode();
            playEpisode = history.getPosition();
            play(finalUrl);
        }
        //??????????????????
        getWebSource();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState ?????????????????????: ");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState ????????????: ");
        recreate();
    }

    @OnClick({R.id.ifv_back, R.id.tv_video_name, R.id.tv_origin_tip, R.id.ifv_share,
            R.id.ifv_download, R.id.ifv_collect, R.id.ifv_x5})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            case R.id.tv_video_name:
                Intent intent = new Intent();
                intent.putExtra("name", name);
                intent.putExtra("imgUrl", imgUrl);
                intent.putExtra("detail", detail);
                intent.putExtra("intro", intro);
                startWithIntent(MovieDetailActivity.class, intent);
                break;

            case R.id.tv_origin_tip:
                showPopMenu();
                mIfvOrigin.setText(R.string.icon_up);
                break;

            case R.id.ifv_collect:
                isCollect = !isCollect;
                AnimUtils.loadClickAnim(mIfvCollect);
                changeCollectState();
                if (isCollect) {
                    //????????????
                    Connection connection = new Connection();
                    connection.setLink(originUrl)
                            .setImage(imgUrl)
                            .setName(name)
                            .setIntro(intro)
                            .setSource(Constant.SOURCE_KK3);
                    CollectionOption.saveCollection(connection);
                } else {
                    //????????????
                    CollectionOption.cancelCollection(originUrl);
                }
                showCollectStatus();
                break;

            case R.id.ifv_download:
                attemptDownload();
                break;

            case R.id.ifv_share:
                AnimUtils.loadClickAnim(mIfvShare);
                intent = new Intent();
                intent.putExtra("image", imgUrl);
                intent.putExtra("name", name);
                intent.putExtra("url", originUrl);
                intent.putExtra("source", Constant.SOURCE_KK3);
                startWithIntent(ShareMovieActivity.class, intent);
                break;

            case R.id.ifv_x5:
                intent = new Intent();
                intent.putExtra("url", finalUrl);
                startWithIntent(X5FullScreenActivity.class, intent);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveHistory();
        videoPlayer.onVideoPause();
        handler.removeMessages(1);
    }

    @Override
    protected void onDestroy() {
        //????????????
        videoPlayer.setVideoAllCallBack(null);
        GSYVideoManager.releaseAllVideos();
        //??????webView??????
        webView.destroy();
        webView = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //????????????????????????
        boolean isLock = ((CoverVideoPlayer) videoPlayer.getCurrentPlayer()).isLock();
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
        boolean isLand = ScreenUtils.getScreenIsLand(this);
        if (!isLand) {
            //???????????????????????????size???????????????item?????????
            AutoSize.autoConvertDensity(Kk3VideoPlayActivity.this,
                    411, true);
        }
        //????????????????????????
        videoPlayer.onConfigurationChanged(this, newConfig, orientationUtils,
                true, true);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        boolean sameItem = currentSource == playSource && position == playEpisode;
        List<Episode> list = sources.get(currentSource).getEpisodeList();
        Episode episode = list.get(position);
        if (!sameItem) {
            episode.setSelect(true);
            adapter.notifyItemChanged(position);

            if (sources.size() > playSource) {
                List<Episode> playList = sources.get(playSource).getEpisodeList();
                if (playList != null && playList.size() > playEpisode) {
                    Episode lastEpisode = playList.get(playEpisode);
                    lastEpisode.setSelect(false);
                    if (currentSource == playSource) {
                        adapter.notifyItemChanged(playEpisode);
                    }
                }
            }
        }
        String link = episode.getLink();
        episodeName = episode.getTitle();
        loadHtml(link);

        playSource = currentSource;
        playEpisode = position;

        //??????????????????
        historyProgress = 0;
        historyEpisodeName = "";
        historySourceId = "";
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    mTvName.setText(name);
                    int size = sources.size();
                    if (size == 1) {
                        mTvOriginTip.setClickable(false);
                        mIfvOrigin.setVisibility(View.GONE);
                    }
                    //???????????????id???????????????
                    if (!TextUtils.isEmpty(historySourceId)) {
                        for (int i = 0; i < size; i++) {
                            if (TextUtils.equals(historySourceId, sources.get(i).getId())) {
                                currentSource = i;
                                playSource = i;
                                break;
                            }
                        }
                    }
                    if (size > currentSource) {
                        mTvOrigin.setText(sources.get(currentSource).getSource());
                    }
                    initRv();
                    if (size == 0) {
                        break;
                    }
                    List<Episode> episodeList = sources.get(currentSource).getEpisodeList();
                    if (episodeList == null || episodeList.size() == 0) {
                        break;
                    }
                    //?????????????????????????????????????????????????????????
                    playEpisode = episodeList.size() - 1 - playEpisode;
                    Episode episode = episodeList.get(playEpisode);
                    if (episode == null) {
                        break;
                    }
                    //??????????????????????????????????????????????????????
                    if (historyProgress <= 0) {
                        String url = episode.getLink();
                        loadHtml(url);
                    }

                    episodeName = episode.getTitle();
                    episode.setSelect(true);
                    adapter.notifyItemChanged(playEpisode);
                    break;

                case 1:
                    if (videoPlayer.isInPlayingState()) {
                        currentPlayPosition = videoPlayer.getCurrentPositionWhenPlaying();
                    }
                    handler.sendEmptyMessageDelayed(1, 3000);
                    break;

                case 2:
                    if (message.obj instanceof String) {
                        String url = (String) message.obj;
                        play(url);
                    }
                    break;

                case 3:
                    String url = (String) message.obj;
                    if (webView != null) {
                        webView.loadUrl(url);
                    }
                    break;

                case 4:
                    if (adapter != null) {
                        if (needNotifyItem &&
                                !ScreenUtils.getScreenIsLand(Kk3VideoPlayActivity.this)) {
                            needNotifyItem = false;
                            adapter.notifyDataSetChanged();
                        } else {
                            handler.sendEmptyMessageDelayed(4, 50);
                        }
                    }
                    break;

                case 5:
                    ((CoverVideoPlayer) videoPlayer.getCurrentPlayer()).loadCoverImage(imgUrl);
                    break;

                case 6:
                    getWebSource();
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * ????????????
     */
    private void attemptDownload() {
        AnimUtils.loadClickAnim(mIfvDownload);
        boolean connectWifi = NetworkUtils.isWifiConnected(this);
        if (!connectWifi) {
            showFlowDialog();
        } else {
            startDownload();
        }
    }

    /**
     * ????????????????????????
     */
    private void showFlowDialog() {
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle(R.string.flow_tip)
                .setMessage(R.string.flow_tip_message)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.confirm, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        startDownload();
                    }
                })
                .create(com.qmuiteam.qmui.R.style.QMUI_Dialog).show();
    }

    /**
     * ????????????
     */
    private void startDownload() {
        Download down = DownloadOption.load(finalUrl);
        if (down != null) {
            showDownloadStatus(true);
            return;
        }
        //??????????????????
        down = new Download();
        down.setName(name).setEpisode(episodeName).setThumb(imgUrl).setUrl(finalUrl);
        DownloadOption.save(down);
        //?????????????????????
        showDownloadStatus(false);
        DownLoadVideoService.startDownload(this);
    }

    /**
     * ?????????????????????
     */
    private void showDownloadStatus(boolean hasDown) {
        String msg = getString(hasDown ? R.string.status_has_download :
                R.string.status_add_download);
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(hasDown ? QMUITipDialog.Builder.ICON_TYPE_INFO :
                        QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(msg)
                .create();
        tipDialog.show();
        mTvName.postDelayed(new Runnable() {
            @Override
            public void run() {
                tipDialog.dismiss();
            }
        }, 1500);
    }

    /**
     * ??????????????????
     */
    private void changeCollectState() {
        mIfvCollect.setText(isCollect ? R.string.icon_collected : R.string.icon_collect);
        mIfvCollect.setTextColor(ContextCompat.getColor(this,
                isCollect ? R.color.red : R.color.color_66));
    }

    /**
     * ?????????RecyclerView
     */
    private void initRv() {
        if (sources.size() <= currentSource) {
            return;
        }
        List<Episode> list = sources.get(currentSource).getEpisodeList();
        adapter = new EpisodeAdapter(this, R.layout.adapter_episode, list);
        adapter.setOnItemClickListener(this);
        int spanCount = 3;
        for (Episode episode : list) {
            int len = episode.getTitle().length();
            if (len > 13) {
                spanCount = 1;
                break;
            } else if (len > 9) {
                spanCount = 2;
            }
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * ???????????????
     */
    private void showPopMenu() {
        PopupMenu popupMenu = new PopupMenu(this, mTvOrigin);
        final Menu sourceMenu = popupMenu.getMenu();
        int size = sources.size();
        for (int i = 0; i < size; i++) {
            sourceMenu.add(Menu.NONE, Menu.FIRST + i, i, sources.get(i).getSource());
        }
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                mIfvOrigin.setText(R.string.icon_down);
            }
        });
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int i = item.getItemId() - 1;
                currentSource = i;
                mTvOrigin.setText(sources.get(i).getSource());
                adapter.setNewData(sources.get(i).getEpisodeList());
                return true;
            }
        });

        popupMenu.show();
    }

    private void getWebSource() {
        //??????????????????
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                getHtml();
            }
        });
    }

    private void getHtml() {
        try {
            String url = Constant.SOURCE_KK3 + originUrl;
            String html = HttpUtils.getKk3HTML(url);
            if (TextUtils.isEmpty(html)) {
                throw new Exception("html is empty");
            }
            Document doc = Jsoup.parse(html);

            Element titleElement = doc.selectFirst("div.title");
            name = titleElement.text();

            Element imgDiv = doc.selectFirst("div.ui-img");
            Element imgElement = imgDiv.selectFirst("img");
            if (imgElement != null) {
                imgUrl = imgElement.attr("src");
            }

            Element statusDiv = doc.selectFirst("div.ui-text");
            Elements statusElements = statusDiv.select("p");
            StringBuilder sb = new StringBuilder();
            for (Element e : statusElements) {
                log("p???" + e.text());
                String detail = e.text();
                if (TextUtils.isEmpty(detail) || detail.length() < 4) {
                    continue;
                }
                sb.append(detail);
                sb.append("\n");
            }
            detail = sb.toString();

            Elements infoDiv = doc.select("div.ui-detail-info");
            for (Element e : infoDiv) {
                Element pElement = e.selectFirst("p");
                if (pElement == null) {
                    continue;
                }
                intro = pElement.text();
            }

            Elements sourceList = doc.select("div.ui-cnt");
            int size = sourceList.size();
            size = size == 1 ? size : size - 1;
            for (int i = 0; i < size; i++) {
                Element e = sourceList.get(i);
                String source = "??????" + (++i);

                List<Episode> list = new ArrayList<>();
                Elements episodeList = e.select("a");
                for (Element episodeElement : episodeList) {
                    Episode episode = new Episode();
                    episode.setTitle(episodeElement.attr("title"))
                            .setLink(episodeElement.attr("href"));
                    list.add(episode);
                }
                Collections.reverse(list);
                sources.add(new Source(source, source, list));
            }
            if (sources.size() == 0) {
                throw new Exception("parse html error");
            }
            handler.sendEmptyMessage(0);
        } catch (Exception e) {
            logE("???????????????" + e.getMessage());
            handler.sendEmptyMessageDelayed(6, 1000);
        }
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
        //????????????ExoPlayer
        boolean toExo = url.contains(".m3u8");
        PlayerUtils.changeExoMode(toExo);
        //??????
        String title = name + " ?? " + episodeName;
        //????????????url
        videoPlayer.setUp(url, true, title);
        //????????????url
        videoPlayer.getCurrentPlayer().setUp(url, true, title);
        videoPlayer.setSeekOnStart(historyProgress);
        videoPlayer.getCurrentPlayer().setSeekOnStart(historyProgress);
        videoPlayer.getCurrentPlayer().startPlayLogic();
    }

    private VideoAllCallBack playStateListener = new GSYSampleCallBack() {

        @Override
        public void onPrepared(String url, Object... objects) {
            super.onPrepared(url, objects);
            //????????????X5??????
            mIfvX5.setVisibility(View.VISIBLE);
            //??????????????????
            boolean canDown = url.contains(".mp4") || url.contains(".mkv") || url.contains(".avi")
                    || url.contains(".mov") || url.contains(".rmvb");
            if (!TextUtils.isEmpty(url) && canDown) {
                mIfvDownload.setVisibility(View.VISIBLE);
            }
            //??????????????????
            videoDuring = videoPlayer.getDuration();
            handler.sendEmptyMessageDelayed(1, 3000);
            Log.i(TAG, "onPrepared ?????????????????????: ");
        }

        @Override
        public void onQuitFullscreen(String url, Object... objects) {
            super.onQuitFullscreen(url, objects);
            setTranslucentBar(false);
            needNotifyItem = true;
            handler.sendEmptyMessage(4);
            if (orientationUtils != null) {
                orientationUtils.setEnable(false);
            }
        }

        @Override
        public void onEnterFullscreen(String url, Object... objects) {
            super.onEnterFullscreen(url, objects);
            setTranslucentBar(true);
            if (orientationUtils != null) {
                orientationUtils.setEnable(true);
            }
        }

        @Override
        public void onPlayError(String url, Object... objects) {
            super.onPlayError(url, objects);
            Log.i(TAG, "onPlayError : ????????????");
            if (TextUtils.isEmpty(url)) {
                return;
            }
            if (currentPlayPosition > 0) {
                CoverVideoPlayer videoPlayer = ((CoverVideoPlayer) objects[1]);
                videoPlayer.setSeekOnStart(currentPlayPosition);
                videoPlayer.startPlayLogic();
            }
        }

        @Override
        public void onAutoComplete(String url, Object... objects) {
            if (sources.size() <= currentSource) {
                super.onAutoComplete(url, objects);
                return;
            }
            List<Episode> list = sources.get(currentSource).getEpisodeList();
            if (list == null || list.size() == 0) {
                super.onAutoComplete(url, objects);
                return;
            }
            int position = playEpisode - 1;
            if (position < 0) {
                videoPlayer.onVideoPause();
                videoPlayer.changeUiToCompleteClear();
                if (ScreenUtils.getScreenIsLand(Kk3VideoPlayActivity.this)) {
                    videoPlayer.onBackFullscreen();
                    handler.sendEmptyMessageDelayed(5, 1000);
                }
                return;
            }
            Episode episode = list.get(position);
            String link = episode.getLink();
            //????????????
            loadHtml(link);
            episodeName = episode.getTitle();
            //????????????
            episode.setSelect(true);
            adapter.notifyItemChanged(position);

            if (sources.size() <= playSource) {
                super.onAutoComplete(url, objects);
                return;
            }
            List<Episode> playList = sources.get(playSource).getEpisodeList();
            Episode lastEpisode = playList.get(playEpisode);
            lastEpisode.setSelect(false);
            if (currentSource == playSource) {
                adapter.notifyItemChanged(playEpisode);
            }

            playSource = currentSource;
            playEpisode = position;

            //??????????????????
            historyProgress = 0;
            historyEpisodeName = "";
            historySourceId = "";
        }

    };

    /**
     * ??????????????????
     */
    private void saveHistory() {
        int size = sources.size();
        if (size <= currentSource) {
            return;
        }
        List<Episode> list = sources.get(currentSource).getEpisodeList();
        if (list == null || list.size() <= playEpisode) {
            return;
        }
        int position = list.size() - playEpisode - 1;
        MovieHistory history = new MovieHistory();
        history.setName(name)
                .setWebSite(Constant.SOURCE_KK3)
                .setEpisode(episodeName)
                .setSubLink(subLink)
                .setImage(imgUrl)
                .setProgress(currentPlayPosition)
                .setDuring(videoDuring)
                .setLink(originUrl)
                .setFinalPlayUrl(finalUrl)
                .setSource(sources.get(currentSource).getId())
                .setPosition(position)
                .setDate(DateUtils.formatToDate(new Date()));
        MovieHistoryOption.saveHistory(history);
    }

    /**
     * ?????????webView??????
     */
    private void loadHtml(String url) {
        subLink = url;
        url = Constant.SOURCE_KK3 + url;
        log("????????????url???" + url);
        if (webView != null) {
            webView.loadUrl(url);
        }
        //????????????X5??????
        mIfvX5.setVisibility(View.GONE);
        //??????????????????
        mIfvDownload.setVisibility(View.GONE);
    }

    WebViewClient webViewClient = new WebViewClient() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            log("?????????Host???" + url.getHost());
            log("?????????Path???" + url.getPath());
            return super.shouldInterceptRequest(view, request);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url.contains(".mp4") || url.contains(".m3u8") || url.contains(".avi") ||
                    url.contains(".mov") || url.contains(".mkv") || url.contains(".flv") ||
                    url.contains(".f4v") || url.contains(".rmvb")) {
                log("??????url???" + url);
                if (!TextUtils.equals(finalUrl, url)) {
                    finalUrl = url;
                    if (!again) {
                        Message msg = Message.obtain();
                        msg.what = 3;
                        msg.obj = url;
                        handler.sendMessageDelayed(msg, 500);
                        again = true;
                    }
                    Message msg = Message.obtain();
                    msg.what = 2;
                    msg.obj = finalUrl;
                    handler.sendMessage(msg);
                }
            }
            log("url???" + url);
            return super.shouldInterceptRequest(view, url);
        }
    };
    boolean again;

    /**
     * ??????????????????
     */
    private void initPlayer() {
        try {
            //??????????????????
            PlayerUtils.initPlayer(videoPlayer, playStateListener);
            //????????????
            orientationUtils = new OrientationUtils(this, videoPlayer);
            orientationUtils.setEnable(false);

            //????????????????????????,????????????????????????????????????????????????
            videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orientationUtils.resolveByClick();
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "initPlayer ???????????????????????????" + e.getMessage());
        }
    }

    /**
     * ?????????????????????
     */
    private void showCollectStatus() {
        String msg = getString(isCollect ? R.string.collect_success : R.string.collect_cancel);
        final QMUITipDialog tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord(msg)
                .create();
        tipDialog.show();
        mTvName.postDelayed(new Runnable() {
            @Override
            public void run() {
                tipDialog.dismiss();
            }
        }, 1500);
    }

}
