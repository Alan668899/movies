package com.xing.mita.movie.activity.common;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemSwipeListener;
import com.gyf.barlibrary.ImmersionBar;
import com.xing.mita.movie.R;
import com.xing.mita.movie.activity.aaqq.AaqqVideoPlayActivity;
import com.xing.mita.movie.adapter.CollectionAdapter;
import com.xing.mita.movie.dao.option.CollectionOption;
import com.xing.mita.movie.entity.Connection;
import com.xing.mita.movie.utils.EmptyViewUtils;
import com.xing.mita.movie.utils.ScreenUtils;
import com.xing.mita.movie.utils.Utils;
import com.xing.mita.movie.view.IconFontView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * @author Mita
 * @date 2018/10/30
 * @Description 我的收藏
 */
public class CollectionActivity extends BaseActivity implements OnItemSwipeListener,
        BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.is_bg)
    ImageSwitcher mImageSwitcher;
    @BindView(R.id.rv_collection)
    RecyclerView mRecyclerView;
    @BindView(R.id.iv_image)
    ImageView mImageView;
    @BindView(R.id.ifv_break)
    IconFontView mIfvBreak;
    @BindView(R.id.ifv_share)
    IconFontView mIfvShare;

    private CollectionAdapter adapter;

    /**
     * 移除比例
     */
    private float deleteScale;
    /**
     * 移除距离
     */
    private float deleteDis;

    /**
     * 当前显示item
     */
    private int currentItem;

    @Override
    public int getContentViewResId() {
        return R.layout.activity_collection;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int rvHeight = Utils.dp2px(CollectionActivity.this, 485);
                float rvTop = (ScreenUtils.getScreenHeight(CollectionActivity.this) - rvHeight) / 2;
                deleteDis = rvTop - mIfvBreak.getBottom();
                deleteScale = deleteDis / rvHeight;
                handler.sendEmptyMessage(0);
            }
        });
    }

    @OnClick({R.id.ifv_back, R.id.ifv_share})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.ifv_back:
                finish();
                break;

            case R.id.ifv_share:
                if (ScreenUtils.checkIsInMultiWindow(this,
                        R.string.tip_multi_window_enter_share)) {
                    break;
                }
                Connection connection = adapter.getData().get(currentItem);
                Intent intent = new Intent();
                intent.putExtra("image", connection.getImage());
                intent.putExtra("name", connection.getName());
                intent.putExtra("url", connection.getLink());
                intent.putExtra("source", connection.getSource());
                startWithIntent(ShareMovieActivity.class, intent);
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
                    initRv();
                    List<Connection> list = CollectionOption.getCollections();
                    if (list == null || list.size() == 0) {
                        showEmptyLayout();
                    } else {
                        Collections.reverse(list);
                        adapter.setNewData(list);
                        initImageSwitcher();
                        setImageSwitcher(list.get(0).getImage());
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 初始化RecyclerView
     */
    private void initRv() {
        List<Connection> list = new ArrayList<>();
        adapter = new CollectionAdapter(R.layout.adapter_collection, list);
        // 开启滑动删除
        ItemDragAndSwipeCallback callback = new ItemDragAndSwipeCallback(adapter) {

            @Override
            public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
                return deleteScale;
            }

        };
        //设置移除方向
        callback.setSwipeMoveFlags(ItemTouchHelper.UP);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        adapter.enableSwipeItem();
        adapter.setOnItemSwipeListener(this);
        adapter.setOnItemClickListener(this);
        final LinearLayoutManager llm = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentItem = llm.findFirstCompletelyVisibleItemPosition();
                    List<Connection> connections = adapter.getData();
                    if (currentItem < 0 || currentItem >= connections.size()) {
                        return;
                    }
                    setImageSwitcher(connections.get(currentItem).getImage());
                }
            }
        });
    }

    @Override
    public void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos) {
        mIfvBreak.setVisibility(View.VISIBLE);
    }

    @Override
    public void clearView(RecyclerView.ViewHolder viewHolder, int pos) {
        mIfvBreak.setVisibility(View.GONE);
    }

    @Override
    public void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos) {
        List<Connection> list = adapter.getData();
        //取消收藏
        CollectionOption.cancelCollection(list.get(pos).getLink());
        if (currentItem != pos) {
            return;
        }
        //显示下一个封面
        if (pos == list.size() - 1) {
            pos--;
        } else {
            pos++;
        }
        if (pos >= 0 && pos < list.size()) {
            setImageSwitcher(list.get(pos).getImage());
        } else {
            showEmptyLayout();
        }
    }

    @Override
    public void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive) {
        boolean canDelete = deleteDis + dY < 0;
        mIfvBreak.setTextColor(ContextCompat.getColor(this, canDelete ? R.color.red : R.color.color_66));
    }

    /**
     * 初始化状态栏（颜色White）
     */
    @Override
    protected void initWhiteBar() {
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.flymeOSStatusBarFontColor(R.color.black)
                .statusBarDarkFont(true)
                .init();
    }

    /**
     * 显示空布局
     */
    private void showEmptyLayout() {
        EmptyViewUtils.setEmptyView(CollectionActivity.this, adapter, R.layout.empty_content);
        mImageSwitcher.setVisibility(View.INVISIBLE);
        mIfvShare.setVisibility(View.INVISIBLE);
        mTvTitle.setText(R.string.my_collect);
    }

    private void initImageSwitcher() {
        //通过代码设定切换效果
        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            @Override
            public View makeView() {
                // makeView返回的是当前需要显示的ImageView控件，用于填充进ImageSwitcher中。
                ImageView image = new ImageView(CollectionActivity.this);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                image.setLayoutParams(new ImageSwitcher.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT));
                return image;
            }
        });
    }

    private void setImageSwitcher(String url) {
        mImageSwitcher.setVisibility(View.VISIBLE);
        Glide.with(this).load(url)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 10))).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                mImageSwitcher.setImageDrawable(resource);
                return true;
            }
        }).into(mImageView);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        List list = adapter.getData();
        Connection connection = (Connection) list.get(position);
        Intent intent = new Intent();
        intent.putExtra("url", connection.getLink());
        intent.putExtra("imgUrl", connection.getImage());
        startWithIntent(AaqqVideoPlayActivity.class, intent);
    }


}
