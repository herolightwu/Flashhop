package com.flashhop.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.models.ProfileModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.utils.Const.APP_TAG;

public class ProfileFragment extends DialogFragment {

    Context context;
    HomeActivity parent;

    ImageView iv_back, iv_setting, iv_bag;
    CircleImageView civ_avatar;
    TextView tv_what, tv_title, tv_name, tv_facts, tv_type, tv_events, tv_age, tv_what_guide;
    TextView[] tv_tag = new TextView[3];
    LinearLayout ll_events, ll_tags;
    Button btn_publish;
    LinearLayout[] ll_list = new LinearLayout[7];
    ImageView[] iv_list = new ImageView[9];
    ProfileModel mData = new ProfileModel();

    int pre_HomeType = Const.HOME_HOME;

    public ProfileFragment() {
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
        View rView = inflater.inflate(R.layout.fragment_profile, container, false);
        context = getContext();
        parent = (HomeActivity) getActivity();

        pre_HomeType = MyApp.home_type;
        //MyApp.home_type = Const.HOME_PROFILE;

        iv_back = rView.findViewById(R.id.pro_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((HomeActivity)getActivity()).displayView();
                //MyApp.home_type = pre_HomeType;
                if(pre_HomeType == Const.HOME_MSG){
                    parent.setNavViewVisible(false);
                }
                dismiss();
                HomeFragment myFragment = (HomeFragment)parent.getSupportFragmentManager().findFragmentByTag(Const.FRAG_HOME_TAG);
                if (myFragment != null && myFragment.isVisible()) {
                    parent.filterDataFromServer();
                }
            }
        });

        initLayout(rView);
        loadData();
        return rView;
    }

    private void initLayout(View view){
        ll_list[0] = view.findViewById(R.id.pro_frag_ll_photo);
        civ_avatar = view.findViewById(R.id.pro_frag_iv_avatar);
        tv_title = view.findViewById(R.id.pro_frag_tv_title);
        tv_name = view.findViewById(R.id.pro_frag_tv_name);
        tv_name.setText(MyApp.curUser.first_name);

        tv_what = view.findViewById(R.id.pro_frag_tv_what);
        tv_what_guide = view.findViewById(R.id.pro_frag_tv_guide_what);
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
        tv_facts = view.findViewById(R.id.pro_frag_tv_facts);
        tv_type = view.findViewById(R.id.pro_frag_tv_type);

        btn_publish = view.findViewById(R.id.pro_frag_btn_publish);
        btn_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.editProData = mData;
                parent.showEditProfile();
            }
        });

        iv_setting = view.findViewById(R.id.pro_frag_iv_setting);
        iv_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingFrag();
            }
        });

        Picasso.get()
                .load(MyApp.curUser.photo_url)
                .resize(100, 100)
                .into(civ_avatar);

        ll_list[1] = view.findViewById(R.id.pro_frag_ll_photo0);
        ll_list[2] = view.findViewById(R.id.pro_frag_ll_photo1);
        ll_list[3] = view.findViewById(R.id.pro_frag_ll_photo2);
        ll_list[4] = view.findViewById(R.id.pro_frag_ll_photo3);
        ll_list[5] = view.findViewById(R.id.pro_frag_ll_photo4);
        ll_list[6] = view.findViewById(R.id.pro_frag_ll_photo5);

        iv_list[0] = view.findViewById(R.id.pro_frag_iv_photo0);
        iv_list[1] = view.findViewById(R.id.pro_frag_iv_photo1);
        iv_list[2] = view.findViewById(R.id.pro_frag_iv_photo2);
        iv_list[3] = view.findViewById(R.id.pro_frag_iv_photo3);
        iv_list[4] = view.findViewById(R.id.pro_frag_iv_photo4);
        iv_list[5] = view.findViewById(R.id.pro_frag_iv_photo5);
        iv_list[6] = view.findViewById(R.id.pro_frag_iv_photo6);
        iv_list[7] = view.findViewById(R.id.pro_frag_iv_photo7);
        iv_list[8] = view.findViewById(R.id.pro_frag_iv_photo8);

        tv_tag[0] = view.findViewById(R.id.pro_frag_tv_tag_0);
        tv_tag[1] = view.findViewById(R.id.pro_frag_tv_tag_1);
        tv_tag[2] = view.findViewById(R.id.pro_frag_tv_tag_2);

        ll_tags = view.findViewById(R.id.pro_frag_ll_tags);
        ll_events = view.findViewById(R.id.pro_frag_ll_events);
        tv_events = view.findViewById(R.id.pro_frag_tv_events);

        tv_age = view.findViewById(R.id.pro_frag_tv_age);
        tv_tag[0].setVisibility(View.GONE);
        tv_tag[1].setVisibility(View.GONE);
        tv_tag[2].setVisibility(View.GONE);

        iv_bag = view.findViewById(R.id.pro_frag_iv_bag);
        iv_bag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.showTipsFrag();
            }
        });
    }

    public void refreshLayout(){
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowUtils.showImagesLikeInsta(context, ll_list, iv_list, mData.images, dm.widthPixels);

        iv_setting.setVisibility(View.VISIBLE);
        tv_title.setText(R.string.profile_title);
        btn_publish.setText(R.string.btn_edit);
        int nAge = TxtUtils.getAge(MyApp.curUser.dob);
        tv_age.setText(nAge+"");
        tv_facts.setText(mData.facts);
        tv_type.setText(mData.person_type);

        if(MyApp.curUser.event_count == 0){
            ll_events.setVisibility(View.GONE);
        } else{
            ll_events.setVisibility(View.VISIBLE);
            tv_events.setText(MyApp.curUser.event_count + " events");
        }

        if(mData.tags.size() == 0){
            ll_tags.setVisibility(View.GONE);
        } else{
            ll_tags.setVisibility(View.VISIBLE);
            for(int i = 0; i < mData.tags.size(); i++){
                if(i >= 3) break;
                tv_tag[i].setVisibility(View.VISIBLE);
                tv_tag[i].setText(mData.tags.get(i));
            }
        }
    }

    private void showSettingFrag(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        SettingsFragment settingDlg = new SettingsFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(android.R.id.content, settingDlg, "SETTING_DLG").addToBackStack("SETTING_DLG").commit();//R.id.home_frame
    }

    private void loadData(){
        mData.images.clear();
        mData.images.addAll(MyApp.curUser.images);

        mData.tags.clear();
        mData.tags.addAll(MyApp.curUser.tags);
        mData.person_type = MyApp.curUser.person_type;
        mData.facts = MyApp.curUser.facts;
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
        setDebitCard();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setDebitCard(){

        AndroidNetworking.post(Const.HOST_URL + Const.GET_DEBIT_CARD)
                .addBodyParameter("user_id", MyApp.curUser.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                MyApp.curUser.is_debit = 1;
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        MyApp.curUser.is_debit = 0;
                    }
                });
    }
}
