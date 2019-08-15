package com.flashhop.app.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.AddressModel;

import java.util.ArrayList;
import java.util.List;

public class AddressListAdapter extends RecyclerView.Adapter<AddressListAdapter.ViewHolder> implements Filterable {

    private List<AddressModel> mDataSet;
    private List<AddressModel> filteredList;
    private OnItemClickListener mOnItemClickListener;
    private ContactFilter contactFilter;

    public int selPos = -1;

    private final Context mContext;

    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tv_local, tv_full;
        public final LinearLayout ll_back;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            tv_local = v.findViewById(R.id.address_item_tv_locality);
            tv_full = v.findViewById(R.id.address_item_tv_full);
            ll_back = v.findViewById(R.id.address_item_ll_back);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public AddressListAdapter(List<AddressModel> dataSet, Context context) {
        mDataSet = dataSet;
        filteredList = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_address_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        AddressModel one = filteredList.get(position);

        viewHolder.tv_local.setText(one.localAddr);
        viewHolder.tv_full.setText(one.fullAddr);

        if(selPos == position){
            viewHolder.ll_back.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorGray_F));
        } else{
            viewHolder.ll_back.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorWhite));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null) {
                    //WalletContactModel one = filteredList.get(position);
                    mOnItemClickListener.onItemClick(position);
                    if(selPos != -1)
                        notifyItemChanged(selPos);
                    selPos = position;
                    notifyItemChanged(position);
                }
            }
        });
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setDataList(List<AddressModel> dataSet){
        selPos = -1;
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
        if (contactFilter == null) {
            contactFilter = new ContactFilter();
        }
        return contactFilter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class ContactFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<AddressModel> tempList = new ArrayList<AddressModel>();

                // search content in friend list
                for (AddressModel one : mDataSet) {
                    if (one.localAddr.toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            one.fullAddr.toLowerCase().contains(constraint.toString().toLowerCase())) {
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
            filteredList = (ArrayList<AddressModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
