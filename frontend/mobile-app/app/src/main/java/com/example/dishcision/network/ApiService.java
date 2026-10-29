package com.example.dishcision.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

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

    @GET("privacy")
    Call<PrivacySettingsResponse> getPrivacySettings(@Header("Authorization") String authHeader);

    @POST("privacy/update")
    Call<PrivacySettingsResponse> updatePrivacySettings(@Header("Authorization") String authHeader, @Body PrivacySettingsModel request);

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
}
