package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingChangeGender extends DialogFragment {
    View root_view;
    ImageView iv_back;
    private TextView tv_male, tv_female, tv_limit;
    Button btn_save;
    ImageView iv_male, iv_female;
    LinearLayout ll_male, ll_female;
    boolean bMale = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_change_gender, container, false);

        if(MyApp.curUser.gender.equals("male")) bMale = true;
        initLayout();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_change_gender_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btn_save = root_view.findViewById(R.id.setting_change_gender_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGender();
            }
        });
        tv_male = root_view.findViewById(R.id.setting_change_gender_tv_male);
        iv_male = root_view.findViewById(R.id.setting_change_gender_iv_male);
        ll_male = root_view.findViewById(R.id.setting_change_gender_ll_male);
        ll_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyApp.curUser.bGenderEnable==1){
                    bMale = true;
                    refreshLayout();
                }

            }
        });

        tv_female = root_view.findViewById(R.id.setting_change_gender_tv_female);
        iv_female = root_view.findViewById(R.id.setting_change_gender_iv_female);
        ll_female = root_view.findViewById(R.id.setting_change_gender_ll_female);
        ll_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyApp.curUser.bGenderEnable == 1){
                    bMale = false;
                    refreshLayout();
                }
            }
        });
        tv_limit = root_view.findViewById(R.id.setting_change_gender_tv_limit);

    }

    private void refreshLayout(){
        if(bMale){
            iv_male.setImageResource(R.drawable.male_y);
            iv_female.setImageResource(R.drawable.female_w);
        } else{
            iv_female.setImageResource(R.drawable.female_y);
            iv_male.setImageResource(R.drawable.male_w);
        }

        if(MyApp.curUser.bGenderEnable == 1){
            iv_male.setColorFilter(R.color.colorDGray ,android.graphics.PorterDuff.Mode.MULTIPLY);
            iv_female.setColorFilter(R.color.colorDGray ,android.graphics.PorterDuff.Mode.MULTIPLY);
        } else{
            iv_male.setColorFilter(R.color.colorGray ,android.graphics.PorterDuff.Mode.MULTIPLY);
            iv_female.setColorFilter(R.color.colorGray ,android.graphics.PorterDuff.Mode.MULTIPLY);
        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d = sdf.parse(MyApp.curUser.dob_update);
            Calendar thatDay = Calendar.getInstance();
            thatDay.setTime(d);
            thatDay.add(Calendar.MONTH,6);
            Date ad = thatDay.getTime();
            SimpleDateFormat edf = new SimpleDateFormat("MMMM dd, yyyy");
            String limitdate = edf.format(ad);
            tv_limit.setText(getString(R.string.setting_date_limit) + " " + limitdate);
        } catch (ParseException ex) {
            Log.v("Exception", ex.getLocalizedMessage());
        }

        if(MyApp.curUser.bGenderEnable == 1){
            tv_limit.setVisibility(View.GONE);
            btn_save.setVisibility(View.VISIBLE);
        } else{
            tv_limit.setVisibility(View.VISIBLE);
            btn_save.setVisibility(View.GONE);
        }
    }

    private void saveGender(){
        String sGen = "male";
        if(!bMale) sGen = "female";
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Changing...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.UPLOAD_PROFILE_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("gender", sGen)
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
                                if(bMale){
                                    MyApp.curUser.gender = "male";
                                } else{
                                    MyApp.curUser.gender = "female";
                                }
                                MyApp.curUser.bGenderEnable = 0;
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
