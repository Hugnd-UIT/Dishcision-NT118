package com.example.dishcision.network;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shows the signed-in user's name, @handle and avatar in a header card.
 * Renders the cached values immediately, then refreshes from GET users/me and updates the cache.
 */
public final class CurrentUserBinder {

    private static final String PREFS = "DishcisionPrefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_USERNAME = "user_username";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_AVATAR = "avatar_url";
    private static final String KEY_CITY = "user_city";

    private CurrentUserBinder() {}

    public static void bind(Activity activity, ImageView avatar, TextView name, TextView handle) {
        if (activity == null) return;
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        render(avatar, name, handle,
                prefs.getString(KEY_NAME, null),
                prefs.getString(KEY_USERNAME, null),
                prefs.getString(KEY_EMAIL, null),
                prefs.getString(KEY_AVATAR, null));

        String authHeader = ApiClient.getAuthHeader(activity);
        if (authHeader.isEmpty()) return;

        ApiClient.getApiService().getMyProfile(authHeader)
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        if (response.code() == 401) {
                            AuthHelper.handleUnauthorized(activity);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getData() == null) {
                            return;
                        }
                        UserProfileResponse.UserData user = response.body().getData();
                        SharedPreferences.Editor editor = prefs.edit();
                        if (user.userId > 0) editor.putInt("user_id", user.userId);
                        if (notEmpty(user.fullName)) editor.putString(KEY_NAME, user.fullName);
                        if (notEmpty(user.username)) editor.putString(KEY_USERNAME, user.username);
                        if (notEmpty(user.email)) editor.putString(KEY_EMAIL, user.email);
                        if (notEmpty(user.avatarUrl)) {
                            editor.putString(KEY_AVATAR, user.avatarUrl);
                        } else {
                            editor.remove(KEY_AVATAR);
                        }
                        if (notEmpty(user.bio)) {
                            editor.putString("user_bio", user.bio.trim());
                        } else {
                            editor.remove("user_bio");
                        }
                        if (notEmpty(user.city)) {
                            editor.putString(KEY_CITY, user.city.trim());
                        } else {
                            editor.remove(KEY_CITY);
                        }
                        if (user.preferences != null) {
                            editor.putStringSet("user_taste_preferences", new java.util.HashSet<>(user.preferences));
                        }
                        if (user.userId > 0 && user.allergies != null) {
                            editor.putStringSet("user_allergies_" + user.userId, new java.util.HashSet<>(user.allergies));
                        }
                        editor.putBoolean("is_google_login", user.isGoogleAccount);
                        editor.apply();
                        if (user.userId > 0) {
                            new TokenManager(activity).saveAuthData(null, user.userId, user.email, user.fullName);
                        }
                        render(avatar, name, handle, user.fullName, user.username, user.email, user.avatarUrl);
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        // Keep the cached values on screen
                    }
                });
    }

    /** Same as {@link #bind} but writes "Name • @handle" into a single TextView. */
    public static void bindInline(Activity activity, TextView nameAndHandle) {
        if (nameAndHandle == null) return;
        TextView name = new TextView(activity);
        TextView handle = new TextView(activity);
        Runnable compose = () -> {
            String n = name.getText().toString();
            String h = handle.getText().toString();
            nameAndHandle.setText(n.isEmpty() ? h : (h.isEmpty() ? n : n + " • " + h));
        };
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) { compose.run(); }
        };
        name.addTextChangedListener(watcher);
        handle.addTextChangedListener(watcher);
        bind(activity, null, name, handle);
    }

    private static void render(ImageView avatar, TextView name, TextView handle,
                               String fullName, String username, String email, String avatarUrl) {
        if (name != null) {
            if (notEmpty(fullName)) name.setText(fullName);
            else if (notEmpty(username)) name.setText(username.replace("@", ""));
        }
        if (handle != null) {
            if (notEmpty(username)) handle.setText("@" + username.replace("@", ""));
            else if (notEmpty(email)) handle.setText(email);
        }
        if (avatar != null) {
            ImageLoader.loadAvatar(avatar, avatarUrl);
        }
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
