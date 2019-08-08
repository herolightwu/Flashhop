package com.flashhop.app.fragments;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.adapters.GalleryAdapter;
import com.flashhop.app.models.Model_images;
import com.flashhop.app.models.PhotoModel;

import java.util.ArrayList;

public class GalleryFragment extends DialogFragment {

    HomeActivity parent;

    ImageView iv_back;
    TextView tv_sel_count;
    ImageView iv_sel0, iv_sel1, iv_sel2, iv_sel3, iv_sel4, iv_sel5, iv_sel6;
    GridView gv_gallery;
    GalleryAdapter galleryAdapter;
    Button btn_save;
    LinearLayout ll_images;

    int sel_count = 0;
    ArrayList<Model_images> al_images = new ArrayList<>();
    ArrayList<PhotoModel> photo_list = new ArrayList<>();
    ArrayList<ImageView> sel_iv_list = new ArrayList<>();
    ArrayList<String> path_list = new ArrayList<>();
    boolean boolean_folder;

    public GalleryFragment() {
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
        View rView = inflater.inflate(R.layout.fragment_gallery, container, false);
        parent = (HomeActivity) getActivity();
        initLayout(rView);
        return rView;
    }

    private void initLayout(View view){
        iv_back = view.findViewById(R.id.gallery_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.editProData.images.clear();
                dismiss();
                parent.refreshPhotoFrag();
            }
        });

        tv_sel_count = view.findViewById(R.id.gallery_frag_tv_sel_count);
        gv_gallery = view.findViewById(R.id.gallery_frag_grid);
        ll_images = view.findViewById(R.id.gallery_frag_ll_sel_photos);
        btn_save = view.findViewById(R.id.gallery_frag_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((WelcomeActivity)getActivity()).photo_list.clear();
                parent.editProData.images.addAll(path_list);
                dismiss();
                parent.refreshPhotoFrag();
            }
        });

        iv_sel0 = view.findViewById(R.id.gallery_frag_iv_sel0);
        iv_sel1 = view.findViewById(R.id.gallery_frag_iv_sel1);
        iv_sel2 = view.findViewById(R.id.gallery_frag_iv_sel2);
        iv_sel3 = view.findViewById(R.id.gallery_frag_iv_sel3);
        iv_sel4 = view.findViewById(R.id.gallery_frag_iv_sel4);
        iv_sel5 = view.findViewById(R.id.gallery_frag_iv_sel5);
        iv_sel6 = view.findViewById(R.id.gallery_frag_iv_sel6);
        sel_iv_list.add(iv_sel0);
        sel_iv_list.add(iv_sel1);
        sel_iv_list.add(iv_sel2);
        sel_iv_list.add(iv_sel3);
        sel_iv_list.add(iv_sel4);
        sel_iv_list.add(iv_sel5);
        sel_iv_list.add(iv_sel6);

        fn_imagespath();

        DisplayMetrics dm = new DisplayMetrics();
        parent.getWindowManager().getDefaultDisplay().getMetrics(dm);

        ViewGroup.LayoutParams params = ll_images.getLayoutParams();
        params.height = dm.widthPixels/7;
        ll_images.setLayoutParams(params);

        galleryAdapter = new GalleryAdapter(getContext(), photo_list);
        gv_gallery.setAdapter(galleryAdapter);
        gv_gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if((sel_count < 7 && !photo_list.get(i).bSel) || photo_list.get(i).bSel){
                    photo_list.get(i).bSel = !photo_list.get(i).bSel;
                    if(photo_list.get(i).bSel){
                        path_list.add(photo_list.get(i).path);
                    } else{
                        for(int j = 0; j < path_list.size(); j++){
                            if(path_list.get(j).equals(photo_list.get(i).path)){
                                path_list.remove(j);
                            }
                        }
                    }
                    galleryAdapter.notifyDataSetChanged();
                    refreshSelectedPhotos();
                }
            }
        });
    }

    private void refreshSelectedPhotos(){
        sel_count = path_list.size();
        for(int k = 0; k < path_list.size(); k++){
            /*Picasso.get()
                    .load(Uri.fromFile(new File(path_list.get(k))))
                    .resize(100, 100)
                    .into(sel_iv_list.get(k));*/
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 6;

            Bitmap myBitmap = BitmapFactory.decodeFile(path_list.get(k),options);
            sel_iv_list.get(k).setImageBitmap(myBitmap);
        }
        for(int j = sel_count; j < 7; j ++){
            sel_iv_list.get(j).setImageResource(R.color.colorGray_D);//android.R.color.transparent
        }

        String count_str = String.format(getString(R.string.selected_format), sel_count, 7);
        tv_sel_count.setText(count_str);
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
        //cursor = getActivity().getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        cursor = parent.getApplicationContext().getApplicationContext().getContentResolver().query(uri, projection, null,null,MediaStore.MediaColumns.DATE_ADDED + " DESC");

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
