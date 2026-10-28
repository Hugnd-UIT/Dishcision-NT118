package com.example.dishcision.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // ── AUTHENTICATION ENDPOINTS ──────────────────
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/forgot-password")
    Call<AuthResponse> forgotPassword(@Body ForgotPasswordRequest request);

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
    @GET("preferences")
    Call<Object> getPreferences(@Header("Authorization") String authHeader);

    @PUT("preferences")
    Call<Object> updatePreferences(
            @Header("Authorization") String authHeader,
            @Body Object preferences
    );

    // ── POSTS & REVIEWS ENDPOINTS ─────────────────
    @GET("posts")
    Call<Object> getFeedPosts(@Header("Authorization") String authHeader);

    @POST("posts")
    Call<Object> createPost(
            @Header("Authorization") String authHeader,
            @Body Object postRequest
    );

    @POST("reviews")
    Call<Object> createReview(
            @Header("Authorization") String authHeader,
            @Body Object reviewRequest
    );

    // ── NOTIFICATIONS ENDPOINTS ───────────────────
    @GET("notifications")
    Call<Object> getNotifications(@Header("Authorization") String authHeader);

    @PUT("notifications/{notificationId}/read")
    Call<Object> markNotificationRead(
            @Header("Authorization") String authHeader,
            @Path("notificationId") String notificationId
    );
}
