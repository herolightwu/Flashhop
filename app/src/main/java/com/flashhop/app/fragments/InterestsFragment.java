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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.flashhop.app.R;
import com.flashhop.app.start.WelcomeActivity;
import com.flashhop.app.utils.WindowUtils;

public class InterestsFragment extends DialogFragment {
    private View root_view;
    private TextView tv_step, tv_desc;
    Button btn_done;
    ImageView iv_skip, iv_back;
    LinearLayout ll_party, ll_games, ll_eating, ll_sports, ll_outdoors, ll_spirits, ll_dating, ll_arts, ll_culture;
    ImageView iv_party, iv_games, iv_eating, iv_sports, iv_outdoors, iv_spirits, iv_dating, iv_arts, iv_culture;
    TextView tv_party, tv_games, tv_eating, tv_sports, tv_outdoors, tv_spirits, tv_dating, tv_arts, tv_culture;
    CheckBox cb_all;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_interests, container, false);

        int step = ((WelcomeActivity)getActivity()).nStep;
        tv_step = root_view.findViewById(R.id.inter_frag_tv_step);
        String step_str = String.format(getString(R.string.welcome_step), step);
        tv_step.setText(step_str);

        tv_desc = root_view.findViewById(R.id.inter_frag_tv_desc);

        btn_done = root_view.findViewById(R.id.inter_frag_btn_done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid()){
                    ((WelcomeActivity)getActivity()).process_next();
                } else{
                    WindowUtils.animateView(getContext(), btn_done);
                    tv_desc.setText(R.string.welcome_inter_err);
                }

            }
        });

        iv_skip = root_view.findViewById(R.id.inter_frag_iv_skip);
        iv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid()){
                    ((WelcomeActivity)getActivity()).process_next();
                } else{
                    WindowUtils.animateView(getContext(), btn_done);
                    tv_desc.setText(R.string.welcome_inter_err);
                }
            }
        });

        iv_back = root_view.findViewById(R.id.inter_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).nStep--;
                dismiss();
            }
        });

        cb_all = root_view.findViewById(R.id.inter_frag_cb_all);
        cb_all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    for(int i = 0; i < 9; i++){
                        ((WelcomeActivity)getActivity()).bInterest[i] = true;
                    }
                    refreshLayout();
                } else{
                    for(int j = 0; j < 9; j++){
                        if(j != 1 || j != 3){
                            ((WelcomeActivity)getActivity()).bInterest[j] = false;
                        }
                    }
                    refreshLayout();
                }
            }
        });

        initLayout(root_view);
        return root_view;
    }

    private void refreshLayout(){
        tv_desc.setText(R.string.welcome_inter_choose);
        if(((WelcomeActivity)getActivity()).bInterest[0]){
            iv_party.setImageResource(R.drawable.party_y);
            //tv_party.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_party.setImageResource(R.drawable.party_w);
            //tv_party.setTextColor(getResources().getColor(R.color.colorWhite));
        }

        if(((WelcomeActivity)getActivity()).bInterest[1]){
            iv_eating.setImageResource(R.drawable.eating_y);
            //tv_eating.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_eating.setImageResource(R.drawable.eating_w);
            //tv_eating.setTextColor(getResources().getColor(R.color.colorWhite));
        }

        if(((WelcomeActivity)getActivity()).bInterest[5]){
            iv_games.setImageResource(R.drawable.games_y);
            //tv_games.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_games.setImageResource(R.drawable.games_w);
            //tv_games.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bInterest[3]){
            iv_sports.setImageResource(R.drawable.sports_y);
            //tv_sports.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_sports.setImageResource(R.drawable.sports_w);
            //tv_sports.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bInterest[4]){
            iv_outdoors.setImageResource(R.drawable.outdoor_y);
           // tv_outdoors.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_outdoors.setImageResource(R.drawable.outdoor_w);
            //tv_outdoors.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bInterest[7]){
            iv_spirits.setImageResource(R.drawable.spirits_y);
            //tv_spirits.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_spirits.setImageResource(R.drawable.spirits_w);
            //tv_spirits.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bInterest[2]){
            iv_dating.setImageResource(R.drawable.dating_y);
            //tv_dating.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_dating.setImageResource(R.drawable.dating_w);
            //tv_dating.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bInterest[8]){
            iv_arts.setImageResource(R.drawable.arts_y);
            //tv_arts.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_arts.setImageResource(R.drawable.arts_w);
            //tv_arts.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(((WelcomeActivity)getActivity()).bInterest[6]){
            iv_culture.setImageResource(R.drawable.culture_y);
            //tv_culture.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_culture.setImageResource(R.drawable.culture_w);
            //tv_culture.setTextColor(getResources().getColor(R.color.colorWhite));
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

    private boolean checkValid(){
        int count = 0;
        for(int i = 0; i < 9 ; i++){
            if(((WelcomeActivity)getActivity()).bInterest[i]){
                count++;
            }
        }
        if(count > 0){
            return true;
        } else {
            return false;
        }
    }

    private void initLayout(View rview){
        iv_party = rview.findViewById(R.id.inter_frag_iv_party);
        tv_party = rview.findViewById(R.id.inter_frag_tv_party);
        ll_party = rview.findViewById(R.id.inter_frag_ll_party);
        ll_party.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[0] = !((WelcomeActivity)getActivity()).bInterest[0];
                refreshLayout();
            }
        });

        iv_eating = rview.findViewById(R.id.inter_frag_iv_eating);
        tv_eating = rview.findViewById(R.id.inter_frag_tv_eating);
        ll_eating = rview.findViewById(R.id.inter_frag_ll_eating);
        ll_eating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[1] = !((WelcomeActivity)getActivity()).bInterest[1];
                refreshLayout();
            }
        });

        iv_games = rview.findViewById(R.id.inter_frag_iv_game);
        tv_games = rview.findViewById(R.id.inter_frag_tv_game);
        ll_games = rview.findViewById(R.id.inter_frag_ll_game);
        ll_games.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[5] = !((WelcomeActivity)getActivity()).bInterest[5];
                refreshLayout();
            }
        });

        iv_sports = rview.findViewById(R.id.inter_frag_iv_sport);
        tv_sports = rview.findViewById(R.id.inter_frag_tv_sport);
        ll_sports = rview.findViewById(R.id.inter_frag_ll_sport);
        ll_sports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[3] = !((WelcomeActivity)getActivity()).bInterest[3];
                refreshLayout();
            }
        });

        iv_outdoors = rview.findViewById(R.id.inter_frag_iv_outdoor);
        tv_outdoors = rview.findViewById(R.id.inter_frag_tv_outdoor);
        ll_outdoors = rview.findViewById(R.id.inter_frag_ll_outdoor);
        ll_outdoors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[4] = !((WelcomeActivity)getActivity()).bInterest[4];
                refreshLayout();
            }
        });

        iv_spirits = rview.findViewById(R.id.inter_frag_iv_spirit);
        tv_spirits = rview.findViewById(R.id.inter_frag_tv_spirit);
        ll_spirits = rview.findViewById(R.id.inter_frag_ll_spirit);
        ll_spirits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[7] = !((WelcomeActivity)getActivity()).bInterest[7];
                refreshLayout();
            }
        });

        iv_dating = rview.findViewById(R.id.inter_frag_iv_date);
        tv_dating = rview.findViewById(R.id.inter_frag_tv_date);
        ll_dating = rview.findViewById(R.id.inter_frag_ll_date);
        ll_dating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[2] = !((WelcomeActivity)getActivity()).bInterest[2];
                refreshLayout();
            }
        });

        iv_arts = rview.findViewById(R.id.inter_frag_iv_art);
        tv_arts = rview.findViewById(R.id.inter_frag_tv_art);
        ll_arts = rview.findViewById(R.id.inter_frag_ll_art);
        ll_arts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[8] = !((WelcomeActivity)getActivity()).bInterest[8];
                refreshLayout();
            }
        });

        iv_culture = rview.findViewById(R.id.inter_frag_iv_culture);
        tv_culture = rview.findViewById(R.id.inter_frag_tv_culture);
        ll_culture = rview.findViewById(R.id.inter_frag_ll_culture);
        ll_culture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).bInterest[6] = !((WelcomeActivity)getActivity()).bInterest[6];
                refreshLayout();
            }
        });
    }
}
