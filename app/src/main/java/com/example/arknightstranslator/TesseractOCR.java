package com.example.arknightstranslator;

import android.content.Context;
import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TesseractOCR {

    private String mDataPath;
    private File parentfile;
    private TessBaseAPI mTess;
    private Bitmap bitmap;
    private Context context;

    public TesseractOCR(Context context) {
        this.context = context;

        mDataPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tessdata/";

        parentfile = new File(mDataPath);
        if (! parentfile.exists ()) {// Убедитесь, что путь существует
            parentfile.mkdir();
        }

        copyFiles(); // копировать шрифт на телефон
        String lang = "eng"; // Использовать упрощенное обнаружение китайского + английского
        mTess = new TessBaseAPI();
        mTess.init(Environment.getExternalStorageDirectory().getAbsolutePath(), lang);
    }

    public String recognize(Bitmap bitmap) {
        mTess.setImage(bitmap);
        String result = mTess.getUTF8Text();
        return result;
    }

    private void copyFiles() {
        String[] datafilepaths = new String[]{mDataPath + "/eng.traineddata"}; // Скопировать два шрифта в прошлом
        for (String datafilepath : datafilepaths) {
            copyFile(datafilepath);
        }
    }

    private void copyFile(String datafilepath) {
        try {
            String filepath = datafilepath;
            String[] filesegment = filepath.split(File.separator);
            String filename = filesegment[(filesegment.length - 1)]; // Получить имя файла шрифта
            AssetManager assetManager = context.getAssets();
            InputStream instream = assetManager.open(filename); // Открыть файл шрифта
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();
            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
