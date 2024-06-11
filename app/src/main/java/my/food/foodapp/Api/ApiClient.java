package my.food.foodapp.Api;

import android.content.Context;

import java.net.CookieHandler;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.1.45:8080/";

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient getHttpClient(Context context) {
        return new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(CookieHandler.getDefault()))
                .build();
    }

}
