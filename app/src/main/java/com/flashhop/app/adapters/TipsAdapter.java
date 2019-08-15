package com.flashhop.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.TipModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.ViewHolder> {

    private List<TipModel> mDataSet;

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
        public final TextView tv_name, tv_receive, tv_give;
        public final CircleImageView civ_avatar;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            civ_avatar = v.findViewById(R.id.tips_item_civ_avatar);
            tv_name = v.findViewById(R.id.tips_item_tv_name);
            tv_receive = v.findViewById(R.id.tips_item_tv_receive);
            tv_give = v.findViewById(R.id.tips_item_tv_give);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public TipsAdapter(List<TipModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_tips_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        TipModel one = mDataSet.get(position);
        Picasso.get()
                .load(one.photo)
                .into(viewHolder.civ_avatar);
        viewHolder.tv_receive.setText("$ " + one.receiving);
        viewHolder.tv_give.setText(one.datestr);
        viewHolder.tv_name.setText(one.name);

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

    public void setDataList(List<TipModel> dataSet){
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public int onItemClick(int pos);
    }
}
