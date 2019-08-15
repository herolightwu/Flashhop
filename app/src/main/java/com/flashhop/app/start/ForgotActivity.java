package com.flashhop.app.start;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.R;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import static com.flashhop.app.utils.Const.APP_TAG;

public class ForgotActivity extends AppCompatActivity {

    ImageView iv_back, iv_email_err;
    EditText et_email;
    Button btn_reset;
    TextView tv_sent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_forgot);

        initLayout();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    private void initLayout(){
        iv_back = findViewById(R.id.forgot_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        iv_email_err = findViewById(R.id.forgot_iv_email_err);
        et_email = findViewById(R.id.forgot_et_email);
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
                    WindowUtils.hideEditErr(et_email, iv_email_err);
                } else {
                    WindowUtils.showEditErr(ForgotActivity.this, et_email, iv_email_err, R.string.err_email_invalid);
                }
            }
        });

        btn_reset = findViewById(R.id.forgot_btn_reset);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidFields()){
                    requestResetPassword();

                    String email = et_email.getText().toString().trim();
                    String desc_str = String.format(getString(R.string.forgot_sent_text), email);
                    tv_sent.setText(desc_str);
                    tv_sent.setVisibility(View.VISIBLE);
                    btn_reset.setEnabled(false);
                }

            }
        });

        tv_sent = findViewById(R.id.forgot_tv_sent);
        tv_sent.setVisibility(View.INVISIBLE);

    }

    private void requestResetPassword(){
        final String email = et_email.getText().toString().trim();
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Verifying...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.FORGOT_URL)
                .addBodyParameter("email", email)
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
                                //String stoken = data_obj.getString("token");
                                Intent intent = new Intent(ForgotActivity.this, ResetActivity.class);
                                //intent.putExtra("token", stoken);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        hud.dismiss();
                        Toast.makeText(getApplicationContext(), error.getErrorBody(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean checkValidFields(){
        boolean bEmail = false;
        String email = et_email.getText().toString().trim();
        if (email.length() > 0){
            if(TxtUtils.isEmailValid(email)){
                bEmail = true;
                WindowUtils.hideEditErr(et_email, iv_email_err);
            } else{
                bEmail = false;
                WindowUtils.showEditErr(this, et_email, iv_email_err, R.string.err_email_invalid);
            }
        } else {
            bEmail = false;
            WindowUtils.showEditErr(this, et_email, iv_email_err, R.string.err_email_invalid);
        }

        return bEmail;
    }
}
