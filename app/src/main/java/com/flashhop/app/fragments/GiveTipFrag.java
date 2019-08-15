package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.models.CardModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.KeyboardUtil;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import me.drakeet.support.toast.ToastCompat;

import static com.flashhop.app.utils.Const.APP_TAG;

public class GiveTipFrag extends DialogFragment {
    View root_view;

    LinearLayout ll_outside;
    ImageView iv_back;
    CircleImageView civ_avatar;
    TextView tv_name;
    TextView tv_one, tv_two, tv_five, tv_ten;
    EditText et_custom;
    Button btn_done;
    int amount = 0;
    UserModel other = new UserModel();
    MySQLiteHelper mHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_give_tip, container, false);
        mHelper = MySQLiteHelper.getInstance(getContext());

        initLayout();
        return root_view;
    }

    private void initLayout(){
        ll_outside = root_view.findViewById(R.id.give_tip_frag_bg);
        ll_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtil.hideKeyboard(getActivity());
            }
        });
        iv_back = root_view.findViewById(R.id.give_tip_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        civ_avatar = root_view.findViewById(R.id.give_tip_frag_civ_avatar);
        tv_name = root_view.findViewById(R.id.give_tip_frag_tv_name);
        tv_one = root_view.findViewById(R.id.give_tip_frag_tv_1);
        tv_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = 1;
                refreshLayout();
            }
        });
        tv_two = root_view.findViewById(R.id.give_tip_frag_tv_2);
        tv_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = 2;
                refreshLayout();
            }
        });
        tv_five = root_view.findViewById(R.id.give_tip_frag_tv_5);
        tv_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = 5;
                refreshLayout();
            }
        });
        tv_ten = root_view.findViewById(R.id.give_tip_frag_tv_10);
        tv_ten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = 10;
                refreshLayout();
            }
        });
        et_custom = root_view.findViewById(R.id.give_tip_frag_et_amount);
        et_custom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_custom.getText().toString();
                int textlength = et_custom.getText().length();

                if(text.startsWith("$"))
                    return;

                if(textlength == 1)
                {
                    et_custom.setText(new StringBuilder(text).insert(0, "$").toString());
                    et_custom.setSelection(et_custom.getText().length());
                }

                if(charSequence.length() > 0){
                    amount = 0;
                    refreshLayout();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btn_done = root_view.findViewById(R.id.give_tip_frag_btn_Done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveTipToUser();
            }
        });

    }

    private void giveTipToUser(){
        float fAmount = (float) amount;
        if(amount == 0){
            if(et_custom.getText().toString().trim().length() > 1){
                String custom_amount_str = et_custom.getText().toString().trim().substring(1);
                fAmount = Float.parseFloat(custom_amount_str);
                //call Api
                showPaymentDlg(fAmount);
                return;
            }
            WindowUtils.animateView(getContext(), btn_done);
        } else{
            //call Api
            showPaymentDlg(fAmount);
        }
    }

    private void callTipApi(CardModel card, float amount){
        String sAmount = String.format("%.2f", amount);
        String card_num = card.card_num.replace(" ", "");
        String[] dd = card.expire.split("/");
        String sExpMonth = Integer.parseInt(dd[0]) + "";
        String sExpYear = "20" + card.expire.substring(card.expire.length() - 2);
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Paying...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.PAY_WITH_STRIPE)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("card_number", card_num)
                .addBodyParameter("exp_month", sExpMonth)
                .addBodyParameter("exp_year", sExpYear)
                .addBodyParameter("cvc", card.cvv)
                .addBodyParameter("amount", sAmount)
                .addBodyParameter("currency", "usd")
                .addBodyParameter("action", "tip")
                .addBodyParameter("receiver_id", other.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try {
                            int bSuccess = response.getInt("success");
                            if (bSuccess == 1) {
                                dismiss();
                            } else{
                                WindowUtils.animateView(getContext(), btn_done);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), btn_done);
                        //ToastCompat.makeText(getContext(), anError.getErrorBody(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshLayout(){
        Picasso.get()
                .load(other.photo_url)
                .into(civ_avatar);
        tv_name.setText(other.first_name);
        switch (amount){
            case 1:
                tv_one.setBackgroundResource(R.drawable.border_circle_tip_green);
                tv_one.setTextColor(ContextCompat.getColor(getContext(),R.color.colorWhite));
                tv_two.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_two.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_five.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_five.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_ten.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_ten.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                break;
            case 2:
                tv_one.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_one.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_two.setBackgroundResource(R.drawable.border_circle_tip_green);
                tv_two.setTextColor(ContextCompat.getColor(getContext(),R.color.colorWhite));
                tv_five.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_five.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_ten.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_ten.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                break;
            case 5:
                tv_one.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_one.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_two.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_two.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_five.setBackgroundResource(R.drawable.border_circle_tip_green);
                tv_five.setTextColor(ContextCompat.getColor(getContext(),R.color.colorWhite));
                tv_ten.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_ten.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                break;
            case 10:
                tv_one.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_one.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_two.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_two.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_five.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_five.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_ten.setBackgroundResource(R.drawable.border_circle_tip_green);
                tv_ten.setTextColor(ContextCompat.getColor(getContext(),R.color.colorWhite));
                break;
            default:
                tv_one.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_one.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_two.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_two.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_five.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_five.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                tv_ten.setBackgroundResource(R.drawable.border_circle_g_w);
                tv_ten.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                break;
        }
    }

    boolean bConfirm;
    CardModel cData;

    private void showPaymentDlg(final float amount){
        final Button btn_cancel_d, btn_next_d;
        final CheckBox cb_save_d;
        final ImageView iv_photo_d;
        final TextView tv_title_d, tv_cardnum_d, tv_expire_d, tv_cvv_d, tv_month_d, tv_day_d, tv_weekday_d, tv_eventtitle_d, tv_time_d, tv_amount_d, tv_holder_d;
        final EditText et_cardnum_d, et_holder_d, et_expire_d, et_cvv_d, et_addr_no_d, et_addr_name_d, et_postcode_d, et_province_d, et_city_d;
        final TextView tv_num_def, tv_holder_def, tv_exp_def, tv_cvv_def;
        final LinearLayout ll_event_d, ll_card_d, ll_info_d, ll_card_def;
        final Dialog pdialog = new Dialog(getContext(), R.style.FullHeightDialog);
        pdialog.setContentView(R.layout.payment_detail_dlg);
        View dview = pdialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        btn_cancel_d  = pdialog.findViewById(R.id.payment_detail_btn_cancel);
        btn_next_d = pdialog.findViewById(R.id.payment_detail_btn_next);
        iv_photo_d = pdialog.findViewById(R.id.payment_detail_iv_image);
        tv_title_d = pdialog.findViewById(R.id.payment_detail_tv_title);
        tv_cardnum_d = pdialog.findViewById(R.id.payment_detail_tv_number);
        tv_holder_d = pdialog.findViewById(R.id.payment_detail_tv_holder);
        tv_expire_d = pdialog.findViewById(R.id.payment_detail_tv_expire);
        tv_cvv_d = pdialog.findViewById(R.id.payment_detail_tv_cvv);
        tv_month_d = pdialog.findViewById(R.id.payment_detail_tv_month);
        tv_day_d = pdialog.findViewById(R.id.payment_detail_tv_day);
        tv_weekday_d = pdialog.findViewById(R.id.payment_detail_tv_weekday);
        tv_eventtitle_d = pdialog.findViewById(R.id.payment_detail_tv_event_title);
        tv_time_d = pdialog.findViewById(R.id.payment_detail_tv_time);
        tv_amount_d = pdialog.findViewById(R.id.payment_detail_tv_amount);
        et_cardnum_d = pdialog.findViewById(R.id.payment_detail_et_number);
        et_holder_d = pdialog.findViewById(R.id.payment_detail_et_name);
        et_expire_d = pdialog.findViewById(R.id.payment_detail_et_expire);
        et_cvv_d = pdialog.findViewById(R.id.payment_detail_et_cvv);
        ll_event_d = pdialog.findViewById(R.id.payment_detail_ll_event);
        ll_card_d = pdialog.findViewById(R.id.payment_detail_ll_card);
        ll_info_d = pdialog.findViewById(R.id.payment_detail_ll_info);
        cb_save_d = pdialog.findViewById(R.id.payment_detail_cb_save);
        tv_num_def = pdialog.findViewById(R.id.payment_detail_tv_number_def);
        tv_exp_def = pdialog.findViewById(R.id.payment_detail_tv_expire_def);
        tv_holder_def = pdialog.findViewById(R.id.payment_detail_tv_name_def);
        tv_cvv_def = pdialog.findViewById(R.id.payment_detail_tv_cvv_def);
        ll_card_def = pdialog.findViewById(R.id.payment_detail_ll_confirm_card);

        String str = String.format("$%.2f USD", amount);
        tv_amount_d.setText(str);

        cData = mHelper.getCardInfo();
        if(cData.card_num != null && cData.card_num.length() > 0){
            String sNum = cData.card_num.substring(cData.card_num.length() - 4);
            et_cardnum_d.setText("xxxx xxxx xxxx " + sNum);
            et_holder_d.setText(cData.holder_name);
            String ex_str = cData.expire.substring(0, 1) + "/" + cData.expire.substring(cData.expire.length() - 2);
            et_expire_d.setText(ex_str);
            et_cvv_d.setText(cData.cvv);
            //et_addr_no_d.setText(cData.street_no);
            //et_addr_name_d.setText(cData.street_name);
            //et_city_d.setText(cData.city);
            //et_province_d.setText(cData.province);
            //et_postcode_d.setText(cData.post_code);
        }

        bConfirm = false;
        btn_cancel_d.setVisibility(View.GONE);
        ll_card_d.setVisibility(View.VISIBLE);
        cb_save_d.setText("  " + getString(R.string.payment_save_card));
        pdialog.show();

        btn_cancel_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdialog.dismiss();
            }
        });

        btn_next_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bConfirm){
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
                    /*if(sCVV.length() != 3){
                        et_cvv_d.setText("");
                        bErr = true;
                    }*/
                    if(!bErr){
                        btn_cancel_d.setVisibility(View.VISIBLE);
                        ll_info_d.setVisibility(View.GONE);
                        ll_card_d.setVisibility(View.GONE);
                        ll_card_def.setVisibility(View.VISIBLE);
                        tv_title_d.setVisibility(View.GONE);
                        //ll_event_d.setVisibility(View.VISIBLE);
                        btn_next_d.setText(R.string.btn_confirm);
                        String ssNum = et_cardnum_d.getText().toString().trim().substring(et_cardnum_d.getText().toString().trim().length() - 4);
                        tv_num_def.setText("xxxx xxxx xxxx " + ssNum);
                        tv_holder_def.setText(et_holder_d.getText().toString().trim());
                        tv_exp_def.setText(et_expire_d.getText().toString().trim());
                        if(cb_save_d.isChecked()){
                            if(!sNum.contains("xxxx")){
                                cData.card_num = sNum;
                            }
                            cData.holder_name = et_holder_d.getText().toString().trim();
                            cData.expire = sExp;
                            cData.cvv = sCVV;
                            //cModel.street_no = et_addr_no_d.getText().toString().trim();
                            //cModel.street_name = et_addr_name_d.getText().toString().trim();
                            //cModel.city = et_city_d.getText().toString().trim();
                            //cModel.province = et_province_d.getText().toString().trim();
                            //cModel.post_code = et_postcode_d.getText().toString().trim();
                            mHelper.putCardInfo(cData);
                        }
                        bConfirm = true;
                    }
                } else{
                    callTipApi(cData, amount);
                    pdialog.dismiss();
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
                    tv_cardnum_d.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                } else{
                    tv_cardnum_d.setText(R.string.payment_card_number);
                    tv_cardnum_d.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGray));
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
                    tv_holder_d.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                } else{
                    tv_holder_d.setText(R.string.payment_cardholder);
                    tv_holder_d.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGray));
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
                    tv_expire_d.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                } else{
                    tv_expire_d.setText(R.string.payment_expiration);
                    tv_expire_d.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGray));
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
                    tv_cvv_d.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                } else{
                    tv_cvv_d.setText(R.string.payment_cvv);
                    tv_cvv_d.setTextColor(ContextCompat.getColor(getContext(), R.color.colorGray));
                }*/
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
}
