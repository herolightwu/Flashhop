package com.flashhop.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.HoppersAdapter;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.HopperModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.flashhop.app.utils.Const.APP_TAG;

public class GroupInfoFragment extends DialogFragment {

    Context context;
    HomeActivity parent;

    View rView;
    ImageView iv_back;
    TextView tv_leave, tv_report, tv_member, tv_expire;
    GridView grid_member;
    HoppersAdapter hoppersAdapter;
    SwitchCompat sw_mute;
    boolean bMute;
    List<HopperModel> allhoppers = new ArrayList<>();

    public EventModel group_event;

    public GroupInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rView = inflater.inflate(R.layout.fragment_group_info, container, false);

        parent = (HomeActivity)getActivity();
        context = getContext();
        initLayout();
        return rView;
    }

    private void initLayout() {
        iv_back = rView.findViewById(R.id.group_info_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        tv_leave = rView.findViewById(R.id.group_info_frag_tv_leave);
        tv_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveEvent();
            }
        });
        tv_report = rView.findViewById(R.id.group_info_frag_tv_report);
        tv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReportDlg();
            }
        });
        tv_member = rView.findViewById(R.id.group_info_frag_tv_member);
        tv_expire = rView.findViewById(R.id.group_info_frag_tv_remain);
        tv_member.setText((group_event.members.size() + 1) + " Hoppers");
        long diff = TxtUtils.getDifferenceTime1(group_event.created_at);
        long l_tt = diff / (60 * 24);
        if (l_tt >= 6) {
            l_tt = 7 * 24 - diff / 60;
            if (l_tt <= 1) {
                l_tt = 7 * 24 * 60 - diff;
                String str = String.format(getString(R.string.chat_group_info_remain), l_tt + " minutes");
                tv_expire.setText(str);
            } else {
                String str = String.format(getString(R.string.chat_group_info_remain), l_tt + " hours");
                tv_expire.setText(str);
            }
        } else {
            String str = String.format(getString(R.string.chat_group_info_remain), (7 - l_tt) + " days");
            tv_expire.setText(str);
        }

        grid_member = rView.findViewById(R.id.group_info_frag_grid_member);
        sw_mute = rView.findViewById(R.id.group_info_frag_switch_mute);
        sw_mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setGroupNotification(b);
            }
        });

        initData();
        boolean bb = group_event.creator.uid.equals(MyApp.curUser.uid);
        hoppersAdapter = new HoppersAdapter(getContext(), allhoppers);
        hoppersAdapter.bMyEvent = bb;
        hoppersAdapter.creator_id = group_event.creator.uid;
        if(group_event.price.equals("0")) hoppersAdapter.bFreeEvent = true;
        grid_member.setAdapter(hoppersAdapter);

        hoppersAdapter.setOnItemClickListener(new HoppersAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                if(!hoppersAdapter.bViewAll && pos == 9){
                    hoppersAdapter.bViewAll = true;
                    hoppersAdapter.notifyDataSetChanged();
                    return 0;
                }
                HopperModel hopper = allhoppers.get(pos);
                if(group_event.creator.uid.equals(MyApp.curUser.uid) &&
                        !group_event.price.equals("0") &&
                        !hopper.uid.equals(MyApp.curUser.uid) &&
                        !(hopper.nOffline == 0 && hopper.nPaid == 1)){
                    //call pay API
                    updatePaidStatus(hopper, pos);
                }
                return pos;
            }
        });
    }

    private void updatePaidStatus(HopperModel hopper, final int pos){
        int paid = 0;
        if(hopper.nPaid == 0) paid = 1;
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Updating...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.UPDATE_PAID_STATE_URL)
                .addBodyParameter("user_id", hopper.uid)
                .addBodyParameter("event_id", group_event.id)
                .addBodyParameter("paid", paid + "")
                .addBodyParameter("is_offline_paid", "1")
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            int bSuccess = response.getInt("success");
                            if (bSuccess == 1) {
                                allhoppers.get(pos).nOffline = 1;
                                loadHoppers();
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    private void initData(){
        allhoppers.clear();
        HopperModel creator = new HopperModel();
        creator.uid = group_event.creator.uid;
        creator.name = group_event.creator.first_name;
        creator.avatar = group_event.creator.photo_url;
        allhoppers.add(creator);
        for(int i = 0 ; i < group_event.members.size(); i++){
            HopperModel one = new HopperModel();
            one.uid = group_event.members.get(i).uid;
            one.name = group_event.members.get(i).first_name;
            one.avatar = group_event.members.get(i).photo_url;
            allhoppers.add(one);
        }
    }

    private void loadHoppers(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Loading Hoppers...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.READ_EVENT_URL)
                .addBodyParameter("event_id", group_event.id)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hud.dismiss();
                            int bSuccess = response.getInt("success");
                            if (bSuccess == 1) {
                                allhoppers.clear();
                                JSONObject event_obj = response.getJSONObject("data");
                                int nMute = event_obj.getInt("chat_mute");
                                if(nMute != 0) bMute = true;
                                if(!event_obj.isNull("creator")){
                                    JSONObject creator_obj = event_obj.getJSONObject("creator");
                                    HopperModel creat_user = new HopperModel();
                                    creat_user.name = creator_obj.getString("first_name");
                                    creat_user.uid = creator_obj.getString("id");
                                    String sVal = "";
                                    if(creator_obj.has("avatar")){
                                        sVal = creator_obj.getString("avatar");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.avatar = sVal;
                                    } else{
                                        creat_user.avatar = "";
                                    }

                                    hoppersAdapter.creator_id = creat_user.uid;
                                    //creat_user.isManualPay = creator_obj.getBoolean("isManualPay");
                                    allhoppers.add(creat_user);
                                }
                                JSONArray mem_array = event_obj.getJSONArray("members");
                                for(int k = 0; k < mem_array.length(); k++){
                                    HopperModel one = new HopperModel();
                                    JSONObject mem_obj = mem_array.getJSONObject(k);
                                    JSONObject user_obj = mem_obj.getJSONObject("user");
                                    one.name = user_obj.getString("first_name");
                                    one.uid = user_obj.getString("id");
                                    String sVal = user_obj.getString("avatar");
                                    if(sVal.equals("null")) sVal = "";
                                    one.avatar = sVal;
                                    one.nPaid = mem_obj.getInt("paid");
                                    one.nOffline = mem_obj.getInt("is_offline_paid");
                                    allhoppers.add(one);
                                }
                                refreshLayout();
                                hoppersAdapter.setDataList(allhoppers);

                            }

                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    private void refreshLayout(){
        if(group_event.creator != null && group_event.creator.uid.equals(MyApp.curUser.uid)){
            tv_leave.setVisibility(View.GONE);
        } else{
            tv_leave.setVisibility(View.VISIBLE);
        }

        if(bMute){
            sw_mute.setChecked(true);
        } else{
            sw_mute.setChecked(false);
        }

        int nLines = (allhoppers.size() + 1) / 2;
        ViewGroup.LayoutParams layoutParams = grid_member.getLayoutParams();
        int nHeight = WindowUtils.convertDpToPixels(nLines * 36, parent);
        layoutParams.height = nHeight; //this is in pixels
        grid_member.setLayoutParams(layoutParams);
    }

    @Override
    public void onResume(){
        super.onResume();
        loadHoppers();
        refreshLayout();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setGroupNotification(final boolean bSet){
        String val = "0";
        if(bSet) val = "1";
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Leaving...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.EVENT_CHAT_MUTE)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("event_id", group_event.id)
                .addBodyParameter("is_muted", val)
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
                                bMute = bSet;
                                refreshLayout();
                            } else{
                                WindowUtils.animateView(getContext(), sw_mute);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), sw_mute);
                    }
                });
    }

    private void showReportDlg(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        ReportDlgFrag reportFrag = new ReportDlgFrag();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, reportFrag, "REPORT_FRAG").addToBackStack("REPORT_FRAG").commit();
    }

    private void leaveEvent(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Leaving...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.LEAVE_EVENT_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("event_id", group_event.id)
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
                                JSONObject event_obj = response.getJSONObject("data");
                                JSONArray mem_array = event_obj.getJSONArray("members");
                                group_event.members.clear();
                                for(int k = 0; k < mem_array.length(); k++){
                                    UserModel one = new UserModel();
                                    JSONObject user_obj = mem_array.getJSONObject(k);
                                    one.email = user_obj.getString("email");
                                    one.first_name = user_obj.getString("first_name");
                                    one.last_name = user_obj.getString("last_name");
                                    one.push_id = user_obj.getString("push_user_id");
                                    one.uid = user_obj.getString("id");
                                    one.created_at = user_obj.getString("created_at");
                                    one.updated_at = user_obj.getString("updated_at");
                                    int nVerify = user_obj.getInt("email_verified");
                                    if(nVerify == 0){
                                        one.bEmailVerify = false;
                                    } else{
                                        one.bEmailVerify = true;
                                    }
                                    String sVal = user_obj.getString("dob");
                                    if(sVal.equals("null")) sVal = "";
                                    one.dob = sVal;
                                    sVal = user_obj.getString("lang");
                                    if(sVal.equals("null")) sVal = "";
                                    one.lang = sVal;
                                    sVal = user_obj.getString("interests");
                                    if(sVal.equals("null")) sVal = "";
                                    one.interests = sVal;
                                    sVal = user_obj.getString("gender");
                                    if(sVal.equals("null")) sVal = "";
                                    one.gender = sVal;
                                    sVal = user_obj.getString("social_id");
                                    if(sVal.equals("null")) sVal = "";
                                    one.social_id = sVal;
                                    sVal = user_obj.getString("social_name");
                                    if(sVal.equals("null")) sVal = "";
                                    one.social_name = sVal;
                                    sVal = user_obj.getString("avatar");
                                    if(sVal.equals("null")) sVal = "";
                                    one.photo_url = sVal;

                                    sVal = user_obj.getString("personality_type");
                                    if(sVal.equals("null")) sVal = "";
                                    one.person_type = sVal;
                                    sVal = user_obj.getString("fun_facts");
                                    if(sVal.equals("null")) sVal = "";
                                    one.facts = sVal;

                                    one.push_chats = user_obj.getInt("push_chats");
                                    one.push_friends_activities = user_obj.getInt("push_friends_activities");
                                    one.push_my_activities = user_obj.getInt("push_my_activities");
                                    one.event_count = user_obj.getInt("event_count");
                                    one.hide_my_location = user_obj.getInt("hide_my_location");
                                    one.hide_my_age = user_obj.getInt("hide_my_age");
                                    one.is_active_by_customer = user_obj.getInt("is_active_by_customer");
                                    group_event.members.add(one);
                                }

                                dismiss();
                                MyApp.home_type = Const.HOME_CHAT;
                                parent.refreshHome();
                            } else{
                                WindowUtils.animateView(getContext(), tv_leave);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), tv_leave);
                    }
                });
    }
}
