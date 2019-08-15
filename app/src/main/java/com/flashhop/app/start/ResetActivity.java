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
import com.flashhop.app.R;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flashhop.app.utils.Const.APP_TAG;

public class ResetActivity extends AppCompatActivity {

    EditText et_pass, et_retype, et_code;
    ImageView iv_pass_err, iv_retype, iv_code;
    Button btn_save;
    String sEmail = "";//sToken = ""

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_reset);
        //sToken = getIntent().getStringExtra("token");
        sEmail = getIntent().getStringExtra("email");

        initLayout();
    }

    private void initLayout(){
        iv_pass_err = findViewById(R.id.reset_iv_pass_err);
        iv_retype = findViewById(R.id.reset_iv_retype_err);
        iv_code = findViewById(R.id.reset_iv_code_err);

        et_pass = findViewById(R.id.reset_et_password);
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
                    WindowUtils.showEditErr(ResetActivity.this, et_pass, iv_pass_err, R.string.err_pass_length);
                }
            }
        });
        et_retype = findViewById(R.id.reset_et_retype);
        et_retype.addTextChangedListener(new TextWatcher() {
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
                    WindowUtils.hideEditErr(et_retype, iv_retype);
                } else {
                    WindowUtils.showEditErr(ResetActivity.this, et_retype, iv_retype, R.string.err_pass_retype);
                }
            }
        });

        et_code = findViewById(R.id.reset_et_code);
        et_code.addTextChangedListener(new TextWatcher() {
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
                    WindowUtils.hideEditErr(et_code, iv_code);
                }
            }
        });

        btn_save = findViewById(R.id.reset_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidFields()){
                    doReset();
                } else{
                    WindowUtils.animateView(ResetActivity.this, btn_save);
                }
            }
        });
    }

    private void doReset(){
        String sPass = et_pass.getText().toString().trim();
        String sCode = et_code.getText().toString().trim();
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Verifying...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.RESET_URL)
                .addBodyParameter("email", sEmail)
                .addBodyParameter("password", sPass)
                .addBodyParameter("token", sCode)
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
                                Intent intent = new Intent(ResetActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else{
                                WindowUtils.animateView(ResetActivity.this, btn_save);
                                WindowUtils.showEditErr(ResetActivity.this, et_code, iv_code, R.string.err_wrong_code);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        WindowUtils.animateView(ResetActivity.this, btn_save);
                        WindowUtils.showEditErr(ResetActivity.this, et_code, iv_code, R.string.err_wrong_code);
                    }
                });
    }

    private boolean checkValidFields(){
        boolean bRetype = false, bPass = false, bCode = false;
        String sRetype = et_retype.getText().toString().trim();
        String sPass = et_pass.getText().toString().trim();
        String sCode = et_code.getText().toString().trim();

        if (sPass.length() > 0){
            if(sPass.length() > 5){
                bPass = true;
                WindowUtils.hideEditErr(et_pass, iv_pass_err);
            } else{
                bPass = false;
                WindowUtils.showEditErr(this, et_pass, iv_pass_err, R.string.err_pass_length);
            }
        } else {
            bPass = false;
            WindowUtils.showEditErr(this, et_pass, iv_pass_err, R.string.err_pass_length);
        }

        if (sRetype.length() > 0){
            if(sRetype.length() > 5){
                bRetype = true;
                WindowUtils.hideEditErr(et_retype, iv_retype);
            } else{
                bRetype = false;
                WindowUtils.showEditErr(this, et_retype, iv_retype, R.string.err_pass_retype);
            }
        } else {
            bRetype = false;
            WindowUtils.showEditErr(this, et_retype, iv_retype, R.string.err_pass_retype);
        }

        if(!sPass.equals(sRetype)){
            bRetype = false;
            WindowUtils.showEditErr(this, et_retype, iv_retype, R.string.err_pass_retype);;
        }

        if (sCode.length() > 0){
            bCode = true;
            WindowUtils.hideEditErr(et_code, iv_code);
        } else {
            bCode = false;
            WindowUtils.showEditErr(this, et_code, iv_code, R.string.err_wrong_code);
        }

        return bRetype && bPass && bCode;
    }
}
