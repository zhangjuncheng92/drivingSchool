package com.zjc.drivingschool.ui.apply;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.mobo.mobolibrary.ui.base.ZBaseFragment;
import com.mobo.mobolibrary.ui.base.adapter.ZBaseRecyclerViewAdapter;
import com.mobo.mobolibrary.ui.divideritem.HorizontalDividerItemDecoration;
import com.zjc.drivingschool.R;
import com.zjc.drivingschool.api.ApiHttpClient;
import com.zjc.drivingschool.api.ResultResponseHandler;
import com.zjc.drivingschool.db.SharePreferences.SharePreferencesUtil;
import com.zjc.drivingschool.db.model.OrderItem;
import com.zjc.drivingschool.db.parser.OrderListResponseParser;
import com.zjc.drivingschool.db.response.OrderListResponse;
import com.zjc.drivingschool.eventbus.StudyOrderCancelEvent;
import com.zjc.drivingschool.ui.apply.adapter.ApplyOrderAdapter;
import com.zjc.drivingschool.utils.Constants;
import com.zjc.drivingschool.utils.ConstantsParams;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/8/17.
 */
public class ApplyListFragment extends ZBaseFragment implements SwipeRefreshLayout.OnRefreshListener, ZBaseRecyclerViewAdapter.OnLoadMoreListener, ZBaseRecyclerViewAdapter.OnItemClickListener {
    private String orderStatus;
    private EasyRecyclerView mRecyclerView;
    private ApplyOrderAdapter mAdapter;

    /**
     * 传入需要的参数，设置给arguments
     */
    public static ApplyListFragment newInstance(String bean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.ARGUMENT, bean);
        ApplyListFragment fragment = new ApplyListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            orderStatus = (String) bundle.getSerializable(Constants.ARGUMENT);
        }
    }

    @Override
    protected int inflateContentView() {
        return R.layout.order_manager_frg;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initEmptyLayout(rootView);
        initView();
        initAdapter();
        findOrders();
    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = (EasyRecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .colorResId(R.color.comm_divider)
                .sizeResId(R.dimen.comm_divider_line)
                .build());
        mRecyclerView.setRefreshListener(this);
    }

    private void initAdapter() {
        mAdapter = new ApplyOrderAdapter(getActivity());
        mAdapter.setOnItemClickLitener(this);
        mAdapter.setMore(R.layout.view_more, this);
        mAdapter.setNoMore(R.layout.view_nomore);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        //跳转到预约详情界面
        OrderItem orderItem = (OrderItem) mAdapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.ARGUMENT, orderItem.getOrid());
        startActivity(ApplyActivity.class, bundle);
    }

    @Override
    public void onRefresh() {
        ApiHttpClient.getInstance().startOrders(SharePreferencesUtil.getInstance().readUser().getUid(), ConstantsParams.PAGE_START, orderStatus, new ResultResponseHandler(getActivity(), mRecyclerView) {

            @Override
            public void onResultSuccess(String result) {
                OrderListResponse orderListResponse = new OrderListResponseParser().parseResultMessage(result);
                mAdapter.clear();
                mAdapter.addAll(orderListResponse.getOrderitems());
                isLoadFinish(orderListResponse.getOrderitems().size());
            }
        });
    }

    private void findOrders() {
        ApiHttpClient.getInstance().startOrders(SharePreferencesUtil.getInstance().readUser().getUid(), ConstantsParams.PAGE_START, orderStatus, new ResultResponseHandler(getActivity(), getEmptyLayout()) {

            @Override
            public void onResultSuccess(String result) {
                OrderListResponse orderListResponse = new OrderListResponseParser().parseResultMessage(result);
                mAdapter.addAll(orderListResponse.getOrderitems());
                isLoadFinish(orderListResponse.getOrderitems().size());
            }
        });
    }

    @Override
    public void onLoadMore() {
        int start = mAdapter.getCount();
        ApiHttpClient.getInstance().startOrders(SharePreferencesUtil.getInstance().readUser().getUid(), start, orderStatus, new ResultResponseHandler(getActivity()) {

            @Override
            public void onResultSuccess(String result) {
                OrderListResponse orderListResponse = new OrderListResponseParser().parseResultMessage(result);
                mAdapter.addAll(orderListResponse.getOrderitems());
                isLoadFinish(orderListResponse.getOrderitems().size());
            }
        });
    }

    /**
     * 加载完成
     */
    public boolean isLoadFinish(int size) {
        if (size < ConstantsParams.PAGE_SIZE) {
            mAdapter.stopMore();
            mAdapter.setNoMore(R.layout.view_nomore);
            return true;
        }
        return false;
    }

    /**
     * 更新预约单取消状态
     *
     * @param event
     */
    public void onEventMainThread(StudyOrderCancelEvent event) {
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            OrderItem item = (OrderItem) mAdapter.getItem(i);
            if (item.getOrid().equals(event.getOrid())) {
                item.setState(ConstantsParams.STUDY_ORDER_NINE);
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
