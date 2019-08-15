package com.flashhop.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.utils.Const;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SavedListAdapter extends RecyclerView.Adapter<SavedListAdapter.ViewHolder> {

    private List<EventModel> mDataSet;

    private OnItemClickListener mOnItemClickListener;

    private final Context mContext;

    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tv_month, tv_day, tv_weekday, tv_Title, tv_time, tv_addr;
        public final ImageView iv_image;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            iv_image = v.findViewById(R.id.list_event_item_iv_image);
            tv_day = v.findViewById(R.id.list_event_item_tv_day);
            tv_month = v.findViewById(R.id.list_event_item_tv_month);
            tv_weekday = v.findViewById(R.id.list_event_item_tv_weekday);
            tv_Title = v.findViewById(R.id.list_event_item_tv_title);
            tv_time = v.findViewById(R.id.list_event_item_tv_time);
            tv_addr = v.findViewById(R.id.list_event_item_tv_addr);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public SavedListAdapter(List<EventModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_event_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        EventModel one = mDataSet.get(position);
        if(one.photo.length() > 0){
            Picasso.get()
                    .load(one.photo)
                    .into(viewHolder.iv_image);
        } else{
            for(int i = 0; i < 9; i++){
                if(Const.interest_list[i].toLowerCase().equals(one.category.toLowerCase())){
                    viewHolder.iv_image.setImageResource(Const.cover_res[i+1]);
                    break;
                }
            }

        }
        try{
            SimpleDateFormat sDf = new SimpleDateFormat("MMM dd yyyy");
            Date dt = sDf.parse(one.date);
            SimpleDateFormat eDf = new SimpleDateFormat("EEE");
            String weekday = eDf.format(dt);
            viewHolder.tv_weekday.setText(weekday);
        } catch (ParseException ex){
            ex.printStackTrace();
        }

        String[] sDate = one.date.split(" ");
        viewHolder.tv_month.setText(sDate[0]);
        viewHolder.tv_day.setText(sDate[1]);
        viewHolder.tv_Title.setText(one.title);
        viewHolder.tv_time.setText(one.time);
        viewHolder.tv_addr.setVisibility(View.GONE);
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
        return mDataSet.size();
    }

    public void setDataList(List<EventModel> dataSet){
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public int onItemClick(int pos);
    }
}
