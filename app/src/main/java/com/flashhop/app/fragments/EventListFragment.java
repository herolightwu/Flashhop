package com.flashhop.app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.EventListAdapter;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.utils.Const;

import java.util.ArrayList;
import java.util.List;

public class EventListFragment extends DialogFragment {

    private View root_view;
    ImageView iv_back;
    RecyclerView recyclerView;
    EventListAdapter eventListAdapter;
    List<EventModel> event_list = new ArrayList<>();

    SaveSharedPrefrence saveSharedPrefrence;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        saveSharedPrefrence = new SaveSharedPrefrence();
        root_view = inflater.inflate(R.layout.fragment_event_list, container, false);
        initLayout();
        return root_view;
    }

    private void initLayout(){
        iv_back = root_view.findViewById(R.id.event_list_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        event_list.addAll(((HomeActivity)getActivity()).event_list);
        recyclerView = root_view.findViewById(R.id.event_list_frag_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        eventListAdapter = new EventListAdapter(event_list, getContext());
        recyclerView.setAdapter(eventListAdapter);
        eventListAdapter.setOnItemClickListener(new EventListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                EventModel one = event_list.get(pos);
                if(one.creator.uid.equals(MyApp.curUser.uid)){
                    showPublishEvent(one);
                } else{
                    showEventProperty(one);
                }
                return pos;
            }
        });
    }

    public void showPublishEvent(EventModel event){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        PublishEventFragment pubeventFrag = new PublishEventFragment();
        pubeventFrag.pub_Event = event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        transaction.add(R.id.home_frame, pubeventFrag, Const.FRAG_PUBLISH_EVENT_TAG).addToBackStack(Const.FRAG_PUBLISH_EVENT_TAG).commit();
    }

    public void showEventProperty(EventModel event){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        EventPropertyFragment propFrag = new EventPropertyFragment();
        propFrag.event = event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_out_up, R.anim.ani_slide_out_down);
        transaction.add(R.id.home_frame, propFrag, "EVENT_PROP_FRAG").addToBackStack("EVENT_PROP_FRAG").commit();
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
