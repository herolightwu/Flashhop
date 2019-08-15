package com.flashhop.app.start;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;


import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.crashlytics.android.Crashlytics;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SplashActivity extends AppCompatActivity {

    SaveSharedPrefrence saveSharedPrefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_splash);
        saveSharedPrefrence = new SaveSharedPrefrence();


        Fabric.with(this, new Crashlytics());

        MyApp.getInstance().myfont_normal = Typeface.createFromAsset(getAssets(), "fonts/sourcesanspro_regular.ttf");
        MyApp.getInstance().myfont_italic = Typeface.createFromAsset(getAssets(), "fonts/sourcesanspro_regularitalic.ttf");

        if(saveSharedPrefrence.getKeyLogin(getApplicationContext())){
            checkUserInfo();
        } else{
            saveSharedPrefrence.saveKeyLogin(getApplicationContext(), false);
            startActivity(new Intent(this, ChooseActivity.class));
            finish();
        }
    }

    private void checkUserInfo(){
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String oneSignalId = status.getSubscriptionStatus().getUserId();
        final String sToken = saveSharedPrefrence.getString(getApplicationContext(), SaveSharedPrefrence.KEY_USER_TOKEN);
        if(sToken != null && sToken.length() > 0){
            final KProgressHUD hud = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setLabel("Login Checking...")
                    .setCancellable(true)
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show();
            AndroidNetworking.post(Const.HOST_URL + Const.CHECK_TOKEN)
                    .addHeaders("Authorization", "Bearer " + sToken)
                    .addBodyParameter("push_user_id", oneSignalId)
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
                                    JSONObject user_obj = response.getJSONObject("data");
                                    MyApp.getInstance().curUser.email = user_obj.getString("email");
                                    MyApp.curUser.first_name = user_obj.getString("first_name");
                                    MyApp.curUser.last_name = user_obj.getString("last_name");
                                    MyApp.curUser.push_id = user_obj.getString("push_user_id");
                                    MyApp.curUser.uid = user_obj.getString("id");
                                    MyApp.curUser.created_at = user_obj.getString("created_at");
                                    MyApp.curUser.updated_at = user_obj.getString("updated_at");
                                    int nVerify = user_obj.getInt("email_verified");
                                    if(nVerify == 0){
                                        MyApp.curUser.bEmailVerify = false;
                                    } else{
                                        MyApp.curUser.bEmailVerify = true;
                                    }
                                    MyApp.curUser.dob = user_obj.getString("dob");
                                    MyApp.curUser.lang = user_obj.getString("lang");
                                    MyApp.curUser.interests = user_obj.getString("interests");
                                    MyApp.curUser.gender = user_obj.getString("gender");
                                    String sVal = user_obj.getString("social_id");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.social_id = sVal;
                                    sVal = user_obj.getString("social_name");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.social_name = sVal;
                                    sVal = user_obj.getString("avatar");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.photo_url = sVal;

                                    JSONArray img_array = user_obj.getJSONArray("images");
                                    MyApp.curUser.images = new ArrayList<>();
                                    for(int i = 0; i < img_array.length(); i++){
                                        String sImg = img_array.getString(i);
                                        MyApp.curUser.images.add(sImg);
                                    }

                                    JSONArray tag_array = user_obj.getJSONArray("tag_list");
                                    MyApp.curUser.tags = new ArrayList<>();
                                    for(int i = 0; i < tag_array.length(); i++){
                                        String sTag = tag_array.getString(i);
                                        MyApp.curUser.tags.add(sTag);
                                    }
                                    sVal = user_obj.getString("personality_type");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.person_type = sVal;
                                    sVal = user_obj.getString("fun_facts");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.facts = sVal;

                                    MyApp.curUser.push_chats = user_obj.getInt("push_chats");
                                    MyApp.curUser.push_friends_activities = user_obj.getInt("push_friends_activities");
                                    MyApp.curUser.push_my_activities = user_obj.getInt("push_my_activities");
                                    MyApp.curUser.event_count = user_obj.getInt("event_count");
                                    MyApp.curUser.hide_my_location = user_obj.getInt("hide_my_location");
                                    MyApp.curUser.hide_my_age = user_obj.getInt("hide_my_age");
                                    MyApp.curUser.is_active_by_customer = user_obj.getInt("is_active_by_customer");
                                    sVal = user_obj.getString("last_dob_updated_at");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.dob_update = sVal;
                                    sVal = user_obj.getString("last_gender_updated_at");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.gender_update = sVal;
                                    MyApp.curUser.bDobEnable = user_obj.getInt("update_dob_enable");
                                    MyApp.curUser.bGenderEnable = user_obj.getInt("update_gender_enable");
                                    if(!user_obj.isNull("lat") && !user_obj.isNull("lng")){
                                        double lat = user_obj.getDouble("lat");
                                        double lng = user_obj.getDouble("lng");
                                        MyApp.selLoc = new LatLng(lat, lng);
                                        MyApp.selAddr = user_obj.getString("address");
                                        MyApp.curUser.location_updated_at = user_obj.getString("location_updated_at");
                                    }

                                    saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_USER_TOKEN, sToken);
                                    saveSharedPrefrence.saveKeyLogin(getApplicationContext(), true);
                                    MyApp.curUser.token = sToken;
                                    moveNext();

                                } else{
                                    saveSharedPrefrence.saveKeyLogin(getApplicationContext(), false);
                                    startActivity(new Intent(SplashActivity.this, ChooseActivity.class));
                                    finish();
                                }
                            } catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }
                        @Override
                        public void onError(ANError error) {
                            hud.dismiss();
                            saveSharedPrefrence.saveKeyLogin(getApplicationContext(), false);
                            startActivity(new Intent(SplashActivity.this, ChooseActivity.class));
                            finish();
                        }
                    });
        }else{
            saveSharedPrefrence.saveKeyLogin(getApplicationContext(), false);
            startActivity(new Intent(SplashActivity.this, ChooseActivity.class));
            finish();
        }

    }

    private void moveNext(){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
