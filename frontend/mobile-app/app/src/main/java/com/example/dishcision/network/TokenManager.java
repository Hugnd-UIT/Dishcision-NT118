package com.example.dishcision.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class TokenManager {

    private static final String PREF_NAME = "dishcision_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthData(String token, String email, String name) {
        saveAuthData(token, 0, email, name);
    }

    public void saveAuthData(String token, int userId, String email, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        if (token != null) editor.putString(KEY_ACCESS_TOKEN, token);
        int resolvedId = userId > 0 ? userId : parseUserIdFromJwt(token);
        if (resolvedId > 0) editor.putInt(KEY_USER_ID, resolvedId);
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

    public int getUserId() {
        int id = prefs.getInt(KEY_USER_ID, 0);
        if (id > 0) return id;
        return parseUserIdFromJwt(getToken());
    }

    public boolean isLoggedIn() {
        return isValidJwtToken(getToken());
    }

    public static boolean isValidJwtToken(String rawToken) {
        if (rawToken == null) return false;
        String token = rawToken.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        if (token.isEmpty() || "demo_token".equals(token) || token.startsWith("google_token_")) {
            return false;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        try {
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            JSONObject payload = new JSONObject(new String(decoded, StandardCharsets.UTF_8));
            long exp = payload.optLong("exp", 0L);
            return exp == 0L || (exp * 1000L) > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    public static int parseUserIdFromJwt(String rawToken) {
        if (rawToken == null) return 0;
        String token = rawToken.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) return 0;
        try {
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            JSONObject payload = new JSONObject(new String(decoded, StandardCharsets.UTF_8));
            return payload.optInt("userId", 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public void clearAuth() {
        prefs.edit().clear().apply();
    }
}
