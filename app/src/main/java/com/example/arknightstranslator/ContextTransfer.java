package com.example.arknightstranslator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

public class ContextTransfer extends Activity {
    private static Context context;
    private static final ContextTransfer INSTANCE = new ContextTransfer(context);

    private MediaProjectionManager mgr;
    private static final int REQUEST_SCREENSHOT=59706;

    public Bundle SavedInstace;

    public ContextTransfer(Context context)
    {
        this.context = context;
    }

    public Context getMainContext()
    {
       return context;
    }
    public static ContextTransfer getInstance(){
        return INSTANCE;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CallScreenService();
    }


private void CallScreenService()
{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mgr.createScreenCaptureIntent(),
                REQUEST_SCREENSHOT);
    }

}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_SCREENSHOT) {
            if (resultCode==RESULT_OK) {
                Intent i=
                        new Intent(this, ScreenshotService.class)
                                .putExtra(ScreenshotService.EXTRA_RESULT_CODE, resultCode)
                                .putExtra(ScreenshotService.EXTRA_RESULT_INTENT, data);

                startService(i);
            }
        }

        finish();
    }
}
