package com.example.arknightstranslator;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ITranslatorClient {

    @GET("/search/repositories")
    Call<ResponseBody> reposByName(@Query("q") String q, @Query("page") int page, @Query("per_page") int per_page);

    @POST("/")
    Call<TranslatorResponse>  translate(@Body TranslatorBody registrationBody, @Header("x-key") String KEY, @Header("content-type") String contentType);
}
