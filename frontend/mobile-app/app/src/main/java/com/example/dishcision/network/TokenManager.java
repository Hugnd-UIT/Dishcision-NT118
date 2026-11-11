package com.example.dishcision.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

public class TokenManager {

    private static final String PREF_NAME_SECURE = "dishcision_auth_prefs_secure";
    private static final String LEGACY_PREF_NAME = "dishcision_auth_prefs";
    private static final String LEGACY_APP_PREFS = "DishcisionPrefs";
    private static final String DEFAULT_MASTER_KEY_ALIAS = "_androidx_security_master_key_";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ID = "user_id";

    private static volatile SharedPreferences cachedPrefs;
    private static final Object LOCK = new Object();

    private final Context appContext;
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = getOrCreatePrefs(this.appContext);
    }

    private static SharedPreferences getOrCreatePrefs(Context appContext) {
        SharedPreferences existing = cachedPrefs;
        if (existing != null) {
            return existing;
        }
        synchronized (LOCK) {
            if (cachedPrefs == null) {
                SharedPreferences securePrefs = createEncryptedPrefsWithRecovery(appContext);
                if (securePrefs != null) {
                    migrateLegacyPrefs(appContext, securePrefs);
                    cachedPrefs = securePrefs;
                } else {
                    cachedPrefs = appContext.getSharedPreferences(LEGACY_PREF_NAME, Context.MODE_PRIVATE);
                }
            }
            return cachedPrefs;
        }
    }

    @SuppressWarnings("deprecation")
    private static SharedPreferences openEncryptedPrefs(Context appContext) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return EncryptedSharedPreferences.create(
                appContext,
                PREF_NAME_SECURE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    private static SharedPreferences createEncryptedPrefsWithRecovery(Context appContext) {
        try {
            return openEncryptedPrefs(appContext);
        } catch (Exception ignored) {
            // Recover if encrypted keyset file became corrupted (e.g., partial restore)
            try {
                appContext.deleteSharedPreferences(PREF_NAME_SECURE);
                return openEncryptedPrefs(appContext);
            } catch (Exception ignoredRetry) {
                // Recover if Keystore MasterKey itself was invalidated
                try {
                    KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
                    ks.load(null);
                    if (ks.containsAlias(DEFAULT_MASTER_KEY_ALIAS)) {
                        ks.deleteEntry(DEFAULT_MASTER_KEY_ALIAS);
                    }
                    appContext.deleteSharedPreferences(PREF_NAME_SECURE);
                    return openEncryptedPrefs(appContext);
                } catch (Exception ignoredFinal) {
                    return null;
                }
            }
        }
    }

    private static void migrateLegacyPrefs(Context appContext, SharedPreferences securePrefs) {
        SharedPreferences legacyAuth = appContext.getSharedPreferences(LEGACY_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences legacyApp = appContext.getSharedPreferences(LEGACY_APP_PREFS, Context.MODE_PRIVATE);

        String existingSecureToken = securePrefs.getString(KEY_ACCESS_TOKEN, null);
        if (!isValidJwtToken(existingSecureToken)) {
            String candidateToken = legacyAuth.getString(KEY_ACCESS_TOKEN, null);
            if (!isValidJwtToken(candidateToken)) {
                candidateToken = legacyApp.getString(KEY_ACCESS_TOKEN, null);
            }
            if (isValidJwtToken(candidateToken)) {
                SharedPreferences.Editor editor = securePrefs.edit();
                editor.putString(KEY_ACCESS_TOKEN, candidateToken);

                int legacyId = legacyAuth.getInt(KEY_USER_ID, 0);
                if (legacyId <= 0) {
                    legacyId = legacyApp.getInt(KEY_USER_ID, 0);
                }
                if (legacyId <= 0) {
                    legacyId = parseUserIdFromJwt(candidateToken);
                }
                if (legacyId > 0) {
                    editor.putInt(KEY_USER_ID, legacyId);
                }

                String legacyEmail = legacyAuth.getString(KEY_USER_EMAIL, null);
                if (legacyEmail == null || legacyEmail.isEmpty()) {
                    legacyEmail = legacyApp.getString(KEY_USER_EMAIL, null);
                }
                if (legacyEmail != null && !legacyEmail.isEmpty()) {
                    editor.putString(KEY_USER_EMAIL, legacyEmail);
                }

                String legacyName = legacyAuth.getString(KEY_USER_NAME, null);
                if (legacyName == null || legacyName.isEmpty()) {
                    legacyName = legacyApp.getString(KEY_USER_NAME, null);
                }
                if (legacyName != null && !legacyName.isEmpty()) {
                    editor.putString(KEY_USER_NAME, legacyName);
                }
                editor.commit();
            }
        }

        if (!legacyAuth.getAll().isEmpty()) {
            legacyAuth.edit().clear().apply();
        }
        if (legacyApp.contains(KEY_ACCESS_TOKEN)) {
            legacyApp.edit().remove(KEY_ACCESS_TOKEN).apply();
        }
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
        appContext.getSharedPreferences(LEGACY_PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        SharedPreferences legacyApp = appContext.getSharedPreferences(LEGACY_APP_PREFS, Context.MODE_PRIVATE);
        if (legacyApp.contains(KEY_ACCESS_TOKEN)) {
            legacyApp.edit().remove(KEY_ACCESS_TOKEN).apply();
        }
    }
}
