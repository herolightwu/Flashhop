package com.flashhop.app.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flashhop.app.MyApp;
import com.flashhop.app.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class WindowUtils {
    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(act, color));
        }
    }

    public static void setSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = act.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_user_marker, null);

        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(40, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    public static int convertDpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    }

    public static Bitmap createCustomMarker(Context context, String photourl) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_user_marker, null);

        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        //ImageView markerImage = (ImageView) marker.findViewById(R.id.user_dp);
        Picasso.get()
                .load(photourl)
                .resize(60, 60)
                .into(markerImage);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(34, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    public static Bitmap createCreatorMarker(Context context, String photourl) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_creator_marker, null);

        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        //ImageView markerImage = (ImageView) marker.findViewById(R.id.user_dp);
        Picasso.get()
                .load(photourl)
                .resize(60, 60)
                .into(markerImage);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(46, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    public static void showBadge(Context context, BottomNavigationView
            bottomNavigationView, @IdRes int itemId, String value) {
        removeBadge(bottomNavigationView, itemId);
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        View badge = LayoutInflater.from(context).inflate(R.layout.layout_news_badge, bottomNavigationView, false);

        //TextView text = badge.findViewById(R.id.badge_text_view);
        itemView.addView(badge);
    }

    public static void removeBadge(BottomNavigationView bottomNavigationView, @IdRes int itemId) {
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        if (itemView.getChildCount() == 3) {
            itemView.removeViewAt(2);
        }
    }

    public static void setSystemBarColorDialog(Context act, Dialog dialog, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = dialog.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(act, color));
        }
    }

    public static void setSystemBarLightDialog(Dialog dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = dialog.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void showEditErr(Context context, EditText editText, ImageView iv_err, int errStr) {
        editText.getText().clear();
        editText.setTypeface(MyApp.myfont_italic);
        editText.setHintTextColor(ContextCompat.getColor(context, R.color.colorYellow));
        editText.setHint(errStr);
        if(iv_err != null){
            iv_err.setVisibility(View.VISIBLE);
            animateImgView(context, iv_err);
        }
    }

    public static void hideEditErr(EditText editText, ImageView iv_err) {
        editText.setTypeface(MyApp.myfont_normal);
        editText.setError(null);
        if(iv_err != null){
            iv_err.setVisibility(View.GONE);
        }
    }

    public static void animateImgView(Context context, View view){
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.ani_shake_img);
        view.startAnimation(shake);
    }

    public static void animateView(Context context, View view){
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.ani_shake);
        view.startAnimation(shake);
    }

    public static void showDate(TextView tv_date, TextView tv_week, Date date){
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String strDate = dateFormat.format(date);
        String[] ddStr = strDate.split("-");
        String sDate = String.format("%s\n%s", ddStr[1], ddStr[0]);
        tv_date.setText(sDate);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        tv_week.setText(sdf.format(date));
    }

    public static void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public static void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    public static void showImagesLikeInsta(Context context, LinearLayout[] ll_list, ImageView[] iv_list, List<String> photo_list, int width){
        int i = 0;
        ll_list[2].setVisibility(View.GONE);
        ll_list[3].setVisibility(View.GONE);
        ll_list[4].setVisibility(View.GONE);
        ll_list[6].setVisibility(View.GONE);
        iv_list[8].setVisibility(View.GONE);
        iv_list[7].setVisibility(View.GONE);
        ViewGroup.LayoutParams param = ll_list[0].getLayoutParams();
        long filesize =0;
        switch (photo_list.size()){
            case 1:
                param.height = width;
                ll_list[0].setLayoutParams(param);
                if(photo_list.get(0).contains("http://") || photo_list.get(0).contains("https://")){
                    Picasso.get()
                            .load(photo_list.get(0))
                            //.resize(200, 200)
                            //.centerCrop()
                            .into(iv_list[0]);
                } else{
                    File file = new File(photo_list.get(0));
                    filesize = file.length()/1024;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if(filesize > 1024){
                        options.inSampleSize = 3;
                    }
                    Bitmap myBitmap = BitmapFactory.decodeFile(photo_list.get(0),options);
                    iv_list[0].setImageBitmap(myBitmap);
                    /*Picasso.get()
                            .load(Uri.fromFile()))
                            .into(iv_list[0]);*/
                }
                break;
            case 2:
                ll_list[2].setVisibility(View.VISIBLE);
                param.height = (int)(width * 95/200);
                ll_list[0].setLayoutParams(param);

                if(photo_list.get(0).contains("http://") || photo_list.get(0).contains("https://")){
                    Picasso.get()
                            .load(photo_list.get(0))
                            .into(iv_list[0]);
                    Picasso.get()
                            .load(photo_list.get(1))
                            .into(iv_list[1]);
                } else{
                    /*Picasso.get()
                            .load(Uri.fromFile(new File(photo_list.get(0))))
                            .into(iv_list[0]);
                    Picasso.get()
                            .load(Uri.fromFile(new File(photo_list.get(1))))
                            .into(iv_list[1]);*/
                    File file = new File(photo_list.get(0));
                    filesize = file.length()/1024;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if(filesize > 1024){
                        options.inSampleSize = 3;
                    }
                    Bitmap myBitmap = BitmapFactory.decodeFile(photo_list.get(0),options);
                    iv_list[0].setImageBitmap(myBitmap);

                    file = new File(photo_list.get(1));
                    filesize = file.length()/1024;
                    options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if(filesize > 1024){
                        options.inSampleSize = 3;
                    }
                    myBitmap = BitmapFactory.decodeFile(photo_list.get(1),options);
                    iv_list[1].setImageBitmap(myBitmap);
                }
                break;
            case 3:
                ll_list[2].setVisibility(View.VISIBLE);
                ll_list[3].setVisibility(View.VISIBLE);
                param.height = width * 95 / 300;
                ll_list[0].setLayoutParams(param);

                if(photo_list.get(0).contains("http://") || photo_list.get(0).contains("https://"))
                {
                    Picasso.get()
                            .load(photo_list.get(0))
                            .into(iv_list[0]);
                    for(i = 1; i < 3; i++){
                        Picasso.get()
                                .load(photo_list.get(i))
                                .into(iv_list[i]);
                    }
                } else{
                    File file = new File(photo_list.get(0));
                    filesize = file.length()/1024;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if(filesize > 1024){
                        options.inSampleSize = 3;
                    }
                    Bitmap myBitmap = BitmapFactory.decodeFile(photo_list.get(0),options);
                    iv_list[0].setImageBitmap(myBitmap);

                    for(i = 1; i < 3; i++){
                        /*Picasso.get()
                                .load(Uri.fromFile(new File(photo_list.get(i))))
                                .into(iv_list[i]);*/
                        file = new File(photo_list.get(i));
                        filesize = file.length()/1024;
                        options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        if(filesize > 1024){
                            options.inSampleSize = 3;
                        }
                        myBitmap = BitmapFactory.decodeFile(photo_list.get(i),options);
                        iv_list[i].setImageBitmap(myBitmap);
                    }
                }
                break;
            case 4:
                ll_list[4].setVisibility(View.VISIBLE);
                param.height = width*3/4;
                ll_list[0].setLayoutParams(param);
                if(photo_list.get(0).contains("http://") || photo_list.get(0).contains("https://")){
                    Picasso.get()
                            .load(photo_list.get(0))
                            .into(iv_list[0]);
                    for(i = 3; i < 6; i++){
                        Picasso.get()
                                .load(photo_list.get(i-2))
                                .into(iv_list[i]);
                    }
                } else{
                    File file = new File(photo_list.get(0));
                    filesize = file.length()/1024;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if(filesize > 1024){
                        options.inSampleSize = 3;
                    }
                    Bitmap myBitmap = BitmapFactory.decodeFile(photo_list.get(0),options);
                    iv_list[0].setImageBitmap(myBitmap);
                    for(i = 3; i < 6; i++){
                        /*Picasso.get()
                                .load(Uri.fromFile(new File(photo_list.get(i-2))))
                                .into(iv_list[i]);*/
                        file = new File(photo_list.get(i-2));
                        filesize = file.length()/1024;
                        options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        if(filesize > 1024){
                            options.inSampleSize = 3;
                        }
                        myBitmap = BitmapFactory.decodeFile(photo_list.get(i-2),options);
                        iv_list[i].setImageBitmap(myBitmap);
                    }
                }
                break;
            case 5:
                ll_list[4].setVisibility(View.VISIBLE);
                ll_list[6].setVisibility(View.VISIBLE);
                iv_list[6].setVisibility(View.VISIBLE);
                param.height = width;
                ll_list[0].setLayoutParams(param);

                LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2.0f
                );
                LinearLayout.LayoutParams param4 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        3.0f
                );
                ll_list[1].setLayoutParams(param1);
                ll_list[4].setLayoutParams(param4);

                if(photo_list.get(0).contains("http://") || photo_list.get(0).contains("https://")){
                    Picasso.get()
                            .load(photo_list.get(0))
                            .into(iv_list[0]);
                    for(i = 3; i < 7; i++){
                        Picasso.get()
                                .load(photo_list.get(i-2))
                                .into(iv_list[i]);
                    }
                } else{
                    File file = new File(photo_list.get(0));
                    filesize = file.length()/1024;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if(filesize > 1024){
                        options.inSampleSize = 3;
                    }
                    Bitmap myBitmap = BitmapFactory.decodeFile(photo_list.get(0),options);
                    iv_list[0].setImageBitmap(myBitmap);
                    for(i = 3; i < 7; i++){
                        file = new File(photo_list.get(i-2));
                        filesize = file.length()/1024;
                        options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        if(filesize > 1024){
                            options.inSampleSize = 3;
                        }
                        myBitmap = BitmapFactory.decodeFile(photo_list.get(i-2),options);
                        iv_list[i].setImageBitmap(myBitmap);
                    }
                }
                break;
            case 6:
                ll_list[4].setVisibility(View.VISIBLE);
                ll_list[6].setVisibility(View.VISIBLE);
                iv_list[6].setVisibility(View.VISIBLE);
                iv_list[7].setVisibility(View.VISIBLE);
                param.height = width * 3 / 4;
                ll_list[0].setLayoutParams(param);

                if(photo_list.get(0).contains("http://") || photo_list.get(0).contains("https://")){
                    Picasso.get()
                            .load(photo_list.get(0))
                            .into(iv_list[0]);
                    for(i = 3; i < 8; i++){
                        Picasso.get()
                                .load(photo_list.get(i-2))
                                .into(iv_list[i]);
                    }
                } else{
                    File file = new File(photo_list.get(0));
                    filesize = file.length()/1024;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if(filesize > 1024){
                        options.inSampleSize = 3;
                    }
                    Bitmap myBitmap = BitmapFactory.decodeFile(photo_list.get(0),options);
                    iv_list[0].setImageBitmap(myBitmap);
                    for(i = 3; i < 8; i++){
                        file = new File(photo_list.get(i-2));
                        filesize = file.length()/1024;
                        options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        if(filesize > 1024){
                            options.inSampleSize = 3;
                        }
                        myBitmap = BitmapFactory.decodeFile(photo_list.get(i-2),options);
                        iv_list[i].setImageBitmap(myBitmap);
                    }
                }
                break;
            case 7:
                ll_list[4].setVisibility(View.VISIBLE);
                ll_list[6].setVisibility(View.VISIBLE);
                iv_list[8].setVisibility(View.VISIBLE);
                iv_list[7].setVisibility(View.VISIBLE);
                param.height = width * 3 / 4;
                ll_list[0].setLayoutParams(param);

                if(photo_list.get(0).contains("http://") || photo_list.get(0).contains("https://")){
                    Picasso.get()
                            .load(photo_list.get(0))
                            .into(iv_list[0]);
                    for(i = 3; i < 9; i++){
                        Picasso.get()
                                .load(photo_list.get(i-2))
                                .into(iv_list[i]);
                    }
                } else{
                    File file = new File(photo_list.get(0));
                    filesize = file.length()/1024;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    if(filesize > 1024){
                        options.inSampleSize = 3;
                    }
                    Bitmap myBitmap = BitmapFactory.decodeFile(photo_list.get(0),options);
                    iv_list[0].setImageBitmap(myBitmap);
                    for(i = 3; i < 9; i++){
                        file = new File(photo_list.get(i-2));
                        filesize = file.length()/1024;
                        options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        if(filesize > 1024){
                            options.inSampleSize = 3;
                        }
                        myBitmap = BitmapFactory.decodeFile(photo_list.get(i-2),options);
                        iv_list[i].setImageBitmap(myBitmap);
                    }
                }
                break;
        }
    }

    public void vibrate(Context context)
    {
        Vibrator vibrate= (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE) ;
        vibrate.vibrate(50);
    }

    public static void setStatusBarColor(Activity act) {
        Window window = act.getWindow();
        window.setFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );
    }

}
