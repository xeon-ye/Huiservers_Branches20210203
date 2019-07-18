package com.huacheng.huiservers.pay.chinaums;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinaums.pppay.unify.UnifyPayListener;
import com.chinaums.pppay.unify.UnifyPayPlugin;
import com.chinaums.pppay.unify.UnifyPayRequest;
import com.coder.zzq.smartshow.toast.SmartToast;
import com.huacheng.huiservers.R;
import com.huacheng.huiservers.http.HttpHelper;
import com.huacheng.huiservers.http.Url_info;
import com.huacheng.huiservers.http.okhttp.RequestParams;
import com.huacheng.huiservers.model.EventBusWorkOrderModel;
import com.huacheng.huiservers.model.protocol.ShopProtocol;
import com.huacheng.huiservers.ui.base.BaseActivity;
import com.huacheng.huiservers.ui.center.ShopOrderListActivity;
import com.huacheng.huiservers.ui.center.geren.bean.PayTypeBean;
import com.huacheng.huiservers.ui.index.property.PropertyPaymentActivity;
import com.huacheng.huiservers.ui.index.property.bean.EventProperty;
import com.huacheng.huiservers.ui.servicenew.ui.order.FragmentOrderListActivity;
import com.huacheng.huiservers.ui.servicenew.ui.order.JpushPresenter;
import com.huacheng.libraryservice.utils.NullUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.unionpay.UPPayAssistEx;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Description: 银联统一支付页面
 * created by wangxiaotao
 * 2019/7/9 0009 上午 10:36
 */
public class UnifyPayActivity extends BaseActivity implements OnUnifyPayListener, UnifyPayListener {
    private UnifyPayPresenter payPresenter;
    private RelativeLayout rl_header;
    private TextView tv_price;
    private ListView listview;
    private View tv_confirm;
    private List<PayTypeBean> pay_type_datas = new ArrayList<PayTypeBean>();
    private List<PayTypeBean> pay_type_datas_clapse = new ArrayList<PayTypeBean>();//折叠的列表
    private AdapterUnifypay adapterUnifypay;
    private boolean isShowClapse;
    private View footerView;

    private int typetag = 1;
    /**
     * 微信支付
     */
    private final int TYPE_WEIXIN = 3;
    /**
     * 支付宝支付
     */
    private final int TYPE_ALIPAY = 1;
    /**
     * 云闪付
     */
    private final int TYPE_CLOUD_QUICK_PAY = 2;

    private String order_id = "";
    private String type = "";
    private String price = "";

    private boolean isGotoPayback = false;
    private RxPermissions rxPermissions;
    private int request_count=0;

    @Override
    protected void initIntentData() {
        order_id = this.getIntent().getExtras().getString("o_id") + "";
        type = this.getIntent().getExtras().getString("type") + "";
        price = this.getIntent().getExtras().getString("price");
    }

    @Override
    protected void initView() {
        findTitleViews();
        titleName.setText("支付订单");
        rl_header = findViewById(R.id.rl_header);
        tv_price = findViewById(R.id.tv_price);
        tv_price.setText(price + "");
        listview = findViewById(R.id.listview);
        footerView = LayoutInflater.from(this).inflate(R.layout.unify_pay_footer, null);
        footerView.setVisibility(View.INVISIBLE);
        listview.addFooterView(footerView);
        tv_confirm = findViewById(R.id.tv_confirm);
        adapterUnifypay = new AdapterUnifypay(this, R.layout.item_unify_pay, pay_type_datas);
        listview.setAdapter(adapterUnifypay);

        UnifyPayPlugin.getInstance(this).setListener(this);

        rxPermissions = new RxPermissions(this);
        requestPermissions();
    }

    private void requestPermissions() {
        rxPermissions.request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isGranted) throws Exception {
                        if (isGranted) {
                        } else {

                        }
                    }
                });
    }

    @Override
    protected void initData() {
        payPresenter = new UnifyPayPresenter(this, this);
        //1.在点击事件中首先先处理
        showDialog(smallDialog);
        payPresenter.getPayType();
    }

    @Override
    protected void initListener() {
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rxPermissions.request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean isGranted) throws Exception {
                                if (isGranted) {
                                    comfirmPay();
                                } else {

                                }
                            }
                        });
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id < 0) {
                    return;
                }
                for (int i = 0; i < pay_type_datas.size(); i++) {
                    if (position == i) {
                        pay_type_datas.get(i).setSelected(true);
                    } else {
                        pay_type_datas.get(i).setSelected(false);
                    }
                }
                adapterUnifypay.notifyDataSetChanged();
                String type_name = pay_type_datas.get(position).getTypename();

                if ("wxpay".equals(type_name)) {
                    //微信
                    typetag = TYPE_WEIXIN;
                } else if ("unionpay".equals(type_name)) {
                    //云闪付
                    typetag = TYPE_CLOUD_QUICK_PAY;
                } else if ("alipay".equals(type_name)) {
                    //支付宝
                    typetag = TYPE_ALIPAY;
                }
            }
        });
    }

    private void comfirmPay() {
        if (pay_type_datas.size() == 0) {
            return;
        }
        String typename = "alipay";
        if (typetag == TYPE_ALIPAY) {
            typename = "alipay";
        } else if (typetag == TYPE_CLOUD_QUICK_PAY) {
            typename = "unionpay";
        } else if (typetag == TYPE_WEIXIN) {
            typename = "wxpay";
        }
        showDialog(smallDialog);
        payPresenter.getOrderInformation(order_id,typename,typetag,type);
       // payPresenter.getOrderInformation("11114", typename, typetag);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGotoPayback){
            isGotoPayback=false;
            if (typetag==TYPE_ALIPAY){
                //注：支付宝渠道如果支付请求发送成功，则会跳转至支付宝APP，
                // 并且支付完成后会停留在支付宝，因此商户 APP无法通过UnifyPayListener
                // 收到支付结果，请以后台的支付结果为准。
                payResultCallBack();
            }

        }
    }


    /**
     * 回调接口
     */
    private void payResultCallBack() {
        if (payPresenter!=null){
            String type_params = "";
            if (type.equals(CanstantPay.PAY_SHOP_CONFIRM_ORDER)||type.equals(CanstantPay.PAY_SHOP_ORDER_DETAIL)){
                type_params="shop";
            }else if(type.equals(CanstantPay.PAY_SERVICE)){
                type_params="serve";
            }else if(type.equals(CanstantPay.PAY_PROPERTY)) {
                type_params="property";
            }else if(type.equals(CanstantPay.PAY_WORKORDER)){
                type_params="work";
            }else if(type.equals(CanstantPay.PAY_HUODONG)){
                type_params="activity";
            }else if(type.equals(CanstantPay.PAY_FACE)){
                type_params="face";
            }
            showDialog(smallDialog);
            payPresenter.confirmOrderPayment(order_id,type_params);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_unify_pay;
    }


    @Override
    protected int getFragmentCotainerId() {
        return 0;
    }

    @Override
    protected void initFragment() {

    }


    @Override
    public void onGetPayTypeDatas(int status, String msg, List<PayTypeBean> mDatas) {
        //获取到支付方式的接口
        hideDialog(smallDialog);
        if (status == 1) {
            rl_header.setVisibility(View.VISIBLE);
            if (mDatas != null && mDatas.size() > 0) {
                for (int i = 0; i < mDatas.size(); i++) {
                    if (i == 0) {
                        mDatas.get(i).setSelected(true);
                    }
                    if (mDatas.get(i).getObvious() == 0) {//不折叠
                        pay_type_datas.add(mDatas.get(i));
                    } else {
                        isShowClapse = true;
                        pay_type_datas_clapse.add(mDatas.get(i));//折叠的列表
                    }
                }
                adapterUnifypay.notifyDataSetChanged();
                String typename = mDatas.get(0).getTypename();
                if ("wxpay".equals(typename)) {
                    //微信
                    typetag = TYPE_WEIXIN;
                } else if ("unionpay".equals(typename)) {
                    //云闪付
                    typetag = TYPE_CLOUD_QUICK_PAY;
                } else if ("alipay".equals(typename)) {
                    //支付宝
                    typetag = TYPE_ALIPAY;
                }
                if (isShowClapse) {
                    footerView.setVisibility(View.VISIBLE);
                    footerView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pay_type_datas.addAll(pay_type_datas_clapse);
                            adapterUnifypay.notifyDataSetChanged();
                            isShowClapse = false;
                            footerView.setVisibility(View.GONE);
                        }
                    });
                } else {
                    footerView.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            SmartToast.showInfo(msg);
        }
    }

    @Override
    public void onGetOrderInformation(int status, String msg, String json, int typetag) {
        //获取到订单数据
        hideDialog(smallDialog);
        if (status == 1) {
            if (!NullUtil.isStringEmpty(json)) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String status_string = jsonObject.getString("errCode");
                    if ("SUCCESS".equalsIgnoreCase(status_string)) {// 成功
                        if (jsonObject.has("appPayRequest")) {
                            String appPayRequest = jsonObject.getString("appPayRequest");
                            if (typetag == TYPE_ALIPAY) {
                             //   SmartToast.showInfo("alipay");
                                payAliPay(appPayRequest);
                            } else if (typetag == TYPE_CLOUD_QUICK_PAY) {
                                SmartToast.showInfo("unionpay");
                                payCloudQuickPay(appPayRequest);
                            } else if (typetag == TYPE_WEIXIN) {
                                SmartToast.showInfo("wxpay");
                                    //todo 不知道微信需不需要
//                                msgApi = WXAPIFactory.createWXAPI(ZhifuActivity.this, mTypeBeen.get(i).getApp_id(), true);
//                                // 将该app注册到微信
//                                msgApi.registerApp(mTypeBeen.get(i).getApp_id());
                                payWX(appPayRequest);
                            }
                        }
                    } else {//失败
                        String errMsg = jsonObject.getString("errMsg");
                        SmartToast.showInfo(errMsg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    SmartToast.showInfo("数据解析异常");
                }
            }
        } else {
            SmartToast.showInfo(msg);
        }

//        //todo 测试
//        String params_ali="{\"qrCode\":\"https://qr.alipay.com/bax023340vwk9tnkwmvs0077\"}";
//        String params_cloud="{\"tn\":\"737434615669962852000\"}";
//
//        String params_wx="{\"package\":\"Sign=WXPay\",\"appid\":\"wxa4207e39a8e5cf0f\",\"sign\":\"b8vhtf2njBjHNOhHcr1\\/92UC3ffvbZ4eq+TbGeZA6IncUSbolIoreYxRjDbaZuUVYTLFIIEd8cHlErOawrr2qlZr7BKvjJRS612DmHkrn\\/m1mIPZVBaV+JtqZjrwkKhjWXbpzNxH4K9llUGdbCjR\\/l76eAwvOzHW5gUQMaCQDl6UtgwEXXUzCDil4B3cj8oTR\\/x8xaHb8H5BSPqhjYzBDQok+J2yjPGWO6bxs1g8k64+p4REha9TDaNiKDFisg0D3ADJ6QOWaBKzOAjpdXdk5Y4EAXT2bPHtqZ6F9JKkqKPboAmdr4XoLkuuWMqpYGwbiNN8WZohmMzhf5SPaWUQZg==\",\"partnerid\":\"1501990501\",\"prepayid\":\"wx13094940892149da39d126721221015300\",\"noncestr\":\"c19c9ce0fa784361bca9c59777234e90\",\"timestamp\":\"1562982580\"}";
//

    }

    @Override
    public void onGetOrderResult(int status, String msg, String data, final String type_params) {
        // 本地服务器的回调
        if (status==1){
            request_count=0;
            hideDialog(smallDialog);
            if (type.equals(CanstantPay.PAY_SHOP_CONFIRM_ORDER)){
                getWuLiu();// 物流分配
                getPush("2");// 推送接口
                // 支付成功后判断优惠券id不为空的话请求优惠券接口
            }else if (type.equals(CanstantPay.PAY_SHOP_ORDER_DETAIL)){
                getWuLiu();// 物流分配
                getPush("1");// 推送接口
            }else if(type.equals(CanstantPay.PAY_SERVICE)){
                Intent intent = new Intent(this, FragmentOrderListActivity.class);
                intent.putExtra("type", "pay_finish");
                startActivity(intent);
                // 支付成功后调用极光
                new JpushPresenter().paySuccessJpush(order_id);
                finish();
            }else if(type.equals(CanstantPay.PAY_PROPERTY)) {
                EventBus.getDefault().post(new EventProperty());
                Intent intent = new Intent(this, PropertyPaymentActivity.class);
                startActivity(intent);
                finish();
            }else if(type.equals(CanstantPay.PAY_WORKORDER)){//工单
                EventBusWorkOrderModel eventBusModel = new EventBusWorkOrderModel();
                eventBusModel.setWork_id(order_id);
                eventBusModel.setEvent_back_type(2);
                EventBus.getDefault().post(eventBusModel);
                finish();
            }else if(type.equals(CanstantPay.PAY_HUODONG)){

            }else if(type.equals(CanstantPay.PAY_FACE)){

            }
            SmartToast.showInfo(msg);
        }else {
            //轮询服务器三次
            if (request_count>=2){
                hideDialog(smallDialog);
                request_count=0;
                SmartToast.showInfo(msg);
                return;
            }
            request_count++;
            listview.postDelayed(new Runnable() {
                @Override
                public void run() {
                    payPresenter.confirmOrderPayment(order_id,type_params);
                }
            },500);
        }
    }

    @Override
    public void onResult(String resultCode, String resultInfo) {
        //TODO 回调
        Log.i("UnifyPayActivity", "onResult resultCode=" + resultCode + ", resultInfo=" + resultInfo);
    }

    /**
     * 微信
     *
     * @param params
     */
    private void payWX(String params) {
        UnifyPayRequest msg = new UnifyPayRequest();
        msg.payChannel = UnifyPayRequest.CHANNEL_WEIXIN;
        msg.payData = params;
        UnifyPayPlugin.getInstance(this).sendPayRequest(msg);
    }

    /**
     * 支付宝
     *
     * @param params
     */
    private void payAliPay(String params) {
        UnifyPayRequest msg = new UnifyPayRequest();
        msg.payChannel = UnifyPayRequest.CHANNEL_ALIPAY;
        msg.payData = params;
        UnifyPayPlugin.getInstance(this).sendPayRequest(msg);
        isGotoPayback=true;
    }

    /**
     * 快捷支付
     *
     * @param params
     */
    private void payUMSPay(String params) {
        UnifyPayRequest msg = new UnifyPayRequest();
        msg.payChannel = UnifyPayRequest.CHANNEL_UMSPAY;
        msg.payData = params;
        UnifyPayPlugin.getInstance(this).sendPayRequest(msg);
    }

    /**
     * 云闪付
     *
     * @param appPayRequest
     */
    private void payCloudQuickPay(String appPayRequest) {
        String tn = "";
        try {
            JSONObject e = new JSONObject(appPayRequest);
            tn = e.getString("tn");
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        /**
         * activity —— 用于启动支付控件的活动对象
         * spId —— 保留使用，这里输入null
         * sysProvider —— 保留使用，这里输入null
         * orderInfo —— 订单信息为交易流水号，即TN，为商户后台从银联后台获取。
         * mode —— 银联后台环境标识，“00”将在银联正式环境发起交易,“01”将在银联测试环境发起交易
         */
        UPPayAssistEx.startPay(this, null, null, tn, "00");
    }

    // 物流分配
    protected void getWuLiu() {
        Url_info info = new Url_info(this);
        RequestParams params = new RequestParams();
        params.addBodyParameter("id", order_id);

        HttpHelper hh = new HttpHelper(info.distribution, params,
                this) {

            @Override
            protected void setData(String json) {
                String strWL = new ShopProtocol().setShop(json);
                if (strWL.equals("1")) {
                } else {

                }
            }

            @Override
            protected void requestFailure(Exception error, String msg) {
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        };
    }

    // 购物订单推送接口
    protected void getPush(final String isStr) {
        Url_info info = new Url_info(this);
        RequestParams params = new RequestParams();
        params.addBodyParameter("id", order_id+"");
        HttpHelper hh = new HttpHelper(info.merchant_push, params, this) {

            @Override
            protected void setData(String json) {
                String str_push = new ShopProtocol().setShop(json);
                if (str_push.equals("1")) {
                    if (isStr.equals("2")) {
                        Intent intent = new Intent(UnifyPayActivity.this,
                                ShopOrderListActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "type_zf_dsh");
                        intent.putExtras(bundle);
                        startActivity(intent);
                        // 支付完成后finish掉购物车页 立即支付页
                        finish();
                    } else {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("item_detete_id", "");
                        intent.putExtras(bundle);
                        setResult(333, intent);
                        finish();
                    }
                } else {
                    SmartToast.showInfo(str_push);
                }
            }

            @Override
            protected void requestFailure(Exception error, String msg) {
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        };
    }
}