package com.flashhop.app.start;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SignupActivity extends AppCompatActivity {

    ImageView iv_back;
    EditText et_first, et_last, et_email, et_pass, et_confirm;
    ImageView iv_first, iv_last, iv_email, iv_pass, iv_confirm;
    Button btn_sendcode;
    SaveSharedPrefrence saveSharedPrefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_signup);
        saveSharedPrefrence = new SaveSharedPrefrence();

        initLayout();
    }

    private void initLayout(){

        iv_first = findViewById(R.id.signup_iv_firstname);
        iv_last = findViewById(R.id.signup_iv_lastname);
        iv_email = findViewById(R.id.signup_iv_email);
        iv_pass = findViewById(R.id.signup_iv_pass);
        iv_confirm = findViewById(R.id.signup_iv_confirm);

        et_first = findViewById(R.id.signup_et_firstname);
        et_first.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0){
                    WindowUtils.hideEditErr(et_first, iv_first);
                } else {
                    WindowUtils.showEditErr(SignupActivity.this, et_first, iv_first, R.string.err_first_empty);
                }
            }
        });
        et_last = findViewById(R.id.signup_et_lastname);
        et_last.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0){
                    WindowUtils.hideEditErr(et_last, iv_last);
                } else {
                    WindowUtils.showEditErr(SignupActivity.this, et_last, iv_last, R.string.err_last_empty);
                }
            }
        });
        et_email = findViewById(R.id.signup_et_email);
        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0){
                    WindowUtils.hideEditErr(et_email, iv_email);
                } else {
                    WindowUtils.showEditErr(SignupActivity.this, et_email, iv_email, R.string.err_email_invalid);
                }
            }
        });
        et_pass = findViewById(R.id.signup_et_password);
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
                    WindowUtils.hideEditErr(et_pass, iv_pass);
                } else {
                    WindowUtils.showEditErr(SignupActivity.this, et_pass, iv_pass, R.string.err_pass_length);
                }
            }
        });

        et_confirm = findViewById(R.id.signup_et_confirm);
        et_confirm.addTextChangedListener(new TextWatcher() {
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
                    WindowUtils.hideEditErr(et_confirm, iv_confirm);
                } else {
                    WindowUtils.showEditErr(SignupActivity.this, et_confirm, iv_confirm, R.string.err_pass_retype);
                }
            }
        });

        btn_sendcode = findViewById(R.id.signup_btn_send);
        btn_sendcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidFields()){
                    sendVerifyCode();
                } else{
                    WindowUtils.animateView(SignupActivity.this, btn_sendcode);
                }
            }
        });

        iv_back = findViewById(R.id.signup_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, ChooseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void sendVerifyCode(){
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String oneSignalId = status.getSubscriptionStatus().getUserId();
        String email = et_email.getText().toString().trim();
        String sfirst = et_first.getText().toString().trim();
        String slast = et_last.getText().toString().trim();
        String pass = et_pass.getText().toString().trim();

        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Registering...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.SIGNUP_URL)
                .addBodyParameter("email", email)
                .addBodyParameter("password", pass)
                .addBodyParameter("first_name", sfirst)
                .addBodyParameter("last_name", slast)
                .addBodyParameter("push_user_id", oneSignalId)
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
                                JSONObject data_obj = response.getJSONObject("data");
                                String sToken = data_obj.getString("token");
                                JSONObject user_obj = data_obj.getJSONObject("user");
                                //Gson gson = new Gson();
                                //MyApp.getInstance().curUser = gson.fromJson(user_obj.toString(), UserModel.class);
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
                                saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_USER_TOKEN, sToken);
                                MyApp.curUser.token = sToken;
                                moveToNext();

                            } else{
                                WindowUtils.animateView(SignupActivity.this, btn_sendcode);
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
                            if(err_code == 105){ // exist same email
                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                finish();
                            } else{
                                WindowUtils.animateView(SignupActivity.this, btn_sendcode);
                            }
                        } catch (Throwable t) {
                            WindowUtils.animateView(SignupActivity.this, btn_sendcode);
                        }
                    }
                });
    }

    private void moveToNext(){
        Intent intent = new Intent(SignupActivity.this, VerifyActivity.class);
        startActivity(intent);
    }

    private boolean checkValidFields(){
        boolean bEmail = false, bfirst = false, blast = false, bPass = false;
        String email = et_email.getText().toString().trim();
        String sfirst = et_first.getText().toString().trim();
        String slast = et_last.getText().toString().trim();
        String pass = et_pass.getText().toString().trim();
        String confirm = et_confirm.getText().toString().trim();

        if (email.length() > 0){
            if(TxtUtils.isEmailValid(email)){
                bEmail = true;
                WindowUtils.hideEditErr(et_email, iv_email);
            } else{
                bEmail = false;
                WindowUtils.showEditErr(this, et_email, iv_email, R.string.err_email_invalid);
            }
        } else {
            bEmail = false;
            WindowUtils.showEditErr(this, et_email, iv_email, R.string.err_email_invalid);
        }

        if (sfirst.length() > 0){
            bfirst = true;
            WindowUtils.hideEditErr(et_first, iv_first);
        } else {
            bfirst = false;
            WindowUtils.showEditErr(this, et_first, iv_first, R.string.err_first_empty);
        }

        if (slast.length() > 0){
            blast = true;
            WindowUtils.hideEditErr(et_last, iv_last);
        } else {
            blast = false;
            WindowUtils.showEditErr(this, et_last, iv_last, R.string.err_last_empty);
        }

        if (pass.length() > 0){
            if(pass.length() > 5){
                bPass = true;
                WindowUtils.hideEditErr(et_pass, iv_pass);
            } else{
                bPass = false;
                WindowUtils.showEditErr(this, et_pass, iv_pass, R.string.err_pass_length);
            }
        } else {
            bPass = false;
            WindowUtils.showEditErr(this, et_pass, iv_pass, R.string.err_pass_length);
        }

        if (confirm.length() > 0){
            if(pass.equals(confirm) && bPass){
                bPass = true;
                WindowUtils.hideEditErr(et_confirm, iv_confirm);
            } else{
                bPass = false;
                WindowUtils.showEditErr(this, et_confirm, iv_confirm, R.string.err_pass_retype);
            }

        } else {
            bPass = false;
            WindowUtils.showEditErr(this, et_confirm, iv_confirm, R.string.err_pass_retype);
        }

        return bEmail && bfirst && blast && bPass;
    }
}
