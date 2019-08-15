package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.start.WelcomeActivity;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;

public class BirthdayFragment extends DialogFragment {
    private View root_view;
    private TextView tv_step;
    EditText et_birth;
    Button btn_next;
    ImageView iv_skip, iv_back;
    int textlength = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_birthday, container, false);

        int step = ((WelcomeActivity)getActivity()).nStep;
        tv_step = root_view.findViewById(R.id.birth_frag_tv_step);
        String step_str = String.format(getString(R.string.welcome_step), step);
        tv_step.setText(step_str);

        btn_next = root_view.findViewById(R.id.birth_frag_btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidate()){
                    ((WelcomeActivity)getActivity()).birthday = et_birth.getText().toString();
                    ((WelcomeActivity)getActivity()).process_next();
                } else{
                    WindowUtils.animateView(getContext(), btn_next);
                }

            }
        });

        iv_skip = root_view.findViewById(R.id.birth_frag_iv_skip);
        iv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidate()){
                    ((WelcomeActivity)getActivity()).birthday = et_birth.getText().toString();
                    ((WelcomeActivity)getActivity()).process_next();
                } else{
                    WindowUtils.animateView(getContext(), btn_next);
                }
            }
        });

        iv_back = root_view.findViewById(R.id.birth_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).nStep--;
                dismiss();
            }
        });

        et_birth = root_view.findViewById(R.id.birth_frag_et_date);
        et_birth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_birth.getText().toString();
                textlength = et_birth.getText().length();

                if(text.endsWith("-"))
                    return;

                if(textlength == 5 || textlength == 8)
                {
                    et_birth.setText(new StringBuilder(text).insert(text.length()-1, "-").toString());
                    et_birth.setSelection(et_birth.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return root_view;
    }

    private boolean checkValidate(){
        String birth_str = et_birth.getText().toString().trim();
        return TxtUtils.isValidDate(birth_str);
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
