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

public class TranslatorClient {

    MainActivity main;
    String key;

    public TranslatorClient(MainActivity main)
    {
        this.main = main;
        this.key = PreferencesSingleton.getInstance().getPreferences().getKey();
    }

    public void translate_MyMemoryService(String textToTranslate)
    {

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.mymemory.translated.net")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        ITranslatorClient api = retrofit.create(ITranslatorClient.class);
        Call<JSONresponce> call = api.translate_MyMemoryService(textToTranslate,"en|ru");

        call.enqueue(new Callback<JSONresponce>() {
            @Override
            public void onResponse(Call<JSONresponce> call, Response<JSONresponce> response) {
                try {
                    String text = response.body().getMatches()[0].getTranslation();
                //Log.d("QQQQQQQQQQQ1",String.valueOf(response.body().getMatches()[0].getId()));
                    Log.d("QQQ",text);
                    main.setText(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JSONresponce> call, Throwable t) {
                if(t.getMessage().equals("timeout"))
                {
                    main.setText("превышено время ожидания запроса на перевод ");
                }
                Log.d("QQQQ",t.getMessage());
            }
        });
    }


    public void translate(String textToTranslate)
    {

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.cloudapi.stream")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        ITranslatorClient api = retrofit.create(ITranslatorClient.class);

        TranslatorBody body = new TranslatorBody();
        body.from = "en";
        body.to = "ru";
        body.text = textToTranslate;
//
        Call<TranslatorResponse> call = api.translate_CloudAPIService(body,key,"application/json");

        call.enqueue(new Callback<TranslatorResponse>() {
            @Override
            public void onResponse(Call<TranslatorResponse> call, Response<TranslatorResponse> response) {
                if (response.body() != null) {
                    String text = response.body().text;
                    main.setText(text);
                    Log.d("QQQQ","text");
                }
            }

            @Override
            public void onFailure(Call<TranslatorResponse> call, Throwable t) {
                if(t.getMessage().equals("timeout"))
                {
                    main.setText("превышено время ожидания запроса на перевод ");
                }
                Log.d("QQQQ",t.getMessage());
            }
        });
    }
}
