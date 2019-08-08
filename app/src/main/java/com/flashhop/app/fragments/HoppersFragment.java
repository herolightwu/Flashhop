package com.flashhop.app.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class HoppersFragment extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap mHopperMap;
    Context context;
    HomeActivity parent;

    View root_view;
    ImageView iv_back, iv_myloc;
    Button btn_group_chat;
    CardView cv_location;
    public EventModel ev = new EventModel();
    int pre_HomeType = Const.HOME_HOME;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_hoppers, container, false);
        context = getContext();
        parent = (HomeActivity) getActivity();
        pre_HomeType = MyApp.home_type;
        iv_back = root_view.findViewById(R.id.hoppers_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pre_HomeType == Const.HOME_MSG){
                    parent.setNavViewVisible(false);
                }
                dismiss();
            }
        });
        btn_group_chat = root_view.findViewById(R.id.hoppers_frag_btn_group_chat);
        btn_group_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoGroupChat();
            }
        });
        cv_location = root_view.findViewById(R.id.hoppers_frag_my_location);

        iv_myloc = root_view.findViewById(R.id.hoppers_frag_iv_myloc);
        iv_myloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_myloc.setColorFilter(ContextCompat.getColor(context, R.color.colorBlue), android.graphics.PorterDuff.Mode.MULTIPLY);
                if(mHopperMap != null && MyApp.curLoc != null){
                    LatLng myLoc = new LatLng(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
                    mHopperMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 14));
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.hoppers_frag_map);
        mapFragment.getMapAsync(this);
        return root_view;
    }

    private void gotoGroupChat(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out);
        GroupChatFragment group_frag = new GroupChatFragment();
        group_frag.group_event = ev;
        fragmentTransaction.add(R.id.home_frame, group_frag, Const.FRAG_CHAT_GROUP).addToBackStack(Const.FRAG_CHAT_GROUP).commit();
    }

    private void refreshLayout(){

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mHopperMap = googleMap;
        mHopperMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mHopperMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        mHopperMap.getUiSettings().setMyLocationButtonEnabled(false);
        mHopperMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
        LatLng myLoc = new LatLng(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
        mHopperMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 14));
        mHopperMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                showUsers();
            }
        });
        mHopperMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                iv_myloc.setColorFilter(ContextCompat.getColor(context, R.color.colorBlack), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
        });
    }

    private void showUsers(){
        if(ev.creator.lat != 0.0 && ev.creator.lng != 0.0){
            LatLng loc = new LatLng(ev.creator.lat, ev.creator.lng);
            String sTag = "hopper-0";
            mHopperMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(
                    WindowUtils.createCreatorMarker(getContext(), ev.creator.photo_url)))).setTag(sTag);
        }

        for(int i = 0; i < ev.members.size(); i++){
            UserModel one = ev.members.get(i);
            if(!one.uid.equals(MyApp.curUser.uid) && one.lat != 0.0 && one.lng != 0.0){
                LatLng loc = new LatLng(one.lat, one.lng);
                String sTag = String.format("hopper-%d", i+1);
                mHopperMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(
                        WindowUtils.createCustomMarker(getContext(), one.photo_url)))).setTag(sTag);
            }
        }

        String[] sLoc = ev.loc.split(",");
        double lat = Double.parseDouble(sLoc[0]);
        double lng = Double.parseDouble(sLoc[1]);
        LatLng loc = new LatLng(lat, lng);
        mHopperMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));

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
