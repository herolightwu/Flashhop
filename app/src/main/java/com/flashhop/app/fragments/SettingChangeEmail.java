package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
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
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.start.SignupActivity;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingChangeEmail extends DialogFragment {
    View root_view;
    ImageView iv_back;
    EditText et_email, et_code;
    TextView tv_code, tv_email, tv_success;
    LinearLayout ll_code;
    Button btn_save, btn_send;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_change_email, container, false);

        initLayout();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_change_email_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btn_save = root_view.findViewById(R.id.setting_change_email_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_code.getText().toString().trim().length() > 0){
                    changeEmail();
                } else{
                    showError(et_code, tv_code, null);
                }
            }
        });

        btn_send = root_view.findViewById(R.id.setting_change_email_btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_success.setVisibility(View.GONE);
                if(et_email.getText().toString().length() > 0){
                    sendVerificationCode();
                } else{
                    showError(et_email, tv_email, null);
                }

            }
        });

        tv_email = root_view.findViewById(R.id.setting_change_email_tv_email);
        tv_code = root_view.findViewById(R.id.setting_change_email_tv_code);
        ll_code = root_view.findViewById(R.id.setting_change_email_ll_code);
        et_email = root_view.findViewById(R.id.setting_change_email_et_email);
        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    hideError(et_email, tv_email);
                } else{
                    showError(et_email, tv_email, null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_code = root_view.findViewById(R.id.setting_change_email_et_code);
        et_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    hideError(et_code, tv_code);
                } else{
                    showError(et_code, tv_code, null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tv_success = root_view.findViewById(R.id.setting_change_email_tv_success);

        ll_code.setVisibility(View.GONE);
        tv_success.setVisibility(View.GONE);

    }

    private void sendVerificationCode(){
        String sEmail = et_email.getText().toString().trim();
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Sending code...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.EMAIL_RESET_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("email", sEmail)
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
                                tv_success.setVisibility(View.GONE);
                                btn_send.setVisibility(View.GONE);
                                ll_code.setVisibility(View.VISIBLE);
                                et_email.setEnabled(false);
                            } else{
                                showError(et_email, tv_email, btn_send);
                            }
                        } catch(JSONException ex){
                            showError(et_email, tv_email, btn_send);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        showError(et_email, tv_email, btn_send);
                    }
                });
    }

    private void changeEmail(){
        final String sEmail = et_email.getText().toString().trim();
        String sCode = et_code.getText().toString().trim();
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Saving...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.CHANGE_EMAIL_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("email", sEmail)
                .addBodyParameter("code", sCode)
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
                                btn_send.setVisibility(View.VISIBLE);
                                ll_code.setVisibility(View.GONE);
                                tv_success.setVisibility(View.VISIBLE);
                                et_email.setEnabled(true);
                                MyApp.curUser.email = sEmail;
                            } else{
                                showError(et_code, tv_code, btn_save);
                            }
                        } catch(JSONException ex){
                            showError(et_code, tv_code, btn_save);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        showError(et_code, tv_code, btn_save);
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void showError(EditText edt, TextView tv, Button btn){
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
        edt.setBackgroundResource(R.drawable.border_edit_small_round_r);
        if(btn != null)
            WindowUtils.animateView(getContext(), btn);
    }

    private void hideError(EditText edt, TextView tv){
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        edt.setBackgroundResource(R.drawable.border_edit_small_round);
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
