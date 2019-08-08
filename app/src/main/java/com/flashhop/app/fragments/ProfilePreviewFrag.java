package com.flashhop.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.models.ProfileModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.ImageFilePath;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.google.android.gms.maps.model.LatLng;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.utils.Const.APP_TAG;

public class ProfilePreviewFrag extends Fragment {

    HomeActivity parent;
    Context context;

    ImageView iv_back, iv_setting, iv_bag;
    CircleImageView civ_avatar;
    TextView tv_what, tv_title, tv_name, tv_facts, tv_type, tv_events, tv_age, tv_what_guide;
    TextView[] tv_tag = new TextView[3];
    LinearLayout ll_events, ll_tags;
    Button btn_publish;
    LinearLayout[] ll_list = new LinearLayout[7];
    ImageView[] iv_list = new ImageView[9];
    public ProfileModel mData = new ProfileModel();

    int pre_HomeType = Const.HOME_HOME;

    public ProfilePreviewFrag() {
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

        parent = (HomeActivity)getActivity();
        context = getContext();

        //pre_HomeType = MyApp.home_type;
        //MyApp.home_type = Const.HOME_EDIT_PROFILE;

        iv_back = rView.findViewById(R.id.pro_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.showEditProfile();
            }
        });

        initLayout(rView);
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
                uploadProfile();
            }
        });

        iv_setting = view.findViewById(R.id.pro_frag_iv_setting);
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

        iv_setting.setVisibility(View.GONE);
        tv_title.setText(R.string.btn_preview);
        btn_publish.setText(R.string.btn_publish);
        int nAge = TxtUtils.getAge(MyApp.curUser.dob);
        tv_age.setText(nAge+"");
        tv_facts.setText(mData.facts);
        tv_type.setText(mData.person_type);

        if(mData.tags.size() == 0){
            ll_tags.setVisibility(View.GONE);
        } else{
            ll_tags.setVisibility(View.VISIBLE);
            for(int i = 0; i < mData.tags.size(); i++){
                if(i > 3) break;
                tv_tag[i].setVisibility(View.VISIBLE);
                tv_tag[i].setText(mData.tags.get(i));
            }
        }

        int nEvents = MyApp.curUser.event_count;
        if(nEvents == 0){
            ll_events.setVisibility(View.GONE);
        } else{
            ll_events.setVisibility(View.VISIBLE);
            tv_events.setText(nEvents + " events");
        }
    }

    private void uploadProfile(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Publishing...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        ANRequest.MultiPartBuilder multiPartBuilder = AndroidNetworking.upload(Const.HOST_URL + Const.UPLOAD_PROFILE_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addMultipartParameter("personality_type", mData.person_type)
                .addMultipartParameter("fun_facts", mData.facts);

        if(mData.images.size() > 0 && !mData.images.get(0).contains("http")){
            for(int i = 0; i < mData.images.size(); i++ ) {
                String sParam = String.format("image%d",i);
                //File file = savebitmap(i, mData.images.size());
                File file = new File(mData.images.get(i));
                if(file != null){
                    long fileSizeInMB = file.length()/ (1024*1024);
                    if(fileSizeInMB < 2){
                        multiPartBuilder.addMultipartFile(sParam, file);
                    } else{
                        File file1 = ImageFilePath.saveBitmapToFile(file);
                        multiPartBuilder.addMultipartFile(sParam, file1);
                    }
                }
            }
        }

        multiPartBuilder.setTag(APP_TAG)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {

                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1) {
                                JSONObject data_obj = response.getJSONObject("data");
                                MyApp.curUser.first_name = data_obj.getString("first_name");
                                MyApp.curUser.last_name = data_obj.getString("last_name");
                                MyApp.curUser.push_id = data_obj.getString("push_user_id");
                                MyApp.curUser.uid = data_obj.getString("id");
                                int nVerify = data_obj.getInt("email_verified");
                                if(nVerify == 0){
                                    MyApp.curUser.bEmailVerify = false;
                                } else{
                                    MyApp.curUser.bEmailVerify = true;
                                }
                                MyApp.curUser.created_at = data_obj.getString("created_at");
                                MyApp.curUser.updated_at = data_obj.getString("updated_at");
                                MyApp.curUser.dob = data_obj.getString("dob");
                                MyApp.curUser.lang = data_obj.getString("lang");
                                MyApp.curUser.interests = data_obj.getString("interests");
                                MyApp.curUser.gender = data_obj.getString("gender");
                                String sVal = data_obj.getString("social_id");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.social_id = sVal;
                                sVal = data_obj.getString("social_name");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.social_name = sVal;
                                sVal = data_obj.getString("avatar");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.photo_url = sVal;

                                JSONArray img_array = data_obj.getJSONArray("images");
                                MyApp.curUser.images = new ArrayList<>();
                                for(int i = 0; i < img_array.length(); i++){
                                    String sImg = img_array.getString(i);
                                    MyApp.curUser.images.add(sImg);
                                }

                                JSONArray tag_array = data_obj.getJSONArray("tag_list");
                                MyApp.curUser.tags = new ArrayList<>();
                                for(int i = 0; i < tag_array.length(); i++){
                                    String sTag = tag_array.getString(i);
                                    MyApp.curUser.tags.add(sTag);
                                }
                                sVal = data_obj.getString("personality_type");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.person_type = sVal;
                                sVal = data_obj.getString("fun_facts");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.facts = sVal;

                                MyApp.curUser.push_chats = data_obj.getInt("push_chats");
                                MyApp.curUser.push_friends_activities = data_obj.getInt("push_friends_activities");
                                MyApp.curUser.push_my_activities = data_obj.getInt("push_my_activities");
                                if(!data_obj.isNull("lat") && !data_obj.isNull("lng")){
                                    double lat = data_obj.getDouble("lat");
                                    double lng = data_obj.getDouble("lng");
                                    MyApp.selLoc = new LatLng(lat, lng);
                                    MyApp.selAddr = data_obj.getString("address");
                                }

                                parent.showProfile();
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

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private File savebitmap(int ind, int nSize) {

        String filename = String.format("image%d", ind);
        File folder = new File(Const.PHOTO_DIR + "/");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if(!success){
            return null;
        }

        OutputStream outStream = null;
        Bitmap bm_photo = ((BitmapDrawable) iv_list[ind].getDrawable()).getBitmap();
        if(nSize > 3 && ind > 0){
            bm_photo = ((BitmapDrawable) iv_list[ind+2].getDrawable()).getBitmap();
        }


        File file = new File(Const.PHOTO_DIR,filename + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(Const.PHOTO_DIR, filename + ".png");
        }
        try {
            // make a new bitmap from your file
            outStream = new BufferedOutputStream(new FileOutputStream(file));//new FileOutputStream(file);
            bm_photo.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }
}
