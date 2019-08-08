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
import android.widget.EditText;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingChangePass extends DialogFragment {
    View root_view;
    ImageView iv_back;
    EditText et_curpass, et_newpass, et_confirm;
    Button btn_save;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_change_pass, container, false);

        initLayout();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_change_pass_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btn_save = root_view.findViewById(R.id.setting_change_pass_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidField()){
                    resetPassword();
                }
            }
        });

        et_curpass = root_view.findViewById(R.id.setting_change_pass_et_cur_pass);
        et_curpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    et_curpass.setBackgroundResource(R.drawable.border_edit_small_round);
                } else{
                    et_curpass.setBackgroundResource(R.drawable.border_edit_small_round_r);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_newpass = root_view.findViewById(R.id.setting_change_pass_et_new_pass);
        et_newpass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    et_newpass.setBackgroundResource(R.drawable.border_edit_small_round);
                } else{
                    et_newpass.setBackgroundResource(R.drawable.border_edit_small_round_r);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_confirm = root_view.findViewById(R.id.setting_change_pass_et_confirm);
        et_confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    et_confirm.setBackgroundResource(R.drawable.border_edit_small_round);
                } else{
                    et_confirm.setBackgroundResource(R.drawable.border_edit_small_round_r);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void resetPassword(){
        String curPass = et_curpass.getText().toString();
        String newPass = et_newpass.getText().toString();
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Changing...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.CHANGE_PASSWORD_URL)
                .addBodyParameter("email", MyApp.curUser.email)
                .addBodyParameter("new_pwd", newPass)
                .addBodyParameter("old_pwd", curPass)
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
                                WindowUtils.animateView(getContext(), btn_save);
                            }
                        } catch(JSONException ex){
                            WindowUtils.animateView(getContext(), btn_save);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), btn_save);
                    }
                });

    }

    private boolean checkValidField(){
        boolean bNew = false, bCurPass = false, bConfirm = false;
        String sNewPass = et_newpass.getText().toString().trim();
        String sCurPass = et_curpass.getText().toString().trim();
        String sConfirm = et_confirm.getText().toString().trim();

        if (sCurPass.length() > 0){
            if(sCurPass.length() > 5){
                bCurPass = true;
            } else{
                bCurPass = false;
                et_curpass.setBackgroundResource(R.drawable.border_edit_small_round_r);
                WindowUtils.animateView(getContext(), et_curpass);
            }
        } else {
            bCurPass = false;
            et_curpass.setBackgroundResource(R.drawable.border_edit_small_round_r);
            WindowUtils.animateView(getContext(), et_curpass);
        }

        if (sNewPass.length() > 0){
            if(sNewPass.length() > 5){
                bNew = true;
            } else{
                bNew = false;
                et_newpass.setBackgroundResource(R.drawable.border_edit_small_round_r);
                WindowUtils.animateView(getContext(), et_newpass);
            }
        } else {
            bNew = false;
            et_newpass.setBackgroundResource(R.drawable.border_edit_small_round_r);
            WindowUtils.animateView(getContext(), et_newpass);
        }

        if(!sConfirm.equals(sNewPass)){
            et_confirm.setBackgroundResource(R.drawable.border_edit_small_round_r);
            WindowUtils.animateView(getContext(), et_confirm);
        } else{
            bConfirm = true;
        }

        return bNew && bCurPass && bConfirm;
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
}
