package com.example.arknightstranslator.TranslateServices;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arknightstranslator.ATranslation;
import com.example.arknightstranslator.MyEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class OfflineTranslation extends ATranslation {
    Translator englishRussianTranslator;

    public OfflineTranslation() {
        super();
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.RUSSIAN)
                        .build();
        englishRussianTranslator =  Translation.getClient(options);
        downloadModel();
    }

    @Override
    public void translate(String textToTranslate) {
        englishRussianTranslator.translate(textToTranslate)
                .addOnSuccessListener(
                        new OnSuccessListener() {

                            @Override
                            public void onSuccess(Object o) {
                                text = o.toString();
                                notifyEventListeners(new MyEvent((this)));
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                // ...
                            }
                        });
    }

    private void downloadModel() {
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        englishRussianTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                Log.d("MODEL MARK","DOWNLOADED");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                                Log.d("MODEL MARK","FAILURE");
                            }
                        });
    }
}
