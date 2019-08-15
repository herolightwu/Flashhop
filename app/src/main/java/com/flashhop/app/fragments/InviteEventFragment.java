package com.flashhop.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.adapters.InviteEventAdapter;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.flashhop.app.utils.Const.APP_TAG;

public class InviteEventFragment extends DialogFragment {

    Context context;
    private View root_view;
    TextView tv_title;
    Button btn_send;
    RelativeLayout rl_outside;
    RecyclerView eventRecycler;
    InviteEventAdapter inviteEventAdapter;
    public List<EventModel> event_list = new ArrayList<>();
    public UserModel other = new UserModel();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_invite_event, container, false);

        context = getContext();
        initLayout();
        loadData();
        return root_view;
    }

    private void refreshLayout(){
        String sTitle = String.format(getActivity().getString(R.string.invite_event_title), other.first_name);
        tv_title.setText(sTitle);
    }

    private void initLayout(){
        rl_outside = root_view.findViewById(R.id.invite_event_frag_rl_outside);
        rl_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        tv_title = root_view.findViewById(R.id.invite_event_frag_tv_title);
        btn_send = root_view.findViewById(R.id.invite_event_frag_btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inviteEventAdapter.sel_row != -1){
                    EventModel one = event_list.get(inviteEventAdapter.sel_row);
                    inviteFriend(one.id);
                } else{
                    WindowUtils.animateView(context, view);
                }
                //dismiss();
            }
        });

        eventRecycler = root_view.findViewById(R.id.invite_event_frag_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        eventRecycler.setLayoutManager(layoutManager);

        inviteEventAdapter = new InviteEventAdapter(event_list, getContext());
        eventRecycler.setAdapter(inviteEventAdapter);
        inviteEventAdapter.setOnItemClickListener(new InviteEventAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                return 0;
            }
        });

    }

    private void inviteFriend(String eId){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Inviting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.INVITE_FRIENDS_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("user_id_list", other.uid)
                .addBodyParameter("event_id", eId)
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
                                dismiss();
                            } else{
                                dismiss();
                            }
                        }catch (JSONException ex){
                            ex.printStackTrace();
                            dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        dismiss();
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    private void loadData(){
        long tslong = System.currentTimeMillis()/1000;
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Loading events...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.MY_HOSTING_EVENT_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("current_time", tslong + "")
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
                                JSONArray events_array = response.getJSONArray("data");
                                for(int i = 0; i < events_array.length(); i++){
                                    EventModel other = new EventModel();
                                    JSONObject event_obj = events_array.getJSONObject(i);
                                    other.id = event_obj.getString("id");
                                    other.title = event_obj.getString("event_title");
                                    other.date = event_obj.getString("event_date");
                                    String etime = event_obj.getString("event_end_time");
                                    String stime = event_obj.getString("event_time");
                                    if(etime.equals("null")){
                                        other.time = stime;
                                    } else{
                                        other.time = stime + " - " + etime;
                                    }
                                    other.address = event_obj.getString("address");
                                    other.people = event_obj.getString("min_members") + "," + event_obj.getString("max_members");
                                    other.age = event_obj.getString("min_age") + "," + event_obj.getString("max_age");
                                    other.category = event_obj.getString("event_category");
                                    other.photo = event_obj.getString("cover_photo");
                                    if(other.photo.equals("null")){
                                        other.photo = "";
                                    }
                                    other.desc = event_obj.getString("event_description");
                                    String sGen = event_obj.getString("gender");
                                    if(sGen.contains("boy")){
                                        other.gender = 1;
                                    } else if(sGen.contains("girl")){
                                        other.gender = 2;
                                    } else{
                                        other.gender = 0;
                                    }
                                    other.followable = event_obj.getInt("is_private");
                                    other.invitaion = event_obj.getInt("allow_invite");
                                    other.created_at = event_obj.getString("created_at");
                                    other.updated_at = event_obj.getString("updated_at");
                                    other.price = event_obj.getString("price");
                                    if(other.price.equals("null")){
                                        other.price = "0";
                                    }
                                    other.currency = event_obj.getString("currency_code");
                                    other.loc = event_obj.getDouble("lat") + "," + event_obj.getDouble("lng");
                                    other.nLike = event_obj.getInt("isLikedByYou");
                                    other.is_pay_later = event_obj.getInt("is_pay_later");
                                    if(!event_obj.isNull("creator")){
                                        JSONObject creator_obj = event_obj.getJSONObject("creator");
                                        UserModel creat_user = new UserModel();
                                        creat_user.email = creator_obj.getString("email");
                                        creat_user.first_name = creator_obj.getString("first_name");
                                        creat_user.last_name = creator_obj.getString("last_name");
                                        creat_user.push_id = creator_obj.getString("push_user_id");
                                        creat_user.uid = creator_obj.getString("id");
                                        creat_user.created_at = creator_obj.getString("created_at");
                                        creat_user.updated_at = creator_obj.getString("updated_at");
                                        int nVerify = creator_obj.getInt("email_verified");
                                        if(nVerify == 0){
                                            creat_user.bEmailVerify = false;
                                        } else{
                                            creat_user.bEmailVerify = true;
                                        }
                                        String sVal = creator_obj.getString("dob");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.dob = sVal;
                                        sVal = creator_obj.getString("lang");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.lang = sVal;
                                        sVal = creator_obj.getString("interests");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.interests = sVal;
                                        sVal = creator_obj.getString("gender");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.gender = sVal;
                                        sVal = creator_obj.getString("social_id");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.social_id = sVal;
                                        sVal = creator_obj.getString("social_name");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.social_name = sVal;
                                        if(creator_obj.has("avatar")){
                                            sVal = creator_obj.getString("avatar");
                                            if(sVal.equals("null")) sVal = "";
                                            creat_user.photo_url = sVal;
                                        } else{
                                            creat_user.photo_url = "";
                                        }

                                        sVal = creator_obj.getString("personality_type");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.person_type = sVal;
                                        sVal = creator_obj.getString("fun_facts");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.facts = sVal;
                                        creat_user.push_chats = creator_obj.getInt("push_chats");
                                        creat_user.push_friends_activities = creator_obj.getInt("push_friends_activities");
                                        creat_user.push_my_activities = creator_obj.getInt("push_my_activities");
                                        creat_user.hide_my_location = creator_obj.getInt("hide_my_location");
                                        creat_user.hide_my_age = creator_obj.getInt("hide_my_age");
                                        creat_user.is_active_by_customer = creator_obj.getInt("is_active_by_customer");
                                        creat_user.lat = creator_obj.getDouble("lat");
                                        creat_user.lng = creator_obj.getDouble("lng");
                                        other.creator = creat_user;
                                    } else{
                                        other.creator = null;
                                    }

                                    JSONArray mem_array = event_obj.getJSONArray("members");
                                    other.members = new ArrayList<>();
                                    for(int k = 0; k < mem_array.length(); k++){
                                        UserModel one = new UserModel();
                                        JSONObject mem_obj = mem_array.getJSONObject(k);
                                        JSONObject user_obj = mem_obj.getJSONObject("user");
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
                                        one.lat = user_obj.getDouble("lat");
                                        one.lng = user_obj.getDouble("lng");
                                        other.members.add(one);
                                    }
                                    other.state = 1;
                                    event_list.add(other);
                                }
                                inviteEventAdapter.setDataList(event_list);
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
