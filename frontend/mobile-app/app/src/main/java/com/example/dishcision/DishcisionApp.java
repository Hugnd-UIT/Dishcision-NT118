package com.example.dishcision;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.TokenManager;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Application entry point handling API initialization and foreground single-session verification.
 */
public class DishcisionApp extends Application implements Application.ActivityLifecycleCallbacks {

    private static final long SESSION_POLL_INTERVAL_MS = 6000L;
    private static final long MIN_RESUME_CHECK_GAP_MS = 1500L;

    private static final Set<String> AUTH_EXEMPT_ACTIVITIES = new HashSet<>(Arrays.asList(
            MainActivity.class.getName(),
            LoginActivity.class.getName(),
            RegisterActivity.class.getName(),
            ForgotPasswordActivity.class.getName(),
            VerifyOtpActivity.class.getName(),
            ResetPasswordActivity.class.getName(),
            ResetPasswordSuccessActivity.class.getName()
    ));

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private WeakReference<Activity> currentActivityRef = new WeakReference<>(null);
    private int resumedCount = 0;
    private long lastSessionCheckMs = 0L;
    private boolean sessionCheckInFlight = false;

    private final Runnable sessionPollRunnable = new Runnable() {
        @Override
        public void run() {
            if (resumedCount <= 0) return;
            verifyActiveSessionIfNeeded();
            if (resumedCount > 0) {
                mainHandler.postDelayed(this, SESSION_POLL_INTERVAL_MS);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivityRef = new WeakReference<>(activity);
        resumedCount++;
        ApiClient.setCurrentActivity(activity);

        mainHandler.removeCallbacks(sessionPollRunnable);
        if (shouldCheckSessionFor(activity)) {
            long elapsed = System.currentTimeMillis() - lastSessionCheckMs;
            if (elapsed >= MIN_RESUME_CHECK_GAP_MS) {
                verifyActiveSessionIfNeeded();
            }
            mainHandler.postDelayed(sessionPollRunnable, SESSION_POLL_INTERVAL_MS);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        resumedCount = Math.max(0, resumedCount - 1);
        if (currentActivityRef.get() == activity) {
            currentActivityRef = new WeakReference<>(null);
            ApiClient.setCurrentActivity(null);
        }
        if (resumedCount == 0) {
            mainHandler.removeCallbacks(sessionPollRunnable);
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}

    private boolean shouldCheckSessionFor(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return false;
        if (AUTH_EXEMPT_ACTIVITIES.contains(activity.getClass().getName())) return false;
        return new TokenManager(this).isLoggedIn();
    }

    private void verifyActiveSessionIfNeeded() {
        Activity activity = currentActivityRef.get();
        if (!shouldCheckSessionFor(activity) || sessionCheckInFlight) return;
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;

        sessionCheckInFlight = true;
        lastSessionCheckMs = System.currentTimeMillis();
        ApiClient.getApiService().checkSessionStatus(authHeader).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                sessionCheckInFlight = false;
                // 401 is handled globally by ApiClient's OkHttp response interceptor.
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                sessionCheckInFlight = false;
                // Ignore transient network drops; offline users must remain signed in locally.
            }
        });
    }
}
