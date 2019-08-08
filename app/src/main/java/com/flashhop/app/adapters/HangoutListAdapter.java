package com.flashhop.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.HangoutModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HangoutListAdapter extends RecyclerView.Adapter<HangoutListAdapter.ViewHolder> implements Filterable {

    private List<HangoutModel> mDataSet;
    private List<HangoutModel> filteredList;
    private OnItemClickListener mOnItemClickListener;
    private HangoutFilter mFilter;

    private final Context mContext;

    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTxt;
        public final CircleImageView iv_avatar;
        public final ImageView[] iv_image = new ImageView[9];
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            nameTxt = v.findViewById(R.id.hangouts_item_tv_name);
            iv_avatar = v.findViewById(R.id.hangouts_item_civ_avatar);
            iv_image[0] = v.findViewById(R.id.hangouts_item_iv_0);
            iv_image[1] = v.findViewById(R.id.hangouts_item_iv_1);
            iv_image[2] = v.findViewById(R.id.hangouts_item_iv_2);
            iv_image[3] = v.findViewById(R.id.hangouts_item_iv_3);
            iv_image[4] = v.findViewById(R.id.hangouts_item_iv_4);
            iv_image[5] = v.findViewById(R.id.hangouts_item_iv_5);
            iv_image[6] = v.findViewById(R.id.hangouts_item_iv_6);
            iv_image[7] = v.findViewById(R.id.hangouts_item_iv_7);
            iv_image[8] = v.findViewById(R.id.hangouts_item_iv_8);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public HangoutListAdapter(List<HangoutModel> dataSet, Context context) {
        mDataSet = dataSet;
        filteredList = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_hangouts_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        HangoutModel one = filteredList.get(position);

        viewHolder.nameTxt.setText(one.name);
        Picasso.get()
                .load(one.photo)
                .into(viewHolder.iv_avatar);
        if(one.action.equals("like")){
            viewHolder.iv_image[0].setVisibility(View.VISIBLE);
            int nSel = one.nCount % 3;
            if(nSel == 0){
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_liked);
            } else if(nSel == 1){
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_liked_3);
            } else {
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_liked_6);
            }
            for(int i = 1; i < 9; i++){
                viewHolder.iv_image[i].setVisibility(View.GONE);
                if(one.nCount > i * 3){
                    viewHolder.iv_image[i].setVisibility(View.VISIBLE);
                    viewHolder.iv_image[i].setImageResource(R.drawable.ic_liked);
                }
            }
        } else if(one.action.equals("dislike")){
            viewHolder.iv_image[0].setVisibility(View.VISIBLE);
            int nSel = one.nCount % 3;
            if(nSel == 0){
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_dissed);
            } else if(nSel == 1){
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_dissed_3);
            } else {
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_dissed_6);
            }
            for(int i = 1; i < 9; i++){
                viewHolder.iv_image[i].setVisibility(View.GONE);
                if(one.nCount > i * 3){
                    viewHolder.iv_image[i].setVisibility(View.VISIBLE);
                    viewHolder.iv_image[i].setImageResource(R.drawable.ic_dissed);
                }
            }
        } else{
            viewHolder.iv_image[0].setVisibility(View.VISIBLE);
            int nSel = one.nCount % 3;
            if(nSel == 0){
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_thunder);
            } else if(nSel == 1){
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_thunder_3);
            } else {
                viewHolder.iv_image[0].setImageResource(R.drawable.ic_thunder_6);
            }
            for(int i = 1; i < 9; i++){
                viewHolder.iv_image[i].setVisibility(View.GONE);
                if(one.nCount > i * 3){
                    viewHolder.iv_image[i].setVisibility(View.VISIBLE);
                    viewHolder.iv_image[i].setImageResource(R.drawable.ic_thunder);
                }
            }
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

    public void setDataList(List<HangoutModel> dataSet){
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
            mFilter = new HangoutFilter();
        }
        return mFilter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class HangoutFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<HangoutModel> tempList = new ArrayList<HangoutModel>();

                // search content in friend list
                for (HangoutModel one : mDataSet) {
                    if (one.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
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
            filteredList = (ArrayList<HangoutModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
