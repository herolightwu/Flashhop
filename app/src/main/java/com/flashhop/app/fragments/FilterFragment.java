package com.flashhop.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.helpers.MySQLiteHelper;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FilterFragment extends DialogFragment {

    HomeActivity parent;
    Context context;

    View root_view;
    Button btn_reset, btn_save;
    TextView tv_tab_all, tv_tab_event, tv_tab_people;
    LinearLayout ll_events, ll_people;
    TextView tv_party, tv_eating, tv_games, tv_sports, tv_outdoors, tv_spirits, tv_dating, tv_arts, tv_study;
    TextView tv_male, tv_female;
    LinearLayout ll_date1,ll_date2, ll_date3, ll_date4, ll_date5, ll_date6, ll_date7;
    TextView tv_date1,tv_date2, tv_date3, tv_date4, tv_date5, tv_date6, tv_date7;
    View v_line1, v_line2, v_line3, v_line4, v_line5, v_line6, v_line7;
    TextView tv_week1,tv_week2, tv_week3, tv_week4, tv_week5, tv_week6, tv_week7;
    TextView tv_age_min, tv_age_max;
    boolean bParty, bEating, bGames, bSports, bOutdoors, bSpirits, bDating, bArts, bStudy;
    int nMale = 0;//both
    boolean[] bDate = new boolean[7];

    RangeSeekBar sb_age;
    TextView tv_age;

    int nTab = 0;//all
    MySQLiteHelper mHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_filter, container, false);
        parent = (HomeActivity) getActivity();
        context = getContext();
        mHelper = MySQLiteHelper.getInstance(parent);
        initLayout();
        loadData();
        return root_view;
    }

    private void loadData(){
        nTab = parent.view_mode;
        String sCate = parent.filter_opt.event_category;
        if(sCate.contains("party")){
            bParty = true;
        }
        if(sCate.contains("eating")){
            bEating = true;
        }
        if(sCate.contains("games")){
            bGames = true;
        }
        if(sCate.contains("sports")){
            bSports = true;
        }
        if(sCate.contains("outdoors")){
            bOutdoors = true;
        }
        if(sCate.contains("spiritual")){
            bSpirits = true;
        }
        if(sCate.contains("dating")){
            bDating = true;
        }
        if(sCate.contains("arts")){
            bArts = true;
        }
        if(sCate.contains("study")){
            bStudy = true;
        }

        if(parent.filter_opt.gender.equals("male")){
            nMale = 1;
        } else if(parent.filter_opt.gender.equals("female")){
            nMale = 2;
        } else{
            nMale = 0;
        }

        sb_age.setProgress(parent.filter_opt.min_age, parent.filter_opt.max_age);

        try{
            Date today = new Date();
            DateFormat dateF = new SimpleDateFormat("MMM dd yyyy");
            String[] date_list = parent.filter_opt.event_date.split(",");
            for(int i = 0; i < date_list.length; i++){
                Date selD = dateF.parse(date_list[i]);
                int diff = (int)((selD.getTime() + 1000 * 3600 * 24 - today.getTime())/(24 * 1000 * 3600));
                bDate[diff] = true;
            }

            if(bDate[0] && date_list.length == 1 && parent.filter_opt.period.equals("7")){
                bDate[1] = true;
                bDate[2] = true;
                bDate[3] = true;
                bDate[4] = true;
                bDate[5] = true;
                bDate[6] = true;
            }
        } catch (ParseException ex){
            ex.printStackTrace();
        }
    }

    private void refreshLayout(){
        switch (nTab){
            case 1://people
                tv_tab_all.setBackgroundResource(R.drawable.border_rectangle_white);
                tv_tab_people.setBackgroundResource(R.drawable.border_rectangle_black);
                tv_tab_event.setBackgroundResource(R.drawable.border_rectangle_white);
                tv_tab_all.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                tv_tab_people.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                tv_tab_event.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                ll_events.setVisibility(View.GONE);
                ll_people.setVisibility(View.VISIBLE);
                break;
            case 2://events
                tv_tab_all.setBackgroundResource(R.drawable.border_rectangle_white);
                tv_tab_people.setBackgroundResource(R.drawable.border_rectangle_white);
                tv_tab_event.setBackgroundResource(R.drawable.border_rectangle_black);
                tv_tab_all.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                tv_tab_people.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                tv_tab_event.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                ll_events.setVisibility(View.VISIBLE);
                ll_people.setVisibility(View.GONE);
                break;
            default:
                tv_tab_all.setBackgroundResource(R.drawable.border_rectangle_black);
                tv_tab_people.setBackgroundResource(R.drawable.border_rectangle_white);
                tv_tab_event.setBackgroundResource(R.drawable.border_rectangle_white);
                tv_tab_all.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                tv_tab_people.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                tv_tab_event.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                ll_events.setVisibility(View.VISIBLE);
                ll_people.setVisibility(View.VISIBLE);
                break;
        }

        if(bParty){
            tv_party.setBackgroundResource(R.drawable.border_w_back_b);
            tv_party.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_party.setBackgroundResource(R.drawable.border_b_back_w);
            tv_party.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }
        if(bEating){
            tv_eating.setBackgroundResource(R.drawable.border_w_back_b);
            tv_eating.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_eating.setBackgroundResource(R.drawable.border_b_back_w);
            tv_eating.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }
        if(bGames){
            tv_games.setBackgroundResource(R.drawable.border_w_back_b);
            tv_games.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_games.setBackgroundResource(R.drawable.border_b_back_w);
            tv_games.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }
        if(bSports){
            tv_sports.setBackgroundResource(R.drawable.border_w_back_b);
            tv_sports.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_sports.setBackgroundResource(R.drawable.border_b_back_w);
            tv_sports.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }
        if(bOutdoors){
            tv_outdoors.setBackgroundResource(R.drawable.border_w_back_b);
            tv_outdoors.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_outdoors.setBackgroundResource(R.drawable.border_b_back_w);
            tv_outdoors.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }
        if(bSpirits){
            tv_spirits.setBackgroundResource(R.drawable.border_w_back_b);
            tv_spirits.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_spirits.setBackgroundResource(R.drawable.border_b_back_w);
            tv_spirits.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }
        if(bDating){
            tv_dating.setBackgroundResource(R.drawable.border_w_back_b);
            tv_dating.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_dating.setBackgroundResource(R.drawable.border_b_back_w);
            tv_dating.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }
        if(bArts){
            tv_arts.setBackgroundResource(R.drawable.border_w_back_b);
            tv_arts.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_arts.setBackgroundResource(R.drawable.border_b_back_w);
            tv_arts.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }
        if(bStudy){
            tv_study.setBackgroundResource(R.drawable.border_w_back_b);
            tv_study.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_study.setBackgroundResource(R.drawable.border_b_back_w);
            tv_study.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        }

        if(nMale == 1){
            tv_male.setBackgroundResource(R.drawable.border_w_back_b);
            tv_male.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            tv_female.setBackgroundResource(R.drawable.border_b_back_w);
            tv_female.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        } else if(nMale == 2){
            tv_male.setBackgroundResource(R.drawable.border_b_back_w);
            tv_male.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
            tv_female.setBackgroundResource(R.drawable.border_w_back_b);
            tv_female.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        } else{
            tv_male.setBackgroundResource(R.drawable.border_w_back_b);
            tv_male.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
            tv_female.setBackgroundResource(R.drawable.border_w_back_b);
            tv_female.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        }

        Calendar calendar = Calendar.getInstance();
        Date date1 = calendar.getTime();
        WindowUtils.showDate(tv_date1, tv_week1, date1);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date date2 = calendar.getTime();
        WindowUtils.showDate(tv_date2, tv_week2, date2);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date date3 = calendar.getTime();
        WindowUtils.showDate(tv_date3, tv_week3, date3);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date date4 = calendar.getTime();
        WindowUtils.showDate(tv_date4, tv_week4, date4);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date date5 = calendar.getTime();
        WindowUtils.showDate(tv_date5, tv_week5, date5);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date date6 = calendar.getTime();
        WindowUtils.showDate(tv_date6, tv_week6, date6);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date date7 = calendar.getTime();
        WindowUtils.showDate(tv_date7, tv_week7, date7);

        unselectDate(ll_date1, tv_date1, tv_week1, v_line1);
        unselectDate(ll_date2, tv_date2, tv_week2, v_line2);
        unselectDate(ll_date3, tv_date3, tv_week3, v_line3);
        unselectDate(ll_date4, tv_date4, tv_week4, v_line4);
        unselectDate(ll_date5, tv_date5, tv_week5, v_line5);
        unselectDate(ll_date6, tv_date6, tv_week6, v_line6);
        unselectDate(ll_date7, tv_date7, tv_week7, v_line7);
        if(bDate[0]){
            selectDate(ll_date1, tv_date1, tv_week1, v_line1);
        }
        if(bDate[1]){
            selectDate(ll_date2, tv_date2, tv_week2, v_line2);
        }
        if(bDate[2]){
            selectDate(ll_date3, tv_date3, tv_week3, v_line3);
        }
        if(bDate[3]){
            selectDate(ll_date4, tv_date4, tv_week4, v_line4);
        }
        if(bDate[4]){
            selectDate(ll_date5, tv_date5, tv_week5, v_line5);
        }
        if(bDate[5]){
            selectDate(ll_date6, tv_date6, tv_week6, v_line6);
        }
        if(bDate[6]){
            selectDate(ll_date7, tv_date7, tv_week7, v_line7);
        }
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

    private void initLayout(){
        tv_tab_all = root_view.findViewById(R.id.filter_frag_tv_all);
        tv_tab_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nTab = Const.VIEW_FILTER_ALL;
                parent.filter_opt.filter_option = "both";
                refreshLayout();
            }
        });
        tv_tab_people = root_view.findViewById(R.id.filter_frag_tv_people);
        tv_tab_people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nTab = Const.VIEW_FILTER_PEOPLE;
                parent.filter_opt.filter_option = "people";
                refreshLayout();
            }
        });
        tv_tab_event = root_view.findViewById(R.id.filter_frag_tv_event);
        tv_tab_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nTab = Const.VIEW_FILTER_EVENT;
                parent.filter_opt.filter_option = "event";
                refreshLayout();
            }
        });

        ll_people = root_view.findViewById(R.id.filter_frag_ll_people);
        ll_events = root_view.findViewById(R.id.filter_frag_ll_events);

        btn_reset = root_view.findViewById(R.id.filter_frag_btn_reset);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                String strDate = dateFormat.format(date);
                parent.filter_opt.event_date = strDate;
                parent.filter_opt.event_category = "party,eating,dating,sports,outdoors,games,study,spiritual,arts";//all
                parent.filter_opt.gender = "co";
                parent.filter_opt.filter_option = "both";
                parent.filter_opt.period = "7";
                parent.filter_opt.min_age = 18;
                parent.filter_opt.max_age = 99;
                parent.view_mode = Const.VIEW_FILTER_ALL;
                mHelper.putFilter(parent.filter_opt);
                dismiss();
                parent.filterDataFromServer();
            }
        });
        btn_save = root_view.findViewById(R.id.filter_frag_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.view_mode = nTab;
                String sCate = "";
                if(bParty){
                    sCate = "party";
                }
                if(bEating){
                    if(sCate.length() > 0) sCate = sCate + ",eating";
                    else sCate = "eating";
                }
                if(bGames){
                    if(sCate.length() > 0) sCate = sCate + ",games";
                    else sCate = "games";
                }
                if(bSports){
                    if(sCate.length() > 0) sCate = sCate + ",sports";
                    else sCate = "sports";
                }
                if(bOutdoors){
                    if(sCate.length() > 0) sCate = sCate + ",outdoors";
                    else sCate = "outdoors";
                }
                if(bSpirits){
                    if(sCate.length() > 0) sCate = sCate + ",spiritual";
                    else sCate = "spiritual";
                }
                if(bDating){
                    if(sCate.length() > 0) sCate = sCate + ",dating";
                    else sCate = "dating";
                }
                if(bArts){
                    if(sCate.length() > 0) sCate = sCate + ",arts";
                    else sCate = "arts";
                }
                if(bStudy){
                    if(sCate.length() > 0) sCate = sCate + ",study";
                    else sCate = "study";
                }
                if(nMale == 1){
                    parent.filter_opt.gender = "male";
                } else if (nMale == 2){
                    parent.filter_opt.gender = "female";
                } else{
                    parent.filter_opt.gender = "co";
                }
                parent.filter_opt.event_category = sCate;
                parent.filter_opt.period = "1";
                parent.filter_opt.event_date = "";
                if(bDate[0] && bDate[1] && bDate[2] && bDate[3] && bDate[4] && bDate[5] && bDate[6]){
                    Calendar calendar = Calendar.getInstance();
                    Date date_t = calendar.getTime();
                    DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                    String strDate = dateFormat.format(date_t);
                    parent.filter_opt.event_date = strDate;
                    parent.filter_opt.period = "7";
                } else{
                    if(bDate[0]){
                        Calendar calendar = Calendar.getInstance();
                        Date date_t = calendar.getTime();
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                        String strDate = dateFormat.format(date_t);
                        parent.filter_opt.event_date = strDate;
                    }
                    if(bDate[1]){
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        Date date_t = calendar.getTime();
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                        String strDate = dateFormat.format(date_t);
                        if(parent.filter_opt.event_date.length() == 0){
                            parent.filter_opt.event_date = strDate;
                        } else{
                            parent.filter_opt.event_date = parent.filter_opt.event_date + "," + strDate;
                        }
                    }
                    if(bDate[2]){
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, 2);
                        Date date_t = calendar.getTime();
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                        String strDate = dateFormat.format(date_t);
                        if(parent.filter_opt.event_date.length() == 0){
                            parent.filter_opt.event_date = strDate;
                        } else{
                            parent.filter_opt.event_date = parent.filter_opt.event_date + "," + strDate;
                        }
                    }
                    if(bDate[3]){
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, 3);
                        Date date_t = calendar.getTime();
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                        String strDate = dateFormat.format(date_t);
                        if(parent.filter_opt.event_date.length() == 0){
                            parent.filter_opt.event_date = strDate;
                        } else{
                            parent.filter_opt.event_date = parent.filter_opt.event_date + "," + strDate;
                        }
                    }
                    if(bDate[4]){
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, 4);
                        Date date_t = calendar.getTime();
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                        String strDate = dateFormat.format(date_t);
                        if(parent.filter_opt.event_date.length() == 0){
                            parent.filter_opt.event_date = strDate;
                        } else{
                            parent.filter_opt.event_date = parent.filter_opt.event_date + "," + strDate;
                        }
                    }
                    if(bDate[5]){
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, 5);
                        Date date_t = calendar.getTime();
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                        String strDate = dateFormat.format(date_t);
                        if(parent.filter_opt.event_date.length() == 0){
                            parent.filter_opt.event_date = strDate;
                        } else{
                            parent.filter_opt.event_date = parent.filter_opt.event_date + "," + strDate;
                        }
                    }
                    if(bDate[6]){
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DAY_OF_YEAR, 6);
                        Date date_t = calendar.getTime();
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");
                        String strDate = dateFormat.format(date_t);
                        if(parent.filter_opt.event_date.length() == 0){
                            parent.filter_opt.event_date = strDate;
                        } else{
                            parent.filter_opt.event_date = parent.filter_opt.event_date + "," + strDate;
                        }
                    }
                }
                mHelper.putFilter(parent.filter_opt);
                dismiss();
                parent.filterDataFromServer();
            }
        });
        tv_party = root_view.findViewById(R.id.filter_frag_tv_party);
        tv_party.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bParty = !bParty;
                refreshLayout();
            }
        });
        tv_eating = root_view.findViewById(R.id.filter_frag_tv_eating);
        tv_eating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bEating = !bEating;
                refreshLayout();
            }
        });
        tv_games = root_view.findViewById(R.id.filter_frag_tv_games);
        tv_games.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bGames = !bGames;
                refreshLayout();
            }
        });
        tv_sports = root_view.findViewById(R.id.filter_frag_tv_sports);
        tv_sports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bSports = !bSports;
                refreshLayout();
            }
        });
        tv_outdoors = root_view.findViewById(R.id.filter_frag_tv_outdoor);
        tv_outdoors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bOutdoors = !bOutdoors;
                refreshLayout();
            }
        });
        tv_spirits = root_view.findViewById(R.id.filter_frag_tv_spirits);
        tv_spirits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bSpirits = !bSpirits;
                refreshLayout();
            }
        });
        tv_dating = root_view.findViewById(R.id.filter_frag_tv_dating);
        tv_dating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bDating = !bDating;
                refreshLayout();
            }
        });
        tv_arts = root_view.findViewById(R.id.filter_frag_tv_arts);
        tv_arts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bArts = !bArts;
                refreshLayout();
            }
        });
        tv_study = root_view.findViewById(R.id.filter_frag_tv_study);
        tv_study.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bStudy = !bStudy;
                refreshLayout();
            }
        });

        tv_male = root_view.findViewById(R.id.filter_frag_tv_male);
        tv_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nMale == 0){
                    nMale = 2;
                } else if(nMale == 2){
                    nMale = 0;
                }
                refreshLayout();
            }
        });
        tv_female = root_view.findViewById(R.id.filter_frag_tv_female);
        tv_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nMale == 0){
                    nMale = 1;
                } else if(nMale == 1){
                    nMale = 0;
                }
                refreshLayout();
            }
        });

        ll_date1 = root_view.findViewById(R.id.filter_frag_ll_date1);
        tv_date1 = root_view.findViewById(R.id.filter_frag_tv_date1);
        v_line1 = root_view.findViewById(R.id.filter_frag_line1);
        tv_week1 = root_view.findViewById(R.id.filter_frag_tv_weekday1);
        ll_date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bDate[0] && !bDate[1] && !bDate[2] && !bDate[3] && !bDate[4] && !bDate[5] && !bDate[6]){

                } else{
                    bDate[0] = !bDate[0];
                }
                refreshLayout();
            }
        });

        ll_date2 = root_view.findViewById(R.id.filter_frag_ll_date2);
        tv_date2 = root_view.findViewById(R.id.filter_frag_tv_date2);
        v_line2 = root_view.findViewById(R.id.filter_frag_line2);
        tv_week2 = root_view.findViewById(R.id.filter_frag_tv_weekday2);
        ll_date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bDate[0] && bDate[1] && !bDate[2] && !bDate[3] && !bDate[4] && !bDate[5] && !bDate[6]){

                } else{
                    bDate[1] = !bDate[1];
                }
                refreshLayout();
            }
        });

        ll_date3 = root_view.findViewById(R.id.filter_frag_ll_date3);
        tv_date3 = root_view.findViewById(R.id.filter_frag_tv_date3);
        v_line3 = root_view.findViewById(R.id.filter_frag_line3);
        tv_week3 = root_view.findViewById(R.id.filter_frag_tv_weekday3);
        ll_date3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bDate[0] && !bDate[1] && bDate[2] && !bDate[3] && !bDate[4] && !bDate[5] && !bDate[6]){

                } else{
                    bDate[2] = !bDate[2];
                }
                refreshLayout();
            }
        });

        ll_date4 = root_view.findViewById(R.id.filter_frag_ll_date4);
        tv_date4 = root_view.findViewById(R.id.filter_frag_tv_date4);
        v_line4 = root_view.findViewById(R.id.filter_frag_line4);
        tv_week4 = root_view.findViewById(R.id.filter_frag_tv_weekday4);
        ll_date4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bDate[0] && !bDate[1] && !bDate[2] && bDate[3] && !bDate[4] && !bDate[5] && !bDate[6]){

                } else{
                    bDate[3] = !bDate[3];
                }
                refreshLayout();
            }
        });

        ll_date5 = root_view.findViewById(R.id.filter_frag_ll_date5);
        tv_date5 = root_view.findViewById(R.id.filter_frag_tv_date5);
        v_line5 = root_view.findViewById(R.id.filter_frag_line5);
        tv_week5 = root_view.findViewById(R.id.filter_frag_tv_weekday5);
        ll_date5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bDate[0] && !bDate[1] && !bDate[2] && !bDate[3] && bDate[4] && !bDate[5] && !bDate[6]){

                } else{
                    bDate[4] = !bDate[4];
                }
                refreshLayout();
            }
        });

        ll_date6 = root_view.findViewById(R.id.filter_frag_ll_date6);
        tv_date6 = root_view.findViewById(R.id.filter_frag_tv_date6);
        v_line6 = root_view.findViewById(R.id.filter_frag_line6);
        tv_week6 = root_view.findViewById(R.id.filter_frag_tv_weekday6);
        ll_date6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bDate[0] && !bDate[1] && !bDate[2] && !bDate[3] && !bDate[4] && bDate[5] && !bDate[6]){

                } else{
                    bDate[5] = !bDate[5];
                }
                refreshLayout();
            }
        });

        ll_date7 = root_view.findViewById(R.id.filter_frag_ll_date7);
        tv_date7 = root_view.findViewById(R.id.filter_frag_tv_date7);
        v_line7 = root_view.findViewById(R.id.filter_frag_line7);
        tv_week7 = root_view.findViewById(R.id.filter_frag_tv_weekday7);
        ll_date7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bDate[0] && !bDate[1] && !bDate[2] && !bDate[3] && !bDate[4] && !bDate[5] && bDate[6]){

                } else{
                    bDate[6] = !bDate[6];
                }
                refreshLayout();
            }
        });

        tv_age = root_view.findViewById(R.id.filter_frag_tv_age);
        sb_age = root_view.findViewById(R.id.filter_frag_sb_age);
        sb_age.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                tv_age_min.setText((int)leftValue + "");
                tv_age_max.setText((int)rightValue + "");
                parent.filter_opt.min_age = (int)leftValue;
                parent.filter_opt.max_age = (int)rightValue;
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

        tv_age_min = root_view.findViewById(R.id.filter_frag_tv_min);
        tv_age_max = root_view.findViewById(R.id.filter_frag_tv_max);
    }

    private void selectDate(LinearLayout lDate, TextView tvDate, TextView tvWeek, View vLine){
        lDate.setBackgroundResource(R.drawable.border_w_back_b);
        tvDate.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
        tvWeek.setTextColor(ContextCompat.getColor(context,R.color.colorWhite));
        vLine.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
    }

    private void unselectDate(LinearLayout lDate, TextView tvDate, TextView tvWeek, View vLine){
        lDate.setBackgroundResource(R.drawable.border_b_back_w);
        tvDate.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        tvWeek.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
        vLine.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDGray));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
