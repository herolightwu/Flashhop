package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.flashhop.app.R;

public class SettingAccountFrag extends DialogFragment {
    View root_view;
    ImageView iv_back;
    LinearLayout ll_email, ll_password, ll_language, ll_interest, ll_dob, ll_gender;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_account, container, false);

        initLayout();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_account_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        ll_email = root_view.findViewById(R.id.setting_account_frag_ll_email);
        ll_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeEmail();
            }
        });

        ll_password = root_view.findViewById(R.id.setting_account_frag_ll_password);
        ll_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

        ll_language = root_view.findViewById(R.id.setting_account_frag_ll_language);
        ll_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLanguages();
            }
        });
        ll_interest = root_view.findViewById(R.id.setting_account_frag_ll_interest);
        ll_interest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeInterests();
            }
        });

        ll_dob = root_view.findViewById(R.id.setting_account_frag_ll_dob);
        ll_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDob();
            }
        });

        ll_gender = root_view.findViewById(R.id.setting_account_frag_ll_gender);
        ll_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeGender();
            }
        });
    }

    private void changeDob(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingChangeDob dobDlg = new SettingChangeDob();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, dobDlg, "SETTING_CHANGE_DOB_DLG").addToBackStack("SETTING_CHANGE_DOB_DLG").commit();//R.id.home_frame
    }

    private void changeGender(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingChangeGender genderDlg = new SettingChangeGender();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, genderDlg, "SETTING_CHANGE_GENDER_DLG").addToBackStack("SETTING_CHANGE_GENDER_DLG").commit();//R.id.home_frame
    }

    private void changeEmail(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingChangeEmail emailDlg = new SettingChangeEmail();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, emailDlg, "SETTING_CHANGE_EMAIL_DLG").addToBackStack("SETTING_CHANGE_EMAIL_DLG").commit();//R.id.home_frame
    }

    private void changePassword(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingChangePass passDlg = new SettingChangePass();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, passDlg, "SETTING_CHANGE_PASS_DLG").addToBackStack("SETTING_CHANGE_PASS_DLG").commit();//R.id.home_frame
    }

    private void changeLanguages(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingChangeLang langDlg = new SettingChangeLang();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, langDlg, "SETTING_CHANGE_LANG_DLG").addToBackStack("SETTING_CHANGE_LANG_DLG").commit();//R.id.home_frame
    }

    private void changeInterests(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        SettingChangeInterests interDlg = new SettingChangeInterests();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, interDlg, "SETTING_CHANGE_INTER_DLG").addToBackStack("SETTING_CHANGE_INTER_DLG").commit();//R.id.home_frame
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
