package com.flashhop.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.GoingListAdapter;
import com.flashhop.app.adapters.HostingListAdapter;
import com.flashhop.app.adapters.SavedListAdapter;
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.flashhop.app.utils.Const.APP_TAG;

public class HostingFragment extends DialogFragment {

    Context context;
    HomeActivity parent;

    RecyclerView hostingRecycler, goingRecycler, savedRecycler;
    HostingListAdapter hostingAdapter;
    GoingListAdapter goingAdapter;
    SavedListAdapter savedAdapter;
    List<EventModel> host_list, going_list, saved_list;
    LinearLayout ll_hosting, ll_going, ll_saved;
    MySQLiteHelper mHelper;

    View rootV;
    public HostingFragment() {
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
        rootV = inflater.inflate(R.layout.fragment_hosting, container, false);
        context = getContext();
        parent = (HomeActivity) getActivity();
        mHelper = MySQLiteHelper.getInstance(context);
        host_list = new ArrayList<>();
        going_list = new ArrayList<>();
        saved_list = new ArrayList<>();
        initLayout();
        loadData();
        return rootV;
    }

    private void loadData(){
        long tslong = System.currentTimeMillis()/1000;
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Loading...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.UPCOMING_EVENT_URL)
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
                                List<EventModel> data_list = new ArrayList<>();
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
                                    data_list.add(other);
                                }
                                int nEnd = data_list.size();
                                for(int i = 0; i < nEnd; i++){
                                    EventModel one = data_list.get(i);
                                    if(one.creator != null && one.creator.uid.equals(MyApp.curUser.uid)){
                                        host_list.add(one);
                                    }
                                    if(one.nLike == 1){
                                        saved_list.add(one);
                                    }
                                    if(checkJoining(one)){
                                        going_list.add(one);
                                    }
                                }

                                if(mHelper != null){
                                    List<EventModel> db_list = mHelper.getEvents(0);
                                    for(int k = 0; k < db_list.size(); k++){
                                        EventModel one = db_list.get(k);
                                        if(one.state == 0 && one.id.length() == 0){
                                            host_list.add(one);
                                        }
                                    }
                                }

                                refreshLayout();
                            }

                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        refreshLayout();
                    }
                });
    }

    private void initLayout(){
        hostingRecycler = rootV.findViewById(R.id.hosting_frag_host_recycler);
        LinearLayoutManager hostManager = new LinearLayoutManager(getContext());
        hostingRecycler.setLayoutManager(hostManager);
        hostingAdapter = new HostingListAdapter(host_list, getContext());
        hostingRecycler.setAdapter(hostingAdapter);
        hostingAdapter.setOnEditClickListener(new HostingListAdapter.OnEditClickListener() {
            @Override
            public int onEditClick(int pos) {
                EventModel one = host_list.get(pos);
                parent.event_photo = "";
                FragmentManager fragmentManager = parent.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                HostEventFragment hosteventFrag = new HostEventFragment();
                hosteventFrag.draftEvent = one;
                hosteventFrag.bChooseAddr = true;
                fragmentTransaction.add(R.id.home_frame, hosteventFrag, Const.FRAG_EVENT_TAG).addToBackStack(Const.FRAG_EVENT_TAG).commit();
                return 0;
            }
        });
        hostingAdapter.setOnCancelClickListener(new HostingListAdapter.OnCancelClickListener() {
            @Override
            public int onCancelClick(int pos) {
                EventModel one = host_list.get(pos);
                long diff = TxtUtils.getDifferenceTime(one.date, one.time);
                if(diff <= 30){
                    showGoingAlertDlg();
                } else{
                    showCancelConfirmDlg(one);
                }
                return 0;
            }
        });

        goingRecycler = rootV.findViewById(R.id.hosting_frag_going_recycler);
        LinearLayoutManager goingManager = new LinearLayoutManager(getContext());
        goingRecycler.setLayoutManager(goingManager);
        goingAdapter = new GoingListAdapter(going_list, getContext());
        goingRecycler.setAdapter(goingAdapter);
        goingAdapter.setOnItemClickListener(new GoingListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                EventModel one = going_list.get(pos);
                showEventProperty(one);
                return 0;
            }
        });

        savedRecycler = rootV.findViewById(R.id.hosting_frag_saved_recycler);
        LinearLayoutManager savedManager = new LinearLayoutManager(getContext());
        savedRecycler.setLayoutManager(savedManager);
        savedAdapter = new SavedListAdapter(saved_list, getContext());
        savedRecycler.setAdapter(savedAdapter);
        savedAdapter.setOnItemClickListener(new SavedListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                EventModel one = saved_list.get(pos);
                showEventProperty(one);
                return 0;
            }
        });

        ll_hosting = rootV.findViewById(R.id.hosting_frag_ll_hosting);
        ll_going = rootV.findViewById(R.id.hosting_frag_ll_going);
        ll_saved = rootV.findViewById(R.id.hosting_frag_ll_saved);

        if(host_list.size() == 0){
            ll_hosting.setVisibility(View.GONE);
        } else{
            ll_hosting.setVisibility(View.VISIBLE);
        }

        if(going_list.size() == 0){
            ll_going.setVisibility(View.GONE);
        } else{
            ll_going.setVisibility(View.VISIBLE);
        }

        if(saved_list.size() == 0){
            ll_saved.setVisibility(View.GONE);
        } else{
            ll_saved.setVisibility(View.VISIBLE);
        }
    }

    private void refreshLayout(){
        if(host_list.size() == 0){
            ll_hosting.setVisibility(View.GONE);
        } else{
            ll_hosting.setVisibility(View.VISIBLE);
        }

        if(going_list.size() == 0){
            ll_going.setVisibility(View.GONE);
        } else{
            ll_going.setVisibility(View.VISIBLE);
        }

        if(saved_list.size() == 0){
            ll_saved.setVisibility(View.GONE);
        } else{
            ll_saved.setVisibility(View.VISIBLE);
        }
        hostingAdapter.setDataList(host_list);
        savedAdapter.setDataList(saved_list);
        goingAdapter.setDataList(going_list);
        if(host_list.size() == 0 && going_list.size() == 0 && saved_list.size() == 0){
            showMineFrag();
        }
    }

    public void showMineFrag(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        MineFragment mineFrag = new MineFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.replace(R.id.home_frame, mineFrag, Const.FRAG_UPCOMING_TAG).commit();
    }

    private boolean checkJoining(EventModel one){
        for(int j = 0; j < one.members.size(); j++){
            UserModel other = one.members.get(j);
            if(other.uid.equals(MyApp.curUser.uid)){
                return true;
            }
        }
        return false;
    }

    private void cancelEvent(final EventModel delE){
        if(delE.id.length() == 0 && delE.db_id != -1){
            mHelper.deleteEvent(delE);
        } else{
            final KProgressHUD hud = KProgressHUD.create(context)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setLabel("Canceling...")
                    .setCancellable(true)
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show();
            AndroidNetworking.post(Const.HOST_URL + Const.CANCEL_EVENT)
                    .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                    .addBodyParameter("event_id", delE.id)
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
                                    host_list.remove(delE);
                                    parent.event_list.remove(delE);
                                    if(host_list.size() == 0){
                                        ll_hosting.setVisibility(View.GONE);
                                    } else{
                                        ll_hosting.setVisibility(View.VISIBLE);
                                        hostingAdapter.setDataList(host_list);
                                    }
                                }
                            }catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            hud.dismiss();
                        }
                    });
        }
    }

    private void showCancelConfirmDlg(final EventModel cancelE){
        TextView tv_desc;
        Button btn_no, btn_yes;
        final Dialog mDlg = new Dialog(context, R.style.FullHeightDialog);
        mDlg.setContentView(R.layout.custom_confirm_dlg);
        View dview = mDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = mDlg.findViewById(R.id.custom_confirm_tv_desc);
        btn_no = mDlg.findViewById(R.id.custom_confirm_btn_cancel);
        btn_yes = mDlg.findViewById(R.id.custom_confirm_btn_sure);

        tv_desc.setText(R.string.event_cancel_confirm_desc);
        btn_no.setText(R.string.btn_no);
        btn_yes.setText(R.string.btn_yes);
        mDlg.show();

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                mDlg.dismiss();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDlg.dismiss();
                cancelEvent(cancelE);
            }
        });
    }

    private void showGoingAlertDlg(){
        TextView tv_desc;
        Button btn_no, btn_yes;
        final Dialog alertDlg = new Dialog(context, R.style.FullHeightDialog);
        alertDlg.setContentView(R.layout.custom_confirm_dlg);
        View dview = alertDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = alertDlg.findViewById(R.id.custom_confirm_tv_desc);
        btn_no = alertDlg.findViewById(R.id.custom_confirm_btn_cancel);
        btn_yes = alertDlg.findViewById(R.id.custom_confirm_btn_sure);

        tv_desc.setText(R.string.event_cancel_not_desc);
        btn_no.setVisibility(View.GONE);
        btn_yes.setText(R.string.btn_ok);
        alertDlg.show();

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                alertDlg.dismiss();
            }
        });
    }

    public void showEventProperty(EventModel event){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        EventPropertyFragment propFrag = new EventPropertyFragment();
        propFrag.event = event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_out_up, R.anim.ani_slide_out_down);
        transaction.add(R.id.home_frame, propFrag, "EVENT_PROP_FRAG").addToBackStack("EVENT_PROP_FRAG").commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
