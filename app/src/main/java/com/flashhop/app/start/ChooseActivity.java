package com.flashhop.app.start;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
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

public class ChooseActivity extends AppCompatActivity{
    public static final int RC_SIGN_IN = 9001;

    Button btn_email;
    LinearLayout ll_facebook, ll_google;
    TextView tv_desc, tv_login;

    private CallbackManager callbackManager;
    SaveSharedPrefrence saveSharedPrefrence;
    private GoogleSignInClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_choose);

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN);*/
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleApiClient = GoogleSignIn.getClient(this, gso);

        initLayout();
    }

    private void initLayout(){

        tv_desc = findViewById(R.id.choose_tv_desc);
        tv_login = findViewById(R.id.choose_tv_login);
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChooseActivity.this, LoginActivity.class));
            }
        });

        btn_email = findViewById(R.id.choose_btn_email);
        btn_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChooseActivity.this, SignupActivity.class));
            }
        });

        ll_facebook = findViewById(R.id.choose_ll_facebook);
        ll_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TxtUtils.isAppInstalled(ChooseActivity.this, "com.facebook.katana") &&
                        !TxtUtils.isAppInstalled(ChooseActivity.this, "com.facebook.lite") &&
                        !TxtUtils.isAppInstalled(ChooseActivity.this, "com.facebook.orca") &&
                        !TxtUtils.isAppInstalled(ChooseActivity.this, "com.facebook.mlite")){
                    WindowUtils.animateView(ChooseActivity.this, ll_facebook);
                    return;
                }
                //facebook logout
                if (AccessToken.getCurrentAccessToken() != null) {
                    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                            .Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            LoginManager.getInstance().logOut();
                            //LoginManager.getInstance().logInWithReadPermissions(ChooseActivity.this, Arrays.asList("public_profile, email"));
                            WindowUtils.animateView(ChooseActivity.this, ll_facebook);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    LoginManager.getInstance().logInWithReadPermissions(ChooseActivity.this, Arrays.asList("public_profile, email"));
                                }
                            }, 1500);
                        }
                    }).executeAsync();
                } else{
                    LoginManager.getInstance().logInWithReadPermissions(ChooseActivity.this, Arrays.asList("public_profile, email"));
                }

            }
        });

        ll_google = findViewById(R.id.choose_ll_google);
        ll_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleLogin();
            }
        });
    }

    public void googleLogin(){
        Intent signInIntent = mGoogleApiClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onSuccessFacebookLogin(final AccessToken atoken) {
        saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_FACE_TOKEN, atoken.toString());
        final AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMeRequest(
                atoken,
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
                                signupWithSocial(fbId, fbEmail, name, surname, photoUrl, "facebook");
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

    private void signupWithSocial(String socialId, String sEmail, String firstname, String lastname, String photourl, String social_name){
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

                                    sVal = user_obj.getString("last_dob_updated_at");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.dob_update = sVal;
                                    sVal = user_obj.getString("last_gender_updated_at");
                                    if(sVal.equals("null")) sVal = "";
                                    MyApp.curUser.gender_update = sVal;
                                    MyApp.curUser.bDobEnable = user_obj.getInt("update_dob_enable");
                                    MyApp.curUser.bGenderEnable = user_obj.getInt("update_gender_enable");
                                } else{
                                    MyApp.curUser.dob = "";
                                    MyApp.curUser.lang = "";
                                    MyApp.curUser.interests = "";
                                    MyApp.curUser.gender = "";
                                }

                                if(MyApp.curUser.dob.length() > 0){
                                    saveSharedPrefrence.saveKeyLogin(getApplicationContext(), true);
                                    moveHomeActivity();
                                } else{
                                    startActivity(new Intent(ChooseActivity.this, WelcomeActivity.class));
                                    finish();
                                }
                            } else{
                                if(social_name.equals("facebook"))
                                    WindowUtils.animateView(ChooseActivity.this, ll_facebook);
                                else
                                    WindowUtils.animateView(ChooseActivity.this, ll_google);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }

                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        if(social_name.equals("facebook"))
                            WindowUtils.animateView(ChooseActivity.this, ll_facebook);
                        else
                            WindowUtils.animateView(ChooseActivity.this, ll_google);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            return;
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String gID = account.getId();
            String gEmail = account.getEmail();
            String gName = account.getGivenName();
            String gSurname = account.getFamilyName();
            String photourl = "";
            if(account.getPhotoUrl() != null){
                photourl = account.getPhotoUrl().toString();
            }
            // Signed in successfully, show authenticated UI.
            signupWithSocial(gID, gEmail, gName, gSurname, photourl, "google");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getApplicationContext(), "signInResult:failed code=" + e.getStatusCode(),Toast.LENGTH_SHORT).show();
            WindowUtils.animateView(this, ll_google);
        }
    }

}


