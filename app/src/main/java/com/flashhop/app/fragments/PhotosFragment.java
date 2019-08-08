package com.flashhop.app.fragments;

import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.adapters.GridAdapter;
import com.flashhop.app.start.WelcomeActivity;
import com.flashhop.app.utils.WindowUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PhotosFragment extends DialogFragment {
    private View root_view;
    private TextView tv_step, tv_upload, tv_edit;
    Button btn_next;
    ImageView iv_back;
    CircleImageView civ_photo;
    LinearLayout ll_preview;
    ArrayList<String> photo_list = new ArrayList<>();
    GridView gridView;
    GridAdapter gridAdapter;

    int status = 0, nSelect = -1;//select

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_photos, container, false);

        int step = ((WelcomeActivity)getActivity()).nStep;
        tv_step = root_view.findViewById(R.id.photo_frag_tv_step);
        String step_str = String.format(getString(R.string.welcome_step), step);
        tv_step.setText(step_str);

        btn_next = root_view.findViewById(R.id.photo_frag_btn_done);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nSelect = gridAdapter.nSelect;
                if(status == 0 && nSelect != -1){
                    Picasso.get()
                            .load(Uri.fromFile(new File(photo_list.get(nSelect))))
                            .resize(200, 200)
                            .into(civ_photo);
                    ll_preview.setVisibility(View.VISIBLE);
                    status = 1;
                    gridView.setVisibility(View.GONE);
                    btn_next.setText(R.string.btn_done);
                    tv_upload.setText(R.string.welcome_preview);
                } else if(status == 1 && nSelect >= 0){
                    ((WelcomeActivity)getActivity()).bm_photo =((BitmapDrawable) civ_photo.getDrawable()).getBitmap();
                    ((WelcomeActivity)getActivity()).process_next();
                } else{
                    WindowUtils.animateView(getContext(), btn_next);
                }

            }
        });

        iv_back = root_view.findViewById(R.id.photo_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((WelcomeActivity)getActivity()).nStep--;
                dismiss();
            }
        });

        tv_upload = root_view.findViewById(R.id.photo_frag_tv_upload);

        gridView = root_view.findViewById(R.id.photo_frag_grid);
        photo_list.addAll(((WelcomeActivity)getActivity()).photo_list);

        gridAdapter = new GridAdapter(getContext(), photo_list);
        gridView.setAdapter(gridAdapter);

        tv_edit = root_view.findViewById(R.id.photo_frag_tv_edit);
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_preview.setVisibility(View.GONE);
                status = 0;
                gridView.setVisibility(View.VISIBLE);
                tv_upload.setText(R.string.welcome_upload_photos);
                btn_next.setText(R.string.btn_select);
            }
        });

        civ_photo = root_view.findViewById(R.id.photo_frag_civ_photo);
        ll_preview = root_view.findViewById(R.id.photo_frag_ll_preview);
        ll_preview.setVisibility(View.GONE);
        btn_next.setText(R.string.btn_select);

        return root_view;
    }

    public void refreshLayout(){

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
