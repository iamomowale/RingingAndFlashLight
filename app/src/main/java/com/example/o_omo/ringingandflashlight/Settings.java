package com.example.o_omo.ringingandflashlight;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class Settings extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Object of db Helper class
    DatabaseHelperClass myDb;
    Camera camera;
    Camera.Parameters parameter;
    boolean isFlash=false;
    boolean isHidden = true;

    int seekBarValue=50, callSwitchVal =0, smsSwitchVal =0, batterySwitchVal =0;
    SeekBar seekbar;
    Switch callSwitch;
    Switch smsSwitch;
    Switch batterySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myDb = new DatabaseHelperClass(this);
        if (!myDb.checkForTableValue()){
            myDb.insertData(callSwitchVal,smsSwitchVal,batterySwitchVal,seekBarValue);
        }

        //Casting
        callSwitch = (Switch) findViewById(R.id.switchCall);
        smsSwitch = (Switch) findViewById(R.id.switchSMS);
        batterySwitch = (Switch) findViewById(R.id.switchBattery);

        //Initializing Switch and Seekbar value from Database
        setSwitch(callSwitch,1);
        setSwitch(smsSwitch,2);
        setSwitch(batterySwitch,3);

        switchValues(callSwitch,"CALL_STATE",callSwitchVal);
        switchValues(smsSwitch,"SMS_STATE",smsSwitchVal);
        switchValues(batterySwitch,"BATTERY_STATE",batterySwitchVal);

        //Setting up Seekbar
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        setSeekBar(seekbar);
        seekbar.incrementProgressBy(50);
        seekbar.setMax(500);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue = progress;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myDb.updateData("SEEKBAR_STATE",seekBarValue);

                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                    isFlash =true;
                }
                if(isFlash){
                    //For Testing blinking frequency using seekbar
                    blinkLights(seekBarValue, camera, parameter);
                }

            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Ringing And Flash Light Version 1.0", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void blinkLights(int delay, Camera camera, Camera.Parameters parameters){
        String numOfBlink = "0101010101010101";
        camera = camera.open();
        parameters = camera.getParameters();

        for (int i=0; i<numOfBlink.length(); i++){
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if(camera!=null){
                camera.release();
                camera=null;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navItem_exitSettings) {
            drawer.closeDrawer(GravityCompat.START);
            if(camera!=null){
                camera.release();
                camera=null;
            }
            finishAffinity();

        } else if (id == R.id.navItem_aboutSettings) {
            NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
            Menu nav_menu = navView.getMenu();
            if (isHidden){
                nav_menu.findItem(R.id.showHide).setVisible(true);
                isHidden =false;
            }else{
                nav_menu.findItem(R.id.showHide).setVisible(false);
                isHidden =true;
            }

        } else if (id == R.id.navItem_homeSettings) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
            Intent intent = new Intent(Settings.this, HomeActivity.class);
            startActivity(intent);

        }

        return true;
    }

    public void switchValues(Switch switc, final String state, final int switcValue){
        switc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            int value = switcValue;
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    value = 1;
                }else{
                    value = 0;
                }
                myDb.updateData(state,value);
            }
        });
    }

    public void setSwitch(Switch sw, int index){
        Cursor cursor = myDb.getDbData();
        cursor.moveToFirst();
        if(Integer.parseInt(cursor.getString(index)) ==0){
            sw.setChecked(false);
        }else{
            sw.setChecked(true);
        }
        cursor.close();
    }
    public  void setSeekBar(SeekBar seek){
        Cursor cursor = myDb.getDbData();
        cursor.moveToFirst();
        seek.setProgress(Integer.parseInt(cursor.getString(4)));
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        if(camera!=null){
            camera.release();
            camera=null;
        }
        super.onDestroy();
    }
}
