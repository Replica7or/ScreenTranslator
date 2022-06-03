package com.example.arknightstranslator;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class TranslatorClient {
    protected  final OkHttpClient okHttpClient;
    protected MainActivity main;
    protected String key;

    public TranslatorClient(MainActivity main)
    {
        this.main = main;
        this.key = PreferencesSingleton.getInstance().getPreferences().getKey();
        okHttpClient =  new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public abstract void translate(String textToTranslate);
}
