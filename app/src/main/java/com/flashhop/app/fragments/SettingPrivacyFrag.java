package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_AGE;
import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_CHATS;
import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_FRIENDS;
import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_LOCATION;
import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_MY;
import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_PAUSE;
import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingPrivacyFrag extends DialogFragment {
    View root_view;
    ImageView iv_back;
    SwitchCompat sw_age, sw_location;
    Button btn_save;
    MySQLiteHelper mHelper;

    int nAge = 0, nLoc = 0;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mHelper = MySQLiteHelper.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_privacy, container, false);

        initLayout();
        loadData();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_privacy_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        sw_age = root_view.findViewById(R.id.setting_privacy_frag_sw_hide_age);
        sw_location = root_view.findViewById(R.id.setting_privacy_frag_sw_location);
        btn_save = root_view.findViewById(R.id.setting_privacy_frag_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePrivacy();
            }
        });

    }

    private void updatePrivacy(){
        if(sw_age.isChecked()) nAge = 1;
        if(sw_location.isChecked()) nLoc = 1;
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Registering...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.CHANGE_PRIVACY_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("hide_my_age", nAge+"")
                .addBodyParameter("hide_my_location", nLoc+"")
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
                                MyApp.curUser.hide_my_age = nAge;
                                MyApp.curUser.hide_my_location = nLoc;

                                mHelper.updateSettingValue(KEY_SETTINGS_AGE, nAge + "");
                                mHelper.updateSettingValue(KEY_SETTINGS_LOCATION, nLoc + "");
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

    private void loadData(){
        String sVal = mHelper.getSettingValue(KEY_SETTINGS_AGE);
        if(sVal.equals("1")){
            sw_age.setChecked(true);
        } else{
            sw_age.setChecked(false);
        }
        sVal = mHelper.getSettingValue(KEY_SETTINGS_LOCATION);
        if(sVal.equals("1")){
            sw_location.setChecked(true);
        } else{
            sw_location.setChecked(false);
        }

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
