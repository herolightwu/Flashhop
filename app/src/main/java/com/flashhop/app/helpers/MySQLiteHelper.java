package com.flashhop.app.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.flashhop.app.models.CardModel;
import com.flashhop.app.models.EventModel;
import com.flashhop.app.models.FilterModel;
import com.flashhop.app.models.UserDB;

import java.util.ArrayList;
import java.util.List;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String TAG = "MySQLiteHelper";
    // Database Info
    private static final String DATABASE_NAME = "Flashhop";
    private static final int DATABASE_VERSION = 2;

    private static MySQLiteHelper sInstance;
    private static Context ctx;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_SETTINGS = "settings";
    private static final String TABLE_FILTER = "filters";
    private static final String TABLE_CARDINFO = "cards_info";
    //settings table rows
    public static final String KEY_SETTINGS_AGE = "hide_age"; //text
    public static final String KEY_SETTINGS_LOCATION = "hide_loc"; //text
    public static final String KEY_SETTINGS_PAUSE = "pause_all";//text
    public static final String KEY_SETTINGS_MY = "my_activities";//text
    public static final String KEY_SETTINGS_FRIENDS = "friends_activities";//text
    public static final String KEY_SETTINGS_CHATS = "chats";//text

    public static MySQLiteHelper getInstance(Context context) {
        ctx = context;
        if (sInstance == null) {
            sInstance = new MySQLiteHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        //db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SETTINGS_TABLE = "CREATE TABLE settings (name TEXT PRIMARY KEY NOT NULL, category TEXT, value TEXT, UNIQUE (name, category));";
        String INSERT_INITIAL_SET_0 = "INSERT INTO settings (name, category, value) VALUES ('hide_age', 'privacy', '0');";
        String INSERT_INITIAL_SET_1 = "INSERT INTO settings (name, category, value) VALUES ('hide_loc', 'privacy', '0');";
        String INSERT_INITIAL_SET_2 = "INSERT INTO settings (name, category, value) VALUES ('pause_all', 'notifications', '0');";
        String INSERT_INITIAL_SET_3 = "INSERT INTO settings (name, category, value) VALUES ('my_activities', 'notifications', '0');";
        String INSERT_INITIAL_SET_4 = "INSERT INTO settings (name, category, value) VALUES ('friends_activities', 'notifications', '1');";
        String INSERT_INITIAL_SET_5 = "INSERT INTO settings (name, category, value) VALUES ('chats', 'notifications', '1');";

        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " (" +
                " key_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, dd TEXT, dt TEXT, addr TEXT, location TEXT, people TEXT, age TEXT, " +
                "cate TEXT, photo TEXT, description TEXT, gender INTEGER, followable INTEGER, invitation INTEGER, state INTEGER, eid TEXT, price TEXT DEFAULT '0', currency_code TEXT DEFAULT 'CAD', later INTEGER );";

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                " uid TEXT PRIMARY KEY NOT NULL, firstname TEXT, lastname TEXT, avatar TEXT, timestamp INTEGER, location TEXT);";
        String CREATE_FILTERS_TABLE = "CREATE TABLE " + TABLE_FILTER + " (" +
                " fid TEXT PRIMARY KEY NOT NULL, fdate TEXT, fcate TEXT, gender TEXT, min_age INTEGER DEFAULT 18, max_age INTEGER DEFAULT 60, foption TEXT, period TEXT DEFAULT '7');";
        String CREATE_CARDS_TABLE = "CREATE TABLE " + TABLE_CARDINFO + " (" +
                " card_num TEXT PRIMARY KEY NOT NULL, holder TEXT, expire TEXT, cvv TEXT, street_no TEXT, street_name TEXT, city TEXT, province TEXT, post_code TEXT);";


        try{
            db.execSQL(CREATE_SETTINGS_TABLE);
            db.execSQL(INSERT_INITIAL_SET_0);
            db.execSQL(INSERT_INITIAL_SET_1);
            db.execSQL(INSERT_INITIAL_SET_2);
            db.execSQL(INSERT_INITIAL_SET_3);
            db.execSQL(INSERT_INITIAL_SET_4);
            db.execSQL(INSERT_INITIAL_SET_5);
            db.execSQL(CREATE_EVENTS_TABLE);
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_FILTERS_TABLE);
            db.execSQL(CREATE_CARDS_TABLE);
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILTER + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDINFO + ";");
            onCreate(db);
        }
    }

    public CardModel getCardInfo(){
        String SETTING_SELECT_QUERY = "SELECT * FROM cards_info ;";

        CardModel fone = new CardModel();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SETTING_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                fone.card_num = cursor.getString(cursor.getColumnIndex("card_num"));
                fone.holder_name = cursor.getString(cursor.getColumnIndex("holder"));
                fone.expire = cursor.getString(cursor.getColumnIndex("expire"));
                fone.cvv = cursor.getString(cursor.getColumnIndex("cvv"));
                fone.street_no = cursor.getString(cursor.getColumnIndex("street_no"));
                fone.street_name = cursor.getString(cursor.getColumnIndex("street_name"));
                fone.city = cursor.getString(cursor.getColumnIndex("city"));
                fone.province = cursor.getString(cursor.getColumnIndex("province"));
                fone.post_code = cursor.getString(cursor.getColumnIndex("post_code"));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Quotes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return fone;
    }

    public boolean putCardInfo(CardModel card){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("card_num", card.card_num);
        contentValues.put("holder", card.holder_name);
        contentValues.put("expire", card.expire);
        contentValues.put("cvv", card.cvv);
        contentValues.put("street_no", card.street_no);
        contentValues.put("street_name", card.street_name);
        contentValues.put("city", card.city);
        contentValues.put("province", card.province);
        contentValues.put("post_code", card.post_code);
        long result =db.replace(TABLE_CARDINFO,null,contentValues);

        if (result== -1){
            return false;
        } else {
            return true;
        }
    }

    public FilterModel getFilter(){
        String SETTING_SELECT_QUERY = "SELECT * FROM filters ;";

        FilterModel fone = new FilterModel();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SETTING_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                fone.event_date = cursor.getString(cursor.getColumnIndex("fdate"));
                fone.event_category = cursor.getString(cursor.getColumnIndex("fcate"));
                fone.min_age = cursor.getInt(cursor.getColumnIndex("min_age"));
                fone.max_age = cursor.getInt(cursor.getColumnIndex("max_age"));
                fone.gender = cursor.getString(cursor.getColumnIndex("gender"));
                fone.filter_option = cursor.getString(cursor.getColumnIndex("foption"));
                fone.period = cursor.getString(cursor.getColumnIndex("period"));
                fone.fid = cursor.getString(cursor.getColumnIndex("fid"));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Quotes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return fone;
    }

    public boolean putFilter(FilterModel fone){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("fid", fone.fid);
        contentValues.put("fdate", fone.event_date);
        contentValues.put("fcate", fone.event_category);
        contentValues.put("gender", fone.gender);
        contentValues.put("min_age", fone.min_age);
        contentValues.put("max_age", fone.max_age);
        contentValues.put("foption", fone.filter_option);
        contentValues.put("period", fone.period);
        long result =db.replace(TABLE_FILTER,null,contentValues);

        if (result== -1){
            return false;
        } else {
            return true;
        }
    }

    public boolean putUser(UserDB udb){
        udb.timestamp = System.currentTimeMillis()/1000;
        UserDB other = getUser(udb.uid);
        if(other.loc.equals(udb.loc)) {
            if ((udb.timestamp - other.timestamp) > 3600){
                return false;
            } else{
                return true;
            }
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uid", udb.uid);
        contentValues.put("firstname", udb.firstname);
        contentValues.put("lastname", udb.lastname);
        contentValues.put("avatar", udb.avatar);
        contentValues.put("location", udb.loc);
        contentValues.put("timestamp", udb.timestamp);
        long result =db.replace(TABLE_USERS,null,contentValues);

        return true;
    }

    public UserDB getUser(String uid){
        String SETTING_SELECT_QUERY = String.format("SELECT * FROM users WHERE uid = '%s';", uid);

        UserDB udb = new UserDB();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SETTING_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                udb.uid = cursor.getString(cursor.getColumnIndex("uid"));
                udb.firstname = cursor.getString(cursor.getColumnIndex("firstname"));
                udb.lastname = cursor.getString(cursor.getColumnIndex("lastname"));
                udb.avatar = cursor.getString(cursor.getColumnIndex("avatar"));
                udb.loc = cursor.getString(cursor.getColumnIndex("location"));
                udb.timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Quotes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return udb;
    }

    public long putEvent(EventModel ev){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", ev.title);
        contentValues.put("dd", ev.date);
        contentValues.put("dt", ev.time);
        contentValues.put("addr", ev.address);
        contentValues.put("location", ev.loc);
        contentValues.put("people", ev.people);
        contentValues.put("age", ev.age);
        contentValues.put("cate", ev.category);
        contentValues.put("photo", ev.photo);
        contentValues.put("description", ev.desc);
        contentValues.put("gender", ev.gender);
        contentValues.put("followable", ev.followable);
        contentValues.put("invitation", ev.invitaion);
        contentValues.put("state", ev.state);
        contentValues.put("price", ev.price);
        contentValues.put("currency_code", ev.currency);
        contentValues.put("later", ev.is_pay_later);
        if(ev.id != null)
            contentValues.put("eid", ev.id);
        if(ev.db_id != -1){
            contentValues.put("key_id", ev.db_id);
        }
        long result =db.replace(TABLE_EVENTS,null,contentValues);

        if (result== -1){
            return -1;
        } else {
            return result;
        }
    }

    public boolean deleteEvent(EventModel ev){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EVENTS, "key_id = " + ev.db_id, null) > 0;
    }

    public List<EventModel> getEvents(int status){
        String EVENT_GET_QUERY = String.format("SELECT * FROM events WHERE state = %d;", status);

        List<EventModel> event_list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EVENT_GET_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do{
                    EventModel one = new EventModel();
                    one.title = cursor.getString(cursor.getColumnIndex("title"));
                    one.date = cursor.getString(cursor.getColumnIndex("dd"));
                    one.time = cursor.getString(cursor.getColumnIndex("dt"));
                    one.address = cursor.getString(cursor.getColumnIndex("addr"));
                    one.loc = cursor.getString(cursor.getColumnIndex("location"));
                    one.people = cursor.getString(cursor.getColumnIndex("people"));
                    one.age = cursor.getString(cursor.getColumnIndex("age"));
                    one.category = cursor.getString(cursor.getColumnIndex("cate"));
                    one.photo = cursor.getString(cursor.getColumnIndex("photo"));
                    one.desc = cursor.getString(cursor.getColumnIndex("description"));
                    one.gender = cursor.getInt(cursor.getColumnIndex("gender"));
                    one.followable = cursor.getInt(cursor.getColumnIndex("followable"));
                    one.invitaion = cursor.getInt(cursor.getColumnIndex("invitation"));
                    one.state = cursor.getInt(cursor.getColumnIndex("state"));
                    one.db_id = cursor.getInt(cursor.getColumnIndex("key_id"));
                    one.id = cursor.getString(cursor.getColumnIndex("eid"));
                    one.price = cursor.getString(cursor.getColumnIndex("price"));
                    one.currency = cursor.getString(cursor.getColumnIndex("currency_code"));
                    one.is_pay_later = cursor.getInt(cursor.getColumnIndex("later"));
                    event_list.add(one);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Quotes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return event_list;
    }

    public String getSettingValue(String name){

        String SETTING_SELECT_QUERY = String.format("SELECT * FROM settings WHERE name = '%s';", name);

        String value="";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SETTING_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                value = cursor.getString(cursor.getColumnIndex("value"));
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Quotes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return value;
    }

    public void updateSettingValue(String name, String value){
        SQLiteDatabase db = getWritableDatabase();

        String SETTING_UPDATE_SQL = String.format("UPDATE "+ TABLE_SETTINGS + " SET value = '%s' WHERE name = '%s';" , value, name);

        db.execSQL(SETTING_UPDATE_SQL);
    }

}
