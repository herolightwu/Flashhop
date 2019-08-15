package com.flashhop.app.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.login.LoginManager;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.services.GPSService;
import com.flashhop.app.start.ChooseActivity;
import com.flashhop.app.utils.Const;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingsFragment extends DialogFragment {

    View root_view;
    ImageView iv_back;
    LinearLayout ll_accounts, ll_noti, ll_payment, ll_privacy, ll_logout, ll_inactive;
    TextView tv_inactive;
    SaveSharedPrefrence saveSharedPrefrence = new SaveSharedPrefrence();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_settings, container, false);

        initLayout();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        ll_accounts = root_view.findViewById(R.id.setting_frag_ll_accounts);
        ll_accounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAccountDlg();
            }
        });

        ll_noti = root_view.findViewById(R.id.setting_frag_ll_notifications);
        ll_noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNotificationDlg();
            }
        });

        ll_payment = root_view.findViewById(R.id.setting_frag_ll_payment);
        ll_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaymentDlg();
            }
        });
        ll_privacy = root_view.findViewById(R.id.setting_frag_ll_privacy);
        ll_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrivacyDlg();
            }
        });

        ll_logout = root_view.findViewById(R.id.setting_frag_ll_logout);
        ll_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDlg();
            }
        });

        ll_inactive = root_view.findViewById(R.id.setting_frag_ll_inactive);
        ll_inactive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInactiveDlg();
            }
        });
        tv_inactive = root_view.findViewById(R.id.setting_frag_tv_inactive);

    }

    private void refreshLayout(){
        if(MyApp.curUser.is_active_by_customer == 1){
            tv_inactive.setText(R.string.setting_inactivate);
        } else{
            tv_inactive.setText(R.string.setting_activate);
        }
    }

    Dialog inactiveDlg;
    private void showInactiveDlg(){
        TextView tv_desc;
        Button btn_cancel, btn_sure;
        inactiveDlg = new Dialog(getContext(), R.style.FullHeightDialog);
        inactiveDlg.setContentView(R.layout.custom_confirm_dlg);
        View dview = inactiveDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = inactiveDlg.findViewById(R.id.custom_confirm_tv_desc);
        btn_cancel = inactiveDlg.findViewById(R.id.custom_confirm_btn_cancel);
        btn_sure = inactiveDlg.findViewById(R.id.custom_confirm_btn_sure);

        if(MyApp.curUser.is_active_by_customer == 1){
            tv_desc.setText(R.string.setting_inactive_desc);
        } else{
            tv_desc.setText(R.string.setting_active_desc);
        }
        inactiveDlg.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                inactiveDlg.dismiss();
            }
        });

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inactiveUser();
            }
        });
    }

    private void inactiveUser(){
        String nAct = "0";
        if(MyApp.curUser.is_active_by_customer == 0) nAct = "1";

        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Saving...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.INACTIVE_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("is_active", nAct)
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
                                inactiveDlg.dismiss();
                                if(MyApp.curUser.is_active_by_customer == 0){
                                    MyApp.curUser.is_active_by_customer = 1;
                                } else{
                                    MyApp.curUser.is_active_by_customer = 0;
                                }
                                refreshLayout();
                            } else{
                                inactiveDlg.dismiss();
                            }
                        }catch (JSONException ex){
                            ex.printStackTrace();
                            inactiveDlg.dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        inactiveDlg.dismiss();
                    }
                });
    }

    Dialog logoutDlg;

    private void showLogoutDlg(){
        TextView tv_desc;
        Button btn_cancel, btn_sure;
        logoutDlg = new Dialog(getContext(), R.style.FullHeightDialog);
        logoutDlg.setContentView(R.layout.custom_confirm_dlg);
        View dview = logoutDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = logoutDlg.findViewById(R.id.custom_confirm_tv_desc);
        btn_cancel = logoutDlg.findViewById(R.id.custom_confirm_btn_cancel);
        btn_sure = logoutDlg.findViewById(R.id.custom_confirm_btn_sure);

        tv_desc.setText(R.string.setting_logout_desc);
        logoutDlg.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                logoutDlg.dismiss();
            }
        });

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    private void logout(){
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Logout...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.LOGOUT_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
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
                                getActivity().stopService(new Intent(getContext(), GPSService.class));
                                saveSharedPrefrence.saveKeyLogin(getContext(), false);
                                logoutDlg.dismiss();
                                if(MyApp.curUser.social_name.equals("facebook")){
                                    LoginManager.getInstance().logOut();
                                }
                                Intent intent = new Intent(getActivity(), ChooseActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            } else{
                                logoutDlg.dismiss();
                            }
                        }catch (JSONException ex){
                            ex.printStackTrace();
                            logoutDlg.dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        logoutDlg.dismiss();
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

    private void showPaymentDlg(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingPaymentFrag paymentDlg = new SettingPaymentFrag();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, paymentDlg, "SETTING_PAYMENT_DLG").addToBackStack("SETTING_PAYMENT_DLG").commit();//R.id.home_frame
    }

    private void showAccountDlg(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingAccountFrag accountDlg = new SettingAccountFrag();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, accountDlg, "SETTING_ACCOUNT_DLG").addToBackStack("SETTING_ACCOUNT_DLG").commit();//R.id.home_frame
    }

    private void showNotificationDlg(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingNotifyFrag notifyDlg = new SettingNotifyFrag();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, notifyDlg, "SETTING_NOTIFY_DLG").addToBackStack("SETTING_NOTIFY_DLG").commit();//R.id.home_frame
    }

    private void showPrivacyDlg(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingPrivacyFrag privacyDlg = new SettingPrivacyFrag();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, privacyDlg, "SETTING_PRIVACY_DLG").addToBackStack("SETTING_PRIVACY_DLG").commit();//R.id.home_frame
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
