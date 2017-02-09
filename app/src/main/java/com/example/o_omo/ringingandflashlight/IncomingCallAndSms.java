package com.example.o_omo.ringingandflashlight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Camera;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by o_omo on 1/25/2017.
 */
public class IncomingCallAndSms extends BroadcastReceiver {
    Context pContext;
    Camera camera;
    Camera.Parameters parameter;
    int batteryLevel=99;
    int call, message, battery, seekbarValue;
    Intent newIntent = null;

    //get the object of smsManager
    final SmsManager sms = SmsManager.getDefault();
    @Override
    public void onReceive(Context context, Intent intent) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batt = context.getApplicationContext().registerReceiver(null,filter);
        if(batt!=null){
            batteryLevel = batt.getIntExtra("level",0);
        }
        //getting switch state and seek bar state from database
        database myDb = new database(context);
        Cursor cursor = myDb.getDatabaseData();
        cursor.moveToFirst();
        call = Integer.parseInt(cursor.getString(1));
        message = Integer.parseInt(cursor.getString(2));
        battery = Integer.parseInt(cursor.getString(3));
        seekbarValue = Integer.parseInt(cursor.getString(4));
        cursor.close();

        //for receiving sms
        final Bundle bundle = intent.getExtras();
        try{
            if (bundle!=null){
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i=0; i<pdusObj.length; i++){
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[])pdusObj[i]);
                    String senderNumber = currentMessage.getDisplayOriginatingAddress();
//                    String message = currentMessage.getDisplayMessageBody();
        // To blink when receiving sms
                    //IF LOW_BATTERY SWITCH IS ON AND SMS BLINK BUTTON IS ON
                    if (battery ==1 && message==1){
                        //If batter is not less than 20%
                        if(batteryLevel>=20){
                            blinkLight(seekbarValue, camera, parameter, intent);
                        }
                    }
                    //IF LOW_BATTERY SWITCH IS OFF AND SMS BLINK BUTTON IS ON
                    if (battery ==0 && message==1){
                        blinkLight(seekbarValue, camera, parameter, intent);
                    }
                }
            }
        }catch (Exception e){
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }

        //for receiving calls
        try{
            //Creating Listener to receive phone state
            TelephonyManager tmgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //referenceing the context and intent
            pContext = context;
            newIntent = intent;
            //Listener (Passing pContext into it)
            MyPhoneStateListener PhoneListener = new MyPhoneStateListener(pContext, newIntent);

            //Register listener for Listen to call state
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }catch (Exception e){
            Log.e("Phone Receive Error", ""+e);
        }

    }

    //Create Phone State Listener Class
    private class MyPhoneStateListener extends PhoneStateListener{
        public MyPhoneStateListener(Context pContext, Intent newIntent){

        }
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d("MyPhoneListener", state+ " Incoming No: "+incomingNumber);
 
            // To blink when receiving call
            //State 1 means when phone is ringing
            if (state==1){
                //IF LOW_BATTERY SWITCH IS ON AND SMS BLINK BUTTON IS ON
                if (battery ==1 && call==1){
                    //If batter is not less than 20%
                    if(batteryLevel>=20){
                        blinkLight(seekbarValue, camera, parameter,newIntent);
                    }
                }
                //IF LOW_BATTERY SWITCH IS OFF AND SMS BLINK BUTTON IS ON
                if (battery ==0 && call==1){
                    blinkLight(seekbarValue, camera, parameter, newIntent);
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    public void blinkLight(int delay, Camera camera, Camera.Parameters parameters, Intent intent){
        String numOfBlink = "0101010101010101";
        camera = camera.open();
        parameters = camera.getParameters();

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        for (int i=0; i<numOfBlink.length(); i++){
            if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) || state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                //Call received or rejected
                break;
            }
            if (numOfBlink.charAt(i)=='0'){
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
            }else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.stopPreview();
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        camera.release();
    }

    public class database extends SQLiteOpenHelper{
        public static final String DATABASE_NAME= "blinkLight.db";
        public static final String TABLE_NAME= "blink";

        public database(Context context) {
            super(context, DATABASE_NAME, null, 1);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        public Cursor getDatabaseData(){
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor result = db.rawQuery("SELECT * FROM "+TABLE_NAME,null);
            return result;
        }
    }
}
