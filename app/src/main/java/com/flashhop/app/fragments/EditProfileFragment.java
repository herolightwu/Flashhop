package com.flashhop.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.flashhop.app.models.ProfileModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.KeyboardUtil;
import com.flashhop.app.utils.WindowUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.utils.Const.APP_TAG;


public class EditProfileFragment extends Fragment {

    HomeActivity parent;
    Context context;

    ImageView iv_back, iv_setting, iv_bag;
    CircleImageView civ_avatar;
    Spinner sp_type;
    TextView tv_what, tv_name;
    EditText et_facts;
    LinearLayout ll_what_desc, ll_bag_desc, ll_outside;
    Button btn_preview;
    LinearLayout[] ll_list = new LinearLayout[7];
    ImageView[] iv_list = new ImageView[9];
    List<String> type_list = new ArrayList<String>();
    ProfileModel mData = new ProfileModel();

    int pre_HomeType = Const.HOME_HOME;

    SaveSharedPrefrence saveSharedPrefrence = new SaveSharedPrefrence();

    public EditProfileFragment() {
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
        View rView = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        parent = (HomeActivity) getActivity();
        context = getContext();

        //pre_HomeType = MyApp.home_type;
        //MyApp.home_type = Const.HOME_EDIT_PROFILE;

        iv_back = rView.findViewById(R.id.edit_pro_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.home_type = pre_HomeType;
                parent.displayView();
            }
        });

        initLayout(rView);
        loadPersonalityTypes();

        parent.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return rView;
    }

    private void initLayout(View view){
        ll_outside = view.findViewById(R.id.edit_pro_frag_outside);
        ll_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtil.hideKeyboard(parent);
            }
        });
        ll_list[0] = view.findViewById(R.id.edit_pro_frag_ll_photo);
        ll_list[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.editProData.images.clear();
                parent.choosePhotoFromGallery();
            }
        });
        civ_avatar = view.findViewById(R.id.edit_pro_frag_iv_avatar);
        civ_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeAvatar();
            }
        });
        tv_name = view.findViewById(R.id.edit_pro_frag_tv_name);
        tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeAvatar();
            }
        });
        tv_name.setText(MyApp.curUser.first_name);
        sp_type = view.findViewById(R.id.edit_pro_frag_sp_type);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(sp_type);
            // Set popupWindow height to 500px
            popupWindow.setHeight(600);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        tv_what = view.findViewById(R.id.edit_pro_frag_tv_what);
        tv_what.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ll_what_desc.getVisibility() == View.VISIBLE){
                    ll_bag_desc.setVisibility(View.GONE);
                    ll_what_desc.setVisibility(View.GONE);
                } else{
                    //ll_bag_desc.setVisibility(View.VISIBLE);
                    ll_what_desc.setVisibility(View.VISIBLE);
                }
            }
        });

        et_facts = view.findViewById(R.id.edit_pro_frag_et_facts);
        ll_bag_desc = view.findViewById(R.id.edit_pro_frag_ll_bag_description);
        ll_what_desc = view.findViewById(R.id.edit_pro_frag_ll_what_description);
        ll_what_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uriUrl = Uri.parse(Const.WHAT_IS_LINK);
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });
        btn_preview = view.findViewById(R.id.edit_pro_frag_btn_preview);
        btn_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mData.person_type = (String) sp_type.getSelectedItem();
                mData.facts = et_facts.getText().toString();
                showPreviewProfile();
            }
        });

        Picasso.get()
                .load(MyApp.curUser.photo_url)
                .into(civ_avatar);

        ll_list[1] = view.findViewById(R.id.edit_pro_frag_ll_photo0);
        ll_list[2] = view.findViewById(R.id.edit_pro_frag_ll_photo1);
        ll_list[3] = view.findViewById(R.id.edit_pro_frag_ll_photo2);
        ll_list[4] = view.findViewById(R.id.edit_pro_frag_ll_photo3);
        ll_list[5] = view.findViewById(R.id.edit_pro_frag_ll_photo4);
        ll_list[6] = view.findViewById(R.id.edit_pro_frag_ll_photo5);

        iv_list[0] = view.findViewById(R.id.edit_pro_frag_iv_photo0);
        iv_list[1] = view.findViewById(R.id.edit_pro_frag_iv_photo1);
        iv_list[2] = view.findViewById(R.id.edit_pro_frag_iv_photo2);
        iv_list[3] = view.findViewById(R.id.edit_pro_frag_iv_photo3);
        iv_list[4] = view.findViewById(R.id.edit_pro_frag_iv_photo4);
        iv_list[5] = view.findViewById(R.id.edit_pro_frag_iv_photo5);
        iv_list[6] = view.findViewById(R.id.edit_pro_frag_iv_photo6);
        iv_list[7] = view.findViewById(R.id.edit_pro_frag_iv_photo7);
        iv_list[8] = view.findViewById(R.id.edit_pro_frag_iv_photo8);

        iv_setting = view.findViewById(R.id.edit_pro_frag_iv_setting);
        iv_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingFrag();
            }
        });

        iv_bag = view.findViewById(R.id.edit_pro_frag_iv_bag);
        iv_bag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.showTipsFrag();
            }
        });
    }

    private void changeAvatar(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ChangeAvatarFragment avatarFrag = new ChangeAvatarFragment();
        fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
        fragmentTransaction.add(R.id.home_frame, avatarFrag, "FRAG_CHANGE_AVATAR").addToBackStack("FRAG_CHANGE_AVATAR").commit();
    }

    private void showPreviewProfile(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ProfilePreviewFrag profileFrag = new ProfilePreviewFrag();
        profileFrag.mData = mData;
        fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
        fragmentTransaction.add(R.id.home_frame, profileFrag, Const.FRAG_PROFILE_TAG).addToBackStack(Const.FRAG_PROFILE_TAG).commit();
    }

    private void showSettingFrag(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        SettingsFragment settingDlg = new SettingsFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
        transaction.add(android.R.id.content, settingDlg, "SETTING_DLG").addToBackStack("SETTING_DLG").commit();//R.id.home_frame
    }

    public void refreshLayout(){
        mData = parent.editProData;
        DisplayMetrics dm = new DisplayMetrics();
        parent.getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowUtils.showImagesLikeInsta(context, ll_list, iv_list, mData.images, dm.widthPixels);

        String sGuide = saveSharedPrefrence.getString(context, SaveSharedPrefrence.KEY_GUIDE);
        if(!sGuide.contains("bag")){
            ll_bag_desc.setVisibility(View.VISIBLE);
            saveSharedPrefrence.putString(context, SaveSharedPrefrence.KEY_GUIDE, sGuide + ",bag");
        } else{
            ll_bag_desc.setVisibility(View.GONE);
        }

        for(int j = 0; j < type_list.size(); j++){
            if(type_list.get(j).equals(mData.person_type)){
                sp_type.setSelection(j);
            }
        }
        et_facts.setText(mData.facts);
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    private void loadPersonalityTypes(){
        AndroidNetworking.get(Const.HOST_URL + Const.PERSONALITY_TYPES_URL)
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
                                    type_list.clear();
                                    for(int i = 0; i < data.length(); i++){
                                        JSONObject obj = data.getJSONObject(i);
                                        type_list.add(obj.getString("type_name"));
                                    }
                                    ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, type_list);
                                    typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    sp_type.setAdapter(typeAdapter);
                                    refreshLayout();
                                    return;
                                }
                            }
                            loadDefaultTypes();
                        } catch (JSONException ex){
                            ex.printStackTrace();
                            loadDefaultTypes();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loadDefaultTypes();
                    }
                });
    }

    private void loadDefaultTypes(){
        String[] default_type = new String[]{ "Architect - INTJ", "Logician - INTP", "Commander - ENTJ", "Debater - ENTP",
                "Advocate - INFJ", "Mediator - INFP", "Protagonist - ENFJ", "Campaigner - ENFP", "Logistician - ISTJ", "Defender - ISFJ",
                "Executive - ESTJ", "Consul - ESFJ", "Virtuoso - ISTP", "Adventurer - ISFP", "Entrepreneur - ESTP", "Entertainer - ESFP"};
        type_list.clear();
        for(int i = 0; i < default_type.length; i++){
            type_list.add(default_type[i]);
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, type_list);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_type.setAdapter(typeAdapter);
        refreshLayout();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
