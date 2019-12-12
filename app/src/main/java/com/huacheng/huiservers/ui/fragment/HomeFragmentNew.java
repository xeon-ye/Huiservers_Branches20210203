package com.huacheng.huiservers.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.coder.zzq.smartshow.toast.SmartToast;
import com.facebook.drawee.view.SimpleDraweeView;
import com.huacheng.huiservers.CommunityListActivity;
import com.huacheng.huiservers.R;
import com.huacheng.huiservers.http.okhttp.ApiHttpClient;
import com.huacheng.huiservers.http.okhttp.MyOkHttp;
import com.huacheng.huiservers.http.okhttp.response.JsonResponseHandler;
import com.huacheng.huiservers.model.ModelCoummnityList;
import com.huacheng.huiservers.model.ModelHome;
import com.huacheng.huiservers.model.ModelHomeCircle;
import com.huacheng.huiservers.model.ModelHomeIndex;
import com.huacheng.huiservers.model.ModelShopIndex;
import com.huacheng.huiservers.model.ModelVBaner;
import com.huacheng.huiservers.ui.base.BaseFragment;
import com.huacheng.huiservers.ui.circle.bean.CircleDetailBean;
import com.huacheng.huiservers.ui.fragment.adapter.HomeGridViewCateAdapter;
import com.huacheng.huiservers.ui.fragment.adapter.HomeIndexGoodsCommonAdapter;
import com.huacheng.huiservers.ui.fragment.adapter.VBannerAdapter;
import com.huacheng.huiservers.utils.MyCornerImageLoader;
import com.huacheng.huiservers.utils.SharePrefrenceUtil;
import com.huacheng.huiservers.view.widget.loadmorelistview.PagingListView;
import com.huacheng.libraryservice.utils.DeviceUtils;
import com.huacheng.libraryservice.utils.NullUtil;
import com.huacheng.libraryservice.utils.TDevice;
import com.huacheng.libraryservice.utils.fresco.FrescoUtils;
import com.huacheng.libraryservice.utils.json.JsonUtil;
import com.huacheng.libraryservice.widget.GridViewNoScroll;
import com.huacheng.libraryservice.widget.verticalbannerview.VerticalBannerView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.stx.xhb.xbanner.OnDoubleClickListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Description: 5.0.0新版本首页
 * created by wangxiaotao
 * 2019/10/16 0016 上午 10:12
 */
public class HomeFragmentNew extends BaseFragment implements HomeGridViewCateAdapter.OnClickItemListener, AMapLocationListener, PoiSearch.OnPoiSearchListener, HomeIndexGoodsCommonAdapter.OnClickCallback, View.OnClickListener {
    SharePrefrenceUtil prefrenceUtil;
    private ImageView iv_bg_title;//顶部背景图
    private View mStatusBar;

    private TextView tv_xiaoqu;
    private PagingListView listView;

    private ImageView iv_message;
    private ImageView iv_red;
    private ImageView iv_scancode;
    private View view_title_line;
    private SmartRefreshLayout refreshLayout;

    private Banner banner;
    private ImageView iv_bg_banner;//banner背景图
    private GridViewNoScroll gridview_home;
    private MyCornerImageLoader myImageLoader;

    //TODO 测试
    List<Integer> colors = new ArrayList<>();
    //头布局
    private View headerView;
    private int current_banner_position = 0 ;
    private float alpha;//用来计算滑动
    private HomeGridViewCateAdapter homeGridViewCateAdapter;
    private List<ModelHomeIndex> mcatelist=new ArrayList<>();
    private List<ModelVBaner> mDatas_v_banner = new ArrayList<>();//首页垂直banner数据公告
    private ImageView iv_bg_grid;//grid 背景图
    private ImageView iv_center;
    private LinearLayout ly_notice;
    private VerticalBannerView v_banner;
    private VBannerAdapter vBannerAdapter;
    private LinearLayout ll_zixun_container;

    private FrameLayout fl_grid_container;
    private LinearLayout ll_on_sale_container;
    private LinearLayout ll_on_sale_img_root;
    private LinearLayout ll_nearby_food_container;
    private LinearLayout ll_nearby_food_img_root;
    private LinearLayout ll_sec_kill_container;
    private LinearLayout ll_sec_kill_container_root;
    private LinearLayout ll_zixun_container_root;
    HomeIndexGoodsCommonAdapter adapter;
    private List<ModelShopIndex> mDatas=new ArrayList<>();//数据

    private RxPermissions rxPermissions;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private boolean isInitLocaion = false;

    private String location_provice="", location_district="", location_city="";//用户第一次使用时定位

    @Override
    public void initView(View view) {
        rxPermissions=new RxPermissions(mActivity);
        prefrenceUtil = new SharePrefrenceUtil(mActivity);
        iv_bg_title = view.findViewById(R.id.iv_bg_title);
        mStatusBar=view.findViewById(R.id.status_bar);
        mStatusBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TDevice.getStatuBarHeight(mActivity)));
        tv_xiaoqu = view.findViewById(R.id.tv_xiaoqu);
        tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName());
        iv_message = view.findViewById(R.id.iv_message);
        iv_red = view.findViewById(R.id.iv_red);
        iv_scancode = view.findViewById(R.id.iv_scancode);
        view_title_line=view.findViewById(R.id.view_title_line);
        view_title_line.setVisibility(View.GONE);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        //    recyclerView=view.findViewById(R.id.recyclerview);
        listView = view.findViewById(R.id.listView);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(true);

        initHeaderView();
        //TODO 测试
        adapter=new HomeIndexGoodsCommonAdapter(mContext, mDatas, this);
        listView.setAdapter(adapter);
        listView.setHasMoreItems(false);
        initLocation();
    }
    private void initLocation() {
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(mActivity);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置需要地理位置信息
            mLocationOption.isNeedAddress();
            //     mLocationOption.setOnceLocation(true);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            mLocationOption.setOnceLocation(true);

        }

    }
    /**
     * 头部的View
     */
    private void initHeaderView() {
        headerView = LayoutInflater.from(mActivity).inflate(R.layout.layout_fragmenthome_header1, null);
        //banner
       banner = headerView.findViewById(R.id.banner);
       setBanner();
       iv_bg_banner = headerView.findViewById(R.id.iv_bg_banner);
       //grid
       fl_grid_container = headerView.findViewById(R.id.fl_grid_container);
       gridview_home = headerView.findViewById(R.id.gridview_home);
       iv_bg_grid = headerView.findViewById(R.id.iv_bg_grid);
       //中部图片
       iv_center = headerView.findViewById(R.id.iv_center);
       //通知公告
       ly_notice = headerView.findViewById(R.id.ly_notice);
       v_banner = headerView.findViewById(R.id.v_banner);
        //特卖
        ll_on_sale_container = headerView.findViewById(R.id.ll_on_sale_container);
        TextView tv_more_sale = headerView.findViewById(R.id.tv_more_sale);
        ImageView tv_more_sale_arrow = headerView.findViewById(R.id.tv_more_sale_arrow);
        ll_on_sale_img_root = headerView.findViewById(R.id.ll_on_sale_img_root);

        //慧秒杀
        ll_sec_kill_container = headerView.findViewById(R.id.ll_sec_kill_container);
        TextView tv_sec_kill_more = headerView.findViewById(R.id.tv_sec_kill_more);
        ImageView tv_more_sec_kill_arrow = headerView.findViewById(R.id.tv_more_sec_kill_arrow);
        ll_sec_kill_container_root = headerView.findViewById(R.id.ll_sec_kill_container_root);
        //附近美食
        ll_nearby_food_container = headerView.findViewById(R.id.ll_nearby_food_container);
        TextView tv_more_nearby_food = headerView.findViewById(R.id.tv_more_nearby_food);
        ImageView tv_more_nearby_food_arrow = headerView.findViewById(R.id.tv_more_nearby_food_arrow);
        ll_nearby_food_img_root = headerView.findViewById(R.id.ll_nearby_food_img_root);
        //租售房
        ImageView iv_rent = headerView.findViewById(R.id.iv_rent);
        ImageView iv_sell = headerView.findViewById(R.id.iv_sell);
        ImageView iv_release_rent_sell = headerView.findViewById(R.id.iv_release_rent_sell);
        //资讯
        ll_zixun_container = headerView.findViewById(R.id.ll_zixun_container);
        ll_zixun_container_root = headerView.findViewById(R.id.ll_zixun_container_root);


        headerView.setVisibility(View.INVISIBLE);
        listView.addHeaderView(headerView);

    }
    private void setBanner() {
        myImageLoader= new MyCornerImageLoader();
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        banner.setImageLoader(myImageLoader);
        banner.isAutoPlay(true);//设置是否轮播
        banner.setIndicatorGravity(BannerConfig.CENTER);//小圆点位置
        banner.setDelayTime(4500);
        banner.setImageLoader(myImageLoader).setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                //TODO 点击banner
            }
        }).start();

        banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (alpha<1){
                    iv_bg_title.setBackgroundColor(getResources().getColor(colors.get(i)));
                }
                iv_bg_banner.setBackgroundColor(getResources().getColor(colors.get(i)));
                current_banner_position=i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });


    }

    @Override
    public void initIntentData() {

    }

    @Override
    public void initListener() {
        ly_notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 通知公告
            }
        });
        gridview_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO 点击分类导航
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {



            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    int scollYHeight = -headerView.getTop();
                    if (scollYHeight < DeviceUtils.dip2px(mContext, 400)) {
                        if (!v_banner.isStarted() && mDatas_v_banner.size() > 0) {
                            v_banner.start();
                        }

//                        if (!v_banner_zixun.isStarted() ) {
//                            v_banner_zixun.start();
//                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (headerView != null) {
                    //设置其透明度
                    alpha = 0;
                    //向上滑动的距离
                    int scollYHeight = -headerView.getTop();
                    if (scollYHeight >= DeviceUtils.dip2px(mActivity, 225) ) {
                        alpha = 1;//滑上去就一直显示
                    } else {
                        alpha = scollYHeight / ((DeviceUtils.dip2px(mActivity, 225) ) * 1.0f);
                    }
                    if (alpha ==1){
                        //TODO 滑上去了
                        iv_bg_title.setBackgroundColor(getResources().getColor(R.color.white));
                        view_title_line.setVisibility(View.VISIBLE);
                        tv_xiaoqu.setTextColor(getResources().getColor(R.color.title_color));
                    }else {
                        ///TODO 滑下来
                        if (colors.size()>0&&colors.size()>current_banner_position){
                            iv_bg_title.setBackgroundColor(getResources().getColor(colors.get(current_banner_position)));
                            view_title_line.setVisibility(View.GONE);
                        }
                        tv_xiaoqu.setTextColor(getResources().getColor(R.color.white));
                    }

                }
            }
        });

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestData();
            }
        });
        listView.setPagingableListener(new PagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {

            }
        });

        tv_xiaoqu.setOnClickListener(this);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        if (!NullUtil.isStringEmpty(prefrenceUtil.getXiaoQuName())){
            //1.小区名字不为空的情况
            tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName()+"");
            if (NullUtil.isStringEmpty(prefrenceUtil.getProvince_cn())){
                //两种情况 旧的线上用户覆盖安装肯定没有省市区
                prefrenceUtil.setProvince_cn("山西省");
                prefrenceUtil.setCity_cn("晋中市");
                prefrenceUtil.setRegion_cn("榆次区");
            }else {
                //新用户肯定有省市区也有名字
            }
            showDialog(smallDialog);
            requestData();
        }else {
            //2.小区名字为空的情况(第一次进来)
            requestLocationPermission();
        }

    }
    /**
     * 请求
     */
    private void requestLocationPermission() {
        rxPermissions.request( Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isGranted) throws Exception {
                        if (isGranted) {
                            //权限同意 ,开始定位
                            showDialog(smallDialog);
                            smallDialog.setTipTextView("定位中...");
                            mlocationClient.startLocation();

                        } else {
                            //权限拒绝 ,默认智慧小区
                            prefrenceUtil.clearPreference(mActivity);
                            prefrenceUtil.setXiaoQuName("智慧小区");
                            prefrenceUtil.setProvince_cn("山西省");
                            prefrenceUtil.setCity_cn("晋中市");
                            prefrenceUtil.setRegion_cn("榆次区");
                            tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName()+"");
                            showDialog(smallDialog);
                            requestData();

                        }
                    }
                });
    }

    /**
     * 请求数据
     */
    private void requestData() {
        HashMap<String, String> params = new HashMap<>();
        // 小区id 要判断
        if (!NullUtil.isStringEmpty(prefrenceUtil.getXiaoQuId())){
            params.put("c_id", prefrenceUtil.getXiaoQuId());
        }
        if (!NullUtil.isStringEmpty(prefrenceUtil.getProvince_cn())){
            params.put("province_cn", prefrenceUtil.getProvince_cn());
            params.put("city_cn", prefrenceUtil.getCity_cn());
            params.put("region_cn", prefrenceUtil.getRegion_cn());
        }
        MyOkHttp.get().post(ApiHttpClient.INDEX, params, new JsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                hideDialog(smallDialog);
                refreshLayout.finishRefresh();
                if (JsonUtil.getInstance().isSuccess(response)) {

                    ModelHome modelHome = (ModelHome) JsonUtil.getInstance().parseJsonFromResponse(response, ModelHome.class);
                    getIndexData(modelHome);
                } else {
                    try {
                        SmartToast.showInfo(response.getString("msg"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, String error_msg) {
                if (refreshLayout != null) {
                    refreshLayout.finishRefresh(false);
                }
                hideDialog(smallDialog);
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        });
    }

    /**
     * 获取首页的数据
     * @param modelHome
     */
    private void getIndexData(ModelHome modelHome) {
        if (modelHome!=null){
            headerView.setVisibility(View.VISIBLE);
            //banner
            colors.clear();
            if (modelHome.getAd_top_list() != null && modelHome.getAd_top_list().size() > 0) {

                ArrayList<String> mDatas_img1 = new ArrayList<>();
                for (int i = 0; i < modelHome.getAd_top_list().size(); i++) {
                    mDatas_img1.add(ApiHttpClient.IMG_URL+modelHome.getAd_top_list().get(i).getImg() + "");
                    //TODO 测试
                    if (i%2==0){
                        colors.add(R.color.gray);
                    }else {
                        colors.add(R.color.red_ed);
                    }
                }
                banner.update(mDatas_img1);
            }

            //分类导航
            if (modelHome.getMenu_list() != null && modelHome.getMenu_list().size() > 0) {
                fl_grid_container.setVisibility(View.VISIBLE);
                if (homeGridViewCateAdapter==null){
                    for (int i = 0; i < modelHome.getMenu_list().size(); i++) {
                        if (i>7) {
                            break;
                        }
                        mcatelist.add(modelHome.getMenu_list().get(i));
                    }

                    homeGridViewCateAdapter = new HomeGridViewCateAdapter(mActivity, mcatelist,1,this);
                    gridview_home.setAdapter(homeGridViewCateAdapter);
                }else {
                    mcatelist.clear();
                    for (int i = 0; i < modelHome.getMenu_list().size(); i++) {
                        if (i>7) {
                            break;
                        }
                        mcatelist.add(modelHome.getMenu_list().get(i));
                    }
                    homeGridViewCateAdapter.notifyDataSetChanged();
                }
            }else {
                fl_grid_container.setVisibility(View.GONE);
            }
            //grid背景图
          //  iv_bg_grid.setBackgroundColor(getResources().getColor(R.color.blue));
            iv_center.setImageResource(R.mipmap.bg_charge_banner);


            //通知公告
            if (modelHome.getP_social_list() != null && modelHome.getP_social_list().size() > 0) {
                ly_notice.setVisibility(View.VISIBLE);
                mDatas_v_banner.clear();
                mDatas_v_banner.addAll(modelHome.getP_social_list());
                if (!v_banner.isStarted()) {
                    vBannerAdapter = new VBannerAdapter(mDatas_v_banner);
                    v_banner.setAdapter(vBannerAdapter);
                    v_banner.start();
                } else {
                    if (vBannerAdapter!=null) {
                        vBannerAdapter.notifyDataChanged();
                    }
                    v_banner.stop();
                    v_banner.start();
                }
            } else {
                ly_notice.setVisibility(View.GONE);
            }
            //特卖专场
            ll_on_sale_container.setVisibility(View.VISIBLE);
            ll_on_sale_img_root.removeAllViews();
            for (int i = 0; i <7; i++) {
                View item_home_on_sale = LayoutInflater.from(mActivity).inflate(R.layout.item_home_on_sale, null);
                SimpleDraweeView sdv_on_sale = item_home_on_sale.findViewById(R.id.sdv_on_sale);
                ll_on_sale_img_root.addView(item_home_on_sale);
            }
            //慧秒杀
            ll_sec_kill_container.setVisibility(View.VISIBLE);
            ll_sec_kill_container_root.removeAllViews();
            for (int i = 0; i <6; i++) {
                View item_home_sec_kill = LayoutInflater.from(mActivity).inflate(R.layout.item_home_sec_kill, null);
                LinearLayout ly_onclick = item_home_sec_kill.findViewById(R.id.ly_onclick);
                ly_onclick.setOnClickListener(new OnDoubleClickListener() {
                    @Override
                    public void onNoDoubleClick(View v) {

                    }
                });
                SimpleDraweeView sdv_sec_kill = item_home_sec_kill.findViewById(R.id.sdv_sec_kill);
                TextView tv_title = item_home_sec_kill.findViewById(R.id.tv_title);
                TextView tv_sub_title = item_home_sec_kill.findViewById(R.id.tv_sub_title);
                TextView tv_shop_price = item_home_sec_kill.findViewById(R.id.tv_shop_price);
                TextView tv_shop_price_original = item_home_sec_kill.findViewById(R.id.tv_shop_price_original);
                tv_shop_price_original.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                ll_sec_kill_container_root.addView(item_home_sec_kill);
            }

            //附近美食
            ll_nearby_food_container.setVisibility(View.VISIBLE);
            ll_nearby_food_img_root.removeAllViews();
            for (int i = 0; i <7; i++) {
                View item_home_nearby_food = LayoutInflater.from(mActivity).inflate(R.layout.item_home_nearby_food, null);
                SimpleDraweeView sdv_nearby_food = item_home_nearby_food.findViewById(R.id.sdv_nearby_food);
                TextView tv_nearby_food_name = item_home_nearby_food.findViewById(R.id.tv_nearby_food_name);
                TextView tv_nearby_food_price = item_home_nearby_food.findViewById(R.id.tv_nearby_food_price);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0,0,DeviceUtils.dip2px(mActivity,10),0);
                ll_nearby_food_img_root.addView(item_home_nearby_food,params);
            }
            //租售房服务

            //热门资讯
            ll_zixun_container.setVisibility(View.VISIBLE);
            if (modelHome.getSocial_list() != null&&modelHome.getSocial_list().size()>0) {
                ll_zixun_container.setVisibility(View.VISIBLE);
                ll_zixun_container_root.removeAllViews();
                for (int i = 0; i < modelHome.getSocial_list().size(); i++) {
                    View item_home_circle = LayoutInflater.from(mActivity).inflate(R.layout.item_home_circle, null);
                    TextView tv_title = item_home_circle.findViewById(R.id.tv_title);
                    TextView tv_content = item_home_circle.findViewById(R.id.tv_content);
                    SimpleDraweeView sdv_circle = item_home_circle.findViewById(R.id.sdv_circle);
                    TextView tv_circle_name = item_home_circle.findViewById(R.id.tv_circle_name);
                    TextView tv_read_count = item_home_circle.findViewById(R.id.tv_read_count);
                    TextView tv_time = item_home_circle.findViewById(R.id.tv_time);
                    TextView tv_more_circle = item_home_circle.findViewById(R.id.tv_more_circle);
                    ImageView tv_more_circle_arrow = item_home_circle.findViewById(R.id.tv_more_circle_arrow);
                    tv_more_circle.setOnClickListener(new OnDoubleClickListener() {
                        @Override
                        public void onNoDoubleClick(View v) {
                            //查看更多
                        }
                    });
                    tv_more_circle_arrow.setOnClickListener(new OnDoubleClickListener() {
                        @Override
                        public void onNoDoubleClick(View v) {
                            //查看更多
                        }
                    });
                    ll_zixun_container_root.addView(item_home_circle);
                    //显示
                    final ModelHomeCircle modelHomeCircle = modelHome.getSocial_list().get(i);
                        tv_title.setText(modelHomeCircle.getTitle()+"");
                        byte[] bytes2 = Base64.decode(modelHomeCircle.getList().getTitle(), Base64.DEFAULT);
                        tv_content.setText(new String(bytes2));
                        if (modelHomeCircle.getList().getImg_list() != null && modelHomeCircle.getList().getImg_list().size() > 0) {
                            if (!TextUtils.isEmpty(modelHomeCircle.getList().getImg_list().get(0).getImg())) {

                                FrescoUtils.getInstance().setImageUri(sdv_circle,ApiHttpClient.IMG_URL + modelHomeCircle.getList().getImg_list().get(0).getImg());
                                sdv_circle.setVisibility(View.VISIBLE);
                            } else {
                                sdv_circle.setVisibility(View.GONE);
                            }
                        } else {
                            sdv_circle.setVisibility(View.GONE);
                        }
                        tv_circle_name.setText(modelHomeCircle.getList().getC_name()+"");
                        tv_read_count.setText(modelHomeCircle.getList().getClick()+"阅读");
                        tv_time.setText(modelHomeCircle.getList().getAddtime()+"");
                }
            }else {
                ll_zixun_container.setVisibility(View.GONE);
            }
            //TODO
            //底部商品信息
            if (modelHome.getPro_list() != null && modelHome.getPro_list().size() > 0) {
                mDatas.clear();
                mDatas.addAll(modelHome.getPro_list());
                listView.setHasMoreItems(false);
            }
        }

    }


    @Override
    public void onLocationChanged(AMapLocation location) {
        if (null != location) {
            StringBuffer sb = new StringBuffer();
            //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
            if (location.getErrorCode() == 0) {
                sb.append("定位成功" + "\n");
                sb.append("定位类型: " + location.getLocationType() + "\n");
                sb.append("经    度    : " + location.getLongitude() + "\n");
                sb.append("纬    度    : " + location.getLatitude() + "\n");
                sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                sb.append("提供者    : " + location.getProvider() + "\n");

                sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                sb.append("角    度    : " + location.getBearing() + "\n");
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : " + location.getSatellites() + "\n");
                sb.append("国    家    : " + location.getCountry() + "\n");
                sb.append("省            : " + location.getProvince() + "\n");
                sb.append("市            : " + location.getCity() + "\n");
                sb.append("城市编码 : " + location.getCityCode() + "\n");
                sb.append("区            : " + location.getDistrict() + "\n");
                sb.append("区域 码   : " + location.getAdCode() + "\n");
                sb.append("地    址    : " + location.getAddress() + "\n");
                sb.append("兴趣点    : " + location.getPoiName() + "\n");

                /*sharePrefrenceUtil.setLongitude(location.getLongitude() + "");
                sharePrefrenceUtil.setAtitude(location.getLatitude() + "");*/

                //定位完成的时间
                //  sb.append("定位时间: " + Utils.formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
                if (!isInitLocaion) {
                    if (NullUtil.isStringEmpty(location.getDistrict())){
//                        hideDialog(smallDialog);
//                        // tvResult.setText("定位失败，loc is null");
//                        text_city.setText("定位失败...点击重新定位");
//                        text_city.setOnClickListener(new OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                startLocation();
//                            }
//                        });
                    }else {
                        isInitLocaion = true;
                        //默认选中
                        mlocationClient.stopLocation();
                        location_provice = location.getProvince() + "";
                        location_city = location.getCity() + "";
                        location_district = location.getDistrict() + "";

                        getPOIsearch(location.getLongitude(),location.getLatitude());

                    }
                }
            } else {
                //定位失败
                sb.append("定位失败" + "\n");
                sb.append("错误码:" + location.getErrorCode() + "\n");
                sb.append("错误信息:" + location.getErrorInfo() + "\n");
                sb.append("错误描述:" + location.getLocationDetail() + "\n");
                hideDialog(smallDialog);
                // tvResult.setText("定位失败，loc is null");
                //定位失败 显示智慧小区
                prefrenceUtil.clearPreference(mActivity);
                prefrenceUtil.setXiaoQuName("智慧小区");
                prefrenceUtil.setProvince_cn("山西省");
                prefrenceUtil.setCity_cn("晋中市");
                prefrenceUtil.setRegion_cn("榆次区");
                tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName()+"");
                showDialog(smallDialog);
                requestData();
            }

        } else {
            hideDialog(smallDialog);
            // tvResult.setText("定位失败，loc is null");
            //定位失败 显示智慧小区
            prefrenceUtil.clearPreference(mActivity);
            prefrenceUtil.setXiaoQuName("智慧小区");
            prefrenceUtil.setProvince_cn("山西省");
            prefrenceUtil.setCity_cn("晋中市");
            prefrenceUtil.setRegion_cn("榆次区");
            tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName()+"");
            showDialog(smallDialog);
            requestData();
        }
    }

    private String[] SetTime(long time) {
       long mDay = time / (1000 * 60 * 60 * 24);
        long mHour = (time - mDay * (1000 * 60 * 60 * 24))
                / (1000 * 60 * 60);
        long mMin = (time - mDay * (1000 * 60 * 60 * 24) - mHour
                * (1000 * 60 * 60))
                / (1000 * 60);
        long  mSecond = (time - mDay * (1000 * 60 * 60 * 24) - mHour
                * (1000 * 60 * 60) - mMin * (1000 * 60))
                / 1000;
        String[] str = new String[4];
        str[0] = mDay + "";
        str[1] = mHour + "";
        str[2] = mMin + "";
        str[3] = mSecond + "";
        return str;
    }

    private String fillZero(String time) {
        String timeStr = "";
        if (time.length() == 1)
            return "0" + time;
        else
            return time;
    }
    @Override
    public int getLayoutId() {
        return R.layout.fragment_home_new;
    }

    //如果你需要考虑更好的体验，可以这么操作
    @Override
    public void onStart() {
        super.onStart();
        //开始轮播
        banner.startAutoPlay();

    }

    @Override
    public void onStop() {
        super.onStop();
        //结束轮播
        banner.stopAutoPlay();
    }



    @Override
    public void onClickImg(View v, int position, int type) {
        //不做操作
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
        mLocationOption = null;

        super.onDestroy();
    }
    /**
     * 调用高德地图搜索周边住宅
     * @param longitude
     * @param latitude
     */
    private void getPOIsearch(double longitude, double latitude) {
        PoiSearch.Query query = new PoiSearch.Query("", "商务住宅", "");
        query.setPageSize(15);
        PoiSearch search = new PoiSearch(mActivity, query);
        search.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude, longitude), 10000));
        search.setOnPoiSearchListener(this);
        search.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult result, int i) {
        PoiSearch.Query query = result.getQuery();
        ArrayList<PoiItem> pois = result.getPois();

        if (pois!=null&&pois.size()>0){

            String community_name=pois.get(0).toString();
            String address = pois.get(0).getSnippet();
            //    prefrenceUtil.clearPreference(mActivity);
            // 选择上了小区
            //TODO 这里要进行匹配
//            prefrenceUtil.setXiaoQuName(community_name);
//            prefrenceUtil.setAddressName(address);
//            tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName()+"");
//            showDialog(smallDialog);
//            smallDialog.setTipTextView("加载中...");
//            requestData();
            ModelCoummnityList modelCoummnityList = new ModelCoummnityList();
            modelCoummnityList.setName(community_name);
            modelCoummnityList.setAddress(address);
            requestCommunityId(modelCoummnityList);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * 根据小区名字请求小区id
     * @param item
     */
    private void requestCommunityId(final ModelCoummnityList item) {
        showDialog(smallDialog);
        smallDialog.setTipTextView("加载中...");
        HashMap<String, String> params = new HashMap<>();
        if (!NullUtil.isStringEmpty(item.getName())){
            params.put("community_name",item.getName()+"");
        }
        MyOkHttp.get().post(ApiHttpClient.GET_COMMUNITY_ID, params, new JsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                //  hideDialog(smallDialog);
                if (JsonUtil.getInstance().isSuccess(response)) {
                    try {
                        if (response.has("data")){
                            //匹配成功
                            JSONObject data = response.getJSONObject("data");
                            prefrenceUtil.clearPreference(mContext);
                            if (!NullUtil.isStringEmpty(data.getString("id"))){
                                prefrenceUtil.setXiaoQuId(data.getString("id"));
                                prefrenceUtil.setCompanyId(data.getString("company_id"));
                            }
                            prefrenceUtil.setXiaoQuName(item.getName());
                            prefrenceUtil.setAddressName(item.getAddress());
                            //保存省市区
                            prefrenceUtil.setProvince_cn(location_provice);
                            prefrenceUtil.setCity_cn(location_city);
                            prefrenceUtil.setRegion_cn(location_district);
                            tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName()+"");
                            showDialog(smallDialog);
                            smallDialog.setTipTextView("加载中...");
                            requestData();
                            getsubmitCommunityId(data.getString("id"));
                        }else {
                            //匹配失败
                            prefrenceUtil.clearPreference(mContext);
                            prefrenceUtil.setXiaoQuId("");
                            prefrenceUtil.setXiaoQuName(item.getName());
                            prefrenceUtil.setAddressName(item.getAddress());
                            //保存省市区
                            prefrenceUtil.setProvince_cn(location_provice);
                            prefrenceUtil.setCity_cn(location_city);
                            prefrenceUtil.setRegion_cn(location_district);
                            tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName()+"");
                            showDialog(smallDialog);
                            smallDialog.setTipTextView("加载中...");
                            requestData();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //匹配失败
                        //  data==null
                        prefrenceUtil.clearPreference(mContext);
                        prefrenceUtil.setXiaoQuId("");
                        prefrenceUtil.setXiaoQuName(item.getName());
                        prefrenceUtil.setAddressName(item.getAddress());
                        //保存省市区
                        prefrenceUtil.setProvince_cn(location_provice);
                        prefrenceUtil.setCity_cn(location_city);
                        prefrenceUtil.setRegion_cn(location_district);
                        tv_xiaoqu.setText(prefrenceUtil.getXiaoQuName()+"");
                        showDialog(smallDialog);
                        smallDialog.setTipTextView("加载中...");
                        requestData();
                    }

                } else {
                    try {
                        SmartToast.showInfo(response.getString("msg"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, String error_msg) {

                hideDialog(smallDialog);
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        });

    }

    //提交小区id
    private void getsubmitCommunityId(String community_id) {
        HashMap<String, String> params = new HashMap<>();
        params.put("community_id", community_id+"");

        MyOkHttp.get().post(ApiHttpClient.SELECT_COMMUNITY, params, new JsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {

            }

            @Override
            public void onFailure(int statusCode, String error_msg) {
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        });
    }

    /**
     * 添加和删除评论的Eventbus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateItem(CircleDetailBean mCirclebean) {
//        try {
//            if (mCirclebean != null) {
//                int type = mCirclebean.getType();
//                if (type == 0) {
//                    //添加评论
//                    if (mCirclebean.getId().equals(mSocial.getId())) {
//                        int i1 = Integer.parseInt(mCirclebean.getReply_num());
//                        mSocial.setReply_num((i1 + 1) + "");
//                        tv_circleReply.setText(mSocial.getReply_num());
//                    }
//                } else if (type == 1) {
//                    //删除评论
//                    if (mCirclebean.getId().equals(mSocial.getId())) {
//                        int i1 = Integer.parseInt(mCirclebean.getReply_num());
//                        mSocial.setReply_num((i1 - 1) + "");
//                        tv_circleReply.setText(mSocial.getReply_num());
//                    }
//                } else if (type == 2) {
//                    //阅读数
//                    if (mCirclebean.getId().equals(mSocial.getId())) {
//                        tv_circleViews.setText(mCirclebean.getClick());
//
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onClickImage(int position) {
        //TODO 点击下方商品图片
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_xiaoqu:
                Intent intent1 = new Intent(getActivity(), CommunityListActivity.class);
                startActivityForResult(intent1, 111);
                break;
                default:
                    break;
        }
    }
}