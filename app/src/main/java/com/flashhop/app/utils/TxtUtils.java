package com.flashhop.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TxtUtils {
    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";//final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isUsernameValid(String name) {
        String expression = "^[a-zA-Z0-9]*$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static boolean isValidExpireDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/YYYY");
        dateFormat.setLenient(false);
        try {
            String[] edate = inDate.split("/");
            String ccDate = edate[0] + "/20" + edate[1];
            dateFormat.parse(ccDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static long getTimestamp(String date, String time){
        long diff = 0;//unit min
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mm a");
        try{
            Date dt = df.parse(date + " " + time);
            long mills = dt.getTime();
            diff = mills/1000;
        } catch (ParseException ex){
            ex.printStackTrace();
        }
        return diff;
    }

    public static long getDifferenceTime(String date, String time){
        long diff = 0;//unit min
        String[] sTime = time.split("-");
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mm a");
        try{
            Date dt = df.parse(date + " " + sTime[0]);
            Date cur_dt = new Date();
            long mills = dt.getTime() - cur_dt.getTime();
            diff = mills/(1000*60);
        } catch (ParseException ex){
            ex.printStackTrace();
        }
        return diff;
    }

    public static long getDifferenceChatTime(String date, String time){
        long diff = 0;//unit min
        String[] sTime = time.split("-");
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mm a");
        try{
            Date dt = df.parse(date + " " + sTime[0]);
            Date cur_dt = new Date();
            long mills = (7 * 24 * 3600 * 1000) + dt.getTime() - cur_dt.getTime();
            diff = mills/(1000*60);
        } catch (ParseException ex){
            ex.printStackTrace();
        }
        return diff;
    }

    public static long getDifferenceEndTime(String date, String time){
        long diff = 0;//unit min
        String[] sTime = time.split("-");
        SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy hh:mm a");
        try{
            Date dt = df.parse(date + " " + sTime[1]);
            Date cur_dt = new Date();
            long mills = dt.getTime() - cur_dt.getTime();
            diff = mills/(1000*60);
        } catch (ParseException ex){
            ex.printStackTrace();
        }
        return diff;
    }

    public static long getDifferenceTime1(String date){
        long diff = 0;//unit min
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            Date dt = df.parse(date);
            Date cur_dt = new Date();
            long mills = cur_dt.getTime() - dt.getTime();
            diff = mills/(1000*60);
        } catch (ParseException ex){
            ex.printStackTrace();
        }
        return diff;
    }

    public static long getDifferenceTime(String date){
        long diff = 100;//unit min
        if(date.length() > 0){
            long dt = Long.parseLong(date);
            long mills = System.currentTimeMillis()/1000 - dt;
            diff = mills/60;
        }
        return diff;
    }

    public static int getAge(String date) {

        int age = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");
        try {
            Date date1 = df.parse(date);
            Calendar now = Calendar.getInstance();
            Calendar dob = Calendar.getInstance();
            dob.setTime(date1);
            if (dob.after(now)) {
                throw new IllegalArgumentException("Can't be born in the future");
            }
            int year1 = now.get(Calendar.YEAR);
            int year2 = dob.get(Calendar.YEAR);
            age = year1 - year2;
            int month1 = now.get(Calendar.MONTH);
            int month2 = dob.get(Calendar.MONTH);
            if (month2 > month1) {
                age--;
            } else if (month1 == month2) {
                int day1 = now.get(Calendar.DAY_OF_MONTH);
                int day2 = dob.get(Calendar.DAY_OF_MONTH);
                if (day2 > day1) {
                    age--;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return age ;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /*public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static boolean isMobileValid(String phone) {
        boolean check = false;
        if(!Pattern.matches("[a-zA-Z]+", phone)) {
            if(phone.length() < 6 || phone.length() > 15) {
                // if(phone.length() != 10) {
                check = false;
            } else {
                check = true;
            }
        } else {
            check=false;
        }
        return check;
        //android.util.Patterns.PHONE.matcher(username).matches()
    }
    public static boolean isValidMobile(String phone) {
        if(phone.length() < 6 || phone.length() > 13)
            return false;
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }*/

    public static String getDurationFromCurrent(int mtime){
        String str_dur = "";
        int curtime = (int)(System.currentTimeMillis()/1000);
        int dur = curtime - mtime;
        if((dur/60 > 1) && (dur/60 < 60)){
            str_dur = dur/60 + "min";
        } else if(dur/3600 >= 1){
            str_dur = dur/3600 + "hrs";
        }
        return str_dur;
    }


}
