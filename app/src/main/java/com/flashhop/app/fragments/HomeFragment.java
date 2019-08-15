package com.flashhop.app.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.AddressListAdapter;
import com.flashhop.app.models.AddressModel;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.KeyboardUtil;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.flashhop.app.utils.Const.APP_TAG;


public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    //GoogleApiClient mGoogleApiClient;
    Context context;
    HomeActivity parent;

    RelativeLayout rl_outside;
    LinearLayout ll_profile, ll_pin, ll_address, ll_drop, ll_curLoc;
    EditText et_search;
    Button btn_event_list;
    RecyclerView addr_recycler;
    ImageView iv_add_event, iv_filter, iv_back, iv_myloc;
    CircleImageView civ_user;
    CardView cv_location, cv_pin;

    AddressListAdapter addrAdapter;
    List<AddressModel> addr_list = new ArrayList<>();
    LatLng centerLoc = null;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rView = inflater.inflate(R.layout.fragment_home, container, false);

        context = getContext();
        parent = (HomeActivity) getActivity();

        rl_outside = rView.findViewById(R.id.home_frag_rl_outside);
        rl_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtil.hideKeyboard(parent);
            }
        });
        ll_profile = rView.findViewById(R.id.home_frag_ll_user);
        ll_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.showProfile();
            }
        });
        civ_user = rView.findViewById(R.id.home_frag_civ_user);
        Picasso.get()
                .load(MyApp.curUser.photo_url)
                .into(civ_user);

        ll_pin = rView.findViewById(R.id.home_frag_ll_pin);
        ll_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(ll_address.getVisibility() == View.GONE){
                cv_pin.setVisibility(View.GONE);
                cv_location.setVisibility(View.GONE);
                    pinMyLocation();
                //}
            }
        });
        cv_pin = rView.findViewById(R.id.home_frag_cv_pin);

        iv_add_event = rView.findViewById(R.id.home_frag_iv_add);
        iv_add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.showHostEvent();
            }
        });

        iv_filter = rView.findViewById(R.id.home_frag_iv_filter);
        iv_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.showFilterFrag();
            }
        });

        ll_address = rView.findViewById(R.id.home_frag_ll_address);
        ll_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtil.hideKeyboard(parent);
            }
        });
        ll_address.setVisibility(View.GONE);
        ll_drop = rView.findViewById(R.id.home_frag_ll_drop);
        ll_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(ll_address.getVisibility() == View.VISIBLE){
                    ll_address.animate()
                            .translationYBy(0)
                            .translationY(ll_address.getHeight())
                            .alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    ll_address.setVisibility(View.GONE);
                                    cv_pin.setVisibility(View.VISIBLE);
                                    cv_location.setVisibility(View.VISIBLE);
                                }
                            });
                    KeyboardUtil.hideKeyboard(parent);
                showUsersAndEvents();
                //}
            }
        });
        ll_curLoc = rView.findViewById(R.id.home_frag_pin_current);
        ll_curLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyApp.curLoc != null){
                    MyApp.selLoc = new LatLng(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
                    MyApp.selAddr = MyApp.myAddr;
                    displayChooseLocation();
                    updateMyLocation();
                }
            }
        });

        cv_location = rView.findViewById(R.id.home_frag_my_location);

        et_search = rView.findViewById(R.id.home_frag_et_search);
        addr_recycler = rView.findViewById(R.id.home_frag_recycler_address);
        LinearLayoutManager ll_m = new LinearLayoutManager(context);
        addr_recycler.setLayoutManager(ll_m);
        addrAdapter = new AddressListAdapter(addr_list, context);
        addr_recycler.setAdapter(addrAdapter);

        addrAdapter.setOnItemClickListener(new AddressListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                String place_id = addr_list.get(pos).place_id;
                getPlaceDetails(place_id);
                return 0;
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getAutoCompletePlaces(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_event_list = rView.findViewById(R.id.home_frag_btn_events_list);
        btn_event_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(parent.event_list.size() > 0)
                    showEventListFrag();
            }
        });

        iv_myloc = rView.findViewById(R.id.home_frag_iv_myloc);
        iv_myloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_myloc.setColorFilter(ContextCompat.getColor(context, R.color.colorBlue), android.graphics.PorterDuff.Mode.MULTIPLY);
                if(mMap != null && MyApp.curLoc != null){
                    centerLoc = new LatLng(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLoc, 14.0f));
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.home_frag_map);
        mapFragment.getMapAsync(this);

        //final ViewGroup googleLogo = (ViewGroup) rView.findViewById(R.id.home_frag_map).findViewWithTag(Const.GOOGLE_MAPS_WATERMARK).getParent();
        //googleLogo.setVisibility(View.GONE);
        parent.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rView;
    }

    public void pinMyLocation(){
        ll_address.animate()
                .translationYBy(ll_address.getHeight())
                .translationY(0).alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        ll_address.setVisibility(View.VISIBLE);
                        ll_address.setAlpha(0.0f);
                    }
                });
    }

    private void displayChooseLocation(){
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(MyApp.selLoc).
                icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mypin))).setTag("SelPin");
        centerLoc = new LatLng(MyApp.selLoc.latitude - 0.01, MyApp.selLoc.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLoc, 14.0f));
    }

    public boolean moveEventLocation(String sLoc){
        String[] str_loc = sLoc.split(",");
        double lat = Double.parseDouble(str_loc[0]);
        double lng = Double.parseDouble(str_loc[1]);
        LatLng eventLoc = new LatLng(lat, lng);
        if(mMap != null)
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLoc, 14.0f));
            return true;
        }
        return false;
    }

    @Override
    public void onResume(){
        super.onResume();
        parent.filterDataFromServer();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));

        if(MyApp.selLoc != null && MyApp.selAddr.length() > 0 ){
            centerLoc = new LatLng(MyApp.selLoc.latitude, MyApp.selLoc.longitude);
        } else{
            centerLoc = new LatLng(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLoc, 14));

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                getAddress(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
                centerLoc = new LatLng(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
                showUsersAndEvents();

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String sTag = (String) marker.getTag();
                String[] tags = sTag.split("-");
                int index = 0;
                if(tags[0].equals("user")){
                    index = Integer.valueOf(tags[1]);
                    UserModel other = parent.user_list.get(index);
                    showUserProfile(other);

                } else if(tags[0].equals("event")){
                    index = Integer.valueOf(tags[1]);
                    EventModel eve = parent.event_list.get(index);
                    if(eve.creator.uid.equals(MyApp.curUser.uid)){
                        showPublishEvent(eve);
                    } else{
                        showEventProperty(eve);
                    }
                }
                return true;
            }
        });
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                iv_myloc.setColorFilter(ContextCompat.getColor(context, R.color.colorBlack), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
        });
    }

    public void showUsersAndEvents(){
        if(mMap == null) return;
        int mode = parent.view_mode;
        if(mode == Const.VIEW_FILTER_EVENT){
            btn_event_list.setVisibility(View.VISIBLE);
        } else {
            btn_event_list.setVisibility(View.GONE);
        }

        mMap.clear();
        if(mode == Const.VIEW_FILTER_EVENT){
            displayEventsOnMap();
        } else if(mode == Const.VIEW_FILTER_PEOPLE){
            displayUsersOnMap();
        } else{
            displayEventsOnMap();
            displayUsersOnMap();
        }

        if(MyApp.selLoc != null){
            String sUpdate = MyApp.curUser.location_updated_at;
            long diff = TxtUtils.getDifferenceTime(sUpdate);
            if(Math.abs(diff) < 60){
                mMap.addMarker(new MarkerOptions().position(MyApp.selLoc).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mypin))).setTag("SelPin");
            }
        }
        /*if(centerLoc != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLoc, 14));
        }*/
    }

    private void displayUsersOnMap(){
        List<UserModel> users = parent.user_list;
        for(int j = 0; j < users.size(); j++){
            UserModel one = users.get(j);
            String sUpdate = one.location_updated_at;
            long diff = TxtUtils.getDifferenceTime(sUpdate);
            if(one.hide_my_location != 1 && Math.abs(diff) < 60){
                LatLng loc = new LatLng(one.lat, one.lng);
                String sTag = String.format("user-%d", j);
                mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(
                        WindowUtils.createCustomMarker(context, one.photo_url)))).setTag(sTag);
            }
        }
    }

    private void displayEventsOnMap(){
        List<EventModel> events = parent.event_list;
        for(int i = 0; i < events.size(); i++){
            EventModel one = events.get(i);
            //long diff = TxtUtils.getDifferenceTime(one.date, one.time);
            if(one.loc.length() > 0 ){//&& diff > 30
                String[] sLoc = one.loc.split(",");
                double lat = Double.parseDouble(sLoc[0]);
                double lng = Double.parseDouble(sLoc[1]);
                LatLng loc = new LatLng(lat, lng);
                String sTag = String.format("event-%d", i);
                mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mark))).setTag(sTag);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void showPublishEvent(EventModel event){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        PublishEventFragment pubeventFrag = new PublishEventFragment();
        pubeventFrag.pub_Event = event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        transaction.add(R.id.home_frame, pubeventFrag, Const.FRAG_PUBLISH_EVENT_TAG).addToBackStack(Const.FRAG_PUBLISH_EVENT_TAG).commit();
    }

    public void showEventProperty(EventModel event){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        EventPropertyFragment propFrag = new EventPropertyFragment();
        propFrag.event = event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        transaction.add(R.id.home_frame, propFrag, "EVENT_PROP_FRAG").addToBackStack("EVENT_PROP_FRAG").commit();
    }

    public void showUserProfile(UserModel other){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        UserProfileFragment userFrag = new UserProfileFragment();
        userFrag.other = other;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        transaction.add(R.id.home_frame, userFrag, "USER_FRAG").addToBackStack("USER_FRAG").commit();
    }

    public void showEventListFrag(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        EventListFragment eListFrag = new EventListFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_fade_in, R.anim.ani_fade_out, R.anim.ani_fade_out, R.anim.ani_fade_in);
        transaction.add(R.id.home_frame, eListFrag, Const.FRAG_EVENT_LIST_TAG).addToBackStack(Const.FRAG_EVENT_LIST_TAG).commit();
    }

    private void showLikeDlg(final UserModel ulike){
        TextView tv_desc;
        Button btn_hell, btn_yet, btn_too;
        ImageView iv_icon;
        CircleImageView iv_photo;
        final Dialog dialog = new Dialog(context, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_like_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = dialog.findViewById(R.id.custom_like_tv_desc);
        btn_hell = dialog.findViewById(R.id.custom_like_btn_hell);
        btn_yet = dialog.findViewById(R.id.custom_like_btn_yet);
        btn_too = dialog.findViewById(R.id.custom_like_btn_too);
        iv_photo = dialog.findViewById(R.id.custom_like_iv_photo);
        iv_icon = dialog.findViewById(R.id.custom_like_iv_icon);

        Picasso.get()
                .load(ulike.photo_url)
                .resize(100, 100)
                .into(iv_photo);

        String msg = String.format(getActivity().getString(R.string.user_liked_you), ulike.first_name);
        tv_desc.setText(msg);
        dialog.show();

        btn_hell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
            }
        });

        btn_yet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_too.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showLikeTooDlg(ulike);
            }
        });

    }

    private void showLikeTooDlg(UserModel uToo){
        TextView tv_desc;
        Button btn_hell, btn_yet, btn_too;
        ImageView iv_icon;
        CircleImageView iv_photo;
        final Dialog dialog = new Dialog(context, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_like_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = dialog.findViewById(R.id.custom_like_tv_desc);
        btn_hell = dialog.findViewById(R.id.custom_like_btn_hell);
        btn_yet = dialog.findViewById(R.id.custom_like_btn_yet);
        btn_too = dialog.findViewById(R.id.custom_like_btn_too);
        iv_photo = dialog.findViewById(R.id.custom_like_iv_photo);
        iv_icon = dialog.findViewById(R.id.custom_like_iv_icon);

        String msg = String.format(getActivity().getString(R.string.like_too_sweet), uToo.first_name);
        tv_desc.setText(msg);
        iv_photo.setVisibility(View.GONE);
        btn_hell.setVisibility(View.GONE);
        btn_yet.setVisibility(View.GONE);
        btn_too.setVisibility(View.GONE);
        dialog.show();

    }

    private void showDissDlg(final UserModel uDiss){
        TextView tv_desc;
        Button btn_hell, btn_yet, btn_too;
        ImageView iv_photo, iv_icon;
        final Dialog dialog = new Dialog(context, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_like_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = dialog.findViewById(R.id.custom_like_tv_desc);
        btn_hell = dialog.findViewById(R.id.custom_like_btn_hell);
        btn_yet = dialog.findViewById(R.id.custom_like_btn_yet);
        btn_too = dialog.findViewById(R.id.custom_like_btn_too);
        iv_photo = dialog.findViewById(R.id.custom_like_iv_photo);
        iv_icon = dialog.findViewById(R.id.custom_like_iv_icon);

        Picasso.get()
                .load(uDiss.photo_url)
                .resize(100, 100)
                .into(iv_photo);

        String msg = String.format(getActivity().getString(R.string.user_dissed_you), uDiss.first_name);
        tv_desc.setText(msg);
        iv_icon.setImageResource(R.drawable.ic_dissed);
        btn_hell.setText(R.string.diss_whatever);
        btn_yet.setVisibility(View.GONE);
        btn_too.setText(R.string.diss_throw_back);
        dialog.show();

        btn_hell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
            }
        });

        btn_yet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_too.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private String getAddress(double lat, double lng) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                for(int j = 0; j < addresses.size(); j++){
                    Address addr = addresses.get(j);
                    AddressModel one = new AddressModel();
                    String addr_str = addr.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    //String city = addr.getLocality();
                    //String state = addr.getAdminArea();
                    //String country = addr.getCountryName();
                    //String postalCode = addr.getPostalCode();
                    String knownName = addr.getFeatureName(); // Only if available else return NULL
                    one.localAddr = knownName;
                    MyApp.myAddr = addr_str;
                    /*if(MyApp.selLoc == null){
                        MyApp.selAddr = addr_str;
                        MyApp.selLoc = new LatLng(MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude());
                    }*/
                }

            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    private void getPlaceDetails(String pId){
        String API_PATH = String.format(Const.PLACE_DETAIL_API, pId, getString(R.string.google_maps_key));
        AndroidNetworking.get(API_PATH)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            String status = response.getString("status");
                            if(status.equals("OK")){
                                JSONObject result = response.getJSONObject("result");
                                MyApp.selAddr = result.getString("formatted_address");
                                JSONObject geo_obj = result.getJSONObject("geometry");
                                JSONObject loc_obj = geo_obj.getJSONObject("location");
                                double lat = loc_obj.getDouble("lat");
                                double lng = loc_obj.getDouble("lng");
                                MyApp.selLoc = new LatLng(lat, lng);
                                updateMyLocation();
                                displayChooseLocation();
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void updateMyLocation(){
        AndroidNetworking.post(Const.HOST_URL + Const.LOCATION_UPDATE_API)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addBodyParameter("address", MyApp.selAddr)
                .addBodyParameter("lat", MyApp.selLoc.latitude + "")
                .addBodyParameter("lng", MyApp.selLoc.longitude + "")
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Location Update", "Success");
                        long l_time = System.currentTimeMillis()/1000;
                        MyApp.curUser.location_updated_at = String.valueOf(l_time);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Location Update", "failed");
                    }
                });
    }

    private void getAutoCompletePlaces(String prefix){
        String API_PATH = String.format(Const.autocomplete_api_origin, prefix, getString(R.string.google_maps_key));//getString(R.string.my_places_api_key));
        if(MyApp.curLoc != null){
            API_PATH = String.format(Const.autocomplete_api, prefix, MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude(), getString(R.string.google_maps_key));//getString(R.string.my_places_api_key));
        }
        AndroidNetworking.get(API_PATH)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            String status = response.getString("status");
                            if(status.equals("OK")){
                                JSONArray data_array = response.getJSONArray("predictions");
                                addr_list.clear();
                                for(int i = 0; i < data_array.length(); i++){
                                    JSONObject one_obj = data_array.getJSONObject(i);
                                    JSONObject format_obj = one_obj.getJSONObject("structured_formatting");
                                    AddressModel one = new AddressModel();
                                    one.place_id = one_obj.getString("place_id");
                                    one.localAddr = format_obj.getString("main_text");
                                    one.fullAddr = format_obj.getString("secondary_text");
                                    addr_list.add(one);
                                }
                                addrAdapter.setDataList(addr_list);
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
