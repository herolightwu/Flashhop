package com.flashhop.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.KeyboardUtil;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.utils.Const.APP_TAG;

public class UserProfileFragment extends Fragment {

    Context context;

    ImageView iv_setting, iv_bag;
    CircleImageView civ_avatar;
    TextView tv_what, tv_name, tv_facts, tv_type, tv_add_friend, tv_unfriend, tv_report, tv_like, tv_addTag, tv_what_guide;
    TextView tv_age, tv_event, tv_hangout, tv_friend_guide, tv_tag_guide;
    TextView[] tv_tag = new TextView[3];
    Button btn_invite;// btn_tag, btn_tip;
    LinearLayout ll_outside, ll_guide, ll_menu, ll_organized;// ll_tip_tag;
    LinearLayout[] ll_list = new LinearLayout[7];
    ImageView[] iv_list = new ImageView[9];
    List<String> tag_list = new ArrayList<>();
    List<String> insert_list;
    boolean bfriend = false;
    int nFriends = 0;
    int pre_HomeType = Const.HOME_HOME;

    //SaveSharedPrefrence saveSharedPrefrence = new SaveSharedPrefrence();

    public UserModel other = new UserModel();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        context = getContext();
        //pre_HomeType = MyApp.home_type;
        //MyApp.home_type = Const.HOME_PROFILE;

        initLayout(rView);
        return rView;
    }

    private void initLayout(View view){
        ll_outside = view.findViewById(R.id.user_frag_ll_outside);
        ll_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ll_guide.getVisibility() == View.VISIBLE){
                    ll_guide.setVisibility(View.GONE);
                }
                KeyboardUtil.hideKeyboard(getActivity());
            }
        });
        ll_guide = view.findViewById(R.id.user_frag_ll_add_guide);
        ll_guide.setVisibility(View.GONE);
        tv_friend_guide = view.findViewById(R.id.user_frag_tv_friend_guide);
        ll_list[0] = view.findViewById(R.id.user_frag_ll_photo);
        civ_avatar = view.findViewById(R.id.user_frag_iv_avatar);
        tv_name = view.findViewById(R.id.user_frag_tv_name);

        tv_what = view.findViewById(R.id.user_frag_tv_what);
        tv_what_guide = view.findViewById(R.id.user_frag_tv_guide_what);
        tv_what.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tv_what_guide.getVisibility() == View.VISIBLE){
                    tv_what_guide.setVisibility(View.GONE);
                } else{
                    tv_what_guide.setVisibility(View.VISIBLE);
                }
            }
        });

        tv_what_guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uriUrl = Uri.parse(Const.WHAT_IS_LINK);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });
        tv_facts = view.findViewById(R.id.user_frag_tv_facts);
        tv_type = view.findViewById(R.id.user_frag_tv_type);
        tv_add_friend = view.findViewById(R.id.user_frag_tv_add_friend);
        tv_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(other.is_friend == 0 && other.is_friendable != 0){
                    //send request friend
                    sendRequestFriend();
                } else if(!bfriend){
                    if(ll_guide.getVisibility() == View.GONE){
                        ll_guide.setVisibility(View.VISIBLE);
                    } else{
                        ll_guide.setVisibility(View.GONE);
                    }
                }
            }
        });

        btn_invite = view.findViewById(R.id.user_frag_btn_invite);
        btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_guide.setVisibility(View.GONE);
                showInviteEventFrag();
            }
        });

        ll_menu = view.findViewById(R.id.user_frag_ll_menu);
        tv_unfriend = view.findViewById(R.id.user_frag_tv_unfriend);
        tv_unfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //request unfriend
                sendRequestUnfriend();

            }
        });
        tv_report = view.findViewById(R.id.user_frag_tv_report);
        tv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_menu.setVisibility(View.GONE);
                showReportDlg();
            }
        });

        iv_setting = view.findViewById(R.id.user_frag_iv_more);
        iv_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bfriend){
                    ll_menu.setVisibility(View.VISIBLE);
                }
            }
        });
        tv_like = view.findViewById(R.id.user_frag_tv_like_question);
        tv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(other.is_friendable != 0)
                    showQuestionDlg();
            }
        });

        tv_addTag = view.findViewById(R.id.user_frag_tv_tag_him);
        tv_addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(other.is_friendable != 0){
                    chooseTag();
                } else{
                    if(tv_tag_guide.getVisibility() == View.VISIBLE){
                        tv_tag_guide.setVisibility(View.GONE);
                    } else{
                        tv_tag_guide.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        tv_tag[0] = view.findViewById(R.id.user_frag_tv_tag0);
        tv_tag[1] = view.findViewById(R.id.user_frag_tv_tag1);
        tv_tag[2] = view.findViewById(R.id.user_frag_tv_tag2);
        tv_age = view.findViewById(R.id.user_frag_tv_age);
        tv_event = view.findViewById(R.id.user_frag_tv_organized);
        tv_hangout = view.findViewById(R.id.user_frag_tv_hangouts);
        ll_organized = view.findViewById(R.id.user_frag_ll_organized);

        if(other != null){
            Picasso.get()
                    .load(other.photo_url)
                    .resize(100, 100)
                    .into(civ_avatar);
        }

        ll_list[1] = view.findViewById(R.id.user_frag_ll_photo0);
        ll_list[2] = view.findViewById(R.id.user_frag_ll_photo1);
        ll_list[3] = view.findViewById(R.id.user_frag_ll_photo2);
        ll_list[4] = view.findViewById(R.id.user_frag_ll_photo3);
        ll_list[5] = view.findViewById(R.id.user_frag_ll_photo4);
        ll_list[6] = view.findViewById(R.id.user_frag_ll_photo5);

        iv_list[0] = view.findViewById(R.id.user_frag_iv_photo0);
        iv_list[1] = view.findViewById(R.id.user_frag_iv_photo1);
        iv_list[2] = view.findViewById(R.id.user_frag_iv_photo2);
        iv_list[3] = view.findViewById(R.id.user_frag_iv_photo3);
        iv_list[4] = view.findViewById(R.id.user_frag_iv_photo4);
        iv_list[5] = view.findViewById(R.id.user_frag_iv_photo5);
        iv_list[6] = view.findViewById(R.id.user_frag_iv_photo6);
        iv_list[7] = view.findViewById(R.id.user_frag_iv_photo7);
        iv_list[8] = view.findViewById(R.id.user_frag_iv_photo8);

        tv_tag_guide = view.findViewById(R.id.user_frag_tv_guide_tags);

        /*ll_tip_tag = view.findViewById(R.id.user_frag_ll_tag_tip);
        btn_tag = view.findViewById(R.id.user_frag_btn_tag);
        btn_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseTag();
            }
        });
        btn_tip = view.findViewById(R.id.user_frag_btn_tip);
        btn_tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGiveTipDlg();
            }
        });*/

        iv_bag = view.findViewById(R.id.user_frag_iv_bag);
        iv_bag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGiveTipDlg();
            }
        });

        loadTags();
    }

    public void refreshLayout(){
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowUtils.showImagesLikeInsta(getContext(), ll_list, iv_list, other.images, dm.widthPixels);
        ll_menu.setVisibility(View.GONE);
        if(other.is_friend == 1){
            bfriend = true;
            String sFriend = "";
            if(nFriends > 0) sFriend = nFriends + "";
            tv_add_friend.setText(sFriend + " " + getString(R.string.profile_friends));
            iv_setting.setVisibility(View.VISIBLE);
        } else if(other.is_friend == 0){
            bfriend = false;
            tv_add_friend.setText(R.string.profile_add_friend);
            iv_setting.setVisibility(View.GONE);
        } else{
            bfriend = false;
            tv_add_friend.setText(R.string.profile_requesting_friend);
            iv_setting.setVisibility(View.GONE);
        }

        String like_str = String.format(getString(R.string.profile_like_question), other.first_name);
        tv_like.setText(like_str);
        if(bfriend){
            tv_like.setVisibility(View.VISIBLE);
        } else{
            tv_like.setVisibility(View.GONE);
        }

        String friend_guide = String.format(getString(R.string.profile_add_guide), other.first_name);
        tv_friend_guide.setText(friend_guide);

        tv_name.setText(other.first_name + " " + other.last_name);
        if(other.hide_my_age == 1){
            tv_age.setText("Secret");
        } else{
            int nAge = TxtUtils.getAge(other.dob);
            tv_age.setText(nAge + "");
        }

        if(other.event_count == 0){
            ll_organized.setVisibility(View.GONE);
        } else {
            ll_organized.setVisibility(View.VISIBLE);
            tv_event.setText(other.event_count + " events");
        }
        tv_tag[0].setVisibility(View.GONE);
        tv_tag[1].setVisibility(View.GONE);
        tv_tag[2].setVisibility(View.GONE);
        int nTag = other.tags.size() > 3 ? 3 : other.tags.size();
        for(int i = 0; i < nTag; i++){
            tv_tag[i].setVisibility(View.VISIBLE);
            tv_tag[i].setText(other.tags.get(i));
        }
        tv_facts.setText(other.facts);

        /*if(other.nTip > 0){
            iv_bag.setImageResource(R.drawable.ic_bag_y);
        } else{
            iv_bag.setImageResource(R.drawable.ic_bag);
        }

        if(other.is_friendable != 0){
            ll_tip_tag.setVisibility(View.VISIBLE);
            btn_invite.setVisibility(View.GONE);
        } else{
            ll_tip_tag.setVisibility(View.GONE);
            btn_invite.setVisibility(View.VISIBLE);
        }

        btn_tip.setText(getText(R.string.btn_tip_user) + " " + other.first_name);
        btn_tag.setText(getText(R.string.btn_tag_user) + " " + other.first_name);*/
    }

    private void showReportDlg(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        ReportDlgFrag reportFrag = new ReportDlgFrag();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, reportFrag, "REPORT_FRAG").addToBackStack("REPORT_FRAG").commit();
    }

    private void showGiveTipDlg(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        GiveTipFrag givetipFrag = new GiveTipFrag();
        givetipFrag.other = other;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.home_frame, givetipFrag, "GIVE_TIP_FRAG").addToBackStack("GIVE_TIP_FRAG").commit();
    }

    private void sendRequestUnfriend(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Adding Friend...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.REMOVE_MY_FRIEND)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("friend_id", other.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                ll_menu.setVisibility(View.GONE);
                                tv_add_friend.setText(R.string.profile_add_friend);
                                other.is_friend = 0;
                                refreshLayout();
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

    private void sendRequestFriend(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Adding Friend...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.ADD_MY_FRIEND)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("friend_id", other.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                JSONObject data_obj = response.getJSONObject("data");
                                JSONArray friend_array = data_obj.getJSONArray("count_related_friends");
                                nFriends = friend_array.length();
                                tv_add_friend.setText(nFriends + " " + getString(R.string.profile_friends));
                                other.is_friend = 2;
                                refreshLayout();
                            } else{
                                WindowUtils.animateView(getContext(), tv_add_friend);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), tv_add_friend);
                    }
                });
    }

    private void loadTags(){
        tag_list.clear();
        for(int i = 0; i < Const.tag_list.length; i++){
            tag_list.add(Const.tag_list[i]);
        }
        AndroidNetworking.get(Const.HOST_URL + Const.TAGS_URL)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                JSONArray data = response.getJSONArray("data");
                                if(data.length() > 0){
                                    tag_list.clear();
                                    for(int i = 0; i < data.length(); i++){
                                        JSONObject obj = data.getJSONObject(i);
                                        tag_list.add(obj.getString("tag_name"));
                                    }
                                }
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

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void chooseTag(){
        Button btn_done, btn_cancel;
        final AppCompatSpinner sp_dur;
        final Dialog dialog = new Dialog(context, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_add_tag_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        sp_dur = dialog.findViewById(R.id.custom_duration_sp_duration);
        btn_done = dialog.findViewById(R.id.custom_duration_btn_done);
        btn_cancel = dialog.findViewById(R.id.custom_duration_btn_cancel);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(sp_dur);
            // Set popupWindow height to 500px
            popupWindow.setHeight(300);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        /*insert_list = new ArrayList<>();
        boolean bFound;
        for(int i = 0; i < tag_list.size(); i++){
            bFound = false;
            for(int j = 0; j < other.tags.size(); j++){
                if(tag_list.get(i).toLowerCase().equals(other.tags.get(j).toLowerCase())){
                    bFound = true; break;
                }
            }
            if(!bFound){
                insert_list.add(tag_list.get(i));
            }
        }*/

        ArrayAdapter<String> tagAdapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, tag_list);
        tagAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        sp_dur.setAdapter(tagAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sp_dur.setPopupBackgroundResource(R.color.colorWhite);
        }
        dialog.show();

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ind = sp_dur.getSelectedItemPosition();
                insertTagToUser(other.uid, tag_list.get(ind));
                dialog.dismiss();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void insertTagToUser(String user_id, final String stag){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Adding tag...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.INSERT_TAG_URL)
                .addBodyParameter("user_id", user_id)
                .addBodyParameter("tags", stag)
                .addBodyParameter("tager_id", MyApp.curUser.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                other.tags.add(0, stag);
                                refreshLayout();
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

    private void showQuestionDlg(){
        TextView tv_cancel;
        Button btn_like, btn_diss;

        final Dialog dialog = new Dialog(context, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_choose_like_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_cancel  = dialog.findViewById(R.id.custom_question_tv_cancel);
        btn_like = dialog.findViewById(R.id.custom_question_btn_like);
        btn_diss = dialog.findViewById(R.id.custom_question_btn_diss);
        dialog.show();

        btn_diss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLikeAndDiss(-1);
                dialog.dismiss();
            }
        });

        btn_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLikeAndDiss(1);
                dialog.dismiss();
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private void setLikeAndDiss(final int val){
        String progress_label = "Liking...";
        if(val != 1) progress_label = "Dissing...";
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel(progress_label)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.LIKE_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("receiver_id", other.uid)
                .addBodyParameter("is_liked", val+"")
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                other.is_liked = val;
                                refreshLayout();
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

    public void showInviteEventFrag(){
        /*List<EventModel> temp = new ArrayList<>();
        for(int i = 0; i < ((HomeActivity)getActivity()).event_list.size(); i++){
            EventModel one = ((HomeActivity)getActivity()).event_list.get(i);
            if(one.creator.uid.equals(MyApp.curUser.uid)){
                temp.add(one);
            }
        }
        if(temp.size() > 0){*/
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            InviteEventFragment ieventFrag = new InviteEventFragment();
            ieventFrag.other = other;
            //ieventFrag.event_list.addAll(temp);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, ieventFrag, "INVITE_EVENT_FRAG").addToBackStack("INVITE_EVENT_FRAG").commit();
        //}

    }

}
