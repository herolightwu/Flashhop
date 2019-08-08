package com.flashhop.app.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.flashhop.app.utils.Const;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.drakeet.support.toast.ToastCompat;

public class PreviewPostedFrag extends DialogFragment {
    static Context context;

    private View root_view;
    ImageView iv_back, iv_image, iv_share, iv_down;
    TextView tv_title;
    int pre_HomeType = Const.HOME_HOME;
    public String image_path, user_name;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_preview_posted, container, false);

        context = getContext();
        pre_HomeType = MyApp.home_type;
        MyApp.home_type = Const.HOME_PREVIEW;

        iv_back = root_view.findViewById(R.id.preview_posted_frag_iv_back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.home_type = pre_HomeType;
                dismiss();
            }
        });
        iv_image = root_view.findViewById(R.id.preview_posted_frag_iv_image);
        iv_share = root_view.findViewById(R.id.preview_posted_frag_iv_share);
        iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage();
            }
        });
        iv_down = root_view.findViewById(R.id.preview_posted_frag_iv_down);
        iv_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = user_name + System.currentTimeMillis()/1000 + ".png";//image_path.substring(image_path.lastIndexOf('/') + 1);
                imageDownload(fileName);
            }
        });
        tv_title = root_view.findViewById(R.id.preview_posted_frag_tv_title);
        String stitle = String.format("Posted by %s", user_name);
        tv_title.setText(stitle);
        Picasso.get()
                .load(image_path)
                .into(iv_image);
        return root_view;
    }

    private void refreshLayout(){

    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void imageDownload(String url){
        /*Picasso.get()
                .load(image_path)
                .into(getTarget(url));*/
        final KProgressHUD hud = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLabel("Downloading...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        Bitmap bitmap = ((BitmapDrawable)iv_image.getDrawable()).getBitmap();
        File file = new File(Const.PHOTO_DIR + "/" + url);
        try {
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.flush();
            ostream.close();
            showToast();
        } catch (IOException e) {
            showFailToast();
        }
        hud.dismiss();
    }

    private void showToast(){
        ToastCompat.makeText(context.getApplicationContext(), "Download successfully", Toast.LENGTH_SHORT).show();
    }
    private void showFailToast(){
        ToastCompat.makeText(context.getApplicationContext(), "Download failed", Toast.LENGTH_SHORT).show();
    }


    //target to save
    /*private static Target getTarget(final String url){

        Target target = new Target(){
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        File file = new File(Const.PHOTO_DIR + "/" + url);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                            ostream.flush();
                            ostream.close();
                            //showToast();
                        } catch (IOException e) {
                            //showFailToast();
                        }
                       // dismissProgress();
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable){
                //dismissProgress();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }*/

    private void shareImage(){
        Bitmap bitmap= ((BitmapDrawable)iv_image.getDrawable()).getBitmap();
        File folder = new File(Const.PHOTO_DIR + "/");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if(!success){
            return;
        }
        OutputStream out = null;
        File file = new File(Const.PHOTO_DIR,"Share.png");
        if (file.exists()) {
            file.delete();
            file = new File(Const.PHOTO_DIR, "Share.png");
        }
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Uri bmpUri = Uri.fromFile(file);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        startActivity(Intent.createChooser(sharingIntent, "Share Image"));
    }
}
