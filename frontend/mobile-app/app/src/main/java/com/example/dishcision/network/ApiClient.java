package com.example.dishcision.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // 10.0.2.2 là IP đại diện cho localhost của máy tính host khi chạy trên Android Emulator.
    // Nếu chạy trên máy thật, có thể gọi setBaseUrl("http://IP_CUA_MAY_TINH:3000/api/v1/");
    private static String baseUrl = "http://10.0.2.2:3000/api/v1/";
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

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
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
