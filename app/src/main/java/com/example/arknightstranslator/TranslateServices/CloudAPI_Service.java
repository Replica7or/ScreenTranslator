package com.example.arknightstranslator.TranslateServices;

import android.util.Log;

import com.example.arknightstranslator.ITranslatorClient;
import com.example.arknightstranslator.MainActivity;
import com.example.arknightstranslator.TranslatorBody;
import com.example.arknightstranslator.TranslatorClient;
import com.example.arknightstranslator.TranslatorResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CloudAPI_Service extends TranslatorClient {
    public CloudAPI_Service(MainActivity main) {
        super(main);
    }


    public  void translate(String textToTranslate)
    {
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

        Call<TranslatorResponse> call = api.translate_CloudAPIService(body,key,"application/json");

        call.enqueue(new Callback<TranslatorResponse>() {
            @Override
            public void onResponse(Call<TranslatorResponse> call, Response<TranslatorResponse> response) {
                if (response.body() != null) {
                    String text = response.body().text;
                    main.setText(text);   //TODO удбрать связь с main и вызов отсюда, возвращать текст вместо этого
                    Log.d("QQQQ","text");
                }
            }

            @Override
            public void onFailure(Call<TranslatorResponse> call, Throwable t) {
                if(t.getMessage().equals("timeout"))
                {
                    //main.setText("превышено время ожидания запроса на перевод ");
                }
                Log.d("QQQQ",t.getMessage());
            }
        });
    }
}
