package com.example.dishcision.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
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
    private static boolean isDebugBuild = false;

    public static void init(Context context) {
        if (context != null) {
            captureAppFlags(context);
            SharedPreferences prefs = context.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE);
            String savedUrl = prefs.getString("custom_base_url", null);
            if (savedUrl != null && !savedUrl.trim().isEmpty()) {
                setBaseUrl(savedUrl);
            }
        }
    }

    private static void captureAppFlags(Context context) {
        if (context == null) return;
        ApplicationInfo info = context.getApplicationContext().getApplicationInfo();
        boolean debuggable = info != null && (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (debuggable != isDebugBuild) {
            isDebugBuild = debuggable;
            retrofit = null;
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

    public static String normalizeMediaUrl(String rawUrl) {
        if (rawUrl == null) return "";
        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty()) return "";
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String root = getServerRootUrl();
        if (trimmed.startsWith("/")) {
            return root + trimmed;
        }
        return root + "/" + trimmed;
    }

    public static String getAuthHeader(Context context) {
        if (context == null) return "";
        captureAppFlags(context);
        TokenManager tokenManager = new TokenManager(context);
        if (tokenManager.isLoggedIn()) {
            return tokenManager.getFormattedToken();
        }
        return "";
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.redactHeader("Authorization");
            loggingInterceptor.redactHeader("Cookie");
            loggingInterceptor.setLevel(isDebugBuild ? HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);

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
