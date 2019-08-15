package com.flashhop.app.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.bottomnavigationviewex.BottomNavigationViewEx;
import com.flashhop.app.fragments.AlarmFragment;
import com.flashhop.app.fragments.ChatFragment;
import com.flashhop.app.fragments.EditProfileFragment;
import com.flashhop.app.fragments.FilterFragment;
import com.flashhop.app.fragments.GalleryFragment;
import com.flashhop.app.fragments.GroupChatFragment;
import com.flashhop.app.fragments.HomeFragment;
import com.flashhop.app.fragments.HostEventFragment;
import com.flashhop.app.fragments.HostingFragment;
import com.flashhop.app.fragments.InviteFriendsFragment;
import com.flashhop.app.fragments.OneGalleryFragment;
import com.flashhop.app.fragments.ProfileFragment;
import com.flashhop.app.fragments.TipsFragment;
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.FilterModel;
import com.flashhop.app.models.MessageEvent;
import com.flashhop.app.models.ProfileModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.services.GPSService;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.fragments.GroupChatFragment.PERMISSION_RECORD_AUDIO;
import static com.flashhop.app.start.WelcomeActivity.LOCATION_PERMISSIONS_REQUEST;
import static com.flashhop.app.utils.Const.ALARM_FRIEND_F;
import static com.flashhop.app.utils.Const.APP_TAG;
import static com.flashhop.app.utils.Const.HOME_ALARM;
import static com.flashhop.app.utils.Const.HOME_CHAT;
import static com.flashhop.app.utils.Const.HOME_MSG;

public class HomeActivity extends AppCompatActivity {

    public static final int STORAGE_PERMISSIONS_REQUEST= 7890;
    public static final int STORAGE_PERMISSIONS_REQUEST_ONE= 7891;
    BottomNavigationViewEx navView;
    int nGuideStep = 0;
    RelativeLayout rl_guide, rl_profile, rl_option;
    LinearLayout ll_guide_pin, ll_guide_event, ll_guide_filter, ll_guide_marker, ll_guide_annotation;
    TextView tv_skip;

    public ProfileModel editProData = new ProfileModel();
    public String event_photo = "";
    public int view_mode = Const.VIEW_FILTER_ALL;
    public int alarm_mode = ALARM_FRIEND_F;
    public FilterModel filter_opt = new FilterModel();
    public List<UserModel> user_list = new ArrayList<>();
    public List<EventModel> event_list = new ArrayList<>();

    DatabaseReference database;
    SaveSharedPrefrence sharedPrefrence = new SaveSharedPrefrence();
    MySQLiteHelper mHelper;
    //GPSTracker gps;

    private BottomNavigationViewEx.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationViewEx.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Menu menu = navView.getMenu();
            menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home_w);
            menu.findItem(R.id.navigation_mine).setIcon(R.drawable.ic_mine_w);
            menu.findItem(R.id.navigation_chat).setIcon(R.drawable.ic_chat_w);
            menu.findItem(R.id.navigation_alarm).setIcon(R.drawable.ic_alarm_w);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    item.setIcon(R.drawable.ic_home_y);
                    MyApp.home_type = Const.HOME_HOME;
                    displayView();
                    return true;
                case R.id.navigation_mine:
                    item.setIcon(R.drawable.ic_mine_y);
                    MyApp.home_type = Const.HOME_MINE;
                    displayView();
                    return true;
                case R.id.navigation_chat:
                    item.setIcon(R.drawable.ic_chat_y);
                    MyApp.home_type = Const.HOME_CHAT;
                    displayView();
                    return true;
                case R.id.navigation_alarm:
                    item.setIcon(R.drawable.ic_alarm_y);
                    alarm_mode = ALARM_FRIEND_F;
                    MyApp.home_type = Const.HOME_ALARM;
                    displayView();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getApplicationContext());
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_home);

        database = FirebaseDatabase.getInstance().getReference();

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mHelper = MySQLiteHelper.getInstance(getApplicationContext());

        //gps = new GPSTracker(this);
        initLayout();

        String sGuide = sharedPrefrence.getString(getApplicationContext(), SaveSharedPrefrence.KEY_GUIDE);
        if(sGuide == null){
            rl_guide.setVisibility(View.VISIBLE);
            processGuide();
        } else{
            rl_guide.setVisibility(View.GONE);
        }

        //initialize filter
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
        String strDate = dateFormat.format(date);
        filter_opt.event_date = strDate;
        filter_opt.event_category = "party,eating,dating,sports,outdoors,games,study,spiritual,arts";//all
        filter_opt.gender = "co";
        filter_opt.filter_option = "both";
        filter_opt.period = "7";

        if(checkPermission()){
            GPSService mSensorService = new GPSService();
            Intent intent = new Intent(getApplicationContext(), mSensorService.getClass());
            if (!isMyServiceRunning(mSensorService.getClass())) {
                startService(intent);
            }
        }

        navView.setSelectedItemId(R.id.navigation_home);
    }

    public void refreshHome(){
        onResume();
        navView.setSelectedItemId(R.id.navigation_home);
    }

    public void setNavViewVisible(boolean bVisible){
        if(bVisible){
            navView.setVisibility(View.VISIBLE);
        } else{
            navView.setVisibility(View.GONE);
        }
    }

    private void initLayout(){
        tv_skip = findViewById(R.id.home_guide_tv_skip);
        tv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_GUIDE, "home");
                rl_guide.setVisibility(View.GONE);
            }
        });
        rl_guide = findViewById(R.id.home_rl_guide);
        rl_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nGuideStep++;
                processGuide();
            }
        });
        ll_guide_pin = findViewById(R.id.home_guide_ll_pin);
        rl_option = findViewById(R.id.home_guide_rl_option);
        ll_guide_event = findViewById(R.id.home_guide_ll_event);
        ll_guide_filter = findViewById(R.id.home_guide_ll_filter);
        rl_profile = findViewById(R.id.home_guide_rl_profile);
        ll_guide_marker = findViewById(R.id.home_guide_ll_marker);
        ll_guide_annotation = findViewById(R.id.home_guide_ll_annotation);

    }

    private void processGuide(){
        switch (nGuideStep){
            case 0://pin
                ll_guide_pin.setVisibility(View.VISIBLE);
                rl_option.setVisibility(View.GONE);
                ll_guide_event.setVisibility(View.GONE);
                ll_guide_filter.setVisibility(View.GONE);
                rl_profile.setVisibility(View.GONE);
                ll_guide_marker.setVisibility(View.GONE);
                ll_guide_annotation.setVisibility(View.GONE);
                break;
            case 1://event
                ll_guide_pin.setVisibility(View.GONE);
                rl_option.setVisibility(View.VISIBLE);
                ll_guide_event.setVisibility(View.VISIBLE);
                ll_guide_filter.setVisibility(View.GONE);
                rl_profile.setVisibility(View.GONE);
                ll_guide_marker.setVisibility(View.GONE);
                ll_guide_annotation.setVisibility(View.GONE);
                break;
            case 2://filter
                ll_guide_pin.setVisibility(View.GONE);
                rl_option.setVisibility(View.VISIBLE);
                ll_guide_event.setVisibility(View.GONE);
                ll_guide_filter.setVisibility(View.VISIBLE);
                rl_profile.setVisibility(View.GONE);
                ll_guide_marker.setVisibility(View.GONE);
                ll_guide_annotation.setVisibility(View.GONE);
                break;
            case 3://profile
                ll_guide_pin.setVisibility(View.GONE);
                rl_option.setVisibility(View.GONE);
                ll_guide_event.setVisibility(View.GONE);
                ll_guide_filter.setVisibility(View.GONE);
                rl_profile.setVisibility(View.VISIBLE);
                ll_guide_marker.setVisibility(View.GONE);
                ll_guide_annotation.setVisibility(View.GONE);
                break;
            case 4://mark
                ll_guide_pin.setVisibility(View.GONE);
                rl_option.setVisibility(View.GONE);
                ll_guide_event.setVisibility(View.GONE);
                ll_guide_filter.setVisibility(View.GONE);
                rl_profile.setVisibility(View.GONE);
                ll_guide_marker.setVisibility(View.VISIBLE);
                ll_guide_annotation.setVisibility(View.GONE);
                break;
            case 5://annotation
                ll_guide_pin.setVisibility(View.GONE);
                rl_option.setVisibility(View.GONE);
                ll_guide_event.setVisibility(View.GONE);
                ll_guide_filter.setVisibility(View.GONE);
                rl_profile.setVisibility(View.GONE);
                ll_guide_marker.setVisibility(View.GONE);
                ll_guide_annotation.setVisibility(View.VISIBLE);
                break;
            default:
                sharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_GUIDE, "home");
                rl_guide.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public void onResume(){
        super.onResume();

        if(database!= null){
            long val = 1;
            database.child("online").child(MyApp.curUser.uid).setValue(val);
        }
        /*int nMode = MyApp.home_type;
        switch (nMode) {
            case 1:
                navView.setSelectedItemId(R.id.navigation_mine);
                break;
            case 2:
                navView.setSelectedItemId(R.id.navigation_chat);
                break;
            case 3:
                navView.setSelectedItemId(R.id.navigation_alarm);
                break;
            case 4://event page
                break;
            case 6://preview page
                break;
            case 7://profile page
                break;
            case 8://edit profile page
                break;
            default:
                navView.setSelectedItemId(R.id.navigation_home);
                break;
        }*/

        //HomeFragment myFragment = (HomeFragment)getSupportFragmentManager().findFragmentByTag(Const.FRAG_HOME_TAG);
        //if (myFragment != null && myFragment.isVisible()) {
        //    filterDataFromServer();
        //}

        refreshBadge();
    }

    public void refreshBadge(){
        String sVal;
        if(MyApp.home_type != HOME_CHAT || MyApp.home_type != HOME_MSG){
             sVal = sharedPrefrence.getString(getApplicationContext(), SaveSharedPrefrence.KEY_BADGE_CHAT);
            if(sVal != null && sVal.equals("1")){
                WindowUtils.showBadge(this, navView, R.id.navigation_chat, "1");
            } else{
                WindowUtils.removeBadge(navView, R.id.navigation_chat);
            }
        } else{
            sharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_BADGE_CHAT, "0");
            WindowUtils.removeBadge(navView, R.id.navigation_chat);
        }

        //if(MyApp.home_type != HOME_ALARM){
            sVal = sharedPrefrence.getString(getApplicationContext(), SaveSharedPrefrence.KEY_BADGE_ME);
            String ssVal = sharedPrefrence.getString(getApplicationContext(), SaveSharedPrefrence.KEY_BADGE_FRIEND);
            if(sVal != null && ssVal != null && (sVal.equals("1") || ssVal.equals("1"))){
                WindowUtils.showBadge(this, navView, R.id.navigation_alarm, "1");
            } else{
                WindowUtils.removeBadge(navView, R.id.navigation_alarm);
            }
        //}
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        registerReceiver(broadcastReceiver, new IntentFilter(GPSService.str_receiver));
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        String type = event.action;
        if(type.equals("chat")){
            if(MyApp.home_type != HOME_CHAT || MyApp.home_type != HOME_MSG){
                if(event.bOpened){
                    MyApp.home_type = HOME_CHAT;
                    navView.setSelectedItemId(R.id.navigation_chat);
                    return;
                }
            }
        } else if(type.equals("liked")){
            if(MyApp.home_type != HOME_ALARM){
                //if(!event.bOpened){
                    UserModel one = new UserModel();
                    one.uid = event.sender_id;
                    one.first_name = event.sender_name;
                    one.photo_url = event.sender_avatar;
                    showLikeDlg(one, event.alarm_id);
                    return;
                /*} else{
                    MyApp.home_type = HOME_ALARM;
                    navView.setSelectedItemId(R.id.navigation_alarm);
                    return;
                }*/
            }
        } else if(type.equals("disliked")){
            if(MyApp.home_type != HOME_ALARM){
                //if(!event.bOpened){
                    UserModel one = new UserModel();
                    one.uid = event.sender_id;
                    one.first_name = event.sender_name;
                    one.photo_url = event.sender_avatar;
                    showDissDlg(one, event.alarm_id);
                    return;
               /* } else{
                    MyApp.home_type = HOME_ALARM;
                    navView.setSelectedItemId(R.id.navigation_alarm);
                    return;
                }*/
            }
        } else if(type.equals("me_too")){
            if(MyApp.home_type != HOME_ALARM){
                //if(!event.bOpened){
                    UserModel one = new UserModel();
                    one.uid = event.sender_id;
                    one.first_name = event.sender_name;
                    one.photo_url = event.sender_avatar;
                    showLikeTooDlg(one);
                    return;
                /*} else{
                    MyApp.home_type = HOME_ALARM;
                    navView.setSelectedItemId(R.id.navigation_alarm);
                    return;
                }*/
            }
        } else{
            if(MyApp.home_type != HOME_ALARM){
                if(event.bOpened){
                    MyApp.home_type = HOME_ALARM;
                    navView.setSelectedItemId(R.id.navigation_alarm);
                    return;
                }
            }
        }
        refreshBadge();
    }

    private void showLikeDlg(final UserModel ulike, String wid){
        TextView tv_desc;
        Button btn_hell, btn_yet, btn_too;
        ImageView iv_icon;
        CircleImageView iv_photo;
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_like_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = dialog.findViewById(R.id.custom_like_tv_desc);
        btn_hell = dialog.findViewById(R.id.custom_like_btn_hell);
        btn_yet = dialog.findViewById(R.id.custom_like_btn_yet);
        btn_too = dialog.findViewById(R.id.custom_like_btn_too);
        iv_photo = dialog.findViewById(R.id.custom_like_iv_photo);
        iv_icon = dialog.findViewById(R.id.custom_like_iv_icon);

        Picasso.get()
                .load(ulike.photo_url)
                .resize(100, 100)
                .into(iv_photo);

        String msg = String.format(getString(R.string.user_liked_you), ulike.first_name);
        tv_desc.setText(msg);
        dialog.show();

        btn_hell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                responseSuperLike(wid, "hell_no");
                dialog.dismiss();
            }
        });

        btn_yet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                responseSuperLike(wid, "not_yet");
                dialog.dismiss();
            }
        });
        btn_too.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                responseSuperLike(wid, "me_too");
            }
        });

    }

    private void showLikeTooDlg(UserModel uToo){
        TextView tv_desc;
        Button btn_hell, btn_yet, btn_too;
        ImageView iv_icon;
        CircleImageView iv_photo;
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_like_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = dialog.findViewById(R.id.custom_like_tv_desc);
        btn_hell = dialog.findViewById(R.id.custom_like_btn_hell);
        btn_yet = dialog.findViewById(R.id.custom_like_btn_yet);
        btn_too = dialog.findViewById(R.id.custom_like_btn_too);
        iv_photo = dialog.findViewById(R.id.custom_like_iv_photo);
        iv_icon = dialog.findViewById(R.id.custom_like_iv_icon);

        String msg = String.format(getString(R.string.like_too_sweet), uToo.first_name);
        tv_desc.setText(msg);
        iv_photo.setVisibility(View.GONE);
        btn_hell.setVisibility(View.GONE);
        btn_yet.setVisibility(View.GONE);
        btn_too.setVisibility(View.GONE);
        dialog.show();

    }

    private void showDissDlg(final UserModel uDiss, String  wid){
        TextView tv_desc;
        Button btn_hell, btn_yet, btn_too;
        ImageView iv_photo, iv_icon;
        final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_like_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = dialog.findViewById(R.id.custom_like_tv_desc);
        btn_hell = dialog.findViewById(R.id.custom_like_btn_hell);
        btn_yet = dialog.findViewById(R.id.custom_like_btn_yet);
        btn_too = dialog.findViewById(R.id.custom_like_btn_too);
        iv_photo = dialog.findViewById(R.id.custom_like_iv_photo);
        iv_icon = dialog.findViewById(R.id.custom_like_iv_icon);

        Picasso.get()
                .load(uDiss.photo_url)
                .resize(100, 100)
                .into(iv_photo);

        String msg = String.format(getString(R.string.user_dissed_you), uDiss.first_name);
        tv_desc.setText(msg);
        iv_icon.setImageResource(R.drawable.ic_dissed);
        btn_hell.setText(R.string.diss_whatever);
        btn_yet.setVisibility(View.GONE);
        btn_too.setText(R.string.diss_throw_back);
        dialog.show();

        btn_hell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                responseSuperDiss(wid, "whatever");
                dialog.dismiss();
            }
        });

        btn_yet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_too.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                responseSuperDiss(wid, "throw_back");
                dialog.dismiss();
            }
        });

    }

    private void responseSuperDiss(String wid, String res){
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
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
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    private void responseSuperLike(String wid, String res){
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
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
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if(database!= null){
            long val = 0;
            database.child("online").child(MyApp.curUser.uid).setValue(val);
        }
    }

    public void filterDataFromServer(){
        long tslong = System.currentTimeMillis()/1000;
        AndroidNetworking.post(Const.HOST_URL + Const.FILTER_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("event_date", filter_opt.event_date)
                .addBodyParameter("event_category", filter_opt.event_category)
                .addBodyParameter("min_age", filter_opt.min_age + "")
                .addBodyParameter("max_age", filter_opt.max_age + "")
                .addBodyParameter("gender", filter_opt.gender)
                .addBodyParameter("filter_option", filter_opt.filter_option)
                .addBodyParameter("period", filter_opt.period)
                .addBodyParameter("current_time", tslong + "")
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
                                JSONArray users_array = data_obj.getJSONArray("users");
                                JSONArray events_array = data_obj.getJSONArray("events");
                                user_list.clear();
                                for(int i = 0; i < users_array.length(); i++){
                                    UserModel one = new UserModel();
                                    JSONObject user_obj = users_array.getJSONObject(i);
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

                                    JSONArray tag_array = user_obj.getJSONArray("tag_list");
                                    one.tags = new ArrayList<>();
                                    for(int j = 0; j < tag_array.length(); j++){
                                        String sTag = tag_array.getString(j);
                                        one.tags.add(sTag);
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
                                    user_list.add(one);
                                }
                                event_list.clear();
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
                                    event_list.add(other);
                                }
                            }
                            displayUsersAndEvents();
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    public void displayUsersAndEvents(){
        FragmentManager fragManager = getSupportFragmentManager();
        HomeFragment homeFrag = (HomeFragment)fragManager.findFragmentByTag(Const.FRAG_HOME_TAG);
        if(homeFrag != null){
            homeFrag.showUsersAndEvents();
        }
    }

    private void initUpdateMapCenter(){
        String sVal = sharedPrefrence.getString(this, "first_run");
        if(sVal == null){
            FragmentManager fragManager = getSupportFragmentManager();
            HomeFragment homeFrag = (HomeFragment)fragManager.findFragmentByTag(Const.FRAG_HOME_TAG);
            if(homeFrag != null){
                String sLoc = String.format("%f,%f", MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
                if(homeFrag.moveEventLocation(sLoc)){
                    sharedPrefrence.putString(this, "first_run", "0");
                }
            }
        }
    }

    public void choosePhotoFromGallery(){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ) {

                try {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                showGalleryFrag();
            }
        } else {
            showGalleryFrag();
        }
    }

    public void refreshPhotoFrag(){
        FragmentManager fragManager = getSupportFragmentManager();
        if(MyApp.home_type == Const.HOME_HOME){
            EditProfileFragment editFrag = (EditProfileFragment)fragManager.findFragmentByTag(Const.FRAG_PROFILE_TAG);
            if(editFrag != null)editFrag.refreshLayout();
        } else if(MyApp.home_type == Const.HOME_MSG){
            GroupChatFragment chatFrag = (GroupChatFragment)fragManager.findFragmentByTag(Const.FRAG_CHAT_GROUP);
            if(chatFrag != null) chatFrag.uploadPhotos();
        }

    }

    public void refreshHostEventPhoto(){
        FragmentManager fragManager = getSupportFragmentManager();
        HostEventFragment hostEventFrag = (HostEventFragment)fragManager.findFragmentByTag(Const.FRAG_EVENT_TAG);
        hostEventFrag.refreshLayout();
    }

    public void showOneGalleryFrag(){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ) {

                try {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_ONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            } else {
                chooseOnePhotoFrag();
            }
        } else {
            chooseOnePhotoFrag();
        }

    }

    public void showTipsFrag(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        TipsFragment newFragment = new TipsFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.home_frame, newFragment, Const.FRAG_TIPS_TAG).addToBackStack(Const.FRAG_TIPS_TAG).commit();
    }

    public void chooseOnePhotoFrag(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        OneGalleryFragment newFragment = new OneGalleryFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    public void showGalleryFrag(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        GalleryFragment newFragment = new GalleryFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    public void showFilterFrag(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FilterFragment filterFrag = new FilterFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, filterFrag, "FILTER_FRAG").addToBackStack("FILTER_FRAG").commit();
    }

    public void showInviteFriendsFrag(EventModel ev){
        FragmentManager fragmentManager = getSupportFragmentManager();
        InviteFriendsFragment iFriendFrag = new InviteFriendsFragment();
        iFriendFrag.eData = ev;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, iFriendFrag, "INVITE_FRIENDS_FRAG").addToBackStack("INVITE_FRIENDS_FRAG").commit();
    }

    public void showEditProfile(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        EditProfileFragment newFragment = new EditProfileFragment();
        fragmentTransaction.replace(R.id.home_frame, newFragment, Const.FRAG_PROFILE_TAG);
        fragmentTransaction.commit();
    }

    public void showProfile(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ProfileFragment profileFrag = new ProfileFragment();
        //fragmentTransaction.replace(R.id.home_frame, profileFrag, Const.FRAG_PROFILE_TAG);
        //fragmentTransaction.commit();
        fragmentTransaction.add(R.id.home_frame, profileFrag, Const.FRAG_PROFILE_TAG).addToBackStack(Const.FRAG_PROFILE_TAG).commit();
    }

    public void showHostEvent(){
        //if(MyApp.selAddr.length() > 0){
            event_photo = "";
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            HostEventFragment hosteventFrag = new HostEventFragment();
            fragmentTransaction.add(R.id.home_frame, hosteventFrag, Const.FRAG_EVENT_TAG).addToBackStack(Const.FRAG_EVENT_TAG).commit();
       // }
    }

    public void displayPinLocation(){
        FragmentManager fragManager = getSupportFragmentManager();
        HomeFragment homeFrag = (HomeFragment)fragManager.findFragmentByTag(Const.FRAG_HOME_TAG);
        if(homeFrag != null) homeFrag.pinMyLocation();
    }

    public void moveEventLocation(String sLoc){
        FragmentManager fragManager = getSupportFragmentManager();
        HomeFragment homeFrag = (HomeFragment)fragManager.findFragmentByTag(Const.FRAG_HOME_TAG);
        if(homeFrag != null) homeFrag.moveEventLocation(sLoc);
    }

    public void displayView(){
        setNavViewVisible(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        int nMode = MyApp.home_type;
        switch (nMode){
            case 1:
                HostingFragment host_frag = new HostingFragment();
                fragmentTransaction.replace(R.id.home_frame, host_frag, Const.FRAG_MINE_TAG);
                fragmentTransaction.commit();
                break;
            case 2:
                ChatFragment chat_frag = new ChatFragment();
                fragmentTransaction.replace(R.id.home_frame, chat_frag, Const.FRAG_CHAT_TAG);
                fragmentTransaction.commit();
                break;
            case 3:
                AlarmFragment alarm_frag = new AlarmFragment();
                fragmentTransaction.replace(R.id.home_frame, alarm_frag, Const.FRAG_ALARM_TAG);
                fragmentTransaction.commit();
                break;
            default:
                HomeFragment fragment = new HomeFragment();
                fragmentTransaction.replace(R.id.home_frame, fragment, Const.FRAG_HOME_TAG);
                fragmentTransaction.commit();

                break;
        }
    }

    private boolean checkPermission(){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED ) {

                try {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSIONS_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean checkRecordPermission(){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_RECORD_AUDIO);
                return false;
            } else{
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showGalleryFrag();
                } else {
                    Toast.makeText(this, R.string.err_permission, Toast.LENGTH_SHORT).show();
                }
                break;

            case LOCATION_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GPSService mSensorService = new GPSService();
                    Intent intent = new Intent(getApplicationContext(), mSensorService.getClass());
                    if (!isMyServiceRunning(mSensorService.getClass())) {
                        startService(intent);
                    }
                } else {
                    Toast.makeText(this, R.string.err_permission, Toast.LENGTH_SHORT).show();
                }
                break;

            case STORAGE_PERMISSIONS_REQUEST_ONE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseOnePhotoFrag();
                } else {
                    Toast.makeText(this, R.string.err_permission, Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FragmentManager fragManager = getSupportFragmentManager();
                    GroupChatFragment groupFrag = (GroupChatFragment)fragManager.findFragmentByTag(Const.FRAG_CHAT_GROUP);
                    if(groupFrag != null)
                        groupFrag.startRecording();
                } else {
                    Toast.makeText(this, R.string.err_permission, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class GPSTracker extends Service implements LocationListener {

        private final Context mContext;

        // flag for GPS status
        public boolean isGPSEnabled = false;

        // flag for network status
        public boolean isNetworkEnabled = false;

        // flag for GPS status
        boolean canGetLocation = false;

        Location location; // location
        double latitude; // latitude
        double longitude; // longitude

        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 60; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSTracker(Context context) {
            this.mContext = context;
            getLocation();
        }

        public Location getLocation() {
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return location;
            }

            try {
                locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
                // getting GPS status
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                // getting network status
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return location;
        }

        /**
         * Stop using GPS listener
         * Calling this function will stop using GPS in your app
         */
        public void stopUsingGPS() {
            if (locationManager != null) {
                locationManager.removeUpdates(GPSTracker.this);
            }
        }

        /**
         * Function to get latitude
         */
        public double getLatitude() {
            if (location != null) {
                latitude = location.getLatitude();
            }

            // return latitude
            return latitude;
        }

        /**
         * Function to get longitude
         */
        public double getLongitude() {
            if (location != null) {
                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }

        /**
         * Function to check GPS/wifi enabled
         *
         * @return boolean
         */
        public boolean canGetLocation() {
            return this.canGetLocation;
        }

        @Override
        public void onLocationChanged(Location location) {
            this.location = location;
            MyApp.curLoc = location;
            initUpdateMapCenter();
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }
    }

    Double latitude,longitude;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            initUpdateMapCenter();

            latitude = Double.valueOf(intent.getStringExtra("latutide"));
            longitude = Double.valueOf(intent.getStringExtra("longitude"));
            Log.e("Location", "Lat:" + latitude + ", Lng:" + longitude);

            //List<Address> addresses = null;
            //try {
                //addresses = geocoder.getFromLocation(latitude, longitude, 1);
                //String cityName = addresses.get(0).getAddressLine(0);
                //String stateName = addresses.get(0).getAddressLine(1);
                //String countryName = addresses.get(0).getAddressLine(2);

                //String addr = countryName + " , " + stateName;

                SaveSharedPrefrence saveShared = new SaveSharedPrefrence();
                String sToken = saveShared.getString(getApplicationContext(), SaveSharedPrefrence.KEY_USER_TOKEN);
                if(sToken != null){
                    AndroidNetworking.post(Const.HOST_URL + Const.GPS_UPDATE_API)
                            .addHeaders("Authorization", "Bearer " + sToken)
                            //.addBodyParameter("address", addr)
                            .addBodyParameter("lat", latitude + "")
                            .addBodyParameter("lng", longitude + "")
                            .setTag(APP_TAG)
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e("Location Update", "Success");
                                }

                                @Override
                                public void onError(ANError anError) {
                                    Log.e("Location Update", "failed");
                                }
                            });
                }
            //} catch (IOException e1) {
            //    e1.printStackTrace();
            //}
        }
    };

    @SuppressWarnings("deprecation")
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isGPSServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isGPSServiceRunning?", false+"");
        return false;
    }

}
