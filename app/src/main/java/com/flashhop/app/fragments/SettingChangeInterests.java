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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingChangeInterests extends DialogFragment {
    private View root_view;
    Button btn_save;
    ImageView iv_back;
    LinearLayout ll_party, ll_games, ll_eating, ll_sports, ll_outdoors, ll_spirits, ll_dating, ll_arts, ll_culture;
    ImageView iv_party, iv_games, iv_eating, iv_sports, iv_outdoors, iv_spirits, iv_dating, iv_arts, iv_culture;
    TextView tv_party, tv_games, tv_eating, tv_sports, tv_outdoors, tv_spirits, tv_dating, tv_arts, tv_culture;
    CheckBox cb_all;

    public boolean bInterest[] = new boolean[9];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_change_interests, container, false);

        btn_save = root_view.findViewById(R.id.setting_change_interest_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValid()){
                    doSaveInterests();
                } else{
                    WindowUtils.animateView(getContext(), btn_save);
                }

            }
        });

        iv_back = root_view.findViewById(R.id.setting_change_interest_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        cb_all = root_view.findViewById(R.id.setting_change_interest_cb_all);
        cb_all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    for(int i = 0; i < 9; i++){
                        bInterest[i] = true;
                    }
                    refreshLayout();
                } else{
                    for(int j = 0; j < 9; j++){
                        if(j != 1 || j != 3){
                            bInterest[j] = false;
                        }
                    }
                    refreshLayout();
                }
            }
        });

        initLayout(root_view);
        loadData();
        return root_view;
    }

    private void doSaveInterests(){
        String sInterest = "";
        for(int j = 0; j < 9; j++){
            if(bInterest[j]){
                if(sInterest.length() == 0){
                    sInterest = Const.interest_list[j];
                } else{
                    sInterest = sInterest + "," + Const.interest_list[j];
                }
            }
        }

        final String sTemp = sInterest;

        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Changing...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.CHANGE_INTERESTS_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("interests", sInterest)
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
                                MyApp.curUser.interests = sTemp;
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
        String[] sInter = MyApp.curUser.interests.split(",");
        for(int i = 0; i < sInter.length; i++){
            for(int j = 0; j < 9; j++){
                if(Const.interest_list[j].toLowerCase().equals(sInter[i].toLowerCase())){
                    bInterest[j] = true;
                    break;
                }
            }
        }
    }

    private void refreshLayout(){
        if(bInterest[0]){
            iv_party.setImageResource(R.drawable.party_d);
            //tv_party.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_party.setImageResource(R.drawable.party_w);
            //tv_party.setTextColor(getResources().getColor(R.color.colorWhite));
        }

        if(bInterest[1]){
            iv_eating.setImageResource(R.drawable.eating_d);
            //tv_eating.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_eating.setImageResource(R.drawable.eating_w);
            //tv_eating.setTextColor(getResources().getColor(R.color.colorWhite));
        }

        if(bInterest[5]){
            iv_games.setImageResource(R.drawable.games_d);
            //tv_games.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_games.setImageResource(R.drawable.games_w);
            //tv_games.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(bInterest[3]){
            iv_sports.setImageResource(R.drawable.sports_d);
            //tv_sports.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_sports.setImageResource(R.drawable.sports_w);
            //tv_sports.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(bInterest[4]){
            iv_outdoors.setImageResource(R.drawable.outdoor_d);
            // tv_outdoors.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_outdoors.setImageResource(R.drawable.outdoor_w);
            //tv_outdoors.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(bInterest[7]){
            iv_spirits.setImageResource(R.drawable.spirits_d);
            //tv_spirits.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_spirits.setImageResource(R.drawable.spirits_w);
            //tv_spirits.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(bInterest[2]){
            iv_dating.setImageResource(R.drawable.dating_d);
            //tv_dating.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_dating.setImageResource(R.drawable.dating_w);
            //tv_dating.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(bInterest[8]){
            iv_arts.setImageResource(R.drawable.arts_d);
            //tv_arts.setTextColor(getResources().getColor(R.color.colorYellow));
        } else{
            iv_arts.setImageResource(R.drawable.arts_w);
            //tv_arts.setTextColor(getResources().getColor(R.color.colorWhite));
        }
        if(bInterest[6]){
            iv_culture.setImageResource(R.drawable.culture_d);
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
            if(bInterest[i]){
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
        iv_party = rview.findViewById(R.id.setting_change_interest_iv_party);
        tv_party = rview.findViewById(R.id.setting_change_interest_tv_party);
        ll_party = rview.findViewById(R.id.setting_change_interest_ll_party);
        ll_party.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[0] = !bInterest[0];
                refreshLayout();
            }
        });

        iv_eating = rview.findViewById(R.id.setting_change_interest_iv_eating);
        tv_eating = rview.findViewById(R.id.setting_change_interest_tv_eating);
        ll_eating = rview.findViewById(R.id.setting_change_interest_ll_eating);
        ll_eating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[1] = !bInterest[1];
                refreshLayout();
            }
        });

        iv_games = rview.findViewById(R.id.setting_change_interest_iv_game);
        tv_games = rview.findViewById(R.id.setting_change_interest_tv_game);
        ll_games = rview.findViewById(R.id.setting_change_interest_ll_game);
        ll_games.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[5] = !bInterest[5];
                refreshLayout();
            }
        });

        iv_sports = rview.findViewById(R.id.setting_change_interest_iv_sport);
        tv_sports = rview.findViewById(R.id.setting_change_interest_tv_sport);
        ll_sports = rview.findViewById(R.id.setting_change_interest_ll_sport);
        ll_sports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[3] = !bInterest[3];
                refreshLayout();
            }
        });

        iv_outdoors = rview.findViewById(R.id.setting_change_interest_iv_outdoor);
        tv_outdoors = rview.findViewById(R.id.setting_change_interest_tv_outdoor);
        ll_outdoors = rview.findViewById(R.id.setting_change_interest_ll_outdoor);
        ll_outdoors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[4] = !bInterest[4];
                refreshLayout();
            }
        });

        iv_spirits = rview.findViewById(R.id.setting_change_interest_iv_spirit);
        tv_spirits = rview.findViewById(R.id.setting_change_interest_tv_spirit);
        ll_spirits = rview.findViewById(R.id.setting_change_interest_ll_spirit);
        ll_spirits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[7] = !bInterest[7];
                refreshLayout();
            }
        });

        iv_dating = rview.findViewById(R.id.setting_change_interest_iv_date);
        tv_dating = rview.findViewById(R.id.setting_change_interest_tv_date);
        ll_dating = rview.findViewById(R.id.setting_change_interest_ll_date);
        ll_dating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[2] = !bInterest[2];
                refreshLayout();
            }
        });

        iv_arts = rview.findViewById(R.id.setting_change_interest_iv_art);
        tv_arts = rview.findViewById(R.id.setting_change_interest_tv_art);
        ll_arts = rview.findViewById(R.id.setting_change_interest_ll_art);
        ll_arts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[8] = !bInterest[8];
                refreshLayout();
            }
        });

        iv_culture = rview.findViewById(R.id.setting_change_interest_iv_culture);
        tv_culture = rview.findViewById(R.id.setting_change_interest_tv_culture);
        ll_culture = rview.findViewById(R.id.setting_change_interest_ll_culture);
        ll_culture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bInterest[6] = !bInterest[6];
                refreshLayout();
            }
        });
    }
}
