package com.example.dishcision.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Pure-Java profile diffing, server merge, and 401 session-kick helper.
 * Kept free of Android framework dependencies so core merge/diff behavior runs directly in JVM unit tests.
 */
public final class ProfileSyncHelper {

    public static final class ProfileState {
        public final String fullName;
        public final String username;
        public final String bio;
        public final String city;
        public final String avatarUrl;
        public final Set<String> preferences;

        public ProfileState(String fullName,
                            String username,
                            String bio,
                            String city,
                            String avatarUrl,
                            Collection<String> preferences) {
            this.fullName = normalizeText(fullName);
            this.username = normalizeUsername(username);
            this.bio = normalizeText(bio);
            this.city = normalizeText(city);
            this.avatarUrl = normalizeText(avatarUrl);
            this.preferences = Collections.unmodifiableSet(normalizeStringSet(preferences));
        }
    }

    private ProfileSyncHelper() {}

    public static ProfileState fromUserData(UserProfileResponse.UserData user) {
        if (user == null) {
            return new ProfileState("", "", "", "", "", null);
        }
        return new ProfileState(
                user.fullName,
                user.username,
                user.bio,
                user.city,
                user.avatarUrl,
                user.preferences
        );
    }

    public static String normalizeText(String raw) {
        return raw == null ? "" : raw.trim();
    }

    public static String normalizeUsername(String raw) {
        if (raw == null) return "";
        return raw.replace("@", "").trim();
    }

    public static Set<String> normalizeStringSet(Collection<String> items) {
        Set<String> out = new LinkedHashSet<>();
        if (items == null) return out;
        for (String item : items) {
            if (item == null) continue;
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }

    public static Set<String> normalizeLowerStringSet(Collection<String> items) {
        Set<String> out = new LinkedHashSet<>();
        if (items == null) return out;
        for (String item : items) {
            if (item == null) continue;
            String trimmed = item.trim().toLowerCase(Locale.ROOT);
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }

    public static boolean setsEqual(Collection<String> left, Collection<String> right) {
        return normalizeStringSet(left).equals(normalizeStringSet(right));
    }

    /**
     * Merges a newly fetched server field onto the UI without clobbering active local edits.
     */
    public static String mergeTextField(String currentUiValue, String newServerValue, boolean editedByUser) {
        if (editedByUser) {
            return currentUiValue != null ? currentUiValue : "";
        }
        return normalizeText(newServerValue);
    }

    /**
     * Merges a newly fetched server tag set onto the working set without clobbering active local edits.
     */
    public static Set<String> mergeSetField(Set<String> currentWorkingSet,
                                            Collection<String> newServerSet,
                                            boolean editedByUser) {
        if (editedByUser) {
            return normalizeStringSet(currentWorkingSet);
        }
        return normalizeStringSet(newServerSet);
    }

    /**
     * Builds a minimal partial PUT /users/me payload containing ONLY fields that differ from baseline.
     * Any untouched field is omitted so concurrent or earlier server changes from another session are never reverted.
     */
    public static Map<String, Object> buildProfileDiffPayload(ProfileState baseline, ProfileState current) {
        Map<String, Object> payload = new HashMap<>();
        if (current == null) return payload;
        ProfileState base = baseline != null ? baseline : new ProfileState("", "", "", "", "", null);

        if (!current.fullName.equals(base.fullName)) {
            payload.put("fullName", current.fullName);
        }
        if (!current.username.equals(base.username)) {
            payload.put("username", current.username);
        }
        if (!current.bio.equals(base.bio)) {
            payload.put("bio", current.bio);
        }
        if (!current.city.equals(base.city)) {
            payload.put("city", current.city);
        }
        if (!current.avatarUrl.equals(base.avatarUrl) && !current.avatarUrl.isEmpty()) {
            payload.put("avatarUrl", current.avatarUrl);
        }
        if (!current.preferences.equals(base.preferences)) {
            payload.put("preferences", new ArrayList<>(current.preferences));
        }
        return payload;
    }

    /**
     * Builds a partial PUT /preferences/me payload containing only modified sections (preferences and/or allergies).
     */
    public static Map<String, Object> buildPreferencesDiffPayload(Set<String> baselineTastes,
                                                                  Set<String> currentTastes,
                                                                  Set<String> baselineAllergies,
                                                                  Set<String> currentAllergies) {
        Map<String, Object> payload = new HashMap<>();
        Set<String> baseT = normalizeStringSet(baselineTastes);
        Set<String> currT = normalizeStringSet(currentTastes);
        if (!currT.equals(baseT)) {
            payload.put("preferences", new ArrayList<>(currT));
        }

        Set<String> baseA = normalizeLowerStringSet(baselineAllergies);
        Set<String> currA = normalizeLowerStringSet(currentAllergies);
        if (!currA.equals(baseA)) {
            payload.put("allergies", new ArrayList<>(currA));
        }
        return payload;
    }

    public static String mapPrivacyKeyToApiField(String settingKey) {
        if (settingKey == null) return "newPosts";
        switch (settingKey) {
            case "profile_info":
                return "profileInfo";
            case "following_list":
                return "followingList";
            case "find_email":
                return "findEmail";
            case "friend_requests":
                return "friendRequests";
            case "messages":
                return "messages";
            case "comments":
                return "comments";
            case "new_posts":
            default:
                return "newPosts";
        }
    }

    public static Map<String, String> buildPrivacySingleFieldPayload(String settingKey, String selectedValue) {
        Map<String, String> payload = new HashMap<>();
        String apiField = mapPrivacyKeyToApiField(settingKey);
        payload.put(apiField, normalizeText(selectedValue));
        return payload;
    }

    public static boolean shouldTriggerGlobalLogoutOn401(String requestPath,
                                                         String authHeader,
                                                         String errorCode,
                                                         String errorMessage) {
        if (authHeader == null || !authHeader.trim().startsWith("Bearer ")) {
            return false;
        }
        String path = requestPath != null ? requestPath.toLowerCase(Locale.ROOT) : "";
        if (path.endsWith("/auth/login")
                || path.endsWith("/auth/login/verify-2fa")
                || path.endsWith("/auth/login/resend-2fa")
                || path.endsWith("/auth/register")
                || path.endsWith("/auth/google")
                || path.endsWith("/auth/forgot-password")
                || path.endsWith("/auth/verify-otp")
                || path.endsWith("/auth/reset-password")) {
            return false;
        }
        if (path.endsWith("/auth/change-password")) {
            if ("SESSION_REPLACED".equals(errorCode)) {
                return true;
            }
            return !"Current password is incorrect".equals(normalizeText(errorMessage));
        }
        return true;
    }

    public static String extractJsonField(String rawBody, String fieldName) {
        if (rawBody == null || rawBody.trim().isEmpty() || fieldName == null) return "";
        try {
            JsonObject obj = JsonParser.parseString(rawBody).getAsJsonObject();
            if (obj.has(fieldName) && !obj.get(fieldName).isJsonNull()) {
                return obj.get(fieldName).getAsString().trim();
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}
