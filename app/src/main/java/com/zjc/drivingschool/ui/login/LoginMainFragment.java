package com.zjc.drivingschool.ui.login;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.mobo.mobolibrary.ui.base.ZBaseToolBarFragment;
import com.mobo.mobolibrary.util.Util;
import com.zjc.drivingschool.R;
import com.zjc.drivingschool.api.ApiHttpClient;
import com.zjc.drivingschool.api.ResultResponseHandler;
import com.zjc.drivingschool.app.MApp;
import com.zjc.drivingschool.db.SharePreferences.SharePreferencesUtil;
import com.zjc.drivingschool.db.model.UserInfo;
import com.zjc.drivingschool.db.parser.UserInfoParser;
import com.zjc.drivingschool.jpush.JPushUtil;

import cn.jpush.android.api.JPushInterface;


/**
 * @author Z
 * @Filename LoginMainFragment.java
 * @Date 2015-10-24
 * @description 密码登录界面
 */
public class LoginMainFragment extends ZBaseToolBarFragment implements View.OnClickListener {
    private TextView tvCommit;
    private TextView tvForget;
    private EditText edtPhone;
    private EditText edtPawd;
    private TextView tvRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 得到布局id
     *
     * @return
     */
    @Override
    protected int inflateContentView() {
        return R.layout.login_main_frg;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initView();
    }


    private void initView() {
        tvCommit = (TextView) rootView.findViewById(R.id.login_main_frg_tv_commit);
        edtPhone = (EditText) rootView.findViewById(R.id.login_main_frg_edt_phone);
        edtPawd = (EditText) rootView.findViewById(R.id.login_main_frg_edt_pawd);
        tvForget = (TextView) rootView.findViewById(R.id.login_main_frg_tv_forget);
        tvRegister = (TextView) rootView.findViewById(R.id.login_main_frg_tv_register);

        tvForget.setOnClickListener(this);
        tvCommit.setOnClickListener(this);
        tvRegister.setOnClickListener(this);

        tvForget.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvForget.getPaint().setAntiAlias(true);//抗锯齿
        tvRegister.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvRegister.getPaint().setAntiAlias(true);//抗锯齿

        edtPhone.setText(SharePreferencesUtil.getInstance().readPhone());
        edtPawd.setText(SharePreferencesUtil.getInstance().readPwd());

        edtPawd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO) {
                    toLogin();
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.login_main_frg_tv_commit) {
            toLogin();
        } else if (i == R.id.login_main_frg_tv_forget) {
            LoginForgetFragment forgetFragment = new LoginForgetFragment();
            replaceFrg(forgetFragment, null);
        } else if (i == R.id.login_main_frg_tv_register) {
            LoginRegisterFragment registerFragment = new LoginRegisterFragment();
            replaceFrg(registerFragment, null);
        }
    }

    /**
     * 请求网络验证登录
     */
    public void toLogin() {
        if (TextUtils.isEmpty(edtPhone.getEditableText().toString()) || TextUtils.isEmpty(edtPawd.getEditableText().toString())) {
            Util.showCustomMsg("请输入完整信息");
            return;
        }
        ApiHttpClient.getInstance().login(edtPhone.getEditableText().toString(), edtPawd.getEditableText().toString(), new ResultResponseHandler(getActivity(), "请稍等") {

            @Override
            public void onResultSuccess(String result) {
                UserInfo userInfo = new UserInfoParser().parseResultMessage(result);
                SharePreferencesUtil.getInstance().savePhone(userInfo.getPhone());
                SharePreferencesUtil.getInstance().savePwd(edtPawd.getEditableText().toString());
                SharePreferencesUtil.getInstance().saveUser(userInfo);
                SharePreferencesUtil.getInstance().setLogin(true);
                JPushUtil.setAliasAndTags();
                getActivity().finish();
            }
        });
    }

    @Override
    protected void setTitle() {
        setTitle(mToolbar, R.string.login);
    }
}
