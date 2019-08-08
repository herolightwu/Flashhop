package com.flashhop.app.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.GalleryAdapter;
import com.flashhop.app.models.Model_images;
import com.flashhop.app.models.PhotoModel;

import java.util.ArrayList;

public class OneGalleryFragment extends DialogFragment {

    HomeActivity parent;
    ImageView iv_back;
    GridView gv_gallery;
    GalleryAdapter galleryAdapter;

    ArrayList<Model_images> al_images = new ArrayList<>();
    ArrayList<PhotoModel> photo_list = new ArrayList<>();
    ArrayList<ImageView> sel_iv_list = new ArrayList<>();
    ArrayList<String> path_list = new ArrayList<>();
    boolean boolean_folder;

    public OneGalleryFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rView = inflater.inflate(R.layout.fragment_one_gallery, container, false);
        parent = (HomeActivity) getActivity();
        initLayout(rView);
        return rView;
    }

    private void initLayout(View view){
        iv_back = view.findViewById(R.id.one_gallery_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        gv_gallery = view.findViewById(R.id.one_gallery_frag_grid);
        fn_imagespath();

        galleryAdapter = new GalleryAdapter(getContext(), photo_list);
        gv_gallery.setAdapter(galleryAdapter);
        gv_gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                parent.event_photo = photo_list.get(i).path;
                dismiss();
                parent.refreshHostEventPhoto();
            }
        });
    }

    private void refreshSelectedPhotos(){
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshSelectedPhotos();
    }

    public ArrayList<Model_images> fn_imagespath() {
        al_images.clear();

        int int_position = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;

        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        //cursor = parent.getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        cursor = parent.getApplicationContext().getContentResolver().query(uri, projection, null,null,MediaStore.MediaColumns.DATE_ADDED + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            //Log.e("Column", absolutePathOfImage);
            //Log.e("Folder", cursor.getString(column_index_folder_name));
            for (int i = 0; i < al_images.size(); i++) {
                if (al_images.get(i).str_folder.equals(cursor.getString(column_index_folder_name))) {
                    boolean_folder = true;
                    int_position = i;
                    break;
                } else {
                    boolean_folder = false;
                }
            }

            if (boolean_folder) {
                ArrayList<String> al_path = new ArrayList<>();
                al_path=al_images.get(int_position).al_imagepath;
                al_path.add(absolutePathOfImage);
                al_images.get(int_position).al_imagepath = al_path;

            } else {
                ArrayList<String> al_path = new ArrayList<>();
                al_path.add(absolutePathOfImage);
                Model_images obj_model = new Model_images();
                obj_model.str_folder = cursor.getString(column_index_folder_name);
                obj_model.al_imagepath = al_path;
                al_images.add(obj_model);
            }

        }

        for (int i = 0; i < al_images.size(); i++) {
            for (int j = 0; j < al_images.get(i).al_imagepath.size(); j++) {
                PhotoModel one = new PhotoModel();
                one.path = al_images.get(i).al_imagepath.get(j);
                one.bSel = false;
                photo_list.add(one);
            }
        }
        return al_images;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
