package com.flashhop.app.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flashhop.app.R;
import com.flashhop.app.models.PhotoModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends ArrayAdapter<PhotoModel> {

    Context context;
    ViewHolder viewHolder;
    ArrayList<PhotoModel> al_photos = new ArrayList<>();


    public GalleryAdapter(Context context, ArrayList<PhotoModel> al_menu) {
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
            viewHolder.rl_layout = convertView.findViewById(R.id.grid_item_layout);
            viewHolder.iv_image = convertView.findViewById(R.id.grid_item_iv_photo);
            viewHolder.iv_check = convertView.findViewById(R.id.grid_item_iv_check);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        /*Picasso.get()
                .load(Uri.fromFile(new File(al_photos.get(position).path)))
                .resize(200, 200)
                .into(viewHolder.iv_image);*/

        File file = new File(al_photos.get(position).path);
        long filesize = file.length()/1024;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        if(filesize > 1024){
            options.inSampleSize = 4;
        }
        Bitmap myBitmap = BitmapFactory.decodeFile(al_photos.get(position).path,options);
        viewHolder.iv_image.setImageBitmap(myBitmap);
        if(al_photos.get(position).bSel){
            viewHolder.iv_check.setVisibility(View.VISIBLE);
        } else{
            viewHolder.iv_check.setVisibility(View.GONE);
        }
        return convertView;

    }

    private static class ViewHolder {
        ImageView iv_image, iv_check;
        RelativeLayout rl_layout;
    }

}
