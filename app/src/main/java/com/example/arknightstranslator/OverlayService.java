package com.example.arknightstranslator;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GestureDetectorCompat;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static java.security.AccessController.getContext;

import com.google.android.material.textfield.TextInputEditText;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

public class OverlayService extends Service {

    public static boolean isRunning;

    private WindowManager.LayoutParams topParams;
    private WindowManager.LayoutParams edgeParams;
    private RelativeLayout topView;
    private View topGrab;
    private View edge;
    private View takeScreen;
    private View translateAreaView;
    private WindowManager windowManager;
    private GestureDetectorCompat gestureDetector;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private TextView textView;

    private List<MyEventListener> eventListeners = new LinkedList<>();
    private final IBinder mBinder = new OverlayService.LocalBinder();

    /**========================== EVENT LISTENER BLOCK START ========================*/
    public void addEventListener(MyEventListener eventListener)
    {
        eventListeners.add(eventListener);
    }
    public void notifyEventListeners(MyEvent event)
    {
        for(MyEventListener eventListener:eventListeners)
        {
            eventListener.processEvent(event);
        }
    }
    /**========================== EVENT LISTENER BLOCK END ========================*/

    public class LocalBinder extends Binder {
        OverlayService getService() {
            // Return this instance of LocalService so clients can call public methods
            return OverlayService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //=================================================================================

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        isRunning = true;
        initScreenUtils();

        preferences = getSharedPreferences(SharedPref.NAME, MODE_PRIVATE);
        editor = preferences.edit();
        gestureDetector = new GestureDetectorCompat(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (topParams.width == 0) {
                    topParams.width = ScreenUtils.width;
                    topView.setVisibility(View.VISIBLE);
                    windowManager.updateViewLayout(topView, topParams);
                } else {
                    topParams.width = 0;
                    windowManager.updateViewLayout(topView, topParams);
                    topView.setVisibility(View.GONE);
                }
                return true;
            }
        });
        initViews();
        initMovable();
        //initOnClicks();
        //initOnTouches();
       // initProgress();
    }

    @Override
    public void onDestroy() {
        windowManager.removeView(topView);
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Toast.makeText(this, "REMOVE TASK", Toast.LENGTH_SHORT).show();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "UNBINDED", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_DOWN: {
                    topParams = (WindowManager.LayoutParams) topView.getLayoutParams();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    topParams.x = (int) event.getRawX() - ScreenUtils.width / 2;
                    topParams.y = (int) event.getRawY() - ScreenUtils.height / 2;
                    windowManager.updateViewLayout(topView, topParams);
                }
                break;
            }
            return true;
        }
    };

    private void initMovable() {

    }

    private void initViews() {
        topView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.top, null);

        topParams = new WindowManager.LayoutParams(/*ScreenUtils.width*/ 1300, 300,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY ,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        topParams.x = 0;
        topParams.y = 0;

        topView.setOnTouchListener(touchListener);


        windowManager.addView(topView, topParams);

        translateAreaView = topView.findViewById((R.id.relativelayout));

        textView = topView.findViewById((R.id.textView));
        takeScreen = (Button) topView.findViewById(R.id.button2);
        takeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyEventListeners(new MyEvent(getApplicationContext()));
            }
        });
       // textEdit = (EditText) topView.findViewById(R.id.editTextTextPersonName);
       /* edge = new View(getApplicationContext());
        edgeParams = new WindowManager.LayoutParams(
                ScreenUtils.width/20,
                ScreenUtils.height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        edgeParams.gravity = Gravity.RIGHT;
        windowManager.addView(edge, edgeParams);*/
    }

    public TranslateArea getTranslateArea()
    {
        int [] viewPosition = new int[2];
        translateAreaView.getLocationOnScreen(viewPosition);
        int width = translateAreaView.getWidth();
        int h = translateAreaView.getHeight();
        return new TranslateArea(viewPosition[0],viewPosition[1],translateAreaView.getWidth(),translateAreaView.getHeight());
    }
    private void initScreenUtils() {
        final Display display = windowManager.getDefaultDisplay();
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ScreenUtils.width = display.getWidth();
        ScreenUtils.height = display.getHeight() - statusBarHeight;
    }

 /*    private void initOnClicks() {   //лработка включения/выключения сервиса
       topView.findViewById(R.id.webButton).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                stopSelf();
                return true;
            }
        });
        topView.findViewById(R.id.webButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView == null) {
                    webView = new WebView(getApplicationContext());
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    contentView.addView(webView);
                    webView.setLayoutParams(layoutParams);
                    webView.setWebViewClient(new WebViewClient());
                    webView.loadUrl(preferences.getString(SharedPref.URL, "http://github.com/c0defather"));
                } else {
                    contentView.removeView(webView);
                    webView.destroy();
                    webView = null;
                }
            }
        });
    }private void initProgress() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                topView.findViewById(R.id.content).setAlpha((float) (i/100.0));
                editor.putInt(SharedPref.ALPHA, i).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int progress = preferences.getInt(SharedPref.ALPHA, 50);
        topView.findViewById(R.id.content).setAlpha((float) (progress/100.0));
        seekBar.setProgress(progress);
    }  private void initOnTouches() {
        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        int x = (int)motionEvent.getRawX();
                        int y = (int)motionEvent.getRawY();
                        contentView.setUnmaskCircle(new Circle(x-ScreenUtils.width/6,y,ScreenUtils.width/6));
                        break;
                    case MotionEvent.ACTION_UP:
                        contentView.setUnmaskCircle(null);
                }
                contentView.invalidate();
                return true;
            }
        });
        edge.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
        topGrab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        topParams.height = Math.max((int) motionEvent.getRawY(), ScreenUtils.convertDpToPx(OverlayService.this, 50));
                        windowManager.updateViewLayout(topView, topParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return true;
            }
        });
    }*/


    public void takeTextFromBitmap(String text) {
        try {
            new MyRunnable(text);
                //textView.setText(text);
            //textView.requestFocus();
                //textView.invalidate();
                //textView.requestLayout();
            //openScreenshot(imageFile);
        } catch (Throwable e) {
            Log.d("COR_ERROR", e.getMessage());
            e.printStackTrace();
        }
        //return text;
    }



    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        Toast.makeText(this, uri.toString() +"\t"+imageFile.length(), Toast.LENGTH_SHORT).show();
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }


    class MyRunnable implements Runnable {
        Thread thread;

        String text;
        // Конструктор
        MyRunnable(String text) {
            this.text = text;
            // Создаём новый второй поток
            thread = new Thread(this, "Поток для примера");
            thread.start(); // Запускаем поток
        }

        // Обязательный метод для интерфейса Runnable
        public void run() {
            try {
                textView.setText(text);
            }
            catch (Exception e)
            {
                Log.d("ERRORERROR", e.getMessage());
            }
        }
    }
}
