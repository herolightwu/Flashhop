package com.flashhop.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.GroupListAdapter;
import com.flashhop.app.adapters.HangoutListAdapter;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.ChatGroupModel;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.HangoutModel;
import com.flashhop.app.models.LastMsgModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.KeyboardUtil;
import com.flashhop.app.utils.TxtUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.flashhop.app.utils.Const.APP_TAG;
import static com.flashhop.app.utils.Const.CHAT_GROUP_F;
import static com.flashhop.app.utils.Const.CHAT_HANGOUT_F;

public class ChatFragment extends Fragment {

    HomeActivity parent;
    Context context;

    TextView tv_group, tv_hangout;
    int nSelect = CHAT_GROUP_F;
    LinearLayout ll_group, ll_hangout, ll_outside;
    EditText et_search;
    RecyclerView group_recycler;
    GroupListAdapter groupListAdapter;
    List<ChatGroupModel> group_list = new ArrayList<>();
    RecyclerView hangout_recycler;
    HangoutListAdapter hangoutListAdapter;
    List<HangoutModel> data_list = new ArrayList<>();

    DatabaseReference database;
    SaveSharedPrefrence sharedPrefrence;

    public ChatFragment() {
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
        View rView = inflater.inflate(R.layout.fragment_chat, container, false);
        parent = (HomeActivity) getActivity();
        context = getContext();
        sharedPrefrence = new SaveSharedPrefrence();
        database = FirebaseDatabase.getInstance().getReference();
        initLayout(rView);
        return rView;
    }

    private void initLayout(View rootView){
        ll_outside = rootView.findViewById(R.id.chat_frag_outside);
        ll_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtil.hideKeyboard(parent);
            }
        });
        tv_group = rootView.findViewById(R.id.chat_frag_tv_group);
        tv_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_group.setTextColor(ContextCompat.getColor(context,R.color.colorWhite));
                tv_group.setBackgroundResource(R.drawable.border_rectangle_black);
                tv_hangout.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                tv_hangout.setBackgroundResource(R.drawable.border_rectangle_white);
                nSelect = CHAT_GROUP_F;
                changeFrag();
            }
        });
        tv_hangout = rootView.findViewById(R.id.chat_frag_tv_hangout);
        tv_hangout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_hangout.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                tv_hangout.setBackgroundResource(R.drawable.border_rectangle_black);
                tv_group.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
                tv_group.setBackgroundResource(R.drawable.border_rectangle_white);
                nSelect = CHAT_HANGOUT_F;
                changeFrag();
            }
        });

        ll_group = rootView.findViewById(R.id.chat_frag_group);
        group_recycler = rootView.findViewById(R.id.chat_frag_group_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        group_recycler.setLayoutManager(layoutManager);

        loadData();
        groupListAdapter = new GroupListAdapter(group_list, context);
        group_recycler.setAdapter(groupListAdapter);
        groupListAdapter.setOnItemClickListener(new GroupListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                KeyboardUtil.hideKeyboard(parent);
                gotoGroupChat(group_list.get(pos).event);
                return pos;
            }
        });

        /*HangoutModel one = new HangoutModel();
        one.name = MyApp.curUser.first_name;
        one.photo = MyApp.curUser.photo_url;
        one.action = "like";
        one.nCount = 1;
        data_list.add(one);*/

        ll_hangout = rootView.findViewById(R.id.chat_frag_hangout);
        hangout_recycler = rootView.findViewById(R.id.chat_frag_hangout_recycler);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(context);
        hangout_recycler.setLayoutManager(layoutManager1);
        hangoutListAdapter = new HangoutListAdapter(data_list, context);
        hangout_recycler.setAdapter(hangoutListAdapter);

        et_search = rootView.findViewById(R.id.chat_frag_et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(nSelect == CHAT_GROUP_F){
                    groupListAdapter.getFilter().filter(charSequence);
                } else{
                    hangoutListAdapter.getFilter().filter(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void changeFrag(){
        et_search.setText("");
        et_search.clearFocus();
        KeyboardUtil.hideKeyboard(parent);
        if(nSelect == CHAT_GROUP_F){
            ll_group.setVisibility(View.VISIBLE);
            ll_hangout.setVisibility(View.GONE);
        } else{
            ll_group.setVisibility(View.GONE);
            ll_hangout.setVisibility(View.VISIBLE);
        }
    }

    private void gotoGroupChat(EventModel ev){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
        GroupChatFragment group_frag = new GroupChatFragment();
        group_frag.group_event = ev;
        fragmentTransaction.add(R.id.home_frame, group_frag, Const.FRAG_CHAT_GROUP).addToBackStack(Const.FRAG_CHAT_GROUP).commit();
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
                .addBodyParameter("before_days", "7")
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
                                        if(creator_obj.has("is_my_friend")){
                                            creat_user.is_friend = creator_obj.getInt("is_my_friend");
                                            creat_user.is_friendable = creator_obj.getInt("is_friendable");
                                        }
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
                                        if(user_obj.has("is_my_friend")){
                                            one.is_friend = user_obj.getInt("is_my_friend");
                                            one.is_friendable = user_obj.getInt("is_friendable");
                                        }
                                        other.members.add(one);
                                    }
                                    other.state = 1;
                                    data_list.add(other);
                                }
                                for(int i = 0; i < data_list.size(); i++){
                                    ChatGroupModel one = new ChatGroupModel();
                                    one.event = data_list.get(i);
                                    if(checkJoinedEvent(one.event))// && one.event.members.size() > 0
                                    {
                                        group_list.add(one);
                                    }
                                }
                                groupListAdapter.setDataList(group_list);
                                checkUnreadMsg();
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

        AndroidNetworking.post(Const.HOST_URL + Const.HANGOUTS_URL)
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
                                JSONObject data_obj = response.getJSONObject("data");
                                JSONArray heart_array = data_obj.getJSONArray("heart");
                                JSONArray hangout_array = data_obj.getJSONArray("hangout");
                                JSONArray poop_array = data_obj.getJSONArray("poop");
                                for(int i = 0; i < heart_array.length(); i++){
                                    JSONObject obj = heart_array.getJSONObject(i);
                                    if(obj.isNull("user")) continue;
                                    JSONObject user_obj = obj.getJSONObject("user");
                                    HangoutModel one = new HangoutModel();
                                    one.name = user_obj.getString("userName");
                                    one.photo = user_obj.getString("avatar");
                                    one.action = "like";
                                    one.nCount = obj.getInt("count");
                                    data_list.add(one);
                                }

                                for(int i = 0; i < hangout_array.length(); i++){
                                    JSONObject obj = hangout_array.getJSONObject(i);
                                    if(obj.isNull("user")) continue;
                                    JSONObject user_obj = obj.getJSONObject("user");
                                    HangoutModel one = new HangoutModel();
                                    one.name = user_obj.getString("userName");
                                    one.photo = user_obj.getString("avatar");
                                    one.action = "thunder";
                                    one.nCount = obj.getInt("count");
                                    data_list.add(one);
                                }

                                for(int i = 0; i < poop_array.length(); i++){
                                    JSONObject obj = poop_array.getJSONObject(i);
                                    if(obj.isNull("user")) continue;
                                    JSONObject user_obj = obj.getJSONObject("user");
                                    HangoutModel one = new HangoutModel();
                                    one.name = user_obj.getString("userName");
                                    one.photo = user_obj.getString("avatar");
                                    one.action = "dislike";
                                    one.nCount = obj.getInt("count");
                                    data_list.add(one);
                                }

                                hangoutListAdapter.setDataList(data_list);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void checkUnreadMsg(){
        database.child("last_history").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot oneshot : dataSnapshot.getChildren()){
                    LastMsgModel one = oneshot.getValue(LastMsgModel.class);
                    String kId = oneshot.getKey();
                    for(int i = 0; i < group_list.size(); i++){
                        ChatGroupModel group = group_list.get(i);
                        if(group.event.id.equals(kId))
                        {
                            group.subject = one.msg;
                            if(!one.likes.contains(MyApp.curUser.uid)){
                                group.bUnread = true;
                            } else{
                                group.bUnread = false;
                            }
                        }
                    }
                }
                groupListAdapter.setDataList(group_list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean checkJoinedEvent(EventModel ev){
        if(ev.creator == null) return false;
        long diff = TxtUtils.getDifferenceTime(ev.date, ev.time);
        if( ((-1) * diff) >= (7 * 24 * 60)){
            return false;
        }
        if(ev.creator.uid.equals(MyApp.curUser.uid)){
            return true;
        }
        for(int k = 0; k < ev.members.size(); k++){
            if(ev.members.get(k).uid.equals(MyApp.curUser.uid)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume(){
        super.onResume();
        changeFrag();
        sharedPrefrence.putString(context, SaveSharedPrefrence.KEY_BADGE_CHAT, "0");
        parent.refreshBadge();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
