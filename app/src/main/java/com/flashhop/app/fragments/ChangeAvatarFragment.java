package com.flashhop.app.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.adapters.GalleryAdapter;
import com.flashhop.app.models.Model_images;
import com.flashhop.app.models.PhotoModel;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.ImageFilePath;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static com.flashhop.app.activities.HomeActivity.STORAGE_PERMISSIONS_REQUEST;
import static com.flashhop.app.utils.Const.APP_TAG;

public class ChangeAvatarFragment extends DialogFragment {

    ImageView iv_back;
    GridView gv_gallery;
    GalleryAdapter galleryAdapter;
    Button btn_save;

    ArrayList<Model_images> al_images = new ArrayList<>();
    ArrayList<PhotoModel> photo_list = new ArrayList<>();
    int nSel = -1;
    boolean boolean_folder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rView = inflater.inflate(R.layout.fragment_change_avatar, container, false);
        initLayout(rView);
        return rView;
    }

    private void initLayout(View view){
        iv_back = view.findViewById(R.id.avatar_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        gv_gallery = view.findViewById(R.id.avatar_frag_grid);
        checkPermission();

        galleryAdapter = new GalleryAdapter(getContext(), photo_list);
        gv_gallery.setAdapter(galleryAdapter);
        gv_gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(nSel != -1){
                    photo_list.get(nSel).bSel = false;
                }
                nSel = i;
                photo_list.get(i).bSel = true;
                galleryAdapter.notifyDataSetChanged();
            }
        });

        btn_save = view.findViewById(R.id.avatar_frag_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfile();
            }
        });
    }

    private void checkPermission(){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ) {

                try {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                fn_imagespath();
            }
        } else {
            fn_imagespath();
        }
    }

    private void uploadProfile(){
        final KProgressHUD hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setLabel("Publishing...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        File file = new File(photo_list.get(nSel).path);
        if(file == null) return;
        long fileSizeInMB = file.length()/ (1024*1024);
        if(fileSizeInMB > 2){
            file = ImageFilePath.saveBitmapToFile(file);
            fileSizeInMB = file.length()/1024;
        }
        AndroidNetworking.upload(Const.HOST_URL + Const.UPLOAD_PROFILE_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addMultipartFile("photo_id", file)
                .setTag(APP_TAG)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {

                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hud.dismiss();
                        // do anything with response
                        try{
                            int bSuccess = response.getInt("success");
                            if(bSuccess == 1) {
                                JSONObject data_obj = response.getJSONObject("data");
                                String sVal = data_obj.getString("avatar");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.photo_url = sVal;
                                refreshProfileFrag();
                                dismiss();
                            }
                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hud.dismiss();
                    }
                });
    }

    private void refreshProfileFrag(){
        FragmentManager fragManager = getActivity().getSupportFragmentManager();
        EditProfileFragment editProf = (EditProfileFragment)fragManager.findFragmentByTag(Const.FRAG_PROFILE_TAG);
        if(editProf != null){
            editProf.onResume();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
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
        cursor = getActivity().getApplicationContext().getContentResolver().query(uri, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");

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
