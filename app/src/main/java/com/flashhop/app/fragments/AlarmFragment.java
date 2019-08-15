package com.flashhop.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.AlarmFriendAdapter;
import com.flashhop.app.adapters.AlarmMeAdapter;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.AlarmModel;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.MessageEvent;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.utils.Const.ALARM_FRIEND_F;
import static com.flashhop.app.utils.Const.ALARM_ME_F;
import static com.flashhop.app.utils.Const.APP_TAG;

public class AlarmFragment extends Fragment {

    Context context;
    HomeActivity parent;

    TextView tv_friend, tv_me;
    RelativeLayout rl_me;
    CircleImageView civ_new;
    LinearLayout ll_friends, ll_me, ll_today, ll_yesterday, ll_last, ll_new, ll_earlier;
    RecyclerView todayRecycler, yesterdayRecycler, lastRecycler, newRecycler, earlierRecycler;
    AlarmFriendAdapter todayAdapter, yesterdayAdapter, lastAdapter;
    AlarmMeAdapter newAdapter, earlierAdapter;
    List<AlarmModel> today_list, yesterday_list, last_list, new_list, earlier_list;
    SaveSharedPrefrence sharedPrefrence = new SaveSharedPrefrence();

    public AlarmFragment() {
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
        View rView =  inflater.inflate(R.layout.fragment_alarm, container, false);

        context = getContext();
        parent = (HomeActivity)getActivity();

        today_list = new ArrayList<>();
        yesterday_list = new ArrayList<>();
        last_list = new ArrayList<>();
        new_list = new ArrayList<>();
        earlier_list = new ArrayList<>();
        initLayout(rView);
        loadData();
        loadMeData();
        return rView;
    }

    private void initLayout(View rootView){
        tv_friend = rootView.findViewById(R.id.alarm_frag_tv_friend);
        tv_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.alarm_mode = ALARM_FRIEND_F;
                reloadData();
                refreshFrag();
            }
        });
        tv_me = rootView.findViewById(R.id.alarm_frag_tv_me);

        rl_me = rootView.findViewById(R.id.alarm_frag_rl_me);
        rl_me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.alarm_mode = ALARM_ME_F;
                loadMeData();
                refreshFrag();
            }
        });
        civ_new = rootView.findViewById(R.id.alarm_frag_civ_new);
        ll_today = rootView.findViewById(R.id.alarm_frag_friend_ll_today);
        ll_yesterday = rootView.findViewById(R.id.alarm_frag_friend_ll_yesterday);
        ll_last = rootView.findViewById(R.id.alarm_frag_friend_ll_last);
        ll_new = rootView.findViewById(R.id.alarm_frag_me_ll_new);
        ll_earlier = rootView.findViewById(R.id.alarm_frag_me_ll_earlier);

        ll_friends = rootView.findViewById(R.id.alarm_frag_ll_friends);
        ll_me = rootView.findViewById(R.id.alarm_frag_ll_me);

        todayRecycler = rootView.findViewById(R.id.alarm_frag_recycler_friends_today);
        LinearLayoutManager todayManager = new LinearLayoutManager(context);
        todayRecycler.setLayoutManager(todayManager);
        todayAdapter = new AlarmFriendAdapter(today_list, context, parent);
        todayRecycler.setAdapter(todayAdapter);

        yesterdayRecycler = rootView.findViewById(R.id.alarm_frag_recycler_friends_yesterday);
        LinearLayoutManager yesterdayMgr = new LinearLayoutManager(context);
        yesterdayRecycler.setLayoutManager(yesterdayMgr);
        yesterdayAdapter = new AlarmFriendAdapter(yesterday_list, context, parent);
        yesterdayRecycler.setAdapter(yesterdayAdapter);

        lastRecycler = rootView.findViewById(R.id.alarm_frag_recycler_friends_last);
        LinearLayoutManager lastMgr = new LinearLayoutManager(context);
        lastRecycler.setLayoutManager(lastMgr);
        lastAdapter = new AlarmFriendAdapter(last_list, context, parent);
        lastRecycler.setAdapter(lastAdapter);

        newRecycler = rootView.findViewById(R.id.alarm_frag_me_recycler_new);
        LinearLayoutManager newMgr = new LinearLayoutManager(context);
        newRecycler.setLayoutManager(newMgr);
        newAdapter = new AlarmMeAdapter(new_list, context, parent);
        newRecycler.setAdapter(newAdapter);
        newAdapter.setOnEventActionClickListener(new AlarmMeAdapter.OnEventActionClickListener() {
            @Override
            public int onEventActionClick(int pos) {
                AlarmModel one = new_list.get(pos);
                if(one.action.equals("ping_30mins_event")){
                    showHoppersMap(one.event);
                } else if(one.action.equals("ping_less_member")){ //"ping_less_member"
                    showReeditEvent(one.event);
                }
                return 0;
            }
        });
        newAdapter.setOnMsgNoClickListener(new AlarmMeAdapter.OnMsgNoClickListener() {
            @Override
            public int onMsgNoClick(int pos) {
                AlarmModel one = new_list.get(pos);
                if(one.action.equals("requested_friend")){
                    responseRequestFriend(one.wId, MyApp.curUser.uid, one.uId, "0");
                } else if(one.action.equals("liked")){
                    responseSuperLike(one.wId, "hell_no");
                } else if(one.action.equals("disliked")){
                    responseSuperDiss(one.wId, "whatever");
                }
                return pos;
            }
        });
        newAdapter.setOnMsgYetClickListener(new AlarmMeAdapter.OnMsgYetClickListener() {
            @Override
            public int onMsgYetClick(int pos) {
                AlarmModel one = new_list.get(pos);
                if(one.action.equals("liked")){
                    responseSuperLike(one.wId, "not_yet");
                }
                return pos;
            }
        });
        newAdapter.setOnMsgTooClickListener(new AlarmMeAdapter.OnMsgTooClickListener() {
            @Override
            public int onMsgTooClick(int pos) {
                AlarmModel one = new_list.get(pos);
                if(one.action.equals("requested_friend")){
                    responseRequestFriend(one.wId, MyApp.curUser.uid, one.uId, "1");
                } else if(one.action.equals("liked")){
                    responseSuperLike(one.wId, "me_too");
                } else if(one.action.equals("disliked")){
                    responseSuperDiss(one.wId, "throw_back");
                }
                return pos;
            }
        });

        earlierRecycler = rootView.findViewById(R.id.alarm_frag_me_recycler_earlier);
        LinearLayoutManager earlierMgr = new LinearLayoutManager(context);
        earlierRecycler.setLayoutManager(earlierMgr);
        earlierAdapter = new AlarmMeAdapter(earlier_list, context, parent);
        earlierRecycler.setAdapter(earlierAdapter);
        earlierAdapter.setOnEventActionClickListener(new AlarmMeAdapter.OnEventActionClickListener() {
            @Override
            public int onEventActionClick(int pos) {
                AlarmModel one = earlier_list.get(pos);
                if(one.action.equals("ping_30mins_event")){
                    showHoppersMap(one.event);
                } else if(one.action.equals("ping_less_member")){ //"ping_less_member"
                    showReeditEvent(one.event);
                }
                return pos;
            }
        });
        earlierAdapter.setOnMsgNoClickListener(new AlarmMeAdapter.OnMsgNoClickListener() {
            @Override
            public int onMsgNoClick(int pos) {
                AlarmModel one = earlier_list.get(pos);
                if(one.action.equals("requested_friend")){
                    responseRequestFriend(one.wId, MyApp.curUser.uid, one.uId, "0");
                } else if(one.action.equals("liked")){
                    responseSuperLike(one.wId, "hell_no");
                } else if(one.action.equals("disliked")){
                    responseSuperDiss(one.wId, "whatever");
                }
                return pos;
            }
        });
        earlierAdapter.setOnMsgYetClickListener(new AlarmMeAdapter.OnMsgYetClickListener() {
            @Override
            public int onMsgYetClick(int pos) {
                AlarmModel one = earlier_list.get(pos);
                if(one.action.equals("liked")){
                    responseSuperLike(one.wId, "not_yet");
                }
                return pos;
            }
        });
        earlierAdapter.setOnMsgTooClickListener(new AlarmMeAdapter.OnMsgTooClickListener() {
            @Override
            public int onMsgTooClick(int pos) {
                AlarmModel one = earlier_list.get(pos);
                if(one.action.equals("requested_friend")){
                    responseRequestFriend(one.wId, MyApp.curUser.uid, one.uId, "1");
                } else if(one.action.equals("liked")){
                    responseSuperLike(one.wId, "me_too");
                } else if(one.action.equals("disliked")){
                    responseSuperDiss(one.wId, "throw_back");
                }
                return pos;
            }
        });
    }

    private void refreshFrag(){
        if(parent.alarm_mode == ALARM_FRIEND_F){
            tv_friend.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            tv_friend.setBackgroundResource(R.drawable.border_rectangle_black);
            tv_me.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
            rl_me.setBackgroundResource(R.drawable.border_rectangle_white);
            ll_friends.setVisibility(View.VISIBLE);
            ll_me.setVisibility(View.GONE);
            if(today_list.size() == 0){
                ll_today.setVisibility(View.GONE);
            } else{
                ll_today.setVisibility(View.VISIBLE);
            }

            if(yesterday_list.size() == 0){
                ll_yesterday.setVisibility(View.GONE);
            } else{
                ll_yesterday.setVisibility(View.VISIBLE);
            }
            if(last_list.size() == 0){
                ll_last.setVisibility(View.GONE);
            } else{
                ll_last.setVisibility(View.VISIBLE);
            }
            String sVal = sharedPrefrence.getString(context, SaveSharedPrefrence.KEY_BADGE_ME);
            if(sVal != null && sVal.equals("1")){
                civ_new.setVisibility(View.VISIBLE);
            } else{
                civ_new.setVisibility(View.GONE);
            }
            sharedPrefrence.putString(context, SaveSharedPrefrence.KEY_BADGE_FRIEND, "0");
        } else{
            tv_me.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            rl_me.setBackgroundResource(R.drawable.border_rectangle_black);
            tv_friend.setTextColor(ContextCompat.getColor(context,R.color.colorDGray));
            tv_friend.setBackgroundResource(R.drawable.border_rectangle_white);
            civ_new.setVisibility(View.GONE);
            ll_friends.setVisibility(View.GONE);
            ll_me.setVisibility(View.VISIBLE);

            if(new_list.size() == 0){
                ll_new.setVisibility(View.GONE);
            } else{
                ll_new.setVisibility(View.VISIBLE);
            }
            if(earlier_list.size() == 0){
                ll_earlier.setVisibility(View.GONE);
            } else{
                ll_earlier.setVisibility(View.VISIBLE);
            }
            sharedPrefrence.putString(context, SaveSharedPrefrence.KEY_BADGE_ME, "0");
        }
        if(parent != null){
            parent.refreshBadge();
        }
    }

    private void loadData(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Loading...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.WHATS_UP_FRIENDS_URL)
                //.addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("user_id", MyApp.curUser.uid)
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
                                JSONObject data_obj = response.getJSONObject("data");
                                JSONArray today_array = data_obj.getJSONArray("today");
                                JSONArray yesterday_array = data_obj.getJSONArray("yesterday");
                                JSONArray last_array = data_obj.getJSONArray("last7");
                                //today
                                for(int i = 0 ; i < today_array.length(); i++){
                                    AlarmModel alarmModel = analyseJSONObject(today_array.getJSONObject(i));
                                    if(alarmModel != null)
                                        today_list.add(alarmModel);
                                }
                                todayAdapter.setDataList(today_list);
                                //yesterday
                                for(int i = 0 ; i < yesterday_array.length(); i++){
                                    AlarmModel alarmModel = analyseJSONObject(yesterday_array.getJSONObject(i));
                                    if(alarmModel != null)
                                        yesterday_list.add(alarmModel);
                                }
                                yesterdayAdapter.setDataList(yesterday_list);
                                //last7
                                for(int i = 0 ; i < last_array.length(); i++){
                                    AlarmModel alarmModel = analyseJSONObject(last_array.getJSONObject(i));
                                    if(alarmModel != null)
                                        last_list.add(alarmModel);
                                }
                                lastAdapter.setDataList(last_list);
                                refreshFrag();

                            } else{
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

    public void reloadData(){
        AndroidNetworking.post(Const.HOST_URL + Const.WHATS_UP_FRIENDS_URL)
                .addBodyParameter("user_id", MyApp.curUser.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int bSuccess = response.getInt("success");
                            if (bSuccess == 1) {
                                today_list.clear();
                                yesterday_list.clear();
                                last_list.clear();
                                JSONObject data_obj = response.getJSONObject("data");
                                JSONArray today_array = data_obj.getJSONArray("today");
                                JSONArray yesterday_array = data_obj.getJSONArray("yesterday");
                                JSONArray last_array = data_obj.getJSONArray("last7");
                                //today
                                for(int i = 0 ; i < today_array.length(); i++){
                                    AlarmModel alarmModel = analyseJSONObject(today_array.getJSONObject(i));
                                    if(alarmModel != null)
                                        today_list.add(alarmModel);
                                }
                                todayAdapter.setDataList(today_list);
                                //yesterday
                                for(int i = 0 ; i < yesterday_array.length(); i++){
                                    AlarmModel alarmModel = analyseJSONObject(yesterday_array.getJSONObject(i));
                                    if(alarmModel != null)
                                        yesterday_list.add(alarmModel);
                                }
                                yesterdayAdapter.setDataList(yesterday_list);
                                //last7
                                for(int i = 0 ; i < last_array.length(); i++){
                                    AlarmModel alarmModel = analyseJSONObject(last_array.getJSONObject(i));
                                    if(alarmModel != null)
                                        last_list.add(alarmModel);
                                }
                                lastAdapter.setDataList(last_list);
                                refreshFrag();
                            }
                        }catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void loadMeData(){
        AndroidNetworking.post(Const.HOST_URL + Const.WHATS_UP_ME_URL)
                .addBodyParameter("user_id", MyApp.curUser.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int bSuccess = response.getInt("success");
                            if (bSuccess == 1) {
                                new_list.clear();
                                earlier_list.clear();
                                JSONObject data_obj = response.getJSONObject("data");
                                JSONArray new_array = data_obj.getJSONArray("last");
                                JSONArray earlier_array = data_obj.getJSONArray("earlier");
                                //new
                                for(int i = 0 ; i < new_array.length(); i++){
                                    AlarmModel alarmModel = analyseJSONObjectForMe(new_array.getJSONObject(i));
                                    if(alarmModel != null)
                                        new_list.add(alarmModel);
                                }
                                newAdapter.setDataList(new_list);
                                //earlier
                                for(int i = 0 ; i < earlier_array.length(); i++){
                                    AlarmModel alarmModel = analyseJSONObjectForMe(earlier_array.getJSONObject(i));
                                    if(alarmModel != null)
                                        earlier_list.add(alarmModel);
                                }
                                earlierAdapter.setDataList(earlier_list);
                                refreshFrag();
                            }
                        }catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        String type = event.action;
        if(!type.equals("chat")){
            reloadData();
            loadMeData();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshFrag();
    }

    private void responseRequestFriend(String wid, String responser, String requester, String res){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Response...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.ACCEPT_REJECT_FRIENDS_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("whatsup_id", wid)
                .addBodyParameter("responser_id", responser)
                .addBodyParameter("requester_id", requester)
                .addBodyParameter("is_accept", res)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        loadMeData();
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    private void responseSuperDiss(String wid, String res){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Response...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.SUPER_DISS_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("whatsup_id", wid)
                .addBodyParameter("reply", res)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        loadMeData();
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    private void responseSuperLike(String wid, String res){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Response...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.SUPER_LIKE_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("whatsup_id", wid)
                .addBodyParameter("reply", res)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        loadMeData();
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    private AlarmModel analyseJSONObjectForMe(JSONObject obj){
        AlarmModel one = new AlarmModel();
        try{
            one.action = obj.getString("action");
            if(one.action.equals("requested_friend")){
                JSONObject actor = obj.getJSONObject("actor_data");
                one.wId = obj.getString("id");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                one.is_checked = obj.getInt("is_checked");
                one.sDesc = String.format("<b>%s</b> has sent you a friend request.", one.uName);
                one.user = analyseUserObject(actor);
                return one;
            } else if(one.action.equals("liked")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                one.is_checked = obj.getInt("is_checked");
                one.sDesc = String.format("<b>%s</b> has sent you a Heart.", one.uName);
                one.user = analyseUserObject(actor);
                return one;
            } else if(one.action.equals("disliked")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                one.is_checked = obj.getInt("is_checked");
                one.sDesc = String.format("<b>%s</b> has sent you a Poop.", one.uName);
                one.user = analyseUserObject(actor);
                return one;
            } else if(one.action.equals("me_too")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                one.sDesc = String.format("<b>%s</b> superlikes you too.", one.uName);
                one.user = analyseUserObject(actor);
                return one;
            } else if(one.action.equals("throw_back")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                one.sDesc = String.format("<b>%s</b> throws back your Poop.", one.uName);
                one.user = analyseUserObject(actor);
                return one;
            } else if(one.action.equals("accept_friend_request")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                one.sDesc = String.format("<b>%s</b> has become your friend.", one.uName);
                one.user = analyseUserObject(actor);
                return one;
            } else if(one.action.equals("friend_invite")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                JSONObject data_obj = obj.getJSONObject("data");
                String eTitle = data_obj.getString("event_title");
                one.sDesc = String.format("<b>%s</b> invites you to a nearby event <b>%s</b>.", one.uName, eTitle);
                one.user = analyseUserObject(actor);
                //one.event = analyseEventObject(data_obj);
                return one;
            } else if(one.action.equals("non_friend_invite")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                JSONObject data_obj = obj.getJSONObject("data");
                String eTitle = data_obj.getString("event_title");
                one.sDesc = String.format("<b>%s</b> invites you to a nearby event <b>%s</b>.", one.uName, eTitle);
                one.user = analyseUserObject(actor);
                //one.event = analyseEventObject(data_obj);
                return one;
            } else if(one.action.equals("ping_2hours_event")){
                one.wId = obj.getString("id");
                JSONObject data_obj = obj.getJSONObject("data");
                one.event = analyseEventObject(data_obj);
                one.created_at = obj.getString("created_at");
                one.sDesc = String.format("Your event <b>%s</b> is going to start in 2 hours.", one.event.title);
                return one;
            } else if(one.action.equals("ping_30mins_event")){
                one.wId = obj.getString("id");
                JSONObject data_obj = obj.getJSONObject("data");
                one.event = analyseEventObject(data_obj);
                one.created_at = obj.getString("created_at");
                one.sDesc = String.format("Your event <b>%s</b> is going to start in 30 mins.", one.event.title);
                return one;
            } else if(one.action.equals("ping_less_member")){
                one.wId = obj.getString("id");
                JSONObject data_obj = obj.getJSONObject("data");
                one.event = analyseEventObject(data_obj);
                one.created_at = obj.getString("created_at");
                one.sDesc = String.format("Your event <b>%s</b> didn\'t reach minimum number of people and will be cancelled.", one.event.title);
                return one;
            } else if(one.action.equals("tagged")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                JSONObject data_obj = obj.getJSONObject("data");
                String sTags = data_obj.getString("tags");
                sTags = "#" + sTags;
                String tag_str = sTags.replace(",", ", #");
                one.sDesc = String.format("<b>%s</b> tagged you %s.", one.uName, tag_str);
                one.user = analyseUserObject(actor);
                return one;
            } else if(one.action.equals("tipped")){
                one.wId = obj.getString("id");
                JSONObject actor = obj.getJSONObject("actor_data");
                one.uId = actor.getString("id");
                one.uName = actor.getString("first_name");
                one.uPhoto = actor.getString("avatar");
                one.created_at = obj.getString("created_at");
                //JSONObject data_obj = obj.getJSONObject("data");
                one.sDesc = String.format("<b>%s</b> tipped you.", one.uName);
                one.user = analyseUserObject(actor);
                return one;
            }
        } catch(JSONException ex){
            ex.printStackTrace();
        }
        return null;
    }

    private AlarmModel analyseJSONObject(JSONObject obj){
        AlarmModel one = new AlarmModel();
        try{
            one.action = obj.getString("action");
            JSONObject actor = obj.getJSONObject("actor_data");
            one.uId = actor.getString("id");
            one.uName = actor.getString("first_name");
            one.uPhoto = actor.getString("avatar");
            one.created_at = obj.getString("created_at");
            one.user = analyseUserObject(actor);
            if(one.action.equals("pinned")){
                String gen = actor.getString("gender");
                String self_str = "himself";
                if(gen.equals("female")) self_str = "herself";
                String sAddr = actor.getString("address");
                one.sDesc = String.format("<b>%s</b> pinned %s at <b>%s</b>.", one.uName, self_str, sAddr);
                return one;
            } else if(one.action.equals("tagged")){
                //A tagged B Z,Z
                JSONObject whom_obj = obj.getJSONObject("whom_data");
                String b_name = whom_obj.getString("first_name");
                JSONObject data_obj = obj.getJSONObject("data");
                String sTags = data_obj.getString("tags");
                sTags = "#" + sTags;
                String tag_str = sTags.replace(",", ", #");
                one.sDesc = String.format("<b>%s</b> tagged <b>%s</b> %s.", one.uName, b_name, tag_str);
                return one;
            } else if(one.action.equals("join_event")){
                //xxx is joining the event yyy.
                JSONObject data_obj = obj.getJSONObject("data");
                String eTitle = data_obj.getString("event_title");
                one.sDesc = String.format("<b>%s</b> is joining the event <b>%s</b>.", one.uName, eTitle);
                return one;
            } else if(one.action.equals("host_event")){
                JSONObject data_obj = obj.getJSONObject("data");
                String eTitle = data_obj.getString("event_title");
                one.sDesc = String.format("<b>%s</b> is hosting a new event <b>%s</b>.", one.uName, eTitle);
                return one;
            } else if(one.action.equals("friend_invite")){
                //xxx invited yyy and 3 more people to zzz
                JSONObject whom_obj = obj.getJSONObject("whom_data");
                String b_name = whom_obj.getString("first_name");
                JSONObject data_obj = obj.getJSONObject("data");
                String eTitle = data_obj.getString("event_title");
                one.sDesc = String.format("<b>%s</b> invited %s to <b>%s</b>.", one.uName, b_name, eTitle);
                return one;
            }
        } catch(JSONException ex){
            ex.printStackTrace();
        }
        return null;
    }

    private UserModel analyseUserObject(JSONObject user_obj){
        UserModel one = new UserModel();
        try{
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

            JSONArray img_array = user_obj.getJSONArray("images");
            one.images = new ArrayList<>();
            for(int j = 0; j < img_array.length(); j++){
                String sImg = img_array.getString(j);
                one.images.add(sImg);
            }

            if(user_obj.has("tag_list")){
                JSONArray tag_array = user_obj.getJSONArray("tag_list");
                one.tags = new ArrayList<>();
                for(int j = 0; j < tag_array.length(); j++){
                    String sTag = tag_array.getString(j);
                    one.tags.add(sTag);
                }
            }
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
            if(!user_obj.isNull("location_updated_at")){
                one.lat = user_obj.getDouble("lat");
                one.lng = user_obj.getDouble("lng");
                one.location_updated_at = user_obj.getString("location_updated_at");//timestamp unit:seconds
            }
            one.is_liked = user_obj.getInt("is_liked");
            one.is_friend = user_obj.getInt("is_my_friend");
            one.is_friendable = user_obj.getInt("is_friendable");
            return one;
        } catch (JSONException ex){
            ex.printStackTrace();
        }
        return null;
    }

    private EventModel analyseEventObject(JSONObject event_obj){
        EventModel other = new EventModel();
        try{
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
            other.is_pay_later = event_obj.getInt("is_pay_later");
            other.loc = event_obj.getDouble("lat") + "," + event_obj.getDouble("lng");
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
            return other;
        } catch (JSONException ex){
            ex.printStackTrace();
        }
         return null;
    }

    private void showHoppersMap(EventModel ev){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        HoppersFragment hoppersFrag = new HoppersFragment();
        hoppersFrag.ev = ev;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
        transaction.add(R.id.home_frame, hoppersFrag, Const.FRAG_HOPPERS_TAG).addToBackStack(Const.FRAG_HOPPERS_TAG).commit();
    }

    private void showReeditEvent(EventModel ev){
        parent.event_photo = "";
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        HostEventFragment hosteventFrag = new HostEventFragment();
        hosteventFrag.draftEvent = ev;
        hosteventFrag.bChooseAddr = true;
        fragmentTransaction.setCustomAnimations(R.anim.ani_enter_up, R.anim.ani_enter_down);
        //fragmentTransaction.commit();
        fragmentTransaction.add(R.id.home_frame, hosteventFrag, Const.FRAG_EVENT_TAG).addToBackStack(Const.FRAG_EVENT_TAG).commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
