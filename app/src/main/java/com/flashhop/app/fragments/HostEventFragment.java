package com.flashhop.app.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.AddressListAdapter;
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.models.AddressModel;
import com.flashhop.app.models.CardModel;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.ImageFilePath;
import com.flashhop.app.utils.TxtUtils;
import com.flashhop.app.utils.WindowUtils;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.drakeet.support.toast.ToastCompat;

import static com.flashhop.app.utils.Const.APP_TAG;
import static com.flashhop.app.utils.Const.autocomplete_api;

public class HostEventFragment extends DialogFragment {

    Context context;
    HomeActivity parent;

    LinearLayout ll_outside;
    ImageView iv_back, iv_photo;
    AppCompatSpinner sp_category;
    Button btn_draft, btn_publish, btn_change;
    RangeSeekBar sb_people, sb_age;
    TextView tv_people, tv_age, tv_max_people, tv_max_age;
    EditText  et_title, et_desc, et_price, et_cate_err;
    EditText actv_addr;
    TextView et_date, et_time;
    CheckBox cb_coed, cb_boys, cb_girls, cb_follow, cb_invitation, cb_later;
    Spinner sp_currency;
    String sTime, eTime = "";
    boolean bSelPhoto = false;
    RecyclerView suggest_rec;
    AddressListAdapter addrAdapter;
    List<AddressModel> addr_list = new ArrayList<>();
    public boolean bChooseAddr = false, bOpen = true;//,

    int nCate = 0, ngender = 0;
    Date selTime = new Date();
    MySQLiteHelper mHelper;

    public EventModel draftEvent = new EventModel();

    public HostEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = MySQLiteHelper.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root_view = inflater.inflate(R.layout.fragment_host_event, container, false);
        context = getContext();
        parent = (HomeActivity)getActivity();
        initLayout(root_view);
        return root_view;
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
        setDebitCard();
    }

    public void refreshLayout(){
        if((draftEvent.id != null && draftEvent.id.length() > 0) || draftEvent.db_id != -1){
            for(int i = 0; i < 9; i++){
                if(Const.interest_list[i].toLowerCase().equals(draftEvent.category.toLowerCase())){
                    nCate = i+1;
                    break;
                }
            }
            String path = parent.event_photo;
            if(bSelPhoto && path != null && path.length() > 0){
                Picasso.get()
                        .load(Uri.fromFile(new File(path)))
                        .into(iv_photo);
            } else if(draftEvent.photo.length() > 0){
                if(draftEvent.photo.contains("http")){
                    Picasso.get()
                            .load(draftEvent.photo)
                            .into(iv_photo);
                } else{
                    Picasso.get()
                            .load(Uri.fromFile(new File(draftEvent.photo)))
                            .into(iv_photo);
                }

            } else{
                iv_photo.setImageResource(Const.cover_res[nCate]);
            }
            sp_category.setSelection(nCate);
            et_title.setText(draftEvent.title);
            et_date.setText(draftEvent.date);
            et_time.setText(draftEvent.time);
            String[] sss = draftEvent.time.split("-");
            sTime = sss[0].trim();
            if(sss.length == 1){
                eTime = "";
            } else{
                eTime = sss[1].trim();
            }
            et_desc.setText(draftEvent.desc);
            actv_addr.setText(draftEvent.address);
            bChooseAddr = true;
            String[] sAge = draftEvent.age.split(",");
            int age_min = Integer.valueOf(sAge[0]);
            int age_max = Integer.valueOf(sAge[1]);
            sb_age.setProgress(age_min, age_max);
            String[] sPeop = draftEvent.people.split(",");
            int peo_min = Integer.valueOf(sPeop[0]);
            int peo_max = Integer.valueOf(sPeop[1]);
            sb_people.setProgress(peo_min, peo_max);

            if(draftEvent.is_pay_later == 0){
                cb_later.setChecked(false);
            } else{
                cb_later.setChecked(true);
            }
            if(draftEvent.gender == 0){
                cb_coed.setChecked(true);
                cb_boys.setChecked(false);
                cb_girls.setChecked(false);
            } else if(draftEvent.gender == 1){
                cb_coed.setChecked(false);
                cb_boys.setChecked(true);
                cb_girls.setChecked(false);
            } else{
                cb_coed.setChecked(false);
                cb_boys.setChecked(false);
                cb_girls.setChecked(true);
            }
            ngender = draftEvent.gender;
            if(draftEvent.invitaion == 1){
                cb_invitation.setChecked(true);
            } else{
                cb_invitation.setChecked(false);
            }
            if(draftEvent.followable == 1){
                cb_follow.setChecked(true);
            } else{
                cb_follow.setChecked(false);
            }

            et_price.setText(draftEvent.price + "");
            if(!draftEvent.price.equals("0")){
                cb_later.setVisibility(View.VISIBLE);
            } else{
                cb_later.setVisibility(View.INVISIBLE);
            }
            if(draftEvent.currency.equals("CAD")){
                sp_currency.setSelection(0);
            } else{
                sp_currency.setSelection(1);
            }

            if(draftEvent.is_pay_later == 1){
                cb_later.setChecked(true);
            }
        } else{
            String path = parent.event_photo;
            if(path != null && path.length() > 0){
                Picasso.get()
                        .load(Uri.fromFile(new File(path)))
                        .into(iv_photo);
                //iv_photo.setImageBitmap(BitmapFactory.decodeFile(path));
            } else{
                if(nCate > 0)
                    iv_photo.setImageResource(Const.cover_res[nCate]);
            }

            /*if(MyApp.selAddr.length() > 0 && MyApp.selLoc != null){
                bChooseAddr = true;
                draftEvent.address = MyApp.selAddr;
                actv_addr.setText(MyApp.selAddr);
                draftEvent.loc = MyApp.selLoc.latitude + "," + MyApp.selLoc.longitude;
            } else{
                bChooseAddr = true;
                draftEvent.address = MyApp.myAddr;
                actv_addr.setText(MyApp.myAddr);
                draftEvent.loc = MyApp.curLoc.getLatitude() + "," + MyApp.curLoc.getLongitude();
            }*/

        }

        bOpen = true;
        et_title.setFocusable(true);
        suggest_rec.setVisibility(View.GONE);
    }

    private void makeEventModel(){
        draftEvent.title = et_title.getText().toString();
        draftEvent.gender = ngender;
        String path = parent.event_photo;
        if(bSelPhoto && path != null && path.length() > 0){
            draftEvent.photo = path;
        }
        draftEvent.category = Const.interest_list[nCate-1].toLowerCase();
        if(cb_follow.isChecked()){
            draftEvent.followable = 1;
        } else{
            draftEvent.followable = 0;
        }
        if(cb_invitation.isChecked()){
            draftEvent.invitaion= 1;
        } else{
            draftEvent.invitaion = 0;
        }
        draftEvent.age = (int)sb_age.getLeftSeekBar().getProgress() + "," + (int)sb_age.getRightSeekBar().getProgress();
        draftEvent.people = (int)sb_people.getLeftSeekBar().getProgress() + "," + (int)sb_people.getRightSeekBar().getProgress();
        draftEvent.date = et_date.getText().toString();
        draftEvent.time = et_time.getText().toString();
        draftEvent.address = actv_addr.getText().toString();
        draftEvent.desc = et_desc.getText().toString();
        draftEvent.currency = (String)sp_currency.getSelectedItem();
        draftEvent.price = et_price.getText().toString();
        draftEvent.is_pay_later = 0;
        if(cb_later.isChecked()){
            draftEvent.is_pay_later = 1;
        }
    }

    private void saveDraftEvent(){
        draftEvent.db_id = (int)mHelper.putEvent(draftEvent);
        draftEvent.creator = MyApp.curUser;
    }

    private void initLayout(View rView){
        ll_outside = rView.findViewById(R.id.hevent_frag_ll_outside);
        ll_outside.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
        et_desc = rView.findViewById(R.id.hevent_frag_et_desc);
        et_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    et_desc.setTypeface(MyApp.myfont_normal);
                    et_desc.setHintTextColor(ContextCompat.getColor(context, R.color.colorGray));
                    et_desc.setHint(R.string.event_280_maximum);
                } else{
                    et_desc.setTypeface(MyApp.myfont_italic);
                    et_desc.setHintTextColor(ContextCompat.getColor(context, R.color.colorRed));
                    et_desc.setHint(R.string.err_desc);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_desc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        iv_photo = rView.findViewById(R.id.hevent_frag_iv_photo);
        iv_back = rView.findViewById(R.id.hevent_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidFields() && (draftEvent.id.length() == 0 && draftEvent.db_id == -1)){
                    showConfirmDlg();
                } else{
                    dismiss();
                    HomeFragment myFragment = (HomeFragment)parent.getSupportFragmentManager().findFragmentByTag(Const.FRAG_HOME_TAG);
                    if (myFragment != null && myFragment.isVisible()) {
                        parent.filterDataFromServer();
                    }

                }
            }
        });

        btn_draft = rView.findViewById(R.id.hevent_frag_btn_draft);
        btn_draft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkValidFields()){
                    makeEventModel();
                    saveDraftEvent();
                    Toast.makeText(context, "Save successfully", Toast.LENGTH_SHORT).show();
                } else{
                    WindowUtils.animateView(context, btn_draft);
                }
            }
        });

        btn_publish = rView.findViewById(R.id.hevent_frag_btn_publish);
        btn_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_price = et_price.getText().toString().trim();
                if(str_price.length() > 0 && !str_price.equals(".") && MyApp.curUser.is_debit == 0 && !str_price.equals("0")){
                    showPaymentDlg();
                    return;
                }
                if(checkValidFields()){
                    makeEventModel();
                    publishEvent();
                } else{
                    WindowUtils.animateView(context, btn_publish);
                }
            }
        });

        btn_change = rView.findViewById(R.id.hevent_frag_btn_change);
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.showOneGalleryFrag();
                bSelPhoto = true;
            }
        });

        tv_people = rView.findViewById(R.id.hevent_frag_tv_people);
        sb_people = rView.findViewById(R.id.hevent_frag_sb_people);
        sb_people.setProgress(4, 30);
        sb_people.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                int minVal = (int)leftValue;
                int maxVal = (int)rightValue;
                if(leftValue > rightValue){
                    minVal = (int)rightValue;
                    maxVal = (int)leftValue;
                }
                tv_max_people.setText(minVal + " - " + maxVal);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

        tv_age = rView.findViewById(R.id.hevent_frag_tv_age);
        sb_age = rView.findViewById(R.id.hevent_frag_sb_age);
        sb_age.setProgress(18, 99);
        sb_age.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                int minVal = (int)leftValue;
                int maxVal = (int)rightValue;
                if(leftValue > rightValue){
                    minVal = (int)rightValue;
                    maxVal = (int)leftValue;
                }
                if(maxVal < 30) {
                    sb_age.setProgress(minVal, 30);
                }
                tv_max_age.setText(minVal + " - " + maxVal);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

        et_date = rView.findViewById(R.id.hevent_frag_et_date);
        et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.MyDatePicker,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                try{
                                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    DateFormat outputFormat = new SimpleDateFormat("MMM dd yyyy");
                                    String inputDateStr=String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                                    Date date = inputFormat.parse(inputDateStr);
                                    String outputDateStr = outputFormat.format(date);

                                    et_date.setTypeface(MyApp.myfont_normal);
                                    et_date.setText(outputDateStr);
                                } catch (ParseException ex){
                                    ex.printStackTrace();
                                }
                            }
                        }, mYear, mMonth, mDay);

                datePickerDialog.getDatePicker().setMinDate((System.currentTimeMillis() - 1000) );
                datePickerDialog.getDatePicker().setMaxDate((System.currentTimeMillis() + 7*24 * 60 * 60 * 1000 ));
                datePickerDialog.show();
            }
        });

        et_time = rView.findViewById(R.id.hevent_frag_et_time);
        et_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(context,  R.style.MyTimePicker ,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
                                try {
                                    et_time.setTypeface(MyApp.myfont_normal);
                                    selTime = sdf.parse(hourOfDay + ":" + minute + ":00");
                                    sTime = sdfs.format(selTime); // <-- I got result here
                                    et_time.setText(sTime);
                                    showDurationDlg();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
                timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
            }
        });

        cb_later = rView.findViewById(R.id.hevent_frag_cb_later);
        cb_later.setText(" Pay later");

        cb_coed = rView.findViewById(R.id.hevent_frag_cb_coed);
        cb_coed.setText("  " + getString(R.string.event_coed));
        cb_boys = rView.findViewById(R.id.hevent_frag_cb_boys);
        cb_boys.setText("  " + getString(R.string.event_boys));
        cb_girls = rView.findViewById(R.id.hevent_frag_cb_girls);
        cb_girls.setText("  " + getString(R.string.event_girls));
        cb_coed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    cb_boys.setChecked(false);
                    cb_girls.setChecked(false);
                    ngender = 0;
                }
                if(!cb_coed.isChecked() && !cb_boys.isChecked() && !cb_girls.isChecked()){
                    cb_coed.setChecked(true);
                }
            }
        });
        cb_boys.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    cb_coed.setChecked(false);
                    cb_girls.setChecked(false);
                    ngender = 1;
                }
                if(!cb_coed.isChecked() && !cb_boys.isChecked() && !cb_girls.isChecked()){
                    cb_boys.setChecked(true);
                }
            }
        });
        cb_girls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    cb_coed.setChecked(false);
                    cb_boys.setChecked(false);
                    ngender = 2;
                }
                if(!cb_coed.isChecked() && !cb_boys.isChecked() && !cb_girls.isChecked()){
                    cb_girls.setChecked(true);
                }
            }
        });

        sp_category = rView.findViewById(R.id.hevent_frag_sp_category);
        List<String> cate_list = new ArrayList<String>();
        cate_list.add("Category");
        for(int i = 0; i < 9; i++){
            cate_list.add(Const.interest_list[i]);
        }
        ArrayAdapter<String> cateAdapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, cate_list);
        cateAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        sp_category.setAdapter(cateAdapter);
        sp_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0 && i != nCate){
                    iv_photo.setImageResource(Const.cover_res[i]);
                    nCate = i;
                } else if(i==0){
                    iv_photo.setImageBitmap(null);
                    nCate = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        et_cate_err = rView.findViewById(R.id.hevent_frag_et_category);
        et_cate_err.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_cate_err.setVisibility(View.GONE);
                sp_category.setVisibility(View.VISIBLE);
                sp_category.setFocusable(true);
                sp_category.requestFocus();
            }
        });
        et_cate_err.setVisibility(View.GONE);

        et_title = rView.findViewById(R.id.hevent_frag_et_title);
        et_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    et_title.setTypeface(MyApp.myfont_normal);
                    et_title.setHintTextColor(ContextCompat.getColor(context, R.color.colorGray));
                    et_title.setHint("Title");
                } else{
                    et_title.setTypeface(MyApp.myfont_italic);
                    et_title.setHintTextColor(ContextCompat.getColor(context, R.color.colorRed));
                    et_title.setHint(R.string.err_title);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        actv_addr = rView.findViewById(R.id.hevent_frag_et_address);
        actv_addr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0){
                    bChooseAddr = false;
                    actv_addr.setTypeface(MyApp.myfont_italic);
                    actv_addr.setHintTextColor(ContextCompat.getColor(context, R.color.colorRed));
                    actv_addr.setHint(R.string.err_invalid_address);
                } else{
                    if(!bOpen){// && !bOpen
                        actv_addr.setTypeface(MyApp.myfont_normal);
                        getAutoCompletePlaces(charSequence.toString());
                    } else{
                        if(!draftEvent.address.equals(actv_addr.getText().toString().trim()) && bOpen){
                            bChooseAddr = false;
                        }
                        bOpen = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        actv_addr.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        actv_addr.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                String prefix = actv_addr.getText().toString().trim();
                if(prefix.length() > 0){
                    bChooseAddr = false;
                    //bOpen = false;
                }
                return false;
            }
        });
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, Const.countries);
        suggest_rec = rView.findViewById(R.id.hevent_frag_suggestion_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        suggest_rec.setLayoutManager(layoutManager);
        addrAdapter = new AddressListAdapter(addr_list, context);
        suggest_rec.setAdapter(addrAdapter);
        addrAdapter.setOnItemClickListener(new AddressListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int pos) {
                getPlaceDetails(addr_list.get(pos).place_id);
                return 0;
            }
        });
        //actv_addr.setAdapter(suggestionAdapter);

        cb_follow = rView.findViewById(R.id.hevent_frag_cb_private);
        cb_follow.setText("  " + getString(R.string.event_private));
        cb_invitation = rView.findViewById(R.id.hevent_frag_cb_exception);
        cb_invitation.setText("  " + getString(R.string.event_exception));
        tv_max_people = rView.findViewById(R.id.hevent_frag_tv_max_people);
        tv_max_age = rView.findViewById(R.id.hevent_frag_tv_max_age);
        et_price = rView.findViewById(R.id.hevent_frag_et_price);
        et_price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0 && !charSequence.toString().trim().equals("0")){
                    cb_later.setVisibility(View.VISIBLE);
                    if(!charSequence.toString().trim().equals(".") && MyApp.curUser.is_debit == 0){
                        //showPaymentDlg();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_price.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
                String str_price = et_price.getText().toString().trim();
                if(str_price.length() > 0 && !str_price.equals(".") && MyApp.curUser.is_debit == 0 && !str_price.equals("0")){
                    showPaymentDlg();
                }
            }
        });

        sp_currency = rView.findViewById(R.id.hevent_frag_sp_currency);
        List<String> currency_list = new ArrayList<String>();
        currency_list.add("CAD");
        currency_list.add("USD");
        ArrayAdapter<String> curAdapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, currency_list);
        cateAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        sp_currency.setAdapter(curAdapter);

        cb_later.setVisibility(View.INVISIBLE);
    }

    private void publishEvent(){
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Publishing...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        String sGen = "co";
        if(draftEvent.gender == 1) sGen = "boy";
        if(draftEvent.gender == 2) sGen = "girl";
        String sLater = "0";
        if(cb_later.isChecked()) sLater = "1";

        String url_addr = Const.HOST_URL + Const.PUBLISH_EVENT_URL;
        if(draftEvent.id != null && draftEvent.id.length() > 0){
            url_addr = Const.HOST_URL + Const.EDIT_EVENT_URL;
        } else{
            draftEvent.id = "";
        }

        String[] ss = draftEvent.loc.split(",");
        float fLat = Float.valueOf(ss[0]);
        float fLng = Float.valueOf(ss[1]);
        String begin_at = TxtUtils.getTimestamp(draftEvent.date, sTime) + "";

        if(draftEvent.photo.length() > 0  && !draftEvent.photo.contains("http:")){
            File bm_file = new File(draftEvent.photo);
            if(bm_file == null) return;
            long fileSizeInMB = bm_file.length()/ (1024*1024);
            if(fileSizeInMB > 2){
                bm_file = ImageFilePath.saveBitmapToFile(bm_file);
                fileSizeInMB = bm_file.length()/1024;
            }
            ANRequest.MultiPartBuilder multiPartBuilder = AndroidNetworking.upload(url_addr)
                    .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                    .addMultipartParameter("event_id", draftEvent.id)
                    .addMultipartParameter("event_title", draftEvent.title)
                    .addMultipartParameter("event_date", draftEvent.date)
                    .addMultipartParameter("event_time", sTime)
                    .addMultipartParameter("event_end_time", eTime)
                    .addMultipartParameter("address", actv_addr.getText().toString())
                    .addMultipartParameter("min_members", (int)sb_people.getLeftSeekBar().getProgress() + "")
                    .addMultipartParameter("max_members", (int)sb_people.getRightSeekBar().getProgress() + "")
                    .addMultipartParameter("min_age", (int)sb_age.getLeftSeekBar().getProgress() + "")
                    .addMultipartParameter("max_age", (int)sb_age.getRightSeekBar().getProgress() + "")
                    .addMultipartParameter("event_category", draftEvent.category)
                    .addMultipartParameter("is_private", draftEvent.followable + "")
                    .addMultipartParameter("event_description", draftEvent.desc)
                    .addMultipartParameter("gender", sGen)
                    .addMultipartParameter("allow_invite", draftEvent.invitaion + "")
                    .addMultipartParameter("lat", fLat + "")
                    .addMultipartParameter("lng", fLng + "")
                    .addMultipartParameter("price", draftEvent.price)
                    .addMultipartParameter("currency_code", draftEvent.currency)
                    .addMultipartParameter("event_begin_at", begin_at)
                    .addMultipartParameter("is_pay_later", sLater);

            multiPartBuilder.addMultipartFile("cover_photo", bm_file)
                    .setTag(APP_TAG)
                    .setPriority(Priority.HIGH)
                    .build()
                    .setUploadProgressListener(new UploadProgressListener() {
                        @Override
                        public void onProgress(long bytesUploaded, long totalBytes) {

                        }
                    }).getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    hud.dismiss();
                    try {
                        int bSuccess = response.getInt("success");
                        if (bSuccess == 1) {
                            JSONObject data_obj = response.getJSONObject("data");
                            draftEvent.id = data_obj.getString("id");
                            draftEvent.id = data_obj.getString("id");
                            draftEvent.address = data_obj.getString("address");
                            draftEvent.people = data_obj.getString("min_members") + "," + data_obj.getString("max_members");
                            draftEvent.age = data_obj.getString("min_age") + "," + data_obj.getString("max_age");
                            draftEvent.category = data_obj.getString("event_category");
                            draftEvent.photo = data_obj.getString("cover_photo");
                            if(draftEvent.photo.equals("null")){
                                draftEvent.photo = "";
                            }
                            draftEvent.followable = data_obj.getInt("is_private");
                            draftEvent.invitaion = data_obj.getInt("allow_invite");
                            draftEvent.created_at = data_obj.getString("created_at");
                            draftEvent.updated_at = data_obj.getString("updated_at");
                            draftEvent.loc = data_obj.getDouble("lat") + "," + data_obj.getDouble("lng");
                            draftEvent.nLike = 0;//data_obj.getInt("isLikedByYou");
                            draftEvent.is_pay_later = data_obj.getInt("is_pay_later");
                            if(!data_obj.isNull("creator")){
                                JSONObject creator_obj = data_obj.getJSONObject("creator");
                                UserModel creat_user = new UserModel();
                                creat_user.email = creator_obj.getString("email");
                                creat_user.first_name = creator_obj.getString("first_name");
                                creat_user.last_name = creator_obj.getString("last_name");
                                creat_user.push_id = creator_obj.getString("push_user_id");
                                creat_user.uid = creator_obj.getString("id");
                                creat_user.created_at = creator_obj.getString("created_at");
                                creat_user.updated_at = creator_obj.getString("updated_at");
                                int nVerify = creator_obj.getInt("email_verified");
                                if(nVerify == 0){
                                    creat_user.bEmailVerify = false;
                                } else{
                                    creat_user.bEmailVerify = true;
                                }
                                String sVal = creator_obj.getString("dob");
                                if(sVal.equals("null")) sVal = "";
                                creat_user.dob = sVal;
                                sVal = creator_obj.getString("lang");
                                if(sVal.equals("null")) sVal = "";
                                creat_user.lang = sVal;
                                sVal = creator_obj.getString("interests");
                                if(sVal.equals("null")) sVal = "";
                                creat_user.interests = sVal;
                                sVal = creator_obj.getString("gender");
                                if(sVal.equals("null")) sVal = "";
                                creat_user.gender = sVal;
                                sVal = creator_obj.getString("social_id");
                                if(sVal.equals("null")) sVal = "";
                                creat_user.social_id = sVal;
                                sVal = creator_obj.getString("social_name");
                                if(sVal.equals("null")) sVal = "";
                                creat_user.social_name = sVal;
                                if(creator_obj.has("avatar")){
                                    sVal = creator_obj.getString("avatar");
                                    if(sVal.equals("null")) sVal = "";
                                    creat_user.photo_url = sVal;
                                } else{
                                    creat_user.photo_url = "";
                                }

                                sVal = creator_obj.getString("personality_type");
                                if(sVal.equals("null")) sVal = "";
                                creat_user.person_type = sVal;
                                sVal = creator_obj.getString("fun_facts");
                                if(sVal.equals("null")) sVal = "";
                                creat_user.facts = sVal;
                                creat_user.push_chats = creator_obj.getInt("push_chats");
                                creat_user.push_friends_activities = creator_obj.getInt("push_friends_activities");
                                creat_user.push_my_activities = creator_obj.getInt("push_my_activities");
                                creat_user.hide_my_location = creator_obj.getInt("hide_my_location");
                                creat_user.hide_my_age = creator_obj.getInt("hide_my_age");
                                creat_user.is_active_by_customer = creator_obj.getInt("is_active_by_customer");
                                creat_user.lat = creator_obj.getDouble("lat");
                                creat_user.lng = creator_obj.getDouble("lng");
                                draftEvent.creator = creat_user;
                            } else{
                                draftEvent.creator = null;
                            }
                            draftEvent.members = new ArrayList<>();

                            MyApp.curUser.event_count++;
                            draftEvent.state = 1;
                            saveDraftEvent();
                            showPublishEventFrag();
                            dismiss();
                        }
                    } catch (JSONException ex){
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onError(ANError anError) {
                    hud.dismiss();
                    WindowUtils.animateView(context, btn_publish);
                }
            });
        } else{
            AndroidNetworking.post(url_addr)
                    .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                    .addBodyParameter("event_id", draftEvent.id)
                    .addBodyParameter("event_title", draftEvent.title)
                    .addBodyParameter("event_date", draftEvent.date)
                    .addBodyParameter("event_time", sTime)
                    .addBodyParameter("event_end_time", eTime)
                    .addBodyParameter("address", actv_addr.getText().toString())
                    .addBodyParameter("min_members", (int)sb_people.getLeftSeekBar().getProgress() + "")
                    .addBodyParameter("max_members", (int)sb_people.getRightSeekBar().getProgress() + "")
                    .addBodyParameter("min_age", (int)sb_age.getLeftSeekBar().getProgress() + "")
                    .addBodyParameter("max_age", (int)sb_age.getRightSeekBar().getProgress() + "")
                    .addBodyParameter("event_category", draftEvent.category)
                    .addBodyParameter("is_private", draftEvent.followable + "")
                    .addBodyParameter("event_description", draftEvent.desc)
                    .addBodyParameter("gender", sGen)
                    .addBodyParameter("allow_invite", draftEvent.invitaion + "")
                    .addBodyParameter("lat", fLat + "")
                    .addBodyParameter("lng", fLng + "")
                    .addBodyParameter("price", draftEvent.price)
                    .addBodyParameter("currency_code", draftEvent.currency)
                    .addBodyParameter("event_begin_at", begin_at)
                    .addBodyParameter("is_pay_later", sLater)
                    .setTag(APP_TAG)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hud.dismiss();
                            try {
                                int bSuccess = response.getInt("success");
                                if (bSuccess == 1) {
                                    JSONObject data_obj = response.getJSONObject("data");
                                    draftEvent.id = data_obj.getString("id");
                                    draftEvent.address = data_obj.getString("address");
                                    draftEvent.people = data_obj.getString("min_members") + "," + data_obj.getString("max_members");
                                    draftEvent.age = data_obj.getString("min_age") + "," + data_obj.getString("max_age");
                                    draftEvent.category = data_obj.getString("event_category");
                                    draftEvent.photo = data_obj.getString("cover_photo");
                                    if(draftEvent.photo.equals("null")){
                                        draftEvent.photo = "";
                                    }
                                    draftEvent.followable = data_obj.getInt("is_private");
                                    draftEvent.invitaion = data_obj.getInt("allow_invite");
                                    draftEvent.created_at = data_obj.getString("created_at");
                                    draftEvent.updated_at = data_obj.getString("updated_at");
                                    draftEvent.loc = data_obj.getDouble("lat") + "," + data_obj.getDouble("lng");
                                    draftEvent.nLike = 0;//data_obj.getInt("isLikedByYou");
                                    draftEvent.is_pay_later = data_obj.getInt("is_pay_later");
                                    if(!data_obj.isNull("creator")){
                                        JSONObject creator_obj = data_obj.getJSONObject("creator");
                                        UserModel creat_user = new UserModel();
                                        creat_user.email = creator_obj.getString("email");
                                        creat_user.first_name = creator_obj.getString("first_name");
                                        creat_user.last_name = creator_obj.getString("last_name");
                                        creat_user.push_id = creator_obj.getString("push_user_id");
                                        creat_user.uid = creator_obj.getString("id");
                                        creat_user.created_at = creator_obj.getString("created_at");
                                        creat_user.updated_at = creator_obj.getString("updated_at");
                                        int nVerify = creator_obj.getInt("email_verified");
                                        if(nVerify == 0){
                                            creat_user.bEmailVerify = false;
                                        } else{
                                            creat_user.bEmailVerify = true;
                                        }
                                        String sVal = creator_obj.getString("dob");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.dob = sVal;
                                        sVal = creator_obj.getString("lang");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.lang = sVal;
                                        sVal = creator_obj.getString("interests");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.interests = sVal;
                                        sVal = creator_obj.getString("gender");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.gender = sVal;
                                        sVal = creator_obj.getString("social_id");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.social_id = sVal;
                                        sVal = creator_obj.getString("social_name");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.social_name = sVal;
                                        if(creator_obj.has("avatar")){
                                            sVal = creator_obj.getString("avatar");
                                            if(sVal.equals("null")) sVal = "";
                                            creat_user.photo_url = sVal;
                                        } else{
                                            creat_user.photo_url = "";
                                        }

                                        sVal = creator_obj.getString("personality_type");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.person_type = sVal;
                                        sVal = creator_obj.getString("fun_facts");
                                        if(sVal.equals("null")) sVal = "";
                                        creat_user.facts = sVal;
                                        creat_user.push_chats = creator_obj.getInt("push_chats");
                                        creat_user.push_friends_activities = creator_obj.getInt("push_friends_activities");
                                        creat_user.push_my_activities = creator_obj.getInt("push_my_activities");
                                        creat_user.hide_my_location = creator_obj.getInt("hide_my_location");
                                        creat_user.hide_my_age = creator_obj.getInt("hide_my_age");
                                        creat_user.is_active_by_customer = creator_obj.getInt("is_active_by_customer");
                                        creat_user.lat = creator_obj.getDouble("lat");
                                        creat_user.lng = creator_obj.getDouble("lng");
                                        draftEvent.creator = creat_user;
                                    } else{
                                        draftEvent.creator = null;
                                    }
                                    draftEvent.members = new ArrayList<>();

                                    MyApp.curUser.event_count++;
                                    draftEvent.state = 1;
                                    saveDraftEvent();
                                    showPublishEventFrag();
                                    dismiss();
                                }
                            } catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            hud.dismiss();
                            WindowUtils.animateView(context, btn_publish);
                        }
                    });
        }
    }

    private void showPublishEventFrag(){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PublishEventFragment pubeventFrag = new PublishEventFragment();
        pubeventFrag.pub_Event = draftEvent;
        //fragmentTransaction.replace(R.id.home_frame, pubeventFrag, Const.FRAG_PUBLISH_EVENT_TAG);
        //fragmentTransaction.commit();
        fragmentTransaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        fragmentTransaction.add(R.id.home_frame, pubeventFrag, Const.FRAG_PUBLISH_EVENT_TAG).addToBackStack(Const.FRAG_PUBLISH_EVENT_TAG).commit();
    }

    private boolean checkValidFields(){
        boolean btitle = false, bdate = false, btime = false, bdesc = false;
        String sTitle = et_title.getText().toString().trim();
        if(sTitle.length() > 0){
            btitle = true;
        } else{
            et_title.setText("");
            btitle = false;
        }
        String sDate = et_date.getText().toString();
        if(sDate.length() > 0){
            bdate = true;
        } else{
            et_date.setTypeface(MyApp.myfont_italic);
            et_date.setHintTextColor(ContextCompat.getColor(context, R.color.colorRed));
            et_date.setHint(R.string.err_pick_date);
            bdate = false;
        }
        String mTime = et_time.getText().toString();
        if(mTime.length() > 0){
            btime = true;
        } else{
            et_time.setTypeface(MyApp.myfont_italic);
            et_time.setHintTextColor(ContextCompat.getColor(context, R.color.colorRed));
            et_time.setHint(R.string.err_pick_time);
            btime = false;
        }
        String sDesc = et_desc.getText().toString().trim();
        if(sDesc.length() > 0){
            bdesc = true;
        } else{
            et_desc.setText("");
            bdesc = false;
        }

        if(bdate && btime){
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy hh:mm a");
            try{
                Date selDate = sdf.parse(et_date.getText().toString().trim() + " " + sTime);
                Date today = new Date();
                long diff = selDate.getTime() - today.getTime();
                if(diff < 1000){
                    et_time.setText("");
                    et_time.setTypeface(MyApp.myfont_italic);
                    et_time.setHintTextColor(ContextCompat.getColor(context, R.color.colorRed));
                    et_time.setHint(R.string.err_pick_time);
                    btime = false;
                }
            } catch (ParseException ex){
                ex.printStackTrace();
                et_time.setTypeface(MyApp.myfont_italic);
                et_time.setHintTextColor(ContextCompat.getColor(context, R.color.colorRed));
                et_time.setHint(R.string.err_pick_time);
                et_time.setText("");
                btime = false;
            }
        }

        if(nCate == 0){
            sp_category.setVisibility(View.GONE);
            et_cate_err.setVisibility(View.VISIBLE);
        }

        return btime && bdate && btitle && (nCate != 0) && bdesc && bChooseAddr;
    }

    private void showDurationDlg(){
        Button btn_done;
        final AppCompatSpinner sp_dur;
        final Dialog dialog = new Dialog(context, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.custom_duration_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        sp_dur = dialog.findViewById(R.id.custom_duration_sp_duration);
        btn_done = dialog.findViewById(R.id.custom_duration_btn_done);

        List<String> time_list = new ArrayList<String>();
        time_list.add("None");
        time_list.add("15 minutes");
        time_list.add("30 minutes");
        time_list.add("1 hour");
        time_list.add("2 hours");
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, time_list);
        timeAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        sp_dur.setAdapter(timeAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sp_dur.setPopupBackgroundResource(R.color.colorWhite);
        }
        dialog.show();

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ind = sp_dur.getSelectedItemPosition();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selTime);
                if (ind == 1){
                    calendar.add(Calendar.MINUTE, 15);
                } else if(ind == 2){
                    calendar.add(Calendar.MINUTE, 30);
                } else if (ind == 3){
                    calendar.add(Calendar.HOUR, 1);
                } else if (ind == 4){
                    calendar.add(Calendar.HOUR, 2);
                }

                if(ind != 0){
                    SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
                    eTime = format.format(calendar.getTime());
                    et_time.setText(sTime + " - " + eTime);
                }

                dialog.dismiss();

            }
        });
    }

    private void showConfirmDlg(){
        TextView tv_desc;
        Button btn_cancel, btn_sure;
        final Dialog backDlg = new Dialog(context, R.style.FullHeightDialog);
        backDlg.setContentView(R.layout.custom_confirm_dlg);
        View dview = backDlg.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        tv_desc  = backDlg.findViewById(R.id.custom_confirm_tv_desc);
        btn_cancel = backDlg.findViewById(R.id.custom_confirm_btn_cancel);
        btn_sure = backDlg.findViewById(R.id.custom_confirm_btn_sure);

        tv_desc.setText(R.string.event_without_save);
        btn_sure.setText(R.string.btn_save_draft);
        btn_cancel.setText(R.string.btn_give_up);
        backDlg.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                backDlg.dismiss();
                dismiss();
            }
        });

        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeEventModel();
                saveDraftEvent();
                backDlg.dismiss();
                dismiss();
            }
        });
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
                                String sAddr = result.getString("formatted_address");
                                JSONObject geo_obj = result.getJSONObject("geometry");
                                JSONObject loc_obj = geo_obj.getJSONObject("location");
                                double lat = loc_obj.getDouble("lat");
                                double lng = loc_obj.getDouble("lng");
                                draftEvent.loc = lat + "," + lng;
                                bChooseAddr = true;
                                bOpen = true;
                                draftEvent.address = sAddr;
                                actv_addr.setText(sAddr);
                                suggest_rec.setVisibility(View.GONE);
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

    private void getAutoCompletePlaces(String prefix){
        String API_PATH = String.format(Const.autocomplete_api_origin, prefix, getString(R.string.google_maps_key));//getString(R.string.my_places_api_key));
        if(MyApp.curLoc != null){
            API_PATH = String.format(autocomplete_api, prefix, MyApp.curLoc.getLatitude(), MyApp.curLoc.getLongitude(), getString(R.string.google_maps_key));//getString(R.string.my_places_api_key));
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
                                //String[] addr_list = new String[data_array.length()];
                                addr_list.clear();
                                for(int i = 0; i < data_array.length(); i++){
                                    JSONObject one_obj = data_array.getJSONObject(i);
                                    JSONObject format_obj = one_obj.getJSONObject("structured_formatting");
                                    AddressModel one = new AddressModel();
                                    one.place_id = one_obj.getString("place_id");
                                    one.localAddr = format_obj.getString("main_text");
                                    one.fullAddr = format_obj.getString("secondary_text");
                                    //addr_list[i] = one.fullAddr;
                                    addr_list.add(one);
                                }
                                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, addr_list);
                                //actv_addr.setAdapter(adapter);
                                if(addr_list.size() > 0){
                                    suggest_rec.setVisibility(View.VISIBLE);
                                    addrAdapter.setDataList(addr_list);
                                } else{
                                    suggest_rec.setVisibility(View.GONE);
                                }
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

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)(getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE));
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    CardModel cData;
    Dialog pdialog;

    private void showPaymentDlg(){
        final Button btn_next_d;
        final CheckBox cb_save_d;
        final TextView tv_title_d, tv_cardnum_d, tv_expire_d, tv_cvv_d, tv_holder_d;
        final EditText et_cardnum_d, et_holder_d, et_expire_d, et_cvv_d, et_addr_name_d, et_postcode_d, et_province_d, et_city_d;
        final LinearLayout ll_card_d, ll_info_d, ll_card_def;
        pdialog = new Dialog(context, R.style.FullHeightDialog);
        pdialog.setContentView(R.layout.payment_debit_dlg);
        View dview = pdialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);
        dview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
        btn_next_d = pdialog.findViewById(R.id.payment_debit_btn_save);
        tv_title_d = pdialog.findViewById(R.id.payment_debit_tv_title);
        tv_cardnum_d = pdialog.findViewById(R.id.payment_debit_tv_number);
        tv_holder_d = pdialog.findViewById(R.id.payment_debit_tv_holder);
        tv_expire_d = pdialog.findViewById(R.id.payment_debit_tv_expire);
        tv_cvv_d = pdialog.findViewById(R.id.payment_debit_tv_cvv);
        et_cardnum_d = pdialog.findViewById(R.id.payment_debit_et_number);
        et_holder_d = pdialog.findViewById(R.id.payment_debit_et_name);
        et_expire_d = pdialog.findViewById(R.id.payment_debit_et_expire);
        et_cvv_d = pdialog.findViewById(R.id.payment_debit_et_cvv);
        et_addr_name_d = pdialog.findViewById(R.id.payment_debit_et_address);
        et_province_d = pdialog.findViewById(R.id.payment_debit_et_province);
        et_city_d = pdialog.findViewById(R.id.payment_debit_et_city);
        et_postcode_d = pdialog.findViewById(R.id.payment_debit_et_postcode);
        ll_card_d = pdialog.findViewById(R.id.payment_debit_ll_card);
        ll_info_d = pdialog.findViewById(R.id.payment_debit_ll_info);
        cb_save_d = pdialog.findViewById(R.id.payment_debit_cb_save);
        ll_card_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });
        ll_info_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
            }
        });

        cData = mHelper.getCardInfo();
        if(cData.card_num != null && cData.card_num.length() > 0){
            String sNum = cData.card_num.substring(cData.card_num.length() - 4);
            et_cardnum_d.setText("xxxx xxxx xxxx " + sNum);
            et_holder_d.setText(cData.holder_name);
            String ex_str = cData.expire.substring(0, 1) + "/" + cData.expire.substring(cData.expire.length() - 2);
            et_expire_d.setText(ex_str);
            et_cvv_d.setText(cData.cvv);
            et_addr_name_d.setText(cData.street_name);
            et_city_d.setText(cData.city);
            et_province_d.setText(cData.province);
            et_postcode_d.setText(cData.post_code);
        }

        ll_card_d.setVisibility(View.VISIBLE);
        cb_save_d.setText("  " + getString(R.string.payment_save_card));
        pdialog.show();

        btn_next_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sNum = et_cardnum_d.getText().toString().trim();
                boolean bErr = false;
                if(sNum.length() != 19){
                    et_cardnum_d.setText("");
                    bErr = true;
                }
                if(et_holder_d.getText().toString().trim().length() == 0){
                    et_holder_d.setText("");
                    bErr = true;
                }
                String sExp = et_expire_d.getText().toString().trim();
                if(sExp.length() < 4 || !TxtUtils.isValidExpireDate(sExp)){
                    et_expire_d.setText("");
                    bErr = true;
                }
                String sCVV = et_cvv_d.getText().toString().trim();
                /*if(sCVV.length() < 3){
                    et_cvv_d.setText("");
                    bErr = true;
                }*/
                if(et_addr_name_d.getText().toString().trim().length() == 0){
                    et_addr_name_d.setText("");
                    bErr = true;
                }
                if(et_city_d.getText().toString().trim().length() == 0){
                    et_city_d.setText("");
                    bErr = true;
                }
                if(et_province_d.getText().toString().trim().length() == 0){
                    et_province_d.setText("");
                    bErr = true;
                }
                if(et_postcode_d.getText().toString().trim().length() == 0){
                    et_postcode_d.setText("");
                    bErr = true;
                }

                if(!bErr){
                    if(cb_save_d.isChecked()){
                        cData.card_num = sNum;
                        cData.holder_name = et_holder_d.getText().toString().trim();
                        cData.expire = sExp;
                        cData.cvv = et_cvv_d.getText().toString().trim();
                        cData.street_name = et_addr_name_d.getText().toString().trim();
                        cData.city = et_city_d.getText().toString().trim();
                        cData.province = et_province_d.getText().toString().trim();
                        cData.post_code = et_postcode_d.getText().toString().trim();
                        mHelper.putCardInfo(cData);
                    }
                    String hname = et_holder_d.getText().toString().trim();
                    String sCvv = et_cvv_d.getText().toString().trim();
                    String sAddr = et_addr_name_d.getText().toString().trim();
                    String sCity = et_city_d.getText().toString().trim();
                    String sProvince = et_province_d.getText().toString().trim();;
                    String sPost = et_postcode_d.getText().toString().trim();
                    //Call API
                    updateDebitCard(sNum, hname, sExp, sCvv, sAddr, sCity, sProvince, sPost);
                }
            }
        });

        et_cardnum_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_cardnum_d.getText().toString().trim();
                int textlength = text.length();

                if(textlength == 0){
                    tv_cardnum_d.setText(R.string.err_card_number);
                    tv_cardnum_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_cardnum_d.setText(R.string.payment_card_number);
                    tv_cardnum_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }

                if(text.endsWith(" "))
                    return;

                if(textlength == 5 || textlength == 10 || textlength == 15)
                {
                    et_cardnum_d.setText(new StringBuilder(text).insert(text.length()-1, " ").toString());
                    et_cardnum_d.setSelection(et_cardnum_d.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_holder_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int textlength = et_holder_d.getText().toString().trim().length();
                if(textlength == 0){
                    tv_holder_d.setText("*Cardholder Name");
                    tv_holder_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_holder_d.setText(R.string.payment_cardholder);
                    tv_holder_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_expire_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = et_expire_d.getText().toString().trim();
                int textlength = text.length();

                if(textlength == 0){
                    tv_expire_d.setText(R.string.err_expire);
                    tv_expire_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_expire_d.setText(R.string.payment_expiration);
                    tv_expire_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }

                if(text.endsWith("/"))
                    return;

                if(textlength == 3)
                {
                    et_expire_d.setText(new StringBuilder(text).insert(text.length()-1, "/").toString());
                    et_expire_d.setSelection(et_expire_d.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_cvv_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int textlength = et_cvv_d.getText().toString().trim().length();
                /*if(textlength == 0){
                    tv_cvv_d.setText(R.string.err_cvv);
                    tv_cvv_d.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
                } else{
                    tv_cvv_d.setText(R.string.payment_cvv);
                    tv_cvv_d.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                }*/
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_addr_name_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_addr_name_d.getText().toString().trim().length() == 0){
                    et_addr_name_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_city_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_city_d.getText().toString().trim().length() == 0){
                    et_city_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_province_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_province_d.getText().toString().trim().length() == 0){
                    et_province_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        et_postcode_d.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_postcode_d.getText().toString().trim().length() == 0){
                    et_postcode_d.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void updateDebitCard(String cardnum, String holdername, String expiredt, String cvc, String addr, String city, String province, String postcode){

        String[] exp_str = expiredt.split("/");
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Adding...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        AndroidNetworking.post(Const.HOST_URL + Const.UPDATE_DEBIT_ACCOUNT)
                .addBodyParameter("user_id", MyApp.curUser.uid)
                .addBodyParameter("card_number", cardnum)
                .addBodyParameter("holder_name", holdername)
                .addBodyParameter("exp_month", exp_str[0])
                .addBodyParameter("exp_year", "20" + exp_str[1])
                .addBodyParameter("card_cvc", cvc)
                .addBodyParameter("address_city", city)
                .addBodyParameter("address_line1", addr)
                .addBodyParameter("address_postal_code", postcode)
                .addBodyParameter("address_state", province)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                MyApp.curUser.is_debit = 1;
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                        pdialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                        String err = anError.getErrorBody();
                        ToastCompat.makeText(context, err, Toast.LENGTH_SHORT).show();
                        et_price.setText("");
                    }
                });
    }

    private void setDebitCard(){

        AndroidNetworking.post(Const.HOST_URL + Const.GET_DEBIT_CARD)
                .addBodyParameter("user_id", MyApp.curUser.uid)
                .setTag(APP_TAG)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1){
                                MyApp.curUser.is_debit = 1;
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        MyApp.curUser.is_debit = 0;
                    }
                });
    }

}
