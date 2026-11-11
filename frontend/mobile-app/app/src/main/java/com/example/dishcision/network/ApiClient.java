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

    public static final String DEFAULT_BASE_URL = "https://dishcision.duckdns.org/api/v1/";

    private static String baseUrl = DEFAULT_BASE_URL;
    private static Retrofit retrofit = null;

    public static void init(Context context) {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE);
            String savedUrl = prefs.getString("custom_base_url", null);
            if (savedUrl != null && !savedUrl.trim().isEmpty()) {
                setBaseUrl(savedUrl);
            }
        }
    }

    public static void setBaseUrl(String newBaseUrl) {
        if (newBaseUrl != null && !newBaseUrl.trim().isEmpty()) {
            newBaseUrl = newBaseUrl.trim();
            if (!newBaseUrl.startsWith("http://") && !newBaseUrl.startsWith("https://")) {
                newBaseUrl = "http://" + newBaseUrl;
            }
            if (!newBaseUrl.endsWith("/")) {
                newBaseUrl += "/";
            }
            if (!newBaseUrl.contains("/api/v1/")) {
                newBaseUrl += "api/v1/";
            }
            baseUrl = newBaseUrl;
            retrofit = null; // Re-initialize Retrofit instance with new URL
        }
    }

    public static void setBaseUrl(Context context, String newBaseUrl) {
        setBaseUrl(newBaseUrl);
        if (context != null) {
            context.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("custom_base_url", baseUrl)
                    .apply();
        }
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getServerRootUrl() {
        if (baseUrl != null && baseUrl.contains("/api/v1")) {
            return baseUrl.substring(0, baseUrl.indexOf("/api/v1"));
        }
        return baseUrl != null ? baseUrl : "";
    }

    public static String getAuthHeader(Context context) {
        if (context == null) return "Bearer demo_token";
        TokenManager tokenManager = new TokenManager(context);
        String formatted = tokenManager.getFormattedToken();
        if (formatted != null && !formatted.isEmpty()) {
            return formatted;
        }
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
