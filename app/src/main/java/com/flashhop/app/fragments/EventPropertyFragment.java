package com.flashhop.app.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.CardModel;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import me.drakeet.support.toast.ToastCompat;

import static com.flashhop.app.utils.Const.APP_TAG;
import static com.flashhop.app.utils.Const.cover_res;

public class EventPropertyFragment extends DialogFragment {

    public final int STATE_JOINABLE = 0;
    public final int STATE_OUTSIDE_AGE = 1;//full people, outside of age
    public final int STATE_FULL_PEOPLE = 2;
    public final int STATE_JOINED = 3;
    public final int STATE_GENDER_NO = 4;

    Context context;

    View rView;
    LinearLayout ll_button;
    Button btn_invite, btn_join, btn_group, btn_where;
    LinearLayout ll_outside, ll_frame;
    RelativeLayout rl_avatars;
    TextView tv_guide_join, tv_guide_where;
    TextView tv_month, tv_day, tv_weekday, tv_title, tv_time, tv_addr, tv_status, tv_desc;
    ImageView iv_like, iv_share, iv_photo, iv_back;
    CircleImageView[] civ_avatar = new CircleImageView[6];
    boolean bWhere = false, bConfirm = false;
    int mode = STATE_JOINABLE, nMembers = 0;
    int pre_HomeType = Const.HOME_HOME;
    String gender_guide_str = "";
    SaveSharedPrefrence saveSharedPrefrence = new SaveSharedPrefrence();

    public EventModel event = new EventModel();
    MySQLiteHelper mHelper;
    HomeActivity myAct;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rView = inflater.inflate(R.layout.fragment_property_event, container, false);
        context = getContext();

        mHelper = MySQLiteHelper.getInstance(context);
        myAct = (HomeActivity)getActivity();
        pre_HomeType = MyApp.home_type;
        MyApp.home_type = Const.HOME_EVENT;
        initLayout(rView);
        initSetting();
        return rView;
    }

    private void initSetting(){
        mode = STATE_JOINABLE;
        String[] sPeop = event.people.split(",");
        int nMax = Integer.valueOf(sPeop[1]);
        if(nMax <= event.members.size() + 1){
            mode = STATE_FULL_PEOPLE;
        }
        int nAge = TxtUtils.getAge(MyApp.curUser.dob);
        String[] sAge = event.age.split(",");
        int nMin = Integer.valueOf(sAge[0]);
        nMax = Integer.valueOf(sAge[1]);
        if(nAge < nMin || nAge > nMax){
            mode = STATE_OUTSIDE_AGE;
        }

        if(event.gender == 1 && MyApp.curUser.gender.equals("female")){
            mode = STATE_GENDER_NO;
            gender_guide_str = String.format(getString(R.string.event_gender_join), "boys");
        } else if(event.gender == 2 && MyApp.curUser.gender.equals("male")){
            mode = STATE_GENDER_NO;
            gender_guide_str = String.format(getString(R.string.event_gender_join), "girls");
        }

        for(int i = 0; i < event.members.size(); i++){
            UserModel one = event.members.get(i);
            if(one.uid.equals(MyApp.curUser.uid)){
                mode = STATE_JOINED;
                break;
            }
        }

        for(int i = 0; i < event.members.size(); i++){
            UserModel one = event.members.get(i);
            if(one.is_friend == 1){
                nMembers++;
            }
        }
        if(event.creator.is_friend == 1){
            nMembers++;
        }
    }

    private void initLayout(View view){
        iv_back = view.findViewById(R.id.event_prop_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.home_type = pre_HomeType;
                rView.animate()
                        .translationYBy(0)
                        .translationY(rView.getHeight())
                        .alpha(1.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                dismiss();
                                HomeFragment myFragment = (HomeFragment)myAct.getSupportFragmentManager().findFragmentByTag(Const.FRAG_HOME_TAG);
                                if (myFragment != null && myFragment.isVisible()) {
                                    myAct.filterDataFromServer();
                                }
                            }
                        });
            }
        });
        tv_guide_join = view.findViewById(R.id.event_prop_frag_tv_guide_join);
        tv_guide_where = view.findViewById(R.id.event_prop_frag_tv_guide_where);
        ll_button = view.findViewById(R.id.event_prop_frag_ll_button);
        btn_invite = view.findViewById(R.id.event_prop_frag_btn_invite);
        btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tv_guide_where.getVisibility() == View.VISIBLE){
                    tv_guide_where.setVisibility(View.GONE);
                }
                ((HomeActivity)getActivity()).showInviteFriendsFrag(event);
            }
        });
        btn_join = view.findViewById(R.id.event_prop_frag_btn_edit);
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tv_guide_where.getVisibility() == View.VISIBLE){
                    tv_guide_where.setVisibility(View.GONE);
                }
                if(mode == STATE_JOINABLE){
                    if(event.price.equals("0")){
                        joinEvent();
                    } else if(event.is_pay_later == 1){
                        showPayComfirmDlg();
                    } else{
                        showPaymentDlg();
                    }
                } else if(mode == STATE_JOINED){
                    showJoiningDlg();
                } else if (mode == STATE_FULL_PEOPLE){
                    if(tv_guide_join.getVisibility() == View.GONE){
                        tv_guide_join.setVisibility(View.VISIBLE);
                        tv_guide_join.setText(R.string.event_guide_full);
                    } else{
                        tv_guide_join.setVisibility(View.GONE);
                    }
                } else if(mode == STATE_OUTSIDE_AGE){
                    if(tv_guide_join.getVisibility() == View.GONE){
                        tv_guide_join.setVisibility(View.VISIBLE);
                        tv_guide_join.setText(R.string.event_guide_join);
                    } else{
                        tv_guide_join.setVisibility(View.GONE);
                    }
                } else if(mode == STATE_GENDER_NO){
                    if(tv_guide_join.getVisibility() == View.GONE){
                        tv_guide_join.setVisibility(View.VISIBLE);
                        tv_guide_join.setText(gender_guide_str);
                    } else{
                        tv_guide_join.setVisibility(View.GONE);
                    }
                }

            }
        });
        btn_group = view.findViewById(R.id.event_prop_frag_btn_groupchat);
        btn_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tv_guide_where.getVisibility() == View.VISIBLE){
                    tv_guide_where.setVisibility(View.GONE);
                }
                gotoGroupChat(event);
            }
        });
        btn_where = view.findViewById(R.id.event_prop_frag_btn_where);
        btn_where.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bWhere){
                    showHoppersMap();
                } else{
                    if(tv_guide_where.getVisibility() == View.VISIBLE){
                        tv_guide_where.setVisibility(View.GONE);
                    } else{
                        tv_guide_where.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        ll_frame = view.findViewById(R.id.event_prop_frag_frame);
        ll_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tv_guide_where.getVisibility() == View.VISIBLE){
                    tv_guide_where.setVisibility(View.GONE);
                }
                if(tv_guide_join.getVisibility() == View.VISIBLE){
                    tv_guide_join.setVisibility(View.GONE);
                }
            }
        });

        tv_month = view.findViewById(R.id.event_prop_frag_tv_month);
        tv_day = view.findViewById(R.id.event_prop_frag_tv_day);
        tv_weekday = view.findViewById(R.id.event_prop_frag_tv_weekday);
        tv_time = view.findViewById(R.id.event_prop_frag_tv_time);
        tv_title = view.findViewById(R.id.event_prop_frag_tv_title);
        tv_addr = view.findViewById(R.id.event_prop_frag_tv_address);
        tv_status = view.findViewById(R.id.event_prop_frag_tv_status);
        tv_desc = view.findViewById(R.id.event_prop_frag_tv_description);
        iv_like = view.findViewById(R.id.event_prop_frag_iv_like);
        iv_share = view.findViewById(R.id.event_prop_frag_iv_share);
        iv_photo = view.findViewById(R.id.event_prop_frag_iv_photo);

        tv_addr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.home_type = Const.HOME_HOME;
                myAct.displayView();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(myAct != null && event.loc != null && event.loc.length() > 0)
                            myAct.moveEventLocation(event.loc);
                    }
                }, 1200);
            }
        });

        iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(event.nLike == 1) {
                    likeEvent(-1);
                } else{
                    likeEvent(1);
                }
            }
        });

        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareEvent();
            }
        });

        civ_avatar[0] = view.findViewById(R.id.event_prop_frag_civ_avatar0);
        civ_avatar[1] = view.findViewById(R.id.event_prop_frag_civ_avatar1);
        civ_avatar[2] = view.findViewById(R.id.event_prop_frag_civ_avatar2);
        civ_avatar[3] = view.findViewById(R.id.event_prop_frag_civ_avatar3);
        civ_avatar[4] = view.findViewById(R.id.event_prop_frag_civ_avatar4);
        civ_avatar[5] = view.findViewById(R.id.event_prop_frag_civ_avatar5);

        rl_avatars = rView.findViewById(R.id.event_prop_frag_rl_avatars);
        rl_avatars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = myAct.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
                GroupInfoFragment group_frag = new GroupInfoFragment();
                group_frag.group_event = event;
                fragmentTransaction.add(android.R.id.content, group_frag, Const.FRAG_GROUP_INFO).addToBackStack(Const.FRAG_GROUP_INFO).commit();
            }
        });
    }

    public void refreshLayout(){
        long diff = TxtUtils.getDifferenceTime(event.date, event.time);
        if(diff <= 30){
            bWhere = true;
        } else{
            bWhere = false;
        }

        String sGuide = saveSharedPrefrence.getString(context, SaveSharedPrefrence.KEY_GUIDE);
        if(mode == STATE_OUTSIDE_AGE){
            btn_join.setVisibility(View.VISIBLE);
            btn_join.setBackgroundResource(R.drawable.border_round_gray);
            btn_join.setText(R.string.btn_join);
            btn_join.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            ll_button.setVisibility(View.GONE);
            btn_invite.setVisibility(View.GONE);
        } else if(mode == STATE_GENDER_NO){
            btn_join.setVisibility(View.VISIBLE);
            btn_join.setBackgroundResource(R.drawable.border_round_gray);
            btn_join.setText(R.string.btn_join);
            btn_join.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            ll_button.setVisibility(View.GONE);
            btn_invite.setVisibility(View.GONE);
        } else if(mode == STATE_FULL_PEOPLE){
            btn_join.setVisibility(View.VISIBLE);
            btn_join.setBackgroundResource(R.drawable.border_round_gray);
            btn_join.setText(R.string.btn_full);
            btn_join.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            ll_button.setVisibility(View.GONE);
            btn_invite.setVisibility(View.GONE);
        } else if(mode == STATE_JOINABLE){
            ll_button.setVisibility(View.GONE);
            btn_invite.setVisibility(View.GONE);
            btn_join.setBackgroundResource(R.drawable.border_round_yellow);
            btn_join.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
            if(event.price.equals("0") || event.is_pay_later == 1){
                btn_join.setText(R.string.btn_join);
            } else{
                btn_join.setText(R.string.btn_pay_join);
            }
        } else{
            ll_button.setVisibility(View.VISIBLE);
            btn_join.setBackgroundResource(R.drawable.border_round_dark);
            btn_join.setText(R.string.btn_joining);
            btn_join.setTextColor(ContextCompat.getColor(context, R.color.colorYellow));

            if(bWhere){
                btn_where.setBackgroundResource(R.drawable.border_round_yellow);
                btn_where.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
            } else{
                btn_where.setBackgroundResource(R.drawable.border_round_gray);
                btn_where.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            }

            //invite button
            btn_invite.setVisibility(View.VISIBLE);
            if(event.invitaion == 1){
                btn_invite.setVisibility(View.GONE);
            }
        }
        //set values
        if(event.photo.length() > 0){
            Picasso.get()
                    .load(event.photo)
                    .into(iv_photo);
        } else{
            for(int i = 0; i < 9; i++){
                if(Const.interest_list[i].toLowerCase().equals(event.category.toLowerCase())){
                    iv_photo.setImageResource(Const.cover_res[i+1]);
                    break;
                }
            }

        }
        try{
            SimpleDateFormat sDf = new SimpleDateFormat("MMM dd yyyy");
            Date dt = sDf.parse(event.date);
            SimpleDateFormat eDf = new SimpleDateFormat("EEE");
            String weekday = eDf.format(dt);
            tv_weekday.setText(weekday);
        } catch (ParseException ex){
            ex.printStackTrace();
        }

        String[] sDate = event.date.split(" ");
        tv_month.setText(sDate[0]);
        tv_day.setText(sDate[1]);
        tv_title.setText(event.title);
        tv_time.setText(event.time.toUpperCase());
        tv_addr.setText(event.address);
        String[] sPeop = event.people.split(",");
        String sStatus = String.format("Min %s, Max %s, %d are going, %d friends", sPeop[0], sPeop[1], event.members.size() + 1, nMembers);
        tv_status.setText(sStatus);
        tv_desc.setText(event.desc);

        if(event.creator != null){
            civ_avatar[0].setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(event.creator.photo_url)
                    .into(civ_avatar[0]);
            for(int p = 1; p < 6; p++){
                civ_avatar[p].setVisibility(View.GONE);
            }
            int nSize = event.members.size() > 5 ? 5 : event.members.size();
            for(int k = 0; k < nSize; k++){
                civ_avatar[k+1].setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(event.members.get(k).photo_url)
                        .into(civ_avatar[k+1]);
            }
        }

        if(event.nLike == 1){
            iv_like.setImageResource(R.drawable.ic_liked);
        } else{
            iv_like.setImageResource(R.drawable.ic_like);
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
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void showJoiningDlg(){
        final Button btn_cancel, btn_not;
        RelativeLayout rl_bg;
        final Dialog dialog = new Dialog(context, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_choose_join_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        btn_cancel  = dialog.findViewById(R.id.custom_join_btn_cancel);
        btn_not = dialog.findViewById(R.id.custom_join_btn_not);
        rl_bg = dialog.findViewById(R.id.custom_join_rl_bg);

        dialog.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_not.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveEvent();
                dialog.dismiss();
            }
        });

        rl_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void joinEvent(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Joining...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.JOIN_EVENT_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("event_id", event.id)
                .addBodyParameter("is_invited", "1")
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
                                event.members.clear();
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
                                    event.members.add(one);
                                }

                                initSetting();
                                refreshLayout();
                            } else{
                                WindowUtils.animateView(getContext(), btn_join);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), btn_join);
                    }
                });
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
                .addBodyParameter("event_id", event.id)
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
                                event.members.clear();
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
                                    event.members.add(one);
                                }

                                initSetting();
                                refreshLayout();
                            } else{
                                WindowUtils.animateView(context, btn_join);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(context, btn_join);
                    }
                });
    }

    private void payToEvent(CardModel card){
        String card_num = card.card_num.replace(" ", "");
        String[] dd = card.expire.split("/");
        String sExpMonth = Integer.parseInt(dd[0]) + "";
        String sExpYear = "20" + card.expire.substring(card.expire.length() - 2);
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Paying...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.PAY_WITH_STRIPE)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("card_number", card_num)
                .addBodyParameter("exp_month", sExpMonth)
                .addBodyParameter("exp_year", sExpYear)
                .addBodyParameter("cvc", card.cvv)
                .addBodyParameter("amount", event.price)
                .addBodyParameter("currency", event.currency.toLowerCase())
                .addBodyParameter("action", "join_event")
                .addBodyParameter("event_id", event.id)
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
                                joinEvent();
                            } else{
                                WindowUtils.animateView(context, btn_join);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(context, btn_join);
                        //ToastCompat.makeText(context, anError.getErrorBody(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPayComfirmDlg(){
        TextView tv_price, tv_currency;
        Button btn_cancel, btn_continue;
        final Dialog backDlg = new Dialog(context, R.style.FullHeightDialog);
        backDlg.setContentView(R.layout.custom_pay_confirm_dlg);
        View dview = backDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_price = backDlg.findViewById(R.id.custom_pay_tv_price);
        tv_currency = backDlg.findViewById(R.id.custom_pay_tv_currency);
        btn_cancel = backDlg.findViewById(R.id.custom_pay_btn_cancel);
        btn_continue = backDlg.findViewById(R.id.custom_pay_btn_continue);

        float fprice = Float.parseFloat(event.price);
        String sPrice = String.format("$%.2f", fprice);
        tv_price.setText(sPrice);
        tv_currency.setText(event.currency);
        backDlg.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                backDlg.dismiss();
                joinEvent();
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backDlg.dismiss();
                showPaymentDlg();
            }
        });
    }

    CardModel cData;

    private void showPaymentDlg(){
        final Button btn_cancel_d, btn_next_d;
        final CheckBox cb_save_d;
        final ImageView iv_photo_d;
        final TextView tv_title_d, tv_cardnum_d, tv_expire_d, tv_cvv_d, tv_month_d, tv_day_d, tv_weekday_d, tv_eventtitle_d, tv_time_d, tv_amount_d, tv_holder_d;
        final EditText et_cardnum_d, et_holder_d, et_expire_d, et_cvv_d, et_addr_no_d, et_addr_name_d, et_postcode_d, et_province_d, et_city_d;
        final TextView tv_num_def, tv_holder_def, tv_exp_def, tv_cvv_def;
        final LinearLayout ll_event_d, ll_card_d, ll_info_d, ll_card_def;
        final Dialog pdialog = new Dialog(context, R.style.FullHeightDialog);
        pdialog.setContentView(R.layout.payment_detail_dlg);
        View dview = pdialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        btn_cancel_d  = pdialog.findViewById(R.id.payment_detail_btn_cancel);
        btn_next_d = pdialog.findViewById(R.id.payment_detail_btn_next);
        iv_photo_d = pdialog.findViewById(R.id.payment_detail_iv_image);
        tv_title_d = pdialog.findViewById(R.id.payment_detail_tv_title);
        tv_cardnum_d = pdialog.findViewById(R.id.payment_detail_tv_number);
        tv_holder_d = pdialog.findViewById(R.id.payment_detail_tv_holder);
        tv_expire_d = pdialog.findViewById(R.id.payment_detail_tv_expire);
        tv_cvv_d = pdialog.findViewById(R.id.payment_detail_tv_cvv);
        tv_month_d = pdialog.findViewById(R.id.payment_detail_tv_month);
        tv_day_d = pdialog.findViewById(R.id.payment_detail_tv_day);
        tv_weekday_d = pdialog.findViewById(R.id.payment_detail_tv_weekday);
        tv_eventtitle_d = pdialog.findViewById(R.id.payment_detail_tv_event_title);
        tv_time_d = pdialog.findViewById(R.id.payment_detail_tv_time);
        tv_amount_d = pdialog.findViewById(R.id.payment_detail_tv_amount);
        et_cardnum_d = pdialog.findViewById(R.id.payment_detail_et_number);
        et_holder_d = pdialog.findViewById(R.id.payment_detail_et_name);
        et_expire_d = pdialog.findViewById(R.id.payment_detail_et_expire);
        et_cvv_d = pdialog.findViewById(R.id.payment_detail_et_cvv);
        et_addr_no_d = pdialog.findViewById(R.id.payment_detail_et_street_no);
        et_addr_name_d = pdialog.findViewById(R.id.payment_detail_et_street_name);
        et_province_d = pdialog.findViewById(R.id.payment_detail_et_province);
        et_city_d = pdialog.findViewById(R.id.payment_detail_et_city);
        et_postcode_d = pdialog.findViewById(R.id.payment_detail_et_postcode);
        ll_event_d = pdialog.findViewById(R.id.payment_detail_ll_event);
        ll_card_d = pdialog.findViewById(R.id.payment_detail_ll_card);
        ll_info_d = pdialog.findViewById(R.id.payment_detail_ll_info);
        cb_save_d = pdialog.findViewById(R.id.payment_detail_cb_save);
        tv_num_def = pdialog.findViewById(R.id.payment_detail_tv_number_def);
        tv_exp_def = pdialog.findViewById(R.id.payment_detail_tv_expire_def);
        tv_holder_def = pdialog.findViewById(R.id.payment_detail_tv_name_def);
        tv_cvv_def = pdialog.findViewById(R.id.payment_detail_tv_cvv_def);
        ll_card_def = pdialog.findViewById(R.id.payment_detail_ll_confirm_card);

        String[] sDate = event.date.split(" ");
        tv_month_d.setText(sDate[0]);
        tv_day_d.setText(sDate[1]);
        tv_eventtitle_d.setText(event.title);
        tv_time_d.setText(event.time.toUpperCase());
        tv_amount_d.setText("$" + event.price + " " + event.currency);
        if(event.photo.length() > 0){
            Picasso.get()
                    .load(event.photo)
                    .into(iv_photo_d);
        } else{
            for(int i = 0; i < 9; i++){
                if(Const.interest_list[i].toLowerCase().equals(event.category.toLowerCase())){
                    iv_photo_d.setImageResource(Const.cover_res[i+1]);
                    break;
                }
            }

        }
        try{
            SimpleDateFormat sDf = new SimpleDateFormat("MMM dd yyyy");
            Date dt = sDf.parse(event.date);
            SimpleDateFormat eDf = new SimpleDateFormat("EEE");
            String weekday = eDf.format(dt);
            tv_weekday_d.setText(weekday);
        } catch (ParseException ex){
            ex.printStackTrace();
        }

        cData = mHelper.getCardInfo();
        if(cData.card_num != null && cData.card_num.length() > 0){
            String sNum = cData.card_num.substring(cData.card_num.length() - 4);
            et_cardnum_d.setText("xxxx xxxx xxxx " + sNum);
            et_holder_d.setText(cData.holder_name);
            String ex_str = cData.expire.substring(0, 1) + "/" + cData.expire.substring(cData.expire.length() - 2);
            et_expire_d.setText(ex_str);
            et_cvv_d.setText(cData.cvv);
            //et_addr_no_d.setText(cData.street_no);
            //et_addr_name_d.setText(cData.street_name);
            //et_city_d.setText(cData.city);
            //et_province_d.setText(cData.province);
            //et_postcode_d.setText(cData.post_code);
        }

        bConfirm = false;
        btn_cancel_d.setVisibility(View.GONE);
        ll_card_d.setVisibility(View.VISIBLE);
        cb_save_d.setText("  " + getString(R.string.payment_save_card));
        pdialog.show();

        btn_cancel_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdialog.dismiss();
            }
        });

        btn_next_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mode == STATE_JOINABLE && !event.price.equals("0") && !bConfirm){
                    String sNum = et_cardnum_d.getText().toString().trim();
                    boolean bErr = false;
                    if(sNum.length() != 19){
                        et_cardnum_d.setText("");
                        bErr = true;
                    }
                    if(et_holder_d.getText().toString().trim().length() == 0){
                        et_holder_d.setText("");
                        bErr = true;
                    }
                    String sExp = et_expire_d.getText().toString().trim();
                    if(sExp.length() < 4 || !TxtUtils.isValidExpireDate(sExp)){
                        et_expire_d.setText("");
                        bErr = true;
                    }
                    String sCVV = et_cvv_d.getText().toString().trim();
                    /*if(sCVV.length() != 3){
                        et_cvv_d.setText("");
                        bErr = true;
                    }
                    if(et_addr_no_d.getText().toString().trim().length() == 0){
                        et_addr_no_d.setText("");
                        bErr = true;
                    }
                    if(et_addr_name_d.getText().toString().trim().length() == 0){
                        et_addr_name_d.setText("");
                        bErr = true;
                    }
                    if(et_city_d.getText().toString().trim().length() == 0){
                        et_city_d.setText("");
                        bErr = true;
                    }
                    if(et_province_d.getText().toString().trim().length() == 0){
                        et_province_d.setText("");
                        bErr = true;
                    }
                    if(et_postcode_d.getText().toString().trim().length() == 0){
                        et_postcode_d.setText("");
                        bErr = true;
                    }*/

                    if(!bErr){
                        btn_cancel_d.setVisibility(View.VISIBLE);
                        ll_info_d.setVisibility(View.GONE);
                        ll_card_d.setVisibility(View.GONE);
                        ll_card_def.setVisibility(View.VISIBLE);
                        tv_title_d.setVisibility(View.GONE);
                        ll_event_d.setVisibility(View.VISIBLE);
                        btn_next_d.setText(R.string.btn_confirm);
                        String ssNum = et_cardnum_d.getText().toString().trim().substring(et_cardnum_d.getText().toString().trim().length() - 4);
                        tv_num_def.setText("xxxx xxxx xxxx " + ssNum);
                        tv_holder_def.setText(et_holder_d.getText().toString().trim());
                        tv_exp_def.setText(et_expire_d.getText().toString().trim());
                        if(cb_save_d.isChecked()){
                            if(!sNum.contains("xxxx")){
                                cData.card_num = sNum;
                            }
                            cData.holder_name = et_holder_d.getText().toString().trim();
                            cData.expire = sExp;
                            cData.cvv = sCVV;
                            //cModel.street_no = et_addr_no_d.getText().toString().trim();
                            //cModel.street_name = et_addr_name_d.getText().toString().trim();
                            //cModel.city = et_city_d.getText().toString().trim();
                            //cModel.province = et_province_d.getText().toString().trim();
                            //cModel.post_code = et_postcode_d.getText().toString().trim();
                            mHelper.putCardInfo(cData);
                        }
                        bConfirm = true;
                    }
                } else if(bConfirm && mode == STATE_JOINABLE){
                    payToEvent(cData);
                    pdialog.dismiss();
                } else{
                    pdialog.dismiss();
                }
            }
        });

        et_cardnum_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_cardnum_d.getText().toString().trim();
                int textlength = text.length();

                if(textlength == 0){
                    tv_cardnum_d.setText(R.string.err_card_number);
                    tv_cardnum_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_cardnum_d.setText(R.string.payment_card_number);
                    tv_cardnum_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }

                if(text.endsWith(" "))
                    return;

                if(textlength == 5 || textlength == 10 || textlength == 15)
                {
                    et_cardnum_d.setText(new StringBuilder(text).insert(text.length()-1, " ").toString());
                    et_cardnum_d.setSelection(et_cardnum_d.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_holder_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int textlength = et_holder_d.getText().toString().trim().length();
                if(textlength == 0){
                    tv_holder_d.setText("*Cardholder Name");
                    tv_holder_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_holder_d.setText(R.string.payment_cardholder);
                    tv_holder_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_expire_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_expire_d.getText().toString().trim();
                int textlength = text.length();

                if(textlength == 0){
                    tv_expire_d.setText(R.string.err_expire);
                    tv_expire_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_expire_d.setText(R.string.payment_expiration);
                    tv_expire_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }

                if(text.endsWith("/"))
                    return;

                if(textlength == 3)
                {
                    et_expire_d.setText(new StringBuilder(text).insert(text.length()-1, "/").toString());
                    et_expire_d.setSelection(et_expire_d.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_cvv_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*int textlength = et_cvv_d.getText().toString().trim().length();
                if(textlength == 0){
                    tv_cvv_d.setText(R.string.err_cvv);
                    tv_cvv_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_cvv_d.setText(R.string.payment_cvv);
                    tv_cvv_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }*/
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        /*et_addr_no_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_addr_no_d.getText().toString().trim().length() == 0){
                    et_addr_no_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_addr_name_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_addr_name_d.getText().toString().trim().length() == 0){
                    et_addr_name_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_city_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_city_d.getText().toString().trim().length() == 0){
                    et_city_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_province_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_province_d.getText().toString().trim().length() == 0){
                    et_province_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_postcode_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_postcode_d.getText().toString().trim().length() == 0){
                    et_postcode_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/

    }

    private void likeEvent(final int nlike){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Liking...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.EVENT_LIKE_DISLIKE)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("event_id", event.id)
                .addBodyParameter("is_liked", nlike + "")
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
                                event.nLike = nlike;
                                refreshLayout();
                            } else{
                                WindowUtils.animateView(getContext(), iv_like);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), iv_like);
                    }
                });
    }

    private void showHoppersMap(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        HoppersFragment hoppersFrag = new HoppersFragment();
        hoppersFrag.ev = event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_in, R.anim.ani_fade_out);
        transaction.replace(R.id.home_frame, hoppersFrag, Const.FRAG_HOPPERS_TAG).addToBackStack(Const.FRAG_HOPPERS_TAG).commit();
    }

    private void gotoGroupChat(EventModel ev){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
        GroupChatFragment group_frag = new GroupChatFragment();
        group_frag.group_event = ev;
        fragmentTransaction.add(R.id.home_frame, group_frag, Const.FRAG_CHAT_GROUP).addToBackStack(Const.FRAG_CHAT_GROUP).commit();
    }

    private void shareEvent(){
        Bitmap bitmap= ((BitmapDrawable)iv_photo.getDrawable()).getBitmap();
        File folder = new File(Const.PHOTO_DIR + "/");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if(!success){
            return;
        }
        OutputStream out = null;
        File file = new File(Const.PHOTO_DIR,"Share.png");
        if (file.exists()) {
            file.delete();
            file = new File(Const.PHOTO_DIR, "Share.png");
        }
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Uri bmpUri = Uri.fromFile(file);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("*/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, event.title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, event.desc);
        startActivity(Intent.createChooser(sharingIntent, null));
    }
}
