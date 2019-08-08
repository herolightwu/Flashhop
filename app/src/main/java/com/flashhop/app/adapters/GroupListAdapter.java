package com.flashhop.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.ChatGroupModel;
import com.flashhop.app.models.UserModel;
import com.flashhop.app.utils.TxtUtils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> implements Filterable {

    private List<ChatGroupModel> mDataSet;
    private List<ChatGroupModel> filteredList;
    private OnItemClickListener mOnItemClickListener;
    private ChatGroupFilter mFilter;

    private final Context mContext;

    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView remainTxt, dateTxt, weekTxt, titleTxt, subjectTxt;
        public final RelativeLayout rl_avatar;
        public final ImageView iv_badge;
        public final CircleImageView[] civ_avatar = new CircleImageView[4];
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            remainTxt = v.findViewById(R.id.group_list_item_tv_remain);
            subjectTxt = v.findViewById(R.id.group_list_item_tv_subject);
            dateTxt = v.findViewById(R.id.group_list_item_tv_date);
            weekTxt = v.findViewById(R.id.group_list_item_tv_weekday);
            titleTxt = v.findViewById(R.id.group_list_item_tv_title);
            iv_badge = v.findViewById(R.id.group_list_item_iv_badge);
            rl_avatar = v.findViewById(R.id.group_list_item_rl_avatar);
            civ_avatar[0] = v.findViewById(R.id.group_list_item_civ_avatar0);
            civ_avatar[1] = v.findViewById(R.id.group_list_item_civ_avatar1);
            civ_avatar[2] = v.findViewById(R.id.group_list_item_civ_avatar2);
            civ_avatar[3] = v.findViewById(R.id.group_list_item_civ_avatar3);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public GroupListAdapter(List<ChatGroupModel> dataSet, Context context) {
        mDataSet = dataSet;
        filteredList = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_chat_list_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ChatGroupModel one = filteredList.get(position);

        viewHolder.titleTxt.setText(one.event.title);
        viewHolder.subjectTxt.setText(one.subject);
        if(one.bUnread){
            viewHolder.iv_badge.setVisibility(View.VISIBLE);
        } else{
            viewHolder.iv_badge.setVisibility(View.GONE);
        }
        try{
            SimpleDateFormat sDf = new SimpleDateFormat("MMM dd yyyy");
            Date dt = sDf.parse(one.event.date);
            SimpleDateFormat eDf = new SimpleDateFormat("EEE");
            String weekday = eDf.format(dt);
            viewHolder.weekTxt.setText(weekday);
        } catch (ParseException ex){
            ex.printStackTrace();
        }

        String[] sDate = one.event.date.split(" ");
        viewHolder.dateTxt.setText(sDate[0] + "\n" + sDate[1]);
        if(one.event.creator != null){
            Picasso.get()
                    .load(one.event.creator.photo_url)
                    .into(viewHolder.civ_avatar[0]);
            viewHolder.civ_avatar[0].setVisibility(View.VISIBLE);
        } else{
            viewHolder.civ_avatar[0].setVisibility(View.GONE);
        }

        viewHolder.civ_avatar[1].setVisibility(View.GONE);
        viewHolder.civ_avatar[2].setVisibility(View.GONE);
        viewHolder.civ_avatar[3].setVisibility(View.GONE);
        int nSize = one.event.members.size() > 3 ? 3: one.event.members.size();
        for(int i = 0; i < nSize; i++){
            Picasso.get()
                    .load(one.event.members.get(i).photo_url)
                    .into(viewHolder.civ_avatar[i+1]);
            viewHolder.civ_avatar[i+1].setVisibility(View.VISIBLE);
        }
        long diff = TxtUtils.getDifferenceChatTime(one.event.date, one.event.time);
        long l_tt = diff/(60 * 24);
        if(l_tt == 0){
            l_tt = diff/60;
            if(l_tt <= 1){
                l_tt = diff;
                viewHolder.remainTxt.setText(l_tt + " minutes left");
            } else{
                viewHolder.remainTxt.setText(l_tt + " hours left");
            }
        } else{
            viewHolder.remainTxt.setText(l_tt + " days left");
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setDataList(List<ChatGroupModel> dataSet){
        mDataSet = dataSet;
        filteredList = dataSet;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public int onItemClick(int pos);
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ChatGroupFilter();
        }
        return mFilter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class ChatGroupFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<ChatGroupModel> tempList = new ArrayList<ChatGroupModel>();

                // search content in friend list
                for (ChatGroupModel one : mDataSet) {
                    if (one.event.title.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(one);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = mDataSet.size();
                filterResults.values = mDataSet;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<ChatGroupModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
