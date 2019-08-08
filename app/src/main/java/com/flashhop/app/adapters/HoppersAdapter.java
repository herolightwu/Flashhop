package com.flashhop.app.adapters;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.models.HopperModel;
import com.flashhop.app.models.UserModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HoppersAdapter extends ArrayAdapter<HopperModel> {

    Context context;
    ViewHolder viewHolder;
    List<HopperModel> members = new ArrayList<>();
    public boolean bViewAll = false;
    public boolean bMyEvent = false;
    public boolean bFreeEvent = false;
    public String creator_id = "";

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public HoppersAdapter(Context context, List<HopperModel> al_menu) {
        super(context, R.layout.grid_groupinfo_item, al_menu);
        this.members = al_menu;
        this.context = context;
    }

    @Override
    public int getCount() {
        if(bViewAll){
            return members.size();
        } else{
            return members.size() > 9 ? 10:members.size();
        }
    }

    public void setDataList(List<HopperModel> dataList){
        members = dataList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        HopperModel one = members.get(position);
        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_groupinfo_item, parent, false);
            viewHolder.itemview = convertView.findViewById(R.id.groupinfo_item_view);
            viewHolder.tv_name = convertView.findViewById(R.id.groupinfo_item_tv_name);
            viewHolder.iv_check = convertView.findViewById(R.id.groupinfo_item_iv_check);
            viewHolder.iv_card = convertView.findViewById(R.id.groupinfo_item_iv_card);
            viewHolder.civ_avatar = convertView.findViewById(R.id.groupinfo_item_civ_avatar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.get()
                .load(members.get(position).avatar)
                .into(viewHolder.civ_avatar);
        viewHolder.tv_name.setText(one.name);

        if(!bViewAll && position == 9){
            viewHolder.civ_avatar.setVisibility(View.GONE);
            viewHolder.iv_card.setVisibility(View.GONE);
            viewHolder.iv_check.setVisibility(View.GONE);
            viewHolder.tv_name.setText(R.string.btn_view_all);
        } else{
            if(bFreeEvent){
                viewHolder.iv_card.setVisibility(View.GONE);
                viewHolder.iv_check.setVisibility(View.GONE);
            } else{
                viewHolder.civ_avatar.setVisibility(View.VISIBLE);
                if(one.nPaid == 1){
                    if(one.nOffline == 0){
                        viewHolder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                    } else{
                        viewHolder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                    }
                    if(bMyEvent){
                        viewHolder.iv_card.setVisibility(View.VISIBLE);
                        viewHolder.iv_check.setVisibility(View.GONE);
                    } else{
                        viewHolder.iv_card.setVisibility(View.GONE);
                        viewHolder.iv_check.setVisibility(View.VISIBLE);
                        viewHolder.iv_check.setImageResource(R.drawable.ic_checked);
                    }
                } else{
                    viewHolder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.colorDGray));
                    if(bMyEvent){
                        viewHolder.iv_card.setVisibility(View.GONE);
                        viewHolder.iv_check.setVisibility(View.VISIBLE);
                        viewHolder.iv_check.setImageResource(R.drawable.ic_unchecked);
                    } else{
                        viewHolder.iv_card.setVisibility(View.GONE);
                        viewHolder.iv_check.setVisibility(View.GONE);
                    }
                }
            }
            if(one.uid.equals(creator_id)){
                viewHolder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                viewHolder.civ_avatar.setColorFilter(new ColorMatrixColorFilter(matrix));
                viewHolder.iv_card.setVisibility(View.GONE);
                viewHolder.iv_check.setVisibility(View.GONE);
            }
        }

        viewHolder.itemview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
        return convertView;

    }

    private static class ViewHolder {
        CircleImageView civ_avatar;
        ImageView iv_check, iv_card;
        TextView tv_name;
        RelativeLayout itemview;
    }

    public interface OnItemClickListener {
        public int onItemClick(int pos);
    }
}
