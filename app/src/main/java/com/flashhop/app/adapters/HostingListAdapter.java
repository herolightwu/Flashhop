package com.flashhop.app.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.utils.Const;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class HostingListAdapter extends RecyclerView.Adapter<HostingListAdapter.ViewHolder> {

    private List<EventModel> mDataSet;

    private OnEditClickListener mOnEditClickListener;
    private OnCancelClickListener mOnCancelClickListener;

    private final Context mContext;

    public void setOnEditClickListener (OnEditClickListener listener) {
        mOnEditClickListener = listener;
    }

    public void setOnCancelClickListener (OnCancelClickListener listener){
        mOnCancelClickListener = listener;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom {@link RecyclerView.ViewHolder}).
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tv_title, tv_status;
        public final Button btn_edit, btn_cancel;
        public final ImageView iv_image;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            iv_image = v.findViewById(R.id.list_hosting_item_iv_image);
            tv_title = v.findViewById(R.id.list_hosting_item_tv_title);
            tv_status = v.findViewById(R.id.list_hosting_item_tv_status);
            btn_edit = v.findViewById(R.id.list_hosting_item_btn_edit);
            btn_cancel = v.findViewById(R.id.list_hosting_item_btn_cancel);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public HostingListAdapter(List<EventModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_hosting_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        EventModel one = mDataSet.get(position);
        if(one.photo.length() > 0){
            if(one.photo.indexOf("http://") != -1 || one.photo.indexOf("https://") != -1){
                Picasso.get()
                        .load(one.photo)
                        .into(viewHolder.iv_image);
            } else{
                Picasso.get()
                        .load(Uri.fromFile(new File(one.photo)))
                        .resize(200, 200)
                        .into(viewHolder.iv_image);
            }
        } else{
            for(int i = 0; i < 9; i++){
                if(Const.interest_list[i].toLowerCase().equals(one.category.toLowerCase())){
                    viewHolder.iv_image.setImageResource(Const.cover_res[i+1]);
                    break;
                }
            }

        }
        viewHolder.tv_title.setText(one.title);
        if(one.id.length() == 0){
            viewHolder.tv_status.setText(R.string.btn_draft);
            viewHolder.tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.colorGray));
        } else{
            viewHolder.tv_status.setText(R.string.btn_publish);
            viewHolder.tv_status.setTextColor(ContextCompat.getColor(mContext, R.color.colorGreen));
        }

        viewHolder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnEditClickListener != null){
                    mOnEditClickListener.onEditClick(position);
                }
            }
        });

        viewHolder.btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnCancelClickListener != null){
                    mOnCancelClickListener.onCancelClick(position);
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

    public interface OnEditClickListener {
        public int onEditClick(int pos);
    }

    public interface OnCancelClickListener {
        public int onCancelClick(int pos);
    }
}
