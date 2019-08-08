package com.flashhop.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.adapters.SettingPaymentAdapter;
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.CardModel;
import com.flashhop.app.utils.Const;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.drakeet.support.toast.ToastCompat;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingPaymentFrag extends DialogFragment {

    Context context;

    View root_view;
    ImageView iv_back, iv_check;
    Button btn_save, btn_edit;
    TextView tv_number, tv_expire, tv_cvv, tv_holdername;
    EditText et_cardNum, et_holder, et_expire, et_cvv, et_addrNo, et_addrStreet, et_addrCity, et_post, et_province;
    RelativeLayout rl_checkinfo;
    LinearLayout ll_edit;
    RecyclerView cardsRecycler;
    SettingPaymentAdapter cardsAdapter;
    List<CardModel> card_list = new ArrayList<>();
    String sCard;
    MySQLiteHelper mHelper;
    CardModel cData = new CardModel();
    int selpos = -1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_payment, container, false);
        context = getContext();
        mHelper = MySQLiteHelper.getInstance(getContext());
        initLayout();
        loadData();
        return root_view;
    }

    private void initLayout(){
        iv_check = root_view.findViewById(R.id.setting_payment_frag_iv_check);
        iv_back = root_view.findViewById(R.id.setting_payment_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(rl_checkinfo.getVisibility() == View.VISIBLE){
                    dismiss();
                //} else{
                //    loadData();
                //}
            }
        });

        rl_checkinfo = root_view.findViewById(R.id.setting_payment_frag_rl_check_info);
        ll_edit = root_view.findViewById(R.id.setting_payment_frag_ll_edit);

        btn_save = root_view.findViewById(R.id.setting_payment_frag_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkValidFields()){
                    cData.card_num = et_cardNum.getText().toString().trim();
                    cData.holder_name = et_holder.getText().toString().trim();
                    cData.expire = et_expire.getText().toString().trim();
                    cData.cvv = et_cvv.getText().toString().trim();
                    //cData.street_no = et_addrNo.getText().toString().trim();
                    cData.street_name = et_addrStreet.getText().toString().trim();
                    cData.city = et_addrCity.getText().toString().trim();
                    cData.province = et_province.getText().toString().trim();
                    cData.post_code = et_post.getText().toString().trim();
                    mHelper.putCardInfo(cData);

                    updateDebitCard(cData.card_num, cData.holder_name, cData.expire, cData.cvv, cData.street_name, cData.city, cData.province, cData.post_code);
                }
            }
        });

        rl_checkinfo.setVisibility(View.VISIBLE);
        ll_edit.setVisibility(View.GONE);

        tv_number = root_view.findViewById(R.id.setting_payment_detail_tv_number);
        tv_expire = root_view.findViewById(R.id.setting_payment_detail_tv_expire);
        tv_holdername = root_view.findViewById(R.id.setting_payment_detail_tv_name);
        tv_cvv = root_view.findViewById(R.id.setting_payment_detail_tv_cvv);
        et_cardNum = root_view.findViewById(R.id.setting_payment_detail_et_number);
        et_cardNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_cardNum.getText().toString();
                int textlength = et_cardNum.getText().length();

                if(textlength == 0){
                    setErrorEnable(et_cardNum, tv_number);
                    tv_number.setText("*" + getString(R.string.payment_card_number));
                } else{
                    setErrorDisable(et_cardNum, tv_number);
                    tv_number.setText(R.string.payment_card_number);
                }

                if(text.endsWith(" "))
                    return;

                if(textlength == 5 || textlength == 10 || textlength == 15)
                {
                    et_cardNum.setText(new StringBuilder(text).insert(text.length()-1, " ").toString());
                    et_cardNum.setSelection(et_cardNum.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_holder = root_view.findViewById(R.id.setting_payment_detail_et_name);
        et_holder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int textlength = et_holder.getText().length();

                if(textlength == 0){
                    setErrorEnable(et_holder, tv_holdername);
                    tv_holdername.setText("*" + getString(R.string.payment_cardholder));
                } else{
                    setErrorDisable(et_holder, tv_holdername);
                    tv_holdername.setText(R.string.payment_cardholder);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_expire = root_view.findViewById(R.id.setting_payment_detail_et_expire);
        et_expire.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_expire.getText().toString();
                int textlength = et_expire.getText().length();

                if(textlength == 0){
                    setErrorEnable(et_expire, tv_expire);
                    tv_expire.setText("*" + getString(R.string.payment_expiration));
                } else{
                    setErrorDisable(et_expire, tv_expire);
                    tv_expire.setText(getString(R.string.payment_expiration));
                }

                if(text.endsWith("/"))
                    return;

                if(textlength == 3)
                {
                    et_expire.setText(new StringBuilder(text).insert(text.length()-1, "/").toString());
                    et_expire.setSelection(et_expire.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_cvv = root_view.findViewById(R.id.setting_payment_detail_et_cvv);
        et_cvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    setErrorEnable(et_cvv, tv_cvv);
                    tv_cvv.setText("*" + getString(R.string.payment_cvv));
                } else{
                    setErrorDisable(et_expire, tv_cvv);
                    tv_cvv.setText(getString(R.string.payment_cvv));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_addrStreet = root_view.findViewById(R.id.setting_payment_detail_et_address);
        et_addrStreet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    et_addrStreet.setHintTextColor(ContextCompat.getColor(context, R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_addrCity = root_view.findViewById(R.id.setting_payment_detail_et_city);
        et_addrCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    et_addrCity.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_post = root_view.findViewById(R.id.setting_payment_detail_et_postcode);
        et_post.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    et_post.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_province = root_view.findViewById(R.id.setting_payment_detail_et_province);
        et_province.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    et_province.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        cardsRecycler = root_view.findViewById(R.id.setting_payment_frag_card_recycler);
        LinearLayoutManager todayManager = new LinearLayoutManager(getContext());
        cardsRecycler.setLayoutManager(todayManager);
        cardsAdapter = new SettingPaymentAdapter(card_list, getContext());
        cardsRecycler.setAdapter(cardsAdapter);
        cardsAdapter.setOnItemClickListener(new SettingPaymentAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                selpos = pos;
                rl_checkinfo.setVisibility(View.GONE);
                ll_edit.setVisibility(View.VISIBLE);
                cData = card_list.get(pos);
                refreshLayout();
                return pos;
            }
        });

    }

    private void refreshLayout(){
        if(cData.card_num != null && cData.card_num.length() > 0){
            String sNum = cData.card_num.substring(cData.card_num.length() - 4);
            et_cardNum.setText("xxxx xxxx xxxx " + sNum);
            et_holder.setText(cData.holder_name);
            String ex_str = cData.expire.substring(0, 1) + "/" + cData.expire.substring(cData.expire.length() - 2);
            et_expire.setText(ex_str);
            et_cvv.setText(cData.cvv);
            //et_addrNo.setText(cData.street_no);
            et_addrStreet.setText(cData.street_name);
            et_addrCity.setText(cData.city);
            et_province.setText(cData.province);
            et_post.setText(cData.post_code);
            if(cData.cvv.equals("123")){
                iv_check.setVisibility(View.VISIBLE);
            } else{
                iv_check.setVisibility(View.GONE);
            }

        } else{
            rl_checkinfo.setVisibility(View.GONE);
            ll_edit.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkValidFields(){
        String sNum = et_cardNum.getText().toString().trim();
        String sHolder = et_holder.getText().toString().trim();
        String sExpire = et_expire.getText().toString().trim();
        String sCvv = et_cvv.getText().toString().trim();
        boolean bnum, bhol, bexp, bSname, bCity, bProvince, bPost, bcvv;
        if(sNum.length() == 19){
            bnum = true;
        } else{
            bnum = false;
            setErrorEnable(et_cardNum, tv_number);
            tv_number.setText("*" + getString(R.string.payment_card_number));
        }

        if(sHolder.length() > 0){
            bhol = true;
        } else{
            bhol = false;
            setErrorEnable(et_holder, tv_holdername);
            tv_holdername.setText("*" + getString(R.string.payment_cardholder));
        }

        if(sExpire.length() == 5 || sExpire.length() == 4){
            String[] sDD = sExpire.split("/");
            int month = Integer.valueOf(sDD[0]);
            int year = Integer.valueOf(sDD[1]);
            if(month > 0 && month < 13 && year > 18){
                bexp = true;
            } else{
                bexp = false;
                setErrorEnable(et_expire, tv_expire);
                tv_expire.setText("*" + getString(R.string.payment_expiration));
            }

        } else{
            bexp = false;
            setErrorEnable(et_expire, tv_expire);
            tv_expire.setText("*" + getString(R.string.payment_expiration));
        }

        if(sCvv.length() > 2){
            bcvv = true;
        } else{
            bcvv = false;
            setErrorEnable(et_cvv, tv_cvv);
            tv_cvv.setText("*" + getString(R.string.payment_cvv));
        }
        /*if(et_addrNo.getText().toString().trim().length() > 0){
            bSno = true;
        } else{
            bSno = false;
            et_addrNo.setText("");
        }*/
        if(et_addrStreet.getText().toString().trim().length() > 0){
            bSname = true;
        } else{
            bSname = false;
            et_addrStreet.setText("");
        }
        if(et_addrCity.getText().toString().trim().length() > 0){
            bCity = true;
        } else{
            bCity = false;
            et_addrCity.setText("");
        }
        if(et_province.getText().toString().trim().length() > 0){
            bProvince = true;
        } else{
            bProvince = false;
            et_province.setText("");
        }
        if(et_post.getText().toString().trim().length() > 0){
            bPost = true;
        } else{
            bPost = false;
            et_post.setText("");
        }
        return bnum && bhol && bexp && bSname && bCity && bPost && bProvince && bcvv;
    }

    private void setErrorDisable(EditText edt, TextView tv){
        edt.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorGray), PorterDuff.Mode.SRC_ATOP);
        edt.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGray));
        tv.setTypeface(MyApp.myfont_normal);
    }

    private void setErrorEnable(EditText edt, TextView tv){
        edt.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorRed), PorterDuff.Mode.SRC_ATOP);
        edt.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
        tv.setTypeface(MyApp.myfont_italic);
    }

    private void loadData(){
        /*cData = mHelper.getCardInfo();
        if(cData.card_num != null && cData.card_num.length() > 0){
            String sNum = cData.card_num.substring(cData.card_num.length() - 4);
            et_cardNum.setText("xxxx xxxx xxxx " + sNum);
            et_holder.setText(cData.holder_name);
            et_expire.setText(cData.expire);
            et_cvv.setText(cData.cvv);
            et_addrNo.setText(cData.street_no);
            et_addrStreet.setText(cData.street_name);
            et_addrCity.setText(cData.city);
            et_province.setText(cData.province);
            et_post.setText(cData.post_code);
            iv_check.setVisibility(View.VISIBLE);
            rl_checkinfo.setVisibility(View.VISIBLE);
            ll_edit.setVisibility(View.GONE);
        } else{
            iv_check.setVisibility(View.GONE);
            rl_checkinfo.setVisibility(View.GONE);
            ll_edit.setVisibility(View.VISIBLE);
        }
        card_list.clear();
        card_list.add(cData);
        cardsAdapter.setDataList(card_list);*/
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Loading...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.GET_DEBIT_CARD)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("user_id", MyApp.curUser.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                MyApp.curUser.is_debit = 1;
                                JSONObject data_obj = response.getJSONObject("data");
                                cData.card_num = "xxxx xxxx xxxx " + data_obj.getString("last4");
                                cData.card_id = data_obj.getString("id");
                                cData.expire = data_obj.getString("exp_month") + "/" + data_obj.getString("exp_year");
                                String val = data_obj.getString("cvc_check");
                                if(val.equals("pass")){
                                    cData.cvv = "123";
                                } else{
                                    cData.cvv = "";
                                }
                                val = data_obj.getString("address_city");
                                if(val.equals("null")){
                                    cData.city = "";
                                } else{
                                    cData.city = val;
                                }
                                val = data_obj.getString("address_line1");
                                if(val.equals("null")){
                                    cData.street_name = "";
                                } else{
                                    cData.street_name = val;
                                }
                                val = data_obj.getString("address_state");
                                if(val.equals("null")){
                                    cData.province = "";
                                } else{
                                    cData.province = val;
                                }
                                val = data_obj.getString("address_zip");
                                if(val.equals("null")){
                                    cData.post_code = "";
                                } else{
                                    cData.post_code = val;
                                }
                                card_list.clear();
                                card_list.add(cData);
                                cardsAdapter.setDataList(card_list);
                                refreshLayout();
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        iv_check.setVisibility(View.GONE);
                        rl_checkinfo.setVisibility(View.GONE);
                        ll_edit.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void updateDebitCard(String cardnum, String holdername, String expiredt, String cvc, String addr, String city, String province, String postcode){

        String[] exp_str = expiredt.split("/");
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Adding...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.UPDATE_DEBIT_ACCOUNT)
                .addBodyParameter("user_id", MyApp.curUser.uid)
                .addBodyParameter("card_number", cardnum)
                .addBodyParameter("holder_name", holdername)
                .addBodyParameter("exp_month", exp_str[0])
                .addBodyParameter("exp_year", "20" + exp_str[1])
                .addBodyParameter("card_cvc", cvc)
                .addBodyParameter("address_city", city)
                .addBodyParameter("address_line1", addr)
                .addBodyParameter("address_postal_code", postcode)
                .addBodyParameter("address_state", province)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                MyApp.curUser.is_debit = 1;
                                rl_checkinfo.setVisibility(View.VISIBLE);
                                ll_edit.setVisibility(View.GONE);
                                loadData();
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        String err = anError.getErrorBody();
                        ToastCompat.makeText(context, err, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
