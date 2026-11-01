package com.example.dishcision.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "dishcision_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthData(String token, String email, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        if (token != null) editor.putString(KEY_ACCESS_TOKEN, token);
        if (email != null) editor.putString(KEY_USER_EMAIL, email);
        if (name != null) editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getFormattedToken() {
        String token = getToken();
        if (token == null || token.isEmpty()) {
            return "";
        }
        if (token.startsWith("Bearer ")) {
            return token;
        }
        return "Bearer " + token;
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearAuth() {
        prefs.edit().clear().apply();
    }
}
