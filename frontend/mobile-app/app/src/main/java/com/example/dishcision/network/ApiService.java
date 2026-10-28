package com.example.dishcision.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

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

    @GET("auth/2fa/status")
    Call<TwoFactorStatusResponse> get2FAStatus(@Header("Authorization") String authHeader);

    @POST("auth/2fa/verify")
    Call<TwoFactorStatusResponse> verify2FA(
            @Header("Authorization") String authHeader,
            @Body TwoFactorVerifyRequest request
    );

    @POST("auth/2fa/disable")
    Call<TwoFactorStatusResponse> disable2FA(@Header("Authorization") String authHeader);

    @GET("auth/sessions")
    Call<SessionsResponse> getSessions(@Header("Authorization") String authHeader);

    @POST("auth/sessions/revoke")
    Call<SessionsResponse> revokeSession(
            @Header("Authorization") String authHeader,
            @Body RevokeSessionRequest request
    );

    @GET("users/me/privacy")
    Call<PrivacySettingsResponse> getPrivacySettings(@Header("Authorization") String authHeader);

    @PUT("users/me/privacy")
    Call<PrivacySettingsResponse> updatePrivacySettings(
            @Header("Authorization") String authHeader,
            @Body PrivacySettingsModel request
    );

    @GET("users/me/blocked")
    Call<AccountsListResponse> getBlockedAccounts(@Header("Authorization") String authHeader);

    @POST("users/me/unblock")
    Call<AccountsListResponse> unblockAccount(
            @Header("Authorization") String authHeader,
            @Body AccountActionRequest request
    );

    @GET("users/me/restricted")
    Call<AccountsListResponse> getRestrictedAccounts(@Header("Authorization") String authHeader);

    @POST("users/me/unrestrict")
    Call<AccountsListResponse> unrestrictAccount(
            @Header("Authorization") String authHeader,
            @Body AccountActionRequest request
    );

    @GET("help/inbox")
    Call<HelpInboxResponse> getHelpInbox(@Header("Authorization") String authHeader);

    @GET("help/tickets/{ticketId}")
    Call<HelpDetailResponse> getTicketDetail(
            @Header("Authorization") String authHeader,
            @Path("ticketId") String ticketId
    );

    @POST("help/tickets")
    Call<HelpCreateResponse> createTicket(
            @Header("Authorization") String authHeader,
            @Body HelpCreateRequest request
    );

    @POST("reports")
    Call<ReportContentResponse> createReport(
            @Header("Authorization") String authHeader,
            @Body ReportContentRequest request
    );
}


