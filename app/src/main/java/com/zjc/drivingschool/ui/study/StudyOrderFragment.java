package com.zjc.drivingschool.ui.study;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.baidu.mapapi.cloud.CloudPoiInfo;
import com.bigkoo.pickerview.OptionsPopupWindow;
import com.bigkoo.pickerview.TimePopupWindow;
import com.mobo.mobolibrary.ui.base.ZBaseToolBarFragment;
import com.mobo.mobolibrary.util.Util;
import com.mobo.mobolibrary.util.UtilDate;
import com.zjc.drivingschool.R;
import com.zjc.drivingschool.api.ApiHttpClient;
import com.zjc.drivingschool.api.ResultResponseHandler;
import com.zjc.drivingschool.db.SharePreferences.SharePreferencesUtil;
import com.zjc.drivingschool.db.model.AccountBalance;
import com.zjc.drivingschool.db.model.ProductSubject;
import com.zjc.drivingschool.db.parser.AccountBalanceParser;
import com.zjc.drivingschool.db.parser.UserProductResponseParser;
import com.zjc.drivingschool.db.request.OrderCreateRequest;
import com.zjc.drivingschool.db.response.Coupon;
import com.zjc.drivingschool.db.response.UserProductResponse;
import com.zjc.drivingschool.eventbus.StudyAddressChooseEvent;
import com.zjc.drivingschool.eventbus.StudyCouponChooseEvent;
import com.zjc.drivingschool.ui.account.AccountManagerActivity;
import com.zjc.drivingschool.utils.ConstantsParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author Z
 * @Filename StudyOrderFragment.java
 * @Date 2016.06.26
 * @description 学车申请
 */
public class StudyOrderFragment extends ZBaseToolBarFragment implements View.OnClickListener {
    private UserProductResponse userProduct;
    private ToggleButton jpushButton;
    private TextView tv_time;
    private TextView tv_locale;
    private TextView tv_timeLength;
    private TextView tv_subject;
    private TextView tv_style;
    private TextView tv_telephone;
    private TextView tv_next;
    private TextView tvMoney;
    private TextView tvCoupon;
    private TextView tvEndMoney;
    private int index;
    private long time = 1000 * 60 * 60 * 3;//练车时间必须大于3小时

    private OptionsPopupWindow timeLengthOptions;
    private ArrayList<String> optionsItems = new ArrayList<>();

    private OptionsPopupWindow tvSubjectOptions;
    private ArrayList<String> optionsItemsSubject = new ArrayList<>();//培训科目集合
    private List<ProductSubject> productSubjectList = new ArrayList<>();//培训科目集合

    private OptionsPopupWindow tvStyleOptions;
    private ArrayList<String> optionsItemsStyle = new ArrayList<>();

    private OrderCreateRequest orderDetail;

    private TimePopupWindow birthOptions;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void setTitle() {
        setTitle(mToolbar, R.string.title_study);
    }

    @Override
    protected int inflateContentView() {
        return R.layout.learn_apply_frg;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initView();
        initBirthOptions();
        initTimeLengthOptions();
        initStyleOptions();
        findProducts();
    }

    private void initView() {
        jpushButton = (ToggleButton) rootView.findViewById(R.id.personal_center_set_frg_jpush_button2);
        tv_time = (TextView) rootView.findViewById(R.id.tv_time);
        tv_locale = (TextView) rootView.findViewById(R.id.tv_locale);
        tv_timeLength = (TextView) rootView.findViewById(R.id.tv_timeLength);
        tv_subject = (TextView) rootView.findViewById(R.id.tv_subject);
        tv_style = (TextView) rootView.findViewById(R.id.tv_style);
        tv_telephone = (TextView) rootView.findViewById(R.id.tv_telephone);
        tv_next = (TextView) rootView.findViewById(R.id.tv_next);
        tvMoney = (TextView) rootView.findViewById(R.id.tv_money);
        tvCoupon = (TextView) rootView.findViewById(R.id.learn_apply_frg_tv_coupon);
        tvEndMoney = (TextView) rootView.findViewById(R.id.tv_end_money);

        tv_time.setOnClickListener(this);
        tv_locale.setOnClickListener(this);
        tv_timeLength.setOnClickListener(this);
        tv_subject.setOnClickListener(this);
        tv_style.setOnClickListener(this);
        tv_telephone.setOnClickListener(this);
        tv_next.setOnClickListener(this);
        tvCoupon.setOnClickListener(this);
        jpushButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    setIndex(1);
                    jpushButton.setChecked(true);
                    jpushButton.setBackgroundResource(R.drawable.toggle_on);
                    tv_telephone.setVisibility(View.VISIBLE);
                } else {
                    jpushButton.setChecked(false);
                    setIndex(0);
                    jpushButton.setBackgroundResource(R.drawable.toggle_off);
                    tv_telephone.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initStyleOptions() {
        tvStyleOptions = new OptionsPopupWindow(getActivity());
        optionsItemsStyle.add("VIP训练");
        optionsItemsStyle.add("普通训练");
        tvStyleOptions.setPicker(optionsItemsStyle, null, null, true);
        tvStyleOptions.setSelectOptions(0);
        tvStyleOptions.setOnoptionsSelectListener(new OptionsPopupWindow.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                String tx = optionsItemsStyle.get(options1);
                tv_style.setText(tx);
                if (options1 == 0) {
                    tv_style.setTag(true);
                } else {
                    tv_style.setTag(false);
                }
                refreshMoney();
            }
        });
    }

    private void initTimeLengthOptions() {
        timeLengthOptions = new OptionsPopupWindow(getActivity());
        optionsItems.add("1小时");
        optionsItems.add("2小时");
        optionsItems.add("3小时");
        timeLengthOptions.setPicker(optionsItems, null, null, true);
        timeLengthOptions.setSelectOptions(0);
        timeLengthOptions.setOnoptionsSelectListener(new OptionsPopupWindow.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                String tx = optionsItems.get(options1);
                tv_timeLength.setText(tx);
                tv_timeLength.setTag(options1 + 1);
                refreshMoney();
            }
        });
    }

    private void refreshMoney() {
        if (tv_timeLength.getTag() == null || tv_style.getTag() == null) {
            return;
        }

        double money;
        int timeLength = (int) tv_timeLength.getTag();//练车时长
        boolean isVip = (boolean) tv_style.getTag();
        if (isVip) {
            money = timeLength * 300.0;
        } else {
            money = timeLength * 100.0;
        }
        tvMoney.setText(money + "");

        //清空优惠信息
        tvCoupon.setText("");
        tvCoupon.setTag(null);
        ((View) tvEndMoney.getParent()).setVisibility(View.GONE);
    }

    private void findProducts() {
        ApiHttpClient.getInstance().findProducts(SharePreferencesUtil.getInstance().readUser().getUid(), new ResultResponseHandler(getActivity()) {

            @Override
            public void onResultSuccess(String result) {
                userProduct = new UserProductResponseParser().parseResultMessage(result);
                initSubjectOptions(userProduct.getSubject());
            }
        });
    }

    private void initSubjectOptions(List<ProductSubject> productSubjects) {
        this.productSubjectList = productSubjects;
        tvSubjectOptions = new OptionsPopupWindow(getActivity());
        //后台获取数据
        for (int i = 0; i < productSubjects.size(); i++) {
            optionsItemsSubject.add(productSubjects.get(i).getSubjectname());
        }
        tvSubjectOptions.setPicker(optionsItemsSubject, null, null, true);
        tvSubjectOptions.setSelectOptions(0);
        tvSubjectOptions.setOnoptionsSelectListener(new OptionsPopupWindow.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                ProductSubject productSubject = productSubjectList.get(options1);
                tv_subject.setText(productSubject.getSubjectname());
                tv_subject.setTag(productSubject);
            }
        });
    }

    @Override
    public void onClick(View v) {
        InputMethodManager inputmanger = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        int i = v.getId();
        if (i == R.id.tv_next) {
            createResident();

        } else if (i == R.id.tv_time) {//隐藏虚拟键盘
            inputmanger.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
            birthOptions.showAtLocation(tv_time, Gravity.BOTTOM, 0, 0);
            //如果第一次选择，初始化时间，往后延3小时10分钟
            if (TextUtils.isEmpty(tv_time.getText())) {
                birthOptions.setTime(new Date(System.currentTimeMillis() + time + (1000 * 600)));
            }

        } else if (i == R.id.tv_locale) {
            StudyAddressFragment fragment = new StudyAddressFragment();
            replaceFrg(fragment, null);

        } else if (i == R.id.tv_timeLength) {
            inputmanger.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
            timeLengthOptions.showAtLocation(tv_timeLength, Gravity.BOTTOM, 0, 0);

        } else if (i == R.id.tv_subject) {
            if (tvSubjectOptions == null) {
                findProducts();
            } else {
                inputmanger.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                tvSubjectOptions.showAtLocation(tv_subject, Gravity.BOTTOM, 0, 0);
            }
        } else if (i == R.id.tv_style) {
            inputmanger.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
            tvStyleOptions.showAtLocation(tv_style, Gravity.BOTTOM, 0, 0);

        } else if (i == R.id.learn_apply_frg_tv_coupon) {
            //优惠卷
            if (!TextUtils.isEmpty(tvMoney.getText().toString())) {
                double result = Double.parseDouble(tvMoney.getText().toString());
                StudyCouponFragment fragment = StudyCouponFragment.newInstance(ConstantsParams.COUPON_ENABLE, result);
                replaceFrg(fragment, null);
            }
        } else if (i == R.id.tv_telephone) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setData(ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (data == null) {
                    return;
                }
                Uri result = data.getData();
                String contactId = result.getLastPathSegment();

                String name = "";
                String phone = "";

                //得到名称
                String[] projection = new String[]{Contacts.People._ID, Contacts.People.NAME, Contacts.People.NUMBER};
                Cursor cursor = getContext().getContentResolver().query(Contacts.People.CONTENT_URI,
                        projection, // select sentence
                        Contacts.People._ID + " = ?", // where sentence
                        new String[]{contactId}, // where values
                        Contacts.People.NAME); // order by

                if (cursor.moveToFirst()) {
                    name = cursor.getString(cursor.getColumnIndex(Contacts.People.NAME));
                }

                //得到 电话
                projection = new String[]{Contacts.Phones.PERSON_ID, Contacts.Phones.NUMBER};
                cursor = getContext().getContentResolver().query(Contacts.Phones.CONTENT_URI,
                        projection, // select sentence
                        Contacts.Phones.PERSON_ID + " = ?", // where sentence
                        new String[]{contactId}, // where values
                        Contacts.Phones.NAME); // order by

                if (cursor.moveToFirst()) {
                    phone = cursor.getString(cursor.getColumnIndex(Contacts.Phones.NUMBER));
                }

                //显示
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                    Util.showCustomMsg("请选择正确联系人");
                } else {
                    tv_telephone.setText(name + ":" + phone);
                }
                break;
        }
    }

    private void initBirthOptions() {
        birthOptions = new TimePopupWindow(getActivity(), TimePopupWindow.Type.ALL);
        birthOptions.setOnTimeSelectListener(new TimePopupWindow.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                //选择时间必须大于现在3小时
                if (date.getTime() - System.currentTimeMillis() > time) {
                    tv_time.setText(UtilDate.formatDatetime(date));
                } else {
                    Util.showCustomMsg("预约练车时间必须提前3小时");
                }
            }
        });
    }


    private void createResident() {
        //必填
        String tvSubject = tv_subject.getText().toString().trim();//培训科目
        String tvTime = tv_time.getText().toString().trim();//预约时间
        String tvLocale = tv_locale.getText().toString().trim();//练车地点
        String tvTimeLength = tv_timeLength.getText().toString().trim();//练车时长
        String tvStyle = tv_style.getText().toString().trim();//服务类型
        boolean isReplace = jpushButton.isChecked();
        String tvTelephone = tv_telephone.getText().toString().trim();//联系方式


        if (TextUtils.isEmpty(tvTime) || TextUtils.isEmpty(tvLocale) || TextUtils.isEmpty(tvTimeLength) || TextUtils.isEmpty(tvSubject) || TextUtils.isEmpty(tvStyle)) {
            Util.showCustomMsg("请输入完整信息");
            return;
        }
        if (isReplace && TextUtils.isEmpty(tvTelephone)) {
            Util.showCustomMsg("请选择联系人");
            return;
        }

        orderDetail = new OrderCreateRequest();

        ProductSubject productSubject = (ProductSubject) tv_subject.getTag();//培训科目
        int timeLength = (int) tv_timeLength.getTag();//练车时长

        CloudPoiInfo poiInfo = (CloudPoiInfo) tv_locale.getTag();

//        * 联系人姓名	contactsname	string	isreplace为true时必传
//        * 联系人电话	contactsphone	string	isreplace为true时必传
//        * 优惠券ID	vid	string	非必传，格式:多个ID用','分割
        orderDetail.setLatitude(poiInfo.latitude);
        orderDetail.setLongitude(poiInfo.longitude);
        orderDetail.setSubjectid(productSubject.getSid());
        orderDetail.setSubjectname(productSubject.getSubjectname());
        orderDetail.setIsvip((boolean) tv_style.getTag());
        orderDetail.setCarsname(userProduct.getCars().get(0).getCarsname());
        orderDetail.setCarsid(userProduct.getCars().get(0).getCid());
        orderDetail.setUid(SharePreferencesUtil.getInstance().readUser().getUid());
        orderDetail.setNumber(timeLength);
        orderDetail.setStarttime(tv_time.getText().toString());
        orderDetail.setLoginname(SharePreferencesUtil.getInstance().readUser().getLoginname());
        orderDetail.setNickname(SharePreferencesUtil.getInstance().readUser().getNickname());

        if (tvCoupon.getTag() != null) {
            orderDetail.setVid(((Coupon.VouchersEntity) tvCoupon.getTag()).getId());
        }

        if (isReplace) {
            orderDetail.setIsreplace(isReplace);//需要动态获取
            String[] arr = tvTelephone.split(":");
            orderDetail.setContactsname(arr[0]);
            orderDetail.setContactsphone(arr[1]);
        } else {
            orderDetail.setIsreplace(isReplace);//需要动态获取
        }

        getAccount();
    }

    /**
     * 查询账户余额
     */
    private void getAccount() {
        ApiHttpClient.getInstance().getMyAccount(SharePreferencesUtil.getInstance().readUser().getUid(), new ResultResponseHandler(getActivity()) {

            @Override
            public void onResultSuccess(String result) {
                AccountBalance accountBalance = new AccountBalanceParser().parseResultMessage(result);
                double money = Double.parseDouble(tvMoney.getText().toString().trim());//价格
                if (accountBalance.getBalance() > money) {
                    commitOrder();
                } else {
                    Util.showCustomMsg("余额不足，请充值");
                    startActivity(AccountManagerActivity.class);
                }
            }
        });
    }

    private void commitOrder() {
        ApiHttpClient.getInstance().learnApply(orderDetail, new ResultResponseHandler(getActivity(), "正在提交，请稍等") {
            @Override
            public void onResultSuccess(String result) {
                getActivity().finish();
            }
        });
    }

    public void onEventMainThread(StudyAddressChooseEvent event) {
        CloudPoiInfo poiInfo = event.getPoiInfo();
        tv_locale.setText(poiInfo.title);
        tv_locale.setTag(poiInfo);
    }

    public void onEventMainThread(StudyCouponChooseEvent event) {
        Coupon.VouchersEntity vouchersEntity = event.getVouchersEntity();
        tvCoupon.setText(vouchersEntity.getVname() + "        ¥" + vouchersEntity.getAmount());
        tvCoupon.setTag(vouchersEntity);
        //设置实际金额
        double result = Double.parseDouble(tvMoney.getText().toString()) - vouchersEntity.getAmount();
        tvEndMoney.setText("" + result);
        ((View) tvEndMoney.getParent()).setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
