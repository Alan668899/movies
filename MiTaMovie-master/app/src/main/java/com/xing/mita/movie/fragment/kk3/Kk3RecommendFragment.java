package com.xing.mita.movie.fragment.kk3;

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
import com.xing.mita.movie.activity.kk3.Kk3VideoPlayActivity;
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
 * @date 2018/12/3
 * @Description KK3首页推荐
 */
public class Kk3RecommendFragment extends BaseFragment implements
        BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.rv_home)
    RecyclerView mRvHome;

    private HomeAdapter adapter;

    private List<Home> list = new ArrayList<>();

    @Override
    protected int setContentView() {
        return R.layout.fragment_kk3_recommend;
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
        requestRecommendHtml();
        requestNewHtml();
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

    /**
     * 加载首页推荐
     */
    private void requestRecommendHtml() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                String htmlStr = HttpUtils.getKk3HTML(Constant.SOURCE_KK3);
                if (TextUtils.isEmpty(htmlStr)) {
                    handler.sendEmptyMessage(10);
                    return;
                }
                try {
                    int[] icons = {R.string.icon_movie, R.string.icon_teleplay,
                            R.string.icon_cartoon, R.string.icon_variety};
                    int[] titles = {R.string.tab_movie, R.string.tab_teleplay,
                            R.string.tab_cartoon, R.string.tab_variety};
                    //从一个URL加载一个Document对象。
                    Document doc = Jsoup.parse(htmlStr);
                    Elements recommendElements = doc.select("div.ui-cnt").select(".listimg-cnt");
                    int size = recommendElements.size();
                    for (int i = 0; i < size; i++) {
                        Elements aElements = recommendElements.get(i).select("a.ui-pic");
                        if (aElements == null || aElements.size() == 0) {
                            continue;
                        }
                        Home home = new Home(true, getString(titles[i]));
                        home.setTitle(getString(titles[i])).setTitleIcon(icons[i]);
                        list.add(home);
                        int j = 0;
                        for (Element ae : aElements) {
                            if (j > 2) {
                                break;
                            }
                            String title = ae.attr("title");
                            String href = ae.attr("href");
                            Element imgElement = ae.selectFirst("img");
                            String img = imgElement.attr("src");
                            Element typeElement = ae.selectFirst("span.ui-type");
                            String label = typeElement.text();
                            home = new Home(false, title);
                            home.setType(1)
                                    .setContentName(title)
                                    .setContentLabel(label)
                                    .setContentPic(img)
                                    .setContentLink(href);
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

    /**
     * 加载首页推荐
     */
    private void requestNewHtml() {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                String htmlStr = HttpUtils.getKk3HTML(Constant.SOURCE_KK3_RECOMMEND);
                if (TextUtils.isEmpty(htmlStr)) {
                    return;
                }
                try {
                    //从一个URL加载一个Document对象。
                    Document doc = Jsoup.parse(htmlStr);
                    Elements hotElements = doc.select("ul.ui-list").select("a");
                    List<HotList> hotLists = new ArrayList<>();
                    for (Element e : hotElements) {
                        String name = e.text();
                        String link = e.attr("href");
                        String date = e.selectFirst("span").text();
                        if (name.length() > date.length()) {
                            name = name.substring(date.length());
                        }
                        HotList hotList = new HotList(name, "", link, Constant.SOURCE_KK3);
                        hotLists.add(hotList);
                    }
                    if (hotLists.size() == 0) {
                        return;
                    }
                    HotListOption.saveHotList(hotLists);
                    //通知加载热搜
                    Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    Intent intent = new Intent(Constant.BROADCAST_KK3_LOAD_SEARCH_RECOMMEND);
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
                } catch (Exception e) {
                    logE(e.toString());
                }
            }
        });
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Home home = list.get(position);
        if (!home.isHeader) {
            Intent intent = new Intent();
            intent.putExtra("url", home.getContentLink());
            intent.putExtra("imgUrl", home.getContentPic());
            startWithIntent(Kk3VideoPlayActivity.class, intent);
        }
    }

}
