package com.flashhop.app.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.libraries.ColorArcProgressBar;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

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

import static com.flashhop.app.utils.Const.APP_TAG;

public class PublishEventFragment extends DialogFragment {

    public static final int STORAGE_PERMISSIONS_REQUEST_2= 7892;

    HomeActivity myAct;

    Button btn_invite, btn_edit, btn_group, btn_where;
    ImageView iv_like, iv_share;
    ImageView iv_photo, iv_back;
    TextView tv_month, tv_day, tv_weekday, tv_title, tv_time, tv_address, tv_status, tv_desc;
    RelativeLayout rl_graph, rl_avatars;
    TextView tv_female, tv_male, tv_guide_where;
    ColorArcProgressBar bar_female, bar_male, bar_rest;
    CircleImageView[] civ_avatar = new CircleImageView[6];
    int pre_HomeType = Const.HOME_HOME;

    boolean bWhere = false;

    public EventModel pub_Event = new EventModel();


    public PublishEventFragment() {
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
        View rootV = inflater.inflate(R.layout.fragment_publish_event, container, false);
        myAct = (HomeActivity)getActivity();
        pre_HomeType = MyApp.home_type;
        MyApp.home_type = Const.HOME_EVENT;
        initLayout(rootV);
        return rootV;
    }

    private void initLayout(View rView){

        iv_back = rView.findViewById(R.id.pub_event_frag_iv_back);
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

        btn_edit = rView.findViewById(R.id.pub_event_frag_btn_edit);
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myAct.event_photo = "";
                FragmentManager fragmentManager = myAct.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                HostEventFragment hosteventFrag = new HostEventFragment();
                hosteventFrag.draftEvent = pub_Event;
                hosteventFrag.bChooseAddr = true;
                fragmentTransaction.add(R.id.home_frame, hosteventFrag, Const.FRAG_EVENT_TAG).addToBackStack(Const.FRAG_EVENT_TAG).commit();
            }
        });

        btn_invite = rView.findViewById(R.id.pub_event_frag_btn_invite);
        btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myAct.showInviteFriendsFrag(pub_Event);
            }
        });

        btn_group = rView.findViewById(R.id.pub_event_frag_btn_groupchat);
        btn_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoGroupChat(pub_Event);
            }
        });

        btn_where = rView.findViewById(R.id.pub_event_frag_btn_where);
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

        iv_like = rView.findViewById(R.id.pub_event_frag_iv_like);
        iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pub_Event.nLike == 1) {
                    likeEvent(-1);
                } else{
                    likeEvent(1);
                }
            }
        });

        iv_share = rView.findViewById(R.id.pub_event_frag_iv_share);
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission())
                    shareEvent();
            }
        });

        iv_photo = rView.findViewById(R.id.pub_event_frag_iv_photo);
        tv_month = rView.findViewById(R.id.pub_event_frag_tv_month);
        tv_day = rView.findViewById(R.id.pub_event_frag_tv_day);
        tv_weekday = rView.findViewById(R.id.pub_event_frag_tv_weekday);
        tv_time = rView.findViewById(R.id.pub_event_frag_tv_time);
        tv_title = rView.findViewById(R.id.pub_event_frag_tv_title);
        tv_status = rView.findViewById(R.id.pub_event_frag_tv_status);
        tv_desc = rView.findViewById(R.id.pub_event_frag_tv_description);
        tv_address = rView.findViewById(R.id.pub_event_frag_tv_address);
        rl_graph = rView.findViewById(R.id.pub_event_frag_rl_graph);
        tv_female = rView.findViewById(R.id.pub_event_frag_tv_female);
        tv_male = rView.findViewById(R.id.pub_event_frag_tv_male);
        bar_female = rView.findViewById(R.id.pub_event_frag_bar_female);
        bar_male = rView.findViewById(R.id.pub_event_frag_bar_male);
        bar_rest = rView.findViewById(R.id.pub_event_frag_bar_rest);
        civ_avatar[0] = rView.findViewById(R.id.pub_event_frag_civ_avatar0);
        civ_avatar[1] = rView.findViewById(R.id.pub_event_frag_civ_avatar1);
        civ_avatar[2] = rView.findViewById(R.id.pub_event_frag_civ_avatar2);
        civ_avatar[3] = rView.findViewById(R.id.pub_event_frag_civ_avatar3);
        civ_avatar[4] = rView.findViewById(R.id.pub_event_frag_civ_avatar4);
        civ_avatar[5] = rView.findViewById(R.id.pub_event_frag_civ_avatar5);
        rl_avatars = rView.findViewById(R.id.pub_event_frag_rl_avatars);
        rl_avatars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = myAct.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
                GroupInfoFragment group_frag = new GroupInfoFragment();
                group_frag.group_event = pub_Event;
                fragmentTransaction.add(android.R.id.content, group_frag, Const.FRAG_GROUP_INFO).addToBackStack(Const.FRAG_GROUP_INFO).commit();
            }
        });

        tv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.home_type = Const.HOME_HOME;
                myAct.displayView();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(myAct != null)
                            myAct.moveEventLocation(pub_Event.loc);
                    }
                }, 1200);
            }
        });

        tv_guide_where = rView.findViewById(R.id.pub_event_frag_tv_guide_where);
    }

    private void refreshLayout(){
        if(pub_Event.photo.length() > 0){
            Picasso.get()
                    .load(pub_Event.photo)
                    .into(iv_photo);
        } else{
            for(int i = 0; i < 9; i++){
                if(Const.interest_list[i].toLowerCase().equals(pub_Event.category.toLowerCase())){
                    iv_photo.setImageResource(Const.cover_res[i+1]);
                    break;
                }
            }

        }
        try{
            SimpleDateFormat  sDf = new SimpleDateFormat("MMM dd yyyy");
            Date dt = sDf.parse(pub_Event.date);
            SimpleDateFormat eDf = new SimpleDateFormat("EEE");
            String weekday = eDf.format(dt);
            tv_weekday.setText(weekday);
        } catch (ParseException ex){
            ex.printStackTrace();
        }

        int nMembers = 0;
        for(int i = 0; i < pub_Event.members.size(); i++){
            UserModel one = pub_Event.members.get(i);
            if(one.is_friend == 1){
                nMembers++;
            }
        }

        String[] sDate = pub_Event.date.split(" ");
        tv_month.setText(sDate[0]);
        tv_day.setText(sDate[1]);
        tv_title.setText(pub_Event.title);
        tv_time.setText(pub_Event.time.toUpperCase());
        tv_address.setText(pub_Event.address);
        String[] sPeop = pub_Event.people.split(",");
        String sStatus = String.format("Min %s, Max %s, %d are going, %d friends", sPeop[0], sPeop[1], pub_Event.members.size() + 1, nMembers);
        tv_status.setText(sStatus);
        tv_desc.setText(pub_Event.desc);

        long diff = TxtUtils.getDifferenceTime(pub_Event.date, pub_Event.time);
        if(diff <= 30){
            btn_where.setBackgroundResource(R.drawable.border_round_yellow);
            btn_where.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDGray));
            bWhere = true;
        } else{
            btn_where.setBackgroundResource(R.drawable.border_round_gray);
            btn_where.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
            bWhere = false;
        }

        int pMax = Integer.valueOf(sPeop[1]);

        if(pub_Event.members.size() == 0){
            rl_graph.setVisibility(View.GONE);
        } else{
            ViewGroup.LayoutParams param = rl_graph.getLayoutParams();
            param.height = getScreenWidth()/2;
            rl_graph.setLayoutParams(param);
            rl_graph.setVisibility(View.VISIBLE);
            int memsize = pub_Event.members.size() + 1;//add creator
            int nrest = 0;//pMax - memsize;
            int nboy = 0;
            for(int i = 0; i < pub_Event.members.size(); i++){
                UserModel one = pub_Event.members.get(i);
                if(one.gender.equals("male")){
                    nboy++;
                }
            }
            if(pub_Event.creator.gender.equals("male")) nboy++;
            int ngirl = memsize - nboy;
            bar_female.setStartAngle(225);
            int nTotal = ngirl*360 / memsize - 10;
            bar_female.setTotalAngle(nTotal);
            int nAngle = (235 + nTotal) % 360;//rest start
            /*nTotal = nrest * 360 / memsize - 10;
            bar_rest.setStartAngle(nAngle);
            bar_rest.setTotalAngle(nTotal);
            nAngle += (10 + nTotal);
            nAngle = nAngle % 360;*/
            bar_male.setStartAngle(nAngle);
            bar_male.setTotalAngle(nboy * 360 /memsize - 10);

            bar_male.setMaxValues(100);
            bar_male.setCurrentValues(100);
            bar_rest.setMaxValues(100);
            bar_rest.setCurrentValues(100);
            bar_female.setMaxValues(100);
            bar_female.setCurrentValues(100);
            tv_male.setText(nboy + "");
            tv_female.setText(ngirl + "");
        }

        if(pub_Event.creator != null){
            civ_avatar[0].setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(pub_Event.creator.photo_url)
                    .into(civ_avatar[0]);
            int nSize = pub_Event.members.size() > 5 ? 5 : pub_Event.members.size();
            for(int k = 0; k < nSize; k++){
                civ_avatar[k+1].setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(pub_Event.members.get(k).photo_url)
                        .into(civ_avatar[k+1]);
            }
        }

        if(pub_Event.nLike == 1){
            iv_like.setImageResource(R.drawable.ic_liked);
        } else{
            iv_like.setImageResource(R.drawable.ic_like);
        }
    }

    private void showHoppersMap(){
        FragmentManager fragmentManager = myAct.getSupportFragmentManager();
        HoppersFragment hoppersFrag = new HoppersFragment();
        hoppersFrag.ev = pub_Event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.replace(R.id.home_frame, hoppersFrag, Const.FRAG_HOPPERS_TAG).addToBackStack(Const.FRAG_HOPPERS_TAG).commit();
    }

    private void gotoGroupChat(EventModel ev){
        //if(ev.members.size() > 0){
            FragmentManager fragmentManager = myAct.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
            GroupChatFragment group_frag = new GroupChatFragment();
            group_frag.group_event = ev;
            fragmentTransaction.add(R.id.home_frame, group_frag, Const.FRAG_CHAT_GROUP).addToBackStack(Const.FRAG_CHAT_GROUP).commit();
        //}
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    private void likeEvent(final int nlike){
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Liking...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.EVENT_LIKE_DISLIKE)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("event_id", pub_Event.id)
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
                                pub_Event.nLike = nlike;
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

    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
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
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, pub_Event.title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, pub_Event.desc);
        startActivity(Intent.createChooser(sharingIntent, null));
    }

    private boolean checkPermission(){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ) {

                try {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_2);
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
}
