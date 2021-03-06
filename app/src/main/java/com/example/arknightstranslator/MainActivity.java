 package com.example.arknightstranslator;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.example.arknightstranslator.TranslateServices.*;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 1404;
    private ImageButton chameleon;
    private EditText keyEditText;
    private Intent serviceOverlayIntent;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean foreground = false;

    private MediaProjectionManager mgr;
    private static final int REQUEST_SCREENSHOT=59706;

    private ScreenshotService screenshotService;
    private OverlayService overlayService;
    boolean mBound = false;
    boolean mBound1 = false;
    ServiceConnection serviceConnection;
    ServiceConnection mConnection;
    private Bitmap bitmap;

    private Handler statusHandler = new Handler();
    private Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            if (foreground)
                chameleon.setImageResource(OverlayService.isRunning ? R.mipmap.chameleon_on : R.mipmap.chameleon_off);
            statusHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        //?????????????? intent ?????? ??????????????
        serviceOverlayIntent = new Intent(this, OverlayService.class);
        serviceOverlayIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        preferences = getSharedPreferences(SharedPref.NAME, MODE_PRIVATE);
        editor = preferences.edit();


        PreferencesSingleton preferencesSingleton = PreferencesSingleton.getInstance();
        PreferencesSingleton.getInstance().initPreferences(this);
        keyEditText.setText( PreferencesSingleton.getInstance().getPreferences().getKey());

        //This permission is by default available for API<23. But for API > 23 you have to ask for the permission in runtime. ?????????????????? ?????????? ???? ??????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initOverlayServiceStarter();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
        }

    }

    private void initViews()
    {
        chameleon = findViewById(R.id.chameleon);
        keyEditText = findViewById(R.id.keyEditText);
    }

    private void initOverlayServiceStarter() {
        chameleon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatus(!OverlayService.isRunning);
            }
        });
        statusHandler.post(statusChecker);
    }

    private void changeStatus(boolean status) {
        chameleon.setImageResource(status? R.mipmap.chameleon_on : R.mipmap.chameleon_off);
        if (status) {
            startService(serviceOverlayIntent);

            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    OverlayService.LocalBinder binder = (OverlayService.LocalBinder) service;

                    overlayService = binder.getService();
                    mBound1 = true;
                    setListenerOnService(overlayService);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mBound1=false;
                }
            };

            bindService(serviceOverlayIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        else {
            this.unbindService(serviceConnection);
            stopService(new Intent(MainActivity.this,OverlayService.class));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//======================================= ?????????????? ???????????? OverlayService
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not. Settings activity never returns proper value so instead check with following method
            if (Settings.canDrawOverlays(this)) {
                initOverlayServiceStarter();
            }
            else { //Permission is not available
                Toast.makeText(this,"Draw over other app permission not available. Closing the application", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
//=====================================     ?????????????? ???????????? MediaProjection, ???????????????? ?? mService
        if (requestCode==REQUEST_SCREENSHOT) {
            if (resultCode==RESULT_OK) {
                startMediaProjectionService(requestCode,resultCode,data);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.defaultmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item_settings:

                Intent settingActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingActivity);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        foreground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        foreground = false;
    }

    @Override
    protected void onDestroy() {
        this.unbindService(serviceConnection);
        stopService(new Intent(MainActivity.this,OverlayService.class));

        this.unbindService(mConnection);
        stopService(new Intent(MainActivity.this, ScreenshotService.class));

        super.onDestroy();
    }

    private void startMediaProjectionService(int requestCode, int resultCode, Intent data)
    {
        Intent i = new Intent(this, ScreenshotService.class)
                .putExtra(ScreenshotService.EXTRA_RESULT_CODE, resultCode)
                .putExtra(ScreenshotService.EXTRA_RESULT_INTENT, data);
        startService(i);

        /** Defines callbacks for service binding, passed to bindService() */
        mConnection= new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                ScreenshotService.LocalBinder binder = (ScreenshotService.LocalBinder) service;

                screenshotService = binder.getService();
                mBound = true;
                setListenerOnService(screenshotService);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void setListenerOnService(Service service)
    {
        if(service.getClass().getName().equals(getPackageName()+".ScreenshotService")) {
            screenshotService.addEventListener(new MyEventListener() {
                @Override
                public void processEvent(MyEvent event) {
                    bitmap = screenshotService.getBitmap();
                    TesseractOCR OCR = new TesseractOCR(getApplicationContext());
                    String text = OCR.recognize(bitmap);
                    text = text.replace("\n", " ");

                    String translationServiceName = PreferencesSingleton.getInstance().getPreferences().getServiceName();
                    ATranslation translation  =null;
                    //TODO ???????????????????? ???????? ??????????. ?????????? ?????????????? ???? ???????????? ?????????????????????? ??????????: ???????? ?????????????? ?? ???????????? ??????????, ???????? ???????????????????? ???????????? ???? ???????????????????? ??????????????????
                    switch (translationServiceName) {
                        case "myMemories": {
                            translation = new MyMemory_Service();
                        }
                        case "cloudAPI": {
                            translation = new CloudAPI_Service();
                        }
                        case "offline": {
                            translation = new OfflineTranslation();
                        }
                    }

                    ATranslation finTranslation = translation;
                    translation.addOnTranslateListener(new MyEventListener() {
                        @Override
                        public void processEvent(MyEvent event) {
                            String result = finTranslation.getText();
                                    setText(result);
                        }
                    });
                    translation.translate(text);
                }
            });
        }


        if(service.getClass().getName().equals(getPackageName()+".OverlayService")) {
            overlayService.addEventListener(new MyEventListener() {
                @Override
                public void processEvent(MyEvent event) {
                        screenshotService.TakeScreen(overlayService.getTranslateArea());  //???????????? ????????????????
                }
            });
        }
    }

    public void setText(String text)
    {
        text=TextCleaner.removeCSS(text);
        overlayService.setTextInTextView(text);
    }
}