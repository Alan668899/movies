package com.xing.mita.movie.fragment.aaqq;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.aaqq.AaqqVideoPlayActivity;
import com.xing.mita.movie.adapter.HomeAdapter;
import com.xing.mita.movie.dao.option.HotListOption;
import com.xing.mita.movie.entity.Home;
import com.xing.mita.movie.entity.HotList;
import com.xing.mita.movie.fragment.common.BaseFragment;
import com.xing.mita.movie.http.HttpUtils;
import com.xing.mita.movie.thread.ThreadPoolProxyFactory;
import com.xing.mita.movie.utils.Constant;
import com.xing.mita.movie.utils.EmptyViewUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author Mita
 * @date 2018/10/12
 * @Description 首页推荐
 */
public class AaqqRecommendFragment extends BaseFragment implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.rv_home)
    RecyclerView mRvHome;

    private HomeAdapter adapter;

    private List<Home> list = new ArrayList<>();
    private int[] icons = {R.string.icon_hot_broadcast, R.string.icon_movie, R.string.icon_teleplay,
            R.string.icon_cartoon};

    @Override
    protected int setContentView() {
        return R.layout.fragment_recommend;
    }

    @Override
    protected void init() {
    }

    @Override
    protected void lazyLoad() {
        initRv();
        showLoading();
    }

    /**
     * 显示加载动画
     */
    private void showLoading() {
        EmptyViewUtils.setEmptyView(getActivity(), adapter,
                R.layout.empty_request_loading);
        requestHtml();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    break;

                case 10:
                    View convertView = EmptyViewUtils.setEmptyView(getActivity(), adapter,
                            R.layout.empty_request_fail);
                    if (convertView == null) {
                        break;
                    }
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showLoading();
                        }
                    });
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    private void initRv() {
        adapter = new HomeAdapter(R.layout.adapter_home_content, R.layout.adapter_home_title, list);
        adapter.setOnItemClickListener(this);
        mRvHome.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        //开启列表加载动画
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        //重复执行
        adapter.isFirstOnly(false);
        mRvHome.setAdapter(adapter);
    }

    private void requestHtml() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                String htmlStr = HttpUtils.getHTML(Constant.SOURCE_AAQQY_MOBILE, true);
                if (TextUtils.isEmpty(htmlStr)) {
                    handler.sendEmptyMessage(10);
                    return;
                }
                try {
                    //从一个URL加载一个Document对象。
                    Document doc = Jsoup.parse(htmlStr);

                    //搜索推荐内容
                    Elements recommendElements = doc.select("p.pLinks").select("a");
                    List<HotList> hotLists = new ArrayList<>();
                    for (Element element : recommendElements) {
                        String link = element.attr("href");
                        String name = element.attr("title");
                        String type = element.select("span").last().text();
                        HotList hotList = new HotList(name, type, link, Constant.SOURCE_AAQQY);
                        hotLists.add(hotList);
                    }
                    HotListOption.saveHotList(hotLists);

                    //广播加载热搜
                    Activity activity = getActivity();
                    if (activity != null) {
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
                        Intent intent = new Intent();
                        intent.setAction(Constant.BROADCAST_AAQQ_LOAD_SEARCH_RECOMMEND);
                        localBroadcastManager.sendBroadcast(intent);
                    }

                    Elements elements = doc.select("p.headerChannelList").select("a");
                    for (Element element : elements) {
                        logI(element.ownText());
                        logI(element.attr("href"));
                    }

                    Elements elementTitles = doc.select("h2").select("a");
                    for (Element element : elementTitles) {
                        logI(element.ownText());
                        logI(element.attr("title"));
                    }

                    Elements elementContents = doc.select("ul.list_tab_img");

                    int titleSize = elementTitles.size();
                    int contentSize = elementContents.size();

                    list.clear();
                    int currentIndex = 0;
                    for (int i = 0; i < contentSize; i++) {
                        if (titleSize > currentIndex && (i == 0 || contentSize - i < titleSize)) {
                            Element element = elementTitles.get(currentIndex);
                            String title = element.attr("title");
                            String titleLink = element.attr("href");
                            Home home = new Home(true, title);
                            home.setTitle(title).setTitleLink(titleLink).setType(0);
                            if (currentIndex < icons.length) {
                                home.setTitleIcon(icons[currentIndex]);
                            }
                            list.add(home);
                            currentIndex++;
                        }
                        Element element = elementContents.get(i);
                        Elements items = element.select("a");
                        int j = 0;
                        for (Element item : items) {
                            if (j > 2) {
                                break;
                            }
                            String name = item.attr("title");
                            String link = item.attr("href");
                            String label = null;
                            Element labels = item.select("label.title").first();
                            if (labels != null) {
                                label = labels.text();
                            }
                            Elements imgs = item.select("img");
                            String img = null;
                            if (imgs.size() > 0) {
                                img = imgs.get(0).attr("data-original");
                                if (img.length() > 5) {
                                    String head = img.substring(0, 5);
                                    if (!head.contains("http")) {
                                        img = "http:" + img;
                                    }
                                }
                            }
                            Home home = new Home(false, name);
                            home.setType(1)
                                    .setContentName(name)
                                    .setContentLink(link)
                                    .setContentPic(img)
                                    .setContentLabel(label);
                            list.add(home);
                            j++;
                        }
                    }
                    if (list == null || list.size() == 0) {
                        throw new Exception("have no data");
                    }
                    handler.sendEmptyMessage(0);
                } catch (Exception e) {
                    logE(e.toString());
                    handler.sendEmptyMessage(10);
                }
            }
        });
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Home home = list.get(position);
        if (home.isHeader) {
            String name = home.getTitle();
            int index = 0;
            if (TextUtils.equals(name, getString(R.string.tab_movie))) {
                index = 1;
            } else if (TextUtils.equals(name, getString(R.string.tab_teleplay))) {
                index = 2;
            } else if (TextUtils.equals(name, getString(R.string.tab_variety))) {
                index = 3;
            } else if (TextUtils.equals(name, getString(R.string.tab_cartoon))) {
                index = 4;
            }
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(activity);
            Intent intent = new Intent(Constant.BROADCAST_SHOW_FRAGMENT_INDEX);
            intent.putExtra("index", index);
            localBroadcastManager.sendBroadcast(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("url", home.getContentLink());
            intent.putExtra("imgUrl", home.getContentPic());
            startWithIntent(AaqqVideoPlayActivity.class, intent);
        }
    }

}
