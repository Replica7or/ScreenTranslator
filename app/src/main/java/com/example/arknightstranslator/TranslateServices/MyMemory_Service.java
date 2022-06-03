package com.example.arknightstranslator.TranslateServices;

import android.util.Log;

import com.example.arknightstranslator.ITranslatorClient;
import com.example.arknightstranslator.JSONresponce;
import com.example.arknightstranslator.MainActivity;
import com.example.arknightstranslator.TranslatorClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyMemory_Service extends TranslatorClient {


    public MyMemory_Service(MainActivity main) {
        super(main);
    }

    public void translate(String textToTranslate) {

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
                    main.setText(text);     //TODO удбрать связь с main и вызов отсюда, возвращать текст вместо этого
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JSONresponce> call, Throwable t) {
                if(t.getMessage().equals("timeout"))
                {
                    //main.setText("превышено время ожидания запроса на перевод ");
                }
                Log.d("QQQQ",t.getMessage());
            }
        });
    }
}
