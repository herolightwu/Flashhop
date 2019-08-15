package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.flashhop.app.R;
import com.flashhop.app.start.WelcomeActivity;


public class LanguageFragment extends DialogFragment {

    private View root_view;
    private TextView tv_step;
    Button btn_next;
    Button btn_en, btn_fr, btn_cn, btn_es, btn_ja, btn_ko, btn_ar, btn_ru, btn_de, btn_pt;
    ImageView iv_skip, iv_back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_language, container, false);

        int step = ((WelcomeActivity)getActivity()).nStep;
        tv_step = root_view.findViewById(R.id.language_tv_step);
        String step_str = String.format(getString(R.string.welcome_step), step);
        tv_step.setText(step_str);

        btn_next = root_view.findViewById(R.id.lang_btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).process_next();
            }
        });

        iv_skip = root_view.findViewById(R.id.lang_iv_skip);
        iv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).process_next();
            }
        });

        iv_back = root_view.findViewById(R.id.lang_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).nStep--;
                dismiss();
            }
        });

        initButtons(root_view);
        return root_view;
    }

    private boolean checkValid(int sel){
        int count = 0;
        for(int i = 0; i < 10 ; i++){
            if(((WelcomeActivity)getActivity()).bLang[i]){
                count++;
            }
        }
        if(count > 1) return true;
        if(((WelcomeActivity)getActivity()).bLang[sel])
            return false;
        return true;
    }

    private void refreshLayout(){
        if(((WelcomeActivity)getActivity()).bLang[0]){
            btn_en.setBackgroundResource(R.drawable.border_btn_white);
            btn_en.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_en.setBackgroundResource(R.drawable.border_btn_dark);
            btn_en.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }

        if(((WelcomeActivity)getActivity()).bLang[1]){
            btn_fr.setBackgroundResource(R.drawable.border_btn_white);
            btn_fr.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_fr.setBackgroundResource(R.drawable.border_btn_dark);
            btn_fr.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }

        if(((WelcomeActivity)getActivity()).bLang[2]){
            btn_cn.setBackgroundResource(R.drawable.border_btn_white);
            btn_cn.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_cn.setBackgroundResource(R.drawable.border_btn_dark);
            btn_cn.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bLang[3]){
            btn_es.setBackgroundResource(R.drawable.border_btn_white);
            btn_es.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_es.setBackgroundResource(R.drawable.border_btn_dark);
            btn_es.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bLang[4]){
            btn_ja.setBackgroundResource(R.drawable.border_btn_white);
            btn_ja.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_ja.setBackgroundResource(R.drawable.border_btn_dark);
            btn_ja.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bLang[5]){
            btn_ko.setBackgroundResource(R.drawable.border_btn_white);
            btn_ko.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_ko.setBackgroundResource(R.drawable.border_btn_dark);
            btn_ko.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bLang[6]){
            btn_ar.setBackgroundResource(R.drawable.border_btn_white);
            btn_ar.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_ar.setBackgroundResource(R.drawable.border_btn_dark);
            btn_ar.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bLang[7]){
            btn_ru.setBackgroundResource(R.drawable.border_btn_white);
            btn_ru.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_ru.setBackgroundResource(R.drawable.border_btn_dark);
            btn_ru.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bLang[8]){
            btn_de.setBackgroundResource(R.drawable.border_btn_white);
            btn_de.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_de.setBackgroundResource(R.drawable.border_btn_dark);
            btn_de.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bLang[9]){
            btn_pt.setBackgroundResource(R.drawable.border_btn_white);
            btn_pt.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
        } else{
            btn_pt.setBackgroundResource(R.drawable.border_btn_dark);
            btn_pt.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
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

    private void initButtons(View rview){
        btn_en = rview.findViewById(R.id.lang_btn_en);
        btn_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btn_fr = rview.findViewById(R.id.lang_btn_fr);
        btn_fr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(1)){
                    ((WelcomeActivity)getActivity()).bLang[1] = !((WelcomeActivity)getActivity()).bLang[1];
                    refreshLayout();
                }
            }
        });
        btn_cn = rview.findViewById(R.id.lang_btn_cn);
        btn_cn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(2)){
                    ((WelcomeActivity)getActivity()).bLang[2] = !((WelcomeActivity)getActivity()).bLang[2];
                    refreshLayout();
                }
            }
        });
        btn_es = rview.findViewById(R.id.lang_btn_es);
        btn_es.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(3)){
                    ((WelcomeActivity)getActivity()).bLang[3] = !((WelcomeActivity)getActivity()).bLang[3];
                    refreshLayout();
                }

            }
        });
        btn_ja = rview.findViewById(R.id.lang_btn_ja);
        btn_ja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(4)){
                    ((WelcomeActivity)getActivity()).bLang[4] = !((WelcomeActivity)getActivity()).bLang[4];
                    refreshLayout();
                }
            }
        });
        btn_ko = rview.findViewById(R.id.lang_btn_ko);
        btn_ko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(5)){
                    ((WelcomeActivity)getActivity()).bLang[5] = !((WelcomeActivity)getActivity()).bLang[5];
                    refreshLayout();
                }
            }
        });
        btn_ar = rview.findViewById(R.id.lang_btn_ar);
        btn_ar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(6)){
                    ((WelcomeActivity)getActivity()).bLang[6] = !((WelcomeActivity)getActivity()).bLang[6];
                    refreshLayout();
                }
            }
        });
        btn_ru = rview.findViewById(R.id.lang_btn_ru);
        btn_ru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(7)){
                    ((WelcomeActivity)getActivity()).bLang[7] = !((WelcomeActivity)getActivity()).bLang[7];
                    refreshLayout();
                }
            }
        });
        btn_de = rview.findViewById(R.id.lang_btn_de);
        btn_de.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(8)){
                    ((WelcomeActivity)getActivity()).bLang[8] = !((WelcomeActivity)getActivity()).bLang[8];
                    refreshLayout();
                }
            }
        });
        btn_pt = rview.findViewById(R.id.lang_btn_pt);
        btn_pt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid(9)){
                    ((WelcomeActivity)getActivity()).bLang[9] = !((WelcomeActivity)getActivity()).bLang[9];
                    refreshLayout();
                }
            }
        });
    }
}
