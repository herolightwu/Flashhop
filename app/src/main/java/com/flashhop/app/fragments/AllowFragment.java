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
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.flashhop.app.R;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.start.WelcomeActivity;

public class AllowFragment extends DialogFragment {

    private View root_view;
    private TextView tv_desc, tv_skip;
    Button btn_enable;
    RelativeLayout rl_cancel;
    SaveSharedPrefrence saveSharedPrefrence;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        saveSharedPrefrence = new SaveSharedPrefrence();
        root_view = inflater.inflate(R.layout.fragment_allow, container, false);

        btn_enable = root_view.findViewById(R.id.allow_frag_btn_enable);
        btn_enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                /*int nstep = ((WelcomeActivity)getActivity()).nStep;
                if(nstep == 5){
                    saveSharedPrefrence.putString(getContext(),SaveSharedPrefrence.KEY_LOCATION, "true");
                    ((WelcomeActivity)getActivity()).bLoc_Enable = true;
                }
                if(nstep == 6){*/
                    saveSharedPrefrence.putString(getContext(), SaveSharedPrefrence.KEY_NOTIFICATION, "true");
                    ((WelcomeActivity)getActivity()).bNoti_Enable = true;
                //}
                ((WelcomeActivity)getActivity()).process_next();
            }
        });

        tv_skip = root_view.findViewById(R.id.allow_frag_tv_skip);
        tv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                /*int nstep = ((WelcomeActivity)getActivity()).nStep;
                if(nstep == 5){
                    saveSharedPrefrence.putString(getContext(), SaveSharedPrefrence.KEY_LOCATION, "false");
                    ((WelcomeActivity)getActivity()).bLoc_Enable = false;
                }
                if(nstep == 6){*/
                    saveSharedPrefrence.putString(getContext(), SaveSharedPrefrence.KEY_NOTIFICATION, "false");
                    ((WelcomeActivity)getActivity()).bNoti_Enable = false;
                //}
                ((WelcomeActivity)getActivity()).process_next();
            }
        });

        rl_cancel = root_view.findViewById(R.id.allow_frag_rl_cancel);
        rl_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((WelcomeActivity)getActivity()).nStep--;
                //dismiss();
            }
        });

        tv_desc = root_view.findViewById(R.id.allow_frag_tv_desc);
        /*int step = ((WelcomeActivity)getActivity()).nStep;
        if(step == 5){
            tv_desc.setText(R.string.welcome_allow_location);
            //tv_skip.setVisibility(View.VISIBLE);
            btn_enable.setText(R.string.btn_enable);
        } else if(step == 6){*/
            tv_desc.setText(R.string.welcome_allow_notification);
            //tv_skip.setVisibility(View.GONE);
            btn_enable.setText(R.string.btn_allow);
        //}
        return root_view;
    }

    private void refreshLayout(){

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
