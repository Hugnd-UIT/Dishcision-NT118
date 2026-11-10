package com.example.dishcision.network;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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
    Call<UserProfileResponse> getMyProfile(@Header("Authorization") String authHeader);

    @PUT("users/me")
    Call<AuthResponse> updateMyProfile(
            @Header("Authorization") String authHeader,
            @Body Object profileData
    );

    @GET("users/{userId}")
    Call<UserProfileResponse> getUserProfile(@Path("userId") String userId);

    @GET("users/me/posts")
    Call<FeedPostsResponse> getMyPosts(@Header("Authorization") String authHeader);

    @GET("users/{userId}/posts")
    Call<FeedPostsResponse> getUserPosts(
            @Header("Authorization") String authHeader,
            @Path("userId") String userId
    );

    @POST("users/{userId}/follow")
    Call<AuthResponse> followUser(
            @Header("Authorization") String authHeader,
            @Path("userId") String userId
    );

    @DELETE("users/{userId}/follow")
    Call<AuthResponse> unfollowUser(
            @Header("Authorization") String authHeader,
            @Path("userId") String userId
    );

    @GET("users/{userId}/followers")
    Call<FollowersListResponse> getFollowers(
            @Header("Authorization") String authHeader,
            @Path("userId") String userId
    );

    @GET("users/{userId}/following")
    Call<FollowersListResponse> getFollowing(
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

    @GET("users/me/blocked")
    Call<AccountsListResponse> getBlockedAccounts(@Header("Authorization") String authHeader);

    @POST("users/me/unblock")
    Call<AccountsListResponse> unblockAccount(
            @Header("Authorization") String authHeader,
            @Body AccountActionRequest request
    );

    @GET("help/inbox")
    Call<HelpInboxResponse> getHelpInbox(@Header("Authorization") String authHeader);

    @GET("help/tickets/{ticketId}")
    Call<HelpDetailResponse> getTicketDetail(@Header("Authorization") String authHeader, @Path("ticketId") String ticketId);

    @GET("auth/sessions")
    Call<SessionsResponse> getSessions(@Header("Authorization") String authHeader);

    @POST("auth/sessions/revoke")
    Call<SessionsResponse> revokeSession(@Header("Authorization") String authHeader, @Body RevokeSessionRequest request);

    // ── POSTS & REVIEWS & MEDIA UPLOAD ENDPOINTS ──
    @GET("posts")
    Call<FeedPostsResponse> getFeedPosts(@Header("Authorization") String authHeader);

    @GET("users/me/saved-posts")
    Call<FeedPostsResponse> getSavedPosts(@Header("Authorization") String authHeader, @Query("limit") int limit);

    @GET("restaurants/following")
    Call<FollowingRestaurantsResponse> getFollowingRestaurants(@Header("Authorization") String authHeader);

    @GET("reviews")
    Call<ReviewsResponse> getReviews(
            @Query("restaurantName") String restaurantName,
            @Query("rating") Integer rating,
            @Query("authorId") String authorId
    );

    @POST("posts/{postId}/save")
    Call<Object> savePost(
            @Header("Authorization") String authHeader,
            @Path("postId") int postId
    );

    @DELETE("posts/{postId}/save")
    Call<Object> unsavePost(
            @Header("Authorization") String authHeader,
            @Path("postId") int postId
    );

    @GET("users/me/privacy")
    Call<PrivacySettingsResponse> getPrivacySettings(@Header("Authorization") String authHeader);

    @PUT("users/me/privacy")
    Call<PrivacySettingsResponse> updatePrivacySettings(@Header("Authorization") String authHeader, @Body PrivacySettingsModel request);

    @Multipart
    @POST("upload")
    Call<UploadResponse> uploadSingleImage(
            @Header("Authorization") String authHeader,
            @Part MultipartBody.Part file
    );

    @Multipart
    @POST("posts/upload-media")
    Call<UploadResponse> uploadMedia(
            @Header("Authorization") String authHeader,
            @Part MultipartBody.Part file
    );

    @POST("reviews")
    Call<Object> createReview(
            @Header("Authorization") String authHeader,
            @Body Object reviewRequest
    );

    @POST("reports")
    Call<ReportContentResponse> createReport(@Header("Authorization") String authHeader, @Body ReportContentRequest request);

    @GET("users/me/restricted")
    Call<AccountsListResponse> getRestrictedAccounts(@Header("Authorization") String authHeader);

    @POST("users/me/unrestrict")
    Call<AccountsListResponse> unrestrictAccount(@Header("Authorization") String authHeader, @Body AccountActionRequest request);

    @GET("auth/2fa/status")
    Call<TwoFactorStatusResponse> get2FAStatus(@Header("Authorization") String authHeader);

    @POST("auth/2fa/disable")
    Call<TwoFactorStatusResponse> disable2FA(@Header("Authorization") String authHeader);

    @POST("auth/2fa/verify")
    Call<TwoFactorStatusResponse> verify2FA(@Header("Authorization") String authHeader, @Body TwoFactorVerifyRequest request);

    @POST("help/tickets")
    Call<HelpCreateResponse> createTicket(@Header("Authorization") String authHeader, @Body HelpCreateRequest request);

    @POST("auth/logout")
    Call<Object> logout(@Header("Authorization") String authHeader);

    @DELETE("users/me")
    Call<Object> deleteMyAccount(@Header("Authorization") String authHeader);

    // ── NOTIFICATIONS ENDPOINTS ───────────────────
    @GET("notifications")
    Call<Object> getNotifications(@Header("Authorization") String authHeader);

    @PATCH("notifications/{notificationId}/read")
    Call<Object> markNotificationRead(
            @Header("Authorization") String authHeader,
            @Path("notificationId") String notificationId
    );
}
