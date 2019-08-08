package com.flashhop.app.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.models.GridViewItem;
import com.flashhop.app.models.InviteUserModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InviteGridAdapter extends BaseAdapter implements Filterable {

    Context context;
    ViewHolder viewHolder;
    List<InviteUserModel> user_list = new ArrayList<>();
    List<InviteUserModel> filter_list = new ArrayList<>();
    private InviteUserFilter filter;

    public InviteGridAdapter(Context context, List<InviteUserModel> ulist) {
        this.user_list = ulist;
        this.filter_list = ulist;
        this.context = context;
    }

    @Override
    public int getCount() {
        return filter_list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return filter_list.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_invite_item, parent, false);
            viewHolder.iv_image = convertView.findViewById(R.id.grid_invite_item_civ_photo);
            viewHolder.tv_name = convertView.findViewById(R.id.grid_invite_item_name);
            viewHolder.root_view = convertView.findViewById(R.id.grid_item_layout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(filter_list.get(position).photo.indexOf("http") == -1){
            Picasso.get()
                    .load(Uri.fromFile(new File(filter_list.get(position).photo)))
                    .resize(100, 100)
                    .into(viewHolder.iv_image);
        } else{
            Picasso.get()
                    .load(filter_list.get(position).photo)
                    .resize(100, 100)
                    .into(viewHolder.iv_image);
        }

        viewHolder.tv_name.setText(filter_list.get(position).name);

        if(filter_list.get(position).bChoose){
            viewHolder.iv_image.setBorderWidth(3);
            viewHolder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.colorRed));
        } else{
            viewHolder.iv_image.setBorderWidth(0);
            viewHolder.tv_name.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }

        return convertView;

    }

    public void setData(List<InviteUserModel> ulist){
        user_list = ulist;
        filter_list = ulist;
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        public GridViewItem root_view;
        public CircleImageView iv_image;
        public TextView tv_name;
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new InviteUserFilter();
        }
        return filter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class InviteUserFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<InviteUserModel> tempList = new ArrayList<InviteUserModel>();

                // search content in friend list
                for (InviteUserModel one : user_list) {
                    if (one.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(one);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = user_list.size();
                filterResults.values = user_list;
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
            filter_list = (ArrayList<InviteUserModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
