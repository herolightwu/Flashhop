package com.flashhop.app.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.flashhop.app.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class GridAdapter extends ArrayAdapter<String> {

    Context context;
    ViewHolder viewHolder;
    ArrayList<String> al_photos = new ArrayList<>();
    public int nSelect = -1;

    public GridAdapter(Context context, ArrayList<String> al_menu) {
        super(context, R.layout.grid_gallery_item, al_menu);
        this.al_photos = al_menu;
        this.context = context;
    }

    @Override
    public int getCount() {
        return al_photos.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_gallery_item, parent, false);
            viewHolder.iv_image = convertView.findViewById(R.id.grid_item_iv_photo);
            viewHolder.iv_check = convertView.findViewById(R.id.grid_item_iv_check);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.get()
                .load(Uri.fromFile(new File(al_photos.get(position))))
                .resize(200, 200)
                .into(viewHolder.iv_image);
        //Bitmap myBitmap = BitmapFactory.decodeFile(al_photos.get(position));
        //viewHolder.iv_image.setImageBitmap(myBitmap);

        if(position == nSelect){
            viewHolder.iv_check.setVisibility(View.VISIBLE);
        } else{
            viewHolder.iv_check.setVisibility(View.GONE);
        }

        viewHolder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nSelect == position){
                    nSelect = -1;
                } else{
                    nSelect = position;
                }
                notifyDataSetChanged();
            }
        });

        return convertView;

    }

    public static class ViewHolder {
        public ImageView iv_image;
        public ImageView iv_check;
    }


}
