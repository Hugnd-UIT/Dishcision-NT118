package com.example.dishcision.network;

import android.content.Context;
import android.content.SharedPreferences;

/** Device-level notification choices, shared by the Settings screen and the Notification settings screen. */
public final class NotificationPrefs {

    private static final String PREFS = "DishcisionPrefs";

    public static final String PUSH = "notif_push";
    public static final String EMAIL_PROMOS = "notif_email_promos";
    public static final String INTERACTIONS = "notif_interactions";
    public static final String MESSAGES = "notif_messages";
    public static final String FOLLOWING = "notif_following";
    public static final String OFFERS = "notif_offers";

    private NotificationPrefs() {}

    public static boolean get(Context context, String key) {
        return prefs(context).getBoolean(key, defaultFor(key));
    }

    public static void set(Context context, String key, boolean value) {
        prefs(context).edit().putBoolean(key, value).apply();
    }

    private static boolean defaultFor(String key) {
        // Marketing channels are opt-in; everything else is on by default
        return !(EMAIL_PROMOS.equals(key) || OFFERS.equals(key));
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
