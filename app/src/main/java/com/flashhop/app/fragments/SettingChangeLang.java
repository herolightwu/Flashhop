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
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingChangeLang extends DialogFragment {
    View root_view;
    ImageView iv_back;
    Button btn_save;
    Button btn_en, btn_fr, btn_cn, btn_es, btn_ja, btn_ko, btn_ar, btn_ru, btn_de, btn_pt;
    public boolean bLang[] = new boolean[10];
    String lang_list[] = new String[]{"en", "fr", "cn", "es", "ja", "ko", "ar", "ru", "de", "po"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_change_lang, container, false);

        bLang[0] = true;
        initLayout();
        loadData();
        return root_view;
    }

    private void loadData(){
        String[] sLang = MyApp.curUser.lang.split(",");
        for(int i = 0; i < sLang.length; i++){
            for(int j = 0; j < 10; j++){
                if(lang_list[j].equals(sLang[i])){
                    bLang[j] = true;
                    break;
                }
            }
        }
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_change_lang_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btn_save = root_view.findViewById(R.id.setting_change_lang_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLanguages();
            }
        });

        btn_en = root_view.findViewById(R.id.setting_change_lang_btn_en);
        btn_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btn_fr = root_view.findViewById(R.id.setting_change_lang_btn_fr);
        btn_fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(1)){
                    bLang[1] = !bLang[1];
                    refreshLayout();
                }
            }
        });
        btn_cn = root_view.findViewById(R.id.setting_change_lang_btn_cn);
        btn_cn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(2)){
                    bLang[2] = !bLang[2];
                    refreshLayout();
                }
            }
        });
        btn_es = root_view.findViewById(R.id.setting_change_lang_btn_es);
        btn_es.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(3)){
                    bLang[3] = !bLang[3];
                    refreshLayout();
                }

            }
        });
        btn_ja = root_view.findViewById(R.id.setting_change_lang_btn_ja);
        btn_ja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(4)){
                    bLang[4] = !bLang[4];
                    refreshLayout();
                }
            }
        });
        btn_ko = root_view.findViewById(R.id.setting_change_lang_btn_ko);
        btn_ko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(5)){
                    bLang[5] = !bLang[5];
                    refreshLayout();
                }
            }
        });
        btn_ar = root_view.findViewById(R.id.setting_change_lang_btn_ar);
        btn_ar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(6)){
                    bLang[6] = !bLang[6];
                    refreshLayout();
                }
            }
        });
        btn_ru = root_view.findViewById(R.id.setting_change_lang_btn_ru);
        btn_ru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(7)){
                    bLang[7] = !bLang[7];
                    refreshLayout();
                }
            }
        });
        btn_de = root_view.findViewById(R.id.setting_change_lang_btn_de);
        btn_de.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(8)){
                    bLang[8] = !bLang[8];
                    refreshLayout();
                }
            }
        });
        btn_pt = root_view.findViewById(R.id.setting_change_lang_btn_pt);
        btn_pt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(9)){
                   bLang[9] = !bLang[9];
                    refreshLayout();
                }
            }
        });

    }

    private void saveLanguages(){
        String sLang = "";
        for(int i = 0; i < 10; i++){
            if(bLang[i]){
                if(sLang.length() == 0){
                    sLang = lang_list[i];
                } else{
                    sLang = sLang + "," + lang_list[i];
                }
            }
        }

        final String sTemp = sLang;
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Changing...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.CHANGE_LANGS_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("lang", sLang)
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
                                MyApp.curUser.lang = sTemp;
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

    private boolean checkValid(int sel){
        int count = 0;
        for(int i = 0; i < 10 ; i++){
            if(bLang[i]){
                count++;
            }
        }
        if(count > 1) return true;
        if(bLang[sel])
            return false;
        return true;
    }

    private void refreshLayout(){
        if(bLang[0]){
            btn_en.setBackgroundResource(R.drawable.border_btn_dark);
            btn_en.setTextColor(ContextCompat.getColor(getContext(),R.color.colorWhite));
        } else{
            btn_en.setBackgroundResource(R.drawable.border_b_back_w);
            btn_en.setTextColor(ContextCompat.getColor(getContext(),R.color.colorDGray));
        }

        if(bLang[1]){
            btn_fr.setBackgroundResource(R.drawable.border_btn_dark);
            btn_fr.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_fr.setBackgroundResource(R.drawable.border_b_back_w);
            btn_fr.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        }

        if(bLang[2]){
            btn_cn.setBackgroundResource(R.drawable.border_btn_dark);
            btn_cn.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_cn.setBackgroundResource(R.drawable.border_b_back_w);
            btn_cn.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        }
        if(bLang[3]){
            btn_es.setBackgroundResource(R.drawable.border_btn_dark);
            btn_es.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_es.setBackgroundResource(R.drawable.border_b_back_w);
            btn_es.setTextColor(ContextCompat.getColor(getContext(),R.color.colorDGray));
        }
        if(bLang[4]){
            btn_ja.setBackgroundResource(R.drawable.border_btn_dark);
            btn_ja.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_ja.setBackgroundResource(R.drawable.border_b_back_w);
            btn_ja.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        }
        if(bLang[5]){
            btn_ko.setBackgroundResource(R.drawable.border_btn_dark);
            btn_ko.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_ko.setBackgroundResource(R.drawable.border_b_back_w);
            btn_ko.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        }
        if(bLang[6]){
            btn_ar.setBackgroundResource(R.drawable.border_btn_dark);
            btn_ar.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_ar.setBackgroundResource(R.drawable.border_b_back_w);
            btn_ar.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        }
        if(bLang[7]){
            btn_ru.setBackgroundResource(R.drawable.border_btn_dark);
            btn_ru.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_ru.setBackgroundResource(R.drawable.border_b_back_w);
            btn_ru.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        }
        if(bLang[8]){
            btn_de.setBackgroundResource(R.drawable.border_btn_dark);
            btn_de.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_de.setBackgroundResource(R.drawable.border_b_back_w);
            btn_de.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        }
        if(bLang[9]){
            btn_pt.setBackgroundResource(R.drawable.border_btn_dark);
            btn_pt.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        } else{
            btn_pt.setBackgroundResource(R.drawable.border_b_back_w);
            btn_pt.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        }
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
