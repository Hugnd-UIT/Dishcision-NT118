package com.example.dishcision.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.dishcision.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthHelper {

    public static final int OTP_RESEND_COOLDOWN_SECONDS = 60;

    /** Per-account values cached in DishcisionPrefs; device-level settings (notifications…) are kept. */
    private static final String[] USER_CACHE_KEYS = {
            "user_id", "user_name", "user_username", "user_email", "avatar_url",
            "user_city", "is_google_login", "user_taste_preferences"
    };

    public static int getCurrentUserId(Context context) {
        if (context == null) return 0;
        int cachedId = context.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE).getInt("user_id", 0);
        if (cachedId > 0) return cachedId;
        return new TokenManager(context).getUserId();
    }

    public static void saveSessionFromAuth(Context context, AuthResponse authResponse,
                                           String fallbackEmail, String fallbackName, boolean defaultGoogle) {
        if (context == null || authResponse == null) return;
        String token = authResponse.getToken();
        UserProfileResponse.UserData user = authResponse.getData() != null ? authResponse.getData().user : null;

        int userId = user != null && user.userId > 0 ? user.userId : TokenManager.parseUserIdFromJwt(token);
        String email = user != null && user.email != null && !user.email.isEmpty() ? user.email : fallbackEmail;
        String fullName = user != null && user.fullName != null && !user.fullName.isEmpty() ? user.fullName : fallbackName;
        String username = user != null && user.username != null ? user.username : "";
        String avatarUrl = user != null && user.avatarUrl != null ? user.avatarUrl : "";
        String city = user != null && user.city != null ? user.city.trim() : "";
        boolean isGoogle = user != null ? user.isGoogleAccount : defaultGoogle;

        new TokenManager(context).saveAuthData(token, userId, email, fullName);

        android.content.SharedPreferences.Editor editor =
                context.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE).edit();
        for (String key : USER_CACHE_KEYS) editor.remove(key);
        editor.remove("access_token");
        if (userId > 0) editor.putInt("user_id", userId);
        if (email != null) editor.putString("user_email", email);
        if (fullName != null) editor.putString("user_name", fullName);
        if (!username.isEmpty()) editor.putString("user_username", username);
        if (!avatarUrl.isEmpty()) editor.putString("avatar_url", avatarUrl);
        if (!city.isEmpty()) editor.putString("user_city", city);
        if (userId > 0 && user != null && user.allergies != null) {
            editor.putStringSet("user_allergies_" + userId, new java.util.HashSet<>(user.allergies));
        }
        editor.putBoolean("is_google_login", isGoogle);
        editor.apply();
    }

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
        editor.remove("access_token");
        editor.apply();

        // Clear Google Sign-In session
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignIn.getClient(activity, gso).signOut();
        } catch (Exception ignored) {}
    }

    /** Clears revoked/expired session data and returns user to LoginActivity once. */
    public static void handleUnauthorized(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        TokenManager tm = new TokenManager(activity);
        boolean hadToken = tm.getToken() != null;
        clearLocalSession(activity);
        if (!hadToken) return;
        Toast.makeText(activity, "Phiên đăng nhập đã hết hạn hoặc bị thu hồi. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    /** Validates password policy matching backend rules; returns null if valid, otherwise error text. */
    public static String validateNewPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Vui lòng nhập mật khẩu!";
        }
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isWhitespace(c)) {
                return "Mật khẩu không được chứa khoảng trắng!";
            }
            if (c >= 'a' && c <= 'z') hasLower = true;
            else if (c >= 'A' && c <= 'Z') hasUpper = true;
            else if (c >= '0' && c <= '9') hasDigit = true;
            else hasSpecial = true;
        }
        if (password.length() < 8 || password.length() > 100) {
            return "Mật khẩu phải từ 8 đến 100 ký tự!";
        }
        if (!hasLower) return "Mật khẩu phải có ít nhất 1 chữ thường (a-z)!";
        if (!hasUpper) return "Mật khẩu phải có ít nhất 1 chữ hoa (A-Z)!";
        if (!hasDigit) return "Mật khẩu phải có ít nhất 1 chữ số (0-9)!";
        if (!hasSpecial) return "Mật khẩu phải có ít nhất 1 ký tự đặc biệt (VD: @, #, !, $...)!";
        return null;
    }

    /** Extracts positive remaining cooldown seconds from a backend 429 message; falls back otherwise. */
    public static int extractCooldownSeconds(String message, int fallbackSeconds) {
        if (message == null || message.trim().isEmpty()) return fallbackSeconds;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)\\s*giây").matcher(message);
        if (matcher.find()) {
            try {
                int parsed = Integer.parseInt(matcher.group(1));
                if (parsed > 0 && parsed <= OTP_RESEND_COOLDOWN_SECONDS) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {}
        }
        return fallbackSeconds;
    }
}
