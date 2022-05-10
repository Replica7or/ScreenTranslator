package com.example.arknightstranslator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.ToneGenerator;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

public class ScreenshotService extends Service {
    private static final String CHANNEL_WHATEVER = "channel_whatever";
    private static final int NOTIFY_ID = 9906;
    static final String EXTRA_RESULT_CODE = "resultCode";
    static final String EXTRA_RESULT_INTENT = "resultIntent";
    static final String ACTION_RECORD = BuildConfig.APPLICATION_ID+".RECORD";
    static final String ACTION_SHUTDOWN = BuildConfig.APPLICATION_ID  +".SHUTDOWN";
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    private Handler handler;
    private MediaProjectionManager mgr;
    private WindowManager wmgr;
    private ImageTransmogrifier it;
    private int resultCode;
    private Intent resultData;
    private TranslateArea translateArea;
    private Bitmap bitmap;

    private final IBinder mBinder = new LocalBinder();

    static final int VIRT_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    final private HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(),
            android.os.Process.THREAD_PRIORITY_BACKGROUND);

    private List<MyEventListener>  eventListeners = new LinkedList<>();

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

    /**==========================    BINDER BLOCK START    ========================*/
    public class LocalBinder extends Binder {
        ScreenshotService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ScreenshotService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
       // throw new IllegalStateException("Binding not supported. Go away.");
    }
    /**==========================     BINDER BLOCK END    ========================*/

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        }
        wmgr=(WindowManager)getSystemService(WINDOW_SERVICE);

        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        if (i.getAction()==null) {
            resultCode=i.getIntExtra(EXTRA_RESULT_CODE, 1337);
            resultData=i.getParcelableExtra(EXTRA_RESULT_INTENT);
            foregroundify();
        }
        else if (ACTION_RECORD.equals(i.getAction())) {
            if (resultData!=null) {
                startCapture();
            }
            else {
                Intent ui= new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(ui);
            }
        }
        else if (ACTION_SHUTDOWN.equals(i.getAction())) {
            stopForeground(true);
            stopSelf();
        }

        return(START_NOT_STICKY);
    }

    @Override
    public void onDestroy() {
        stopCapture();
        super.onDestroy();
    }


    WindowManager getWindowManager() {
        return(wmgr);
    }

    Handler getHandler() {
        return(handler);
    }

    void setBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
        notifyEventListeners(new MyEvent((this)));
        stopCapture();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    void processImage(final byte[] png) {
        new Thread() {
            @Override
            public void run() {
                File output=new File(getExternalFilesDir(null), "screenshot.png");

                try {
                    FileOutputStream fos=new FileOutputStream(output);

                    fos.write(png);
                    fos.flush();
                    fos.getFD().sync();
                    fos.close();

                    MediaScannerConnection.scanFile(ScreenshotService.this, new String[] {output.getAbsolutePath()}, new String[] {"image/png"},null);
                }
                catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Exception writing out screenshot", e);
                }
            }
        }.start();

        stopCapture();
    }

    private void stopCapture() {
        if (projection!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                projection.stop();
            }
            vdisplay.release();
            projection=null;
        }
    }

    public void TakeScreen(TranslateArea translateArea)
    {
        this.translateArea = translateArea;
        startCapture();
    }

    private void startCapture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            try {
                projection = mgr.getMediaProjection(resultCode, resultData);
            }
            catch (IllegalStateException e)
            {
                Toast.makeText(getApplicationContext(), "работаю, хватит тыкать", Toast.LENGTH_SHORT).show();
                return;
            }
            if(translateArea!=null) {
                it = new ImageTransmogrifier(this, translateArea);
            }
            else
            {
                translateArea = new TranslateArea(0,0, 0,0);
            }
            MediaProjection.Callback cb = null;
            cb = new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    vdisplay.release();
                }
            };


            vdisplay = projection.createVirtualDisplay("andshooter", it.getWidth(), it.getHeight(),
                    getResources().getDisplayMetrics().densityDpi,
                    VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);

            projection.registerCallback(cb, handler);
        }
    }

    private void foregroundify() {
        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O && mgr.getNotificationChannel(CHANNEL_WHATEVER)==null) {
            mgr.createNotificationChannel(new NotificationChannel(CHANNEL_WHATEVER,"Whatever", NotificationManager.IMPORTANCE_DEFAULT));
        }

        NotificationCompat.Builder b= new NotificationCompat.Builder(this, CHANNEL_WHATEVER);

        b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);

        b.setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(getString(R.string.app_name));

        b.addAction(R.drawable.ic_record_white_24dp,
                getString(R.string.notify_record),
                buildPendingIntent(ACTION_RECORD));

        b.addAction(R.drawable.ic_eject_white_24dp,
                getString(R.string.notify_shutdown),
                buildPendingIntent(ACTION_SHUTDOWN));

        startForeground(NOTIFY_ID, b.build());
    }

    private PendingIntent buildPendingIntent(String action) {
        Intent i=new Intent(this, getClass());

        i.setAction(action);

        return(PendingIntent.getService(this, 0, i, 0));
    }
}
