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
import android.widget.CompoundButton;
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

import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_CHATS;
import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_FRIENDS;
import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_MY;
import static com.flashhop.app.helpers.MySQLiteHelper.KEY_SETTINGS_PAUSE;
import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingNotifyFrag extends DialogFragment {
    View root_view;
    ImageView iv_back;
    SwitchCompat sw_pause, sw_my, sw_friends, sw_chats;
    Button btn_save;

    int myVal = 0, friendVal = 0, chatVal = 0;

    MySQLiteHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
         mHelper = MySQLiteHelper.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_notify, container, false);

        initLayout();
        loadData();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_notify_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        sw_pause = root_view.findViewById(R.id.setting_notify_frag_sw_pause_all);
        sw_pause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(sw_pause.isChecked()){
                    sw_my.setChecked(true);
                    sw_friends.setChecked(true);
                    sw_chats.setChecked(true);
                }
            }
        });
        sw_my = root_view.findViewById(R.id.setting_notify_frag_sw_my);
        sw_my.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!sw_my.isChecked()){
                    sw_pause.setChecked(false);
                }
            }
        });
        sw_friends = root_view.findViewById(R.id.setting_notify_frag_sw_friends);
        sw_friends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!sw_friends.isChecked()){
                    sw_pause.setChecked(false);
                }
            }
        });
        sw_chats = root_view.findViewById(R.id.setting_notify_frag_sw_chats);
        sw_chats.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!sw_chats.isChecked()){
                    sw_pause.setChecked(false);
                }
            }
        });
        btn_save = root_view.findViewById(R.id.setting_notify_frag_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateNotificationStatus();
            }
        });

    }

    private void updateNotificationStatus(){
        if(sw_pause.isChecked()){
            myVal = 1; friendVal = 1; chatVal = 1;
        } else{
            if(sw_my.isChecked()) myVal = 1;
            if(sw_friends.isChecked()) friendVal = 1;
            if(sw_chats.isChecked()) chatVal = 1;
        }

        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Registering...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.NOTIFICATION_SETTING_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("push_my_activities", myVal+"")
                .addBodyParameter("push_friends_activities", friendVal+"")
                .addBodyParameter("push_chats", chatVal+"")
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
                                MyApp.curUser.push_my_activities = myVal;
                                MyApp.curUser.push_friends_activities = friendVal;
                                MyApp.curUser.push_chats = chatVal;

                                int nPause = 0;
                                if(myVal == 1 && friendVal == 1 && chatVal == 1) nPause = 1;
                                mHelper.updateSettingValue(KEY_SETTINGS_PAUSE, nPause + "");
                                mHelper.updateSettingValue(KEY_SETTINGS_MY, myVal + "");
                                mHelper.updateSettingValue(KEY_SETTINGS_FRIENDS, friendVal + "");
                                mHelper.updateSettingValue(KEY_SETTINGS_CHATS, chatVal + "");
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
        String sVal = mHelper.getSettingValue(KEY_SETTINGS_PAUSE);
        if(sVal.equals("1")){
            sw_pause.setChecked(true);
        } else{
            sw_pause.setChecked(false);
        }
        sVal = mHelper.getSettingValue(KEY_SETTINGS_MY);
        if(sVal.equals("1")){
            sw_my.setChecked(true);
        } else{
            sw_my.setChecked(false);
        }
        sVal = mHelper.getSettingValue(KEY_SETTINGS_FRIENDS);
        if(sVal.equals("1")){
            sw_friends.setChecked(true);
        } else{
            sw_friends.setChecked(false);
        }
        sVal = mHelper.getSettingValue(KEY_SETTINGS_CHATS);
        if(sVal.equals("1")){
            sw_chats.setChecked(true);
        } else{
            sw_chats.setChecked(false);
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
