package com.flashhop.app.start;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.helpers.AuthenticationListener;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static com.flashhop.app.utils.Const.APP_TAG;
import static com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Prompt.SIGN_IN;

public class LoginActivity extends AppCompatActivity{

    ImageView iv_back;
    Button btn_login;
    EditText et_email, et_pass;
    ImageView iv_email_err, iv_pass_err;
    TextView tv_forgot;
    LinearLayout ll_facebook, ll_google;

    private CallbackManager callbackManager;
    SaveSharedPrefrence saveSharedPrefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_login);
        saveSharedPrefrence = new SaveSharedPrefrence();

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        onSuccessFacebookLogin(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    }
                });

        initLayout();
    }

    private void initLayout(){
        iv_email_err = findViewById(R.id.login_iv_email_err);
        et_email = findViewById(R.id.login_et_email);
        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String email = editable.toString().trim();
                if (email.length() > 0){
                    WindowUtils.hideEditErr(et_email, iv_email_err);
                } else {
                    WindowUtils.showEditErr(LoginActivity.this, et_email, iv_email_err, R.string.err_email_invalid);
                }
            }
        });

        iv_back = findViewById(R.id.login_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ChooseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        iv_pass_err = findViewById(R.id.login_iv_pass_err);
        et_pass = findViewById(R.id.login_et_password);
        et_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pass = editable.toString().trim();
                if (pass.length() > 0){
                    WindowUtils.hideEditErr(et_pass, iv_pass_err);
                } else {
                    WindowUtils.showEditErr(LoginActivity.this, et_pass, iv_pass_err, R.string.err_pass_length);
                }
            }
        });

        tv_forgot = findViewById(R.id.login_tv_forgot);
        tv_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotActivity.class));
            }
        });

        ll_facebook = findViewById(R.id.login_ll_facebook);
        ll_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if(!TxtUtils.isAppInstalled(LoginActivity.this, "com.facebook.katana") &&
                            !TxtUtils.isAppInstalled(LoginActivity.this, "com.facebook.lite") &&
                            !TxtUtils.isAppInstalled(LoginActivity.this, "com.facebook.orca") &&
                            !TxtUtils.isAppInstalled(LoginActivity.this, "com.facebook.mlite")){
                        WindowUtils.animateView(LoginActivity.this, ll_facebook);
                        return;
                    }
                //}
                if (AccessToken.getCurrentAccessToken() != null) {
                    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                            .Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {

                            LoginManager.getInstance().logOut();
                            WindowUtils.animateView(LoginActivity.this, ll_facebook);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile, email"));
                                }
                            }, 1500);
                        }
                    }).executeAsync();
                } else{
                    LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile, email"));
                }
            }
        });

        ll_google = findViewById(R.id.login_ll_google);
        ll_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleLogin();
            }
        });

        btn_login = findViewById(R.id.login_btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(LoginActivity.this, HomeActivity.class));//debug
                if(checkValidFields()){
                    loginWithEmail();
                } else{
                    WindowUtils.animateView(LoginActivity.this, btn_login);
                }

            }
        });
    }

    private GoogleApiClient mGoogleApiClient;

    public void googleLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN);
    }

    private void loginWithEmail(){
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String oneSignalId = status.getSubscriptionStatus().getUserId();
        final String sEmail = et_email.getText().toString().trim();
        String pass = et_pass.getText().toString().trim();

        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Login...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.LOGIN_URL)
                .addBodyParameter("email", sEmail)
                .addBodyParameter("password", pass)
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
                                String sToken = response.getString("token");
                                JSONObject user_obj = response.getJSONObject("user");
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
                                String sVal = user_obj.getString("dob");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.dob = sVal;
                                sVal = user_obj.getString("lang");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.lang = sVal;
                                sVal = user_obj.getString("interests");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.interests = sVal;
                                sVal = user_obj.getString("gender");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.gender = sVal;
                                sVal = user_obj.getString("social_id");
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
                                if(MyApp.curUser.dob.length() == 0){
                                    startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
                                    finish();
                                    return;
                                } else{
                                    moveNext();
                                }

                            } else{
                                WindowUtils.showEditErr(LoginActivity.this, et_email, iv_email_err, R.string.err_email_invalid);
                                WindowUtils.showEditErr(LoginActivity.this, et_pass, iv_pass_err, R.string.err_login_pass_failed);
                                WindowUtils.animateView(LoginActivity.this, btn_login);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        String err_body = error.getErrorBody();
                        try {
                            JSONObject err_json = new JSONObject(err_body);
                            int success = err_json.getInt("success");
                            JSONObject err_obj = err_json.getJSONObject("error");
                            int err_code = err_obj.getInt("code");
                            if(err_code == 103){ // unverified
                                String ssToken = err_obj.getString("token");
                                MyApp.getInstance().curUser.email = sEmail;
                                MyApp.curUser.token = ssToken;
                                saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_USER_TOKEN, ssToken);
                                startActivity(new Intent(LoginActivity.this, VerifyActivity.class));
                                finish();
                                return;
                            } else if(err_code == 101){//email err
                                WindowUtils.showEditErr(LoginActivity.this, et_email, iv_email_err, R.string.err_email_invalid);
                            } else if(err_code == 102){//wrong pass
                                WindowUtils.showEditErr(LoginActivity.this, et_pass, iv_pass_err, R.string.err_login_pass_failed);
                            }
                        } catch (Throwable t) {
                        }
                        WindowUtils.animateView(LoginActivity.this, btn_login);
                    }
                });
    }

    private void moveNext(){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean checkValidFields(){
        boolean bEmailValid = false, bPassValid = false;
        String email = et_email.getText().toString().trim();
        String pass = et_pass.getText().toString().trim();
        if (email.length() > 0){
            if(TxtUtils.isEmailValid(email)){
                bEmailValid = true;
                WindowUtils.hideEditErr(et_email, iv_email_err);
            } else{
                bEmailValid = false;
                WindowUtils.showEditErr(this, et_email, iv_email_err, R.string.err_email_invalid);
            }
        } else {
            bEmailValid = false;
            WindowUtils.showEditErr(this, et_email, iv_email_err, R.string.err_email_invalid);
        }

        if (pass.length() > 0){
            if(pass.length() > 5){
                bPassValid = true;
                WindowUtils.hideEditErr(et_pass, iv_pass_err);
            } else{
                bPassValid = false;
                WindowUtils.showEditErr(this, et_pass, iv_pass_err, R.string.err_pass_length);
            }
        } else {
            bPassValid = false;
            WindowUtils.showEditErr(this, et_pass, iv_pass_err, R.string.err_pass_length);
        }

        return bEmailValid && bPassValid;
    }

    public void onSuccessFacebookLogin(final AccessToken token) {
        final AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        if (object != null) {
                            try {
                                String fbId = object.getString("id");
                                String fbEmail = object.getString("email");
                                String name = object.getString("first_name");
                                String surname = object.getString("last_name");
                                String photoUrl = "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large";
                                loginWithSocial(fbId, fbEmail, name, surname, photoUrl, "facebook");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {

                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,email,birthday,location,locale,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void loginWithSocial(String socialId, String sEmail, String firstname, String lastname, String photourl, String social_name){
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String oneSignalId = status.getSubscriptionStatus().getUserId();

        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Social Login...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.SOCIAL_URL)
                .addBodyParameter("email", sEmail)
                .addBodyParameter("first_name", firstname)
                .addBodyParameter("last_name", lastname)
                .addBodyParameter("push_user_id", oneSignalId)
                .addBodyParameter("social_image", photourl)
                .addBodyParameter("social_id", socialId)
                .addBodyParameter("social_name", social_name)
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
                                String sToken = data_obj.getString("token");
                                JSONObject user_obj = data_obj.getJSONObject("user");
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

                                if(user_obj.has("dob")){
                                    sVal = user_obj.getString("dob");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.dob = sVal;
                                    sVal = user_obj.getString("lang");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.lang = sVal;
                                    sVal = user_obj.getString("interests");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.interests = sVal;
                                    sVal = user_obj.getString("gender");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.gender = sVal;
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
                                } else{
                                    MyApp.curUser.dob = "";
                                    MyApp.curUser.lang = "";
                                    MyApp.curUser.interests = "";
                                    MyApp.curUser.gender = "";
                                    MyApp.curUser.person_type = "";
                                    MyApp.curUser.facts = "";

                                    MyApp.curUser.push_chats = 1;
                                    MyApp.curUser.push_friends_activities = 1;
                                    MyApp.curUser.push_my_activities = 0;
                                    MyApp.curUser.event_count = 0;
                                    MyApp.curUser.hide_my_location = 0;
                                    MyApp.curUser.hide_my_age = 0;
                                    MyApp.curUser.is_active_by_customer = 0;
                                    MyApp.curUser.dob_update = "";
                                    MyApp.curUser.gender_update = "";
                                    MyApp.selLoc = null;
                                    MyApp.selAddr = "";
                                    MyApp.curUser.location_updated_at = "";
                                }

                                saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_USER_TOKEN, sToken);
                                MyApp.curUser.token = sToken;
                                if(MyApp.curUser.dob.length() == 0){
                                    startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
                                    finish();
                                    return;
                                } else{
                                    saveSharedPrefrence.saveKeyLogin(getApplicationContext(), true);
                                    moveNext();
                                }

                            } else{
                                WindowUtils.showEditErr(LoginActivity.this, et_email, iv_email_err, R.string.err_email_invalid);
                                WindowUtils.showEditErr(LoginActivity.this, et_pass, iv_pass_err, R.string.err_login_pass_failed);
                                if(social_name.equals("facebook"))
                                    WindowUtils.animateView(LoginActivity.this, ll_facebook);
                                else
                                    WindowUtils.animateView(LoginActivity.this, ll_google);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        WindowUtils.showEditErr(LoginActivity.this, et_email, iv_email_err, R.string.err_email_invalid);
                        WindowUtils.showEditErr(LoginActivity.this, et_pass, iv_pass_err, R.string.err_login_pass_failed);
                        if(social_name.equals("facebook"))
                            WindowUtils.animateView(LoginActivity.this, ll_facebook);
                        else
                            WindowUtils.animateView(LoginActivity.this, ll_google);
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //Calling a new function to handle signin
            handleSignInResult(result);
            return;
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void handleSignInResult(GoogleSignInResult result)
    {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            if (acct != null) {
//                String personName = acct.getDisplayName();
//                String personGivenName = acct.getGivenName();
//                String personFamilyName = acct.getFamilyName();
//                String personEmail = acct.getEmail();
//                String personId = acct.getId();
                String photourl = "";
                if(acct.getPhotoUrl() != null){
                    photourl = acct.getPhotoUrl().toString();
                }

                loginWithSocial(acct.getId(), acct.getEmail(), acct.getGivenName(), acct.getFamilyName(), photourl, "google");

            }
        } else {
            Toast.makeText(this, "Login Failed..!", Toast.LENGTH_LONG).show();
        }
    }

}
