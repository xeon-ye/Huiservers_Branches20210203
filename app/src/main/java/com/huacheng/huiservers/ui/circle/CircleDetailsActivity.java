package com.huacheng.huiservers.ui.circle;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coder.zzq.smartshow.toast.SmartToast;
import com.facebook.drawee.view.SimpleDraweeView;
import com.huacheng.huiservers.R;
import com.huacheng.huiservers.dialog.CommomDialog;
import com.huacheng.huiservers.http.HttpHelper;
import com.huacheng.huiservers.http.MyCookieStore;
import com.huacheng.huiservers.http.Url_info;
import com.huacheng.huiservers.http.okhttp.ApiHttpClient;
import com.huacheng.huiservers.http.okhttp.RequestParams;
import com.huacheng.huiservers.model.protocol.CircleProtocol;
import com.huacheng.huiservers.model.protocol.CommonProtocol;
import com.huacheng.huiservers.sharesdk.PopupWindowShare;
import com.huacheng.huiservers.ui.base.BaseActivityOld;
import com.huacheng.huiservers.ui.circle.adapter.CircleDetailListAdapter;
import com.huacheng.huiservers.ui.circle.bean.CircleDetailBean;
import com.huacheng.huiservers.ui.login.LoginVerifyCodeActivity;
import com.huacheng.huiservers.utils.LogUtils;
import com.huacheng.huiservers.utils.StringUtils;
import com.huacheng.huiservers.utils.UIUtils;
import com.huacheng.huiservers.view.CircularImage;
import com.huacheng.huiservers.view.MyListView;
import com.huacheng.libraryservice.utils.AppConstant;
import com.huacheng.libraryservice.utils.NullUtil;
import com.huacheng.libraryservice.utils.fresco.FrescoUtils;
import com.huacheng.libraryservice.utils.glide.GlideUtils;
import com.huacheng.libraryservice.utils.linkme.LinkedMeUtils;
import com.lidroid.xutils.BitmapUtils;
import com.microquation.linkedme.android.log.LMErrorCode;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.huacheng.huiservers.R.id.et_input;
import static com.huacheng.huiservers.R.id.tv_send;


/**
 * 类：
 * 时间：2018/3/16 15:43
 * 功能描述:Huiservers
 */
public class CircleDetailsActivity extends BaseActivityOld {

    CircleProtocol mCircleProtocol = new CircleProtocol();
    CircleDetailBean mCirclebean = new CircleDetailBean();
    CircleDetailListAdapter cricle2detailListAdapter;
    BitmapUtils bitmapUtils;
    @BindView(R.id.lin_pinglun)
    LinearLayout mLinPinglun;
    @BindView(R.id.scrollView)
    ScrollView mScrollView;
    @BindView(R.id.lin_top_all)
    LinearLayout mLinTopAll;
    @BindView(R.id.lin_comment)
    LinearLayout linComment;
    @BindView(R.id.right_share)
    ImageView mRightShare;
    @BindView(R.id.tv_pinglun_num)
    TextView mTvPinglunNum;

    private SharedPreferences preferencesLogin;
    private String login_type;
    private Dialog WaitDialog;

    @BindView(R.id.webview)
    WebView mWebview;
    @BindView(R.id.lin_nodata)
    LinearLayout mLinNodata;
    @BindView(R.id.lin_yescontent)
    LinearLayout mLinYescontent;

    String circle_id, circle_comment;
    @BindView(R.id.left)
    ImageView mLeft;
    @BindView(R.id.lin_left)
    LinearLayout mLinLeft;
    @BindView(R.id.title_name)
    TextView mTitleName;
    @BindView(R.id.iv_photo)
    SimpleDraweeView mIvPhoto;
    @BindView(R.id.tv_name)
    TextView mTvName;
    @BindView(R.id.tv_time)
    TextView mTvTime;
    @BindView(R.id.lin_guan)
    LinearLayout mLinGuan;
    @BindView(R.id.tv_user_content)
    TextView mTvUserContent;

    @BindView(R.id.lin_user)
    LinearLayout mLinUser;
    @BindView(R.id.list_reply)
    MyListView mListReply;
    @BindView(et_input)
    EditText mEtInput;
    @BindView(tv_send)
    TextView mTvSend;
    @BindView(R.id.iv_top)
    ImageView mIvTop;
    @BindView(R.id.lin_img)
    LinearLayout mLinImg;
    @BindView(R.id.iv_photo_bootom)
    CircularImage mIvPhotoBootom;
    @BindView(R.id.ll_title1)
    LinearLayout ll_title1;
    @BindView(R.id.ll_title2)
    LinearLayout ll_title2;
    @BindView(R.id.tv_title1)
    TextView tv_title1;
    @BindView(R.id.tv_sub_title1)
    TextView tv_sub_title1;


    private String SCROLLtag = "0";
    private String commentSuccess = "0";

    private int isPro = 0;//是否是物业公告
    private Handler handler = new Handler();
    private String share_url;
    private String share_title;
    private String share_desc;
    private String share_icon;


    /**
     * 滑动到指定位置
     */
    private void scrollToPosition() {

        new Handler().postDelayed(new Runnable() {
            public void run() {
                //显示dialog
                mScrollView.scrollTo(0, mLinPinglun.getTop() - mLinTopAll.getTop());
            }
        }, 500);   //0.5秒
    }

    boolean isRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cricle_detail);
        ButterKnife.bind(this);
        //       SetTransStatus.GetStatus(this);
        MyCookieStore.My_notify = 1;
        circle_id = this.getIntent().getExtras().getString("id");
        isPro = this.getIntent().getExtras().getInt("mPro");

        // circle_comment = this.getIntent().getExtras().getString("circle_comment");
//        isRefresh = this.getIntent().getExtras().getBoolean("refresh");
        if (!NullUtil.isStringEmpty(this.getIntent().getExtras().getString("SCROLLtag"))) {
            SCROLLtag = "1";
        } else {
            SCROLLtag = "0";
        }
        preferencesLogin = this.getSharedPreferences("login", 0);
        login_type = preferencesLogin.getString("login_type", "");
        bitmapUtils = new BitmapUtils(this);
        mTitleName.setText("邻里详情");
        showDialog(smallDialog);
        getdata();
        mEtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (StringUtils.isEmpty(mEtInput.getText().toString())) {
                    mTvSend.setBackground(getResources().getDrawable(R.drawable.bg_gray2_corners));
                    mTvSend.setTextColor(getResources().getColor(R.color.gray3));
                    mTvSend.setEnabled(false);
                } else {
                    mTvSend.setBackground(getResources().getDrawable(R.drawable.bg_primary2_corners));
                    mTvSend.setTextColor(getResources().getColor(R.color.white));
                    mTvSend.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //setEditTextInputSpace(mEtInput);

            }
        });

        mRightShare.setVisibility(View.VISIBLE);
    }

    String content1;

    //获取邻里详情
    private void getdata() {
        Url_info info = new Url_info(this);
        RequestParams params = new RequestParams();
        params.addBodyParameter("id", circle_id);
        params.addBodyParameter("is_pro", isPro + "");
        HttpHelper hh = new HttpHelper(info.get_social, params, CircleDetailsActivity.this) {

            @Override
            protected void setData(String json) {
                hideDialog(smallDialog);
                mCirclebean = mCircleProtocol.getSocialDetail(json);
                //顶部头像
                if (!NullUtil.isStringEmpty(mCirclebean.getAvatars())) {
                    FrescoUtils.getInstance().setImageUri(mIvPhoto, StringUtils.getImgUrl(mCirclebean.getAvatars()));
                    // GlideUtils.getInstance().glideLoad(context, StringUtils.getImgUrl(mCirclebean.getAvatars()), mIvPhoto, R.drawable.ic_default_head);
                }
                //底部头像
               /* Glide.with(context).load(MyCookieStore.URL + mCirclebean.getAvatars()).skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.ic_default_head).error(R.drawable.ic_default_head).into(mIvPhotoBootom);*/

                mTvName.setText(mCirclebean.getNickname());
                mTvTime.setText(mCirclebean.getAddtime());
                //判断内容是官方发布还是用户发布admin_id为0标识为用户发布
                if (mCirclebean.getAdmin_id().equals("0")) {//用户发布
                    //mTvName.setTextColor(context.getResources().getColor(R.color.black_jain_87));
                    mLinGuan.setVisibility(View.GONE);
                    mLinUser.setVisibility(View.VISIBLE);
                    byte[] bytes = Base64.decode(mCirclebean.getContent(), Base64.DEFAULT);
                    mTvUserContent.setText(new String(bytes));
                    // mTvUserContent.setText("     " + mCirclebean.getContent());
                    //动态添加用户发表图片
                    getAddview();

                    ll_title1.setVisibility(View.GONE);
                    ll_title2.setVisibility(View.VISIBLE);
                } else {///官方图文混排
                    ll_title1.setVisibility(View.VISIBLE);
                    ll_title2.setVisibility(View.GONE);
                    //显示头部
                    byte[] bytes1 = Base64.decode(mCirclebean.getTitle(), Base64.DEFAULT);
                    tv_title1.setText(new String(bytes1));
                    tv_sub_title1.setText("发布源："+mCirclebean.getNickname()+"    "+mCirclebean.getAddtime());

                    // mTvName.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    if (isPro == 1) {
                        mLinGuan.setVisibility(View.VISIBLE);
                        mLinUser.setVisibility(View.VISIBLE);
                        getAddview();
                    } else {
                        mLinGuan.setVisibility(View.VISIBLE);
                        mLinUser.setVisibility(View.GONE);
                    }

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
                    byte[] bytes = Base64.decode(mCirclebean.getContent(), Base64.DEFAULT);
                    //加载HTML字符串进行显示
                    // mWebview.loadDataWithBaseURL(null, getNewContent(new String(bytes)), "text/html", "utf-8", null);
                    String content = new String(bytes);
                    if (!"".equals(content)) {
                        String css = "<style type=\"text/css\"> " +
                                "img {" +
                                "max-width: 100% !important;" +//限定图片宽度填充屏幕
                                "height:auto !important;" +//限定图片高度自动
                                "}" +
                                "</style>";
                        content1 = "<head>" + css + "</head><body>" + content + "</body></html>";
                        mWebview.loadDataWithBaseURL(null, content1, "text/html", "utf-8", null);
                        LogUtils.d("[content1]" + content1);
                    }
                }
                if (mCirclebean.getReply_list() != null) {
                    mTvPinglunNum.setText("评论(" + mCirclebean.getReply_list().size() + ")");
                    mLinYescontent.setVisibility(View.VISIBLE);
                    mLinNodata.setVisibility(View.GONE);
                    cricle2detailListAdapter = new CircleDetailListAdapter(CircleDetailsActivity.this, mCirclebean.getReply_list());
                    mListReply.setAdapter(cricle2detailListAdapter);

                    cricle2detailListAdapter.setmOnItemDeleteListener(new CircleDetailListAdapter.onItemDeleteListener() {
                        @Override
                        public void onDeleteClick(final int i) {

                            new CommomDialog(CircleDetailsActivity.this, R.style.my_dialog_DimEnabled, "删除此信息吗？", new CommomDialog.OnCloseListener() {
                                @Override
                                public void onClick(Dialog dialog, boolean confirm) {
                                    if (confirm) {
                                        if (mCirclebean.getReply_list() != null) {
                                            socialReplyDel(mCirclebean.getReply_list().get(i).getId(), mCirclebean.getReply_list().get(i).getSocial_id());
                                        }
                                        dialog.dismiss();
                                    }
                                }
                            }).show();//.setTitle("提示")

                        }
                    });
                } else {
                    mTvPinglunNum.setText("评论");
                    mLinYescontent.setVisibility(View.GONE);
                    mLinNodata.setVisibility(View.VISIBLE);
                    //显示无评论图
                }

//                mCirclebean.getIs_observe()

                if ("1".equals(mCirclebean.getIs_observe())) {
                    linComment.setVisibility(View.VISIBLE);
                    mLinPinglun.setVisibility(View.VISIBLE);
                } else {
                    linComment.setVisibility(View.GONE);
                    mLinPinglun.setVisibility(View.GONE);
                }
                if (SCROLLtag.equals("1")) {//值为1 的时候  代表评论成功执行这段话
                    scrollToPosition();

                }
//                linComment.setVisibility(View.GONE);

            }

            @Override
            protected void requestFailure(Exception error, String msg) {
                hideDialog(smallDialog);
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        };
    }

    /**
     * 删除邻里回复
     *
     * @param socialID
     */
    private void socialReplyDel(String id, String socialID) {
        showDialog(smallDialog);
        Url_info urlInfo = new Url_info(this);
        RequestParams params = new RequestParams();
        params.addBodyParameter("social_reply_id", id);
        params.addBodyParameter("social_id", socialID);
        params.addBodyParameter("is_pro", isPro + "");
        new HttpHelper(urlInfo.social_reply_del, params, this) {

            @Override
            protected void setData(String json) {
                hideDialog(smallDialog);
                String result = new CommonProtocol().getResult(json);
                if (result.equals("1")) {
                    mLinImg.removeAllViews();
                    SCROLLtag = "1";
                    getdata();

                    // scrollToPosition();
                    mCirclebean.setType(1);
                    EventBus.getDefault().post(mCirclebean);
                } else {
                    SmartToast.showInfo("删除失败");
                }
            }

            @Override
            protected void requestFailure(Exception error, String msg) {
                hideDialog(smallDialog);
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        };
    }

    //评论
    private void pinglun(String social_id, String content) {
        showDialog(smallDialog);
        Url_info info = new Url_info(this);
        RequestParams params = new RequestParams();
        params.addBodyParameter("social_id", social_id);
        params.addBodyParameter("content", content);
        params.addBodyParameter("is_pro", isPro + "");

        HttpHelper hh = new HttpHelper(info.social_reply, params, this) {

            @Override
            protected void setData(String json) {
                hideDialog(smallDialog);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String status = jsonObject.getString("status");
                    if (StringUtils.isEquals(status, "1")) {
//                        MyCookieStore.Circle_refresh = 1;
                        SmartToast.showInfo("评论成功");
                        mLinImg.removeAllViews();
                        SCROLLtag = "1";
                        getdata();

                        // scrollToPosition();
                        mCirclebean.setType(0);
                        EventBus.getDefault().post(mCirclebean);
                    } else {
                        SmartToast.showInfo(jsonObject.getString("msg"));
                    }
                } catch (Exception e) {
                    LogUtils.e(e);
                }
            }

            @Override
            protected void requestFailure(Exception error, String msg) {
                hideDialog(smallDialog);
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        };
    }


    protected void getAddview() {
        mLinImg.removeAllViews();
        if (mCirclebean.getImg_list() != null) {
            for (int i = 0; i < mCirclebean.getImg_list().size(); i++) {
                View view = LinearLayout.inflate(CircleDetailsActivity.this, R.layout.cricle_detail_imgdesc_item, null);
                ImageView img = (ImageView) view.findViewById(R.id.img);

                //
             //   bitmapUtils.display(img, MyCookieStore.URL + mCirclebean.getImg_list().get(i).getImg());//"http://property.hui-shenghuo.cn/"

                GlideUtils.getInstance().glideLoad(this,MyCookieStore.URL + mCirclebean.getImg_list().get(i).getImg(),img,R.color.default_color);
                mLinImg.addView(view);
            }
            if (SCROLLtag.equals("1")) {//值为1 的时候  代表评论成功执行这段话
                scrollToPosition();

            }
//            mCirclebean.getImg_list().clear();
        }
    }


    @OnClick({R.id.lin_left, et_input, tv_send, R.id.right_share})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.lin_left:
                UIUtils.closeInputMethod(this, mEtInput);

             /*   if (!StringUtils.isEmpty(circle_comment)) {
                    if (circle_comment.equals("1")) {
                        setResult(11);
                    }
                }
*/
               /* if (isRefresh) {
                    if (commentSuccess.equals("1")) {

                    }
                }*/
                finish();
                break;
            case et_input:
                break;
            case tv_send:
                preferencesLogin = getSharedPreferences("login", 0);
                login_type = preferencesLogin.getString("login_type", "");
                if (login_type.equals("") || ApiHttpClient.TOKEN == null || ApiHttpClient.TOKEN_SECRET == null) {
                    startActivity(new Intent(CircleDetailsActivity.this, LoginVerifyCodeActivity.class));
                } else {

                    if (TextUtils.isEmpty(mEtInput.getText().toString().trim())) {
                        SmartToast.showInfo("请输入您要说的话");
                    } else {

                        String str_count = Base64.encodeToString(getStringNoBlank(mEtInput.getText().toString().trim()).getBytes(), Base64.DEFAULT);
                        pinglun(circle_id, str_count);
                        mEtInput.setText("");
                    }
                }
                break;
            case R.id.right_share://分享
                if (NullUtil.isStringEmpty(circle_id)) {
                    return;
                }
                if (!NullUtil.isStringEmpty(mCirclebean.getTitle())) {
                    byte[] bytes = Base64.decode(mCirclebean.getTitle(), Base64.DEFAULT);
                    share_title = new String(bytes) + "";
                } else {
                    share_title = mCirclebean.getC_name() + "";
                }
                share_desc = mCirclebean.getShare_content();
                share_icon = StringUtils.getImgUrl(mCirclebean.getShare_img());
                share_url = ApiHttpClient.API_URL_SHARE + ApiHttpClient.API_VERSION + "social/socialShare/id/" + circle_id;
                HashMap<String, String> params = new HashMap<>();
                params.put("type", "circle_details");
                params.put("id", circle_id);
                params.put("sub_type", isPro + "");
                showDialog(smallDialog);
                LinkedMeUtils.getInstance().getLinkedUrl(this, share_url, share_title, params, new LinkedMeUtils.OnGetLinkedmeUrlListener() {
                    @Override
                    public void onGetUrl(String url, LMErrorCode error) {
                        hideDialog(smallDialog);
                        if (error == null) {
                            String share_url_new = share_url + "?linkedme=" + url;
                            showSharePop(share_title, share_desc, share_icon, share_url_new);
                        } else {
                            //可以看报错
                            String share_url_new = share_url + "?linkedme=" + "";
                            showSharePop(share_title, share_desc, share_icon, share_url_new);
                        }
                    }
                });

                break;
        }
    }

    public static String getStringNoBlank(String str) {//去除首尾空格 换行符
        if (str != null && !"".equals(str)) {
            str.replaceAll("\n", "");
            Pattern p = Pattern.compile("(^\\s*)|(\\s*$)");
            Matcher m = p.matcher(str);
            String strNoBlank = m.replaceAll("");
            return strNoBlank;
        } else {
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCirclebean != null) {
            if (!"0".equals(mCirclebean.getId())) {
                mCirclebean.setType(2);
                EventBus.getDefault().post(mCirclebean);
            }
        }
    }

    /**
     * 显示分享弹窗
     *
     * @param share_title
     * @param share_desc
     * @param share_icon
     * @param share_url_new
     */
    private void showSharePop(String share_title, String share_desc, String share_icon, String share_url_new) {
        PopupWindowShare popup = new PopupWindowShare(this, share_title, share_desc, share_icon, share_url_new, AppConstant.SHARE_COMMON);
        popup.showBottom(mRightShare);
    }
}
