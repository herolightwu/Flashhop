package com.flashhop.app.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.fragments.PublishEventFragment;
import com.flashhop.app.fragments.UserProfileFragment;
import com.flashhop.app.models.AlarmModel;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.TxtUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AlarmMeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AlarmModel> mDataSet;

    private OnMsgNoClickListener onMsgNoClickListener;
    private OnMsgYetClickListener onMsgYetClickListener;
    private OnMsgTooClickListener onMsgTooClickListener;
    private OnEventActionClickListener onEventActionClickListener;

    private final Context mContext;
    private final HomeActivity parent;

    public void setOnEventActionClickListener (OnEventActionClickListener listener) {
        onEventActionClickListener = listener;
    }

    public void setOnMsgNoClickListener (OnMsgNoClickListener listener) {
        onMsgNoClickListener = listener;
    }

    public void setOnMsgYetClickListener (OnMsgYetClickListener listener) {
        onMsgYetClickListener = listener;
    }

    public void setOnMsgTooClickListener (OnMsgTooClickListener listener) {
        onMsgTooClickListener = listener;
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public AlarmMeAdapter(List<AlarmModel> dataSet, Context context, HomeActivity act) {
        mDataSet = dataSet;
        mContext = context;
        parent = act;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v;
        switch (viewType){
            case Const.ALARM_ITEM_TYPE_MSG:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_alarm_me_msg_item, viewGroup, false);
                return new ItemMsgAlarmHolder(v);
            case Const.ALARM_ITEM_TYPE_EVENT:
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_alarm_me_event_item, viewGroup, false);
                return new ItemEventAlarmHolder(v);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        AlarmModel one = mDataSet.get(position);

        if(viewHolder instanceof ItemMsgAlarmHolder){
            ((ItemMsgAlarmHolder)viewHolder).tv_content.setText(fromHtml(one.sDesc));
            Picasso.get()
                    .load(one.uPhoto)
                    .into(((ItemMsgAlarmHolder)viewHolder).civ_avatar);

            ((ItemMsgAlarmHolder)viewHolder).ll_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(one.user != null){
                        gotoUserProfile(one.user);
                    }
                }
            });
            ((ItemMsgAlarmHolder)viewHolder).btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onMsgNoClickListener != null){
                        onMsgNoClickListener.onMsgNoClick(position);
                    }
                }
            });
            ((ItemMsgAlarmHolder)viewHolder).btn_yet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onMsgYetClickListener != null){
                        onMsgYetClickListener.onMsgYetClick(position);
                    }
                }
            });
            ((ItemMsgAlarmHolder)viewHolder).btn_too.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onMsgTooClickListener != null){
                        onMsgTooClickListener.onMsgTooClick(position);
                    }
                }
            });

            if(one.action.equals("requested_friend")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                if(one.is_checked == 1){
                    ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                    ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                } else{
                    ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.VISIBLE);
                    ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.VISIBLE);
                }
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setText(R.string.btn_decline);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setText(R.string.btn_accept);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.GONE);
            } else if(one.action.equals("liked")){
                if(one.is_checked == 1){
                    ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                    ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                    ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                } else{
                    ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.VISIBLE);
                    ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.VISIBLE);
                    ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.VISIBLE);
                }

                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.VISIBLE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_liked);
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setText(R.string.like_not_yet);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setText(R.string.like_hell_no);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setText(R.string.like_me_too);
            } else if(one.action.equals("disliked")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                if(one.is_checked == 1){
                    ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                    ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                } else{
                    ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.VISIBLE);
                    ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.VISIBLE);
                }

                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.VISIBLE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_dissed);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setText(R.string.diss_whatever);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setText(R.string.diss_throw_back);
            } else if(one.action.equals("accept_friend_request")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.GONE);
            } else if(one.action.equals("me_too")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.VISIBLE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_liked);
            } else if(one.action.equals("throw_back")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.VISIBLE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setImageResource(R.drawable.ic_dissed);
            } else if(one.action.equals("non_friend_invite")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.GONE);
            } else if(one.action.equals("friend_invite")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.GONE);
            } else if(one.action.equals("tagged")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.GONE);
            } else if(one.action.equals("tipped")){
                ((ItemMsgAlarmHolder)viewHolder).btn_yet.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_no.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).btn_too.setVisibility(View.GONE);
                ((ItemMsgAlarmHolder)viewHolder).iv_like.setVisibility(View.GONE);
            }

        } else if(viewHolder instanceof ItemEventAlarmHolder){
            if(one.event.photo.length() > 0){
                Picasso.get()
                        .load(one.event.photo)
                        .into(((ItemEventAlarmHolder)viewHolder).iv_image);
            } else{
                for(int i = 0; i < 9; i++){
                    if(Const.interest_list[i].toLowerCase().equals(one.event.category.toLowerCase())){
                        ((ItemEventAlarmHolder)viewHolder).iv_image.setImageResource(Const.cover_res[i+1]);
                        break;
                    }
                }
            }

            ((ItemEventAlarmHolder)viewHolder).iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPublishEvent(one.event);
                }
            });

            ((ItemEventAlarmHolder)viewHolder).tv_content.setText(fromHtml(one.sDesc));
            ((ItemEventAlarmHolder)viewHolder).btn_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onEventActionClickListener != null){
                        onEventActionClickListener.onEventActionClick(position);
                    }
                }
            });

            String[] sTime = one.event.time.split("-");
            long s_diff = TxtUtils.getDifferenceTime(one.event.date, one.event.time);
            long e_diff = s_diff + 120;
            if(sTime.length > 1){
                e_diff = TxtUtils.getDifferenceEndTime(one.event.date, one.event.time);
            }

            if(one.action.equals("ping_2hours_event")){
                ((ItemEventAlarmHolder)viewHolder).btn_action.setVisibility(View.GONE);
            } else if(one.action.equals("ping_30mins_event")){
                if(e_diff > 0){
                    ((ItemEventAlarmHolder)viewHolder).btn_action.setVisibility(View.VISIBLE);
                    ((ItemEventAlarmHolder)viewHolder).btn_action.setText(R.string.btn_where_hoppers);
                } else{
                    ((ItemEventAlarmHolder)viewHolder).btn_action.setVisibility(View.GONE);
                }

            } else{ //"ping_less_member"
                if(s_diff > 0){
                    ((ItemEventAlarmHolder)viewHolder).btn_action.setVisibility(View.VISIBLE);
                    ((ItemEventAlarmHolder)viewHolder).btn_action.setText(R.string.btn_reedit_event);
                } else{
                    ((ItemEventAlarmHolder)viewHolder).btn_action.setVisibility(View.GONE);
                }

            }
        }
    }

    private void gotoUserProfile(UserModel other){
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        UserProfileFragment userFrag = new UserProfileFragment();
        userFrag.other = other;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        transaction.add(R.id.home_frame, userFrag, "USER_FRAG").addToBackStack("USER_FRAG").commit();
    }

    private void showPublishEvent(EventModel event){
        if(event.creator == null) return;
        FragmentManager fragmentManager = parent.getSupportFragmentManager();
        PublishEventFragment pubeventFrag = new PublishEventFragment();
        pubeventFrag.pub_Event = event;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_slide_in_up, R.anim.ani_slide_in_down, R.anim.ani_slide_in_up, R.anim.ani_slide_in_down);
        transaction.add(R.id.home_frame, pubeventFrag, Const.FRAG_PUBLISH_EVENT_TAG).addToBackStack(Const.FRAG_PUBLISH_EVENT_TAG).commit();
    }

    @SuppressWarnings("deprecation")
    public Spanned fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setDataList(List<AlarmModel> dataSet){
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    //return view type
    @Override
    public int getItemViewType(int postion){
        AlarmModel one = mDataSet.get(postion);
        int vtype = Const.ALARM_ITEM_TYPE_MSG;
        if(one.action.equals("ping_2hours_event") || one.action.equals("ping_30mins_event") || one.action.equals("ping_less_member")){
            vtype = Const.ALARM_ITEM_TYPE_EVENT;
        }
        return vtype;
    }

    public interface OnEventActionClickListener {
        public int onEventActionClick(int pos);
    }

    public interface OnMsgNoClickListener {
        public int onMsgNoClick(int pos);
    }

    public interface OnMsgYetClickListener {
        public int onMsgYetClick(int pos);
    }

    public interface OnMsgTooClickListener {
        public int onMsgTooClick(int pos);
    }
}

class ItemMsgAlarmHolder extends RecyclerView.ViewHolder{
    public CircleImageView civ_avatar;
    public TextView tv_content;
    public ImageView iv_like;
    public LinearLayout ll_avatar;
    public Button btn_no, btn_yet, btn_too;

    public ItemMsgAlarmHolder(View itemV){
        super(itemV);
        civ_avatar = itemV.findViewById(R.id.alarm_msg_item_civ_avatar);
        tv_content = itemV.findViewById(R.id.alarm_msg_item_tv_content);
        btn_no = itemV.findViewById(R.id.alarm_msg_item_btn_hell);
        btn_yet = itemV.findViewById(R.id.alarm_msg_item_btn_yet);
        btn_too = itemV.findViewById(R.id.alarm_msg_item_btn_too);
        iv_like = itemV.findViewById(R.id.alarm_msg_item_iv_like);
        ll_avatar = itemV.findViewById(R.id.alarm_msg_item_ll_avatar);
    }
}

class ItemEventAlarmHolder extends RecyclerView.ViewHolder{
    public ImageView iv_image;
    public TextView tv_content;
    public Button btn_action;

    public ItemEventAlarmHolder(View itemV){
        super(itemV);
        iv_image = itemV.findViewById(R.id.alarm_event_item_iv_image);
        tv_content = itemV.findViewById(R.id.alarm_event_item_tv_content);
        btn_action = itemV.findViewById(R.id.alarm_msg_item_btn_action);
    }
}
