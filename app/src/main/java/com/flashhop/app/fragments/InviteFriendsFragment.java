package com.flashhop.app.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.InviteGridAdapter;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.InviteUserModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.flashhop.app.utils.Const.APP_TAG;

public class InviteFriendsFragment extends DialogFragment {

    Context context;

    private View root_view;
    Button btn_cancel, btn_send;
    EditText et_search;
    TextView tv_users;
    GridView user_grid;
    RelativeLayout rl_outside;
    InviteGridAdapter inviteGridAdapter;
    List<InviteUserModel> user_list = new ArrayList<>();
    public EventModel eData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_invite_friends, container, false);
        context = getContext();
        initLayout();
        return root_view;
    }

    private void refreshLayout(){

    }

    private void initLayout(){
        rl_outside = root_view.findViewById(R.id.invite_frag_rl_outside);
        rl_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
        btn_cancel = root_view.findViewById(R.id.invite_frag_btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_send = root_view.findViewById(R.id.invite_frag_btn_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInvitations();
                dismiss();
            }
        });

        et_search = root_view.findViewById(R.id.invite_frag_et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inviteGridAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tv_users = root_view.findViewById(R.id.invite_frag_tv_names);

        user_grid = root_view.findViewById(R.id.invite_frag_grid_user);
        initData();
        inviteGridAdapter = new InviteGridAdapter(context, user_list);
        user_grid.setAdapter(inviteGridAdapter);
        user_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                user_list.get(i).bChoose = !user_list.get(i).bChoose;
                inviteGridAdapter.setData(user_list);
                setNames();
            }
        });
        tv_users.setText("");
    }

    private void setNames(){
        tv_users.setText("");
        for(int i = 0; i < user_list.size(); i++){
            if(user_list.get(i).bChoose){
                if(tv_users.getText().length() == 0){
                    tv_users.setText(user_list.get(i).name);
                } else{
                    String old_str = tv_users.getText().toString();
                    tv_users.setText(old_str + ", " + user_list.get(i).name);
                }
            }
        }
    }

    private void sendInvitations(){
        String user_ids = "";
        for(int i = 0; i < user_list.size(); i++){
            if(user_list.get(i).bChoose){
                if(user_ids.length() == 0){
                    user_ids = user_list.get(i).userId;
                } else{
                    user_ids = user_ids + "," + user_list.get(i).userId;
                }
            }
        }
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Inviting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.INVITE_FRIENDS_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("user_id_list", user_ids)
                .addBodyParameter("event_id", eData.id)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    private void initData(){
        user_list.clear();
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Friends...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.ALL_FRIENDS_URL)
                .addBodyParameter("user_id", MyApp.curUser.uid)
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
                                JSONArray data_array = response.getJSONArray("data");
                                for(int i = 0; i < data_array.length(); i++){
                                    JSONObject uData = data_array.getJSONObject(i);
                                    InviteUserModel one = new InviteUserModel();
                                    one.name = uData.getString("first_name");
                                    one.bChoose = false;
                                    one.photo = uData.getString("avatar");
                                    one.userId = uData.getString("id");
                                    if(!checkMemberofEvent(one.userId)){
                                        user_list.add(one);
                                    }
                                }
                                inviteGridAdapter.setData(user_list);
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

    private boolean checkMemberofEvent(String uID){
        if(eData.creator != null && eData.creator.uid.equals(uID)){
            return true;
        }
        for(int k = 0; k < eData.members.size(); k++){
            UserModel uu = eData.members.get(k);
            if(uu.uid.equals(uID)){
                return true;
            }
        }
        return false;
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

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)(getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
