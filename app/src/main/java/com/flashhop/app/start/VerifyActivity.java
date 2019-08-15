package com.flashhop.app.start;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static com.flashhop.app.utils.Const.APP_TAG;

public class VerifyActivity extends AppCompatActivity {

    ImageView iv_back;
    OtpView otpview;
    Button btn_ok;
    TextView tv_resend, tv_desc, tv_facebook;
    String sEmail="";
    String otpcode="";
    boolean bresend = false;
    private CallbackManager callbackManager;
    SaveSharedPrefrence saveSharedPrefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_verify);
        saveSharedPrefrence = new SaveSharedPrefrence();
        sEmail = MyApp.getInstance().curUser.email;

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
        iv_back = findViewById(R.id.verify_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_ok = findViewById(R.id.verify_btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(otpcode.length() == 6){
                    verifyCode();
                    //startActivity(new Intent(VerifyActivity.this, WelcomeActivity.class));
                } else{
                    WindowUtils.animateView(VerifyActivity.this, btn_ok);
                }

            }
        });

        tv_desc = findViewById(R.id.verify_tv_desc);
        String desc_str = String.format(getString(R.string.verify_desc), sEmail);
        tv_desc.setText(desc_str);

        tv_resend = findViewById(R.id.verify_tv_resend);
        tv_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bresend){
                    resendCode();
                }

            }
        });

        otpview = findViewById(R.id.verify_otp_view);
        otpview.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                otpcode = otp;
            }
        });

        otpview.addTextChangedListener(new TextWatcher() {
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
                    String desc_str = String.format(getString(R.string.verify_desc), sEmail);
                    tv_desc.setText(desc_str);
                    tv_desc.setTypeface(MyApp.myfont_normal);
                    tv_desc.setTextColor(ContextCompat.getColor(VerifyActivity.this, R.color.colorWhite));
                }

            }
        });

        tv_facebook = findViewById(R.id.verify_tv_facebook);
        tv_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TxtUtils.isAppInstalled(VerifyActivity.this, "com.facebook.katana") &&
                        !TxtUtils.isAppInstalled(VerifyActivity.this, "com.facebook.lite") &&
                        !TxtUtils.isAppInstalled(VerifyActivity.this, "com.facebook.orca") &&
                        !TxtUtils.isAppInstalled(VerifyActivity.this, "com.facebook.mlite")){
                    WindowUtils.animateView(VerifyActivity.this, tv_facebook);
                    return;
                }
                if (AccessToken.getCurrentAccessToken() != null) {
                    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                            .Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {

                            LoginManager.getInstance().logOut();
                            //LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile, email"));
                            WindowUtils.animateView(VerifyActivity.this, tv_facebook);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    LoginManager.getInstance().logInWithReadPermissions(VerifyActivity.this, Arrays.asList("public_profile, email"));
                                }
                            }, 1500);
                        }
                    }).executeAsync();
                } else{
                    LoginManager.getInstance().logInWithReadPermissions(VerifyActivity.this, Arrays.asList("public_profile, email"));
                }
            }
        });
        tv_facebook.setVisibility(View.GONE);

    }

    private void resendCode(){
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Resending...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.RESEND_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("email", sEmail)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        bresend = true;
                        tv_resend.setText(R.string.resent_verify_code_txt);
                        String desc_str = String.format(getString(R.string.verify_desc), sEmail);
                        tv_desc.setText(desc_str);
                        tv_desc.setTextColor(ContextCompat.getColor(VerifyActivity.this, R.color.colorWhite));
                        tv_facebook.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        // handle error
                        WindowUtils.animateView(VerifyActivity.this, tv_resend);
                    }
                });
    }

    private void verifyCode(){
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Verifying...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.VERIFY_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("code", otpcode)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                MyApp.curUser.bEmailVerify = true;
                                startActivity(new Intent(VerifyActivity.this, WelcomeActivity.class));
                                finish();
                            } else{
                                tv_desc.setText(R.string.err_invalid_code);
                                tv_desc.setTypeface(MyApp.myfont_italic);
                                tv_desc.setTextColor(ContextCompat.getColor(VerifyActivity.this, R.color.colorYellow));
                                WindowUtils.animateView(VerifyActivity.this, btn_ok);
                                otpcode = "";
                            }
                        } catch(JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        tv_desc.setText(R.string.err_invalid_code);
                        tv_desc.setTypeface(MyApp.myfont_italic);
                        tv_desc.setTextColor(ContextCompat.getColor(VerifyActivity.this, R.color.colorYellow));
                        WindowUtils.animateView(VerifyActivity.this, btn_ok);
                        otpcode = "";
                    }
                });
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
                                saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_USER_TOKEN, sToken);
                                MyApp.curUser.token = sToken;

                                if(user_obj.has("dob")){
                                    MyApp.curUser.dob = user_obj.getString("dob");
                                    MyApp.curUser.lang = user_obj.getString("lang");
                                    MyApp.curUser.interests = user_obj.getString("interests");
                                    MyApp.curUser.gender = user_obj.getString("gender");

                                    saveSharedPrefrence.saveKeyLogin(getApplicationContext(), true);
                                    moveHomeActivity();
                                } else{
                                    startActivity(new Intent(VerifyActivity.this, WelcomeActivity.class));
                                }
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                    }
                });

    }

    private void moveHomeActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
