package com.huacheng.huiservers.ui.center.house;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coder.zzq.smartshow.toast.SmartToast;
import com.huacheng.huiservers.R;
import com.huacheng.huiservers.dialog.InviteAddPersonDialog;
import com.huacheng.huiservers.http.HttpHelper;
import com.huacheng.huiservers.http.Url_info;
import com.huacheng.huiservers.http.okhttp.RequestParams;
import com.huacheng.huiservers.ui.base.BaseActivity;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 类：访客邀请Activity
 *
 *  时间：2018/3/23 08:18
 * 功能描述:Huiservers
 */
public class HouseInviteActivity extends BaseActivity {

    @BindView(R.id.lin_code)
    LinearLayout mLinCode;
    @BindView(R.id.lin_password)
    LinearLayout mLinPassword;
    @BindView(R.id.lin_left)
    LinearLayout mLinLeft;
    @BindView(R.id.title_name)
    TextView mTitleName;

    InviteAddPersonDialog dialog;
    @BindView(R.id.txt_mianmi)
    TextView mTxtMianmi;

    private String room_id, mobile, community, building, room_code;

    @Override
    protected void initView() {
        ButterKnife.bind(this);

        Intent intent = getIntent();
        room_id = intent.getStringExtra("room_id");
        getResult();
        mTitleName.setText("访客邀请");
        mTxtMianmi.setText("记录");
        mTxtMianmi.setTextColor(getResources().getColor(R.color.title_sub_color));
        mTxtMianmi.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.invite_type;
    }


    @Override
    protected void initIntentData() {

    }

    @Override
    protected int getFragmentCotainerId() {
        return 0;
    }

    @Override
    protected void initFragment() {

    }

    private void getResult() {
        showDialog(smallDialog);
        Url_info info = new Url_info(this);
        RequestParams params = new RequestParams();
        params.addBodyParameter("room_id", room_id);
        HttpHelper hh = new HttpHelper(info.checkIsAjb, params, this) {


            @Override
            protected void setData(String json) {
                hideDialog(smallDialog);
                JSONObject jsonObject, jsonData;
                try {
                    jsonObject = new JSONObject(json);
                    String data = jsonObject.getString("data");
                    String status = jsonObject.getString("status");
                    if (status.equals("1")) {
                        jsonData = new JSONObject(data);
                        mobile = jsonData.getString("mobile");
                        community = jsonData.getString("community");
                        building = jsonData.getString("building");
                        room_code = jsonData.getString("room_code");
                    } else {
                        SmartToast.showInfo(jsonObject.getString("msg"));
                    }
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            protected void requestFailure(Exception error, String msg) {
                hideDialog(smallDialog);
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        };
    }

    @OnClick({R.id.lin_code, R.id.lin_password, R.id.lin_left, R.id.txt_mianmi})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.lin_code://二维码

                dialog = new InviteAddPersonDialog(this, "1", new InviteAddPersonDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String name, String s) {
                        if (name.equals("1")) {
                            Intent intent = new Intent(HouseInviteActivity.this, HouseCodeActivity.class);
                            intent.putExtra("name", s);
                            intent.putExtra("mobile", mobile);
                            intent.putExtra("community", community);
                            intent.putExtra("building", building);
                            intent.putExtra("room_code", room_code);
                            intent.putExtra("ajb_type", "1");
                            startActivity(intent);
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.lin_password://通行码
                dialog = new InviteAddPersonDialog(this, "2", new InviteAddPersonDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String name, String s) {
                        if (name.equals("2")) {
                            Intent intent1 = new Intent(HouseInviteActivity.this, HousePassActivity.class);
                            intent1.putExtra("name", s);
                            intent1.putExtra("mobile", mobile);
                            intent1.putExtra("community", community);
                            intent1.putExtra("building", building);
                            intent1.putExtra("room_code", room_code);
                            intent1.putExtra("ajb_type", "1");
                            startActivity(intent1);
                        }
                    }
                });
                dialog.show();

                break;
            case R.id.lin_left://返回
                finish();
                break;
            case R.id.txt_mianmi://记录

                Intent intent = new Intent(HouseInviteActivity.this, HouseOpenKeepActivity.class);
                intent.putExtra("mobile", mobile);
                intent.putExtra("community", community);
                intent.putExtra("building", building);
                intent.putExtra("room_code", room_code);
                startActivity(intent);

                break;
        }
    }

}
