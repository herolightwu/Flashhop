package com.flashhop.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.utils.Const;

public class MineFragment extends Fragment {

    View rootview;
    Button btn_pin, btn_join, btn_host;
    ImageView iv_mine;
    HomeActivity myAct = null;

    public MineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAct = (HomeActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.fragment_mine, container, false);
        initLayout();
        return rootview;
    }

    private void initLayout(){
        iv_mine = rootview.findViewById(R.id.mine_frag_iv_mine);
        iv_mine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showHostingFrag();
            }
        });
        btn_pin = rootview.findViewById(R.id.mine_frag_btn_loc);
        btn_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.home_type = Const.HOME_HOME;
                myAct.displayView();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(myAct != null)
                            myAct.displayPinLocation();
                    }
                }, 1000);
            }
        });
        btn_join = rootview.findViewById(R.id.mine_frag_btn_join);
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.home_type = Const.HOME_HOME;
                myAct.displayView();
            }
        });
        btn_host = rootview.findViewById(R.id.mine_frag_btn_host);
        btn_host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.home_type = Const.HOME_HOME;
                myAct.showHostEvent();
            }
        });
    }

    public void showHostingFrag(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        HostingFragment hostFrag = new HostingFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(R.id.home_frame, hostFrag, Const.FRAG_UPCOMING_TAG).addToBackStack(Const.FRAG_UPCOMING_TAG).commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
