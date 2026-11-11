package com.example.dishcision;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.dishcision.network.AuthHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public final class ChatPrefsHelper {

    private static final String PREFS_NAME = "DishcisionChatPrefs";

    private ChatPrefsHelper() {}

    private static String scope(Context context, int partnerId) {
        int myId = AuthHelper.getCurrentUserId(context);
        return myId + "_" + partnerId;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isMuted(Context context, int partnerId) {
        return prefs(context).getBoolean("muted_" + scope(context, partnerId), false);
    }

    public static void setMuted(Context context, int partnerId, boolean muted) {
        prefs(context).edit().putBoolean("muted_" + scope(context, partnerId), muted).apply();
    }

    public static String getThemeName(Context context, int partnerId) {
        return prefs(context).getString("theme_name_" + scope(context, partnerId), "Cam Ớt Cay");
    }

    public static int getThemeColor(Context context, int partnerId) {
        return prefs(context).getInt("theme_color_" + scope(context, partnerId), 0xFFFF5A36);
    }

    public static void setTheme(Context context, int partnerId, String name, int color) {
        prefs(context).edit()
                .putString("theme_name_" + scope(context, partnerId), name)
                .putInt("theme_color_" + scope(context, partnerId), color)
                .apply();
    }

    public static String getQuickEmoji(Context context, int partnerId) {
        return prefs(context).getString("emoji_" + scope(context, partnerId), "🍜");
    }

    public static void setQuickEmoji(Context context, int partnerId, String emoji) {
        prefs(context).edit().putString("emoji_" + scope(context, partnerId), emoji).apply();
    }

    public static String getPartnerNickname(Context context, int partnerId) {
        return prefs(context).getString("nick_partner_" + scope(context, partnerId), "");
    }

    public static void setPartnerNickname(Context context, int partnerId, String nick) {
        prefs(context).edit().putString("nick_partner_" + scope(context, partnerId), nick != null ? nick.trim() : "").apply();
    }

    public static String getSelfNickname(Context context, int partnerId) {
        return prefs(context).getString("nick_self_" + scope(context, partnerId), "");
    }

    public static void setSelfNickname(Context context, int partnerId, String nick) {
        prefs(context).edit().putString("nick_self_" + scope(context, partnerId), nick != null ? nick.trim() : "").apply();
    }

    public static Set<String> getPinnedMessageIds(Context context, int partnerId) {
        Set<String> stored = prefs(context).getStringSet("pinned_" + scope(context, partnerId), null);
        return stored != null ? new HashSet<>(stored) : new HashSet<>();
    }

    public static boolean isMessagePinned(Context context, int partnerId, int messageId) {
        return getPinnedMessageIds(context, partnerId).contains(String.valueOf(messageId));
    }

    public static void togglePinMessage(Context context, int partnerId, int messageId) {
        Set<String> set = getPinnedMessageIds(context, partnerId);
        String key = String.valueOf(messageId);
        if (set.contains(key)) {
            set.remove(key);
        } else {
            set.add(key);
        }
        prefs(context).edit().putStringSet("pinned_" + scope(context, partnerId), set).apply();
    }

    public static void removePinnedMessage(Context context, int partnerId, int messageId) {
        Set<String> set = getPinnedMessageIds(context, partnerId);
        set.remove(String.valueOf(messageId));
        prefs(context).edit().putStringSet("pinned_" + scope(context, partnerId), set).apply();
    }

    public static String formatShortTime(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) return "";
        try {
            String normalized = isoString.replaceFirst("\\.\\d+", "");
            SimpleDateFormat parser = new SimpleDateFormat(
                    normalized.endsWith("Z") ? "yyyy-MM-dd'T'HH:mm:ss'Z'" : "yyyy-MM-dd'T'HH:mm:ss",
                    Locale.US);
            if (normalized.endsWith("Z")) {
                parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            Date parsed = parser.parse(normalized);
            if (parsed == null) return "";
            long diffMs = System.currentTimeMillis() - parsed.getTime();
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            if (diffMs >= 0 && diffMs < 24L * 3600 * 1000) {
                return timeFmt.format(parsed);
            }
            return new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(parsed);
        } catch (Exception e) {
            return "";
        }
    }
}

