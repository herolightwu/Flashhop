package com.flashhop.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.flashhop.app.utils.Const.APP_TAG;

public class SettingChangeDob extends DialogFragment {

    Context context;
    View root_view;
    ImageView iv_back;
    EditText et_dob;
    TextView tv_limit;
    Button btn_save;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_setting_change_dob, container, false);

        context = getContext();
        initLayout();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.setting_change_dob_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btn_save = root_view.findViewById(R.id.setting_change_dob_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidate()){
                    saveDob();
                }
            }
        });

        tv_limit = root_view.findViewById(R.id.setting_change_dob_tv_limit);
        et_dob = root_view.findViewById(R.id.setting_change_dob_et_date);
        et_dob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_dob.getText().toString();
                int textlength = et_dob.getText().length();

                if(text.endsWith("-"))
                    return;

                if(textlength == 5 || textlength == 8)
                {
                    et_dob.setText(new StringBuilder(text).insert(text.length()-1, "-").toString());
                    et_dob.setSelection(et_dob.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_dob.setText(MyApp.curUser.dob);

    }

    private void refreshLayout(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d = sdf.parse(MyApp.curUser.dob_update);
            Calendar thatDay = Calendar.getInstance();
            thatDay.setTime(d);
            thatDay.add(Calendar.MONTH,6);
            Date ad = thatDay.getTime();
            SimpleDateFormat edf = new SimpleDateFormat("MMMM dd, yyyy");
            String limitdate = edf.format(ad);
            tv_limit.setText(getString(R.string.setting_date_limit) + " " + limitdate);
        } catch (ParseException ex) {
            Log.v("Exception", ex.getLocalizedMessage());
        }

        if(MyApp.curUser.bDobEnable == 1){
            tv_limit.setVisibility(View.GONE);
            btn_save.setVisibility(View.VISIBLE);
            et_dob.setFocusable(true);
            et_dob.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        } else{
            tv_limit.setVisibility(View.VISIBLE);
            btn_save.setVisibility(View.GONE);
            et_dob.setFocusable(false);
            et_dob.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }
    }

    private void saveDob(){
        final String birth_str = et_dob.getText().toString().trim();
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Changing...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.UPLOAD_PROFILE_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("dob", birth_str)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try {
                            int bSuccess = response.getInt("success");
                            if (bSuccess == 1) {
                                JSONObject user_obj = response.getJSONObject("data");
                                String sVal = user_obj.getString("last_dob_updated_at");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.dob_update = sVal;
                                MyApp.curUser.bDobEnable = user_obj.getInt("update_dob_enable");
                                MyApp.curUser.dob = user_obj.getString("dob");
                                //MyApp.curUser.dob = birth_str;
                                //MyApp.curUser.bDobEnable = 0;
                                refreshLayout();
                            } else{
                                WindowUtils.animateView(getContext(), btn_save);
                            }
                        } catch(JSONException ex){
                            WindowUtils.animateView(getContext(), btn_save);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), btn_save);
                    }
                });
    }

    private boolean checkValidate(){
        String birth_str = et_dob.getText().toString().trim();
        return TxtUtils.isValidDate(birth_str);
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
