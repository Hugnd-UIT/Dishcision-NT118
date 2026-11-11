package com.example.dishcision;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public final class BottomNavHelper {

    public static final int TAB_NONE = 0;
    public static final int TAB_HOME = 1;
    public static final int TAB_DECIDE = 2;
    public static final int TAB_MESSAGES = 3;
    public static final int TAB_PROFILE = 4;

    private BottomNavHelper() {}

    public static void bind(Activity activity, int activeTab) {
        bind(activity, activeTab, null, null);
    }

    public static void bind(Activity activity, int activeTab, Runnable onHomeReselect, Runnable onCreateOverride) {
        if (activity == null) return;

        int activeColor = ContextCompat.getColor(activity, R.color.text_dark);
        int inactiveColor = ContextCompat.getColor(activity, R.color.text_gray);

        applyTabLabelStyle(activity.findViewById(R.id.tvNavHome), activeTab == TAB_HOME, activeColor, inactiveColor);
        applyTabLabelStyle(activity.findViewById(R.id.tvNavDecide), activeTab == TAB_DECIDE, activeColor, inactiveColor);
        applyTabLabelStyle(activity.findViewById(R.id.tvNavMessages), activeTab == TAB_MESSAGES, activeColor, inactiveColor);
        applyTabLabelStyle(activity.findViewById(R.id.tvNavProfile), activeTab == TAB_PROFILE, activeColor, inactiveColor);

        View navHome = activity.findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (activeTab == TAB_HOME) {
                    if (onHomeReselect != null) onHomeReselect.run();
                    return;
                }
                Intent intent = new Intent(activity, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.finish();
            });
        }

        View navDecide = activity.findViewById(R.id.navDecide);
        if (navDecide != null) {
            navDecide.setOnClickListener(v -> {
                if (activeTab != TAB_DECIDE) {
                    activity.startActivity(new Intent(activity, DecideActivity.class));
                }
            });
        }

        View navCreate = activity.findViewById(R.id.navCreate);
        if (navCreate != null) {
            navCreate.setOnClickListener(v -> {
                if (onCreateOverride != null) {
                    onCreateOverride.run();
                } else {
                    activity.startActivity(new Intent(activity, CreatePostActivity.class));
                }
            });
        }

        View navMessages = activity.findViewById(R.id.navMessages);
        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                if (activeTab != TAB_MESSAGES) {
                    activity.startActivity(new Intent(activity, MessagesInboxActivity.class));
                }
            });
        }

        View navProfile = activity.findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (activeTab != TAB_PROFILE) {
                    activity.startActivity(new Intent(activity, ProfileActivity.class));
                }
            });
        }
    }

    private static void applyTabLabelStyle(TextView label, boolean active, int activeColor, int inactiveColor) {
        if (label == null) return;
        label.setTextColor(active ? activeColor : inactiveColor);
        label.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }
}
