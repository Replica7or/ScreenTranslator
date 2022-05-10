package com.example.arknightstranslator;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageTransmogrifier implements ImageReader.OnImageAvailableListener {
    private final int width;
    private final int height;
    private final ImageReader imageReader;
    private final ScreenshotService svc;
    private Bitmap latestBitmap=null;
    private TranslateArea translateArea;
    int COUNT =0;

    ImageTransmogrifier(ScreenshotService svc, TranslateArea translateArea) {
        this.svc=svc;
        this.translateArea = translateArea;

        Display display=svc.getWindowManager().getDefaultDisplay();

        Point size=new Point();

        display.getRealSize(size);

        int width=size.x;
        int height=size.y;

       // while (width*height > (2<<19)) {
       //     width=width>>1;
       //     height=height>>1;
       // }

        this.width=width;
        this.height=height;

        imageReader=ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(this, svc.getHandler());
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        final Image image=imageReader.acquireNextImage();
        COUNT++;

        if(COUNT>1)
        {
            if(image!=null)
            image.close();
            close();
            return;
        }

        if (image!=null) {
            Image.Plane[] planes=image.getPlanes();
            ByteBuffer buffer=planes[0].getBuffer();
            int pixelStride=planes[0].getPixelStride();
            int rowStride=planes[0].getRowStride();
            int rowPadding=rowStride - pixelStride * width;
            int bitmapWidth=width + rowPadding / pixelStride;

            if (latestBitmap == null || latestBitmap.getWidth() != bitmapWidth ||  latestBitmap.getHeight() != height)
            {
                if (latestBitmap != null)
                {
                    latestBitmap.recycle();
                }
                latestBitmap=Bitmap.createBitmap(bitmapWidth,height, Bitmap.Config.ARGB_8888);
            }

            latestBitmap.copyPixelsFromBuffer(buffer);
            image.close();

            if(translateArea.width==0 || translateArea.height==0)
            {
                translateArea.width = width;
                translateArea.height= height;
            }

            Bitmap cropped=Bitmap.createBitmap(latestBitmap, translateArea.x, translateArea.y, translateArea.width, translateArea.height);
            cropped = toGrayscale(cropped);
            cropped = toStrictBlackWhite(cropped);
            //Bitmap cropped=Bitmap.createBitmap(latestBitmap, 0, 0, width, height);
            svc.setBitmap(cropped);

            //ByteArrayOutputStream baos=new ByteArrayOutputStream();
            //cropped.compress(Bitmap.CompressFormat.PNG, 100, baos);
           // byte[] newPng=baos.toByteArray();
            //svc.processImage(newPng);
        }
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
//-15592942
    //  FF000000 - black, FFFFFF - white        -2829100
    public Bitmap toStrictBlackWhite(Bitmap bitmap){
        int length = bitmap.getWidth()*bitmap.getHeight();
        int[] array = new int[length];
        bitmap.getPixels(array,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        for (int i=0;i<length;i++){
// If the bitmap is in ARGB_8888 format
            if(array[i]<-2829100) {
                array[i] = -16777216;
            }

    }
bitmap.setPixels(array,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        return bitmap;
    }

    Surface getSurface() {
        return(imageReader.getSurface());
    }

    int getWidth() {
        return(width);
    }

    int getHeight() {
        return(height);
    }

    void close() {
        imageReader.close();
    }
}