package com.flashhop.app.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.adapters.TipsAdapter;
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.models.CardModel;
import com.flashhop.app.models.TipModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.drakeet.support.toast.ToastCompat;

import static com.flashhop.app.utils.Const.APP_TAG;

public class TipsFragment extends DialogFragment {

    Context context;

    View root_view;
    ImageView iv_back;
    TextView tv_receive, tv_give, tv_total;
    RecyclerView tipRecycler;
    LinearLayout ll_total;
    TipsAdapter tipsAdapter;
    //AppCompatSpinner historySpinner;
    EditText et_date;
    List<TipModel> tip_list = new ArrayList<>();

    MySQLiteHelper mHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_tips, container, false);

        context = getContext();
        mHelper = MySQLiteHelper.getInstance(context);

        initLayout();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.tips_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        tv_receive = root_view.findViewById(R.id.tips_frag_tv_receive);
        tv_give = root_view.findViewById(R.id.tips_frag_tv_give);
        tv_total = root_view.findViewById(R.id.tips_frag_tv_total);
        ll_total = root_view.findViewById(R.id.tips_frag_ll_total);
        ll_total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyApp.curUser.is_debit == 0){
                    showConfirmDlg();
                }
            }
        });

        tipRecycler = root_view.findViewById(R.id.tips_frag_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        tipRecycler.setLayoutManager(layoutManager);

        tipsAdapter= new TipsAdapter(tip_list, context);
        tipRecycler.setAdapter(tipsAdapter);

        //historySpinner = root_view.findViewById(R.id.tips_frag_payout_history);
        et_date = root_view.findViewById(R.id.tips_frag_et_date);
        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.MyDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                try{
                                    DateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                                    String inputDateStr=String.format("%02d/%02d/%d", monthOfYear + 1, dayOfMonth, year);
                                    Date date = inputFormat.parse(inputDateStr + " 23:59:59");

                                    et_date.setTypeface(MyApp.myfont_normal);
                                    et_date.setText(inputDateStr);
                                    long ltime = date.getTime()/1000;
                                    loadData(ltime + "");
                                } catch (ParseException ex){
                                    ex.printStackTrace();
                                }
                            }
                        }, mYear, mMonth, mDay);

                try{
                    SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date sDt = sdf0.parse(MyApp.curUser.created_at);
                    datePickerDialog.getDatePicker().setMinDate(sDt.getTime());
                } catch (ParseException ex){
                    ex.printStackTrace();
                }
                Date dt = new Date();
                datePickerDialog.getDatePicker().setMaxDate(dt.getTime());
                datePickerDialog.show();
            }
        });
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date dt = new Date();
        long ltimestamp = dt.getTime()/1000;
        String limitdate = sdf.format(dt);
        SimpleDateFormat sdf0 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        try {
            Date dt0 = sdf0.parse(limitdate + " 23:59:59");
            if(dt0 != null){
                ltimestamp = dt0.getTime()/1000;
            }
        } catch (ParseException ex) {
            Log.v("Exception", ex.getLocalizedMessage());
        }
        loadData(ltimestamp + "");

    }

    private void refreshLayout(){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date dt = new Date();
        String limitdate = sdf.format(dt);
        et_date.setText(limitdate);
    }

    private void loadData(String sTimestamp){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Getting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.GET_PAYMENT_TRANSACTION)
                .addBodyParameter("user_id", MyApp.curUser.uid)
                .addBodyParameter("date_timestamp", sTimestamp)
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
                                tip_list.clear();
                                JSONObject data_obj = response.getJSONObject("data");
                                JSONArray total_obj = data_obj.getJSONArray("total_earnings");
                                for(int j = 0; j < total_obj.length(); j++){
                                    JSONObject obj = total_obj.getJSONObject(j);
                                    String currency = obj.getString("currency");
                                    if(currency.toLowerCase().equals("cad")){
                                        String in_total = obj.getString("amount");
                                        tv_receive.setText("$" + in_total);
                                        //tv_total.setText("$" + stotal);
                                        break;
                                    }
                                }

                                JSONArray available_obj = data_obj.getJSONArray("available_balances");
                                for(int j = 0; j < available_obj.length(); j++){
                                    JSONObject obj = available_obj.getJSONObject(j);
                                    String currency = obj.getString("currency");
                                    if(currency.toLowerCase().equals("cad")){
                                        String in_total = obj.getString("amount");
                                        tv_give.setText("$" + in_total);
                                        break;
                                    }
                                }

                                JSONArray user_array = data_obj.getJSONArray("data");
                                for(int i = 0; i < user_array.length(); i++){
                                    TipModel one = new TipModel();
                                    JSONObject tip_one = user_array.getJSONObject(i);
                                    one.tid = tip_one.getString("id");
                                    long created = tip_one.getLong("created");
                                    Date rvDt = new Date(created * 1000);
                                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                                    one.datestr = sdf.format(rvDt);

                                    JSONObject detail_obj = tip_one.getJSONObject("details");
                                    one.receiving = detail_obj.getString("net");
                                    JSONObject user_obj = detail_obj.getJSONObject("sender");
                                    one.uid = user_obj.getString("id");
                                    one.name = user_obj.getString("first_name");
                                    one.photo = user_obj.getString("avatar");

                                    tip_list.add(one);
                                }
                                tipsAdapter.setDataList(tip_list);

                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
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

    private void showConfirmDlg(){
        TextView tv_desc;
        Button btn_cancel, btn_sure;
        final Dialog backDlg = new Dialog(context, R.style.FullHeightDialog);
        backDlg.setContentView(R.layout.custom_confirm_dlg);
        View dview = backDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = backDlg.findViewById(R.id.custom_confirm_tv_desc);
        btn_cancel = backDlg.findViewById(R.id.custom_confirm_btn_cancel);
        btn_sure = backDlg.findViewById(R.id.custom_confirm_btn_sure);

        tv_desc.setText(R.string.payment_withdraw_desc);
        btn_sure.setText(R.string.btn_continue);
        btn_cancel.setText(R.string.btn_not_now);
        backDlg.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                backDlg.dismiss();
            }
        });

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backDlg.dismiss();
                showPaymentDlg();
            }
        });
    }

    CardModel cData;
    Dialog pdialog;

    private void showPaymentDlg(){
        final Button btn_next_d;
        final CheckBox cb_save_d;
        final TextView tv_title_d, tv_cardnum_d, tv_expire_d, tv_cvv_d, tv_holder_d;
        final EditText et_cardnum_d, et_holder_d, et_expire_d, et_cvv_d, et_addr_name_d, et_postcode_d, et_province_d, et_city_d;
        final LinearLayout ll_card_d, ll_info_d, ll_card_def;
        pdialog = new Dialog(context, R.style.FullHeightDialog);
        pdialog.setContentView(R.layout.payment_debit_dlg);
        View dview = pdialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        dview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
        btn_next_d = pdialog.findViewById(R.id.payment_debit_btn_save);
        tv_title_d = pdialog.findViewById(R.id.payment_debit_tv_title);
        tv_cardnum_d = pdialog.findViewById(R.id.payment_debit_tv_number);
        tv_holder_d = pdialog.findViewById(R.id.payment_debit_tv_holder);
        tv_expire_d = pdialog.findViewById(R.id.payment_debit_tv_expire);
        tv_cvv_d = pdialog.findViewById(R.id.payment_debit_tv_cvv);
        et_cardnum_d = pdialog.findViewById(R.id.payment_debit_et_number);
        et_holder_d = pdialog.findViewById(R.id.payment_debit_et_name);
        et_expire_d = pdialog.findViewById(R.id.payment_debit_et_expire);
        et_cvv_d = pdialog.findViewById(R.id.payment_debit_et_cvv);
        et_addr_name_d = pdialog.findViewById(R.id.payment_debit_et_address);
        et_province_d = pdialog.findViewById(R.id.payment_debit_et_province);
        et_city_d = pdialog.findViewById(R.id.payment_debit_et_city);
        et_postcode_d = pdialog.findViewById(R.id.payment_debit_et_postcode);
        ll_card_d = pdialog.findViewById(R.id.payment_debit_ll_card);
        ll_info_d = pdialog.findViewById(R.id.payment_debit_ll_info);
        cb_save_d = pdialog.findViewById(R.id.payment_debit_cb_save);
        ll_card_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
        ll_info_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

        cData = mHelper.getCardInfo();
        if(cData.card_num != null && cData.card_num.length() > 0){
            String sNum = cData.card_num.substring(cData.card_num.length() - 4);
            et_cardnum_d.setText("xxxx xxxx xxxx " + sNum);
            et_holder_d.setText(cData.holder_name);
            et_expire_d.setText(cData.expire);
            et_cvv_d.setText(cData.cvv);
            et_addr_name_d.setText(cData.street_name);
            et_city_d.setText(cData.city);
            et_province_d.setText(cData.province);
            et_postcode_d.setText(cData.post_code);
        }

        ll_card_d.setVisibility(View.VISIBLE);
        cb_save_d.setText("  " + getString(R.string.payment_save_card));
        pdialog.show();

        btn_next_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sNum = et_cardnum_d.getText().toString().trim();
                boolean bErr = false;
                if(sNum.length() != 19){
                    et_cardnum_d.setText("");
                    bErr = true;
                }
                if(et_holder_d.getText().toString().trim().length() == 0){
                    et_holder_d.setText("");
                    bErr = true;
                }
                String sExp = et_expire_d.getText().toString().trim();

                if(sExp.length() < 4 || !TxtUtils.isValidExpireDate(sExp)){
                    et_expire_d.setText("");
                    bErr = true;
                }
                String sCVV = et_cvv_d.getText().toString().trim();
                /*if(sCVV.length() < 3){
                    et_cvv_d.setText("");
                    bErr = true;
                }*/
                if(et_addr_name_d.getText().toString().trim().length() == 0){
                    et_addr_name_d.setText("");
                    bErr = true;
                }
                if(et_city_d.getText().toString().trim().length() == 0){
                    et_city_d.setText("");
                    bErr = true;
                }
                if(et_province_d.getText().toString().trim().length() == 0){
                    et_province_d.setText("");
                    bErr = true;
                }
                if(et_postcode_d.getText().toString().trim().length() == 0){
                    et_postcode_d.setText("");
                    bErr = true;
                }

                if(!bErr){
                    if(cb_save_d.isChecked()){
                        cData.card_num = sNum;
                        cData.holder_name = et_holder_d.getText().toString().trim();
                        cData.expire = sExp;
                        cData.cvv = et_cvv_d.getText().toString().trim();
                        cData.street_name = et_addr_name_d.getText().toString().trim();
                        cData.city = et_city_d.getText().toString().trim();
                        cData.province = et_province_d.getText().toString().trim();
                        cData.post_code = et_postcode_d.getText().toString().trim();
                        mHelper.putCardInfo(cData);
                    }
                    String hname = et_holder_d.getText().toString().trim();
                    String sAddr = et_addr_name_d.getText().toString().trim();
                    String sCvv = et_cvv_d.getText().toString().trim();
                    String sCity = et_city_d.getText().toString().trim();
                    String sProvince = et_province_d.getText().toString().trim();;
                    String sPost = et_postcode_d.getText().toString().trim();
                    //Call API
                    updateDebitCard(sNum, hname, sExp, sCvv, sAddr, sCity, sProvince, sPost);
                }
            }
        });

        et_cardnum_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_cardnum_d.getText().toString().trim();
                int textlength = text.length();

                if(textlength == 0){
                    tv_cardnum_d.setText(R.string.err_card_number);
                    tv_cardnum_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_cardnum_d.setText(R.string.payment_card_number);
                    tv_cardnum_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }

                if(text.endsWith(" "))
                    return;

                if(textlength == 5 || textlength == 10 || textlength == 15)
                {
                    et_cardnum_d.setText(new StringBuilder(text).insert(text.length()-1, " ").toString());
                    et_cardnum_d.setSelection(et_cardnum_d.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_holder_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int textlength = et_holder_d.getText().toString().trim().length();
                if(textlength == 0){
                    tv_holder_d.setText("*Cardholder Name");
                    tv_holder_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_holder_d.setText(R.string.payment_cardholder);
                    tv_holder_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_expire_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_expire_d.getText().toString().trim();
                int textlength = text.length();

                if(textlength == 0){
                    tv_expire_d.setText(R.string.err_expire);
                    tv_expire_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_expire_d.setText(R.string.payment_expiration);
                    tv_expire_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }

                if(text.endsWith("/"))
                    return;

                if(textlength == 3)
                {
                    et_expire_d.setText(new StringBuilder(text).insert(text.length()-1, "/").toString());
                    et_expire_d.setSelection(et_expire_d.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_cvv_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*int textlength = et_cvv_d.getText().toString().trim().length();
                if(textlength == 0){
                    tv_cvv_d.setText(R.string.err_cvv);
                    tv_cvv_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_cvv_d.setText(R.string.payment_cvv);
                    tv_cvv_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }*/
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_addr_name_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_addr_name_d.getText().toString().trim().length() == 0){
                    et_addr_name_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_city_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_city_d.getText().toString().trim().length() == 0){
                    et_city_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_province_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_province_d.getText().toString().trim().length() == 0){
                    et_province_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_postcode_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_postcode_d.getText().toString().trim().length() == 0){
                    et_postcode_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                        pdialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)(getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
