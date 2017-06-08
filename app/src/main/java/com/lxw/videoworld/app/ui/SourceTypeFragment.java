package com.lxw.videoworld.app.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lxw.videoworld.R;
import com.lxw.videoworld.app.api.HttpHelper;
import com.lxw.videoworld.app.model.SourceDetailModel;
import com.lxw.videoworld.app.model.SourceListModel;
import com.lxw.videoworld.framework.base.BaseActivity;
import com.lxw.videoworld.framework.http.BaseResponse;
import com.lxw.videoworld.framework.http.HttpManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 资源列表
 */
public class SourceTypeFragment extends Fragment {
    @BindView(R.id.recyclerview_source_type)
    RecyclerView recyclerviewSourceType;
    Unbinder unbinder;
    @BindView(R.id.refresh_source_type)
    SwipeRefreshLayout refreshSourceType;
    private View rootView;
    private SourceListModel sourceListModel;
    private List<SourceDetailModel> sourceDetails = new ArrayList<>();
    private BaseQuickAdapter<SourceDetailModel, BaseViewHolder> sourceAdapter;
    private String category;
    private String type;
    private final int BANNER_LIMIT = 5;
    private final int LIST_LIMIT = 9;
    private boolean frag_refresh = true;
    private int page = 0;

    public SourceTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        category = getArguments().getString("category");
        type = getArguments().getString("type");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null && !TextUtils.isEmpty(category) && !TextUtils.isEmpty(type)) {
            rootView = inflater.inflate(R.layout.fragment_source_type, null);
            ButterKnife.bind(this, rootView);
            // 下拉刷新
            refreshSourceType.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // TODO
                    page = 0;
                    frag_refresh = true;
                    sourceAdapter.setEnableLoadMore(true);
                    refreshSourceType.setRefreshing(false);
                    // 加载数据
                    getList(category, type, "0", LIST_LIMIT + BANNER_LIMIT + "");

                }
            });
            // 列表适配器
            sourceAdapter = new BaseQuickAdapter<SourceDetailModel, BaseViewHolder>(R.layout., sourceDetails) {
                @Override
                protected void convert(BaseViewHolder helper, SourceDetailModel item) {

                }
            };
            // item 点击事件
            sourceAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    // TODO

                }
            });
            // 加载更多
            sourceAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
                @Override
                public void onLoadMoreRequested() {
                    recyclerviewSourceType.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // TODO
                            frag_refresh = false;
                            // 加载数据
                            getList(category, type, LIST_LIMIT * page + BANNER_LIMIT + "", LIST_LIMIT + "");
                        }

                    }, 500);
                }
            }, recyclerviewSourceType);

            recyclerviewSourceType.setAdapter(sourceAdapter);
            // 加载数据
            getList(category, type, "0", LIST_LIMIT + BANNER_LIMIT + "");
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    public void getList(String category, String type, String start, String limit) {
        new HttpManager<SourceListModel>((BaseActivity) SourceTypeFragment.this.getActivity(), HttpHelper.getInstance().getList(category, type, start, limit)) {

            @Override
            public void onSuccess(BaseResponse<SourceListModel> response) {
                sourceListModel = response.getResult();

                SourceTypeFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (sourceListModel != null && sourceListModel.getList() != null) {
                            page++;
                            sourceAdapter.addData(sourceListModel.getList());
                            sourceAdapter.loadMoreComplete();
                        } else {
                            sourceAdapter.loadMoreFail();
                        }
                    }

                });
            }

            @Override
            public void onFailure(BaseResponse<SourceListModel> response) {
                SourceTypeFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        sourceAdapter.loadMoreFail();
                    }

                });
            }
        }.doRequest();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}