package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.flashhop.app.R;
import com.flashhop.app.start.WelcomeActivity;

public class GenderFragment extends DialogFragment {

    private View root_view;
    private TextView tv_step, tv_male, tv_female;
    Button btn_next;
    ImageView iv_skip, iv_back, iv_male, iv_female;
    LinearLayout ll_male, ll_female;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_gender, container, false);

        int step = ((WelcomeActivity)getActivity()).nStep;
        tv_step = root_view.findViewById(R.id.gender_tv_step);
        String step_str = String.format(getString(R.string.welcome_step), step);
        tv_step.setText(step_str);

        btn_next = root_view.findViewById(R.id.gender_btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).process_next();
            }
        });

        iv_skip = root_view.findViewById(R.id.gender_iv_skip);
        iv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).process_next();
            }
        });

        iv_back = root_view.findViewById(R.id.gender_iv_back);
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

    private void refreshLayout(){
        if(((WelcomeActivity)getActivity()).bMale){
            iv_male.setImageResource(R.drawable.male_y);
            //tv_male.setTextColor(getResources().getColor(R.color.colorWhite));
            iv_female.setImageResource(R.drawable.female_w);
            //tv_female.setTextColor(getResources().getColor(R.color.colorWhite));

        } else{
            iv_female.setImageResource(R.drawable.female_y);
            //tv_female.setTextColor(getResources().getColor(R.color.colorWhite));
            iv_male.setImageResource(R.drawable.male_w);
            //tv_male.setTextColor(getResources().getColor(R.color.colorWhite));
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
        tv_male = rview.findViewById(R.id.gender_tv_male);
        iv_male = rview.findViewById(R.id.gender_iv_male);
        ll_male = rview.findViewById(R.id.gender_ll_male);
        ll_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bMale = true;
                refreshLayout();
            }
        });

        tv_female = rview.findViewById(R.id.gender_tv_female);
        iv_female = rview.findViewById(R.id.gender_iv_female);
        ll_female = rview.findViewById(R.id.gender_ll_female);
        ll_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bMale = false;
                refreshLayout();
            }
        });
    }
}
