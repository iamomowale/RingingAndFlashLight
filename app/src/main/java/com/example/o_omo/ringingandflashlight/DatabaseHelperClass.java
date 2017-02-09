package com.example.o_omo.ringingandflashlight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by o_omo on 1/25/2017.
 */

public class DatabaseHelperClass extends SQLiteOpenHelper {
    public static final String DATABASE_NAME= "blinkLight.db";
    public static final String TABLE_NAME= "blink";
    public static final String COL_1= "ID";
    public static final String COL_2= "CALL_STATE";
    public static final String COL_3= "SMS_STATE";
    public static final String COL_4= "BATTERY_STATE";
    public static final String COL_5= "SEEKBAR_STATE";


    public DatabaseHelperClass(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,CALL_STATE INTEGER,SMS_STATE INTEGER,BATTERY_STATE INTEGER,SEEKBAR_STATE INTEGER )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(int call_state, int sms_state, int battery_state, int seekbar_state ){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,call_state);
        contentValues.put(COL_3,sms_state);
        contentValues.put(COL_4,battery_state);
        contentValues.put(COL_5,seekbar_state);
        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result ==-1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getDbData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM "+TABLE_NAME,null);
        return result;
    }

    public boolean updateData(String indexName, int value){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(indexName,value);
        db.update(TABLE_NAME,contentValues,"ID=1",null);
        return true;
    }

    public boolean checkForTableValue(){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " +TABLE_NAME, null);

        if(cursor != null){

            cursor.moveToFirst();

            int count = cursor.getInt(0);

            if(count > 0){
                return true;
            }

        }
        cursor.close();

        return false;
    }
}
