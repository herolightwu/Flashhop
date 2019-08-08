package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.flashhop.app.utils.Const.APP_TAG;

public class ReportDlgFrag extends DialogFragment {

    private View root_view;
    ImageView iv_back;
    Button btn_report;
    EditText et_content, et_subject;
    AppCompatSpinner sp_subject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_report, container, false);

        btn_report = root_view.findViewById(R.id.report_frag_btn_report);
        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportContent();
            }
        });

        iv_back = root_view.findViewById(R.id.report_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        et_content = root_view.findViewById(R.id.report_frag_et_content);
        //et_subject = root_view.findViewById(R.id.report_frag_et_subject);
        sp_subject = root_view.findViewById(R.id.report_frag_sp_subject);
        List<String> cate_list = new ArrayList<String>();
        cate_list.add("Subject");
        cate_list.add("Admin");
        cate_list.add("User");
        cate_list.add("Event");
        ArrayAdapter<String> cateAdapter = new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, cate_list);
        cateAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        sp_subject.setAdapter(cateAdapter);

        return root_view;
    }

    private void reportContent(){
        /*if(et_subject.getText().toString().length() == 0){
            WindowUtils.animateView(getContext(), et_subject);
            return;
        }*/
        if(sp_subject.getSelectedItemPosition() == 0){
            WindowUtils.animateView(getContext(), sp_subject);
            return;
        }
        if(et_content.getText().toString().length() == 0){
            WindowUtils.animateView(getContext(), et_content);
            return;
        }

        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Reporting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.REPORT_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("category", (String)sp_subject.getSelectedItem())
                .addBodyParameter("content", et_content.getText().toString())
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

                            } else{
                                WindowUtils.animateView(getContext(), btn_report);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        WindowUtils.animateView(getContext(), btn_report);
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
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
