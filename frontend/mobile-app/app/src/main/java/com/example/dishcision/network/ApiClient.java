package com.example.dishcision.network;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // IP Wi-Fi thực tế của máy bạn đang chạy Docker Backend
    private static String baseUrl = "http://192.168.1.36:3000/api/v1/";
    private static Retrofit retrofit = null;

    public static void setBaseUrl(String newBaseUrl) {
        if (newBaseUrl != null && !newBaseUrl.isEmpty()) {
            if (!newBaseUrl.endsWith("/")) {
                newBaseUrl += "/";
            }
            baseUrl = newBaseUrl;
            retrofit = null; // Re-initialize Retrofit instance with new URL
        }
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getAuthHeader(Context context) {
        if (context == null) return "Bearer demo_token";
        SharedPreferences prefs = context.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return "Bearer demo_token";
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder()
                                    .header("Connection", "close")
                                    .header("Accept-Encoding", "identity")
                                    .build();
                            return chain.proceed(request);
                        }
                    })
                    .retryOnConnectionFailure(true)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
