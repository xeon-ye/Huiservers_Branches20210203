package com.huacheng.huiservers.ui.center;

import android.content.Context;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.coder.zzq.smartshow.toast.SmartToast;
import com.huacheng.huiservers.BaseApplication;
import com.huacheng.huiservers.R;
import com.huacheng.huiservers.db.UserSql;
import com.huacheng.huiservers.http.Url_info;
import com.huacheng.huiservers.http.okhttp.MyOkHttp;
import com.huacheng.huiservers.http.okhttp.RequestParams;
import com.huacheng.huiservers.http.okhttp.response.RawResponseHandler;
import com.huacheng.huiservers.model.ModelUser;
import com.huacheng.huiservers.model.protocol.ShopProtocol;
import com.huacheng.huiservers.ui.base.BaseActivityOld;
import com.huacheng.huiservers.ui.center.bean.PersoninfoBean;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

public class NamenickVerfityActivity extends BaseActivityOld implements OnClickListener {

    TextView title_name, tv_description, tv_flag, right;
    EditText et_content;

    @Override
    protected void init() {
        super.init();
        //     SetTransStatus.GetStatus(this);
        setContentView(R.layout.verify_editxt_new);
        title_name = (TextView) findViewById(R.id.title_name);
        right = (TextView) findViewById(R.id.right);
        right.setVisibility(View.VISIBLE);
        tv_description = (TextView) findViewById(R.id.tv_description);
        tv_flag = (TextView) findViewById(R.id.tv_flag);
        et_content = (EditText) findViewById(R.id.et_content);
        // set
        title_name.setText("昵称");
        right.setTextColor(getResources().getColor(R.color.orange_bg));
        right.setText("确定");
        // get
        String nickname = getIntent().getExtras().getString("nickname");
        if (!nickname.equals("")) {
            et_content.setText(nickname);
        } else {
            et_content.setHint("请输入");
        }
        String sex = getIntent().getExtras().getString("tv_sex");
        if (sex.equals("男")) {
            tv_description.setText("先生");
        } else if (sex.equals("女")) {
            tv_description.setText("女士");
        } else {
            tv_description.setText("先生/女士");
        }

        tv_flag.setText("将用于社区慧生活社区交流，昵称不能超过8位，包含汉字、字母或数字，且不能与别人重复");
        // 限定edittext能输入内容
        et_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        // 强制隐藏Android输入法窗口
        et_content.setFocusableInTouchMode(true);
        et_content.setFocusable(true);
        et_content.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) et_content.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et_content, 0);
            }
        }, 100);
        // listener
        findViewById(R.id.lin_left).setOnClickListener(this);
        right.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right:
                String nickname = et_content.getText().toString();
                if (!nickname.equals("")) {
                    getMyinfo(nickname);
                    /*closeInputMethod();
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("nickname", nickname);
                    intent.putExtras(bundle);
                    setResult(22, intent);
                    finish();*/
                } else {
                    SmartToast.showInfo("昵称不能为空");
                }

                break;
            case R.id.lin_left:
                closeInputMethod();
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 修改个人信息
     *
     * @param param
     */
    private void getMyinfo(final String param) {
        showDialog(smallDialog);
        Url_info info = new Url_info(this);
        RequestParams params = new RequestParams();
        params.addBodyParameter("nickname", param);

        MyOkHttp.get().post(info.edit_center, params.getParams(), new RawResponseHandler() {
            @Override
            public void onSuccess(int statusCode, String response) {
                hideDialog(smallDialog);
                ShopProtocol protocol = new ShopProtocol();
                String str = protocol.setShop(response);
                if (str.equals("1")) {
                    closeInputMethod();
                    EventBus.getDefault().post(new PersoninfoBean());
                    finish();
                    //更改昵称更新数据库
                    ModelUser modelUser = BaseApplication.getUser();
                    modelUser.setNickname(param);
                    UserSql.getInstance().updateObject(modelUser);
                    SmartToast.showInfo("修改成功");
                } else {
                    SmartToast.showInfo(str);
                }
            }

            @Override
            public void onFailure(int statusCode, String error_msg) {
                hideDialog(smallDialog);
                SmartToast.showInfo("网络异常，请检查网络设置");
            }
        });

    }

    /**
     * 关闭软键盘
     */
    private void closeInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            // imm.toggleSoftInput(0,
            // InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示

            imm.hideSoftInputFromWindow(et_content.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
