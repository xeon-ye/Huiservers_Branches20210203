package com.huacheng.huiservers.ui.index.oldservice;

import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.huacheng.huiservers.R;
import com.huacheng.huiservers.ui.base.BaseActivity;

/**
 * 类描述：资讯详情
 * 时间：2019/8/15
 * created by DFF
 */
public class ZXDetailActivity extends BaseActivity implements View.OnClickListener {
    private WebView mWebview;
    private TextView mTvName;
    private TextView tv_person_addtime;
    private TextView tv_fav;
    private TextView tv_read_count;
    private String id = "";
    String str_url;

    @Override
    protected void initView() {
        findTitleViews();
        titleName.setText("资讯详情");

        mTvName = findViewById(R.id.tv_title);
        tv_person_addtime = findViewById(R.id.tv_person_addtime);
        mWebview = findViewById(R.id.web_content);
        tv_fav = findViewById(R.id.tv_fav);
        tv_read_count = findViewById(R.id.tv_read_count);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        tv_fav.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_zixun_detail;
    }

    @Override
    protected void initIntentData() {
        id = getIntent().getStringExtra("id");

    }

   /* private void getdata() {
        showDialog(smallDialog);
        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        MyOkHttp.get().post(ApiHttpClient.CIRCLE_DETAILS, params, new JsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                hideDialog(smallDialog);
                if (JsonUtil.getInstance().isSuccess(response)) {
                    ModelFav info = (ModelFav) JsonUtil.getInstance().parseJsonFromResponse(response, ModelFav.class);
                    if (info != null) {
                        mTvName.setText(info.getTitle());
                        tv_person_addtime.setText("来源：" + info.getSource() + "    " + StringUtils.getDateToString(info.getAdd_time(), "7"));
                        tv_read_count.setText("阅读 " + info.getBrowse_num());

                        //能够的调用JavaScript代码
                        mWebview.getSettings().setJavaScriptEnabled(true);
                        // 设置允许JS弹窗
                        //        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                        // 设置可以支持缩放
                        mWebview.getSettings().setSupportZoom(true);
                        mWebview.getSettings().setBuiltInZoomControls(true);
                        // 设置隐藏缩放工具控制条
                        mWebview.getSettings().setDisplayZoomControls(false);
                        //扩大比例的缩放
                        mWebview.getSettings().setUseWideViewPort(false);
                        //自适应屏幕
                        mWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                        mWebview.getSettings().setLoadWithOverviewMode(true);

                        //设置字体大小
                        mWebview.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
                       *//* byte[] bytes = Base64.decode(info.getContent(), Base64.DEFAULT);
                        String content = new String(bytes);*//*
                        if (!"".equals(info.getContent())) {
                            String css = "<style type=\"text/css\"> " +
                                    "img {" +
                                    "max-width: 100% !important;" +//限定图片宽度填充屏幕
                                    "height:auto !important;" +//限定图片高度自动
                                    "}" +
                                    "</style>";
                            String content1 = "<head>" + css + "</head><body>" + info.getContent() + "</body></html>";
                            mWebview.loadDataWithBaseURL(null, content1, "text/html", "utf-8", null);
                            LogUtils.d("[content1]" + content1);
                        }
                        if (info.getIs_collection() == 1) {//未收藏
                            tv_fav.setBackground(getResources().getDrawable(R.drawable.shape_gray_stroke20));
                            tv_fav.setTextColor(getResources().getColor(R.color.gray_66));
                            tv_fav.setText("收藏");
                        } else {
                            tv_fav.setBackground(getResources().getDrawable(R.drawable.shape_blue_stroke20));
                            tv_fav.setTextColor(getResources().getColor(R.color.blue));
                            tv_fav.setText("取消收藏");
                        }
                    }
                } else {
                    String msg = JsonUtil.getInstance().getMsgFromResponse(response, "请求失败");
                    ToastUtils.showShort(mContext.getApplicationContext(), msg);
                }
            }

            @Override
            public void onFailure(int statusCode, String error_msg) {
                hideDialog(smallDialog);
                ToastUtils.showShort(mContext.getApplicationContext(), "网络异常，请检查网络设置");
            }
        });
    }
*/
    //收藏
   /* private void fav(final int type) {
        showDialog(smallDialog);
        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        if (type == 1) {
            str_url = ApiHttpClient.CIRCLE_COLLECTION;
        } else {
            str_url = ApiHttpClient.CIRCLE_COLLECTION_CANCEL;
        }
        MyOkHttp.get().post(str_url, params, new JsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                //  hideDialog(smallDialog);
                if (JsonUtil.getInstance().isSuccess(response)) {
                    getdata();
                    //刷新我的页面
                    EventBus.getDefault().post(new ModelMy());
                    if (type == 2) {//取消收藏刷新我的收藏列表界面
                        ModelFav modelFav = new ModelFav();
                        modelFav.setId(id);
                        EventBus.getDefault().post(modelFav);
                    }
                } else {
                    hideDialog(smallDialog);
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
    }*/

    @Override
    protected int getFragmentCotainerId() {
        return 0;
    }

    @Override
    protected void initFragment() {

    }

    @Override
    public void onClick(View v) {

    }

   /* @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_fav:
                if (tv_fav.getText().toString().equals("收藏")) {//点击收藏
                    fav(1);
                } else {//取消收藏
                    fav(2);
                }
                break;
        }

    }*/
}
