package com.lxw.videoworld.framework.widget;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lxw.videoworld.R;
import com.lxw.videoworld.app.model.KeyValueModel;
import com.lxw.videoworld.app.service.DownloadManager;
import com.lxw.videoworld.app.util.ColorUtil;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Zion on 2018/4/15.
 */

public class CustomListGSYVideoPlayer extends ListGSYVideoPlayer {

    //比例类型
    private int mType = 0;
    // 播放速度
    private float mSpeed = 1.0f;
    private DrawerLayout drawerlayoutSourceLinks;
    private RecyclerView recyclerviewSourceLinks;
    private BaseQuickAdapter<KeyValueModel, BaseViewHolder> sourceLinkAdapter;
    private List<KeyValueModel> sourceList = new ArrayList<>();
    private ArrayList<String> urlList = new ArrayList<>();

    public CustomListGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public CustomListGSYVideoPlayer(Context context) {
        super(context);
    }

    public CustomListGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initView();
    }

    private void initView() {
        resolveUrlList();
        resolvePlaySpeed();
        resolveMoreScaleTypeUI();
    }

    public boolean resolveBackPress() {
        if (drawerlayoutSourceLinks.isDrawerOpen(GravityCompat.END)) {
            drawerlayoutSourceLinks.closeDrawers();
            return true;
        } return false;
    }

    public void setUpUrl(String url, ArrayList<String> urlList) {
        if (urlList != null) {
            this.urlList = urlList;
            int index = urlList.indexOf(url);
            sourceLinkAdapter.getData().clear();
            for (int i = 0; i < urlList.size(); i++) {
                boolean isSelected = i == index;
                sourceLinkAdapter.getData().add(new KeyValueModel("(" + (i + 1) + ") " + urlList.get(i), urlList.get(i), isSelected));
            }
            sourceLinkAdapter.notifyDataSetChanged();
            if (index >= 0) {
                recyclerviewSourceLinks.smoothScrollToPosition(index);
                List<GSYVideoModel> list = new ArrayList<>();
                for (int i = 0; i < urlList.size(); i++) {
                    list.add(new GSYVideoModel(urlList.get(i), ""));
                }
                setUp(list, false, index);
            } else setUp(url, false, "");
        } else setUp(url, false, "");
    }

    private void resolveUrlList() {
        TextView mUrlList = findViewById(R.id.urlList);
        mUrlList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!drawerlayoutSourceLinks.isDrawerOpen(GravityCompat.END)) {
                    drawerlayoutSourceLinks.openDrawer(GravityCompat.END);
                } else drawerlayoutSourceLinks.closeDrawers();
            }
        });
        recyclerviewSourceLinks = findViewById(R.id.recyclerview_source_links);
        drawerlayoutSourceLinks = findViewById(R.id.drawerlayout_source_links);
        recyclerviewSourceLinks.setLayoutManager(new LinearLayoutManager(getContext()));
        sourceLinkAdapter = new BaseQuickAdapter<KeyValueModel, BaseViewHolder>(R.layout.item_link_selector, sourceList) {
            @Override
            protected void convert(final BaseViewHolder helper, final KeyValueModel item) {
                helper.setText(R.id.selector, item.getKey());
                helper.addOnClickListener(R.id.content);
                if (item.isSelected()) {
                    helper.setTextColor(R.id.selector, ColorUtil.getCustomColor(getContext(), R.styleable.BaseColor_com_assist_A));
                } else
                    helper.setTextColor(R.id.selector, ColorUtil.getCustomColor(getContext(), R.styleable.BaseColor_com_font_A));
            }
        };
        sourceLinkAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (drawerlayoutSourceLinks.isDrawerOpen(GravityCompat.END)) {
                    drawerlayoutSourceLinks.closeDrawers();
                }
                KeyValueModel keyValueModel = (KeyValueModel) adapter.getData().get(position);
                if (!keyValueModel.isSelected()) {
                    for (int i = 0; i < adapter.getData().size(); i++) {
                        ((KeyValueModel) adapter.getData().get(i)).setSelected(i == position);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerviewSourceLinks.smoothScrollToPosition(position);
                    setUp(mUriList, false, position);
                    startPlayLogic();
                }
            }
        });
        recyclerviewSourceLinks.setAdapter(sourceLinkAdapter);
    }

    /**
     * 显示比例
     * 注意，GSYVideoType.setShowType是全局静态生效，除非重启APP。
     */
    private void resolvePlaySpeed() {
        final TextView mPlaySpeed = findViewById(R.id.playSpeed);
        mPlaySpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSpeed == 1f) {
                    mSpeed = 1.5f;
                } else if (mSpeed == 1.5f) {
                    mSpeed = 2f;
                } else if (mSpeed == 2f) {
                    mSpeed = 0.5f;
                } else if (mSpeed == 0.5f) {
                    mSpeed = 1f;
                }
                mPlaySpeed.setText("速度:" + mSpeed);
                setSpeedPlaying(mSpeed, true);
            }
        });
    }

    /**
     * 显示比例
     * 注意，GSYVideoType.setShowType是全局静态生效，除非重启APP。
     */
    private void resolveMoreScaleTypeUI() {
        final TextView mMoreScale = findViewById(R.id.moreScale);
        //切换清晰度
        mMoreScale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHadPlay) {
                    return;
                }
                if (mType == 0) {
                    mType = 1;
                } else if (mType == 1) {
                    mType = 2;
                } else if (mType == 2) {
                    mType = 3;
                } else if (mType == 3) {
                    mType = 4;
                } else if (mType == 4) {
                    mType = 0;
                }
                if (!mHadPlay) {
                    return;
                }
                if (mType == 1) {
                    mMoreScale.setText(" 16 : 9 ");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);
                } else if (mType == 2) {
                    mMoreScale.setText(" 4 : 3 ");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
                } else if (mType == 3) {
                    mMoreScale.setText("比例全屏");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
                } else if (mType == 4) {
                    mMoreScale.setText(" 全屏 ");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
                } else if (mType == 0) {
                    mMoreScale.setText("默认比例");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
                }
                changeTextureViewShowType();
                if (mTextureView != null)
                    mTextureView.requestLayout();
            }
        });

    }

    @Override
    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position) {
        resolveUrl(url.get(position).getUrl());
        return super.setUp(url, cacheWithPlay, position);
    }

    @Override
    public boolean setUp(List<GSYVideoModel> url, boolean cacheWithPlay, int position, File cachePath) {
        resolveUrl(url.get(position).getUrl());
        return super.setUp(url, cacheWithPlay, position, cachePath);
    }

    @Override
    public boolean setUp(String url, boolean cacheWithPlay, String title) {
        resolveUrl(url);
        return super.setUp(url, cacheWithPlay, title);
    }

    @Override
    public boolean setUp(String url, boolean cacheWithPlay, File cachePath, String title) {
        resolveUrl(url);
        return super.setUp(url, cacheWithPlay, cachePath, title);
    }

    @Override
    public boolean setUp(String url, boolean cacheWithPlay, File cachePath, Map<String, String> mapHeadData, String title) {
        resolveUrl(url);
        return super.setUp(url, cacheWithPlay, cachePath, mapHeadData, title);
    }

    private void resolveUrl(String url) {
        if (url.startsWith("ftp") || url.startsWith("thunder") || url.startsWith("ed2k") || url.startsWith("magnet")) {
            DownloadManager.addNormalTask(getContext(), url, true, false, urlList);
        }
    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        if (sourceLinkAdapter.getData().size() > 0) {
            for (int i = 0; i < sourceLinkAdapter.getData().size(); i++) {
                sourceLinkAdapter.getData().get(i).setSelected(i == mPlayPosition);
                sourceLinkAdapter.notifyDataSetChanged();
                recyclerviewSourceLinks.smoothScrollToPosition(i);
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_list_player_contrller;
    }
}