package com.flashhop.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.CommentModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<CommentModel> mDataSet;

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
        public final TextView tv_name, tv_time, tv_content;
        public final CircleImageView civ_avatar;
        public final LinearLayout ll_like, ll_comment;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            civ_avatar = v.findViewById(R.id.chat_text_item_civ_avatar);
            tv_name = v.findViewById(R.id.chat_text_item_tv_name);
            tv_content = v.findViewById(R.id.chat_text_item_tv_msg);
            tv_time = v.findViewById(R.id.chat_text_item_tv_time);
            ll_like = v.findViewById(R.id.chat_text_item_ll_like);
            ll_comment = v.findViewById(R.id.chat_text_item_ll_comment);
            //tv_delete = v.findViewById(R.id.chat_text_item_tv_delete);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public CommentAdapter(List<CommentModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_chat_text, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        CommentModel one = mDataSet.get(position);
        if(one.uPhoto.length() > 0){
            Picasso.get()
                    .load(one.uPhoto)
                    .into(viewHolder.civ_avatar);
        }

        viewHolder.tv_name.setText(one.uName);
        viewHolder.tv_time.setText(getTime(one.lTime*1000L));
        viewHolder.tv_content.setText(one.sMsg);
        viewHolder.ll_like.setVisibility(View.GONE);
        viewHolder.ll_comment.setVisibility(View.GONE);
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

    public void setDataList(List<CommentModel> dataSet){
        mDataSet = dataSet;
    }

    public interface OnItemClickListener {
        public int onItemClick(int pos);
    }

    private String getTime(long timeStamp){

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }
}
