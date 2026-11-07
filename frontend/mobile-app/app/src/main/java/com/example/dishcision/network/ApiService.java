package com.example.dishcision.network;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ── AUTHENTICATION ENDPOINTS ──────────────────
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/forgot-password")
    Call<AuthResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/verify-otp")
    Call<AuthResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auth/reset-password")
    Call<AuthResponse> resetPassword(@Body ResetPasswordRequest request);

    @POST("auth/change-password")
    Call<AuthResponse> changePassword(
            @Header("Authorization") String authHeader,
            @Body ChangePasswordRequest request
    );

    // ── USER PROFILE ENDPOINTS ─────────────────────
    @GET("users/me")
    Call<AuthResponse> getMyProfile(@Header("Authorization") String authHeader);

    @PUT("users/me")
    Call<AuthResponse> updateMyProfile(
            @Header("Authorization") String authHeader,
            @Body Object profileData
    );

    @GET("users/{userId}")
    Call<AuthResponse> getUserProfile(@Path("userId") String userId);

    @POST("users/{userId}/follow")
    Call<AuthResponse> followUser(
            @Header("Authorization") String authHeader,
            @Path("userId") String userId
    );

    // ── TASTE PREFERENCES ENDPOINTS ───────────────
    @GET("preferences/me")
    Call<Object> getPreferences(@Header("Authorization") String authHeader);

    @PUT("preferences/me")
    Call<Object> updatePreferences(
            @Header("Authorization") String authHeader,
            @Body Object preferences
    );

    // ── AI FOOD DECIDE ENDPOINTS ──────────────────
    @POST("ai/food-decide")
    Call<FoodDecideResponse> foodDecide(@Body FoodDecideRequest request);

    @GET("accounts/blocked")
    Call<AccountsListResponse> getBlockedAccounts(@Header("Authorization") String authHeader);

    @POST("accounts/unblock")
    Call<AccountsListResponse> unblockAccount(
            @Header("Authorization") String authHeader,
            @Body AccountActionRequest request
    );

    @GET("help/inbox")
    Call<HelpInboxResponse> getHelpInbox(@Header("Authorization") String authHeader);

    @GET("help/ticket")
    Call<HelpDetailResponse> getTicketDetail(@Header("Authorization") String authHeader, @Query("ticketId") String ticketId);

    @GET("sessions")
    Call<SessionsResponse> getSessions(@Header("Authorization") String authHeader);

    @POST("sessions/revoke")
    Call<SessionsResponse> revokeSession(@Header("Authorization") String authHeader, @Body RevokeSessionRequest request);

    // ── POSTS & REVIEWS & MEDIA UPLOAD ENDPOINTS ──
    @GET("posts")
    Call<Object> getFeedPosts(@Header("Authorization") String authHeader);

    @GET("privacy")
    Call<PrivacySettingsResponse> getPrivacySettings(@Header("Authorization") String authHeader);

    @POST("privacy/update")
    Call<PrivacySettingsResponse> updatePrivacySettings(@Header("Authorization") String authHeader, @Body PrivacySettingsModel request);

    @Multipart
    @POST("posts/upload-media")
    Call<Object> uploadMedia(
            @Header("Authorization") String authHeader,
            @Part MultipartBody.Part file
    );

    @POST("reviews")
    Call<Object> createReview(
            @Header("Authorization") String authHeader,
            @Body Object reviewRequest
    );

    @POST("report")
    Call<ReportContentResponse> createReport(@Header("Authorization") String authHeader, @Body ReportContentRequest request);

    @GET("accounts/restricted")
    Call<AccountsListResponse> getRestrictedAccounts(@Header("Authorization") String authHeader);

    @POST("accounts/unrestrict")
    Call<AccountsListResponse> unrestrictAccount(@Header("Authorization") String authHeader, @Body AccountActionRequest request);

    @GET("2fa/status")
    Call<TwoFactorStatusResponse> get2FAStatus(@Header("Authorization") String authHeader);

    @POST("2fa/disable")
    Call<TwoFactorStatusResponse> disable2FA(@Header("Authorization") String authHeader);

    @POST("2fa/verify")
    Call<TwoFactorStatusResponse> verify2FA(@Header("Authorization") String authHeader, @Body TwoFactorVerifyRequest request);

    @POST("help/ticket")
    Call<HelpCreateResponse> createTicket(@Header("Authorization") String authHeader, @Body HelpCreateRequest request);

    // ── NOTIFICATIONS ENDPOINTS ───────────────────
    @GET("notifications")
    Call<Object> getNotifications(@Header("Authorization") String authHeader);

    @PATCH("notifications/{notificationId}/read")
    Call<Object> markNotificationRead(
            @Header("Authorization") String authHeader,
            @Path("notificationId") String notificationId
    );
}
