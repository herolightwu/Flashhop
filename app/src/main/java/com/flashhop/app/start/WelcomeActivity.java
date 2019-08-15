package com.flashhop.app.start;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.activities.HomeActivity;
import com.flashhop.app.fragments.AllowFragment;
import com.flashhop.app.fragments.BirthdayFragment;
import com.flashhop.app.fragments.GenderFragment;
import com.flashhop.app.fragments.InterestsFragment;
import com.flashhop.app.fragments.LanguageFragment;
import com.flashhop.app.fragments.PhotosFragment;
import com.flashhop.app.helpers.SaveSharedPrefrence;
import com.flashhop.app.models.Model_images;
import com.flashhop.app.utils.Const;
import com.flashhop.app.utils.WindowUtils;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.flashhop.app.activities.HomeActivity.STORAGE_PERMISSIONS_REQUEST;
import static com.flashhop.app.utils.Const.APP_TAG;

public class WelcomeActivity extends AppCompatActivity {

    public static final int LOCATION_PERMISSIONS_REQUEST = 6789;

    Button btn_next;
    public int nStep = 0;
    public boolean bLang[] = new boolean[10];
    public boolean bInterest[] = new boolean[9];
    public boolean bMale = true;
    public boolean bLoc_Enable = false;
    public boolean bNoti_Enable = false;
    public Bitmap bm_photo = null;
    public String birthday = "0000-00-00";
    String lang_list[] = new String[]{"en", "fr", "cn", "es", "ja", "ko", "ar", "ru", "de", "po"};
    String interest_list[] = new String[]{"party", "eating", "dating", "sports", "outdoors", "games", "study", "spiritual", "arts"};
    public ArrayList<Model_images> al_images = new ArrayList<>();
    public ArrayList<String> photo_list = new ArrayList<>();
    boolean boolean_folder;

    SaveSharedPrefrence saveSharedPrefrence = new SaveSharedPrefrence();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtils.setSystemBarColor(this, android.R.color.transparent);
        WindowUtils.setSystemBarLight(this);
        setContentView(R.layout.activity_welcome);

        initLayout();
    }

    private void initLayout(){
        btn_next = findViewById(R.id.welcome_btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                process_next();
            }
        });

        bLang[0] = true;
    }

    public void process_next(){
        if(nStep == 5){
            regUserProfile();
            return;
        }
        nStep++;
        if(nStep == 1){
            showChooseGender();
        } else if(nStep == 2){
            showChooseBirth();
        } else if(nStep == 3){
            showChooseLanguages();
        } else if(nStep == 4){
            showChooseInterest();
        } else if(nStep == 5){
            choosePhotos();
        } else if(nStep == 7){
            showEnableLocation();
        } else if(nStep == 8){
            showAllowNotification();
        } else if(nStep > 8){
            saveSharedPrefrence.saveKeyLogin(getApplicationContext(), true);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    public void refreshPhotoFrag(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("UPLOAD_PHOTO_FRAG");
        if(fragment instanceof PhotosFragment){
            ((PhotosFragment)fragment).refreshLayout();
        }
    }

    public void showPhotoFrag(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        PhotosFragment newFragment = new PhotosFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_enter_from_right, R.anim.ani_exit_to_left);
        transaction.add(android.R.id.content, newFragment, "UPLOAD_PHOTO_FRAG").addToBackStack("UPLOAD_PHOTO_FRAG").commit();
    }

    public void choosePhotos(){

        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ) {

                try {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                fn_imagespath();
                showPhotoFrag();
            }
        } else {
            fn_imagespath();
            showPhotoFrag();
        }

    }

    private void regUserProfile(){
        String sGender = "male";
        if(!bMale){
            sGender = "female";
        }
        String sDob = birthday.replace(" ", "");
        String sLang = "";
        for(int i = 0; i < 10; i++){
            if(bLang[i]){
                if(sLang.length() == 0){
                    sLang = lang_list[i];
                } else{
                    sLang = sLang + "," + lang_list[i];
                }
            }
        }

        String sInterest = "";
        for(int j = 0; j < 9; j++){
            if(bInterest[j]){
                if(sInterest.length() == 0){
                    sInterest = interest_list[j];
                } else{
                    sInterest = sInterest + "," + interest_list[j];
                }
            }
        }

        if(bm_photo == null){
            return;
        }
        File bm_file = savebitmap("flashhop_avatar");
        if(bm_file == null) return;
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Registering profile...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        ANRequest.MultiPartBuilder multiPartBuilder = AndroidNetworking.upload(Const.HOST_URL + Const.REG_PROFILE_URL)
                .addHeaders("Authorization", "Bearer " + MyApp.curUser.token)
                .addMultipartParameter("lang", sLang)
                .addMultipartParameter("dob", sDob)
                .addMultipartParameter("gender", sGender)
                .addMultipartParameter("interests", sInterest);

        multiPartBuilder.addMultipartFile("photo_id", bm_file)
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
                                MyApp.curUser.first_name = data_obj.getString("first_name");
                                MyApp.curUser.last_name = data_obj.getString("last_name");
                                MyApp.curUser.push_id = data_obj.getString("push_user_id");
                                MyApp.curUser.uid = data_obj.getString("id");
                                int nVerify = data_obj.getInt("email_verified");
                                if(nVerify == 0){
                                    MyApp.curUser.bEmailVerify = false;
                                } else{
                                    MyApp.curUser.bEmailVerify = true;
                                }
                                MyApp.curUser.created_at = data_obj.getString("created_at");
                                MyApp.curUser.updated_at = data_obj.getString("updated_at");
                                MyApp.curUser.dob = data_obj.getString("dob");
                                MyApp.curUser.lang = data_obj.getString("lang");
                                MyApp.curUser.interests = data_obj.getString("interests");
                                MyApp.curUser.gender = data_obj.getString("gender");
                                String sVal = data_obj.getString("social_id");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.social_id = sVal;
                                sVal = data_obj.getString("social_name");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.social_name = sVal;
                                sVal = data_obj.getString("avatar");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.photo_url = sVal;

                                JSONArray img_array = data_obj.getJSONArray("images");
                                MyApp.curUser.images = new ArrayList<>();
                                for(int i = 0; i < img_array.length(); i++){
                                    String sImg = img_array.getString(i);
                                    MyApp.curUser.images.add(sImg);
                                }

                                JSONArray tag_array = data_obj.getJSONArray("tag_list");
                                MyApp.curUser.tags = new ArrayList<>();
                                for(int i = 0; i < tag_array.length(); i++){
                                    String sTag = tag_array.getString(i);
                                    MyApp.curUser.tags.add(sTag);
                                }
                                sVal = data_obj.getString("personality_type");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.person_type = sVal;
                                sVal = data_obj.getString("fun_facts");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.facts = sVal;

                                MyApp.curUser.push_chats = data_obj.getInt("push_chats");
                                MyApp.curUser.push_friends_activities = data_obj.getInt("push_friends_activities");
                                MyApp.curUser.push_my_activities = data_obj.getInt("push_my_activities");
                                MyApp.curUser.event_count = data_obj.getInt("event_count");
                                MyApp.curUser.hide_my_location = data_obj.getInt("hide_my_location");
                                MyApp.curUser.hide_my_age = data_obj.getInt("hide_my_age");
                                MyApp.curUser.is_active_by_customer = data_obj.getInt("is_active_by_customer");
                                sVal = data_obj.getString("last_dob_updated_at");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.dob_update = sVal;
                                sVal = data_obj.getString("last_gender_updated_at");
                                if(sVal.equals("null")) sVal = "";
                                MyApp.curUser.gender_update = sVal;
                                MyApp.curUser.bDobEnable = data_obj.getInt("update_dob_enable");
                                MyApp.curUser.bGenderEnable = data_obj.getInt("update_gender_enable");

                                nStep++;
                                showEnableLocation();
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
        //nStep++;
        //showEnableLocation();
    }

    private void showAllowNotification(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        AllowFragment newFragment = new AllowFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    private void showEnableLocation(){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED ) {

                try {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSIONS_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_LOCATION, "true");
                bLoc_Enable = true;
                process_next();
            }
        } else {
            saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_LOCATION, "true");
            bLoc_Enable = true;
            process_next();
        }
    }

    private void showChooseLanguages() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        LanguageFragment newFragment = new LanguageFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.setCustomAnimations(R.anim.ani_enter_from_right, R.anim.ani_exit_to_left);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    private void showChooseGender() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        GenderFragment newFragment = new GenderFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_enter_from_right, R.anim.ani_exit_to_left);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    private void showChooseBirth() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        BirthdayFragment newFragment = new BirthdayFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_enter_from_right, R.anim.ani_exit_to_left);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
    }

    private void showChooseInterest() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        InterestsFragment newFragment = new InterestsFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.ani_enter_from_right, R.anim.ani_exit_to_left);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
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
        cursor = getContentResolver().query(uri, projection, null, null, MediaStore.MediaColumns.DATE_ADDED + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
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
                String path = al_images.get(i).al_imagepath.get(j);
                photo_list.add(path);
            }
        }
        return al_images;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_LOCATION, "true");
                    bLoc_Enable = true;
                    process_next();
                } else {
                    saveSharedPrefrence.putString(getApplicationContext(), SaveSharedPrefrence.KEY_LOCATION, "false");
                    bLoc_Enable = false;
                }
                break;
            case STORAGE_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fn_imagespath();
                    showPhotoFrag();
                } else {
                    //Toast.makeText(this, R.string.err_permission, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private File savebitmap(String filename) {

        File folder = new File(Const.PHOTO_DIR + "/");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if(!success){
            return null;
        }

        OutputStream outStream = null;

        File file = new File(Const.PHOTO_DIR,filename + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(Const.PHOTO_DIR, filename + ".png");
        }
        try {
            // make a new bitmap from your file
            outStream = new BufferedOutputStream(new FileOutputStream(file));//new FileOutputStream(file);
            bm_photo.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;

    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

}
