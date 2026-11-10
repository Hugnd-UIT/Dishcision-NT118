package com.example.dishcision.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.example.dishcision.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthHelper {

    /** Per-account values cached in DishcisionPrefs; device-level settings (notifications…) are kept. */
    private static final String[] USER_CACHE_KEYS = {
            "user_name", "user_username", "user_email", "avatar_url",
            "is_google_login", "user_taste_preferences"
    };

    public static void logout(Activity activity) {
        if (activity == null) return;

        // 1. Tell the backend to invalidate the token (fire-and-forget; must read the header before clearing it)
        String authHeader = ApiClient.getAuthHeader(activity);
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            ApiClient.getApiService().logout(authHeader).enqueue(new Callback<Object>() {
                @Override public void onResponse(Call<Object> call, Response<Object> response) {}
                @Override public void onFailure(Call<Object> call, Throwable t) {}
            });
        }

        clearLocalSession(activity);

        // 4. Redirect to LoginActivity and clear backstack
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    /** Clears tokens, the Google session and cached profile data without calling the backend. */
    public static void clearLocalSession(Activity activity) {
        // 2. Clear TokenManager tokens
        new TokenManager(activity).clearAuth();

        // 3. Clear cached profile of this account
        android.content.SharedPreferences.Editor editor =
                activity.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE).edit();
        for (String key : USER_CACHE_KEYS) editor.remove(key);
        editor.apply();

        // Clear Google Sign-In session
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignIn.getClient(activity, gso).signOut();
        } catch (Exception ignored) {}
    }
}
