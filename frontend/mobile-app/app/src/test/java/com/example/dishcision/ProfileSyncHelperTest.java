package com.example.dishcision;

import com.example.dishcision.network.ProfileSyncHelper;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProfileSyncHelperTest {

    @Test
    public void buildProfileDiffPayload_onlyIncludesModifiedFields() {
        ProfileSyncHelper.ProfileState baseline = new ProfileSyncHelper.ProfileState(
                "Thanh Tien",
                "thanhtien21007",
                "Bio goc",
                "TP.HCM",
                "/uploads/images/a.jpg",
                Arrays.asList("Chua cay", "Món nước")
        );

        ProfileSyncHelper.ProfileState editedCityOnly = new ProfileSyncHelper.ProfileState(
                "Thanh Tien",
                "@thanhtien21007",
                "Bio goc",
                "Hà Nội",
                "/uploads/images/a.jpg",
                Arrays.asList("Chua cay", "Món nước")
        );

        Map<String, Object> diff = ProfileSyncHelper.buildProfileDiffPayload(baseline, editedCityOnly);
        assertEquals(1, diff.size());
        assertEquals("Hà Nội", diff.get("city"));
        assertFalse(diff.containsKey("fullName"));
        assertFalse(diff.containsKey("username"));
        assertFalse(diff.containsKey("bio"));
        assertFalse(diff.containsKey("avatarUrl"));
        assertFalse(diff.containsKey("preferences"));
    }

    @Test
    public void buildProfileDiffPayload_supportsClearingBioCityAndPreferences() {
        ProfileSyncHelper.ProfileState baseline = new ProfileSyncHelper.ProfileState(
                "Thanh Tien",
                "thanhtien21007",
                "Bio cu",
                "Đà Nẵng",
                "/uploads/images/a.jpg",
                Collections.singletonList("Chua cay")
        );

        ProfileSyncHelper.ProfileState cleared = new ProfileSyncHelper.ProfileState(
                "Thanh Tien",
                "thanhtien21007",
                "",
                "   ",
                "/uploads/images/a.jpg",
                Collections.emptyList()
        );

        Map<String, Object> diff = ProfileSyncHelper.buildProfileDiffPayload(baseline, cleared);
        assertEquals(3, diff.size());
        assertEquals("", diff.get("bio"));
        assertEquals("", diff.get("city"));
        assertEquals(Collections.emptyList(), diff.get("preferences"));

        Map<String, Object> emptyDiff = ProfileSyncHelper.buildProfileDiffPayload(baseline, baseline);
        assertTrue(emptyDiff.isEmpty());
    }

    @Test
    public void buildPreferencesDiffPayload_omitsUntouchedSection() {
        Set<String> baseTastes = new LinkedHashSet<>(Arrays.asList("Chua cay", "Đậm đà"));
        Set<String> sameTastes = new LinkedHashSet<>(Arrays.asList("Đậm đà", "Chua cay"));
        Set<String> baseAllergies = new LinkedHashSet<>(Collections.singletonList("peanuts"));
        Set<String> newAllergies = new LinkedHashSet<>(Arrays.asList("PEANUTS", "milk"));

        Map<String, Object> diff = ProfileSyncHelper.buildPreferencesDiffPayload(
                baseTastes, sameTastes, baseAllergies, newAllergies
        );
        assertFalse(diff.containsKey("preferences"));
        assertTrue(diff.containsKey("allergies"));
        @SuppressWarnings("unchecked")
        List<String> savedAllergies = (List<String>) diff.get("allergies");
        assertEquals(Arrays.asList("peanuts", "milk"), savedAllergies);

        Set<String> updatedTastes = new LinkedHashSet<>(Arrays.asList("Chua cay", "Món nước"));
        Map<String, Object> tastesOnlyDiff = ProfileSyncHelper.buildPreferencesDiffPayload(
                baseTastes, updatedTastes, baseAllergies, new LinkedHashSet<>(Collections.singletonList("  Peanuts "))
        );
        assertTrue(tastesOnlyDiff.containsKey("preferences"));
        assertFalse(tastesOnlyDiff.containsKey("allergies"));
    }

    @Test
    public void mergeFields_updatesUneditedFromServerAndPreservesLocalEdits() {
        assertEquals("Server City", ProfileSyncHelper.mergeTextField("Old Local", "Server City", false));
        assertEquals("User Typed City", ProfileSyncHelper.mergeTextField("User Typed City", "Server City", true));

        Set<String> working = new LinkedHashSet<>(Arrays.asList("LocalTag"));
        Set<String> server = new LinkedHashSet<>(Arrays.asList("ServerTag1", "ServerTag2"));
        assertEquals(server, ProfileSyncHelper.mergeSetField(working, server, false));
        assertEquals(working, ProfileSyncHelper.mergeSetField(working, server, true));
    }

    @Test
    public void mapPrivacyKeyToApiField_buildsSingleFieldPayload() {
        assertEquals("newPosts", ProfileSyncHelper.mapPrivacyKeyToApiField("new_posts"));
        assertEquals("profileInfo", ProfileSyncHelper.mapPrivacyKeyToApiField("profile_info"));
        assertEquals("followingList", ProfileSyncHelper.mapPrivacyKeyToApiField("following_list"));
        assertEquals("findEmail", ProfileSyncHelper.mapPrivacyKeyToApiField("find_email"));
        assertEquals("friendRequests", ProfileSyncHelper.mapPrivacyKeyToApiField("friend_requests"));
        assertEquals("messages", ProfileSyncHelper.mapPrivacyKeyToApiField("messages"));
        assertEquals("comments", ProfileSyncHelper.mapPrivacyKeyToApiField("comments"));

        Map<String, String> payload = ProfileSyncHelper.buildPrivacySingleFieldPayload("profile_info", "only_me");
        assertEquals(1, payload.size());
        assertEquals("only_me", payload.get("profileInfo"));
    }

    @Test
    public void shouldTriggerGlobalLogoutOn401_distinguishesKickedSessionVsAuthForms() {
        String bearer = "Bearer abc.def.ghi";

        assertTrue(ProfileSyncHelper.shouldTriggerGlobalLogoutOn401(
                "/api/v1/users/me", bearer, "SESSION_REPLACED", "Tài khoản đã được đăng nhập trên thiết bị khác."
        ));
        assertTrue(ProfileSyncHelper.shouldTriggerGlobalLogoutOn401(
                "/api/v1/auth/session-status", bearer, "", "Invalid token"
        ));

        assertFalse(ProfileSyncHelper.shouldTriggerGlobalLogoutOn401(
                "/api/v1/users/me", null, "SESSION_REPLACED", "No header"
        ));
        assertFalse(ProfileSyncHelper.shouldTriggerGlobalLogoutOn401(
                "/api/v1/auth/login", bearer, "", "Invalid credentials"
        ));
        assertFalse(ProfileSyncHelper.shouldTriggerGlobalLogoutOn401(
                "/api/v1/auth/login/verify-2fa", bearer, "", "Invalid code"
        ));
        assertFalse(ProfileSyncHelper.shouldTriggerGlobalLogoutOn401(
                "/api/v1/auth/change-password", bearer, "", "Current password is incorrect"
        ));
        assertTrue(ProfileSyncHelper.shouldTriggerGlobalLogoutOn401(
                "/api/v1/auth/change-password", bearer, "SESSION_REPLACED", "Current password is incorrect"
        ));
    }

    @Test
    public void extractJsonField_handlesValidAndMalformedJson() {
        String json = "{\"success\":false,\"code\":\"SESSION_REPLACED\",\"message\":\"Logged in elsewhere\"}";
        assertEquals("SESSION_REPLACED", ProfileSyncHelper.extractJsonField(json, "code"));
        assertEquals("Logged in elsewhere", ProfileSyncHelper.extractJsonField(json, "message"));
        assertEquals("", ProfileSyncHelper.extractJsonField(json, "missing"));
        assertEquals("", ProfileSyncHelper.extractJsonField("not-json", "code"));
        assertEquals("", ProfileSyncHelper.extractJsonField(null, "code"));
    }
}
