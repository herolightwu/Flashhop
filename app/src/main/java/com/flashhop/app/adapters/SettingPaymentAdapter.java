package com.flashhop.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.CardModel;

import java.util.List;

public class SettingPaymentAdapter extends RecyclerView.Adapter<SettingPaymentAdapter.ViewHolder> {

    private List<CardModel> mDataSet;

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
        public final TextView tv_cardnum, tv_expire;
        public final Button btn_edit;
        public final View mItemView;
        // We'll use this field to showcase matching the holder from the test.

        ViewHolder(View v) {
            super(v);
            tv_cardnum = v.findViewById(R.id.payment_card_item_tv_cardnum);
            tv_expire = v.findViewById(R.id.payment_card_item_tv_expiration);
            btn_edit = v.findViewById(R.id.payment_card_item_btn_edit);

            mItemView = v;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public SettingPaymentAdapter(List<CardModel> dataSet, Context context) {
        mDataSet = dataSet;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_payment_card_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        CardModel one = mDataSet.get(position);

        String sCard = "xxxx xxxx xxxx " + one.card_num.substring(one.card_num.length() - 4);
        viewHolder.tv_cardnum.setText(sCard);
        String ex_str = one.expire.substring(0, 1) + "/" + one.expire.substring(one.expire.length() - 2);
        viewHolder.tv_expire.setText(ex_str);
        viewHolder.btn_edit.setOnClickListener(new View.OnClickListener() {
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

    public void setDataList(List<CardModel> dataSet){
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public int onItemClick(int pos);
    }
}
