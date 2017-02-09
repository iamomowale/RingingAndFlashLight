package com.example.o_omo.ringingandflashlight;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.media.MediaBrowserCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //Initialize for flashlight
    ImageButton imageButton;
    Camera camera;
    Camera.Parameters parameter;
    boolean isFlash = false;
    boolean isOn =false;
    boolean isHidden = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //Casting
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        //Checking if a device has flashlight
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            camera = camera.open();
            parameter = camera.getParameters();
            isFlash =true;
        }
        //Getting lenght and width of the phone to locate where the on/off button is
        DisplayMetrics metrics = HomeActivity.this.getResources().getDisplayMetrics();
        final int screenWidth = metrics.widthPixels;
        final int screenHeight = metrics.heightPixels;

        //Adding sound effect to Flash Light Button click
        final MediaPlayer mp = MediaPlayer.create(HomeActivity.this,R.raw.penclick);
        final MediaPlayer mp2 = MediaPlayer.create(HomeActivity.this,R.raw.button_click);

        imageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                double x = event.getX();
                double y = event.getY();

                double expectedWidth1 = (40.44*screenWidth)/100;
                double expectedWidth2 = (57.29*screenWidth)/100;
                double expectedHeight1 = (58.68*screenHeight)/100;
                double expectedHeight2 = (68.8*screenHeight)/100;

                //if the button is touch on torchLight ImageButton
                if(x>=expectedWidth1 && x<=expectedWidth2 && y>=expectedHeight1 && y<=expectedHeight2){
                    // If device has flashlight and its not in use
                    if (isFlash){
                        if(!isOn){
                            mp.start();
                            imageButton.setImageResource(R.drawable.ontorch);
                            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            camera.setParameters(parameter);
                            camera.startPreview();
                            isOn=true;
                        }else{
                            mp2.start();
                            imageButton.setImageResource(R.drawable.offtorch);
                            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            camera.setParameters(parameter);
                            camera.stopPreview();
                            isOn=false;
                        }
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        builder.setTitle("Error...");
                        builder.setMessage("FlashLight is not available on this device...");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }

                return true;
            }

        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    @Override
    protected void onDestroy() {
        if(camera!=null){
            camera.release();
            camera=null;
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navItem_exit) {
            drawer.closeDrawer(GravityCompat.START);
            if(camera!=null){
                camera.release();
                camera=null;
            }
            finishAffinity();

        } else if (id == R.id.navItem_about) {
            NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
            Menu nav_menu = navView.getMenu();
            if (isHidden){
                nav_menu.findItem(R.id.showHide).setVisible(true);
                isHidden =false;
            }else{
                nav_menu.findItem(R.id.showHide).setVisible(false);
                isHidden =true;
            }

        } else if (id == R.id.navItem_notification) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
            Intent intent = new Intent(HomeActivity.this, Settings.class);
            startActivity(intent);

        }

        return true;
    }
}
