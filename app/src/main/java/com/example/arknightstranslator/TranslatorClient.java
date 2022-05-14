package com.example.arknightstranslator;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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

    public void translate(String textToTranslate)
    {

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
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
        Call<TranslatorResponse> call = api.translate(body,key,"application/json");
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
                    main.setText("превышено время ожидания запроса на перевод");
                }
                Log.d("QQQQ",t.getMessage());
            }
        });
    }
}
